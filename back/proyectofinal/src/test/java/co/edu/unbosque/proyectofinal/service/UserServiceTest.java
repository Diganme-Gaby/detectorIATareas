package co.edu.unbosque.proyectofinal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unbosque.proyectofinal.dto.ChangePasswordDTO;
import co.edu.unbosque.proyectofinal.dto.RegisterDTO;
import co.edu.unbosque.proyectofinal.model.Audit;
import co.edu.unbosque.proyectofinal.model.Audit.Action;
import co.edu.unbosque.proyectofinal.model.EmailToken;
import co.edu.unbosque.proyectofinal.model.User;
import co.edu.unbosque.proyectofinal.repository.AuditRepository;
import co.edu.unbosque.proyectofinal.repository.EmailTokenRepository;
import co.edu.unbosque.proyectofinal.repository.UserRepository;
import co.edu.unbosque.proyectofinal.service.UserService.LoginStatus;
import co.edu.unbosque.proyectofinal.util.EmailSender;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Pruebas del ciclo de vida de una cuenta: registro, verificación, bloqueo por intentos fallidos y
 * recuperación de contraseña. Usa la base de datos configurada en application.properties y un envío
 * de correo simulado. Cada ejecución crea usuarios con un sufijo distinto para no chocar con los de
 * ejecuciones anteriores.
 */
@SpringBootTest
class UserServiceTest {

  /** Servicio de usuarios. */
  @Autowired private UserService userService;

  /** Repositorio de usuarios. */
  @Autowired private UserRepository userRepo;

  /** Repositorio de códigos enviados por correo. */
  @Autowired private EmailTokenRepository tokenRepo;

  /** Servicio de auditoría. */
  @Autowired private AuditService auditService;

  /** Repositorio de auditoría. */
  @Autowired private AuditRepository auditRepo;

  /** Envío de correos simulado para no mandar correos reales. */
  @MockitoBean private EmailSender emailSender;

  /** Nombre de usuario del administrador configurado. */
  @Value("${app.admin.username}")
  private String admin;

  /** Sufijo único de esta ejecución. */
  private static final String RUN = Long.toString(System.currentTimeMillis() % 1_000_000_000L, 36);

  /** Constructor vacío de la clase de pruebas. */
  UserServiceTest() {}

  /** El correo simulado siempre se "envía". */
  @BeforeEach
  void setUp() {
    when(emailSender.enviarVerificacion(anyString(), anyString(), anyString())).thenReturn(true);
    when(emailSender.enviarRecuperacion(anyString(), anyString(), anyString())).thenReturn(true);
  }

  /**
   * Devuelve los datos de registro válidos.
   *
   * @param sufijo texto para que el usuario no se repita entre pruebas
   * @return datos de registro válidos
   */
  private RegisterDTO registro(String sufijo) {
    return new RegisterDTO(nombre(sufijo), nombre(sufijo) + "@test.com", "Usuario " + sufijo, null, "Clave#2026");
  }

  /**
   * Devuelve el nombre de usuario único para esta ejecución.
   *
   * @param sufijo texto de la prueba
   * @return nombre de usuario único para esta ejecución
   */
  private String nombre(String sufijo) {
    return "t" + RUN + sufijo;
  }

  /** Registro, correo de verificación y confirmación. */
  @Test
  void registraYVerifica() {
    assertEquals(0, userService.register(registro("a1"), "127.0.0.1"));
    User u = userRepo.findByUsername(nombre("a1")).orElseThrow();
    assertFalse(u.isCorreoVerificado());
    assertFalse(u.getPassword().contains("Clave#2026"), "La contraseña debe quedar codificada");

    ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
    verify(emailSender).enviarVerificacion(eq(nombre("a1") + "@test.com"), anyString(), token.capture());

    assertEquals(LoginStatus.NO_VERIFICADA, userService.checkLogin(nombre("a1"), "Clave#2026", "127.0.0.1"));
    assertEquals(0, userService.verifyEmail(token.getValue(), "127.0.0.1"));
    assertEquals(1, userService.verifyEmail(token.getValue(), "127.0.0.1"), "El enlace sirve una sola vez");
    assertEquals(LoginStatus.OK, userService.checkLogin(nombre("a1"), "Clave#2026", "127.0.0.1"));
    assertFalse(auditService.buscar(nombre("a1"), Action.REGISTRO, null, null).isEmpty());
  }

  /**
   * El teléfono y la IP de la auditoría se guardan cifrados con AES y se descifran al convertirlos
   * en DTO.
   */
  @Test
  void cifraTelefonoEIp() {
    RegisterDTO datos = registro("h1");
    datos.setTelefono("3001234567");
    assertEquals(0, userService.register(datos, "127.0.0.1"));
    User u = userRepo.findByUsername(nombre("h1")).orElseThrow();
    assertNotEquals("3001234567", u.getTelefono(), "El teléfono debe quedar cifrado");
    assertEquals("3001234567", userService.toDto(u).getTelefono());

    Audit registro =
        auditRepo.findAllByOrderByFechaDesc().stream()
            .filter(a -> nombre("h1").equals(a.getUsername()) && a.getAccion() == Action.REGISTRO)
            .findFirst()
            .orElseThrow();
    assertNotEquals("127.0.0.1", registro.getIp(), "La IP debe quedar cifrada");
    assertEquals("127.0.0.1", auditService.toDto(registro).getIp());
  }

  /** Usuario y correo duplicados. */
  @Test
  void rechazaDuplicados() {
    assertEquals(0, userService.register(registro("b1"), "127.0.0.1"));
    assertEquals(1, userService.register(registro("b1"), "127.0.0.1"));
    RegisterDTO mismoCorreo = registro("b2");
    mismoCorreo.setEmail(nombre("b1").toUpperCase() + "@test.com");
    assertEquals(2, userService.register(mismoCorreo, "127.0.0.1"), "El correo se compara sin mayúsculas");
  }

  /** Cinco intentos fallidos bloquean la cuenta aunque luego la contraseña sea correcta. */
  @Test
  void bloqueaDespuesDeCincoIntentos() {
    userService.register(registro("c1"), "127.0.0.1");
    User u = userRepo.findByUsername(nombre("c1")).orElseThrow();
    u.setCorreoVerificado(true);
    userRepo.save(u);
    for (int i = 1; i < UserService.MAX_INTENTOS; i++) {
      assertEquals(LoginStatus.CREDENCIALES_INVALIDAS, userService.checkLogin(nombre("c1"), "mala" + i, "127.0.0.1"));
    }
    assertEquals(LoginStatus.BLOQUEADA, userService.checkLogin(nombre("c1"), "mala5", "127.0.0.1"));
    assertEquals(LoginStatus.BLOQUEADA, userService.checkLogin(nombre("c1"), "Clave#2026", "127.0.0.1"));
    assertTrue(userService.minutosBloqueo(nombre("c1")) >= 1);

    User bloqueado = userRepo.findByUsername(nombre("c1")).orElseThrow();
    assertEquals(0, userService.unlock(bloqueado.getId(), admin, "127.0.0.1"));
    assertEquals(LoginStatus.OK, userService.checkLogin(nombre("c1"), "Clave#2026", "127.0.0.1"));
  }

  /** Recuperación de contraseña: el código sirve una vez y vence. */
  @Test
  void recuperaContrasena() {
    userService.register(registro("d1"), "127.0.0.1");
    userService.forgotPassword(nombre("d1") + "@test.com", "127.0.0.1");
    ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
    verify(emailSender).enviarRecuperacion(eq(nombre("d1") + "@test.com"), anyString(), token.capture());

    assertEquals(0, userService.resetPassword(token.getValue(), "Nueva#2027", "127.0.0.1"));
    assertEquals(1, userService.resetPassword(token.getValue(), "Otra#2028", "127.0.0.1"));

    User u = userRepo.findByUsername(nombre("d1")).orElseThrow();
    u.setCorreoVerificado(true);
    userRepo.save(u);
    assertEquals(LoginStatus.OK, userService.checkLogin(nombre("d1"), "Nueva#2027", "127.0.0.1"));
  }

  /** Un código vencido no sirve. */
  @Test
  void tokenVencido() {
    userService.register(registro("e1"), "127.0.0.1");
    User u = userRepo.findByUsername(nombre("e1")).orElseThrow();
    EmailToken t = new EmailToken("token-vencido-" + RUN, EmailToken.TokenType.RECUPERACION, u.getId(), LocalDateTime.now().minusMinutes(1));
    tokenRepo.save(t);
    assertEquals(2, userService.resetPassword("token-vencido-" + RUN, "Nueva#2027", "127.0.0.1"));
  }

  /** Cambio de contraseña desde el perfil. */
  @Test
  void cambiaContrasena() {
    userService.register(registro("f1"), "127.0.0.1");
    assertEquals(1, userService.changePassword(nombre("f1"), new ChangePasswordDTO("incorrecta", "Nueva#2027"), "127.0.0.1"));
    assertEquals(0, userService.changePassword(nombre("f1"), new ChangePasswordDTO("Clave#2026", "Nueva#2027"), "127.0.0.1"));
  }

  /** El administrador no puede desactivarse ni quitarse su rol; si puede desactivar a otros. */
  @Test
  void reglasDelAdministrador() {
    User adminUser = userRepo.findByUsername(admin).orElseThrow();
    assertEquals(2, userService.setActive(adminUser.getId(), false, admin, "127.0.0.1"));
    userService.register(registro("g1"), "127.0.0.1");
    User otro = userRepo.findByUsername(nombre("g1")).orElseThrow();
    assertEquals(0, userService.setActive(otro.getId(), false, admin, "127.0.0.1"));
    assertFalse(userRepo.findByUsername(nombre("g1")).orElseThrow().isActivo());
    assertNotNull(userService.getById(otro.getId()));
  }
}
