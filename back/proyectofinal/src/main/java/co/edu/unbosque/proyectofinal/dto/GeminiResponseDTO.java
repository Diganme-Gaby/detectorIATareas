package co.edu.unbosque.proyectofinal.dto;

import java.util.List;
import java.util.Objects;

/**
 * Respuesta del endpoint generateContent de Gemini. Gson llena los candidatos; el código HTTP y
 * el cuerpo de error los agrega {@code ExternalHTTPRequestHandler}. No se guarda en la base de
 * datos.
 */
public class GeminiResponseDTO {

  /** Respuestas candidatas; se usa la primera. */
  private List<GeminiCandidateDTO> candidates;

  /** Código HTTP de la respuesta. */
  private int statuscode;

  /** Cuerpo de la respuesta cuando el código no es 2xx. */
  private String cuerpoError;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public GeminiResponseDTO() {}

  /**
   * Crea el objeto con todos sus campos.
   *
   * @param candidates respuestas candidatas; se usa la primera
   * @param statuscode código HTTP de la respuesta
   * @param cuerpoError cuerpo de la respuesta cuando el código no es 2xx
   */
  public GeminiResponseDTO(List<GeminiCandidateDTO> candidates, int statuscode, String cuerpoError) {
    this.candidates = candidates;
    this.statuscode = statuscode;
    this.cuerpoError = cuerpoError;
  }

  /**
   * Devuelve las respuestas candidatas; se usa la primera.
   *
   * @return respuestas candidatas; se usa la primera
   */
  public List<GeminiCandidateDTO> getCandidates() {
    return candidates;
  }

  /**
   * Cambia las respuestas candidatas; se usa la primera.
   *
   * @param candidates respuestas candidatas; se usa la primera
   */
  public void setCandidates(List<GeminiCandidateDTO> candidates) {
    this.candidates = candidates;
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
   * Devuelve el cuerpo de la respuesta cuando el código no es 2xx.
   *
   * @return cuerpo de la respuesta cuando el código no es 2xx
   */
  public String getCuerpoError() {
    return cuerpoError;
  }

  /**
   * Cambia el cuerpo de la respuesta cuando el código no es 2xx.
   *
   * @param cuerpoError cuerpo de la respuesta cuando el código no es 2xx
   */
  public void setCuerpoError(String cuerpoError) {
    this.cuerpoError = cuerpoError;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(candidates, statuscode, cuerpoError);
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
    GeminiResponseDTO other = (GeminiResponseDTO) obj;
    return Objects.equals(candidates, other.candidates)
        && Objects.equals(statuscode, other.statuscode)
        && Objects.equals(cuerpoError, other.cuerpoError);
  }

  /**
   * Pasa el objeto a texto para depurar.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "GeminiResponseDTO [candidates="
        + candidates
        + ", statuscode="
        + statuscode
        + ", cuerpoError="
        + cuerpoError
        + "]";
  }
}
