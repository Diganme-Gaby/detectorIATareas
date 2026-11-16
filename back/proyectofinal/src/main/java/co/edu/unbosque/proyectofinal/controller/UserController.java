package co.edu.unbosque.proyectofinal.controller;

import co.edu.unbosque.proyectofinal.dto.ChangePasswordDTO;
import co.edu.unbosque.proyectofinal.dto.UpdateProfileDTO;
import co.edu.unbosque.proyectofinal.dto.UserDTO;
import co.edu.unbosque.proyectofinal.exception.PasswordNotValidException;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST del perfil del usuario en sesión. Accesible para USER y ADMIN. El nombre de
 * usuario y el correo no se pueden cambiar.
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:4200", "https://url-del-front.netlify.app"})
@Tag(name = "Perfil de usuario", description = "Consulta y edición de los datos del usuario en sesión")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

  /** Servicio para operaciones relacionadas con usuarios. */
  private final UserService userServ;

  /**
   * Spring inyecta aquí las dependencias.
   *
   * @param userServ servicio de usuarios
   */
  public UserController(UserService userServ) {
    this.userServ = userServ;
  }

  /**
   * Datos del usuario en sesión.
   *
   * @param authentication usuario autenticado
   * @return datos del usuario
   */
  @Operation(
      summary = "Ver mi perfil",
      description =
          """
            Devuelve los datos del usuario dueño del token. La contraseña nunca se incluye.

            **Nota:** Requiere autenticación (USER o ADMIN).
            """)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Datos del usuario",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDTO.class),
                    examples =
                        @ExampleObject(
                            value =
                                """
                        {
                          "id": 3,
                          "username": "laura.gomez",
                          "email": "laura.gomez@ejemplo.com",
                          "nombreCompleto": "Laura Gomez",
                          "telefono": "3001234567",
                          "role": "USER",
                          "activo": true,
                          "correoVerificado": true,
                          "bloqueado": false,
                          "intentosFallidos": 0,
                          "fechaRegistro": "2026-11-10T21:15:00",
                          "ultimoAcceso": "2026-11-11T08:02:41"
                        }
                    """))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
      })
  @GetMapping("/me")
  public ResponseEntity<UserDTO> me(@Parameter(hidden = true) Authentication authentication) {
    UserDTO dto = userServ.getByUsername(authentication.getName());
    if (dto == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    return ResponseEntity.ok(dto);
  }

  /**
   * Actualiza nombre y teléfono del usuario en sesión.
   *
   * @param data datos nuevos
   * @param authentication usuario autenticado
   * @param request petición HTTP, para la auditoría
   * @return resultado
   */
  @Operation(
      summary = "Actualizar mi perfil",
      description =
          """
            Cambia el nombre completo y el teléfono. El campo `role` se ignora en este endpoint.

            **Nota:** El nombre de usuario y el correo no se pueden cambiar.
            """)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "202", description = "Perfil actualizado",
            content = @Content(examples = @ExampleObject(value = "Perfil actualizado exitosamente"))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(examples = @ExampleObject(value = "El nombre es obligatorio, máximo 100 caracteres y sin caracteres especiales de HTML")))
      })
  @PutMapping(path = "/updateprofile", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> updateProfile(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Datos nuevos",
              required = true,
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = UpdateProfileDTO.class),
                      examples = @ExampleObject(value = """
                    {
                      "nombreCompleto": "Laura Gomez Rios",
                      "telefono": "3109876543"
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
    int status = userServ.updateProfile(authentication.getName(), data, NetworkUtil.obtenerIpCliente(request));
    if (status == 0) {
      return new ResponseEntity<>("Perfil actualizado exitosamente", HttpStatus.ACCEPTED);
    }
    return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
  }

  /**
   * Cambia la contraseña del usuario en sesión.
   *
   * @param data contraseña actual y nueva
   * @param authentication usuario autenticado
   * @param request petición HTTP, para la auditoría
   * @return resultado
   */
  @Operation(
      summary = "Cambiar mi contraseña",
      description =
          """
            Pide la contraseña actual y la nueva. La nueva debe cumplir el estándar: mínimo 8 caracteres
            con mayúscula, minúscula, número y símbolo.
            """)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "202", description = "Contraseña cambiada",
            content = @Content(examples = @ExampleObject(value = "Contraseña actualizada exitosamente"))),
        @ApiResponse(responseCode = "400", description = "La nueva contraseña no cumple el estándar", content = @Content),
        @ApiResponse(responseCode = "401", description = "La contraseña actual no coincide",
            content = @Content(examples = @ExampleObject(value = "La contraseña actual no es correcta")))
      })
  @PutMapping(path = "/changepassword", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> changePassword(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Contraseña actual y nueva",
              required = true,
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = ChangePasswordDTO.class),
                      examples = @ExampleObject(value = """
                    {
                      "passwordActual": "Clave#2026",
                      "passwordNueva": "OtraClave#2027"
                    }
                """)))
          @RequestBody
          ChangePasswordDTO data,
      @Parameter(hidden = true) Authentication authentication,
      HttpServletRequest request) {
    if (data == null) {
      return new ResponseEntity<>("Debes enviar la contraseña actual y la nueva", HttpStatus.BAD_REQUEST);
    }
    try {
      InputValidator.validarPassword(data.getPasswordNueva());
    } catch (PasswordNotValidException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    int status = userServ.changePassword(authentication.getName(), data, NetworkUtil.obtenerIpCliente(request));
    if (status == 0) {
      return new ResponseEntity<>("Contraseña actualizada exitosamente", HttpStatus.ACCEPTED);
    }
    if (status == 1) {
      return new ResponseEntity<>("La contraseña actual no es correcta", HttpStatus.UNAUTHORIZED);
    }
    return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
  }
}
