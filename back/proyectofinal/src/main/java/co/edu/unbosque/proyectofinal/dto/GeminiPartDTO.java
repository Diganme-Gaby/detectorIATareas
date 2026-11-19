package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;

/**
 * Parte de un contenido de Gemini. Los modelos que piensan devuelven partes con thought=true que
 * no hacen parte de la respuesta final. No se guarda en la base de datos.
 */
public class GeminiPartDTO {

  /** Texto de la parte. */
  private String text;

  /** True si la parte es razonamiento interno del modelo. */
  private Boolean thought;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public GeminiPartDTO() {}

  /**
   * Crea el objeto con todos sus campos.
   *
   * @param text texto de la parte
   * @param thought true si la parte es razonamiento interno del modelo
   */
  public GeminiPartDTO(String text, Boolean thought) {
    this.text = text;
    this.thought = thought;
  }

  /**
   * Devuelve el texto de la parte.
   *
   * @return texto de la parte
   */
  public String getText() {
    return text;
  }

  /**
   * Cambia el texto de la parte.
   *
   * @param text texto de la parte
   */
  public void setText(String text) {
    this.text = text;
  }

  /**
   * Devuelve true si la parte es razonamiento interno del modelo.
   *
   * @return true si la parte es razonamiento interno del modelo
   */
  public Boolean getThought() {
    return thought;
  }

  /**
   * Define si la parte es razonamiento interno del modelo.
   *
   * @param thought true si la parte es razonamiento interno del modelo
   */
  public void setThought(Boolean thought) {
    this.thought = thought;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(text, thought);
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
    GeminiPartDTO other = (GeminiPartDTO) obj;
    return Objects.equals(text, other.text)
        && Objects.equals(thought, other.thought);
  }

  /**
   * Pasa el objeto a texto para depurar.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "GeminiPartDTO [text="
        + text
        + ", thought="
        + thought
        + "]";
  }
}
