package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;

/**
 * Gráfica exportada por Chart.js en el front (toBase64Image) para incluirla en el PDF.
 */
public class ChartImageDTO {

  /** Título que se pone encima de la gráfica. */
  private String titulo;

  /** Imagen PNG en base64, con o sin el prefijo data:image/png;base64. */
  private String imagenBase64;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public ChartImageDTO() {}

  /**
   * Crea el objeto con los datos principales.
   *
   * @param titulo título que se pone encima de la gráfica
   * @param imagenBase64 imagen PNG en base64, con o sin el prefijo data:image/png;base64
   */
  public ChartImageDTO(String titulo, String imagenBase64) {
    this.titulo = titulo;
    this.imagenBase64 = imagenBase64;
  }

  /**
   * Devuelve el título que se pone encima de la gráfica.
   *
   * @return título que se pone encima de la gráfica
   */
  public String getTitulo() {
    return titulo;
  }

  /**
   * Cambia el título que se pone encima de la gráfica.
   *
   * @param titulo título que se pone encima de la gráfica
   */
  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  /**
   * Devuelve la imagen PNG en base64, con o sin el prefijo data:image/png;base64.
   *
   * @return imagen PNG en base64, con o sin el prefijo data:image/png;base64
   */
  public String getImagenBase64() {
    return imagenBase64;
  }

  /**
   * Cambia la imagen PNG en base64, con o sin el prefijo data:image/png;base64.
   *
   * @param imagenBase64 imagen PNG en base64, con o sin el prefijo data:image/png;base64
   */
  public void setImagenBase64(String imagenBase64) {
    this.imagenBase64 = imagenBase64;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(titulo, imagenBase64);
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
    ChartImageDTO other = (ChartImageDTO) obj;
    return Objects.equals(titulo, other.titulo)
        && Objects.equals(imagenBase64, other.imagenBase64);
  }

  /**
   * Pasa el objeto a texto para depurar. Deja por fuera contraseñas y códigos de correo, y de
   * los contenidos largos solo muestra el tamaño.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "ChartImageDTO [titulo="
        + titulo
        + ", imagenBase64="
        + (imagenBase64 == null ? 0 : imagenBase64.length()) + " caracteres"
        + "]";
  }
}
