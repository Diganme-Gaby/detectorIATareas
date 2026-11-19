package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;

/**
 * Repositorio identificado a partir de la URL de GitHub que envía el usuario. No se guarda en la
 * base de datos.
 */
public class GithubRepositoryDTO {

  /** Dueño del repositorio. */
  private String dueno;

  /** Nombre del repositorio. */
  private String nombre;

  /** Rama indicada en la URL, o null. */
  private String rama;

  /** Carpeta indicada en la URL, o null. */
  private String carpeta;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public GithubRepositoryDTO() {}

  /**
   * Crea el objeto con todos sus campos.
   *
   * @param dueno dueño del repositorio
   * @param nombre nombre del repositorio
   * @param rama rama indicada en la URL, o null
   * @param carpeta carpeta indicada en la URL, o null
   */
  public GithubRepositoryDTO(String dueno, String nombre, String rama, String carpeta) {
    this.dueno = dueno;
    this.nombre = nombre;
    this.rama = rama;
    this.carpeta = carpeta;
  }

  /**
   * Devuelve el dueño del repositorio.
   *
   * @return dueño del repositorio
   */
  public String getDueno() {
    return dueno;
  }

  /**
   * Cambia el dueño del repositorio.
   *
   * @param dueno dueño del repositorio
   */
  public void setDueno(String dueno) {
    this.dueno = dueno;
  }

  /**
   * Devuelve el nombre del repositorio.
   *
   * @return nombre del repositorio
   */
  public String getNombre() {
    return nombre;
  }

  /**
   * Cambia el nombre del repositorio.
   *
   * @param nombre nombre del repositorio
   */
  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  /**
   * Devuelve la rama indicada en la URL, o null.
   *
   * @return rama indicada en la URL, o null
   */
  public String getRama() {
    return rama;
  }

  /**
   * Cambia la rama indicada en la URL, o null.
   *
   * @param rama rama indicada en la URL, o null
   */
  public void setRama(String rama) {
    this.rama = rama;
  }

  /**
   * Devuelve la carpeta indicada en la URL, o null.
   *
   * @return carpeta indicada en la URL, o null
   */
  public String getCarpeta() {
    return carpeta;
  }

  /**
   * Cambia la carpeta indicada en la URL, o null.
   *
   * @param carpeta carpeta indicada en la URL, o null
   */
  public void setCarpeta(String carpeta) {
    this.carpeta = carpeta;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(dueno, nombre, rama, carpeta);
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
    GithubRepositoryDTO other = (GithubRepositoryDTO) obj;
    return Objects.equals(dueno, other.dueno)
        && Objects.equals(nombre, other.nombre)
        && Objects.equals(rama, other.rama)
        && Objects.equals(carpeta, other.carpeta);
  }

  /**
   * Pasa el objeto a texto para depurar.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "GithubRepositoryDTO [dueno="
        + dueno
        + ", nombre="
        + nombre
        + ", rama="
        + rama
        + ", carpeta="
        + carpeta
        + "]";
  }
}
