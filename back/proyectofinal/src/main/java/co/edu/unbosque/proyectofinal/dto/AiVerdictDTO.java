package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Respuesta ya interpretada de una IA.
 */
public class AiVerdictDTO {

  /** Probabilidad de IA (0 a 100). */
  private double porcentaje;

  /** Veredicto corto. */
  private String veredicto;

  /** Motivos que dio la IA. */
  private List<String> motivos = new ArrayList<>();

  /** Modelo que respondió. */
  private String modelo;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public AiVerdictDTO() {}

  /**
   * Devuelve la probabilidad de IA (0 a 100).
   *
   * @return probabilidad de IA (0 a 100)
   */
  public double getPorcentaje() {
    return porcentaje;
  }

  /**
   * Cambia la probabilidad de IA (0 a 100).
   *
   * @param porcentaje probabilidad de IA (0 a 100)
   */
  public void setPorcentaje(double porcentaje) {
    this.porcentaje = porcentaje;
  }

  /**
   * Devuelve el veredicto corto.
   *
   * @return veredicto corto
   */
  public String getVeredicto() {
    return veredicto;
  }

  /**
   * Cambia el veredicto corto.
   *
   * @param veredicto veredicto corto
   */
  public void setVeredicto(String veredicto) {
    this.veredicto = veredicto;
  }

  /**
   * Devuelve los motivos que dio la IA.
   *
   * @return motivos que dio la IA
   */
  public List<String> getMotivos() {
    return motivos;
  }

  /**
   * Cambia los motivos que dio la IA.
   *
   * @param motivos motivos que dio la IA
   */
  public void setMotivos(List<String> motivos) {
    this.motivos = motivos;
  }

  /**
   * Devuelve el modelo que respondió.
   *
   * @return modelo que respondió
   */
  public String getModelo() {
    return modelo;
  }

  /**
   * Cambia el modelo que respondió.
   *
   * @param modelo modelo que respondió
   */
  public void setModelo(String modelo) {
    this.modelo = modelo;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(porcentaje, veredicto, motivos, modelo);
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
    AiVerdictDTO other = (AiVerdictDTO) obj;
    return Double.compare(porcentaje, other.porcentaje) == 0
        && Objects.equals(veredicto, other.veredicto)
        && Objects.equals(motivos, other.motivos)
        && Objects.equals(modelo, other.modelo);
  }

  /**
   * Pasa el objeto a texto para depurar. Deja por fuera contraseñas y códigos de correo, y de
   * los contenidos largos solo muestra el tamaño.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "AiVerdictDTO [porcentaje="
        + porcentaje
        + ", veredicto="
        + veredicto
        + ", motivos="
        + motivos
        + ", modelo="
        + modelo
        + "]";
  }
}
