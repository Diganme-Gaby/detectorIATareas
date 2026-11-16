package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;

/**
 * Una de las opciones que devuelve el endpoint /chat/completions. No se guarda en la base de
 * datos.
 */
public class ChatChoiceDTO {

  /** Mensaje generado. */
  private ChatMessageDTO message;

  /** Motivo por el que el modelo dejó de escribir (llega como finish_reason). */
  private String finishReason;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public ChatChoiceDTO() {}

  /**
   * Crea el objeto con todos sus campos.
   *
   * @param message mensaje generado
   * @param finishReason motivo por el que el modelo dejó de escribir (llega como finish_reason)
   */
  public ChatChoiceDTO(ChatMessageDTO message, String finishReason) {
    this.message = message;
    this.finishReason = finishReason;
  }

  /**
   * Devuelve el mensaje generado.
   *
   * @return mensaje generado
   */
  public ChatMessageDTO getMessage() {
    return message;
  }

  /**
   * Cambia el mensaje generado.
   *
   * @param message mensaje generado
   */
  public void setMessage(ChatMessageDTO message) {
    this.message = message;
  }

  /**
   * Devuelve el motivo por el que el modelo dejó de escribir (llega como finish_reason).
   *
   * @return motivo por el que el modelo dejó de escribir (llega como finish_reason)
   */
  public String getFinishReason() {
    return finishReason;
  }

  /**
   * Cambia el motivo por el que el modelo dejó de escribir (llega como finish_reason).
   *
   * @param finishReason motivo por el que el modelo dejó de escribir (llega como finish_reason)
   */
  public void setFinishReason(String finishReason) {
    this.finishReason = finishReason;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(message, finishReason);
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
    ChatChoiceDTO other = (ChatChoiceDTO) obj;
    return Objects.equals(message, other.message)
        && Objects.equals(finishReason, other.finishReason);
  }

  /**
   * Pasa el objeto a texto para depurar.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "ChatChoiceDTO [message="
        + message
        + ", finishReason="
        + finishReason
        + "]";
  }
}
