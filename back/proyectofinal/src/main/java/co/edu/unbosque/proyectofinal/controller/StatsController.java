package co.edu.unbosque.proyectofinal.controller;

import co.edu.unbosque.proyectofinal.dto.ChartImageDTO;
import co.edu.unbosque.proyectofinal.dto.StatsDTO;
import co.edu.unbosque.proyectofinal.service.AdminReportService;
import co.edu.unbosque.proyectofinal.service.StatsService;
import co.edu.unbosque.proyectofinal.util.NetworkUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controlador REST de estadísticas de uso para el módulo administrativo. Solo ADMIN. */
@RestController
@RequestMapping("/admin/stats")
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:4200", "https://url-del-front.netlify.app"})
@Tag(name = "Estadísticas", description = "Uso del aplicativo y comportamiento de cada IA (solo ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class StatsController {

  /** Servicio de estadísticas. */
  private final StatsService statsService;

  /** Reportes administrativos. */
  private final AdminReportService reportService;

  /**
   * Spring inyecta aquí las dependencias.
   *
   * @param statsService servicio de estadísticas
   * @param reportService reportes administrativos
   */
  public StatsController(StatsService statsService, AdminReportService reportService) {
    this.statsService = statsService;
    this.reportService = reportService;
  }

  /**
   * Estadísticas actuales.
   *
   * @return estadísticas
   */
  @Operation(
      summary = "Resumen de estadísticas",
      description =
          """
            Devuelve los datos listos para graficar con Chart.js: análisis e inicios de sesión por día (30 días),
            uso por día de la semana, archivos por tipo y por nivel, usuarios con más análisis,
            porcentaje promedio de cada IA y veces que cada IA no respondió.

            **Nota:** Requiere rol ADMIN.
            """)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Estadísticas",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StatsDTO.class)))
      })
  @GetMapping("/summary")
  public ResponseEntity<StatsDTO> summary() {
    return ResponseEntity.ok(statsService.calcular());
  }

  /**
   * Reporte PDF de estadísticas, con las gráficas del front si se envían.
   *
   * @param graficas imágenes de Chart.js
   * @param descargar true para descargar
   * @param authentication administrador en sesión
   * @param request petición HTTP
   * @return PDF
   */
  @Operation(
      summary = "Reporte PDF de estadísticas",
      description =
          """
            Genera el PDF de estadísticas. El cuerpo es opcional: el front puede enviar las gráficas de Chart.js
            convertidas a imagen con `chart.toBase64Image()` y se incluyen en el PDF junto con las tablas.

            **Nota:** Requiere rol ADMIN.
            """)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "PDF generado", content = @Content(mediaType = "application/pdf"))})
  @PostMapping(path = "/reportpdf", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> reportPdf(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Gráficas opcionales",
              required = false,
              content = @Content(examples = @ExampleObject(value = """
                    [
                      {"titulo": "Análisis por día", "imagenBase64": "data:image/png;base64,iVBORw0KGgo..."}
                    ]
                """)))
          @RequestBody(required = false)
          List<ChartImageDTO> graficas,
      @Parameter(description = "true descarga el archivo, false lo muestra", example = "true")
          @RequestParam(defaultValue = "true") boolean descargar,
      @Parameter(hidden = true) Authentication authentication,
      HttpServletRequest request) {
    byte[] pdf = reportService.estadisticas(graficas, authentication.getName(), NetworkUtil.obtenerIpCliente(request));
    return AdminController.respuestaPdf(pdf, "reporte-estadisticas.pdf", descargar);
  }
}
