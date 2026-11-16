package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;

/**
 * Datos que envía una persona para crear su cuenta.
 */
public class RegisterDTO {

  /** Nombre de usuario deseado. */
  private String username;

  /** Correo donde llega la verificación. */
  private String email;

  /** Nombre completo. */
  private String nombreCompleto;

  /** Teléfono de contacto (opcional). */
  private String telefono;

  /** Contraseña en texto plano; se codifica antes de guardar. */
  private String password;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public RegisterDTO() {}

  /**
   * Crea el objeto con los datos principales.
   *
   * @param username nombre de usuario deseado
   * @param email correo donde llega la verificación
   * @param nombreCompleto nombre completo
   * @param telefono teléfono de contacto (opcional)
   * @param password contraseña en texto plano; se codifica antes de guardar
   */
  public RegisterDTO(String username, String email, String nombreCompleto, String telefono, String password) {
    this.username = username;
    this.email = email;
    this.nombreCompleto = nombreCompleto;
    this.telefono = telefono;
    this.password = password;
  }

  /**
   * Devuelve el nombre de usuario deseado.
   *
   * @return nombre de usuario deseado
   */
  public String getUsername() {
    return username;
  }

  /**
   * Cambia el nombre de usuario deseado.
   *
   * @param username nombre de usuario deseado
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Devuelve el correo donde llega la verificación.
   *
   * @return correo donde llega la verificación
   */
  public String getEmail() {
    return email;
  }

  /**
   * Cambia el correo donde llega la verificación.
   *
   * @param email correo donde llega la verificación
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Devuelve el nombre completo.
   *
   * @return nombre completo
   */
  public String getNombreCompleto() {
    return nombreCompleto;
  }

  /**
   * Cambia el nombre completo.
   *
   * @param nombreCompleto nombre completo
   */
  public void setNombreCompleto(String nombreCompleto) {
    this.nombreCompleto = nombreCompleto;
  }

  /**
   * Devuelve el teléfono de contacto (opcional).
   *
   * @return teléfono de contacto (opcional)
   */
  public String getTelefono() {
    return telefono;
  }

  /**
   * Cambia el teléfono de contacto (opcional).
   *
   * @param telefono teléfono de contacto (opcional)
   */
  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  /**
   * Devuelve la contraseña en texto plano; se codifica antes de guardar.
   *
   * @return contraseña en texto plano; se codifica antes de guardar
   */
  public String getPassword() {
    return password;
  }

  /**
   * Cambia la contraseña en texto plano; se codifica antes de guardar.
   *
   * @param password contraseña en texto plano; se codifica antes de guardar
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
    return Objects.hash(username, email, nombreCompleto, telefono, password);
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
    RegisterDTO other = (RegisterDTO) obj;
    return Objects.equals(username, other.username)
        && Objects.equals(email, other.email)
        && Objects.equals(nombreCompleto, other.nombreCompleto)
        && Objects.equals(telefono, other.telefono)
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
    return "RegisterDTO [username="
        + username
        + ", email="
        + email
        + ", nombreCompleto="
        + nombreCompleto
        + ", telefono="
        + telefono
        + "]";
  }
}
