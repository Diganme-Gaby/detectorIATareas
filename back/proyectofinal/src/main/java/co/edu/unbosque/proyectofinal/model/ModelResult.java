package co.edu.unbosque.proyectofinal.model;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Lo que respondió una IA sobre un archivo (guardado por su id): porcentaje, veredicto y motivos.
 * Si la IA no respondió, queda con {@code exitoso = false} y un mensaje para el usuario.
 */
@Entity
@Table(name = "resultado_modelo")
public class ModelResult {

  /** Identificador generado por la base de datos. */
  private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

  /** Id del archivo evaluado. */
  @Column(nullable = false)
  private Long archivoId;

  /** Huella SHA-256 del archivo evaluado; permite reutilizar el resultado si se vuelve a subir. */
  @Column(length = 64)
  private String hashSha256;

  /** Proveedor que respondió (Gemini, Groq, OpenRouter o uno de los respaldos). */
  private String proveedor;

  /**
   * Si respondió un modelo de respaldo, el proveedor principal al que reemplazó. Queda vacío cuando
   * respondió el principal.
   */
  @Column(length = 60)
  private String respaldoDe;

  /** Modelo exacto que se uso. */
  private String modelo;

  /** Probabilidad de que el contenido sea generado por IA (0 a 100). */
  @Column(nullable = true)
  private Double porcentajeIA;

  /** Veredicto corto que devolvió la IA. */
  @Column(length = 60)
  private String veredicto;

  /** Motivos separados por salto de línea. */
  @Column(length = 4000)
  private String motivos;

  /** La IA respondió con un resultado válido. */
  private boolean exitoso;

  /** Mensaje que ve el usuario cuando la IA no respondió. */
  @Column(length = 500)
  private String mensajeUsuario;

  /** Tiempo que tardo la respuesta. */
  private long tiempoMs;

  /** El resultado se reutilizó de un análisis anterior del mismo archivo. */
  private boolean desdeCache;

  /** Momento del resultado. */
  private LocalDateTime fecha;

  /** Constructor vacío que exige JPA. */
  public ModelResult() {
    this.fecha = LocalDateTime.now();
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
   * Devuelve el id del archivo evaluado.
   *
   * @return id del archivo evaluado
   */
  public Long getArchivoId() {
    return archivoId;
  }

  /**
   * Cambia el id del archivo evaluado.
   *
   * @param archivoId id del archivo evaluado
   */
  public void setArchivoId(Long archivoId) {
    this.archivoId = archivoId;
  }

  /**
   * Devuelve la huella SHA-256 del archivo.
   *
   * @return huella SHA-256 del archivo
   */
  public String getHashSha256() {
    return hashSha256;
  }

  /**
   * Cambia la huella SHA-256 del archivo.
   *
   * @param hashSha256 huella SHA-256 del archivo
   */
  public void setHashSha256(String hashSha256) {
    this.hashSha256 = hashSha256;
  }

  /**
   * Devuelve el proveedor.
   *
   * @return proveedor
   */
  public String getProveedor() {
    return proveedor;
  }

  /**
   * Cambia el proveedor.
   *
   * @param proveedor proveedor
   */
  public void setProveedor(String proveedor) {
    this.proveedor = proveedor;
  }

  /**
   * Devuelve el modelo.
   *
   * @return modelo
   */
  public String getModelo() {
    return modelo;
  }

  /**
   * Cambia el modelo.
   *
   * @param modelo modelo
   */
  public void setModelo(String modelo) {
    this.modelo = modelo;
  }

  /**
   * Devuelve el porcentaje de IA.
   *
   * @return porcentaje de IA
   */
  public Double getPorcentajeIA() {
    return porcentajeIA;
  }

  /**
   * Cambia el porcentaje de IA.
   *
   * @param porcentajeIA porcentaje de IA
   */
  public void setPorcentajeIA(Double porcentajeIA) {
    this.porcentajeIA = porcentajeIA;
  }

  /**
   * Devuelve el veredicto.
   *
   * @return veredicto
   */
  public String getVeredicto() {
    return veredicto;
  }

  /**
   * Cambia el veredicto.
   *
   * @param veredicto veredicto
   */
  public void setVeredicto(String veredicto) {
    this.veredicto = veredicto;
  }

  /**
   * Devuelve los motivos separados por salto de línea.
   *
   * @return motivos separados por salto de línea
   */
  public String getMotivos() {
    return motivos;
  }

  /**
   * Cambia los motivos separados por salto de línea.
   *
   * @param motivos motivos separados por salto de línea
   */
  public void setMotivos(String motivos) {
    this.motivos = motivos;
  }

  /**
   * Devuelve true si la IA respondió.
   *
   * @return true si la IA respondió
   */
  public boolean isExitoso() {
    return exitoso;
  }

  /**
   * Cambia la marca de éxito.
   *
   * @param exitoso marca de éxito
   */
  public void setExitoso(boolean exitoso) {
    this.exitoso = exitoso;
  }

  /**
   * Devuelve el mensaje para el usuario.
   *
   * @return mensaje para el usuario
   */
  public String getMensajeUsuario() {
    return mensajeUsuario;
  }

  /**
   * Cambia el mensaje para el usuario.
   *
   * @param mensajeUsuario mensaje para el usuario
   */
  public void setMensajeUsuario(String mensajeUsuario) {
    this.mensajeUsuario = mensajeUsuario;
  }

  /**
   * Devuelve el tiempo de respuesta en milisegundos.
   *
   * @return tiempo de respuesta en milisegundos
   */
  public long getTiempoMs() {
    return tiempoMs;
  }

  /**
   * Cambia el tiempo de respuesta en milisegundos.
   *
   * @param tiempoMs tiempo de respuesta en milisegundos
   */
  public void setTiempoMs(long tiempoMs) {
    this.tiempoMs = tiempoMs;
  }

  /**
   * Devuelve true si se reutilizó de otro análisis.
   *
   * @return true si se reutilizó de otro análisis
   */
  public boolean isDesdeCache() {
    return desdeCache;
  }

  /**
   * Cambia la marca de reutilización.
   *
   * @param desdeCache marca de reutilización
   */
  public void setDesdeCache(boolean desdeCache) {
    this.desdeCache = desdeCache;
  }

  /**
   * Devuelve el proveedor principal al que reemplazó el respaldo, o null.
   *
   * @return proveedor principal al que reemplazó el respaldo, o null
   */
  public String getRespaldoDe() {
    return respaldoDe;
  }

  /**
   * Cambia el proveedor principal al que reemplazó el respaldo.
   *
   * @param respaldoDe proveedor principal al que reemplazó el respaldo
   */
  public void setRespaldoDe(String respaldoDe) {
    this.respaldoDe = respaldoDe;
  }

  /**
   * Devuelve la fecha del resultado.
   *
   * @return fecha del resultado
   */
  public LocalDateTime getFecha() {
    return fecha;
  }

  /**
   * Cambia la fecha del resultado.
   *
   * @param fecha fecha del resultado
   */
  public void setFecha(LocalDateTime fecha) {
    this.fecha = fecha;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, proveedor, modelo);
  }

  /**
   * Dos registros son iguales si coinciden id, proveedor, modelo.
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
    ModelResult other = (ModelResult) obj;
    return Objects.equals(id, other.id)
        && Objects.equals(proveedor, other.proveedor)
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
    return "ModelResult [id="
        + id
        + ", proveedor="
        + proveedor
        + ", respaldoDe="
        + respaldoDe
        + ", modelo="
        + modelo
        + ", porcentajeIA="
        + porcentajeIA
        + ", veredicto="
        + veredicto
        + ", motivos="
        + motivos
        + ", exitoso="
        + exitoso
        + ", mensajeUsuario="
        + mensajeUsuario
        + ", tiempoMs="
        + tiempoMs
        + ", desdeCache="
        + desdeCache
        + ", fecha="
        + fecha
        + "]";
  }
}
