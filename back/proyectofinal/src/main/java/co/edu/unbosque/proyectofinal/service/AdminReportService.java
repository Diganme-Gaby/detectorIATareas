package co.edu.unbosque.proyectofinal.service;

import co.edu.unbosque.proyectofinal.dto.AuditDTO;
import co.edu.unbosque.proyectofinal.dto.ChartImageDTO;
import co.edu.unbosque.proyectofinal.model.Audit.Action;
import co.edu.unbosque.proyectofinal.util.PdfReportGenerator;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

/** Reportes PDF del módulo administrativo. Cada descarga queda en la auditoría. */
@Service
public class AdminReportService {

  /** Servicio de usuarios. */
  private final UserService userService;

  /** Servicio de auditoría. */
  private final AuditService auditService;

  /** Servicio de estadísticas. */
  private final StatsService statsService;

  /** Generador de PDF. */
  private final PdfReportGenerator pdf;

  /**
   * Spring inyecta aquí las dependencias.
   *
   * @param userService servicio de usuarios
   * @param auditService servicio de auditoría
   * @param statsService servicio de estadísticas
   * @param pdf generador de PDF
   */
  public AdminReportService(UserService userService, AuditService auditService, StatsService statsService,
      PdfReportGenerator pdf) {
    this.userService = userService;
    this.auditService = auditService;
    this.statsService = statsService;
    this.pdf = pdf;
  }

  /**
   * Devuelve el PDF con todos los usuarios.
   *
   * @param admin administrador que lo pide
   * @param ip IP de origen
   * @return PDF con todos los usuarios
   */
  public byte[] usuarios(String admin, String ip) {
    byte[] bytes = pdf.reporteUsuarios(userService.getAll(), admin);
    auditService.registrar(admin, Action.ADMIN_DESCARGA_REPORTE, "Reporte de usuarios", ip);
    return bytes;
  }

  /**
   * Devuelve el PDF de la auditoría filtrada.
   *
   * @param username filtro de usuario
   * @param accion filtro de acción
   * @param desde fecha mínima
   * @param hasta fecha máxima
   * @param admin administrador que lo pide
   * @param ip IP de origen
   * @return PDF de la auditoría filtrada
   */
  public byte[] auditoria(String username, Action accion, LocalDateTime desde, LocalDateTime hasta,
      String admin, String ip) {
    List<AuditDTO> registros = auditService.buscar(username, accion, desde, hasta);
    String filtros = "usuario=" + (username == null || username.isBlank() ? "todos" : username)
        + ", accion=" + (accion == null ? "todas" : accion)
        + ", desde=" + (desde == null ? "-" : desde.toLocalDate())
        + ", hasta=" + (hasta == null ? "-" : hasta.toLocalDate());
    byte[] bytes = pdf.reporteAuditoria(registros, filtros, admin);
    auditService.registrar(admin, Action.ADMIN_DESCARGA_REPORTE, "Reporte de auditoría (" + filtros + ")", ip);
    return bytes;
  }

  /**
   * Devuelve el PDF de estadísticas.
   *
   * @param graficas imágenes de Chart.js, puede ser null
   * @param admin administrador que lo pide
   * @param ip IP de origen
   * @return PDF de estadísticas
   */
  public byte[] estadisticas(List<ChartImageDTO> graficas, String admin, String ip) {
    byte[] bytes = pdf.reporteEstadisticas(statsService.calcular(), graficas == null ? List.of() : graficas, admin);
    auditService.registrar(admin, Action.ADMIN_DESCARGA_REPORTE, "Reporte de estadisticas", ip);
    return bytes;
  }
}
