package co.edu.unbosque.proyectofinal.dto;

import java.util.List;
import java.util.Objects;

/**
 * Contenido de una respuesta candidata de Gemini. No se guarda en la base de datos.
 */
public class GeminiContentDTO {

  /** Partes del contenido. */
  private List<GeminiPartDTO> parts;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public GeminiContentDTO() {}

  /**
   * Crea el objeto con todos sus campos.
   *
   * @param parts partes del contenido
   */
  public GeminiContentDTO(List<GeminiPartDTO> parts) {
    this.parts = parts;
  }

  /**
   * Devuelve las partes del contenido.
   *
   * @return partes del contenido
   */
  public List<GeminiPartDTO> getParts() {
    return parts;
  }

  /**
   * Cambia las partes del contenido.
   *
   * @param parts partes del contenido
   */
  public void setParts(List<GeminiPartDTO> parts) {
    this.parts = parts;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(parts);
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
    GeminiContentDTO other = (GeminiContentDTO) obj;
    return Objects.equals(parts, other.parts);
  }

  /**
   * Pasa el objeto a texto para depurar.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "GeminiContentDTO [parts="
        + parts
        + "]";
  }
}
