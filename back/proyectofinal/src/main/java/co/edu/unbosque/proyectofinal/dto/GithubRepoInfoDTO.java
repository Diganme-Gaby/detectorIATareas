package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;

/**
 * Datos de un repositorio que devuelve la API de GitHub. Solo se usa la rama por defecto; no se
 * guarda en la base de datos.
 */
public class GithubRepoInfoDTO {

  /** Rama por defecto (llega como default_branch). */
  private String defaultBranch;

  /** Código HTTP de la respuesta. */
  private int statuscode;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public GithubRepoInfoDTO() {}

  /**
   * Crea el objeto con todos sus campos.
   *
   * @param defaultBranch rama por defecto (llega como default_branch)
   * @param statuscode código HTTP de la respuesta
   */
  public GithubRepoInfoDTO(String defaultBranch, int statuscode) {
    this.defaultBranch = defaultBranch;
    this.statuscode = statuscode;
  }

  /**
   * Devuelve la rama por defecto (llega como default_branch).
   *
   * @return rama por defecto (llega como default_branch)
   */
  public String getDefaultBranch() {
    return defaultBranch;
  }

  /**
   * Cambia la rama por defecto (llega como default_branch).
   *
   * @param defaultBranch rama por defecto (llega como default_branch)
   */
  public void setDefaultBranch(String defaultBranch) {
    this.defaultBranch = defaultBranch;
  }

  /**
   * Devuelve el código HTTP de la respuesta.
   *
   * @return código HTTP de la respuesta
   */
  public int getStatuscode() {
    return statuscode;
  }

  /**
   * Cambia el código HTTP de la respuesta.
   *
   * @param statuscode código HTTP de la respuesta
   */
  public void setStatuscode(int statuscode) {
    this.statuscode = statuscode;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(defaultBranch, statuscode);
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
    GithubRepoInfoDTO other = (GithubRepoInfoDTO) obj;
    return Objects.equals(defaultBranch, other.defaultBranch)
        && Objects.equals(statuscode, other.statuscode);
  }

  /**
   * Pasa el objeto a texto para depurar.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "GithubRepoInfoDTO [defaultBranch="
        + defaultBranch
        + ", statuscode="
        + statuscode
        + "]";
  }
}
