package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;

/**
 * IP pública del servidor que devuelve la API de ipify. No se guarda en la base de datos.
 */
public class PublicIpDTO {

  /** IP pública. */
  private String ip;

  /** Código HTTP de la respuesta. */
  private int statuscode;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public PublicIpDTO() {}

  /**
   * Crea el objeto con todos sus campos.
   *
   * @param ip IP pública
   * @param statuscode código HTTP de la respuesta
   */
  public PublicIpDTO(String ip, int statuscode) {
    this.ip = ip;
    this.statuscode = statuscode;
  }

  /**
   * Devuelve la IP pública.
   *
   * @return IP pública
   */
  public String getIp() {
    return ip;
  }

  /**
   * Cambia la IP pública.
   *
   * @param ip IP pública
   */
  public void setIp(String ip) {
    this.ip = ip;
  }

  /**
   * Devuelve el código HTTP de la respuesta.
   *
   * @return código HTTP de la respuesta
   */
  public int getStatuscode() {
    return statuscode;
  }

  /**
   * Cambia el código HTTP de la respuesta.
   *
   * @param statuscode código HTTP de la respuesta
   */
  public void setStatuscode(int statuscode) {
    this.statuscode = statuscode;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(ip, statuscode);
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
    PublicIpDTO other = (PublicIpDTO) obj;
    return Objects.equals(ip, other.ip)
        && Objects.equals(statuscode, other.statuscode);
  }

  /**
   * Pasa el objeto a texto para depurar.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "PublicIpDTO [ip="
        + ip
        + ", statuscode="
        + statuscode
        + "]";
  }
}
