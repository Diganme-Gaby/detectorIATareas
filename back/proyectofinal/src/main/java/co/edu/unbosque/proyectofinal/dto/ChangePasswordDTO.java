package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;

/**
 * Cambio de contraseña con sesión iniciada.
 */
public class ChangePasswordDTO {

  /** Contraseña actual. */
  private String passwordActual;

  /** Contraseña nueva. */
  private String passwordNueva;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public ChangePasswordDTO() {}

  /**
   * Crea el objeto con los datos principales.
   *
   * @param passwordActual contraseña actual
   * @param passwordNueva contraseña nueva
   */
  public ChangePasswordDTO(String passwordActual, String passwordNueva) {
    this.passwordActual = passwordActual;
    this.passwordNueva = passwordNueva;
  }

  /**
   * Devuelve la contraseña actual.
   *
   * @return contraseña actual
   */
  public String getPasswordActual() {
    return passwordActual;
  }

  /**
   * Cambia la contraseña actual.
   *
   * @param passwordActual contraseña actual
   */
  public void setPasswordActual(String passwordActual) {
    this.passwordActual = passwordActual;
  }

  /**
   * Devuelve la contraseña nueva.
   *
   * @return contraseña nueva
   */
  public String getPasswordNueva() {
    return passwordNueva;
  }

  /**
   * Cambia la contraseña nueva.
   *
   * @param passwordNueva contraseña nueva
   */
  public void setPasswordNueva(String passwordNueva) {
    this.passwordNueva = passwordNueva;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(passwordActual, passwordNueva);
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
    ChangePasswordDTO other = (ChangePasswordDTO) obj;
    return Objects.equals(passwordActual, other.passwordActual)
        && Objects.equals(passwordNueva, other.passwordNueva);
  }

  /**
   * Pasa el objeto a texto para depurar. Deja por fuera contraseñas y códigos de correo, y de
   * los contenidos largos solo muestra el tamaño.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "ChangePasswordDTO [contraseñas ocultas]";
  }
}
