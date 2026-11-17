package co.edu.unbosque.proyectofinal.util;

import co.edu.unbosque.proyectofinal.dto.IncomingFileDTO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Lectura y creación de archivos .zip en memoria, con límites para evitar archivos bomba.
 */
public final class ZipUtil {

  /** Solo tiene métodos estáticos, así que no se instancia. */
  private ZipUtil() {}

  /** Resultado de descomprimir: archivos leídos y cuántos se dejaron por fuera. */
  public static class Resultado {
    /** Archivos leídos. */
    private final List<IncomingFileDTO> archivos = new ArrayList<>();
    /** Archivos válidos que no entraron por el límite de cantidad. */
    private int omitidosPorLimite;
    /** Archivos descartados por tamaño. */
    private int omitidosPorTamano;

    /** Crea un resultado vacío; {@link ZipUtil} lo va llenando al descomprimir. */
    public Resultado() {}

    /**
     * Devuelve los archivos leídos.
     *
     * @return archivos leídos
     */
    public List<IncomingFileDTO> getArchivos() {
      return archivos;
    }

    /**
     * Devuelve los archivos que no entraron por el límite de cantidad.
     *
     * @return archivos que no entraron por el límite de cantidad
     */
    public int getOmitidosPorLimite() {
      return omitidosPorLimite;
    }

    /**
     * Devuelve los archivos descartados por tamaño.
     *
     * @return archivos descartados por tamaño
     */
    public int getOmitidosPorTamano() {
      return omitidosPorTamano;
    }
  }

  /**
   * Descomprime un zip en memoria.
   *
   * @param zip bytes del zip
   * @param maxArchivos máximo de archivos a devolver
   * @param maxBytesArchivo tamaño máximo de cada archivo
   * @param maxBytesTotal tamaño máximo descomprimido en total
   * @param filtro recibe la ruta interna y decide si el archivo se incluye
   * @return archivos leídos y conteo de omitidos
   * @throws IOException si el zip está dañado o supera el tamaño total
   */
  public static Resultado descomprimir(
      byte[] zip, int maxArchivos, long maxBytesArchivo, long maxBytesTotal, Predicate<String> filtro)
      throws IOException {
    try {
      return descomprimir(zip, maxArchivos, maxBytesArchivo, maxBytesTotal, filtro, StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      // Zips creados en Windows antiguos usan CP437 para los nombres.
      return descomprimir(zip, maxArchivos, maxBytesArchivo, maxBytesTotal, filtro, Charset.forName("CP437"));
    }
  }

  /**
   * Descomprime el zip usando una codificación concreta para los nombres de los archivos.
   *
   * @param zip bytes del zip
   * @param maxArchivos máximo de archivos
   * @param maxBytesArchivo tamaño máximo por archivo
   * @param maxBytesTotal tamaño máximo total
   * @param filtro filtro por ruta
   * @param charset codificación de los nombres
   * @return resultado
   * @throws IOException si el zip está dañado
   */
  private static Resultado descomprimir(
      byte[] zip,
      int maxArchivos,
      long maxBytesArchivo,
      long maxBytesTotal,
      Predicate<String> filtro,
      Charset charset)
      throws IOException {
    Resultado resultado = new Resultado();
    long total = 0;
    try (ZipInputStream entrada = new ZipInputStream(new ByteArrayInputStream(zip), charset)) {
      ZipEntry entry;
      byte[] buffer = new byte[8192];
      while ((entry = entrada.getNextEntry()) != null) {
        String ruta = entry.getName().replace('\\', '/');
        if (entry.isDirectory() || esIgnorado(ruta) || !filtro.test(ruta)) {
          continue;
        }
        if (resultado.archivos.size() >= maxArchivos) {
          resultado.omitidosPorLimite++;
          continue;
        }
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        long tamano = 0;
        boolean muyGrande = false;
        int leidos;
        while ((leidos = entrada.read(buffer)) != -1) {
          tamano += leidos;
          total += leidos;
          if (total > maxBytesTotal) {
            throw new IOException("El contenido descomprimido supera el límite permitido");
          }
          if (tamano > maxBytesArchivo) {
            muyGrande = true;
            break;
          }
          salida.write(buffer, 0, leidos);
        }
        if (muyGrande) {
          resultado.omitidosPorTamano++;
          continue;
        }
        if (tamano == 0) {
          continue;
        }
        String nombre = ruta.substring(ruta.lastIndexOf('/') + 1);
        resultado.archivos.add(new IncomingFileDTO(nombre, ruta, salida.toByteArray()));
      }
    }
    return resultado;
  }

  /**
   * Carpetas de sistema y archivos ocultos que nunca se analizan.
   *
   * @param ruta ruta interna
   * @return true si se ignora
   */
  private static boolean esIgnorado(String ruta) {
    if (ruta.startsWith("__MACOSX/") || ruta.contains("/__MACOSX/")) {
      return true;
    }
    String nombre = ruta.substring(ruta.lastIndexOf('/') + 1);
    return nombre.startsWith(".") || nombre.equalsIgnoreCase("Thumbs.db") || nombre.toLowerCase().endsWith(".zip");
  }

  /**
   * Crea un zip con varios archivos. Se usa para adjuntar muchos reportes en un solo correo.
   *
   * @param archivos nombre y contenido de cada archivo
   * @return bytes del zip
   * @throws IOException si falla la escritura
   */
  public static byte[] comprimir(java.util.Map<String, byte[]> archivos) throws IOException {
    ByteArrayOutputStream salida = new ByteArrayOutputStream();
    try (ZipOutputStream zip = new ZipOutputStream(salida, StandardCharsets.UTF_8)) {
      for (java.util.Map.Entry<String, byte[]> archivo : archivos.entrySet()) {
        zip.putNextEntry(new ZipEntry(archivo.getKey()));
        zip.write(archivo.getValue());
        zip.closeEntry();
      }
    }
    return salida.toByteArray();
  }
}
