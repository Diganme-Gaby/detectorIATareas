package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Respuesta de una IA sobre un archivo.
 */
public class ModelResultDTO {

  /** Identificador. */
  private Long id;

  /** Proveedor de la IA que respondió. */
  private String proveedor;

  /** Proveedor principal al que reemplazó, si respondió un modelo de respaldo. */
  private String respaldoDe;

  /** Modelo usado. */
  private String modelo;

  /** Probabilidad de contenido generado por IA (0 a 100). */
  private Double porcentajeIA;

  /** Veredicto corto. */
  private String veredicto;

  /** Motivos que dio la IA. */
  private List<String> motivos = new ArrayList<>();

  /** Indica si la IA respondió. */
  private boolean exitoso;

  /** Aviso cuando la IA no respondió. */
  private String mensajeUsuario;

  /** Tiempo de respuesta en milisegundos. */
  private long tiempoMs;

  /** Indica si se reutilizó de un análisis anterior. */
  private boolean desdeCache;

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public ModelResultDTO() {}

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
   * Devuelve el proveedor de la IA.
   *
   * @return proveedor de la IA
   */
  public String getProveedor() {
    return proveedor;
  }

  /**
   * Cambia el proveedor de la IA.
   *
   * @param proveedor proveedor de la IA
   */
  public void setProveedor(String proveedor) {
    this.proveedor = proveedor;
  }

  /**
   * Devuelve el modelo usado.
   *
   * @return modelo usado
   */
  public String getModelo() {
    return modelo;
  }

  /**
   * Cambia el modelo usado.
   *
   * @param modelo modelo usado
   */
  public void setModelo(String modelo) {
    this.modelo = modelo;
  }

  /**
   * Devuelve la probabilidad de contenido generado por IA (0 a 100).
   *
   * @return probabilidad de contenido generado por IA (0 a 100)
   */
  public Double getPorcentajeIA() {
    return porcentajeIA;
  }

  /**
   * Cambia la probabilidad de contenido generado por IA (0 a 100).
   *
   * @param porcentajeIA probabilidad de contenido generado por IA (0 a 100)
   */
  public void setPorcentajeIA(Double porcentajeIA) {
    this.porcentajeIA = porcentajeIA;
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
   * Indica si la IA respondió.
   *
   * @return indica si la IA respondió
   */
  public boolean isExitoso() {
    return exitoso;
  }

  /**
   * Define si la IA respondió.
   *
   * @param exitoso indica si la IA respondió
   */
  public void setExitoso(boolean exitoso) {
    this.exitoso = exitoso;
  }

  /**
   * Devuelve el aviso cuando la IA no respondió.
   *
   * @return aviso cuando la IA no respondió
   */
  public String getMensajeUsuario() {
    return mensajeUsuario;
  }

  /**
   * Cambia el aviso cuando la IA no respondió.
   *
   * @param mensajeUsuario aviso cuando la IA no respondió
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
   * Devuelve el proveedor principal al que reemplazó, o null si respondió el principal.
   *
   * @return proveedor principal al que reemplazó, o null si respondió el principal
   */
  public String getRespaldoDe() {
    return respaldoDe;
  }

  /**
   * Cambia el proveedor principal al que reemplazó.
   *
   * @param respaldoDe proveedor principal al que reemplazó
   */
  public void setRespaldoDe(String respaldoDe) {
    this.respaldoDe = respaldoDe;
  }

  /**
   * Nombre de la posición en la que va este resultado: el proveedor principal, aunque haya
   * respondido su respaldo.
   *
   * @return proveedor principal
   */
  public String getPosicion() {
    return respaldoDe == null ? proveedor : respaldoDe;
  }

  /**
   * Indica si se reutilizó de un análisis anterior.
   *
   * @return indica si se reutilizó de un análisis anterior
   */
  public boolean isDesdeCache() {
    return desdeCache;
  }

  /**
   * Define si se reutilizó de un análisis anterior.
   *
   * @param desdeCache indica si se reutilizó de un análisis anterior
   */
  public void setDesdeCache(boolean desdeCache) {
    this.desdeCache = desdeCache;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, proveedor, respaldoDe, modelo, porcentajeIA, veredicto, motivos, exitoso,
            mensajeUsuario, tiempoMs, desdeCache);
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
    ModelResultDTO other = (ModelResultDTO) obj;
    return Objects.equals(id, other.id)
        && Objects.equals(proveedor, other.proveedor)
        && Objects.equals(respaldoDe, other.respaldoDe)
        && Objects.equals(modelo, other.modelo)
        && Objects.equals(porcentajeIA, other.porcentajeIA)
        && Objects.equals(veredicto, other.veredicto)
        && Objects.equals(motivos, other.motivos)
        && exitoso == other.exitoso
        && Objects.equals(mensajeUsuario, other.mensajeUsuario)
        && tiempoMs == other.tiempoMs
        && desdeCache == other.desdeCache;
  }

  /**
   * Pasa el objeto a texto para depurar. Deja por fuera contraseñas y códigos de correo, y de
   * los contenidos largos solo muestra el tamaño.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "ModelResultDTO [id="
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
        + "]";
  }
}
