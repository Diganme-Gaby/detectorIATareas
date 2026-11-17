package co.edu.unbosque.proyectofinal.service;

import co.edu.unbosque.proyectofinal.dto.AiVerdictDTO;
import co.edu.unbosque.proyectofinal.dto.ChatChoiceDTO;
import co.edu.unbosque.proyectofinal.dto.ChatCompletionDTO;
import co.edu.unbosque.proyectofinal.dto.FileContentDTO;
import co.edu.unbosque.proyectofinal.dto.ImagePartDTO;
import co.edu.unbosque.proyectofinal.exception.AiProviderException;
import co.edu.unbosque.proyectofinal.util.AiPromptUtil;
import co.edu.unbosque.proyectofinal.util.RateLimiter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;

/**
 * Base para los proveedores que exponen el endpoint {@code /chat/completions} con el formato de
 * OpenAI (Groq, OpenRouter y los respaldos NVIDIA, Cohere y GitHub Models). Cambiar de proveedor
 * es cuestión de cambiar la URL, el modelo y la API key en el application.properties.
 *
 * <p>Si un proveedor no tiene modelo de visión configurado, los archivos con imágenes se le envían
 * solo con el texto y los datos del archivo.
 */
public abstract class OpenAiCompatibleDetector implements AiDetector {

  /**
   * Máximo de tokens de la respuesta. Los modelos que razonan antes de responder gastan parte de
   * este cupo pensando; con un valor bajo se quedan sin espacio para el JSON.
   */
  private static final int MAX_TOKENS_RESPUESTA = 3000;

  /** Nombre visible del proveedor. */
  private final String proveedor;

  /** URL base, sin /chat/completions. */
  protected final String url;

  /** API key. */
  protected final String apiKey;

  /** Modelo para texto. */
  private final String modeloTexto;

  /** Modelo para imágenes. */
  private final String modeloVision;

  /** Forma de enviar la imagen: objeto {"url": ...} (OpenAI, Groq, OpenRouter) o texto plano. */
  private final boolean imagenComoObjeto;

  /** Tiempo máximo de respuesta. */
  protected final int timeout;

  /** Pedir la respuesta en modo JSON (response_format). Algunos modelos gratuitos no lo soportan. */
  private final boolean modoJson;

  /** Limitador de ritmo de peticiones. */
  private final RateLimiter limiter;

  /**
   * Spring inyecta aquí las dependencias.
   *
   * @param proveedor nombre visible
   * @param url URL base
   * @param apiKey API key
   * @param modeloTexto modelo para texto
   * @param modeloVision modelo para imágenes; vacío si el proveedor solo recibe texto
   * @param intervaloMs milisegundos minimos entre peticiones
   * @param imagenComoObjeto formato del campo image_url
   * @param modoJson true para enviar response_format json_object
   * @param timeout tiempo máximo de respuesta en segundos
   */
  protected OpenAiCompatibleDetector(
      String proveedor,
      String url,
      String apiKey,
      String modeloTexto,
      String modeloVision,
      long intervaloMs,
      boolean imagenComoObjeto,
      boolean modoJson,
      int timeout) {
    this.proveedor = proveedor;
    this.url = url;
    this.apiKey = apiKey;
    this.modeloTexto = modeloTexto;
    this.modeloVision = modeloVision;
    this.imagenComoObjeto = imagenComoObjeto;
    this.timeout = timeout;
    this.modoJson = modoJson;
    this.limiter = new RateLimiter(intervaloMs);
  }

  /**
   * Devuelve el nombre del proveedor.
   *
   * @return nombre del proveedor
   */
  @Override
  public String getProveedor() {
    return proveedor;
  }

  /**
   * Devuelve el modelo de visión si hay imágenes, si no el de texto.
   *
   * @param contenido archivo preparado
   * @return modelo de visión si hay imágenes, si no el de texto
   */
  @Override
  public String modeloPara(FileContentDTO contenido) {
    return usaImagenes(contenido) ? modeloVision : modeloTexto;
  }

  /**
   * Devuelve true si el archivo trae imágenes y el proveedor tiene modelo de visión.
   *
   * @param contenido archivo preparado
   * @return true si el archivo trae imágenes y el proveedor tiene modelo de visión
   */
  private boolean usaImagenes(FileContentDTO contenido) {
    return !contenido.getImagenes().isEmpty() && modeloVision != null && !modeloVision.isBlank();
  }

  /**
   * Envía el contenido al endpoint de chat y devuelve el veredicto.
   *
   * @param contenido archivo preparado
   * @return veredicto interpretado
   * @throws AiProviderException si la API falla
   * @throws InterruptedException si se interrumpe el hilo
   */
  @Override
  public AiVerdictDTO analizar(FileContentDTO contenido) throws AiProviderException, InterruptedException {
    verificarConfiguracion();
    String modelo = modeloPara(contenido);
    String cuerpo = construirCuerpo(contenido, modelo);
    String endpoint = url + "/chat/completions";

    ChatCompletionDTO respuesta = enviar(endpoint, cuerpo);
    if (respuesta.getStatuscode() == 429 || respuesta.getStatuscode() >= 500) {
      Thread.sleep(esperaReintento(respuesta));
      respuesta = enviar(endpoint, cuerpo);
    }
    if (respuesta.getStatuscode() != 200) {
      throw new AiProviderException(proveedor, respuesta.getStatuscode(), recortar(respuesta.getCuerpoError()));
    }
    if (respuesta.getChoices() == null || respuesta.getChoices().isEmpty()) {
      throw new AiProviderException(proveedor, 200, "Respuesta sin opciones");
    }
    ChatChoiceDTO opcion = respuesta.getChoices().get(0);
    String motivoFin = opcion.getFinishReason() == null ? "?" : opcion.getFinishReason();
    String texto =
        opcion.getMessage() == null || opcion.getMessage().getContent() == null
            ? ""
            : opcion.getMessage().getContent();
    if (texto.isBlank()) {
      throw new AiProviderException(proveedor, 200, "Respuesta vacía (finish_reason=" + motivoFin + ")");
    }
    try {
      return AiPromptUtil.interpretarRespuesta(texto, modelo);
    } catch (IllegalArgumentException e) {
      throw new AiProviderException(proveedor, 200, "Respuesta no interpretable: " + recortar(texto));
    }
  }

  /**
   * Arma el cuerpo con el mensaje de sistema y el del usuario. Con imágenes el contenido del
   * usuario es un arreglo de partes; sin imágenes es texto y, si el proveedor lo soporta, se pide
   * respuesta en modo JSON.
   *
   * @param contenido archivo preparado
   * @param modelo modelo a usar
   * @return cuerpo JSON
   */
  private String construirCuerpo(FileContentDTO contenido, String modelo) {
    JsonArray mensajes = new JsonArray();
    JsonObject sistema = new JsonObject();
    sistema.addProperty("role", "system");
    sistema.addProperty("content", AiPromptUtil.SISTEMA);
    mensajes.add(sistema);

    JsonObject usuario = new JsonObject();
    usuario.addProperty("role", "user");
    String prompt = AiPromptUtil.construirPrompt(contenido, false);
    boolean conImagenes = usaImagenes(contenido);
    if (!conImagenes && !contenido.getImagenes().isEmpty()) {
      prompt += "\n\nNota: este modelo no puede ver las imágenes; evalúa con los datos y el texto"
          + " disponibles.";
    }
    if (!conImagenes) {
      usuario.addProperty("content", prompt);
    } else {
      JsonArray partes = new JsonArray();
      JsonObject texto = new JsonObject();
      texto.addProperty("type", "text");
      texto.addProperty("text", prompt);
      partes.add(texto);
      int agregadas = 0;
      for (ImagePartDTO img : contenido.getImagenes()) {
        if (agregadas++ >= maxImagenes()) {
          break;
        }
        String dataUrl = "data:" + img.getMimeType() + ";base64," + img.getBase64();
        JsonObject parte = new JsonObject();
        parte.addProperty("type", "image_url");
        if (imagenComoObjeto) {
          JsonObject urlObj = new JsonObject();
          urlObj.addProperty("url", dataUrl);
          parte.add("image_url", urlObj);
        } else {
          parte.addProperty("image_url", dataUrl);
        }
        partes.add(parte);
      }
      usuario.add("content", partes);
    }
    mensajes.add(usuario);

    JsonObject cuerpo = new JsonObject();
    cuerpo.addProperty("model", modelo);
    cuerpo.add("messages", mensajes);
    cuerpo.addProperty("temperature", 0.2);
    cuerpo.addProperty("max_tokens", maxTokens());
    if (modoJson && !conImagenes) {
      JsonObject formato = new JsonObject();
      formato.addProperty("type", "json_object");
      cuerpo.add("response_format", formato);
    }
    opcionesExtra(cuerpo, modelo);
    return cuerpo.toString();
  }

  /**
   * Permite que cada proveedor agregue parámetros propios al cuerpo, por ejemplo para limitar el
   * razonamiento del modelo. Por defecto no agrega nada.
   *
   * @param cuerpo cuerpo de la petición
   * @param modelo modelo que se va a usar
   */
  protected void opcionesExtra(JsonObject cuerpo, String modelo) {
    // Sin parámetros adicionales por defecto.
  }

  /**
   * Devuelve el máximo de tokens de la respuesta; algunos proveedores tienen un contexto más pequeño.
   *
   * @return máximo de tokens de la respuesta; algunos proveedores tienen un contexto más pequeño
   */
  protected int maxTokens() {
    return MAX_TOKENS_RESPUESTA;
  }

  /**
   * Devuelve el máximo de imágenes que acepta el proveedor en una sola petición.
   *
   * @return máximo de imágenes que acepta el proveedor en una sola petición
   */
  protected int maxImagenes() {
    return 4;
  }

  /**
   * Verifica que el proveedor tenga API key configurada en el application.properties.
   *
   * @throws AiProviderException si no hay API key
   */
  protected void verificarConfiguracion() throws AiProviderException {
    if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("CAMBIAR")) {
      throw new AiProviderException(proveedor, 0, "API key sin configurar");
    }
  }

  /**
   * Espera su turno y envía la petición. La API key va en el encabezado de autorización.
   *
   * @param endpoint URL completa
   * @param cuerpo cuerpo JSON
   * @return respuesta convertida en DTO
   * @throws AiProviderException si no hay conexión o la respuesta no tiene el formato esperado
   * @throws InterruptedException si se interrumpe el hilo
   */
  private ChatCompletionDTO enviar(String endpoint, String cuerpo)
      throws AiProviderException, InterruptedException {
    limiter.esperarTurno();
    try {
      return ExternalHTTPRequestHandler.doPostAndConvertToChatCompletionDto(endpoint, apiKey, cuerpo, timeout);
    } catch (IOException e) {
      throw new AiProviderException(proveedor, 0, "Sin conexión: " + e.getMessage());
    } catch (RuntimeException e) {
      throw new AiProviderException(proveedor, 200, "Formato inesperado: " + recortar(e.getMessage()));
    }
  }

  /**
   * Usa el encabezado Retry-After si vino; si no, espera 5 segundos. Nunca más de 20.
   *
   * @param respuesta respuesta 429 o 5xx
   * @return milisegundos de espera
   */
  private long esperaReintento(ChatCompletionDTO respuesta) {
    if (respuesta.getReintentarDespues() == null) {
      return 5000L;
    }
    try {
      return Math.min(20000L, (long) (Double.parseDouble(respuesta.getReintentarDespues()) * 1000));
    } catch (NumberFormatException e) {
      return 5000L;
    }
  }

  /**
   * Devuelve los primeros 300 caracteres.
   *
   * @param texto texto largo
   * @return primeros 300 caracteres
   */
  protected String recortar(String texto) {
    if (texto == null) {
      return "";
    }
    return texto.length() > 300 ? texto.substring(0, 300) : texto;
  }
}
