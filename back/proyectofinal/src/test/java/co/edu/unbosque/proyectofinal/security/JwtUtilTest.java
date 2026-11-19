package co.edu.unbosque.proyectofinal.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unbosque.proyectofinal.model.User;
import co.edu.unbosque.proyectofinal.model.User.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Clase de prueba para el componente JwtUtil.
 *
 * @author Universidad El Bosque
 * @version 1.0
 */
@SpringBootTest
class JwtUtilTest {

  /** Utilidad para generar y validar tokens JWT. */
  @Autowired private JwtUtil jwtUtil;

  /** Constructor vacío de la clase de pruebas. */
  JwtUtilTest() {}

  /** El rol queda dentro del token y se puede extraer. */
  @Test
  void testRoleIsIncludedInJwtToken() {
    User user = new User("testuser", "test@test.com", "Test", "x", Role.ADMIN);
    String token = jwtUtil.generateToken(user);
    String role = jwtUtil.extractRole(token);
    assertNotNull(role, "El rol no debe ser nulo");
    assertEquals("ADMIN", role, "El rol debe ser ADMIN");
    assertTrue(jwtUtil.validateToken(token, user));
  }

  /** Un token de un usuario no sirve para otro. */
  @Test
  void tokenDeOtroUsuario() {
    User a = new User("usuarioa", "a@test.com", "A", "x");
    User b = new User("usuariob", "b@test.com", "B", "x");
    assertFalse(jwtUtil.validateToken(jwtUtil.generateToken(a), b));
  }

  /** Un token alterado se rechaza al leerlo. */
  @Test
  void tokenAlterado() {
    String token = jwtUtil.generateToken(new User("usuarioc", "c@test.com", "C", "x"));
    String alterado = token.substring(0, token.length() - 3) + (token.endsWith("abc") ? "xyz" : "abc");
    assertThrows(Exception.class, () -> jwtUtil.extractUsername(alterado));
  }
}
