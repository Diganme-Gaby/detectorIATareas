package co.edu.unbosque.proyectofinal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Pruebas de lectura de zip con sus límites. */
class ZipUtilTest {

  /** Constructor vacío de la clase de pruebas. */
  ZipUtilTest() {}

  /**
   * Devuelve el zip con dos archivos válidos, uno oculto y uno de __MACOSX.
   *
   * @return zip con dos archivos válidos, uno oculto y uno de __MACOSX
   * @throws IOException si falla la creación
   */
  private byte[] zipDePrueba() throws IOException {
    Map<String, byte[]> archivos = new LinkedHashMap<>();
    archivos.put("tarea/ensayo.txt", "Texto del ensayo".getBytes(StandardCharsets.UTF_8));
    archivos.put("tarea/Main.java", "class Main {}".getBytes(StandardCharsets.UTF_8));
    archivos.put("tarea/.DS_Store", new byte[] {1, 2, 3});
    archivos.put("__MACOSX/tarea/._ensayo.txt", new byte[] {4, 5});
    return ZipUtil.comprimir(archivos);
  }

  /**
   * Se ignoran archivos ocultos y de sistema; cada archivo válido sale por separado.
   *
   * @throws IOException si falla la lectura o escritura del archivo de prueba
   */
  @Test
  void descomprimeIgnorandoOcultos() throws IOException {
    ZipUtil.Resultado r = ZipUtil.descomprimir(zipDePrueba(), 10, 1024, 10_000, ruta -> true);
    assertEquals(2, r.getArchivos().size());
    assertEquals("ensayo.txt", r.getArchivos().get(0).getNombre());
    assertEquals("tarea/ensayo.txt", r.getArchivos().get(0).getRuta());
  }

  /**
   * El límite de cantidad deja el resto como omitido.
   *
   * @throws IOException si falla la lectura o escritura del archivo de prueba
   */
  @Test
  void respetaLimiteDeCantidad() throws IOException {
    ZipUtil.Resultado r = ZipUtil.descomprimir(zipDePrueba(), 1, 1024, 10_000, ruta -> true);
    assertEquals(1, r.getArchivos().size());
    assertEquals(1, r.getOmitidosPorLimite());
  }

  /**
   * Archivos demasiado grandes se omiten.
   *
   * @throws IOException si falla la lectura o escritura del archivo de prueba
   */
  @Test
  void omiteArchivosGrandes() throws IOException {
    ZipUtil.Resultado r = ZipUtil.descomprimir(zipDePrueba(), 10, 14, 10_000, ruta -> true);
    assertEquals(1, r.getArchivos().size());
    assertEquals(1, r.getOmitidosPorTamano());
  }

  /**
   * Protege contra zips que se expanden más de lo permitido.
   *
   * @throws IOException si falla la lectura o escritura del archivo de prueba
   */
  @Test
  void detieneZipBomba() throws IOException {
    Map<String, byte[]> archivos = new LinkedHashMap<>();
    archivos.put("grande.txt", new byte[50_000]);
    byte[] zip = ZipUtil.comprimir(archivos);
    assertThrows(IOException.class, () -> ZipUtil.descomprimir(zip, 10, 100_000, 10_000, ruta -> true));
  }
}
