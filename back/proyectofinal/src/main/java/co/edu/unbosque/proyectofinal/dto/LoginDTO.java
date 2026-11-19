package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;

/**
 * Credenciales para iniciar sesión.
 */
public class LoginDTO {

  /** Nombre de usuario. */
  private String username;

  /** Contraseña. */
  private String password;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public LoginDTO() {}

  /**
   * Crea el objeto con los datos principales.
   *
   * @param username nombre de usuario
   * @param password contraseña
   */
  public LoginDTO(String username, String password) {
    this.username = username;
    this.password = password;
  }

  /**
   * Devuelve el nombre de usuario.
   *
   * @return nombre de usuario
   */
  public String getUsername() {
    return username;
  }

  /**
   * Cambia el nombre de usuario.
   *
   * @param username nombre de usuario
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Devuelve la contraseña.
   *
   * @return contraseña
   */
  public String getPassword() {
    return password;
  }

  /**
   * Cambia la contraseña.
   *
   * @param password contraseña
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(username, password);
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
    LoginDTO other = (LoginDTO) obj;
    return Objects.equals(username, other.username)
        && Objects.equals(password, other.password);
  }

  /**
   * Pasa el objeto a texto para depurar. Deja por fuera contraseñas y códigos de correo, y de
   * los contenidos largos solo muestra el tamaño.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "LoginDTO [username="
        + username
        + "]";
  }
}
