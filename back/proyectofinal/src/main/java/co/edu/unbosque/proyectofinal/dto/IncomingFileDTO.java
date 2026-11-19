package co.edu.unbosque.proyectofinal.dto;

import java.util.Arrays;
import java.util.Objects;

/**
 * Archivo ya leído en memoria, listo para procesarse en segundo plano.
 */
public class IncomingFileDTO {

  /** Nombre del archivo. */
  private String nombre;

  /** Ruta dentro del zip o repositorio. */
  private String ruta;

  /** Bytes del archivo. */
  private byte[] contenido;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public IncomingFileDTO() {}

  /**
   * Crea el objeto con los datos principales.
   *
   * @param nombre nombre del archivo
   * @param ruta ruta dentro del zip o repositorio
   * @param contenido bytes del archivo
   */
  public IncomingFileDTO(String nombre, String ruta, byte[] contenido) {
    this.nombre = nombre;
    this.ruta = ruta;
    this.contenido = contenido;
  }

  /**
   * Devuelve el nombre del archivo.
   *
   * @return nombre del archivo
   */
  public String getNombre() {
    return nombre;
  }

  /**
   * Cambia el nombre del archivo.
   *
   * @param nombre nombre del archivo
   */
  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  /**
   * Devuelve la ruta dentro del zip o repositorio.
   *
   * @return ruta dentro del zip o repositorio
   */
  public String getRuta() {
    return ruta;
  }

  /**
   * Cambia la ruta dentro del zip o repositorio.
   *
   * @param ruta ruta dentro del zip o repositorio
   */
  public void setRuta(String ruta) {
    this.ruta = ruta;
  }

  /**
   * Devuelve los bytes del archivo.
   *
   * @return bytes del archivo
   */
  public byte[] getContenido() {
    return contenido;
  }

  /**
   * Cambia los bytes del archivo.
   *
   * @param contenido bytes del archivo
   */
  public void setContenido(byte[] contenido) {
    this.contenido = contenido;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    int result = Objects.hash(nombre, ruta);
    result = 31 * result + Arrays.hashCode(contenido);
    return result;
  }

  /**
   * Compara campo por campo: son iguales si todos coinciden.
   *
   * @param obj objeto a comparar
   * @return true si son iguales
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    IncomingFileDTO other = (IncomingFileDTO) obj;
    return Objects.equals(nombre, other.nombre)
        && Objects.equals(ruta, other.ruta)
        && Arrays.equals(contenido, other.contenido);
  }

  /**
   * Pasa el objeto a texto para depurar. Deja por fuera contraseñas y códigos de correo, y de
   * los contenidos largos solo muestra el tamaño.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "IncomingFileDTO [nombre="
        + nombre
        + ", ruta="
        + ruta
        + ", contenido="
        + (contenido == null ? 0 : contenido.length) + " bytes"
        + "]";
  }
}
