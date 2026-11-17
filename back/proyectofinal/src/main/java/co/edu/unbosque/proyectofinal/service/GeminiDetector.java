package co.edu.unbosque.proyectofinal.service;

import co.edu.unbosque.proyectofinal.dto.AiVerdictDTO;
import co.edu.unbosque.proyectofinal.dto.FileContentDTO;
import co.edu.unbosque.proyectofinal.dto.GeminiCandidateDTO;
import co.edu.unbosque.proyectofinal.dto.GeminiPartDTO;
import co.edu.unbosque.proyectofinal.dto.GeminiResponseDTO;
import co.edu.unbosque.proyectofinal.dto.ImagePartDTO;
import co.edu.unbosque.proyectofinal.exception.AiProviderException;
import co.edu.unbosque.proyectofinal.model.AnalyzedFile.Category;
import co.edu.unbosque.proyectofinal.util.AiPromptUtil;
import co.edu.unbosque.proyectofinal.util.RateLimiter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Cliente REST de Google Gemini. Es la IA que recibe los PDF, audios y videos originales, porque
 * su API los lee de forma nativa.
 */
@Service
public class GeminiDetector implements AiDetector {

  /** Nombre visible del proveedor. */
  public static final String PROVEEDOR = "Gemini";

  /** Tamaño máximo de archivo que se adjunta en línea (la API acepta hasta 20 MB por petición). */
  private static final long MAX_ADJUNTO = 15L * 1024 * 1024;

  /** Limitador de ritmo de peticiones. */
  private final RateLimiter limiter;

  /** API key. */
  private final String apiKey;

  /** URL base de los modelos. */
  private final String url;

  /** Modelo a usar. */
  private final String modelo;

  /** Tiempo máximo de respuesta. */
  private final int timeout;

  /**
   * Spring inyecta aquí las dependencias.
   *
   * @param apiKey API key de Google AI Studio
   * @param url URL base de los modelos
   * @param modelo modelo a usar
   * @param intervaloMs milisegundos minimos entre peticiones
   * @param timeout tiempo máximo de respuesta en segundos
   */
  public GeminiDetector(
      @Value("${ai.gemini.api-key}") String apiKey,
      @Value("${ai.gemini.url}") String url,
      @Value("${ai.gemini.model}") String modelo,
      @Value("${ai.gemini.min-interval-ms:13000}") long intervaloMs,
      @Value("${ai.timeout-seconds:90}") int timeout) {
    this.apiKey = apiKey;
    this.url = url;
    this.modelo = modelo;
    this.limiter = new RateLimiter(intervaloMs);
    this.timeout = timeout;
  }

  /**
   * Devuelve el nombre del proveedor.
   *
   * @return nombre del proveedor
   */
  @Override
  public String getProveedor() {
    return PROVEEDOR;
  }

  /**
   * Devuelve el mismo modelo para todos los tipos.
   *
   * @param contenido archivo preparado
   * @return el mismo modelo para todos los tipos
   */
  @Override
  public String modeloPara(FileContentDTO contenido) {
    return modelo;
  }

  /**
   * Envía el contenido a Gemini con el archivo original adjunto cuando aplica.
   *
   * @param contenido archivo preparado
   * @return veredicto interpretado
   * @throws AiProviderException si la API falla
   * @throws InterruptedException si se interrumpe el hilo
   */
  @Override
  public AiVerdictDTO analizar(FileContentDTO contenido) throws AiProviderException, InterruptedException {
    if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("CAMBIAR")) {
      throw new AiProviderException(PROVEEDOR, 0, "API key sin configurar");
    }
    boolean adjuntaOriginal = adjuntaOriginal(contenido);
    String cuerpo = construirCuerpo(contenido, adjuntaOriginal);
    String endpoint = url + "/" + modelo + ":generateContent";

    GeminiResponseDTO respuesta = enviar(endpoint, cuerpo);
    if (respuesta.getStatuscode() == 429 || respuesta.getStatuscode() >= 500) {
      Thread.sleep(6000);
      respuesta = enviar(endpoint, cuerpo);
    }
    if (respuesta.getStatuscode() != 200) {
      throw new AiProviderException(PROVEEDOR, respuesta.getStatuscode(), recortar(respuesta.getCuerpoError()));
    }
    String texto = extraerTexto(respuesta);
    try {
      return AiPromptUtil.interpretarRespuesta(texto, modelo);
    } catch (IllegalArgumentException e) {
      throw new AiProviderException(PROVEEDOR, 200, "Respuesta no interpretable: " + recortar(texto));
    }
  }

  /**
   * PDF, audio y video se adjuntan completos si no pasan del límite.
   *
   * @param contenido archivo preparado
   * @return true si se adjunta el archivo original
   */
  private boolean adjuntaOriginal(FileContentDTO contenido) {
    Category c = contenido.getCategoria();
    return contenido.getBytesOriginales() != null
        && contenido.getBytesOriginales().length <= MAX_ADJUNTO
        && (c == Category.PDF || c == Category.AUDIO || c == Category.VIDEO);
  }

  /**
   * Arma el JSON de la petición generateContent.
   *
   * @param contenido archivo preparado
   * @param adjuntaOriginal true si se adjunta el archivo original
   * @return cuerpo JSON
   */
  private String construirCuerpo(FileContentDTO contenido, boolean adjuntaOriginal) {
    JsonArray partes = new JsonArray();
    JsonObject texto = new JsonObject();
    texto.addProperty("text", AiPromptUtil.construirPrompt(contenido, adjuntaOriginal));
    partes.add(texto);
    if (adjuntaOriginal) {
      partes.add(inline(contenido.getMimeType(), Base64.getEncoder().encodeToString(contenido.getBytesOriginales())));
    } else {
      for (ImagePartDTO img : contenido.getImagenes()) {
        partes.add(inline(img.getMimeType(), img.getBase64()));
      }
    }
    JsonObject mensaje = new JsonObject();
    mensaje.addProperty("role", "user");
    mensaje.add("parts", partes);
    JsonArray contents = new JsonArray();
    contents.add(mensaje);

    JsonObject sistemaTexto = new JsonObject();
    sistemaTexto.addProperty("text", AiPromptUtil.SISTEMA);
    JsonArray sistemaPartes = new JsonArray();
    sistemaPartes.add(sistemaTexto);
    JsonObject sistema = new JsonObject();
    sistema.add("parts", sistemaPartes);

    JsonObject config = new JsonObject();
    config.addProperty("temperature", 0.2);
    config.addProperty("responseMimeType", "application/json");
    if (!modelo.startsWith("gemini-1") && !modelo.startsWith("gemini-2")) {
      // Los modelos Gemini 3 en adelante piensan antes de responder. Con un PDF completo el nivel
      // por defecto puede tardar más que el tiempo máximo de espera; "low" basta para esta tarea.
      JsonObject pensamiento = new JsonObject();
      pensamiento.addProperty("thinkingLevel", "low");
      config.add("thinkingConfig", pensamiento);
    }

    JsonObject cuerpo = new JsonObject();
    cuerpo.add("systemInstruction", sistema);
    cuerpo.add("contents", contents);
    cuerpo.add("generationConfig", config);
    return cuerpo.toString();
  }

  /**
   * Devuelve la parte inline_data.
   *
   * @param mime tipo MIME
   * @param base64 datos en base64
   * @return parte inline_data
   */
  private JsonObject inline(String mime, String base64) {
    JsonObject datos = new JsonObject();
    datos.addProperty("mime_type", mime);
    datos.addProperty("data", base64);
    JsonObject parte = new JsonObject();
    parte.add("inline_data", datos);
    return parte;
  }

  /**
   * Junta el texto de la primera respuesta candidata. Las partes marcadas como pensamiento no
   * hacen parte de la respuesta y se omiten.
   *
   * @param respuesta respuesta convertida en DTO
   * @return texto generado
   * @throws AiProviderException si no hay candidatos o el texto está vacío
   */
  private String extraerTexto(GeminiResponseDTO respuesta) throws AiProviderException {
    if (respuesta.getCandidates() == null || respuesta.getCandidates().isEmpty()) {
      throw new AiProviderException(PROVEEDOR, 200, "Sin candidatos en la respuesta");
    }
    GeminiCandidateDTO candidato = respuesta.getCandidates().get(0);
    StringBuilder sb = new StringBuilder();
    if (candidato.getContent() != null && candidato.getContent().getParts() != null) {
      for (GeminiPartDTO parte : candidato.getContent().getParts()) {
        if (parte.getText() != null && !Boolean.TRUE.equals(parte.getThought())) {
          sb.append(parte.getText());
        }
      }
    }
    if (sb.toString().isBlank()) {
      throw new AiProviderException(
          PROVEEDOR, 200, "Respuesta vacía (finishReason=" + candidato.getFinishReason() + ")");
    }
    return sb.toString();
  }

  /**
   * Espera su turno y envía la petición. La API key va en el encabezado x-goog-api-key.
   *
   * @param endpoint URL completa
   * @param cuerpo cuerpo JSON
   * @return respuesta convertida en DTO
   * @throws AiProviderException si no hay conexión o la respuesta no tiene el formato esperado
   * @throws InterruptedException si se interrumpe el hilo
   */
  private GeminiResponseDTO enviar(String endpoint, String cuerpo)
      throws AiProviderException, InterruptedException {
    limiter.esperarTurno();
    try {
      return ExternalHTTPRequestHandler.doPostAndConvertToGeminiDto(endpoint, apiKey, cuerpo, timeout);
    } catch (IOException e) {
      throw new AiProviderException(PROVEEDOR, 0, "Sin conexión: " + e.getMessage());
    } catch (RuntimeException e) {
      throw new AiProviderException(PROVEEDOR, 200, "Formato inesperado: " + recortar(e.getMessage()));
    }
  }

  /**
   * Devuelve los primeros 300 caracteres.
   *
   * @param texto texto largo
   * @return primeros 300 caracteres
   */
  private String recortar(String texto) {
    if (texto == null) {
      return "";
    }
    return texto.length() > 300 ? texto.substring(0, 300) : texto;
  }
}
