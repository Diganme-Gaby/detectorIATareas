package co.edu.unbosque.proyectofinal.dto;

import java.util.List;
import java.util.Objects;

/**
 * Respuesta del endpoint /chat/completions de los proveedores con formato de OpenAI. Gson llena
 * los campos que vienen en el JSON; el código HTTP, el cuerpo de error y el encabezado
 * Retry-After los agrega {@code ExternalHTTPRequestHandler}. No se guarda en la base de datos.
 */
public class ChatCompletionDTO {

  /** Opciones generadas; se usa la primera. */
  private List<ChatChoiceDTO> choices;

  /** Código HTTP de la respuesta. */
  private int statuscode;

  /** Cuerpo de la respuesta cuando el código no es 2xx. */
  private String cuerpoError;

  /** Valor del encabezado Retry-After en segundos, o null si no vino. */
  private String reintentarDespues;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public ChatCompletionDTO() {}

  /**
   * Crea el objeto con todos sus campos.
   *
   * @param choices opciones generadas; se usa la primera
   * @param statuscode código HTTP de la respuesta
   * @param cuerpoError cuerpo de la respuesta cuando el código no es 2xx
   * @param reintentarDespues valor del encabezado Retry-After en segundos, o null si no vino
   */
  public ChatCompletionDTO(List<ChatChoiceDTO> choices, int statuscode, String cuerpoError, String reintentarDespues) {
    this.choices = choices;
    this.statuscode = statuscode;
    this.cuerpoError = cuerpoError;
    this.reintentarDespues = reintentarDespues;
  }

  /**
   * Devuelve las opciones generadas; se usa la primera.
   *
   * @return opciones generadas; se usa la primera
   */
  public List<ChatChoiceDTO> getChoices() {
    return choices;
  }

  /**
   * Cambia las opciones generadas; se usa la primera.
   *
   * @param choices opciones generadas; se usa la primera
   */
  public void setChoices(List<ChatChoiceDTO> choices) {
    this.choices = choices;
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
   * Devuelve el valor del encabezado Retry-After en segundos, o null si no vino.
   *
   * @return valor del encabezado Retry-After en segundos, o null si no vino
   */
  public String getReintentarDespues() {
    return reintentarDespues;
  }

  /**
   * Cambia el valor del encabezado Retry-After en segundos, o null si no vino.
   *
   * @param reintentarDespues valor del encabezado Retry-After en segundos, o null si no vino
   */
  public void setReintentarDespues(String reintentarDespues) {
    this.reintentarDespues = reintentarDespues;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(choices, statuscode, cuerpoError, reintentarDespues);
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
    ChatCompletionDTO other = (ChatCompletionDTO) obj;
    return Objects.equals(choices, other.choices)
        && Objects.equals(statuscode, other.statuscode)
        && Objects.equals(cuerpoError, other.cuerpoError)
        && Objects.equals(reintentarDespues, other.reintentarDespues);
  }

  /**
   * Pasa el objeto a texto para depurar.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "ChatCompletionDTO [choices="
        + choices
        + ", statuscode="
        + statuscode
        + ", cuerpoError="
        + cuerpoError
        + ", reintentarDespues="
        + reintentarDespues
        + "]";
  }
}
