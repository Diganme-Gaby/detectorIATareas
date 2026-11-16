package co.edu.unbosque.proyectofinal.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unbosque.proyectofinal.exception.PasswordNotValidException;
import org.junit.jupiter.api.Test;

/** Pruebas de las reglas de validación de datos de entrada. */
class InputValidatorTest {

  /** Constructor vacío de la clase de pruebas. */
  InputValidatorTest() {}

  /** Contraseñas que cumplen el estándar. */
  @Test
  void aceptaPasswordsSeguras() {
    String[] validas = {"Clave#2026", "aB3$aaaa", "Otra_Clave9"};
    for (String password : validas) {
      assertTrue(InputValidator.passwordValida(password), password);
      assertDoesNotThrow(() -> InputValidator.validarPassword(password), password);
    }
  }

  /** Contraseñas sin alguno de los requisitos. */
  @Test
  void rechazaPasswordsDebiles() {
    String[] debiles = {"corta1#", "sinmayuscula1#", "SINMINUSCULA1#", "SinNumero#", "SinSimbolo12", ""};
    for (String password : debiles) {
      assertFalse(InputValidator.passwordValida(password), password);
      assertThrows(PasswordNotValidException.class, () -> InputValidator.validarPassword(password), password);
    }
  }

  /** Filtros de fecha recibidos como texto. */
  @Test
  void interpretaFechas() {
    assertTrue(InputValidator.fecha("2026-11-01") != null);
    assertTrue(InputValidator.fecha(null) == null);
    assertTrue(InputValidator.fecha("  ") == null);
    assertTrue(InputValidator.fecha("01/11/2026") == null);
  }

  /** Nombres de usuario permitidos y no permitidos. */
  @Test
  void validaUsername() {
    assertTrue(InputValidator.usernameValido("laura.gomez"));
    assertTrue(InputValidator.usernameValido("user_01"));
    assertFalse(InputValidator.usernameValido("abc"));
    assertFalse(InputValidator.usernameValido("con espacio"));
    assertFalse(InputValidator.usernameValido("<script>"));
    assertFalse(InputValidator.usernameValido("' OR 1=1 --"));
    assertFalse(InputValidator.usernameValido(null));
  }

  /** Formato de correo. */
  @Test
  void validaEmail() {
    assertTrue(InputValidator.emailValido("laura.gomez@ejemplo.edu.co"));
    assertFalse(InputValidator.emailValido("laura@"));
    assertFalse(InputValidator.emailValido("sin-arroba.com"));
    assertFalse(InputValidator.emailValido(null));
  }

  /** El nombre no puede traer HTML. */
  @Test
  void nombreSinHtml() {
    assertTrue(InputValidator.nombreValido("Laura Gómez"));
    assertFalse(InputValidator.nombreValido("<img src=x onerror=alert(1)>"));
    assertFalse(InputValidator.nombreValido("   "));
  }

  /** El teléfono es opcional. */
  @Test
  void telefonoOpcional() {
    assertTrue(InputValidator.telefonoValido(null));
    assertTrue(InputValidator.telefonoValido(""));
    assertTrue(InputValidator.telefonoValido("+57 300 123 4567"));
    assertFalse(InputValidator.telefonoValido("abc"));
  }
}
