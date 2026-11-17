package co.edu.unbosque.proyectofinal.util;

import co.edu.unbosque.proyectofinal.dto.AnalysisDTO;
import co.edu.unbosque.proyectofinal.dto.AnalyzedFileDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/**
 * Envío de correos con JavaMail: confirmación de cuenta, recuperación de contraseña y reportes de
 * análisis con los PDF adjuntos.
 *
 * <p>Todos los datos que escribe el usuario (nombre, título, nombres de archivo) se escapan antes
 * de meterlos en el HTML del correo.
 */
@Component
public class EmailSender {

  /** Logger para dejar registro en consola. */
  private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

  /** Formato de fecha de los correos. */
  private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  /** Verde institucional de la Universidad El Bosque. */
  private static final String VERDE = "#006b3f";

  /** Gris de las líneas finas. */
  private static final String LINEA = "#d6d6d6";

  /** Gris del texto secundario. */
  private static final String TEXTO_SUAVE = "#666666";

  /** Naranja institucional de la Universidad El Bosque. */
  private static final String NARANJA = "#e87722";

  /** Naranja oscuro para avisos, legible sobre fondo blanco. */
  private static final String NARANJA_TEXTO = "#b0520a";

  /**
   * Fuentes del correo, las mismas de un documento institucional. Cada cliente de correo usa la
   * primera de la lista que tenga instalada.
   */
  private static final String FUENTES = "Calibri,Carlito,'Segoe UI',Helvetica,Arial,sans-serif";

  /** Cliente SMTP configurado por Spring Boot. */
  private final JavaMailSender mailSender;

  /** Cuenta que envía los correos. */
  @Value("${spring.mail.username}")
  private String remitente;

  /** Nombre visible del remitente. */
  @Value("${app.mail.from-name:Detector IA}")
  private String nombreRemitente;

  /** URL base del front para armar los enlaces. */
  @Value("${app.frontend-url:http://localhost:4200}")
  private String frontendUrl;

  /**
   * Spring inyecta aquí las dependencias.
   *
   * @param mailSender cliente SMTP
   */
  public EmailSender(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  /**
   * Envía el enlace para confirmar la cuenta.
   *
   * @param destino correo del usuario
   * @param nombre nombre del usuario
   * @param token código de verificación
   * @return true si el servidor de correo acepto el mensaje
   */
  public boolean enviarVerificacion(String destino, String nombre, String token) {
    String enlace = frontendUrl + "/verificar-correo?token=" + token;
    String cuerpo =
        "<p>Hola " + HtmlUtils.htmlEscape(nombre) + ",</p>"
            + "<p>Gracias por registrarte en el Detector de IA. Para activar tu cuenta confirma tu"
            + " correo con el siguiente botón. El enlace vence en 24 horas.</p>"
            + boton(enlace, "Confirmar mi correo")
            + "<p style='font-size:13px;color:" + TEXTO_SUAVE + "'>Si el botón no abre, copia este enlace en el"
            + " navegador:<br>" + enlace + "</p>"
            + "<p style='font-size:13px;color:" + TEXTO_SUAVE + "'>Si no creaste esta cuenta, ignora este"
            + " mensaje.</p>";
    return enviar(destino, "Confirma tu cuenta del Detector de IA", plantilla(cuerpo), null);
  }

  /**
   * Envía el enlace para restablecer la contraseña.
   *
   * @param destino correo del usuario
   * @param nombre nombre del usuario
   * @param token código de recuperación
   * @return true si el servidor de correo acepto el mensaje
   */
  public boolean enviarRecuperacion(String destino, String nombre, String token) {
    String enlace = frontendUrl + "/restablecer-contrasena?token=" + token;
    String cuerpo =
        "<p>Hola " + HtmlUtils.htmlEscape(nombre) + ",</p>"
            + "<p>Recibimos una solicitud para cambiar tu contraseña. El enlace sirve una sola vez"
            + " y vence en 30 minutos.</p>"
            + boton(enlace, "Cambiar mi contraseña")
            + "<p style='font-size:13px;color:" + TEXTO_SUAVE + "'>Si no pediste el cambio, no hagas nada: tu"
            + " contraseña actual sigue funcionando.</p>";
    return enviar(destino, "Restablece tu contraseña", plantilla(cuerpo), null);
  }

  /**
   * Envía el resumen de un análisis terminado con los reportes adjuntos.
   *
   * @param destino correo del usuario
   * @param nombre nombre del usuario
   * @param analisis análisis con sus archivos y resultados
   * @param adjuntos nombre de archivo y contenido de cada adjunto
   * @return true si el servidor de correo acepto el mensaje
   */
  public boolean enviarReporte(
      String destino, String nombre, AnalysisDTO analisis, Map<String, byte[]> adjuntos) {
    StringBuilder filas = new StringBuilder();
    String celda = "padding:6px 8px;border-bottom:1px solid " + LINEA;
    for (AnalyzedFileDTO archivo : analisis.getArchivos()) {
      filas
          .append("<tr><td style='").append(celda).append("'>")
          .append(HtmlUtils.htmlEscape(archivo.getRuta()))
          .append("</td><td style='").append(celda).append(";text-align:right'>")
          .append(archivo.getPromedioIA() == null ? "Sin resultado" : formatoPorcentaje(archivo.getPromedioIA()))
          .append("</td><td style='").append(celda).append("'>")
          .append(nivel(archivo.getNivel()))
          .append("</td></tr>");
    }
    String general =
        analisis.getPromedioIA() == null ? "Sin resultado" : formatoPorcentaje(analisis.getPromedioIA());
    String encabezado = "padding:6px 8px;color:" + VERDE + ";border-bottom:2px solid " + VERDE
        + ";font-weight:bold;text-align:left";
    String cuerpo =
        "<p>Hola " + HtmlUtils.htmlEscape(nombre) + ",</p>"
            + "<p>Terminó el análisis <strong>" + HtmlUtils.htmlEscape(analisis.getTitulo())
            + "</strong> que enviaste el " + analisis.getFechaCreacion().format(FECHA) + ".</p>"
            + "<p>Promedio general de IA: <strong>" + general + "</strong> (nivel "
            + nivel(analisis.getNivel()).toLowerCase() + ").</p>"
            + "<table style='border-collapse:collapse;width:100%;font-size:14px;margin:12px 0'>"
            + "<tr><th style='" + encabezado + "'>Archivo</th>"
            + "<th style='" + encabezado + ";text-align:right'>Promedio IA</th>"
            + "<th style='" + encabezado + "'>Nivel</th></tr>"
            + filas
            + "</table>"
            + (analisis.getMensaje() == null
                ? ""
                : "<p style='color:" + NARANJA_TEXTO + "'>" + HtmlUtils.htmlEscape(analisis.getMensaje()) + "</p>")
            + "<p>Adjuntamos el reporte en PDF. También lo puedes ver y descargar desde tu"
            + " historial en el aplicativo:</p>"
            + boton(frontendUrl + "/historial", "Ver mi historial");
    return enviar(
        destino, "Reporte de análisis: " + analisis.getTitulo(), plantilla(cuerpo), adjuntos);
  }

  /**
   * Envía un correo HTML con adjuntos opcionales. Los errores del servidor SMTP se registran en el
   * log y no detienen el flujo que llamo el método.
   *
   * @param destino correo destino
   * @param asunto asunto
   * @param html cuerpo HTML
   * @param adjuntos adjuntos PDF o ZIP, puede ser null
   * @return true si se envió
   */
  public boolean enviar(String destino, String asunto, String html, Map<String, byte[]> adjuntos) {
    try {
      MimeMessage mensaje = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
      helper.setFrom(remitente, nombreRemitente);
      helper.setTo(destino);
      helper.setSubject(asunto);
      helper.setText(html, true);
      if (adjuntos != null) {
        for (Map.Entry<String, byte[]> adjunto : adjuntos.entrySet()) {
          String tipo =
              adjunto.getKey().toLowerCase().endsWith(".zip") ? "application/zip" : "application/pdf";
          helper.addAttachment(adjunto.getKey(), new ByteArrayResource(adjunto.getValue()), tipo);
        }
      }
      mailSender.send(mensaje);
      return true;
    } catch (MessagingException | MailException | UnsupportedEncodingException e) {
      log.error("No se pudo enviar el correo '{}' a {}: {}", asunto, destino, e.getMessage());
      return false;
    }
  }

  /**
   * Envuelve el contenido en la plantilla común de los correos.
   *
   * @param contenido HTML interno
   * @return HTML completo
   */
  private String plantilla(String contenido) {
    return "<div style=\"font-family:" + FUENTES + ";max-width:620px;margin:auto;color:#222;"
        + "font-size:15px;line-height:1.5\">"
        + "<div style='padding:18px 4px 10px;border-bottom:2px solid " + VERDE + "'>"
        + "<div style='font-size:11px;letter-spacing:1px;color:" + VERDE + ";font-weight:bold'>"
        + "UNIVERSIDAD EL BOSQUE</div>"
        + "<div style='font-size:20px;font-weight:bold;color:#222'>Detector de contenido generado con IA</div>"
        + "</div>"
        + "<div style='border-top:1px solid " + NARANJA + ";margin-top:2px'></div>"
        + "<div style='padding:18px 4px'>"
        + contenido
        + "</div>"
        + "<div style='border-top:1px solid " + LINEA + ";padding:10px 4px;font-size:12px;color:"
        + TEXTO_SUAVE + "'>Universidad El Bosque · Desarrollo de Sistemas de Información 3<br>"
        + "Este es un mensaje automático, por favor no lo respondas.</div></div>";
  }

  /**
   * Devuelve el HTML de un botón sencillo.
   *
   * @param enlace destino del enlace
   * @param texto texto del enlace
   * @return HTML de un botón sencillo
   */
  private String boton(String enlace, String texto) {
    return "<p style='margin:20px 0'><a href='" + enlace + "' style='background:" + VERDE
        + ";color:#fff;padding:9px 18px;border-radius:3px;text-decoration:none;font-weight:bold'>"
        + texto + "</a></p>";
  }

  /**
   * Devuelve el nivel para mostrar.
   *
   * @param nivel nivel ALTO, MEDIO, BAJO o SIN_RESULTADO
   * @return nivel para mostrar
   */
  private String nivel(String nivel) {
    if ("ALTO".equals(nivel)) {
      return "Alto";
    }
    if ("MEDIO".equals(nivel)) {
      return "Medio";
    }
    if ("BAJO".equals(nivel)) {
      return "Bajo";
    }
    return "Sin resultado";
  }

  /**
   * Devuelve el porcentaje con un decimal y el símbolo %.
   *
   * @param valor porcentaje
   * @return porcentaje con un decimal y el símbolo %
   */
  private String formatoPorcentaje(double valor) {
    return String.format("%.1f %%", valor);
  }
}
