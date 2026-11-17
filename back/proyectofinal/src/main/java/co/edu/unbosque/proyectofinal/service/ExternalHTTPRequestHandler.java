package co.edu.unbosque.proyectofinal.service;

import co.edu.unbosque.proyectofinal.dto.ChatCompletionDTO;
import co.edu.unbosque.proyectofinal.dto.GeminiResponseDTO;
import co.edu.unbosque.proyectofinal.dto.GeolocationDTO;
import co.edu.unbosque.proyectofinal.dto.GithubRepoInfoDTO;
import co.edu.unbosque.proyectofinal.dto.PublicIpDTO;
import co.edu.unbosque.proyectofinal.dto.TranscriptionDTO;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * Clase para manejar las peticiones HTTP a servicios externos: las IAs, la geolocalización por IP
 * y GitHub. Cada método hace la petición y convierte el JSON de la respuesta en su DTO con Gson,
 * igual que en la guía de clase. Las API keys no se escriben aquí: cada servicio las lee del
 * application.properties con {@code @Value} y las pasa como parámetro.
 *
 * <p>El front nunca llama estas APIs; todo pasa por el back y al front solo llega lo necesario.
 */
public class ExternalHTTPRequestHandler {

  /**
   * Cliente HTTP compartido para realizar peticiones. Usa HTTP/1.1, que es el más estable para
   * enviar archivos grandes (un PDF completo a Gemini), sigue las redirecciones normales y tiene un
   * tiempo de espera de conexión de 10 segundos.
   */
  private static final HttpClient HTTP_CLIENT =
      HttpClient.newBuilder()
          .version(HttpClient.Version.HTTP_1_1)
          .followRedirects(HttpClient.Redirect.NORMAL)
          .connectTimeout(Duration.ofSeconds(10))
          .build();

  /**
   * Gson para las APIs que usan nombres con guion bajo en el JSON (formato de OpenAI y GitHub). Con
   * esta política el campo {@code finishReason} del DTO se llena con la clave {@code
   * finish_reason} del JSON, y los DTO siguen las convenciones de nombres de Java.
   */
  private static final Gson GSON_GUION_BAJO =
      new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

  /** Gson para las APIs cuyas claves ya coinciden con los nombres de los campos (Gemini, ip-api, ipify). */
  private static final Gson GSON = new GsonBuilder().create();

  /** Agente de usuario que se envía a GitHub, que lo exige en todas las peticiones. */
  private static final String USER_AGENT = "DetectorIA-UnBosque";

  /** Encabezado de tipo de contenido. */
  private static final String CONTENT_TYPE = "Content-Type";

  /** Tipo de contenido JSON. */
  private static final String JSON = "application/json";

  /** Tiempo máximo de respuesta de las APIs de geolocalización. */
  private static final int TIMEOUT_GEO = 4;

  /** Tiempo máximo de respuesta de la API de GitHub. */
  private static final int TIMEOUT_GITHUB = 15;

  /** Solo tiene métodos estáticos, así que no se instancia. */
  private ExternalHTTPRequestHandler() {}

  /**
   * Consulta la ubicación aproximada de una IP y la convierte en DTO.
   *
   * @param url dirección completa con la IP y los campos pedidos
   * @return ubicación con el código HTTP; sin datos si el código no es 200
   * @throws IOException si falla la conexión
   * @throws InterruptedException si se interrumpe el hilo
   */
  public static GeolocationDTO doGetAndConvertToGeolocationDto(String url)
      throws IOException, InterruptedException {
    HttpResponse<String> response = doGet(url, Map.of(), TIMEOUT_GEO);
    GeolocationDTO ubicacion = null;
    if (response.statusCode() == 200) {
      ubicacion = GSON.fromJson(response.body(), GeolocationDTO.class);
    }
    if (ubicacion == null) {
      ubicacion = new GeolocationDTO();
    }
    ubicacion.setStatuscode(response.statusCode());
    return ubicacion;
  }

  /**
   * Consulta la IP pública del servidor y la convierte en DTO.
   *
   * @param url dirección de la API de IP pública
   * @return IP con el código HTTP; sin IP si el código no es 200
   * @throws IOException si falla la conexión
   * @throws InterruptedException si se interrumpe el hilo
   */
  public static PublicIpDTO doGetAndConvertToPublicIpDto(String url)
      throws IOException, InterruptedException {
    HttpResponse<String> response = doGet(url, Map.of(), TIMEOUT_GEO);
    PublicIpDTO ip = null;
    if (response.statusCode() == 200) {
      ip = GSON.fromJson(response.body(), PublicIpDTO.class);
    }
    if (ip == null) {
      ip = new PublicIpDTO();
    }
    ip.setStatuscode(response.statusCode());
    return ip;
  }

  /**
   * Consulta los datos de un repositorio en la API de GitHub y los convierte en DTO.
   *
   * @param url dirección de la API, por ejemplo https://api.github.com/repos/dueno/nombre
   * @return datos del repositorio con el código HTTP; sin datos si el código no es 200
   * @throws IOException si falla la conexión
   * @throws InterruptedException si se interrumpe el hilo
   */
  public static GithubRepoInfoDTO doGetAndConvertToGithubRepoDto(String url)
      throws IOException, InterruptedException {
    HttpResponse<String> response =
        doGet(url, Map.of("Accept", "application/vnd.github+json", "User-Agent", USER_AGENT), TIMEOUT_GITHUB);
    GithubRepoInfoDTO repo = null;
    if (response.statusCode() == 200) {
      repo = GSON_GUION_BAJO.fromJson(response.body(), GithubRepoInfoDTO.class);
    }
    if (repo == null) {
      repo = new GithubRepoInfoDTO();
    }
    repo.setStatuscode(response.statusCode());
    return repo;
  }

  /**
   * Envía una petición al endpoint /chat/completions de un proveedor con formato de OpenAI y
   * convierte la respuesta en DTO. Si el código no es 200 no se convierte: se guarda el cuerpo
   * para el mensaje de error y el encabezado Retry-After para el reintento.
   *
   * @param url dirección completa del endpoint
   * @param apiKey API key del proveedor, leída del application.properties
   * @param json cuerpo de la petición
   * @param timeoutSegundos tiempo máximo de respuesta
   * @return respuesta del modelo con el código HTTP
   * @throws IOException si falla la conexión
   * @throws InterruptedException si se interrumpe el hilo
   */
  public static ChatCompletionDTO doPostAndConvertToChatCompletionDto(
      String url, String apiKey, String json, int timeoutSegundos)
      throws IOException, InterruptedException {
    HttpResponse<String> response =
        doPost(url, Map.of("Authorization", "Bearer " + apiKey), json, timeoutSegundos);
    ChatCompletionDTO completion = null;
    if (response.statusCode() == 200) {
      completion = GSON_GUION_BAJO.fromJson(response.body(), ChatCompletionDTO.class);
    }
    if (completion == null) {
      completion = new ChatCompletionDTO();
    }
    completion.setStatuscode(response.statusCode());
    if (response.statusCode() != 200) {
      completion.setCuerpoError(response.body());
      completion.setReintentarDespues(response.headers().firstValue("retry-after").orElse(null));
    }
    return completion;
  }

  /**
   * Envía una petición al endpoint generateContent de Gemini y convierte la respuesta en DTO. Si
   * el código no es 200 no se convierte y se guarda el cuerpo para el mensaje de error.
   *
   * @param url dirección completa del endpoint
   * @param apiKey API key de Google AI Studio, leída del application.properties
   * @param json cuerpo de la petición
   * @param timeoutSegundos tiempo máximo de respuesta
   * @return respuesta del modelo con el código HTTP
   * @throws IOException si falla la conexión
   * @throws InterruptedException si se interrumpe el hilo
   */
  public static GeminiResponseDTO doPostAndConvertToGeminiDto(
      String url, String apiKey, String json, int timeoutSegundos)
      throws IOException, InterruptedException {
    HttpResponse<String> response = doPost(url, Map.of("x-goog-api-key", apiKey), json, timeoutSegundos);
    GeminiResponseDTO gemini = null;
    if (response.statusCode() == 200) {
      gemini = GSON.fromJson(response.body(), GeminiResponseDTO.class);
    }
    if (gemini == null) {
      gemini = new GeminiResponseDTO();
    }
    gemini.setStatuscode(response.statusCode());
    if (response.statusCode() != 200) {
      gemini.setCuerpoError(response.body());
    }
    return gemini;
  }

  /**
   * Envía un audio a la API de transcripción (formato de OpenAI) como multipart/form-data y
   * convierte la respuesta en DTO.
   *
   * @param url dirección completa del endpoint
   * @param apiKey API key del proveedor, leída del application.properties
   * @param campos campos de texto del formulario, por ejemplo el modelo
   * @param nombreArchivo nombre con el que se envía el archivo
   * @param mimeType tipo MIME del archivo
   * @param contenido bytes del archivo
   * @param timeoutSegundos tiempo máximo de respuesta
   * @return texto transcrito con el código HTTP
   * @throws IOException si falla la conexión
   * @throws InterruptedException si se interrumpe el hilo
   */
  public static TranscriptionDTO doPostMultipartAndConvertToTranscriptionDto(
      String url,
      String apiKey,
      Map<String, String> campos,
      String nombreArchivo,
      String mimeType,
      byte[] contenido,
      int timeoutSegundos)
      throws IOException, InterruptedException {
    String limite = "----DetectorIA" + UUID.randomUUID().toString().replace("-", "");
    ByteArrayOutputStream cuerpo = new ByteArrayOutputStream();
    for (Map.Entry<String, String> campo : campos.entrySet()) {
      escribir(cuerpo, "--" + limite + "\r\n");
      escribir(cuerpo, "Content-Disposition: form-data; name=\"" + campo.getKey() + "\"\r\n\r\n");
      escribir(cuerpo, campo.getValue() + "\r\n");
    }
    String nombreSeguro = nombreArchivo.replaceAll("[\"\\r\\n]", "_");
    escribir(cuerpo, "--" + limite + "\r\n");
    escribir(cuerpo, "Content-Disposition: form-data; name=\"file\"; filename=\"" + nombreSeguro + "\"\r\n");
    escribir(cuerpo, CONTENT_TYPE + ": " + mimeType + "\r\n\r\n");
    cuerpo.write(contenido);
    escribir(cuerpo, "\r\n--" + limite + "--\r\n");

    HttpRequest request =
        HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofByteArray(cuerpo.toByteArray()))
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(timeoutSegundos))
            .header(CONTENT_TYPE, "multipart/form-data; boundary=" + limite)
            .header("Authorization", "Bearer " + apiKey)
            .build();
    HttpResponse<String> response =
        HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    TranscriptionDTO transcripcion = null;
    if (response.statusCode() == 200) {
      transcripcion = GSON.fromJson(response.body(), TranscriptionDTO.class);
    }
    if (transcripcion == null) {
      transcripcion = new TranscriptionDTO();
    }
    transcripcion.setStatuscode(response.statusCode());
    if (response.statusCode() != 200) {
      transcripcion.setCuerpoError(response.body());
    }
    return transcripcion;
  }

  /**
   * Descarga un archivo binario (el zip de un repositorio) sin pasar de un tamaño máximo. Esta
   * respuesta no es JSON, por eso no tiene DTO.
   *
   * @param url dirección completa
   * @param maxBytes tamaño máximo permitido
   * @param timeoutSegundos tiempo máximo de respuesta
   * @return bytes descargados
   * @throws IOException si falla la conexión, el código no es 200 o se pasa del tamaño
   * @throws InterruptedException si se interrumpe el hilo
   */
  public static byte[] doGetBytes(String url, long maxBytes, int timeoutSegundos)
      throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(timeoutSegundos))
            .header("User-Agent", USER_AGENT)
            .build();
    HttpResponse<InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
    try (InputStream entrada = response.body()) {
      if (response.statusCode() != 200) {
        throw new IOException("Código HTTP " + response.statusCode());
      }
      ByteArrayOutputStream salida = new ByteArrayOutputStream();
      byte[] buffer = new byte[8192];
      long total = 0;
      int leidos;
      while ((leidos = entrada.read(buffer)) != -1) {
        total += leidos;
        if (total > maxBytes) {
          throw new IOException("El archivo supera el tamaño máximo permitido");
        }
        salida.write(buffer, 0, leidos);
      }
      return salida.toByteArray();
    }
  }

  /**
   * Hace una petición GET y devuelve la respuesta en texto.
   *
   * @param url dirección completa
   * @param headers encabezados adicionales, puede ser vacío
   * @param timeoutSegundos tiempo máximo de respuesta
   * @return respuesta con código y cuerpo
   * @throws IOException si falla la conexión
   * @throws InterruptedException si se interrumpe el hilo
   */
  private static HttpResponse<String> doGet(String url, Map<String, String> headers, int timeoutSegundos)
      throws IOException, InterruptedException {
    HttpRequest.Builder builder =
        HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(timeoutSegundos))
            .header(CONTENT_TYPE, JSON);
    headers.forEach(builder::header);
    return HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  /**
   * Hace una petición POST con cuerpo JSON y devuelve la respuesta en texto.
   *
   * @param url dirección completa
   * @param headers encabezados adicionales, por ejemplo el de autorización
   * @param json cuerpo de la petición
   * @param timeoutSegundos tiempo máximo de respuesta
   * @return respuesta con código y cuerpo
   * @throws IOException si falla la conexión
   * @throws InterruptedException si se interrumpe el hilo
   */
  private static HttpResponse<String> doPost(
      String url, Map<String, String> headers, String json, int timeoutSegundos)
      throws IOException, InterruptedException {
    HttpRequest.Builder builder =
        HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(timeoutSegundos))
            .header(CONTENT_TYPE, JSON);
    headers.forEach(builder::header);
    return HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  /**
   * Escribe texto en UTF-8 dentro del cuerpo multipart.
   *
   * @param salida flujo de salida
   * @param texto texto a escribir
   * @throws IOException si falla la escritura
   */
  private static void escribir(ByteArrayOutputStream salida, String texto) throws IOException {
    salida.write(texto.getBytes(StandardCharsets.UTF_8));
  }
}
