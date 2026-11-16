package co.edu.unbosque.proyectofinal.controller;

import co.edu.unbosque.proyectofinal.dto.AuthResponseDTO;
import co.edu.unbosque.proyectofinal.dto.LoginDTO;
import co.edu.unbosque.proyectofinal.dto.RegisterDTO;
import co.edu.unbosque.proyectofinal.dto.ResetPasswordDTO;
import co.edu.unbosque.proyectofinal.exception.PasswordNotValidException;
import co.edu.unbosque.proyectofinal.model.User;
import co.edu.unbosque.proyectofinal.security.JwtUtil;
import co.edu.unbosque.proyectofinal.service.UserService;
import co.edu.unbosque.proyectofinal.service.UserService.LoginStatus;
import co.edu.unbosque.proyectofinal.util.InputValidator;
import co.edu.unbosque.proyectofinal.util.NetworkUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la autenticación de usuarios. Maneja el inicio de sesión, el registro, la
 * confirmación del correo y la recuperación de contraseña. Todos sus endpoints son públicos.
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:4200", "https://url-del-front.netlify.app"})
@Tag(name = "Autenticación", description = "Registro, inicio de sesión, verificación de correo y recuperación de contraseña")
public class AuthController {

  /** Gestor de autenticación para validar credenciales de usuario. */
  private final AuthenticationManager authenticationManager;

  /** Utilidad para operaciones con tokens JWT. */
  private final JwtUtil jwtUtil;

  /** Servicio para operaciones relacionadas con usuarios. */
  private final UserService userService;

  /**
   * Constructor que inicializa las dependencias necesarias para el controlador.
   *
   * @param authenticationManager Gestor de autenticación
   * @param jwtUtil Utilidad para tokens JWT
   * @param userService Servicio de usuarios
   */
  public AuthController(
      AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
    this.userService = userService;
  }

  /**
   * Inicia sesión y devuelve el token JWT con el rol del usuario.
   *
   * @param loginRequest credenciales
   * @param request petición HTTP, para la auditoría
   * @return token y rol, o el motivo por el que no se pudo iniciar sesión
   */
  @Operation(
      summary = "Iniciar sesión de usuario",
      description =
          """
            Verifica el usuario y la contraseña y, si son correctos, genera un token JWT válido por 24 horas.

            **Paso a paso:**

            1. Envía tu nombre de usuario y contraseña en formato JSON
            2. Si son correctos recibes el token y tu rol (USER o ADMIN)
            3. Usa el token en el botón **Authorize** o en el encabezado `Authorization: Bearer tu_token_jwt`

            **Reglas de seguridad:**

            * La cuenta debe tener el correo confirmado.
            * Después de **5 intentos fallidos seguidos** la cuenta se bloquea **15 minutos** (código 423).
            * Si el administrador desactivó la cuenta, no se puede iniciar sesión (código 403).
            """)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Inicio de sesión exitoso",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AuthResponseDTO.class),
                    examples =
                        @ExampleObject(
                            value =
                                """
                        {
                          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "role": "USER",
                          "username": "laura.gomez"
                        }
                    """))),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciales inválidas",
            content = @Content(examples = @ExampleObject(value = "Nombre de usuario o contraseña inválidos"))),
        @ApiResponse(
            responseCode = "403",
            description = "Correo sin confirmar o cuenta desactivada",
            content = @Content(examples = @ExampleObject(value = "Debes confirmar tu correo antes de iniciar sesión"))),
        @ApiResponse(
            responseCode = "423",
            description = "Cuenta bloqueada por intentos fallidos",
            content = @Content(examples = @ExampleObject(value = "Cuenta bloqueada por intentos fallidos. Intenta de nuevo en 15 minuto(s)")))
      })
  @PostMapping("/login")
  public ResponseEntity<?> login(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Credenciales de usuario para iniciar sesión",
              required = true,
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = LoginDTO.class),
                      examples = @ExampleObject(
                      value =
                          """
                    {
                      "username": "admin",
                      "password": "Clave#2026"
                    }
                """)))
          @RequestBody
          LoginDTO loginRequest,
      HttpServletRequest request) {
    String ip = NetworkUtil.obtenerIpCliente(request);
    if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Debes enviar usuario y contraseña");
    }
    LoginStatus estado = userService.checkLogin(loginRequest.getUsername(), loginRequest.getPassword(), ip);
    switch (estado) {
      case CREDENCIALES_INVALIDAS:
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nombre de usuario o contraseña inválidos");
      case BLOQUEADA:
        return ResponseEntity.status(HttpStatus.LOCKED)
            .body("Cuenta bloqueada por intentos fallidos. Intenta de nuevo en "
                + userService.minutosBloqueo(loginRequest.getUsername().trim()) + " minuto(s)");
      case NO_VERIFICADA:
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Debes confirmar tu correo antes de iniciar sesión");
      case DESACTIVADA:
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Tu cuenta está desactivada. Comunícate con el administrador");
      default:
        break;
    }
    try {
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  loginRequest.getUsername().trim(), loginRequest.getPassword()));

      UserDetails userDetails = (UserDetails) authentication.getPrincipal();
      String jwt = jwtUtil.generateToken(userDetails);

      String role = null;
      if (userDetails instanceof User) {
        role = ((User) userDetails).getRole().name();
      }
      userService.registerSuccessfulLogin(userDetails.getUsername(), ip);
      return ResponseEntity.ok(new AuthResponseDTO(jwt, role, userDetails.getUsername()));
    } catch (AuthenticationException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nombre de usuario o contraseña inválidos");
    }
  }

  /**
   * Registra una cuenta nueva y envía el correo de confirmación.
   *
   * @param registerRequest datos del registro
   * @param request petición HTTP, para la auditoría
   * @return mensaje de éxito o el error de validación
   */
  @Operation(
      summary = "Registrar un nuevo usuario",
      description =
          """
            Crea una cuenta con rol USER y envía al correo un enlace para confirmarla.
            Hasta que el correo no se confirme no se puede iniciar sesión.

            **Reglas:**

            * Nombre de usuario: 4 a 30 caracteres (letras, números, punto, guion o guion bajo). No se puede cambiar después.
            * Correo: válido y único. No se puede cambiar después.
            * Contraseña: mínimo 8 caracteres con al menos una mayúscula, una minúscula, un número y un símbolo.
            * Nombre completo: obligatorio, sin caracteres `< > " '`.
            * Teléfono: opcional, de 7 a 20 dígitos.

            **Nota:** Este endpoint es público y no requiere autenticación.
            """)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Usuario registrado; se envió el correo de confirmación",
            content = @Content(examples = @ExampleObject(value = "Usuario registrado. Revisa tu correo para confirmar la cuenta"))),
        @ApiResponse(
            responseCode = "409",
            description = "El nombre de usuario o el correo ya existen",
            content = @Content(examples = @ExampleObject(value = "El nombre de usuario ya existe"))),
        @ApiResponse(
            responseCode = "400",
            description = "Datos inválidos",
            content = @Content(examples = @ExampleObject(value = "La contraseña no cumple con el estándar ...")))
      })
  @PostMapping("/register")
  public ResponseEntity<String> register(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Información del nuevo usuario",
              required = true,
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = RegisterDTO.class),
                      examples = @ExampleObject(
                      value =
                          """
                    {
                      "username": "laura.gomez",
                      "email": "laura.gomez@ejemplo.com",
                      "nombreCompleto": "Laura Gomez",
                      "telefono": "3001234567",
                      "password": "Clave#2026"
                    }
                """)))
          @RequestBody
          RegisterDTO registerRequest,
      HttpServletRequest request) {
    String error = validarRegistro(registerRequest);
    if (error != null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    int result = userService.register(registerRequest, NetworkUtil.obtenerIpCliente(request));
    if (result == 1) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("El nombre de usuario ya existe");
    }
    if (result == 2) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("El correo ya está registrado");
    }
    return ResponseEntity.status(HttpStatus.CREATED)
        .body("Usuario registrado. Revisa tu correo para confirmar la cuenta");
  }

  /**
   * Confirma el correo con el código que llegó en el enlace.
   *
   * @param token código de verificación
   * @param request petición HTTP, para la auditoría
   * @return resultado de la confirmación
   */
  @Operation(
      summary = "Confirmar correo",
      description =
          """
            El correo de registro trae un enlace hacia el front (`/verificar-correo?token=...`).
            El front toma el token de la URL y llama a este endpoint.

            El enlace sirve una sola vez y vence en 24 horas. Si venció, usa `/auth/resendverification`.
            """)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Correo confirmado",
            content = @Content(examples = @ExampleObject(value = "Correo confirmado. Ya puedes iniciar sesión"))),
        @ApiResponse(responseCode = "400", description = "Código inválido o ya usado",
            content = @Content(examples = @ExampleObject(value = "El enlace no es válido o ya fue usado"))),
        @ApiResponse(responseCode = "410", description = "Código vencido",
            content = @Content(examples = @ExampleObject(value = "El enlace venció. Solicita uno nuevo")))
      })
  @GetMapping("/verifyemail")
  public ResponseEntity<String> verifyEmail(
      @Parameter(description = "Código recibido en el correo", required = true, example = "3f6c1a0e-...")
          @RequestParam
          String token,
      HttpServletRequest request) {
    int result = userService.verifyEmail(token, NetworkUtil.obtenerIpCliente(request));
    if (result == 0) {
      return ResponseEntity.ok("Correo confirmado. Ya puedes iniciar sesión");
    }
    if (result == 2) {
      return ResponseEntity.status(HttpStatus.GONE).body("El enlace venció. Solicita uno nuevo");
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El enlace no es válido o ya fue usado");
  }

  /**
   * Reenvía el correo de confirmación.
   *
   * @param email correo de la cuenta
   * @param request petición HTTP, para la auditoría
   * @return mensaje genérico
   */
  @Operation(
      summary = "Reenviar correo de confirmación",
      description =
          """
            Genera un enlace nuevo de confirmación y lo envía al correo. Los enlaces anteriores dejan de servir.

            **Nota:** La respuesta es la misma exista o no la cuenta, para no revelar qué correos están registrados.
            """)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Solicitud recibida",
            content = @Content(examples = @ExampleObject(value = "Si el correo está registrado y sin confirmar, te enviamos un enlace nuevo")))
      })
  @PostMapping("/resendverification")
  public ResponseEntity<String> resendVerification(
      @Parameter(description = "Correo de la cuenta", required = true, example = "laura.gomez@ejemplo.com")
          @RequestParam
          String email,
      HttpServletRequest request) {
    if (InputValidator.emailValido(email)) {
      userService.resendVerification(email, NetworkUtil.obtenerIpCliente(request));
    }
    return ResponseEntity.ok("Si el correo está registrado y sin confirmar, te enviamos un enlace nuevo");
  }

  /**
   * Envía el enlace de recuperación de contraseña.
   *
   * @param email correo de la cuenta
   * @param request petición HTTP, para la auditoría
   * @return mensaje genérico
   */
  @Operation(
      summary = "Olvidé mi contraseña",
      description =
          """
            Envía al correo un enlace hacia el front (`/restablecer-contrasena?token=...`) que vence en 30 minutos.
            En esa página el usuario escribe la nueva contraseña y el front llama a `/auth/resetpassword`.

            La contraseña nunca se muestra ni se envía por correo: solo se puede cambiar.
            """)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Solicitud recibida",
            content = @Content(examples = @ExampleObject(value = "Si el correo está registrado, te enviamos un enlace para restablecer la contraseña")))
      })
  @PostMapping("/forgotpassword")
  public ResponseEntity<String> forgotPassword(
      @Parameter(description = "Correo de la cuenta", required = true, example = "laura.gomez@ejemplo.com")
          @RequestParam
          String email,
      HttpServletRequest request) {
    if (InputValidator.emailValido(email)) {
      userService.forgotPassword(email, NetworkUtil.obtenerIpCliente(request));
    }
    return ResponseEntity.ok("Si el correo está registrado, te enviamos un enlace para restablecer la contraseña");
  }

  /**
   * Cambia la contraseña con el código de recuperación.
   *
   * @param data código y contraseña nueva
   * @param request petición HTTP, para la auditoría
   * @return resultado del cambio
   */
  @Operation(
      summary = "Restablecer contraseña",
      description =
          """
            Cambia la contraseña usando el código que llegó al correo. El código sirve una sola vez.
            Al restablecerla también se quita el bloqueo por intentos fallidos.
            """)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Contraseña actualizada",
            content = @Content(examples = @ExampleObject(value = "Contraseña actualizada. Ya puedes iniciar sesión"))),
        @ApiResponse(responseCode = "400", description = "Código inválido o contraseña que no cumple el estándar",
            content = @Content(examples = @ExampleObject(value = "El enlace no es válido o ya fue usado"))),
        @ApiResponse(responseCode = "410", description = "Código vencido",
            content = @Content(examples = @ExampleObject(value = "El enlace venció. Solicita uno nuevo")))
      })
  @PutMapping("/resetpassword")
  public ResponseEntity<String> resetPassword(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Código del correo y contraseña nueva",
              required = true,
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = ResetPasswordDTO.class),
                      examples = @ExampleObject(value = """
                    {
                      "token": "3f6c1a0e-...",
                      "passwordNueva": "NuevaClave#2026"
                    }
                """)))
          @RequestBody
          ResetPasswordDTO data,
      HttpServletRequest request) {
    if (data == null || data.getToken() == null || data.getToken().isBlank()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El enlace no es válido o ya fue usado");
    }
    if (!InputValidator.passwordValida(data.getPasswordNueva())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new PasswordNotValidException().getMessage());
    }
    int result = userService.resetPassword(data.getToken(), data.getPasswordNueva(), NetworkUtil.obtenerIpCliente(request));
    if (result == 0) {
      return ResponseEntity.ok("Contraseña actualizada. Ya puedes iniciar sesión");
    }
    if (result == 2) {
      return ResponseEntity.status(HttpStatus.GONE).body("El enlace venció. Solicita uno nuevo");
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El enlace no es válido o ya fue usado");
  }

  /**
   * Revisa los datos del registro.
   *
   * @param r datos del registro
   * @return mensaje de error o null si todo es válido
   */
  private String validarRegistro(RegisterDTO r) {
    if (r == null) {
      return "Debes enviar los datos del registro";
    }
    if (!InputValidator.usernameValido(r.getUsername())) {
      return "El nombre de usuario debe tener entre 4 y 30 caracteres: letras, números, punto, guion o guion bajo";
    }
    if (!InputValidator.emailValido(r.getEmail())) {
      return "El correo no es válido";
    }
    if (!InputValidator.nombreValido(r.getNombreCompleto())) {
      return "El nombre es obligatorio, máximo 100 caracteres y sin caracteres especiales de HTML";
    }
    if (!InputValidator.telefonoValido(r.getTelefono())) {
      return "El teléfono debe tener entre 7 y 20 dígitos";
    }
    try {
      InputValidator.validarPassword(r.getPassword());
    } catch (PasswordNotValidException e) {
      return e.getMessage();
    }
    return null;
  }
}
