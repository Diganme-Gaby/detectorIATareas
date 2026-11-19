package co.edu.unbosque.proyectofinal.dto;

import co.edu.unbosque.proyectofinal.model.User.Role;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Datos de un usuario que se envían al front. Como en la guía, trae el campo de contraseña para
 * {@code CRUDOperation.create}, pero el servicio nunca lo llena al responder, así que siempre sale
 * vacío.
 */
public class UserDTO {

  /** Identificador. */
  private Long id;

  /** Nombre de usuario. */
  private String username;

  /** Correo electrónico. */
  private String email;

  /** Nombre completo. */
  private String nombreCompleto;

  /** Teléfono de contacto. */
  private String telefono;

  /** Contraseña en texto plano. Solo se recibe; nunca se envía de vuelta. */
  private String password;

  /** Rol en el sistema. */
  private Role role;

  /** Estado asignado por el administrador. */
  private boolean activo;

  /** Indica si el correo ya fue confirmado. */
  private boolean correoVerificado;

  /** Indica si la cuenta está bloqueada por intentos fallidos. */
  private boolean bloqueado;

  /** Intentos fallidos seguidos. */
  private int intentosFallidos;

  /** Fecha de registro. */
  private LocalDateTime fechaRegistro;

  /** Último inicio de sesión correcto. */
  private LocalDateTime ultimoAcceso;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public UserDTO() {}

  /**
   * Devuelve el identificador.
   *
   * @return identificador
   */
  public Long getId() {
    return id;
  }

  /**
   * Cambia el identificador.
   *
   * @param id identificador
   */
  public void setId(Long id) {
    this.id = id;
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
   * Devuelve el correo electrónico.
   *
   * @return correo electrónico
   */
  public String getEmail() {
    return email;
  }

  /**
   * Cambia el correo electrónico.
   *
   * @param email correo electrónico
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
   * Devuelve la contraseña recibida.
   *
   * @return contraseña recibida
   */
  public String getPassword() {
    return password;
  }

  /**
   * Cambia la contraseña recibida.
   *
   * @param password contraseña recibida
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Devuelve el rol en el sistema.
   *
   * @return rol en el sistema
   */
  public Role getRole() {
    return role;
  }

  /**
   * Cambia el rol en el sistema.
   *
   * @param role rol en el sistema
   */
  public void setRole(Role role) {
    this.role = role;
  }

  /**
   * Devuelve el estado asignado por el administrador.
   *
   * @return estado asignado por el administrador
   */
  public boolean isActivo() {
    return activo;
  }

  /**
   * Cambia el estado asignado por el administrador.
   *
   * @param activo estado asignado por el administrador
   */
  public void setActivo(boolean activo) {
    this.activo = activo;
  }

  /**
   * Indica si el correo ya fue confirmado.
   *
   * @return indica si el correo ya fue confirmado
   */
  public boolean isCorreoVerificado() {
    return correoVerificado;
  }

  /**
   * Define si el correo ya fue confirmado.
   *
   * @param correoVerificado indica si el correo ya fue confirmado
   */
  public void setCorreoVerificado(boolean correoVerificado) {
    this.correoVerificado = correoVerificado;
  }

  /**
   * Indica si la cuenta está bloqueada por intentos fallidos.
   *
   * @return indica si la cuenta está bloqueada por intentos fallidos
   */
  public boolean isBloqueado() {
    return bloqueado;
  }

  /**
   * Define si la cuenta está bloqueada por intentos fallidos.
   *
   * @param bloqueado indica si la cuenta está bloqueada por intentos fallidos
   */
  public void setBloqueado(boolean bloqueado) {
    this.bloqueado = bloqueado;
  }

  /**
   * Devuelve los intentos fallidos seguidos.
   *
   * @return intentos fallidos seguidos
   */
  public int getIntentosFallidos() {
    return intentosFallidos;
  }

  /**
   * Cambia los intentos fallidos seguidos.
   *
   * @param intentosFallidos intentos fallidos seguidos
   */
  public void setIntentosFallidos(int intentosFallidos) {
    this.intentosFallidos = intentosFallidos;
  }

  /**
   * Devuelve la fecha de registro.
   *
   * @return fecha de registro
   */
  public LocalDateTime getFechaRegistro() {
    return fechaRegistro;
  }

  /**
   * Cambia la fecha de registro.
   *
   * @param fechaRegistro fecha de registro
   */
  public void setFechaRegistro(LocalDateTime fechaRegistro) {
    this.fechaRegistro = fechaRegistro;
  }

  /**
   * Devuelve el último inicio de sesión correcto.
   *
   * @return último inicio de sesión correcto
   */
  public LocalDateTime getUltimoAcceso() {
    return ultimoAcceso;
  }

  /**
   * Cambia el último inicio de sesión correcto.
   *
   * @param ultimoAcceso último inicio de sesión correcto
   */
  public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
    this.ultimoAcceso = ultimoAcceso;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, username, email, nombreCompleto, telefono, role, activo,
            correoVerificado, bloqueado, intentosFallidos, fechaRegistro, ultimoAcceso);
  }

  /**
   * Dos objetos son iguales si todos sus campos coinciden, salvo la contraseña.
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
    UserDTO other = (UserDTO) obj;
    return Objects.equals(id, other.id)
        && Objects.equals(username, other.username)
        && Objects.equals(email, other.email)
        && Objects.equals(nombreCompleto, other.nombreCompleto)
        && Objects.equals(telefono, other.telefono)
        && Objects.equals(role, other.role)
        && activo == other.activo
        && correoVerificado == other.correoVerificado
        && bloqueado == other.bloqueado
        && intentosFallidos == other.intentosFallidos
        && Objects.equals(fechaRegistro, other.fechaRegistro)
        && Objects.equals(ultimoAcceso, other.ultimoAcceso);
  }

  /**
   * Pasa el objeto a texto para depurar. Deja por fuera contraseñas y códigos de correo, y de
   * los contenidos largos solo muestra el tamaño.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "UserDTO [id="
        + id
        + ", username="
        + username
        + ", email="
        + email
        + ", nombreCompleto="
        + nombreCompleto
        + ", telefono="
        + telefono
        + ", role="
        + role
        + ", activo="
        + activo
        + ", correoVerificado="
        + correoVerificado
        + ", bloqueado="
        + bloqueado
        + ", intentosFallidos="
        + intentosFallidos
        + ", fechaRegistro="
        + fechaRegistro
        + ", ultimoAcceso="
        + ultimoAcceso
        + "]";
  }
}
