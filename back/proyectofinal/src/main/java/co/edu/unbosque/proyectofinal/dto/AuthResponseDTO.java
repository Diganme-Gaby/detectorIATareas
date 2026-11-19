package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;

/**
 * Respuesta del inicio de sesión: token JWT, rol y nombre del usuario autenticado. No se guarda
 * en la base de datos.
 */
public class AuthResponseDTO {

  /** Token JWT generado para el usuario autenticado. */
  private String token;

  /** Rol del usuario autenticado. */
  private String role;

  /** Nombre de usuario autenticado. */
  private String username;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public AuthResponseDTO() {}

  /**
   * Crea el objeto con todos sus campos.
   *
   * @param token token JWT generado para el usuario autenticado
   * @param role rol del usuario autenticado
   * @param username nombre de usuario autenticado
   */
  public AuthResponseDTO(String token, String role, String username) {
    this.token = token;
    this.role = role;
    this.username = username;
  }

  /**
   * Devuelve el token JWT generado para el usuario autenticado.
   *
   * @return token JWT generado para el usuario autenticado
   */
  public String getToken() {
    return token;
  }

  /**
   * Cambia el token JWT generado para el usuario autenticado.
   *
   * @param token token JWT generado para el usuario autenticado
   */
  public void setToken(String token) {
    this.token = token;
  }

  /**
   * Devuelve el rol del usuario autenticado.
   *
   * @return rol del usuario autenticado
   */
  public String getRole() {
    return role;
  }

  /**
   * Cambia el rol del usuario autenticado.
   *
   * @param role rol del usuario autenticado
   */
  public void setRole(String role) {
    this.role = role;
  }

  /**
   * Devuelve el nombre de usuario autenticado.
   *
   * @return nombre de usuario autenticado
   */
  public String getUsername() {
    return username;
  }

  /**
   * Cambia el nombre de usuario autenticado.
   *
   * @param username nombre de usuario autenticado
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(token, role, username);
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
    AuthResponseDTO other = (AuthResponseDTO) obj;
    return Objects.equals(token, other.token)
        && Objects.equals(role, other.role)
        && Objects.equals(username, other.username);
  }

  /**
   * Pasa el objeto a texto para depurar.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "AuthResponseDTO [token="
        + (token == null ? "null" : "[oculto]")
        + ", role="
        + role
        + ", username="
        + username
        + "]";
  }
}
