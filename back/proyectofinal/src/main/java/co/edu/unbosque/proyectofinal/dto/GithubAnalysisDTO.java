package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Solicitud para analizar un repositorio público de GitHub.
 * Permite escoger qué archivos se analizan.
 */
public class GithubAnalysisDTO {

  /** URL del repositorio. */
  private String url;

  /** Rama a descargar; si va vacía se usa la rama por defecto. */
  private String rama;

  /** Carpeta del repositorio a la que se limita el análisis. */
  private String carpeta;

  /** Extensiones permitidas sin punto. */
  private List<String> extensiones = new ArrayList<>();

  /** Máximo de archivos a analizar. */
  private Integer maxArchivos;

  /** Título del análisis. */
  private String titulo;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public GithubAnalysisDTO() {}

  /**
   * Devuelve la URL del repositorio.
   *
   * @return URL del repositorio
   */
  public String getUrl() {
    return url;
  }

  /**
   * Cambia la URL del repositorio.
   *
   * @param url URL del repositorio
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Devuelve la rama a descargar; si va vacía se usa la rama por defecto.
   *
   * @return rama a descargar; si va vacía se usa la rama por defecto
   */
  public String getRama() {
    return rama;
  }

  /**
   * Cambia la rama a descargar; si va vacía se usa la rama por defecto.
   *
   * @param rama rama a descargar; si va vacía se usa la rama por defecto
   */
  public void setRama(String rama) {
    this.rama = rama;
  }

  /**
   * Devuelve la carpeta del repositorio a la que se limita el análisis.
   *
   * @return carpeta del repositorio a la que se limita el análisis
   */
  public String getCarpeta() {
    return carpeta;
  }

  /**
   * Cambia la carpeta del repositorio a la que se limita el análisis.
   *
   * @param carpeta carpeta del repositorio a la que se limita el análisis
   */
  public void setCarpeta(String carpeta) {
    this.carpeta = carpeta;
  }

  /**
   * Devuelve las extensiones permitidas sin punto.
   *
   * @return extensiones permitidas sin punto
   */
  public List<String> getExtensiones() {
    return extensiones;
  }

  /**
   * Cambia las extensiones permitidas sin punto.
   *
   * @param extensiones extensiones permitidas sin punto
   */
  public void setExtensiones(List<String> extensiones) {
    this.extensiones = extensiones;
  }

  /**
   * Devuelve el máximo de archivos a analizar.
   *
   * @return máximo de archivos a analizar
   */
  public Integer getMaxArchivos() {
    return maxArchivos;
  }

  /**
   * Cambia el máximo de archivos a analizar.
   *
   * @param maxArchivos máximo de archivos a analizar
   */
  public void setMaxArchivos(Integer maxArchivos) {
    this.maxArchivos = maxArchivos;
  }

  /**
   * Devuelve el título del análisis.
   *
   * @return título del análisis
   */
  public String getTitulo() {
    return titulo;
  }

  /**
   * Cambia el título del análisis.
   *
   * @param titulo título del análisis
   */
  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(url, rama, carpeta, extensiones, maxArchivos, titulo);
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
    GithubAnalysisDTO other = (GithubAnalysisDTO) obj;
    return Objects.equals(url, other.url)
        && Objects.equals(rama, other.rama)
        && Objects.equals(carpeta, other.carpeta)
        && Objects.equals(extensiones, other.extensiones)
        && Objects.equals(maxArchivos, other.maxArchivos)
        && Objects.equals(titulo, other.titulo);
  }

  /**
   * Pasa el objeto a texto para depurar. Deja por fuera contraseñas y códigos de correo, y de
   * los contenidos largos solo muestra el tamaño.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "GithubAnalysisDTO [url="
        + url
        + ", rama="
        + rama
        + ", carpeta="
        + carpeta
        + ", extensiones="
        + extensiones
        + ", maxArchivos="
        + maxArchivos
        + ", titulo="
        + titulo
        + "]";
  }
}
