package co.edu.unbosque.proyectofinal;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import co.edu.unbosque.proyectofinal.controller.AnalysisController;
import co.edu.unbosque.proyectofinal.controller.AuthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verifica que el contexto de Spring arranque con todas las piezas (seguridad, JPA, correo, IAs)
 * con la base de datos configurada en application.properties.
 */
@SpringBootTest
class ProyectofinalApplicationTests {

  /** Controlador de autenticación. */
  @Autowired private AuthController authController;

  /** Controlador de análisis. */
  @Autowired private AnalysisController analysisController;

  /** Constructor vacío de la clase de pruebas. */
  ProyectofinalApplicationTests() {}

  /** El contexto carga y los controllers principales existen. */
  @Test
  void contextLoads() {
    assertNotNull(authController);
    assertNotNull(analysisController);
  }
}
