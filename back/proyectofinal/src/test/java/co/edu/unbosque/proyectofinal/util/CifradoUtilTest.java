package co.edu.unbosque.proyectofinal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Pruebas del cifrado con un IV distinto para cada valor. */
class CifradoUtilTest {

  /** Llave de 16 caracteres que solo se usa en estas pruebas. */
  private static final String LLAVE = "llavePruebas1234";

  /** Constructor vacío de la clase de pruebas. */
  CifradoUtilTest() {}

  /** Lo que se cifra se puede descifrar con la misma llave. */
  @Test
  void cifraYDescifra() {
    String guardado = CifradoUtil.cifrar(LLAVE, "3001234567");
    assertNotEquals("3001234567", guardado);
    assertTrue(CifradoUtil.tieneFormato(guardado));
    assertEquals("3001234567", CifradoUtil.descifrar(LLAVE, guardado));
  }

  /** El mismo texto cifrado dos veces da resultados distintos, porque el IV cambia. */
  @Test
  void usaUnIvDistintoCadaVez() {
    String primero = CifradoUtil.cifrar(LLAVE, "192.168.0.10");
    String segundo = CifradoUtil.cifrar(LLAVE, "192.168.0.10");
    assertNotEquals(primero, segundo);
    assertNotEquals(primero.split(":")[0], segundo.split(":")[0]);
  }

  /** Los valores vacíos no se cifran y los que no tienen el formato no se descifran. */
  @Test
  void ignoraValoresSinFormato() {
    assertNull(CifradoUtil.cifrar(LLAVE, null));
    assertEquals("", CifradoUtil.cifrar(LLAVE, ""));
    assertNull(CifradoUtil.descifrar(LLAVE, null));
    assertNull(CifradoUtil.descifrar(LLAVE, "3001234567"));
    assertFalse(CifradoUtil.tieneFormato("sin-separador"));
    assertFalse(CifradoUtil.tieneFormato(":solo-texto"));
  }
}
