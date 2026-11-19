package co.edu.unbosque.proyectofinal.service;

import co.edu.unbosque.proyectofinal.dto.TranscriptionDTO;
import co.edu.unbosque.proyectofinal.exception.AiProviderException;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Cliente REST de Groq. Además de opinar sobre el archivo, transcribe audios y videos con Whisper
 * para que las otras IAs puedan leer lo que se dice en ellos.
 */
@Service
public class GroqDetector extends OpenAiCompatibleDetector {

  /** Nombre visible del proveedor. */
  public static final String PROVEEDOR = "Groq";

  /** Límite de tamaño de la API de transcripción en el plan gratuito. */
  private static final long MAX_AUDIO = 25L * 1024 * 1024;

  /** Modelo de transcripción. */
  private final String modeloAudio;

  /**
   * Spring inyecta aquí las dependencias.
   *
   * @param apiKey API key de Groq
   * @param url URL base
   * @param modeloTexto modelo para texto
   * @param modeloVision modelo para imágenes
   * @param modeloAudio modelo Whisper
   * @param intervaloMs milisegundos minimos entre peticiones
   * @param imagenComoObjeto formato del campo image_url
   * @param modoJson true para pedir respuesta en modo JSON
   * @param timeout tiempo máximo de respuesta en segundos
   */
  public GroqDetector(
      @Value("${ai.groq.api-key}") String apiKey,
      @Value("${ai.groq.url}") String url,
      @Value("${ai.groq.model-text}") String modeloTexto,
      @Value("${ai.groq.model-vision}") String modeloVision,
      @Value("${ai.groq.model-audio}") String modeloAudio,
      @Value("${ai.groq.min-interval-ms:2500}") long intervaloMs,
      @Value("${ai.groq.image-url-as-object:true}") boolean imagenComoObjeto,
      @Value("${ai.groq.json-mode:false}") boolean modoJson,
      @Value("${ai.timeout-seconds:90}") int timeout) {
    super(PROVEEDOR, url, apiKey, modeloTexto, modeloVision, intervaloMs, imagenComoObjeto, modoJson, timeout);
    this.modeloAudio = modeloAudio;
  }

  /**
   * Los modelos GPT-OSS razonan antes de responder. Se pide un razonamiento corto y que no venga en
   * la respuesta, para que el cupo de tokens alcance para el JSON.
   *
   * @param cuerpo cuerpo de la petición
   * @param modelo modelo que se va a usar
   */
  @Override
  protected void opcionesExtra(JsonObject cuerpo, String modelo) {
    if (modelo != null && modelo.startsWith("openai/gpt-oss")) {
      cuerpo.addProperty("reasoning_effort", "low");
      cuerpo.addProperty("include_reasoning", false);
    }
  }

  /**
   * Devuelve el modelo de visión de Groq acepta máximo 3 imágenes por petición.
   *
   * @return el modelo de visión de Groq acepta máximo 3 imágenes por petición
   */
  @Override
  protected int maxImagenes() {
    return 3;
  }

  /**
   * Transcribe un audio o el audio de un video.
   *
   * @param contenido bytes del archivo
   * @param nombreArchivo nombre con extensión (Whisper la usa para detectar el formato)
   * @param mimeType tipo MIME
   * @return texto transcrito
   * @throws AiProviderException si la API falla o el archivo es muy grande
   * @throws InterruptedException si se interrumpe el hilo
   */
  public String transcribir(byte[] contenido, String nombreArchivo, String mimeType)
      throws AiProviderException, InterruptedException {
    verificarConfiguracion();
    if (contenido.length > MAX_AUDIO) {
      throw new AiProviderException(PROVEEDOR, 0, "Audio demasiado grande para transcribir");
    }
    Map<String, String> campos = new LinkedHashMap<>();
    campos.put("model", modeloAudio);
    campos.put("response_format", "json");
    try {
      TranscriptionDTO transcripcion =
          ExternalHTTPRequestHandler.doPostMultipartAndConvertToTranscriptionDto(
              url + "/audio/transcriptions", apiKey, campos, nombreArchivo, mimeType, contenido, timeout);
      if (transcripcion.getStatuscode() != 200) {
        throw new AiProviderException(PROVEEDOR, transcripcion.getStatuscode(), recortar(transcripcion.getCuerpoError()));
      }
      return transcripcion.getText() == null ? "" : transcripcion.getText();
    } catch (IOException e) {
      throw new AiProviderException(PROVEEDOR, 0, "Sin conexión: " + e.getMessage());
    } catch (RuntimeException e) {
      throw new AiProviderException(PROVEEDOR, 200, "Transcripción con formato inesperado");
    }
  }
}
