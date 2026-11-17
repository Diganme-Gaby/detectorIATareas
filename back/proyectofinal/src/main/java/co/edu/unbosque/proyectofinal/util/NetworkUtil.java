package co.edu.unbosque.proyectofinal.util;

import jakarta.servlet.http.HttpServletRequest;

/** Utilidades para saber desde qué IP llegó una petición. */
public final class NetworkUtil {

  /** Solo tiene métodos estáticos, así que no se instancia. */
  private NetworkUtil() {}

  /**
   * Obtiene la IP del cliente. Detrás de un balanceador o proxy de la nube la IP real viene en
   * X-Forwarded-For; si no está, se usa la dirección remota de la conexión.
   *
   * @param request petición HTTP
   * @return IP del cliente, o "desconocida"
   */
  public static String obtenerIpCliente(HttpServletRequest request) {
    if (request == null) {
      return "desconocida";
    }
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return limpiar(forwarded.split(",")[0]);
    }
    String realIp = request.getHeader("X-Real-IP");
    if (realIp != null && !realIp.isBlank()) {
      return limpiar(realIp);
    }
    return limpiar(request.getRemoteAddr());
  }

  /**
   * Indica si la IP es local o de una red privada. Para esas no sirve la geolocalización.
   *
   * @param ip dirección IP
   * @return true si es privada, local o desconocida
   */
  public static boolean esIpPrivada(String ip) {
    if (ip == null || ip.isBlank() || "desconocida".equals(ip)) {
      return true;
    }
    if (ip.startsWith("127.") || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1")) {
      return true;
    }
    if (ip.startsWith("10.") || ip.startsWith("192.168.") || ip.startsWith("169.254.")) {
      return true;
    }
    if (ip.startsWith("172.")) {
      String[] partes = ip.split("\\.");
      if (partes.length > 1) {
        try {
          int segundo = Integer.parseInt(partes[1]);
          return segundo >= 16 && segundo <= 31;
        } catch (NumberFormatException e) {
          return false;
        }
      }
    }
    return ip.toLowerCase().startsWith("fc") || ip.toLowerCase().startsWith("fd");
  }

  /**
   * Quita espacios y deja solo caracteres válidos de una IP.
   *
   * @param ip texto recibido
   * @return IP limpia, máximo 64 caracteres
   */
  private static String limpiar(String ip) {
    if (ip == null) {
      return "desconocida";
    }
    String limpia = ip.trim().replaceAll("[^0-9a-fA-F:.]", "");
    if (limpia.isEmpty()) {
      return "desconocida";
    }
    return limpia.length() > 64 ? limpia.substring(0, 64) : limpia;
  }
}
