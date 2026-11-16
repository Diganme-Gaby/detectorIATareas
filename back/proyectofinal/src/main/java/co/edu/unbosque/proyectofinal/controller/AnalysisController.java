package co.edu.unbosque.proyectofinal.controller;

import co.edu.unbosque.proyectofinal.dto.AnalysisDTO;
import co.edu.unbosque.proyectofinal.dto.GithubAnalysisDTO;
import co.edu.unbosque.proyectofinal.exception.AnalysisRequestException;
import co.edu.unbosque.proyectofinal.model.Analysis.Status;
import co.edu.unbosque.proyectofinal.model.AnalyzedFile.Category;
import co.edu.unbosque.proyectofinal.service.AnalysisReportService.PdfFile;
import co.edu.unbosque.proyectofinal.service.AnalysisService;
import co.edu.unbosque.proyectofinal.util.InputValidator;
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
import java.util.Map;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador REST de los análisis: envío de archivos o repositorios, consulta del estado, historial
 * con filtros y descarga de reportes PDF. Accesible para USER y ADMIN; cada usuario solo ve sus
 * propios análisis (el ADMIN puede ver todos).
 */
@RestController
@RequestMapping("/analysis")
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:4200", "https://url-del-front.netlify.app"})
@Tag(name = "Análisis de IA", description = "Detección de contenido generado con IA usando Gemini, Groq y OpenRouter, con modelos de respaldo")
@SecurityRequirement(name = "bearerAuth")
public class AnalysisController {

  /** Ejemplo de respuesta al crear un análisis. */
  private static final String EJEMPLO_CREADO =
      """
        {
          "id": 12,
          "username": "laura.gomez",
          "titulo": "Taller 3",
          "origen": "ARCHIVOS",
          "estado": "PENDIENTE",
          "fechaCreacion": "2026-11-11T10:20:00",
          "nivel": "SIN_RESULTADO",
          "totalArchivos": 2,
          "correoEnviado": false,
          "archivos": []
        }
      """;

  /** Servicio de análisis. */
  private final AnalysisService analysisService;

  /**
   * Spring inyecta aquí las dependencias.
   *
   * @param analysisService servicio de análisis
   */
  public AnalysisController(AnalysisService analysisService) {
    this.analysisService = analysisService;
  }

  /**
   * Envía uno o varios archivos para analizar.
   *
   * @param archivos archivos
   * @param titulo título opcional
   * @param authentication usuario autenticado
   * @param request petición HTTP
   * @return análisis creado
   */
  @Operation(
      summary = "Analizar archivos",
      description =
          """
            Recibe uno o varios archivos de **cualquier tipo** (texto, código, PDF, Word, Excel, PowerPoint,
            imágenes, audio, video o .zip) y los analiza con las tres IAs.

            **¿Cómo funciona?**

            1. El análisis queda en estado `PENDIENTE` y se responde de inmediato con su `id` (código 202).
            2. En segundo plano cada archivo se prepara (se extrae el texto, se transcribe el audio, se toman fotogramas del video) y se envía a Gemini, Groq y OpenRouter.
            3. Consulta `/analysis/getbyid/{id}` hasta que el estado sea `COMPLETADO`, `COMPLETADO_PARCIAL` o `FALLIDO`.
            4. Al terminar llega un correo con los reportes PDF adjuntos.

            **Reglas:**

            * Máximo 10 archivos por envío y 20 MB por archivo.
            * Cada archivo dentro de un .zip se analiza por separado y tiene su propio reporte.
            * Si una IA no responde se consulta un modelo de respaldo (NVIDIA, Cohere o GitHub Models). Solo si
              tampoco responden, el reporte incluye las que sí respondieron y un aviso.
            """)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "202", description = "Análisis aceptado y en proceso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnalysisDTO.class),
                examples = @ExampleObject(value = EJEMPLO_CREADO))),
        @ApiResponse(responseCode = "400", description = "Sin archivos, demasiados archivos o zip dañado",
            content = @Content(examples = @ExampleObject(value = "Puedes enviar máximo 10 archivos por análisis.")))
      })
  @PostMapping(path = "/uploadfiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> uploadFiles(
      @RequestPart("archivos") MultipartFile[] archivos,
      @Parameter(description = "Título del análisis (opcional)", example = "Taller 3 de Bases de Datos")
          @RequestParam(value = "titulo", required = false)
          String titulo,
      @Parameter(hidden = true) Authentication authentication,
      HttpServletRequest request) {
    try {
      AnalysisDTO dto = analysisService.crearDesdeArchivos(
          authentication.getName(), archivos, titulo, NetworkUtil.obtenerIpCliente(request));
      return ResponseEntity.status(HttpStatus.ACCEPTED).body(dto);
    } catch (AnalysisRequestException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }

  /**
   * Analiza un repositorio público de GitHub.
   *
   * @param solicitud URL y restricciones
   * @param authentication usuario autenticado
   * @param request petición HTTP
   * @return análisis creado
   */
  @Operation(
      summary = "Analizar un repositorio de GitHub",
      description =
          """
            Descarga un repositorio **público** de GitHub y analiza sus archivos.

            **Restricciones que puedes enviar:**

            * `rama`: si va vacía se usa la rama por defecto. También se acepta en la URL (`/tree/rama`).
            * `carpeta`: limita el análisis a una carpeta, por ejemplo `back/src/main/java`.
            * `extensiones`: lista de extensiones, por ejemplo `["java", "ts"]`. Si va vacía se usan las del servidor.
            * `maxArchivos`: cantidad máxima (el servidor tiene un tope de 15).

            Se ignoran carpetas generadas como `node_modules`, `target`, `dist` y `.git`.
            """)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "202", description = "Análisis aceptado y en proceso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnalysisDTO.class))),
        @ApiResponse(responseCode = "400", description = "URL inválida, repositorio privado o sin archivos que cumplan las restricciones",
            content = @Content(examples = @ExampleObject(value = "El repositorio no existe o es privado.")))
      })
  @PostMapping(path = "/github", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> github(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Repositorio y restricciones",
              required = true,
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = GithubAnalysisDTO.class),
                      examples = @ExampleObject(value = """
                    {
                      "url": "https://github.com/usuario/proyecto",
                      "rama": "main",
                      "carpeta": "back/src/main/java",
                      "extensiones": ["java"],
                      "maxArchivos": 10,
                      "titulo": "Revisión del back"
                    }
                """)))
          @RequestBody
          GithubAnalysisDTO solicitud,
      @Parameter(hidden = true) Authentication authentication,
      HttpServletRequest request) {
    try {
      AnalysisDTO dto = analysisService.crearDesdeGithub(
          authentication.getName(), solicitud, NetworkUtil.obtenerIpCliente(request));
      return ResponseEntity.status(HttpStatus.ACCEPTED).body(dto);
    } catch (AnalysisRequestException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }

  /**
   * Historial del usuario con filtros.
   *
   * @param desde fecha mínima
   * @param hasta fecha máxima
   * @param categoria tipo de archivo
   * @param estado estado
   * @param nivel nivel de IA
   * @param texto texto a buscar
   * @param authentication usuario autenticado
   * @return análisis del usuario
   */
  @Operation(
      summary = "Mi historial de análisis",
      description =
          """
            Lista los análisis del usuario, del más reciente al más antiguo. El historial es permanente: no se puede borrar.

            **Filtros (todos opcionales y combinables):**

            * `desde` / `hasta`: rango de fechas (yyyy-MM-dd).
            * `categoria`: análisis que tengan algún archivo de ese tipo.
            * `estado`: PENDIENTE, PROCESANDO, COMPLETADO, COMPLETADO_PARCIAL o FALLIDO.
            * `nivel`: ALTO (70% o más), MEDIO (40% a 69%), BAJO (menos de 40%) o SIN_RESULTADO.
            * `texto`: busca en el título y en los nombres de archivo.

            Los archivos vienen sin el detalle de cada IA; para verlo usa `/analysis/getbyid/{id}`.
            """)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Historial",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnalysisDTO.class)))
      })
  @GetMapping("/history")
  public ResponseEntity<List<AnalysisDTO>> history(
      @Parameter(description = "Fecha mínima (yyyy-MM-dd). Si el formato no es válido el filtro se ignora", example = "2026-11-01")
          @RequestParam(required = false) String desde,
      @Parameter(description = "Fecha máxima (yyyy-MM-dd). Si el formato no es válido el filtro se ignora", example = "2026-11-30")
          @RequestParam(required = false) String hasta,
      @Parameter(description = "Tipo de archivo") @RequestParam(required = false) Category categoria,
      @Parameter(description = "Estado del análisis") @RequestParam(required = false) Status estado,
      @Parameter(description = "Nivel de IA: ALTO, MEDIO, BAJO o SIN_RESULTADO", example = "ALTO")
          @RequestParam(required = false) String nivel,
      @Parameter(description = "Texto en el título o en los nombres de archivo", example = "taller")
          @RequestParam(required = false) String texto,
      @Parameter(hidden = true) Authentication authentication) {
    return ResponseEntity.ok(
        analysisService.historial(authentication.getName(), InputValidator.fecha(desde), InputValidator.fecha(hasta),
            categoria, estado, nivel, texto));
  }

  /**
   * Detalle de un análisis con el resultado de cada IA.
   *
   * @param id análisis
   * @param authentication usuario autenticado
   * @return detalle
   */
  @Operation(
      summary = "Detalle y estado de un análisis",
      description =
          """
            Devuelve el estado del análisis y, por cada archivo, el porcentaje y los motivos de cada IA,
            el promedio y los avisos de las IAs que no respondieron.

            El front puede consultar este endpoint cada pocos segundos mientras el estado sea `PENDIENTE` o `PROCESANDO`.
            """)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Detalle del análisis",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnalysisDTO.class))),
        @ApiResponse(responseCode = "404", description = "No existe o no es tuyo", content = @Content)
      })
  @GetMapping("/getbyid/{id}")
  public ResponseEntity<AnalysisDTO> getById(
      @Parameter(description = "ID del análisis", required = true, example = "12") @PathVariable Long id,
      @Parameter(hidden = true) Authentication authentication) {
    AnalysisDTO dto = analysisService.detalle(id, authentication.getName(), esAdmin(authentication));
    if (dto == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    return ResponseEntity.ok(dto);
  }

  /**
   * Reporte en PDF del análisis completo o de uno solo de sus archivos.
   *
   * @param id análisis
   * @param archivoId archivo del análisis; si no se envía, se genera el reporte consolidado
   * @param descargar true para descargar, false para ver en el navegador
   * @param authentication usuario autenticado
   * @param request petición HTTP
   * @return PDF
   */
  @Operation(
      summary = "Reporte PDF del análisis o de un archivo",
      description =
          """
            Sin `archivoId` devuelve el reporte **consolidado**: el resumen de todos los archivos y el detalle de
            cada uno (usuario, archivo, fecha, modelo de cada IA, porcentaje, veredicto y motivos).

            Con `archivoId` devuelve el reporte de **un solo archivo** del análisis. Sirve para los .zip y los
            repositorios, donde cada archivo tiene su propio reporte. El `archivoId` es el `id` de cada elemento
            de `archivos` en `/analysis/getbyid/{id}`.

            Con `descargar=false` el PDF se abre en el navegador; con `descargar=true` se descarga.
            """)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "PDF generado", content = @Content(mediaType = "application/pdf")),
        @ApiResponse(responseCode = "404", description = "No existe, no es tuyo o el archivo no es de ese análisis",
            content = @Content)
      })
  @GetMapping(path = "/reportpdf/{id}", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> reportPdf(
      @Parameter(description = "ID del análisis", required = true, example = "12") @PathVariable Long id,
      @Parameter(description = "ID de un archivo del análisis (opcional)", example = "31")
          @RequestParam(required = false) Long archivoId,
      @Parameter(description = "true descarga el archivo, false lo muestra", example = "false")
          @RequestParam(defaultValue = "false") boolean descargar,
      @Parameter(hidden = true) Authentication authentication,
      HttpServletRequest request) {
    String ip = NetworkUtil.obtenerIpCliente(request);
    PdfFile pdf = archivoId == null
        ? analysisService.pdfAnalisis(id, authentication.getName(), esAdmin(authentication), ip)
        : analysisService.pdfArchivo(id, archivoId, authentication.getName(), esAdmin(authentication), ip);
    return respuestaPdf(pdf, descargar);
  }

  /**
   * Reenvía el correo con los reportes.
   *
   * @param id análisis
   * @param authentication usuario autenticado
   * @param request petición HTTP
   * @return resultado
   */
  @Operation(
      summary = "Reenviar reporte al correo",
      description = "Vuelve a enviar al correo del usuario el reporte del análisis con los PDF adjuntos.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Correo enviado",
            content = @Content(examples = @ExampleObject(value = "Reporte enviado a tu correo"))),
        @ApiResponse(responseCode = "404", description = "No existe o no es tuyo", content = @Content),
        @ApiResponse(responseCode = "409", description = "El análisis todavía está en proceso", content = @Content),
        @ApiResponse(responseCode = "503", description = "El servidor de correo no respondió", content = @Content)
      })
  @PostMapping("/resendemail/{id}")
  public ResponseEntity<String> resendEmail(
      @Parameter(description = "ID del análisis", required = true, example = "12") @PathVariable Long id,
      @Parameter(hidden = true) Authentication authentication,
      HttpServletRequest request) {
    int r = analysisService.reenviarCorreo(id, authentication.getName(), esAdmin(authentication),
        NetworkUtil.obtenerIpCliente(request));
    return switch (r) {
      case 0 -> ResponseEntity.ok("Reporte enviado a tu correo");
      case 1 -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Análisis no encontrado");
      case 2 -> ResponseEntity.status(HttpStatus.CONFLICT).body("El análisis todavía está en proceso");
      default -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("No se pudo enviar el correo, intenta más tarde");
    };
  }

  /**
   * Límites vigentes del servidor.
   *
   * @return límites
   */
  @Operation(summary = "Límites de envío", description = "Cantidad máxima de archivos y extensiones por defecto, para validar en el front antes de enviar.")
  @GetMapping("/limits")
  public ResponseEntity<Map<String, Object>> limits() {
    return ResponseEntity.ok(analysisService.limites());
  }

  /**
   * Devuelve la respuesta HTTP.
   *
   * @param pdf PDF o null
   * @param descargar true para attachment, false para inline
   * @return respuesta HTTP
   */
  private ResponseEntity<byte[]> respuestaPdf(PdfFile pdf, boolean descargar) {
    if (pdf == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    ContentDisposition cd = (descargar ? ContentDisposition.attachment() : ContentDisposition.inline())
        .filename(pdf.getNombre())
        .build();
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
        .body(pdf.getContenido());
  }

  /**
   * Devuelve true si tiene rol ADMIN.
   *
   * @param authentication usuario autenticado
   * @return true si tiene rol ADMIN
   */
  private boolean esAdmin(Authentication authentication) {
    for (GrantedAuthority a : authentication.getAuthorities()) {
      if ("ROLE_ADMIN".equals(a.getAuthority())) {
        return true;
      }
    }
    return false;
  }
}
