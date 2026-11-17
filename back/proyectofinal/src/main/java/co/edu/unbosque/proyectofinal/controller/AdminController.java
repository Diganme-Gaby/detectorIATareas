package co.edu.unbosque.proyectofinal.controller;

import co.edu.unbosque.proyectofinal.dto.AnalysisDTO;
import co.edu.unbosque.proyectofinal.dto.UpdateProfileDTO;
import co.edu.unbosque.proyectofinal.dto.UserDTO;
import co.edu.unbosque.proyectofinal.model.Analysis.Status;
import co.edu.unbosque.proyectofinal.model.AnalyzedFile.Category;
import co.edu.unbosque.proyectofinal.service.AdminReportService;
import co.edu.unbosque.proyectofinal.service.AnalysisService;
import co.edu.unbosque.proyectofinal.service.UserService;
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
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST de administración de usuarios y análisis. Todos los endpoints requieren rol
 * ADMIN. No existe endpoint para borrar usuarios ni análisis, y el nombre de usuario y el correo
 * no se pueden cambiar.
 */
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:4200", "https://url-del-front.netlify.app"})
@Tag(name = "Administración de usuarios", description = "Gestión de cuentas y reportes de usuarios (solo ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

  /** Servicio de usuarios. */
  private final UserService userServ;

  /** Servicio de análisis. */
  private final AnalysisService analysisService;

  /** Reportes administrativos. */
  private final AdminReportService reportService;

  /**
   * Spring inyecta aquí las dependencias.
   *
   * @param userServ servicio de usuarios
   * @param analysisService servicio de análisis
   * @param reportService reportes administrativos
   */
  public AdminController(UserService userServ, AnalysisService analysisService, AdminReportService reportService) {
    this.userServ = userServ;
    this.analysisService = analysisService;
    this.reportService = reportService;
  }

  /**
   * Lista todos los usuarios.
   *
   * @return usuarios
   */
  @Operation(
      summary = "Obtener todos los usuarios",
      description = "Lista todas las cuentas del sistema, de la más reciente a la más antigua. **Requiere rol ADMIN.**")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "202", description = "Lista de usuarios",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "204", description = "No hay usuarios", content = @Content)
      })
  @GetMapping("/user/getall")
  public ResponseEntity<List<UserDTO>> getAll() {
    List<UserDTO> users = userServ.getAll();
    if (users.isEmpty()) {
      return new ResponseEntity<>(users, HttpStatus.NO_CONTENT);
    }
    return new ResponseEntity<>(users, HttpStatus.ACCEPTED);
  }

  /**
   * Cuenta los usuarios.
   *
   * @return cantidad
   */
  @Operation(summary = "Contar usuarios", description = "Número total de cuentas registradas. **Requiere rol ADMIN.**")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "202", description = "Conteo exitoso",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "5")))
      })
  @GetMapping("/user/count")
  public ResponseEntity<Long> countAll() {
    return new ResponseEntity<>(userServ.count(), HttpStatus.ACCEPTED);
  }

  /**
   * Busca un usuario por ID.
   *
   * @param id usuario
   * @return usuario o 404
   */
  @Operation(summary = "Obtener usuario por ID", description = "Datos de una cuenta. **Requiere rol ADMIN.**")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "202", description = "Usuario encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
      })
  @GetMapping("/user/getbyid/{id}")
  public ResponseEntity<UserDTO> getById(
      @Parameter(description = "ID del usuario", required = true, example = "2") @PathVariable Long id) {
    UserDTO found = userServ.getById(id);
    if (found == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    return new ResponseEntity<>(found, HttpStatus.ACCEPTED);
  }

  /**
   * Actualiza nombre, teléfono y rol de un usuario.
   *
   * @param id usuario
   * @param data datos nuevos
   * @param authentication administrador en sesión
   * @param request petición HTTP
   * @return resultado
   */
  @Operation(
      summary = "Actualizar usuario (JSON)",
      description =
          """
            Cambia el nombre completo, el teléfono y el rol de una cuenta.

            **Reglas:**

            * El nombre de usuario y el correo **no se pueden cambiar**.
            * Un administrador no puede quitarse su propio rol ADMIN.

            **Nota:** Requiere rol ADMIN.
            """)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "202", description = "Usuario actualizado",
            content = @Content(examples = @ExampleObject(value = "Usuario actualizado exitosamente"))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content),
        @ApiResponse(responseCode = "409", description = "Intento de quitarse el propio rol ADMIN", content = @Content)
      })
  @PutMapping(path = "/user/updatejson/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> updateJson(
      @Parameter(description = "ID del usuario", required = true, example = "2") @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Datos nuevos",
              required = true,
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = UpdateProfileDTO.class),
                      examples = @ExampleObject(value = """
                    {
                      "nombreCompleto": "Usuario de pruebas",
                      "telefono": "3000000000",
                      "role": "USER"
                    }
                """)))
          @RequestBody
          UpdateProfileDTO data,
      @Parameter(hidden = true) Authentication authentication,
      HttpServletRequest request) {
    if (data == null || !InputValidator.nombreValido(data.getNombreCompleto())) {
      return new ResponseEntity<>("El nombre es obligatorio, máximo 100 caracteres y sin caracteres especiales de HTML", HttpStatus.BAD_REQUEST);
    }
    if (!InputValidator.telefonoValido(data.getTelefono())) {
      return new ResponseEntity<>("El teléfono debe tener entre 7 y 20 dígitos", HttpStatus.BAD_REQUEST);
    }
    int status = userServ.adminUpdate(id, data, authentication.getName(), NetworkUtil.obtenerIpCliente(request));
    return switch (status) {
      case 0 -> new ResponseEntity<>("Usuario actualizado exitosamente", HttpStatus.ACCEPTED);
      case 2 -> new ResponseEntity<>("No puedes quitarte tu propio rol de administrador", HttpStatus.CONFLICT);
      default -> new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
    };
  }

  /**
   * Activa una cuenta.
   *
   * @param id usuario
   * @param authentication administrador en sesión
   * @param request petición HTTP
   * @return resultado
   */
  @Operation(summary = "Activar cuenta", description = "Permite que la cuenta vuelva a iniciar sesión. **Requiere rol ADMIN.**")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "202", description = "Cuenta activada", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
      })
  @PutMapping("/user/enable/{id}")
  public ResponseEntity<String> enable(
      @Parameter(description = "ID del usuario", required = true, example = "2") @PathVariable Long id,
      @Parameter(hidden = true) Authentication authentication,
      HttpServletRequest request) {
    int status = userServ.setActive(id, true, authentication.getName(), NetworkUtil.obtenerIpCliente(request));
    if (status == 0) {
      return new ResponseEntity<>("Cuenta activada", HttpStatus.ACCEPTED);
    }
    return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
  }

  /**
   * Desactiva una cuenta. El token que tenga deja de servir de inmediato.
   *
   * @param id usuario
   * @param authentication administrador en sesión
   * @param request petición HTTP
   * @return resultado
   */
  @Operation(
      summary = "Desactivar cuenta",
      description =
          """
            Impide el inicio de sesión. Si el usuario tiene una sesión abierta, su token deja de funcionar
            en la siguiente petición. El historial del usuario se conserva.

            **Nota:** Un administrador no puede desactivarse a sí mismo. Requiere rol ADMIN.
            """)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "202", description = "Cuenta desactivada", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content),
        @ApiResponse(responseCode = "409", description = "Intento de desactivarse a sí mismo", content = @Content)
      })
  @PutMapping("/user/disable/{id}")
  public ResponseEntity<String> disable(
      @Parameter(description = "ID del usuario", required = true, example = "2") @PathVariable Long id,
      @Parameter(hidden = true) Authentication authentication,
      HttpServletRequest request) {
    int status = userServ.setActive(id, false, authentication.getName(), NetworkUtil.obtenerIpCliente(request));
    return switch (status) {
      case 0 -> new ResponseEntity<>("Cuenta desactivada", HttpStatus.ACCEPTED);
      case 2 -> new ResponseEntity<>("No puedes desactivar tu propia cuenta", HttpStatus.CONFLICT);
      default -> new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
    };
  }

  /**
   * Quita el bloqueo por intentos fallidos.
   *
   * @param id usuario
   * @param authentication administrador en sesión
   * @param request petición HTTP
   * @return resultado
   */
  @Operation(summary = "Desbloquear cuenta", description = "Quita el bloqueo de 15 minutos por intentos fallidos. **Requiere rol ADMIN.**")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "202", description = "Cuenta desbloqueada", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
      })
  @PutMapping("/user/unlock/{id}")
  public ResponseEntity<String> unlock(
      @Parameter(description = "ID del usuario", required = true, example = "2") @PathVariable Long id,
      @Parameter(hidden = true) Authentication authentication,
      HttpServletRequest request) {
    int status = userServ.unlock(id, authentication.getName(), NetworkUtil.obtenerIpCliente(request));
    if (status == 0) {
      return new ResponseEntity<>("Cuenta desbloqueada", HttpStatus.ACCEPTED);
    }
    return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
  }

  /**
   * Reporte PDF de usuarios.
   *
   * @param descargar true para descargar
   * @param authentication administrador en sesión
   * @param request petición HTTP
   * @return PDF
   */
  @Operation(summary = "Reporte PDF de usuarios", description = "PDF con todas las cuentas: usuario, nombre, correo, rol, estado, registro y último acceso. **Requiere rol ADMIN.**")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "PDF generado", content = @Content(mediaType = "application/pdf"))})
  @GetMapping(path = "/user/reportpdf", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> usersPdf(
      @Parameter(description = "true descarga el archivo, false lo muestra", example = "true")
          @RequestParam(defaultValue = "true") boolean descargar,
      @Parameter(hidden = true) Authentication authentication,
      HttpServletRequest request) {
    byte[] pdf = reportService.usuarios(authentication.getName(), NetworkUtil.obtenerIpCliente(request));
    return respuestaPdf(pdf, "reporte-usuarios.pdf", descargar);
  }

  /**
   * Todos los análisis del sistema.
   *
   * @param desde fecha mínima
   * @param hasta fecha máxima
   * @param categoria tipo de archivo
   * @param estado estado
   * @param nivel nivel de IA
   * @param texto texto a buscar
   * @return análisis
   */
  @Operation(
      summary = "Todos los análisis",
      description = "Lista los análisis de todos los usuarios con los mismos filtros del historial. **Requiere rol ADMIN.**")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Análisis",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnalysisDTO.class)))
      })
  @GetMapping("/analysis/getall")
  public ResponseEntity<List<AnalysisDTO>> allAnalyses(
      @Parameter(description = "Fecha mínima (yyyy-MM-dd). Si el formato no es válido el filtro se ignora")
          @RequestParam(required = false) String desde,
      @Parameter(description = "Fecha máxima (yyyy-MM-dd). Si el formato no es válido el filtro se ignora")
          @RequestParam(required = false) String hasta,
      @Parameter(description = "Tipo de archivo") @RequestParam(required = false) Category categoria,
      @Parameter(description = "Estado del análisis") @RequestParam(required = false) Status estado,
      @Parameter(description = "Nivel de IA") @RequestParam(required = false) String nivel,
      @Parameter(description = "Texto a buscar") @RequestParam(required = false) String texto) {
    return ResponseEntity.ok(analysisService.todos(
        InputValidator.fecha(desde), InputValidator.fecha(hasta), categoria, estado, nivel, texto));
  }

  /**
   * Devuelve la respuesta HTTP.
   *
   * @param pdf bytes del PDF
   * @param nombre nombre sugerido
   * @param descargar true para attachment
   * @return respuesta HTTP
   */
  static ResponseEntity<byte[]> respuestaPdf(byte[] pdf, String nombre, boolean descargar) {
    ContentDisposition cd = (descargar ? ContentDisposition.attachment() : ContentDisposition.inline())
        .filename(nombre)
        .build();
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
        .body(pdf);
  }
}
