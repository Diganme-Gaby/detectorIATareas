package co.edu.unbosque.proyectofinal.model;

import java.time.LocalDateTime;
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
 * Una solicitud de análisis. Puede traer varios archivos sueltos, un zip o un repositorio de
 * GitHub; cada archivo que sale de ahí se guarda como un {@link AnalyzedFile} con el id de este
 * análisis.
 *
 * <p>El historial es permanente: no hay endpoint para borrar análisis.
 */
@Entity
@Table(name = "analisis")
public class Analysis {

  /** Identificador generado por la base de datos. */
  private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

  /** Nombre de usuario de quien pidió el análisis (el nombre de usuario no cambia). */
  @Column(nullable = false)
  private String username;

  /** Nombre que le dio el usuario o uno generado. */
  private String titulo;

  /** De donde vienen los archivos. */
  @Enumerated(EnumType.STRING)
  private Source origen;

  /** URL del repositorio cuando el origen es GitHub. */
  @Column(nullable = true, length = 500)
  private String urlRepositorio;

  /** Estado del procesamiento. */
  @Enumerated(EnumType.STRING)
  private Status estado;

  /** Momento en que se envió. */
  @Column(updatable = false)
  private LocalDateTime fechaCreacion;

  /** Momento en que terminó. */
  @Column(nullable = true)
  private LocalDateTime fechaFinalizacion;

  /** Promedio del porcentaje de IA de todos los archivos con resultado. */
  @Column(nullable = true)
  private Double promedioIA;

  /** Cantidad de archivos del análisis. */
  private int totalArchivos;

  /** El reporte ya salio por correo. */
  private boolean correoEnviado;

  /** IP desde la que se envió, para la auditoría del procesamiento en segundo plano. */
  @Column(length = 64)
  private String ipOrigen;

  /** Mensaje para el usuario cuando algo no salio bien. */
  @Column(length = 1000)
  private String mensaje;

  /** Origen de los archivos. */
  public enum Source {
    /** Uno o varios archivos subidos. */
    ARCHIVOS,
    /** Archivos que venian dentro de un .zip. */
    ZIP,
    /** Repositorio público de GitHub. */
    GITHUB
  }

  /** Estados del procesamiento. */
  public enum Status {
    /** En cola. */
    PENDIENTE,
    /** Las IAs están trabajando. */
    PROCESANDO,
    /** Las tres IAs respondieron para todos los archivos. */
    COMPLETADO,
    /** Terminó, pero alguna IA no respondió en uno o más archivos. */
    COMPLETADO_PARCIAL,
    /** No se pudo analizar ningún archivo. */
    FALLIDO
  }

  /** Constructor vacío que exige JPA. */
  public Analysis() {
    this.fechaCreacion = LocalDateTime.now();
    this.estado = Status.PENDIENTE;
  }

  /**
   * Crea un análisis pendiente.
   *
   * @param username dueño
   * @param titulo título visible
   * @param origen origen de los archivos
   */
  public Analysis(String username, String titulo, Source origen) {
    this();
    this.username = username;
    this.titulo = titulo;
    this.origen = origen;
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
   * Devuelve el nombre de usuario del dueño.
   *
   * @return nombre de usuario del dueño
   */
  public String getUsername() {
    return username;
  }

  /**
   * Cambia el nombre de usuario del dueño.
   *
   * @param username nombre de usuario del dueño
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
   * Devuelve el origen.
   *
   * @return origen
   */
  public Source getOrigen() {
    return origen;
  }

  /**
   * Cambia el origen.
   *
   * @param origen origen
   */
  public void setOrigen(Source origen) {
    this.origen = origen;
  }

  /**
   * Devuelve la url del repositorio.
   *
   * @return url del repositorio
   */
  public String getUrlRepositorio() {
    return urlRepositorio;
  }

  /**
   * Cambia la url del repositorio.
   *
   * @param urlRepositorio url del repositorio
   */
  public void setUrlRepositorio(String urlRepositorio) {
    this.urlRepositorio = urlRepositorio;
  }

  /**
   * Devuelve el estado.
   *
   * @return estado
   */
  public Status getEstado() {
    return estado;
  }

  /**
   * Cambia el estado.
   *
   * @param estado estado
   */
  public void setEstado(Status estado) {
    this.estado = estado;
  }

  /**
   * Devuelve la fecha de creación.
   *
   * @return fecha de creación
   */
  public LocalDateTime getFechaCreacion() {
    return fechaCreacion;
  }

  /**
   * Cambia la fecha de creación.
   *
   * @param fechaCreacion fecha de creación
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
   * Devuelve true si ya se envió el correo.
   *
   * @return true si ya se envió el correo
   */
  public boolean isCorreoEnviado() {
    return correoEnviado;
  }

  /**
   * Cambia la marca de envío.
   *
   * @param correoEnviado marca de envío
   */
  public void setCorreoEnviado(boolean correoEnviado) {
    this.correoEnviado = correoEnviado;
  }

  /**
   * Devuelve la ip de origen.
   *
   * @return ip de origen
   */
  public String getIpOrigen() {
    return ipOrigen;
  }

  /**
   * Cambia la ip de origen.
   *
   * @param ipOrigen ip de origen
   */
  public void setIpOrigen(String ipOrigen) {
    this.ipOrigen = ipOrigen;
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
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, username, titulo, fechaCreacion);
  }

  /**
   * Dos análisis son iguales si coinciden id, usuario, título y fecha de creación.
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
    Analysis other = (Analysis) obj;
    return Objects.equals(id, other.id)
        && Objects.equals(username, other.username)
        && Objects.equals(titulo, other.titulo)
        && Objects.equals(fechaCreacion, other.fechaCreacion);
  }

  /**
   * Pasa el objeto a texto para depurar.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "Analysis [id="
        + id
        + ", username="
        + username
        + ", titulo="
        + titulo
        + ", origen="
        + origen
        + ", estado="
        + estado
        + ", fechaCreacion="
        + fechaCreacion
        + ", promedioIA="
        + promedioIA
        + ", totalArchivos="
        + totalArchivos
        + ", correoEnviado="
        + correoEnviado
        + "]";
  }
}
