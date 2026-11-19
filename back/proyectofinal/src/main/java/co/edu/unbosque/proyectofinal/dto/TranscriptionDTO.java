package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;

/**
 * Respuesta de la API de transcripción de Groq (Whisper). No se guarda en la base de datos.
 */
public class TranscriptionDTO {

  /** Texto transcrito. */
  private String text;

  /** Código HTTP de la respuesta. */
  private int statuscode;

  /** Cuerpo de la respuesta cuando el código no es 2xx. */
  private String cuerpoError;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public TranscriptionDTO() {}

  /**
   * Crea el objeto con todos sus campos.
   *
   * @param text texto transcrito
   * @param statuscode código HTTP de la respuesta
   * @param cuerpoError cuerpo de la respuesta cuando el código no es 2xx
   */
  public TranscriptionDTO(String text, int statuscode, String cuerpoError) {
    this.text = text;
    this.statuscode = statuscode;
    this.cuerpoError = cuerpoError;
  }

  /**
   * Devuelve el texto transcrito.
   *
   * @return texto transcrito
   */
  public String getText() {
    return text;
  }

  /**
   * Cambia el texto transcrito.
   *
   * @param text texto transcrito
   */
  public void setText(String text) {
    this.text = text;
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
    return Objects.hash(text, statuscode, cuerpoError);
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
    TranscriptionDTO other = (TranscriptionDTO) obj;
    return Objects.equals(text, other.text)
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
    return "TranscriptionDTO [text="
        + (text == null ? 0 : text.length()) + " caracteres"
        + ", statuscode="
        + statuscode
        + ", cuerpoError="
        + cuerpoError
        + "]";
  }
}
