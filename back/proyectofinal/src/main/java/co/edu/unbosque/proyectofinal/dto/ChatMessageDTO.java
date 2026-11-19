package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;

/**
 * Mensaje que devuelven las APIs con formato de OpenAI (Groq, OpenRouter, NVIDIA, Cohere y
 * GitHub Models) dentro de cada opción de la respuesta. No se guarda en la base de datos.
 */
public class ChatMessageDTO {

  /** Texto generado por el modelo. */
  private String content;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public ChatMessageDTO() {}

  /**
   * Crea el objeto con todos sus campos.
   *
   * @param content texto generado por el modelo
   */
  public ChatMessageDTO(String content) {
    this.content = content;
  }

  /**
   * Devuelve el texto generado por el modelo.
   *
   * @return texto generado por el modelo
   */
  public String getContent() {
    return content;
  }

  /**
   * Cambia el texto generado por el modelo.
   *
   * @param content texto generado por el modelo
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(content);
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
    ChatMessageDTO other = (ChatMessageDTO) obj;
    return Objects.equals(content, other.content);
  }

  /**
   * Pasa el objeto a texto para depurar.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "ChatMessageDTO [content="
        + content
        + "]";
  }
}
