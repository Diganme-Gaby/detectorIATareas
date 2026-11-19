package co.edu.unbosque.proyectofinal.controller;

import co.edu.unbosque.proyectofinal.dto.AuditDTO;
import co.edu.unbosque.proyectofinal.model.Audit.Action;
import co.edu.unbosque.proyectofinal.service.AdminReportService;
import co.edu.unbosque.proyectofinal.service.AuditService;
import co.edu.unbosque.proyectofinal.util.InputValidator;
import co.edu.unbosque.proyectofinal.util.NetworkUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST de la auditoría. Solo ADMIN. La auditoría se consulta y se exporta; no hay
 * endpoints para modificarla ni borrarla.
 */
@RestController
@RequestMapping("/admin/audit")
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:4200", "https://url-del-front.netlify.app"})
@Tag(name = "Auditoría", description = "Registro de quién hizo qué, cuándo y desde dónde (solo ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AuditController {

  /** Servicio de auditoría. */
  private final AuditService auditService;

  /** Reportes administrativos. */
  private final AdminReportService reportService;

  /**
   * Spring inyecta aquí las dependencias.
   *
   * @param auditService servicio de auditoría
   * @param reportService reportes administrativos
   */
  public AuditController(AuditService auditService, AdminReportService reportService) {
    this.auditService = auditService;
    this.reportService = reportService;
  }

  /**
   * Consulta la auditoría con filtros.
   *
   * @param username usuario
   * @param accion acción
   * @param desde fecha mínima
   * @param hasta fecha máxima
   * @return registros
   */
  @Operation(
      summary = "Consultar auditoría",
      description =
          """
            Lista los registros del más reciente al más antiguo. Cada registro trae usuario, acción,
            descripción, fecha, IP, ciudad, país, latitud, longitud y un enlace `mapaUrl` a Google Maps.

            **Filtros opcionales:** `username`, `acción`, `desde` y `hasta` (yyyy-MM-dd).

            **Nota:** Requiere rol ADMIN.
            """)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Registros de auditoría",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuditDTO.class)))
      })
  @GetMapping("/getall")
  public ResponseEntity<List<AuditDTO>> getAll(
      @Parameter(description = "Usuario exacto", example = "laura.gomez") @RequestParam(required = false) String username,
      @Parameter(description = "Acción registrada") @RequestParam(required = false) Action accion,
      @Parameter(description = "Fecha mínima (yyyy-MM-dd). Si el formato no es válido el filtro se ignora", example = "2026-11-01")
          @RequestParam(required = false) String desde,
      @Parameter(description = "Fecha máxima (yyyy-MM-dd). Si el formato no es válido el filtro se ignora", example = "2026-11-30")
          @RequestParam(required = false) String hasta) {
    return ResponseEntity.ok(auditService.buscar(
        username, accion, inicio(InputValidator.fecha(desde)), fin(InputValidator.fecha(hasta))));
  }

  /**
   * Exporta la auditoría filtrada en PDF.
   *
   * @param username usuario
   * @param accion acción
   * @param desde fecha mínima
   * @param hasta fecha máxima
   * @param descargar true para descargar
   * @param authentication administrador en sesión
   * @param request petición HTTP
   * @return PDF
   */
  @Operation(summary = "Exportar auditoría en PDF", description = "PDF de la auditoría con los mismos filtros de la consulta. **Requiere rol ADMIN.**")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "PDF generado", content = @Content(mediaType = "application/pdf"))})
  @GetMapping(path = "/reportpdf", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> reportPdf(
      @Parameter(description = "Usuario exacto") @RequestParam(required = false) String username,
      @Parameter(description = "Acción registrada") @RequestParam(required = false) Action accion,
      @Parameter(description = "Fecha mínima (yyyy-MM-dd). Si el formato no es válido el filtro se ignora")
          @RequestParam(required = false) String desde,
      @Parameter(description = "Fecha máxima (yyyy-MM-dd). Si el formato no es válido el filtro se ignora")
          @RequestParam(required = false) String hasta,
      @Parameter(description = "true descarga el archivo, false lo muestra", example = "true")
          @RequestParam(defaultValue = "true") boolean descargar,
      @Parameter(hidden = true) Authentication authentication,
      HttpServletRequest request) {
    byte[] pdf = reportService.auditoria(username, accion, inicio(InputValidator.fecha(desde)),
        fin(InputValidator.fecha(hasta)), authentication.getName(),
        NetworkUtil.obtenerIpCliente(request));
    return AdminController.respuestaPdf(pdf, "reporte-auditoria.pdf", descargar);
  }

  /**
   * Devuelve el inicio del día o null.
   *
   * @param fecha día o null
   * @return inicio del día o null
   */
  private LocalDateTime inicio(LocalDate fecha) {
    return fecha == null ? null : fecha.atStartOfDay();
  }

  /**
   * Devuelve el último instante del día o null.
   *
   * @param fecha día o null
   * @return último instante del día o null
   */
  private LocalDateTime fin(LocalDate fecha) {
    return fecha == null ? null : fecha.plusDays(1).atStartOfDay().minusNanos(1);
  }
}
