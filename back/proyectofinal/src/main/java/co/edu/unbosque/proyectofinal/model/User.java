package co.edu.unbosque.proyectofinal.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Cuenta de usuario del detector de IA. Implementa UserDetails para que Spring Security la use
 * directamente al autenticar.
 *
 * <p>El nombre de usuario y el correo no cambian después del registro. La cuenta solo puede iniciar
 * sesión cuando el administrador la tiene activa y el correo ya fue confirmado.
 */
@Entity
@Table(name = "usuario_detector")
public class User implements UserDetails {

  /** Número de versión para la serialización. */
  private static final long serialVersionUID = 1L;

  /** Identificador generado por la base de datos. */
  private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

  /** Nombre de usuario con el que se inicia sesión. */
  @Column(unique = true, nullable = false, updatable = false)
  private String username;

  /** Correo al que llegan la verificación, la recuperación y los reportes. */
  @Column(unique = true, nullable = false, updatable = false)
  private String email;

  /** Nombre y apellidos de la persona. */
  private String nombreCompleto;

  /** Número de contacto (opcional), cifrado con AES (Base64). */
  @Column(nullable = true)
  private String telefono;

  /** Hash BCrypt de la contraseña. Nunca sale de la capa de servicio. */
  private String password;

  /** Rol dentro del sistema. */
  @Enumerated(EnumType.STRING)
  private Role role;

  /** La cuenta no ha expirado. */
  private boolean accountNonExpired;

  /** Las credenciales no han expirado. */
  private boolean credentialsNonExpired;

  /** El administrador tiene la cuenta activa. */
  private boolean activo;

  /** El usuario ya confirmó su correo. */
  private boolean correoVerificado;

  /** Intentos de inicio de sesión fallidos seguidos. */
  private int intentosFallidos;

  /** Hasta cuando queda bloqueada la cuenta por intentos fallidos. Nulo si no está bloqueada. */
  @Column(nullable = true)
  private LocalDateTime bloqueadoHasta;

  /** Momento en que se creó la cuenta. */
  @Column(updatable = false)
  private LocalDateTime fechaRegistro;

  /** Último inicio de sesión correcto. */
  @Column(nullable = true)
  private LocalDateTime ultimoAcceso;

  /**
   * Constructor vacío que exige JPA. La cuenta queda activa, sin verificar y con rol USER.
   */
  public User() {
    this.accountNonExpired = true;
    this.credentialsNonExpired = true;
    this.activo = true;
    this.correoVerificado = false;
    this.role = Role.USER;
    this.fechaRegistro = LocalDateTime.now();
  }

  /**
   * Constructor con los datos del registro.
   *
   * @param username nombre de usuario
   * @param email correo electrónico
   * @param nombreCompleto nombre de la persona
   * @param password contraseña ya codificada
   */
  public User(String username, String email, String nombreCompleto, String password) {
    this();
    this.username = username;
    this.email = email;
    this.nombreCompleto = nombreCompleto;
    this.password = password;
  }

  /**
   * Constructor con rol explícito, usado al precargar usuarios.
   *
   * @param username nombre de usuario
   * @param email correo electrónico
   * @param nombreCompleto nombre de la persona
   * @param password contraseña ya codificada
   * @param rol rol asignado
   */
  public User(String username, String email, String nombreCompleto, String password, Role rol) {
    this(username, email, nombreCompleto, password);
    this.role = rol;
  }

  /**
   * Roles disponibles.
   *
   * <p>USER: analiza archivos y consulta su historial. ADMIN: además administra usuarios, auditoría
   * y reportes.
   */
  public enum Role {
    /** Usuario regular. */
    USER,
    /** Administrador. */
    ADMIN
  }

  /**
   * Autoridades del usuario con el prefijo ROLE_ que espera Spring Security.
   *
   * @return lista con el rol del usuario
   */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  /**
   * Indica si la cuenta no ha expirado.
   *
   * @return true si sigue vigente
   */
  @Override
  public boolean isAccountNonExpired() {
    return accountNonExpired;
  }

  /**
   * La cuenta está bloqueada mientras no haya pasado la fecha de {@code bloqueadoHasta}.
   *
   * @return true si se puede usar
   */
  @Override
  public boolean isAccountNonLocked() {
    return bloqueadoHasta == null || bloqueadoHasta.isBefore(LocalDateTime.now());
  }

  /**
   * Indica si las credenciales no han expirado.
   *
   * @return true si siguen vigentes
   */
  @Override
  public boolean isCredentialsNonExpired() {
    return credentialsNonExpired;
  }

  /**
   * Para Spring Security la cuenta está habilitada solo si está activa y con correo verificado.
   *
   * @return true si puede iniciar sesión
   */
  @Override
  public boolean isEnabled() {
    return activo && correoVerificado;
  }

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
  @Override
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
   * Devuelve el teléfono.
   *
   * @return teléfono
   */
  public String getTelefono() {
    return telefono;
  }

  /**
   * Cambia el teléfono.
   *
   * @param telefono teléfono
   */
  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  /**
   * Devuelve el hash de la contraseña.
   *
   * @return hash de la contraseña
   */
  @Override
  public String getPassword() {
    return password;
  }

  /**
   * Cambia el hash de la contraseña.
   *
   * @param password hash de la contraseña
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Devuelve el rol.
   *
   * @return rol
   */
  public Role getRole() {
    return role;
  }

  /**
   * Cambia el rol.
   *
   * @param role rol
   */
  public void setRole(Role role) {
    this.role = role;
  }

  /**
   * Cambia si la cuenta sigue vigente.
   *
   * @param accountNonExpired si la cuenta sigue vigente
   */
  public void setAccountNonExpired(boolean accountNonExpired) {
    this.accountNonExpired = accountNonExpired;
  }

  /**
   * Cambia si las credenciales siguen vigentes.
   *
   * @param credentialsNonExpired si las credenciales siguen vigentes
   */
  public void setCredentialsNonExpired(boolean credentialsNonExpired) {
    this.credentialsNonExpired = credentialsNonExpired;
  }

  /**
   * Devuelve true si el administrador tiene la cuenta activa.
   *
   * @return true si el administrador tiene la cuenta activa
   */
  public boolean isActivo() {
    return activo;
  }

  /**
   * Cambia el estado que asigna el administrador.
   *
   * @param activo estado que asigna el administrador
   */
  public void setActivo(boolean activo) {
    this.activo = activo;
  }

  /**
   * Devuelve true si ya confirmó el correo.
   *
   * @return true si ya confirmó el correo
   */
  public boolean isCorreoVerificado() {
    return correoVerificado;
  }

  /**
   * Cambia el estado de la verificación.
   *
   * @param correoVerificado estado de la verificación
   */
  public void setCorreoVerificado(boolean correoVerificado) {
    this.correoVerificado = correoVerificado;
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
   * Devuelve la fecha hasta la que dura el bloqueo, o null.
   *
   * @return fecha hasta la que dura el bloqueo, o null
   */
  public LocalDateTime getBloqueadoHasta() {
    return bloqueadoHasta;
  }

  /**
   * Cambia la fecha de fin del bloqueo, o null para desbloquear.
   *
   * @param bloqueadoHasta fecha de fin del bloqueo, o null para desbloquear
   */
  public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) {
    this.bloqueadoHasta = bloqueadoHasta;
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
   * Devuelve el último acceso correcto.
   *
   * @return último acceso correcto
   */
  public LocalDateTime getUltimoAcceso() {
    return ultimoAcceso;
  }

  /**
   * Cambia el último acceso correcto.
   *
   * @param ultimoAcceso último acceso correcto
   */
  public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
    this.ultimoAcceso = ultimoAcceso;
  }

  /**
   * El hash usa solo campos que no cambian.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, username, email);
  }

  /**
   * Dos usuarios son iguales si coinciden id, username y correo.
   *
   * @param obj objeto a comparar
   * @return true si son el mismo usuario
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    User other = (User) obj;
    return Objects.equals(id, other.id)
        && Objects.equals(username, other.username)
        && Objects.equals(email, other.email);
  }

  /**
   * Representación de texto sin la contraseña.
   *
   * @return texto con los datos basicos
   */
  @Override
  public String toString() {
    return "User [id=" + id + ", username=" + username + ", email=" + email + ", role=" + role
        + ", activo=" + activo + ", correoVerificado=" + correoVerificado + "]";
  }
}
