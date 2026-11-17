package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;

/**
 * Imagen en base64 que se manda a las IAs con visión.
 */
public class ImagePartDTO {

  /** Tipo MIME de la imagen. */
  private String mimeType;

  /** Contenido en base64 sin prefijo. */
  private String base64;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public ImagePartDTO() {}

  /**
   * Crea el objeto con los datos principales.
   *
   * @param mimeType tipo MIME de la imagen
   * @param base64 contenido en base64 sin prefijo
   */
  public ImagePartDTO(String mimeType, String base64) {
    this.mimeType = mimeType;
    this.base64 = base64;
  }

  /**
   * Devuelve el tipo MIME de la imagen.
   *
   * @return tipo MIME de la imagen
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Cambia el tipo MIME de la imagen.
   *
   * @param mimeType tipo MIME de la imagen
   */
  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  /**
   * Devuelve el contenido en base64 sin prefijo.
   *
   * @return contenido en base64 sin prefijo
   */
  public String getBase64() {
    return base64;
  }

  /**
   * Cambia el contenido en base64 sin prefijo.
   *
   * @param base64 contenido en base64 sin prefijo
   */
  public void setBase64(String base64) {
    this.base64 = base64;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(mimeType, base64);
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
    ImagePartDTO other = (ImagePartDTO) obj;
    return Objects.equals(mimeType, other.mimeType)
        && Objects.equals(base64, other.base64);
  }

  /**
   * Pasa el objeto a texto para depurar. Deja por fuera contraseñas y códigos de correo, y de
   * los contenidos largos solo muestra el tamaño.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "ImagePartDTO [mimeType="
        + mimeType
        + ", base64="
        + (base64 == null ? 0 : base64.length()) + " caracteres"
        + "]";
  }
}
