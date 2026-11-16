package co.edu.unbosque.proyectofinal.model;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Registro de auditoría: quién hizo qué, cuándo y desde dónde. Ninguna columna se puede actualizar
 * y la aplicación no expone forma de borrar registros.
 */
@Entity
@Table(name = "auditoria")
public class Audit {

  /** Identificador generado por la base de datos. */
  private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

  /** Usuario que hizo la acción, o "anonimo" si no había sesión. */
  @Column(updatable = false, nullable = false)
  private String username;

  /** Acción realizada. */
  @Enumerated(EnumType.STRING)
  @Column(updatable = false, nullable = false, length = 40)
  private Action accion;

  /** Detalle legible de la acción. */
  @Column(updatable = false, length = 1000)
  private String descripcion;

  /** Fecha y hora del servidor. */
  @Column(updatable = false, nullable = false)
  private LocalDateTime fecha;

  /**
   * Dirección IP desde la que llegó la petición, cifrada con AES (Base64). Se guarda en la columna
   * ip_cifrada, que Hibernate crea sola con ddl-auto=update; la columna ip de los registros
   * anteriores al cifrado queda en la tabla sin usarse.
   */
  @Column(name = "ip_cifrada", updatable = false, length = 128)
  private String ip;

  /** Latitud aproximada de la IP. */
  @Column(updatable = false)
  private Double latitud;

  /** Longitud aproximada de la IP. */
  @Column(updatable = false)
  private Double longitud;

  /** Ciudad aproximada de la IP. */
  @Column(updatable = false)
  private String ciudad;

  /** País aproximado de la IP. */
  @Column(updatable = false)
  private String pais;

  /** Acciones que quedan registradas. */
  public enum Action {
    /** Se creó una cuenta. */
    REGISTRO,
    /** Se confirmó un correo. */
    VERIFICACION_CORREO,
    /** Se pidió reenviar el correo de verificación. */
    REENVIO_VERIFICACION,
    /** Inicio de sesión correcto. */
    LOGIN_EXITOSO,
    /** Inicio de sesión con credenciales incorrectas. */
    LOGIN_FALLIDO,
    /** La cuenta se bloqueo por intentos fallidos. */
    CUENTA_BLOQUEADA,
    /** Se pidió recuperar la contraseña. */
    SOLICITUD_RECUPERACION,
    /** Se cambió o restableció la contraseña. */
    CAMBIO_CONTRASENA,
    /** El usuario actualizó sus datos. */
    ACTUALIZACION_PERFIL,
    /** Se envió un análisis. */
    ANALISIS_CREADO,
    /** Terminó un análisis. */
    ANALISIS_FINALIZADO,
    /** Se descargo o visualizo un reporte. */
    DESCARGA_REPORTE,
    /** Se reenvió un reporte al correo. */
    REENVIO_REPORTE,
    /** El administrador modifico un usuario. */
    ADMIN_ACTUALIZA_USUARIO,
    /** El administrador activo una cuenta. */
    ADMIN_ACTIVA_USUARIO,
    /** El administrador desactivó una cuenta. */
    ADMIN_DESACTIVA_USUARIO,
    /** El administrador desbloqueó una cuenta. */
    ADMIN_DESBLOQUEA_USUARIO,
    /** El administrador descargo un reporte administrativo. */
    ADMIN_DESCARGA_REPORTE
  }

  /** Constructor por defecto requerido por JPA. */
  public Audit() {
    this.fecha = LocalDateTime.now();
  }

  /**
   * Crea un registro con la fecha actual.
   *
   * @param username usuario que hizo la acción
   * @param accion acción realizada
   * @param descripcion detalle legible
   */
  public Audit(String username, Action accion, String descripcion) {
    this();
    this.username = username;
    this.accion = accion;
    this.descripcion = descripcion;
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
   * Devuelve el usuario.
   *
   * @return usuario
   */
  public String getUsername() {
    return username;
  }

  /**
   * Cambia el usuario.
   *
   * @param username usuario
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Devuelve la acción.
   *
   * @return acción
   */
  public Action getAccion() {
    return accion;
  }

  /**
   * Cambia la acción.
   *
   * @param accion acción
   */
  public void setAccion(Action accion) {
    this.accion = accion;
  }

  /**
   * Devuelve la descripción.
   *
   * @return descripción
   */
  public String getDescripcion() {
    return descripcion;
  }

  /**
   * Cambia la descripción.
   *
   * @param descripcion descripción
   */
  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  /**
   * Devuelve la fecha y hora.
   *
   * @return fecha y hora
   */
  public LocalDateTime getFecha() {
    return fecha;
  }

  /**
   * Cambia la fecha y hora.
   *
   * @param fecha fecha y hora
   */
  public void setFecha(LocalDateTime fecha) {
    this.fecha = fecha;
  }

  /**
   * Devuelve la ip de origen.
   *
   * @return ip de origen
   */
  public String getIp() {
    return ip;
  }

  /**
   * Cambia la ip de origen.
   *
   * @param ip ip de origen
   */
  public void setIp(String ip) {
    this.ip = ip;
  }

  /**
   * Devuelve la latitud.
   *
   * @return latitud
   */
  public Double getLatitud() {
    return latitud;
  }

  /**
   * Cambia la latitud.
   *
   * @param latitud latitud
   */
  public void setLatitud(Double latitud) {
    this.latitud = latitud;
  }

  /**
   * Devuelve la longitud.
   *
   * @return longitud
   */
  public Double getLongitud() {
    return longitud;
  }

  /**
   * Cambia la longitud.
   *
   * @param longitud longitud
   */
  public void setLongitud(Double longitud) {
    this.longitud = longitud;
  }

  /**
   * Devuelve la ciudad.
   *
   * @return ciudad
   */
  public String getCiudad() {
    return ciudad;
  }

  /**
   * Cambia la ciudad.
   *
   * @param ciudad ciudad
   */
  public void setCiudad(String ciudad) {
    this.ciudad = ciudad;
  }

  /**
   * Devuelve el país.
   *
   * @return país
   */
  public String getPais() {
    return pais;
  }

  /**
   * Cambia el país.
   *
   * @param pais país
   */
  public void setPais(String pais) {
    this.pais = pais;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, username, accion);
  }

  /**
   * Dos registros son iguales si coinciden id, username, acción.
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
    Audit other = (Audit) obj;
    return Objects.equals(id, other.id)
        && Objects.equals(username, other.username)
        && Objects.equals(accion, other.accion);
  }

  /**
   * Pasa el objeto a texto para depurar. Deja por fuera contraseñas y códigos de correo, y de
   * los contenidos largos solo muestra el tamaño.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "Audit [id="
        + id
        + ", username="
        + username
        + ", accion="
        + accion
        + ", descripcion="
        + descripcion
        + ", fecha="
        + fecha
        + ", ip="
        + ip
        + ", latitud="
        + latitud
        + ", longitud="
        + longitud
        + ", ciudad="
        + ciudad
        + ", pais="
        + pais
        + "]";
  }
}
