package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;

/**
 * Restablecimiento de contraseña desde el enlace del correo.
 */
public class ResetPasswordDTO {

  /** Código que llegó en el correo. */
  private String token;

  /** Contraseña nueva. */
  private String passwordNueva;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public ResetPasswordDTO() {}

  /**
   * Crea el objeto con los datos principales.
   *
   * @param token código que llegó en el correo
   * @param passwordNueva contraseña nueva
   */
  public ResetPasswordDTO(String token, String passwordNueva) {
    this.token = token;
    this.passwordNueva = passwordNueva;
  }

  /**
   * Devuelve el código que llegó en el correo.
   *
   * @return código que llegó en el correo
   */
  public String getToken() {
    return token;
  }

  /**
   * Cambia el código que llegó en el correo.
   *
   * @param token código que llegó en el correo
   */
  public void setToken(String token) {
    this.token = token;
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
    return Objects.hash(token, passwordNueva);
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
    ResetPasswordDTO other = (ResetPasswordDTO) obj;
    return Objects.equals(token, other.token)
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
    return "ResetPasswordDTO [contrasenas ocultas]";
  }
}
