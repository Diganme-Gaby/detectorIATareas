package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;

/**
 * Respuesta candidata de Gemini. No se guarda en la base de datos.
 */
public class GeminiCandidateDTO {

  /** Contenido generado. */
  private GeminiContentDTO content;

  /** Motivo por el que el modelo dejó de escribir. */
  private String finishReason;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public GeminiCandidateDTO() {}

  /**
   * Crea el objeto con todos sus campos.
   *
   * @param content contenido generado
   * @param finishReason motivo por el que el modelo dejó de escribir
   */
  public GeminiCandidateDTO(GeminiContentDTO content, String finishReason) {
    this.content = content;
    this.finishReason = finishReason;
  }

  /**
   * Devuelve el contenido generado.
   *
   * @return contenido generado
   */
  public GeminiContentDTO getContent() {
    return content;
  }

  /**
   * Cambia el contenido generado.
   *
   * @param content contenido generado
   */
  public void setContent(GeminiContentDTO content) {
    this.content = content;
  }

  /**
   * Devuelve el motivo por el que el modelo dejó de escribir.
   *
   * @return motivo por el que el modelo dejó de escribir
   */
  public String getFinishReason() {
    return finishReason;
  }

  /**
   * Cambia el motivo por el que el modelo dejó de escribir.
   *
   * @param finishReason motivo por el que el modelo dejó de escribir
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
    return Objects.hash(content, finishReason);
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
    GeminiCandidateDTO other = (GeminiCandidateDTO) obj;
    return Objects.equals(content, other.content)
        && Objects.equals(finishReason, other.finishReason);
  }

  /**
   * Pasa el objeto a texto para depurar.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "GeminiCandidateDTO [content="
        + content
        + ", finishReason="
        + finishReason
        + "]";
  }
}
