package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;
import co.edu.unbosque.proyectofinal.model.Audit.Action;
import java.time.LocalDateTime;

/**
 * Registro de auditoría tal como lo ve el administrador.
 */
public class AuditDTO {

  /** Identificador. */
  private Long id;

  /** Usuario que hizo la acción. */
  private String username;

  /** Acción realizada. */
  private Action accion;

  /** Detalle de la acción. */
  private String descripcion;

  /** Fecha y hora. */
  private LocalDateTime fecha;

  /** Dirección IP de origen. */
  private String ip;

  /** Latitud aproximada. */
  private Double latitud;

  /** Longitud aproximada. */
  private Double longitud;

  /** Ciudad aproximada. */
  private String ciudad;

  /** País aproximado. */
  private String pais;

  /** Enlace a Google Maps con la ubicación. */
  private String mapaUrl;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public AuditDTO() {}

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
   * Devuelve el usuario que hizo la acción.
   *
   * @return usuario que hizo la acción
   */
  public String getUsername() {
    return username;
  }

  /**
   * Cambia el usuario que hizo la acción.
   *
   * @param username usuario que hizo la acción
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Devuelve la acción realizada.
   *
   * @return acción realizada
   */
  public Action getAccion() {
    return accion;
  }

  /**
   * Cambia la acción realizada.
   *
   * @param accion acción realizada
   */
  public void setAccion(Action accion) {
    this.accion = accion;
  }

  /**
   * Devuelve el detalle de la acción.
   *
   * @return detalle de la acción
   */
  public String getDescripcion() {
    return descripcion;
  }

  /**
   * Cambia el detalle de la acción.
   *
   * @param descripcion detalle de la acción
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
   * Devuelve la dirección IP de origen.
   *
   * @return dirección IP de origen
   */
  public String getIp() {
    return ip;
  }

  /**
   * Cambia la dirección IP de origen.
   *
   * @param ip dirección IP de origen
   */
  public void setIp(String ip) {
    this.ip = ip;
  }

  /**
   * Devuelve la latitud aproximada.
   *
   * @return latitud aproximada
   */
  public Double getLatitud() {
    return latitud;
  }

  /**
   * Cambia la latitud aproximada.
   *
   * @param latitud latitud aproximada
   */
  public void setLatitud(Double latitud) {
    this.latitud = latitud;
  }

  /**
   * Devuelve la longitud aproximada.
   *
   * @return longitud aproximada
   */
  public Double getLongitud() {
    return longitud;
  }

  /**
   * Cambia la longitud aproximada.
   *
   * @param longitud longitud aproximada
   */
  public void setLongitud(Double longitud) {
    this.longitud = longitud;
  }

  /**
   * Devuelve la ciudad aproximada.
   *
   * @return ciudad aproximada
   */
  public String getCiudad() {
    return ciudad;
  }

  /**
   * Cambia la ciudad aproximada.
   *
   * @param ciudad ciudad aproximada
   */
  public void setCiudad(String ciudad) {
    this.ciudad = ciudad;
  }

  /**
   * Devuelve el país aproximado.
   *
   * @return país aproximado
   */
  public String getPais() {
    return pais;
  }

  /**
   * Cambia el país aproximado.
   *
   * @param pais país aproximado
   */
  public void setPais(String pais) {
    this.pais = pais;
  }

  /**
   * Devuelve el enlace a Google Maps con la ubicación.
   *
   * @return enlace a Google Maps con la ubicación
   */
  public String getMapaUrl() {
    return mapaUrl;
  }

  /**
   * Cambia el enlace a Google Maps con la ubicación.
   *
   * @param mapaUrl enlace a Google Maps con la ubicación
   */
  public void setMapaUrl(String mapaUrl) {
    this.mapaUrl = mapaUrl;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, username, accion, descripcion, fecha, ip, latitud, longitud, ciudad,
            pais, mapaUrl);
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
    AuditDTO other = (AuditDTO) obj;
    return Objects.equals(id, other.id)
        && Objects.equals(username, other.username)
        && Objects.equals(accion, other.accion)
        && Objects.equals(descripcion, other.descripcion)
        && Objects.equals(fecha, other.fecha)
        && Objects.equals(ip, other.ip)
        && Objects.equals(latitud, other.latitud)
        && Objects.equals(longitud, other.longitud)
        && Objects.equals(ciudad, other.ciudad)
        && Objects.equals(pais, other.pais)
        && Objects.equals(mapaUrl, other.mapaUrl);
  }

  /**
   * Pasa el objeto a texto para depurar. Deja por fuera contraseñas y códigos de correo, y de
   * los contenidos largos solo muestra el tamaño.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "AuditDTO [id="
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
        + ", mapaUrl="
        + mapaUrl
        + "]";
  }
}
