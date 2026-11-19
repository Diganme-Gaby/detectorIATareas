package co.edu.unbosque.proyectofinal.stress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import co.edu.unbosque.proyectofinal.model.User;
import co.edu.unbosque.proyectofinal.repository.UserRepository;
import co.edu.unbosque.proyectofinal.security.JwtUtil;
import co.edu.unbosque.proyectofinal.util.AiPromptUtil;
import co.edu.unbosque.proyectofinal.util.EmailSender;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Pruebas de estres dentro del back: muchas peticiones concurrentes autenticadas, login en
 * paralelo y generación masiva de tokens.
 *
 * <p>Para una prueba de carga contra el servidor desplegado se recomienda complementar con JMeter
 * o k6 apuntando a la URL pública.
 */
@SpringBootTest
class StressTest {

  /** Hilos concurrentes. */
  private static final int HILOS = 20;

  /** Peticiones totales por prueba. Cada login fallido deja un registro en la auditoría. */
  private static final int PETICIONES = 200;

  /** Contexto de la aplicación web, usado para armar el cliente simulado. */
  @Autowired private WebApplicationContext context;

  /** Cliente HTTP simulado con los filtros de seguridad reales. */
  private MockMvc mvc;

  /** Utilidad para generar y validar tokens JWT. */
  @Autowired private JwtUtil jwtUtil;

  /** Repositorio de usuarios. */
  @Autowired private UserRepository userRepo;

  /** Envío de correos simulado para no mandar correos reales. */
  @MockitoBean private EmailSender emailSender;

  /** Constructor vacío de la clase de pruebas. */
  StressTest() {}

  /** Arma el cliente sobre la aplicación real, incluyendo la cadena de seguridad. */
  @BeforeEach
  void prepararCliente() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  /**
   * Ejecuta una tarea muchas veces en paralelo y devuelve los tiempos de cada ejecución.
   *
   * @param tarea tarea que devuelve el código HTTP
   * @param codigoEsperado código que deben devolver todas
   * @return tiempos en milisegundos, ordenados
   * @throws Exception si alguna tarea falla
   */
  private List<Long> ejecutar(Callable<Integer> tarea, int codigoEsperado) throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(HILOS);
    try {
      List<Future<long[]>> futuros = new ArrayList<>();
      for (int i = 0; i < PETICIONES; i++) {
        futuros.add(pool.submit(() -> {
          long inicio = System.nanoTime();
          int codigo = tarea.call();
          return new long[] {codigo, (System.nanoTime() - inicio) / 1_000_000};
        }));
      }
      List<Long> tiempos = new ArrayList<>();
      for (Future<long[]> f : futuros) {
        long[] r = f.get();
        assertEquals(codigoEsperado, (int) r[0]);
        tiempos.add(r[1]);
      }
      Collections.sort(tiempos);
      return tiempos;
    } finally {
      pool.shutdown();
    }
  }

  /**
   * Devuelve el percentil 95.
   *
   * @param tiempos tiempos ordenados
   * @return percentil 95
   */
  private long p95(List<Long> tiempos) {
    return tiempos.get((int) Math.ceil(tiempos.size() * 0.95) - 1);
  }

  /**
   * 200 consultas autenticadas al perfil con 20 hilos.
   *
   * @throws Exception si falla la petición simulada o una aserción interna
   */
  @Test
  void perfilBajoCarga() throws Exception {
    String token = "Bearer " + jwtUtil.generateToken(userRepo.findByUsername("normaluser").orElseThrow());
    List<Long> tiempos = ejecutar(
        () -> mvc.perform(get("/user/me").header("Authorization", token)).andReturn().getResponse().getStatus(), 200);
    System.out.println("[STRESS] /user/me p95=" + p95(tiempos) + " ms, max=" + tiempos.get(tiempos.size() - 1) + " ms");
    assertTrue(p95(tiempos) < 1500, "El 95% de las peticiones debe responder en menos de 1.5 s");
  }

  /**
   * Logins fallidos concurrentes contra un usuario inexistente: el servidor responde siempre 401.
   *
   * @throws Exception si falla la petición simulada o una aserción interna
   */
  @Test
  void loginsConcurrentes() throws Exception {
    String body = "{\"username\": \"no.existe.stress\", \"password\": \"Clave#2026\"}";
    List<Long> tiempos = ejecutar(
        () -> mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
            .andReturn().getResponse().getStatus(), 401);
    System.out.println("[STRESS] /auth/login p95=" + p95(tiempos) + " ms");
    assertTrue(p95(tiempos) < 4000);
  }

  /** Generación y validación de 2000 tokens. */
  @Test
  void generacionMasivaDeTokens() {
    User user = userRepo.findByUsername("normaluser").orElseThrow();
    long inicio = System.currentTimeMillis();
    for (int i = 0; i < 2000; i++) {
      assertTrue(jwtUtil.validateToken(jwtUtil.generateToken(user), user));
    }
    long total = System.currentTimeMillis() - inicio;
    System.out.println("[STRESS] 2000 tokens en " + total + " ms");
    assertTrue(total < 10000);
  }

  /** Interpretación de 10000 respuestas de IA. */
  @Test
  void interpretacionMasiva() {
    String respuesta = "{\"porcentaje\": 64, \"veredicto\": \"MIXTO\", \"motivos\": [\"a\", \"b\", \"c\"]}";
    long inicio = System.currentTimeMillis();
    for (int i = 0; i < 10000; i++) {
      AiPromptUtil.interpretarRespuesta(respuesta, "m");
    }
    assertTrue(System.currentTimeMillis() - inicio < 5000);
  }
}
