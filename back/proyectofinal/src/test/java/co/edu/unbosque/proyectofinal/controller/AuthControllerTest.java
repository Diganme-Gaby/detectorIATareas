package co.edu.unbosque.proyectofinal.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unbosque.proyectofinal.dto.LoginDTO;
import co.edu.unbosque.proyectofinal.dto.RegisterDTO;
import co.edu.unbosque.proyectofinal.security.JwtUtil;
import co.edu.unbosque.proyectofinal.util.EmailSender;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Clase de prueba para el AuthController. Llama los métodos del controller directamente, como en
 * la guía, contra la base de datos del proyecto y con el administrador que precarga LoadDatabase.
 *
 * @author Universidad El Bosque
 * @version 1.0
 */
@SpringBootTest
class AuthControllerTest {

  /** Sufijo único de esta ejecución para no repetir usuarios entre corridas. */
  private static final String RUN = Long.toString(System.currentTimeMillis() % 1_000_000_000L, 36);

  /** Controlador de autenticación. */
  @Autowired private AuthController authController;

  /** Utilidad para generar y validar tokens JWT. */
  @Autowired private JwtUtil jwtUtil;

  /** Envío de correos simulado para no mandar correos reales. */
  @MockitoBean private EmailSender emailSender;

  /** Nombre de usuario del administrador configurado. */
  @Value("${app.admin.username}")
  private String adminUsername;

  /** Contraseña del administrador configurado. */
  @Value("${app.admin.password}")
  private String adminPassword;

  /** Correo del administrador configurado. */
  @Value("${app.admin.email}")
  private String adminEmail;

  /** Constructor vacío de la clase de pruebas. */
  AuthControllerTest() {}

  /**
   * Devuelve el nombre de usuario único.
   *
   * @param base texto de la prueba
   * @return nombre de usuario único
   */
  private String nombre(String base) {
    return base + "." + RUN;
  }

  /** El login del administrador precargado devuelve token y rol. */
  @Test
  void testLoginReturnsRoleInResponse() {
    ResponseEntity<?> response =
        authController.login(new LoginDTO(adminUsername, adminPassword), new MockHttpServletRequest());
    assertEquals(HttpStatus.OK, response.getStatusCode(), "El estado de la respuesta debe ser OK");
    JsonObject json = new Gson().toJsonTree(response.getBody()).getAsJsonObject();
    assertEquals("ADMIN", json.get("role").getAsString());
    String token = json.get("token").getAsString();
    assertNotNull(token);
    assertEquals(adminUsername, jwtUtil.extractUsername(token));
  }

  /** Credenciales incorrectas dan 401 sin decir cuál dato falló. */
  @Test
  void testLoginInvalido() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    String usuario = nombre("clave.mala");
    authController.register(new RegisterDTO(usuario, usuario + "@test.com", "Clave Mala", null, "Clave#2026"), req);
    ResponseEntity<?> r1 = authController.login(new LoginDTO("noexiste." + RUN, "x"), req);
    ResponseEntity<?> r2 = authController.login(new LoginDTO(usuario, "incorrecta"), req);
    assertEquals(HttpStatus.UNAUTHORIZED, r1.getStatusCode());
    assertEquals(HttpStatus.UNAUTHORIZED, r2.getStatusCode());
    assertEquals(r1.getBody(), r2.getBody(), "El mensaje no debe revelar si el usuario existe");
  }

  /** El registro valida la contraseña y los datos. */
  @Test
  void testRegistroValidaDatos() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    ResponseEntity<String> debil = authController.register(
        new RegisterDTO(nombre("nuevo.uno"), nombre("nuevo1") + "@test.com", "Nuevo Uno", null, "123"), req);
    assertEquals(HttpStatus.BAD_REQUEST, debil.getStatusCode());

    ResponseEntity<String> xss = authController.register(
        new RegisterDTO(nombre("nuevo.dos"), nombre("nuevo2") + "@test.com", "<script>alert(1)</script>", null, "Clave#2026"), req);
    assertEquals(HttpStatus.BAD_REQUEST, xss.getStatusCode());

    ResponseEntity<String> ok = authController.register(
        new RegisterDTO(nombre("nuevo.tres"), nombre("nuevo3") + "@test.com", "Nuevo Tres", "3001234567", "Clave#2026"), req);
    assertEquals(HttpStatus.CREATED, ok.getStatusCode());

    ResponseEntity<String> repetido = authController.register(
        new RegisterDTO(nombre("nuevo.tres"), nombre("otro") + "@test.com", "Nuevo Tres", null, "Clave#2026"), req);
    assertEquals(HttpStatus.CONFLICT, repetido.getStatusCode());
  }

  /** Sin confirmar el correo no se puede entrar. */
  @Test
  void testLoginSinVerificar() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    String usuario = nombre("sin.verificar");
    authController.register(new RegisterDTO(usuario, usuario + "@test.com", "Sin Verificar", null, "Clave#2026"), req);
    ResponseEntity<?> r = authController.login(new LoginDTO(usuario, "Clave#2026"), req);
    assertEquals(HttpStatus.FORBIDDEN, r.getStatusCode());
    assertTrue(String.valueOf(r.getBody()).contains("confirmar"));
  }

  /** Recuperar contraseña responde igual exista o no el correo. */
  @Test
  void testForgotPasswordNoRevelaCorreos() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(authController.forgotPassword("noexiste." + RUN + "@test.com", req).getBody(),
        authController.forgotPassword(adminEmail, req).getBody());
  }
}
