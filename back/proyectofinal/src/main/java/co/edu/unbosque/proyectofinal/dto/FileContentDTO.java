package co.edu.unbosque.proyectofinal.dto;

import java.util.Arrays;
import java.util.Objects;
import co.edu.unbosque.proyectofinal.model.AnalyzedFile.Category;
import java.util.ArrayList;
import java.util.List;

/**
 * Contenido de un archivo preparado para las IAs.
 * Según el tipo trae texto, imágenes o los bytes originales para los modelos que los leen directo.
 */
public class FileContentDTO {

  /** Nombre del archivo. */
  private String nombre;

  /** Ruta dentro del zip o repositorio. */
  private String ruta;

  /** Categoría detectada. */
  private Category categoria;

  /** Tipo MIME detectado. */
  private String mimeType;

  /** Tamaño en bytes. */
  private long tamanoBytes;

  /** Huella SHA-256. */
  private String hashSha256;

  /** Texto extraido o transcrito. */
  private String texto;

  /** Indica si el texto se recorto por longitud. */
  private boolean textoRecortado;

  /** Imágenes o fotogramas. */
  private List<ImagePartDTO> imagenes = new ArrayList<>();

  /** Bytes originales para PDF, audio o video. */
  private byte[] bytesOriginales;

  /** Nota sobre cómo se preparó el contenido. */
  private String nota;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public FileContentDTO() {}

  /**
   * Devuelve el nombre del archivo.
   *
   * @return nombre del archivo
   */
  public String getNombre() {
    return nombre;
  }

  /**
   * Cambia el nombre del archivo.
   *
   * @param nombre nombre del archivo
   */
  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  /**
   * Devuelve la ruta dentro del zip o repositorio.
   *
   * @return ruta dentro del zip o repositorio
   */
  public String getRuta() {
    return ruta;
  }

  /**
   * Cambia la ruta dentro del zip o repositorio.
   *
   * @param ruta ruta dentro del zip o repositorio
   */
  public void setRuta(String ruta) {
    this.ruta = ruta;
  }

  /**
   * Devuelve la categoría detectada.
   *
   * @return categoría detectada
   */
  public Category getCategoria() {
    return categoria;
  }

  /**
   * Cambia la categoría detectada.
   *
   * @param categoria categoría detectada
   */
  public void setCategoria(Category categoria) {
    this.categoria = categoria;
  }

  /**
   * Devuelve el tipo MIME detectado.
   *
   * @return tipo MIME detectado
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Cambia el tipo MIME detectado.
   *
   * @param mimeType tipo MIME detectado
   */
  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  /**
   * Devuelve el tamaño en bytes.
   *
   * @return tamaño en bytes
   */
  public long getTamanoBytes() {
    return tamanoBytes;
  }

  /**
   * Cambia el tamaño en bytes.
   *
   * @param tamanoBytes tamaño en bytes
   */
  public void setTamanoBytes(long tamanoBytes) {
    this.tamanoBytes = tamanoBytes;
  }

  /**
   * Devuelve la huella SHA-256.
   *
   * @return huella SHA-256
   */
  public String getHashSha256() {
    return hashSha256;
  }

  /**
   * Cambia la huella SHA-256.
   *
   * @param hashSha256 huella SHA-256
   */
  public void setHashSha256(String hashSha256) {
    this.hashSha256 = hashSha256;
  }

  /**
   * Devuelve el texto extraido o transcrito.
   *
   * @return texto extraido o transcrito
   */
  public String getTexto() {
    return texto;
  }

  /**
   * Cambia el texto extraido o transcrito.
   *
   * @param texto texto extraido o transcrito
   */
  public void setTexto(String texto) {
    this.texto = texto;
  }

  /**
   * Indica si el texto se recorto por longitud.
   *
   * @return indica si el texto se recorto por longitud
   */
  public boolean isTextoRecortado() {
    return textoRecortado;
  }

  /**
   * Define si el texto se recorto por longitud.
   *
   * @param textoRecortado indica si el texto se recorto por longitud
   */
  public void setTextoRecortado(boolean textoRecortado) {
    this.textoRecortado = textoRecortado;
  }

  /**
   * Devuelve las imágenes o fotogramas.
   *
   * @return imágenes o fotogramas
   */
  public List<ImagePartDTO> getImagenes() {
    return imagenes;
  }

  /**
   * Cambia las imágenes o fotogramas.
   *
   * @param imagenes imágenes o fotogramas
   */
  public void setImagenes(List<ImagePartDTO> imagenes) {
    this.imagenes = imagenes;
  }

  /**
   * Devuelve los bytes originales para PDF, audio o video.
   *
   * @return bytes originales para PDF, audio o video
   */
  public byte[] getBytesOriginales() {
    return bytesOriginales;
  }

  /**
   * Cambia los bytes originales para PDF, audio o video.
   *
   * @param bytesOriginales bytes originales para PDF, audio o video
   */
  public void setBytesOriginales(byte[] bytesOriginales) {
    this.bytesOriginales = bytesOriginales;
  }

  /**
   * Devuelve la nota sobre cómo se preparó el contenido.
   *
   * @return nota sobre cómo se preparó el contenido
   */
  public String getNota() {
    return nota;
  }

  /**
   * Cambia la nota sobre cómo se preparó el contenido.
   *
   * @param nota nota sobre cómo se preparó el contenido
   */
  public void setNota(String nota) {
    this.nota = nota;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    int result = Objects.hash(nombre, ruta, categoria, mimeType, tamanoBytes, hashSha256, texto,
            textoRecortado, imagenes, nota);
    result = 31 * result + Arrays.hashCode(bytesOriginales);
    return result;
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
    FileContentDTO other = (FileContentDTO) obj;
    return Objects.equals(nombre, other.nombre)
        && Objects.equals(ruta, other.ruta)
        && Objects.equals(categoria, other.categoria)
        && Objects.equals(mimeType, other.mimeType)
        && tamanoBytes == other.tamanoBytes
        && Objects.equals(hashSha256, other.hashSha256)
        && Objects.equals(texto, other.texto)
        && textoRecortado == other.textoRecortado
        && Objects.equals(imagenes, other.imagenes)
        && Arrays.equals(bytesOriginales, other.bytesOriginales)
        && Objects.equals(nota, other.nota);
  }

  /**
   * Pasa el objeto a texto para depurar. Deja por fuera contraseñas y códigos de correo, y de
   * los contenidos largos solo muestra el tamaño.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "FileContentDTO [nombre="
        + nombre
        + ", ruta="
        + ruta
        + ", categoria="
        + categoria
        + ", mimeType="
        + mimeType
        + ", tamanoBytes="
        + tamanoBytes
        + ", hashSha256="
        + hashSha256
        + ", texto="
        + (texto == null ? 0 : texto.length()) + " caracteres"
        + ", textoRecortado="
        + textoRecortado
        + ", imagenes="
        + imagenes
        + ", bytesOriginales="
        + (bytesOriginales == null ? 0 : bytesOriginales.length) + " bytes"
        + ", nota="
        + nota
        + "]";
  }
}
