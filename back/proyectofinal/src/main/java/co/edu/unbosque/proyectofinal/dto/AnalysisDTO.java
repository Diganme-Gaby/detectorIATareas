package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;
import co.edu.unbosque.proyectofinal.model.Analysis.Source;
import co.edu.unbosque.proyectofinal.model.Analysis.Status;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Análisis con su estado y, en el detalle, sus archivos.
 */
public class AnalysisDTO {

  /** Identificador. */
  private Long id;

  /** Dueño del análisis. */
  private String username;

  /** Título. */
  private String titulo;

  /** Origen de los archivos. */
  private Source origen;

  /** URL del repositorio si aplica. */
  private String urlRepositorio;

  /** Estado del procesamiento. */
  private Status estado;

  /** Fecha de envío. */
  private LocalDateTime fechaCreacion;

  /** Fecha de finalización. */
  private LocalDateTime fechaFinalizacion;

  /** Promedio general de IA. */
  private Double promedioIA;

  /** Nivel general ALTO, MEDIO, BAJO o SIN_RESULTADO. */
  private String nivel;

  /** Cantidad de archivos. */
  private int totalArchivos;

  /** Indica si el reporte ya salio por correo. */
  private boolean correoEnviado;

  /** Mensaje para el usuario. */
  private String mensaje;

  /** Archivos analizados. */
  private List<AnalyzedFileDTO> archivos = new ArrayList<>();

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public AnalysisDTO() {}

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
   * Devuelve el dueño del análisis.
   *
   * @return dueño del análisis
   */
  public String getUsername() {
    return username;
  }

  /**
   * Cambia el dueño del análisis.
   *
   * @param username dueño del análisis
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Devuelve el título.
   *
   * @return título
   */
  public String getTitulo() {
    return titulo;
  }

  /**
   * Cambia el título.
   *
   * @param titulo título
   */
  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  /**
   * Devuelve el origen de los archivos.
   *
   * @return origen de los archivos
   */
  public Source getOrigen() {
    return origen;
  }

  /**
   * Cambia el origen de los archivos.
   *
   * @param origen origen de los archivos
   */
  public void setOrigen(Source origen) {
    this.origen = origen;
  }

  /**
   * Devuelve la URL del repositorio si aplica.
   *
   * @return URL del repositorio si aplica
   */
  public String getUrlRepositorio() {
    return urlRepositorio;
  }

  /**
   * Cambia la URL del repositorio si aplica.
   *
   * @param urlRepositorio URL del repositorio si aplica
   */
  public void setUrlRepositorio(String urlRepositorio) {
    this.urlRepositorio = urlRepositorio;
  }

  /**
   * Devuelve el estado del procesamiento.
   *
   * @return estado del procesamiento
   */
  public Status getEstado() {
    return estado;
  }

  /**
   * Cambia el estado del procesamiento.
   *
   * @param estado estado del procesamiento
   */
  public void setEstado(Status estado) {
    this.estado = estado;
  }

  /**
   * Devuelve la fecha de envío.
   *
   * @return fecha de envío
   */
  public LocalDateTime getFechaCreacion() {
    return fechaCreacion;
  }

  /**
   * Cambia la fecha de envío.
   *
   * @param fechaCreacion fecha de envío
   */
  public void setFechaCreacion(LocalDateTime fechaCreacion) {
    this.fechaCreacion = fechaCreacion;
  }

  /**
   * Devuelve la fecha de finalización.
   *
   * @return fecha de finalización
   */
  public LocalDateTime getFechaFinalizacion() {
    return fechaFinalizacion;
  }

  /**
   * Cambia la fecha de finalización.
   *
   * @param fechaFinalizacion fecha de finalización
   */
  public void setFechaFinalizacion(LocalDateTime fechaFinalizacion) {
    this.fechaFinalizacion = fechaFinalizacion;
  }

  /**
   * Devuelve el promedio general de IA.
   *
   * @return promedio general de IA
   */
  public Double getPromedioIA() {
    return promedioIA;
  }

  /**
   * Cambia el promedio general de IA.
   *
   * @param promedioIA promedio general de IA
   */
  public void setPromedioIA(Double promedioIA) {
    this.promedioIA = promedioIA;
  }

  /**
   * Devuelve el nivel general ALTO, MEDIO, BAJO o SIN_RESULTADO.
   *
   * @return nivel general ALTO, MEDIO, BAJO o SIN_RESULTADO
   */
  public String getNivel() {
    return nivel;
  }

  /**
   * Cambia el nivel general ALTO, MEDIO, BAJO o SIN_RESULTADO.
   *
   * @param nivel nivel general ALTO, MEDIO, BAJO o SIN_RESULTADO
   */
  public void setNivel(String nivel) {
    this.nivel = nivel;
  }

  /**
   * Devuelve la cantidad de archivos.
   *
   * @return cantidad de archivos
   */
  public int getTotalArchivos() {
    return totalArchivos;
  }

  /**
   * Cambia la cantidad de archivos.
   *
   * @param totalArchivos cantidad de archivos
   */
  public void setTotalArchivos(int totalArchivos) {
    this.totalArchivos = totalArchivos;
  }

  /**
   * Indica si el reporte ya salio por correo.
   *
   * @return indica si el reporte ya salio por correo
   */
  public boolean isCorreoEnviado() {
    return correoEnviado;
  }

  /**
   * Define si el reporte ya salio por correo.
   *
   * @param correoEnviado indica si el reporte ya salio por correo
   */
  public void setCorreoEnviado(boolean correoEnviado) {
    this.correoEnviado = correoEnviado;
  }

  /**
   * Devuelve el mensaje para el usuario.
   *
   * @return mensaje para el usuario
   */
  public String getMensaje() {
    return mensaje;
  }

  /**
   * Cambia el mensaje para el usuario.
   *
   * @param mensaje mensaje para el usuario
   */
  public void setMensaje(String mensaje) {
    this.mensaje = mensaje;
  }

  /**
   * Devuelve los archivos analizados.
   *
   * @return archivos analizados
   */
  public List<AnalyzedFileDTO> getArchivos() {
    return archivos;
  }

  /**
   * Cambia los archivos analizados.
   *
   * @param archivos archivos analizados
   */
  public void setArchivos(List<AnalyzedFileDTO> archivos) {
    this.archivos = archivos;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, username, titulo, origen, urlRepositorio, estado, fechaCreacion,
            fechaFinalizacion, promedioIA, nivel, totalArchivos, correoEnviado, mensaje);
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
    AnalysisDTO other = (AnalysisDTO) obj;
    return Objects.equals(id, other.id)
        && Objects.equals(username, other.username)
        && Objects.equals(titulo, other.titulo)
        && Objects.equals(origen, other.origen)
        && Objects.equals(urlRepositorio, other.urlRepositorio)
        && Objects.equals(estado, other.estado)
        && Objects.equals(fechaCreacion, other.fechaCreacion)
        && Objects.equals(fechaFinalizacion, other.fechaFinalizacion)
        && Objects.equals(promedioIA, other.promedioIA)
        && Objects.equals(nivel, other.nivel)
        && totalArchivos == other.totalArchivos
        && correoEnviado == other.correoEnviado
        && Objects.equals(mensaje, other.mensaje);
  }

  /**
   * Pasa el objeto a texto para depurar. Deja por fuera contraseñas y códigos de correo, y de
   * los contenidos largos solo muestra el tamaño.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "AnalysisDTO [id="
        + id
        + ", username="
        + username
        + ", titulo="
        + titulo
        + ", origen="
        + origen
        + ", urlRepositorio="
        + urlRepositorio
        + ", estado="
        + estado
        + ", fechaCreacion="
        + fechaCreacion
        + ", fechaFinalizacion="
        + fechaFinalizacion
        + ", promedioIA="
        + promedioIA
        + ", nivel="
        + nivel
        + ", totalArchivos="
        + totalArchivos
        + ", correoEnviado="
        + correoEnviado
        + ", mensaje="
        + mensaje
        + "]";
  }
}
