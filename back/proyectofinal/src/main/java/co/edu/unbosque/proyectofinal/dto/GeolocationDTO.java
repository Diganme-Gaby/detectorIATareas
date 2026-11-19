package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;

/**
 * Ubicación aproximada de una IP que devuelve la API de ip-api.com. Se copia a la auditoría, pero
 * el DTO como tal no se guarda en la base de datos.
 */
public class GeolocationDTO {

  /** Estado de la consulta: success o fail. */
  private String status;

  /** País. */
  private String country;

  /** Ciudad. */
  private String city;

  /** Latitud. */
  private Double lat;

  /** Longitud. */
  private Double lon;

  /** Código HTTP de la respuesta. */
  private int statuscode;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public GeolocationDTO() {}

  /**
   * Crea el objeto con todos sus campos.
   *
   * @param status estado de la consulta: success o fail
   * @param country país
   * @param city ciudad
   * @param lat latitud
   * @param lon longitud
   * @param statuscode código HTTP de la respuesta
   */
  public GeolocationDTO(String status, String country, String city, Double lat, Double lon, int statuscode) {
    this.status = status;
    this.country = country;
    this.city = city;
    this.lat = lat;
    this.lon = lon;
    this.statuscode = statuscode;
  }

  /**
   * Devuelve el estado de la consulta: success o fail.
   *
   * @return estado de la consulta: success o fail
   */
  public String getStatus() {
    return status;
  }

  /**
   * Cambia el estado de la consulta: success o fail.
   *
   * @param status estado de la consulta: success o fail
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Devuelve el país.
   *
   * @return país
   */
  public String getCountry() {
    return country;
  }

  /**
   * Cambia el país.
   *
   * @param country país
   */
  public void setCountry(String country) {
    this.country = country;
  }

  /**
   * Devuelve la ciudad.
   *
   * @return ciudad
   */
  public String getCity() {
    return city;
  }

  /**
   * Cambia la ciudad.
   *
   * @param city ciudad
   */
  public void setCity(String city) {
    this.city = city;
  }

  /**
   * Devuelve la latitud.
   *
   * @return latitud
   */
  public Double getLat() {
    return lat;
  }

  /**
   * Cambia la latitud.
   *
   * @param lat latitud
   */
  public void setLat(Double lat) {
    this.lat = lat;
  }

  /**
   * Devuelve la longitud.
   *
   * @return longitud
   */
  public Double getLon() {
    return lon;
  }

  /**
   * Cambia la longitud.
   *
   * @param lon longitud
   */
  public void setLon(Double lon) {
    this.lon = lon;
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
    return Objects.hash(status, country, city, lat, lon, statuscode);
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
    GeolocationDTO other = (GeolocationDTO) obj;
    return Objects.equals(status, other.status)
        && Objects.equals(country, other.country)
        && Objects.equals(city, other.city)
        && Objects.equals(lat, other.lat)
        && Objects.equals(lon, other.lon)
        && Objects.equals(statuscode, other.statuscode);
  }

  /**
   * Pasa el objeto a texto para depurar.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "GeolocationDTO [status="
        + status
        + ", country="
        + country
        + ", city="
        + city
        + ", lat="
        + lat
        + ", lon="
        + lon
        + ", statuscode="
        + statuscode
        + "]";
  }
}
