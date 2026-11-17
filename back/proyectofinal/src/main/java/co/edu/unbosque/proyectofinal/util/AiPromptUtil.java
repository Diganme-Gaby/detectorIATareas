package co.edu.unbosque.proyectofinal.util;

import co.edu.unbosque.proyectofinal.dto.AiVerdictDTO;
import co.edu.unbosque.proyectofinal.dto.FileContentDTO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Arma la instrucción que se envía a las tres IAs y convierte su respuesta en un
 * {@link AiVerdictDTO}. Las tres reciben exactamente la misma instrucción para que los porcentajes
 * sean comparables.
 */
public final class AiPromptUtil {

  /** Instrucción de sistema común a los tres modelos. */
  public static final String SISTEMA =
      "Eres un analista forense de contenido. Tu tarea es estimar la probabilidad de que un"
          + " archivo haya sido generado total o parcialmente por una inteligencia artificial"
          + " (por ejemplo ChatGPT, Claude, Gemini, Copilot, Midjourney, DALL-E, Suno o"
          + " ElevenLabs). Basa tu juicio solo en evidencia observable del contenido. Responde"
          + " siempre en español y únicamente con un objeto JSON válido.";

  /** Máximo de motivos que se guardan por IA. */
  private static final int MAX_MOTIVOS = 6;

  /** Solo tiene métodos estáticos, así que no se instancia. */
  private AiPromptUtil() {}

  /**
   * Construye la instrucción de usuario.
   *
   * @param contenido archivo preparado
   * @param recibeArchivo true si el modelo también recibe el archivo original adjunto
   * @return texto de la instrucción
   */
  public static String construirPrompt(FileContentDTO contenido, boolean recibeArchivo) {
    StringBuilder sb = new StringBuilder();
    sb.append("Analiza el siguiente archivo y estima la probabilidad de que haya sido generado con IA.\n\n");
    sb.append("Datos del archivo:\n");
    sb.append("- Nombre: ").append(contenido.getNombre()).append('\n');
    sb.append("- Ruta: ").append(contenido.getRuta()).append('\n');
    sb.append("- Tipo: ").append(contenido.getCategoria()).append(" (").append(contenido.getMimeType()).append(")\n");
    sb.append("- Tamaño: ").append(contenido.getTamanoBytes()).append(" bytes\n");
    if (contenido.getNota() != null) {
      sb.append("- Nota de preparación: ").append(contenido.getNota()).append('\n');
    }
    sb.append('\n').append(criterios(contenido)).append('\n');

    if (recibeArchivo) {
      sb.append("\nEl archivo original va adjunto a este mensaje.\n");
    }
    if (!contenido.getImagenes().isEmpty()) {
      sb.append("\nSe adjuntan ").append(contenido.getImagenes().size())
          .append(" imagen(es) del archivo (la imagen original o fotogramas del video).\n");
    }
    if (contenido.getTexto() != null && !contenido.getTexto().isBlank()) {
      sb.append("\nContenido en texto");
      if (contenido.isTextoRecortado()) {
        sb.append(" (recortado por longitud)");
      }
      sb.append(":\n<<<INICIO>>>\n").append(contenido.getTexto()).append("\n<<<FIN>>>\n");
    }
    sb.append("\nResponde solo con este JSON, sin texto adicional ni bloques de código:\n")
        .append("{\"porcentaje\": <entero de 0 a 100>, ")
        .append("\"veredicto\": \"<PROBABLEMENTE_IA | MIXTO | PROBABLEMENTE_HUMANO | INDETERMINADO>\", ")
        .append("\"motivos\": [\"<motivo concreto 1>\", \"<motivo concreto 2>\", \"<motivo concreto 3>\"]}\n")
        .append("Da entre 3 y 5 motivos concretos que citen rasgos del contenido. Si no hay")
        .append(" evidencia suficiente usa INDETERMINADO y un porcentaje cercano a 50.");
    return sb.toString();
  }

  /**
   * Criterios de análisis según el tipo de archivo.
   *
   * @param contenido archivo preparado
   * @return criterios a considerar
   */
  private static String criterios(FileContentDTO contenido) {
    switch (contenido.getCategoria()) {
      case CODIGO:
        return "Considera: comentarios genéricos o excesivamente explicativos, nombres demasiado"
            + " uniformes, estructura de plantilla, manejo de errores repetitivo, documentación"
            + " perfecta en todo el archivo, ausencia de rastros de depuración o de estilo"
            + " personal, y patrones típicos de asistentes de código.";
      case IMAGEN:
        return "Considera: artefactos de difusión, manos, texto o simetrías incoherentes,"
            + " texturas demasiado lisas, iluminación imposible, fondos que se deforman y"
            + " ausencia de ruido de cámara.";
      case AUDIO:
        return "Considera: prosodia y respiración poco naturales, pausas regulares, voz"
            + " sintética, ausencia de muletillas y la transcripción del contenido.";
      case VIDEO:
        return "Considera: incoherencias entre fotogramas, rostros o fondos que se deforman,"
            + " movimiento poco natural y la transcripción del audio si existe.";
      case OTRO:
        return "El archivo es binario o de un formato no legible. Usa los metadatos y el texto"
            + " disponible; si no alcanzan, responde INDETERMINADO.";
      default:
        return "Considera: estructura y transiciones de plantilla, tono uniforme, vocabulario"
            + " genérico, frases de relleno típicas de modelos de lenguaje, ausencia de errores"
            + " o experiencias personales verificables, y listas o conclusiones demasiado"
            + " ordenadas.";
    }
  }

  /**
   * Interpreta la respuesta de un modelo. Tolera texto antes o después del JSON y bloques de
   * código markdown.
   *
   * @param respuesta texto devuelto por el modelo
   * @param modelo modelo que respondió
   * @return veredicto interpretado
   * @throws IllegalArgumentException si no hay un JSON con porcentaje
   */
  public static AiVerdictDTO interpretarRespuesta(String respuesta, String modelo) {
    if (respuesta == null) {
      throw new IllegalArgumentException("Respuesta vacía");
    }
    // Algunos modelos escriben su razonamiento entre etiquetas <think> antes del JSON.
    respuesta = respuesta.replaceAll("(?s)<think>.*?</think>", "");
    int inicio = respuesta.indexOf('{');
    int fin = respuesta.lastIndexOf('}');
    if (inicio < 0 || fin <= inicio) {
      throw new IllegalArgumentException("La respuesta no trae JSON");
    }
    JsonObject json;
    try {
      json = JsonParser.parseString(respuesta.substring(inicio, fin + 1)).getAsJsonObject();
    } catch (JsonSyntaxException | IllegalStateException e) {
      throw new IllegalArgumentException("JSON inválido", e);
    }
    JsonElement porcentajeJson = json.has("porcentaje") ? json.get("porcentaje") : json.get("porcentajeIA");
    if (porcentajeJson == null || porcentajeJson.isJsonNull()) {
      throw new IllegalArgumentException("Falta el porcentaje");
    }
    double porcentaje;
    try {
      porcentaje = porcentajeJson.getAsDouble();
    } catch (NumberFormatException | UnsupportedOperationException e) {
      porcentaje = Double.parseDouble(porcentajeJson.getAsString().replaceAll("[^0-9.]", ""));
    }
    if (porcentaje > 0 && porcentaje <= 1 && porcentajeJson.toString().contains(".")) {
      porcentaje = porcentaje * 100;
    }
    porcentaje = Math.max(0, Math.min(100, porcentaje));

    String veredicto = json.has("veredicto") && !json.get("veredicto").isJsonNull()
        ? json.get("veredicto").getAsString().trim().toUpperCase()
        : veredictoPorPorcentaje(porcentaje);
    if (veredicto.length() > 60) {
      veredicto = veredicto.substring(0, 60);
    }

    List<String> motivos = new ArrayList<>();
    if (json.has("motivos") && json.get("motivos").isJsonArray()) {
      JsonArray arreglo = json.getAsJsonArray("motivos");
      for (JsonElement e : arreglo) {
        if (motivos.size() >= MAX_MOTIVOS) {
          break;
        }
        if (e.isJsonPrimitive()) {
          String m = e.getAsString().trim();
          if (!m.isEmpty()) {
            motivos.add(m.length() > 600 ? m.substring(0, 600) : m);
          }
        }
      }
    }
    if (motivos.isEmpty()) {
      motivos.add("El modelo no detallo motivos.");
    }

    AiVerdictDTO dto = new AiVerdictDTO();
    dto.setPorcentaje(Math.round(porcentaje * 10.0) / 10.0);
    dto.setVeredicto(veredicto);
    dto.setMotivos(motivos);
    dto.setModelo(modelo);
    return dto;
  }

  /**
   * Devuelve el veredicto por defecto según el porcentaje.
   *
   * @param porcentaje probabilidad de IA
   * @return veredicto por defecto según el porcentaje
   */
  public static String veredictoPorPorcentaje(double porcentaje) {
    if (porcentaje >= 70) {
      return "PROBABLEMENTE_IA";
    }
    if (porcentaje >= 40) {
      return "MIXTO";
    }
    return "PROBABLEMENTE_HUMANO";
  }

  /**
   * Nivel que se muestra en el front y en los reportes.
   *
   * @param promedio promedio de las IAs, o null
   * @return ALTO, MEDIO, BAJO o SIN_RESULTADO
   */
  public static String nivel(Double promedio) {
    if (promedio == null) {
      return "SIN_RESULTADO";
    }
    if (promedio >= 70) {
      return "ALTO";
    }
    if (promedio >= 40) {
      return "MEDIO";
    }
    return "BAJO";
  }

  /**
   * Promedio de una lista de porcentajes, redondeado a un decimal.
   *
   * @param valores porcentajes
   * @return promedio, o null si la lista está vacía
   */
  public static Double promedio(List<Double> valores) {
    if (valores == null || valores.isEmpty()) {
      return null;
    }
    double suma = 0;
    for (Double v : valores) {
      suma += v;
    }
    return Math.round(suma / valores.size() * 10.0) / 10.0;
  }
}
