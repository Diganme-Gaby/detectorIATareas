package co.edu.unbosque.proyectofinal.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Un archivo dentro de un análisis. Guarda el id del análisis al que pertenece; lo que dijo cada IA
 * se guarda en {@link ModelResult} con el id de este archivo. Cada archivo tiene su propio reporte
 * PDF.
 */
@Entity
@Table(name = "archivo_analizado")
public class AnalyzedFile {

  /** Identificador generado por la base de datos. */
  private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

  /** Id del análisis al que pertenece. */
  @Column(nullable = false)
  private Long analysisId;

  /** Nombre del archivo. */
  private String nombreArchivo;

  /** Ruta dentro del zip o del repositorio. Igual al nombre si se subió suelto. */
  @Column(length = 700)
  private String ruta;

  /** Categoría detectada. */
  @Enumerated(EnumType.STRING)
  private Category categoria;

  /** Tipo MIME detectado. */
  private String mimeType;

  /** Tamaño en bytes. */
  private long tamanoBytes;

  /** Huella SHA-256 del contenido. Sirve para reutilizar resultados. */
  @Column(length = 64)
  private String hashSha256;

  /** Promedio de las IAs que respondieron. Nulo si ninguna respondió. */
  @Column(nullable = true)
  private Double promedioIA;

  /** ALTO, MEDIO, BAJO o SIN_RESULTADO según el promedio. */
  @Column(length = 20)
  private String nivel;

  /** Categorías de archivo que entiende el analizador. */
  public enum Category {
    /** Texto plano, markdown, csv. */
    TEXTO,
    /** Código fuente o configuración. */
    CODIGO,
    /** Documento PDF. */
    PDF,
    /** Word, PowerPoint, Excel, OpenDocument, RTF, EPUB. */
    DOCUMENTO,
    /** Imagen. */
    IMAGEN,
    /** Audio. */
    AUDIO,
    /** Video. */
    VIDEO,
    /** Cualquier otro binario. */
    OTRO
  }

  /** Constructor vacío que exige JPA. */
  public AnalyzedFile() {
    this.nivel = "SIN_RESULTADO";
  }

  /**
   * Devuelve el identificador.
   *
   * @return identificador
   */
  public Long getId() {
    return id;
  }

  /**
   * Cambia el identificador.
   *
   * @param id identificador
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Devuelve el id del análisis al que pertenece.
   *
   * @return id del análisis al que pertenece
   */
  public Long getAnalysisId() {
    return analysisId;
  }

  /**
   * Cambia el id del análisis al que pertenece.
   *
   * @param analysisId id del análisis al que pertenece
   */
  public void setAnalysisId(Long analysisId) {
    this.analysisId = analysisId;
  }

  /**
   * Devuelve el nombre del archivo.
   *
   * @return nombre del archivo
   */
  public String getNombreArchivo() {
    return nombreArchivo;
  }

  /**
   * Cambia el nombre del archivo.
   *
   * @param nombreArchivo nombre del archivo
   */
  public void setNombreArchivo(String nombreArchivo) {
    this.nombreArchivo = nombreArchivo;
  }

  /**
   * Devuelve la ruta.
   *
   * @return ruta
   */
  public String getRuta() {
    return ruta;
  }

  /**
   * Cambia la ruta.
   *
   * @param ruta ruta
   */
  public void setRuta(String ruta) {
    this.ruta = ruta;
  }

  /**
   * Devuelve la categoría.
   *
   * @return categoría
   */
  public Category getCategoria() {
    return categoria;
  }

  /**
   * Cambia la categoría.
   *
   * @param categoria categoría
   */
  public void setCategoria(Category categoria) {
    this.categoria = categoria;
  }

  /**
   * Devuelve el tipo MIME.
   *
   * @return tipo MIME
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Cambia el tipo MIME.
   *
   * @param mimeType tipo MIME
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
   * Devuelve el hash SHA-256.
   *
   * @return hash SHA-256
   */
  public String getHashSha256() {
    return hashSha256;
  }

  /**
   * Cambia el hash SHA-256.
   *
   * @param hashSha256 hash SHA-256
   */
  public void setHashSha256(String hashSha256) {
    this.hashSha256 = hashSha256;
  }

  /**
   * Devuelve el promedio de IA.
   *
   * @return promedio de IA
   */
  public Double getPromedioIA() {
    return promedioIA;
  }

  /**
   * Cambia el promedio de IA.
   *
   * @param promedioIA promedio de IA
   */
  public void setPromedioIA(Double promedioIA) {
    this.promedioIA = promedioIA;
  }

  /**
   * Devuelve el nivel.
   *
   * @return nivel
   */
  public String getNivel() {
    return nivel;
  }

  /**
   * Cambia el nivel.
   *
   * @param nivel nivel
   */
  public void setNivel(String nivel) {
    this.nivel = nivel;
  }


  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, nombreArchivo, ruta);
  }

  /**
   * Dos registros son iguales si coinciden id, nombreArchivo, ruta.
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
    AnalyzedFile other = (AnalyzedFile) obj;
    return Objects.equals(id, other.id)
        && Objects.equals(nombreArchivo, other.nombreArchivo)
        && Objects.equals(ruta, other.ruta);
  }

  /**
   * Pasa el objeto a texto para depurar. Deja por fuera contraseñas y códigos de correo, y de
   * los contenidos largos solo muestra el tamaño.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "AnalyzedFile [id="
        + id
        + ", nombreArchivo="
        + nombreArchivo
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
        + ", promedioIA="
        + promedioIA
        + ", nivel="
        + nivel
        + "]";
  }
}
