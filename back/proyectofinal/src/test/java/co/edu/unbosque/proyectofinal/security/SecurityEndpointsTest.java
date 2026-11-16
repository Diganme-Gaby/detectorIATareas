package co.edu.unbosque.proyectofinal.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.unbosque.proyectofinal.model.User;
import co.edu.unbosque.proyectofinal.repository.UserRepository;
import co.edu.unbosque.proyectofinal.util.EmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Pruebas de seguridad sobre los endpoints reales: control de acceso por rol, tokens inválidos,
 * cuentas desactivadas, inyección en el login y exposición de datos sensibles. Usa la base de
 * datos del proyecto y el administrador que precarga LoadDatabase.
 */
@SpringBootTest
class SecurityEndpointsTest {

  /** Contexto de la aplicación web, usado para armar el cliente simulado. */
  @Autowired private WebApplicationContext context;

  /** Cliente HTTP simulado con los filtros de seguridad reales. */
  private MockMvc mvc;

  /** Utilidad para generar y validar tokens JWT. */
  @Autowired private JwtUtil jwtUtil;

  /** Repositorio de usuarios. */
  @Autowired private UserRepository userRepo;

  /** Codificador de contraseñas. */
  @Autowired private PasswordEncoder encoder;

  /** Envío de correos simulado para no mandar correos reales. */
  @MockitoBean private EmailSender emailSender;

  /** Constructor vacío de la clase de pruebas. */
  SecurityEndpointsTest() {}

  /** Arma el cliente sobre la aplicación real, incluyendo la cadena de seguridad. */
  @BeforeEach
  void prepararCliente() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  /** Nombre de usuario del administrador configurado. */
  @Value("${app.admin.username}")
  private String admin;

  /** Sufijo único de esta ejecución para no repetir usuarios entre corridas. */
  private static final String RUN = Long.toString(System.currentTimeMillis() % 1_000_000_000L, 36);

  /**
   * Devuelve el encabezado Authorization con un token válido.
   *
   * @param username usuario existente
   * @return encabezado Authorization con un token válido
   */
  private String bearer(String username) {
    return "Bearer " + jwtUtil.generateToken(userRepo.findByUsername(username).orElseThrow());
  }

  /**
   * Devuelve true si fue rechazada por falta de autenticación o permisos.
   *
   * @param resultado respuesta
   * @return true si fue rechazada por falta de autenticación o permisos
   */
  private boolean rechazada(MvcResult resultado) {
    int s = resultado.getResponse().getStatus();
    return s == 401 || s == 403;
  }

  /**
   * Sin token no se accede a nada protegido.
   *
   * @throws Exception si falla la petición simulada o una aserción interna
   */
  @Test
  void sinTokenNoHayAcceso() throws Exception {
    assertTrue(rechazada(mvc.perform(get("/analysis/history")).andReturn()));
    assertTrue(rechazada(mvc.perform(get("/user/me")).andReturn()));
    assertTrue(rechazada(mvc.perform(get("/admin/user/getall")).andReturn()));
    assertTrue(rechazada(mvc.perform(get("/admin/audit/getall")).andReturn()));
  }

  /**
   * Un USER no entra al módulo administrativo.
   *
   * @throws Exception si falla la petición simulada o una aserción interna
   */
  @Test
  void userNoEntraAAdmin() throws Exception {
    String token = bearer("normaluser");
    mvc.perform(get("/admin/user/getall").header("Authorization", token)).andExpect(status().isForbidden());
    mvc.perform(get("/admin/stats/summary").header("Authorization", token)).andExpect(status().isForbidden());
    mvc.perform(put("/admin/user/disable/1").header("Authorization", token)).andExpect(status().isForbidden());
    mvc.perform(get("/analysis/history").header("Authorization", token)).andExpect(status().isOk());
  }

  /**
   * El ADMIN si entra.
   *
   * @throws Exception si falla la petición simulada o una aserción interna
   */
  @Test
  void adminEntraAAdmin() throws Exception {
    String token = bearer(admin);
    mvc.perform(get("/admin/user/getall").header("Authorization", token)).andExpect(status().isAccepted());
    mvc.perform(get("/admin/stats/summary").header("Authorization", token)).andExpect(status().isOk());
    mvc.perform(get("/admin/audit/getall").header("Authorization", token)).andExpect(status().isOk());
  }

  /**
   * Tokens falsificados o mal formados no autentican.
   *
   * @throws Exception si falla la petición simulada o una aserción interna
   */
  @Test
  void tokenInvalido() throws Exception {
    String falso = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiJ9.firmaFalsa";
    assertTrue(rechazada(mvc.perform(get("/admin/user/getall").header("Authorization", falso)).andReturn()));
    assertTrue(rechazada(mvc.perform(get("/user/me").header("Authorization", "Bearer basura")).andReturn()));
    String alg = "Bearer eyJhbGciOiJub25lIn0.eyJzdWIiOiJhZG1pbiJ9.";
    assertTrue(rechazada(mvc.perform(get("/user/me").header("Authorization", alg)).andReturn()));
  }

  /**
   * Al desactivar una cuenta su token deja de funcionar.
   *
   * @throws Exception si falla la petición simulada o una aserción interna
   */
  @Test
  void cuentaDesactivadaPierdeAcceso() throws Exception {
    String usuario = "desactivable." + RUN;
    User u = new User(usuario, usuario + "@test.com", "Desactivable", encoder.encode("Clave#2026"));
    u.setCorreoVerificado(true);
    userRepo.save(u);
    String token = bearer(usuario);
    mvc.perform(get("/user/me").header("Authorization", token)).andExpect(status().isOk());
    u = userRepo.findByUsername(usuario).orElseThrow();
    u.setActivo(false);
    userRepo.save(u);
    assertTrue(rechazada(mvc.perform(get("/user/me").header("Authorization", token)).andReturn()));
  }

  /**
   * Intentos de inyección SQL en el login no autentican.
   *
   * @throws Exception si falla la petición simulada o una aserción interna
   */
  @Test
  void inyeccionEnLogin() throws Exception {
    String[] ataques = {"admin' OR '1'='1", "admin'--", "' OR 1=1 #", "admin\"; DROP TABLE usuario_detector; --"};
    for (String ataque : ataques) {
      String body = "{\"username\": \"" + ataque.replace("\"", "\\\"") + "\", \"password\": \"x' OR '1'='1\"}";
      mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
          .andExpect(status().isUnauthorized());
    }
    mvc.perform(get("/admin/user/count").header("Authorization", bearer(admin))).andExpect(status().isAccepted());
  }

  /**
   * El perfil nunca devuelve la contraseña ni su hash.
   *
   * @throws Exception si falla la petición simulada o una aserción interna
   */
  @Test
  void noExponeContrasena() throws Exception {
    String json = mvc.perform(get("/user/me").header("Authorization", bearer(admin)))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    assertFalse(json.contains("$2a$"));
    String lista = mvc.perform(get("/admin/user/getall").header("Authorization", bearer(admin)))
        .andReturn().getResponse().getContentAsString();
    assertFalse(lista.contains("$2a$"));
  }

  /**
   * Un usuario no puede ver análisis o reportes de otro.
   *
   * @throws Exception si falla la petición simulada o una aserción interna
   */
  @Test
  void noVeAnalisisAjenos() throws Exception {
    mvc.perform(get("/analysis/getbyid/999999").header("Authorization", bearer("normaluser")))
        .andExpect(status().isNotFound());
    mvc.perform(get("/analysis/reportpdf/999999").header("Authorization", bearer("normaluser")))
        .andExpect(status().isNotFound());
  }

  /**
   * Swagger y los endpoints de autenticación son públicos.
   *
   * @throws Exception si falla la petición simulada o una aserción interna
   */
  @Test
  void endpointsPublicos() throws Exception {
    mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    mvc.perform(get("/auth/verifyemail").param("token", "no-existe")).andExpect(status().isBadRequest());
  }

  /**
   * Una URL que no es de GitHub no se descarga (protección SSRF).
   *
   * @throws Exception si falla la petición simulada o una aserción interna
   */
  @Test
  void githubRechazaOtrosDominios() throws Exception {
    mvc.perform(post("/analysis/github").header("Authorization", bearer("normaluser"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"url\": \"http://169.254.169.254/latest/meta-data\"}"))
        .andExpect(status().isBadRequest());
  }
}
