package co.edu.unbosque.proyectofinal.dto;

import co.edu.unbosque.proyectofinal.model.User.Role;
import java.util.Objects;

/**
 * Datos que el usuario o el administrador pueden cambiar.
 * El nombre de usuario y el correo no se pueden cambiar.
 */
public class UpdateProfileDTO {

  /** Nombre completo. */
  private String nombreCompleto;

  /** Teléfono de contacto. */
  private String telefono;

  /** Rol nuevo; solo lo tiene en cuenta el administrador. */
  private Role role;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public UpdateProfileDTO() {}

  /**
   * Crea el objeto con los datos principales.
   *
   * @param nombreCompleto nombre completo
   * @param telefono teléfono de contacto
   */
  public UpdateProfileDTO(String nombreCompleto, String telefono) {
    this.nombreCompleto = nombreCompleto;
    this.telefono = telefono;
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
   * Devuelve el teléfono de contacto.
   *
   * @return teléfono de contacto
   */
  public String getTelefono() {
    return telefono;
  }

  /**
   * Cambia el teléfono de contacto.
   *
   * @param telefono teléfono de contacto
   */
  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  /**
   * Devuelve el rol nuevo; solo lo tiene en cuenta el administrador.
   *
   * @return rol nuevo; solo lo tiene en cuenta el administrador
   */
  public Role getRole() {
    return role;
  }

  /**
   * Cambia el rol nuevo; solo lo tiene en cuenta el administrador.
   *
   * @param role rol nuevo; solo lo tiene en cuenta el administrador
   */
  public void setRole(Role role) {
    this.role = role;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(nombreCompleto, telefono, role);
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
    UpdateProfileDTO other = (UpdateProfileDTO) obj;
    return Objects.equals(nombreCompleto, other.nombreCompleto)
        && Objects.equals(telefono, other.telefono)
        && Objects.equals(role, other.role);
  }

  /**
   * Pasa el objeto a texto para depurar. Deja por fuera contraseñas y códigos de correo, y de
   * los contenidos largos solo muestra el tamaño.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "UpdateProfileDTO [nombreCompleto="
        + nombreCompleto
        + ", telefono="
        + telefono
        + ", role="
        + role
        + "]";
  }
}
