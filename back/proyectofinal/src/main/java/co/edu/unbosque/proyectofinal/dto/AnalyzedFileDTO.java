package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;
import co.edu.unbosque.proyectofinal.model.AnalyzedFile.Category;
import java.util.ArrayList;
import java.util.List;

/**
 * Archivo analizado con el resultado de cada IA.
 */
public class AnalyzedFileDTO {

  /** Identificador. */
  private Long id;

  /** Nombre del archivo. */
  private String nombreArchivo;

  /** Ruta dentro del zip o repositorio. */
  private String ruta;

  /** Categoría detectada. */
  private Category categoria;

  /** Tipo MIME. */
  private String mimeType;

  /** Tamaño en bytes. */
  private long tamanoBytes;

  /** Promedio de las IAs que respondieron. */
  private Double promedioIA;

  /** Nivel ALTO, MEDIO, BAJO o SIN_RESULTADO. */
  private String nivel;

  /** Resultado de cada IA. */
  private List<ModelResultDTO> resultados = new ArrayList<>();

  /** Avisos para el usuario sobre IAs que no respondieron. */
  private List<String> avisos = new ArrayList<>();

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public AnalyzedFileDTO() {}

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
   * Devuelve el promedio de las IAs que respondieron.
   *
   * @return promedio de las IAs que respondieron
   */
  public Double getPromedioIA() {
    return promedioIA;
  }

  /**
   * Cambia el promedio de las IAs que respondieron.
   *
   * @param promedioIA promedio de las IAs que respondieron
   */
  public void setPromedioIA(Double promedioIA) {
    this.promedioIA = promedioIA;
  }

  /**
   * Devuelve el nivel ALTO, MEDIO, BAJO o SIN_RESULTADO.
   *
   * @return nivel ALTO, MEDIO, BAJO o SIN_RESULTADO
   */
  public String getNivel() {
    return nivel;
  }

  /**
   * Cambia el nivel ALTO, MEDIO, BAJO o SIN_RESULTADO.
   *
   * @param nivel nivel ALTO, MEDIO, BAJO o SIN_RESULTADO
   */
  public void setNivel(String nivel) {
    this.nivel = nivel;
  }

  /**
   * Devuelve el resultado de cada IA.
   *
   * @return resultado de cada IA
   */
  public List<ModelResultDTO> getResultados() {
    return resultados;
  }

  /**
   * Cambia el resultado de cada IA.
   *
   * @param resultados resultado de cada IA
   */
  public void setResultados(List<ModelResultDTO> resultados) {
    this.resultados = resultados;
  }

  /**
   * Devuelve los avisos para el usuario sobre IAs que no respondieron.
   *
   * @return avisos para el usuario sobre IAs que no respondieron
   */
  public List<String> getAvisos() {
    return avisos;
  }

  /**
   * Cambia los avisos para el usuario sobre IAs que no respondieron.
   *
   * @param avisos avisos para el usuario sobre IAs que no respondieron
   */
  public void setAvisos(List<String> avisos) {
    this.avisos = avisos;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, nombreArchivo, ruta, categoria, mimeType, tamanoBytes, promedioIA,
            nivel, avisos);
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
    AnalyzedFileDTO other = (AnalyzedFileDTO) obj;
    return Objects.equals(id, other.id)
        && Objects.equals(nombreArchivo, other.nombreArchivo)
        && Objects.equals(ruta, other.ruta)
        && Objects.equals(categoria, other.categoria)
        && Objects.equals(mimeType, other.mimeType)
        && tamanoBytes == other.tamanoBytes
        && Objects.equals(promedioIA, other.promedioIA)
        && Objects.equals(nivel, other.nivel)
        && Objects.equals(avisos, other.avisos);
  }

  /**
   * Pasa el objeto a texto para depurar. Deja por fuera contraseñas y códigos de correo, y de
   * los contenidos largos solo muestra el tamaño.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "AnalyzedFileDTO [id="
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
        + ", promedioIA="
        + promedioIA
        + ", nivel="
        + nivel
        + ", avisos="
        + avisos
        + "]";
  }
}
