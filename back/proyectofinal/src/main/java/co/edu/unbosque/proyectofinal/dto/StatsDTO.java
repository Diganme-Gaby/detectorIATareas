package co.edu.unbosque.proyectofinal.dto;

import java.util.Objects;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Estadísticas de uso para el módulo administrativo y las gráficas del front.
 */
public class StatsDTO {

  /** Usuarios registrados. */
  private long totalUsuarios;

  /** Usuarios activos. */
  private long usuariosActivos;

  /** Usuarios con correo verificado. */
  private long usuariosVerificados;

  /** Análisis realizados. */
  private long totalAnalisis;

  /** Archivos analizados. */
  private long totalArchivos;

  /** Promedio de IA de todos los archivos con resultado. */
  private Double promedioGeneralIA;

  /** Análisis por día en los últimos 30 días. */
  private Map<String, Long> analisisPorDia = new LinkedHashMap<>();

  /** Inicios de sesión por día en los últimos 30 días. */
  private Map<String, Long> loginsPorDia = new LinkedHashMap<>();

  /** Análisis por día de la semana. */
  private Map<String, Long> usoPorDiaSemana = new LinkedHashMap<>();

  /** Archivos por categoría. */
  private Map<String, Long> archivosPorCategoria = new LinkedHashMap<>();

  /** Archivos por nivel de IA. */
  private Map<String, Long> archivosPorNivel = new LinkedHashMap<>();

  /** Porcentaje promedio que da cada IA. */
  private Map<String, Double> promedioPorProveedor = new LinkedHashMap<>();

  /** Veces que cada IA no respondió. */
  private Map<String, Long> fallosPorProveedor = new LinkedHashMap<>();

  /** Usuarios con más análisis. */
  private Map<String, Long> topUsuarios = new LinkedHashMap<>();

  /** Constructor vacío; Gson y Spring lo usan para crear el objeto. */
  public StatsDTO() {}

  /**
   * Devuelve los usuarios registrados.
   *
   * @return usuarios registrados
   */
  public long getTotalUsuarios() {
    return totalUsuarios;
  }

  /**
   * Cambia los usuarios registrados.
   *
   * @param totalUsuarios usuarios registrados
   */
  public void setTotalUsuarios(long totalUsuarios) {
    this.totalUsuarios = totalUsuarios;
  }

  /**
   * Devuelve los usuarios activos.
   *
   * @return usuarios activos
   */
  public long getUsuariosActivos() {
    return usuariosActivos;
  }

  /**
   * Cambia los usuarios activos.
   *
   * @param usuariosActivos usuarios activos
   */
  public void setUsuariosActivos(long usuariosActivos) {
    this.usuariosActivos = usuariosActivos;
  }

  /**
   * Devuelve los usuarios con correo verificado.
   *
   * @return usuarios con correo verificado
   */
  public long getUsuariosVerificados() {
    return usuariosVerificados;
  }

  /**
   * Cambia los usuarios con correo verificado.
   *
   * @param usuariosVerificados usuarios con correo verificado
   */
  public void setUsuariosVerificados(long usuariosVerificados) {
    this.usuariosVerificados = usuariosVerificados;
  }

  /**
   * Devuelve los análisis realizados.
   *
   * @return análisis realizados
   */
  public long getTotalAnalisis() {
    return totalAnalisis;
  }

  /**
   * Cambia los análisis realizados.
   *
   * @param totalAnalisis análisis realizados
   */
  public void setTotalAnalisis(long totalAnalisis) {
    this.totalAnalisis = totalAnalisis;
  }

  /**
   * Devuelve los archivos analizados.
   *
   * @return archivos analizados
   */
  public long getTotalArchivos() {
    return totalArchivos;
  }

  /**
   * Cambia los archivos analizados.
   *
   * @param totalArchivos archivos analizados
   */
  public void setTotalArchivos(long totalArchivos) {
    this.totalArchivos = totalArchivos;
  }

  /**
   * Devuelve el promedio de IA de todos los archivos con resultado.
   *
   * @return promedio de IA de todos los archivos con resultado
   */
  public Double getPromedioGeneralIA() {
    return promedioGeneralIA;
  }

  /**
   * Cambia el promedio de IA de todos los archivos con resultado.
   *
   * @param promedioGeneralIA promedio de IA de todos los archivos con resultado
   */
  public void setPromedioGeneralIA(Double promedioGeneralIA) {
    this.promedioGeneralIA = promedioGeneralIA;
  }

  /**
   * Devuelve los análisis por día en los últimos 30 días.
   *
   * @return análisis por día en los últimos 30 días
   */
  public Map<String, Long> getAnalisisPorDia() {
    return analisisPorDia;
  }

  /**
   * Cambia los análisis por día en los últimos 30 días.
   *
   * @param analisisPorDia análisis por día en los últimos 30 días
   */
  public void setAnalisisPorDia(Map<String, Long> analisisPorDia) {
    this.analisisPorDia = analisisPorDia;
  }

  /**
   * Devuelve los inicios de sesión por día en los últimos 30 días.
   *
   * @return inicios de sesión por día en los últimos 30 días
   */
  public Map<String, Long> getLoginsPorDia() {
    return loginsPorDia;
  }

  /**
   * Cambia los inicios de sesión por día en los últimos 30 días.
   *
   * @param loginsPorDia inicios de sesión por día en los últimos 30 días
   */
  public void setLoginsPorDia(Map<String, Long> loginsPorDia) {
    this.loginsPorDia = loginsPorDia;
  }

  /**
   * Devuelve los análisis por día de la semana.
   *
   * @return análisis por día de la semana
   */
  public Map<String, Long> getUsoPorDiaSemana() {
    return usoPorDiaSemana;
  }

  /**
   * Cambia los análisis por día de la semana.
   *
   * @param usoPorDiaSemana análisis por día de la semana
   */
  public void setUsoPorDiaSemana(Map<String, Long> usoPorDiaSemana) {
    this.usoPorDiaSemana = usoPorDiaSemana;
  }

  /**
   * Devuelve los archivos por categoría.
   *
   * @return archivos por categoría
   */
  public Map<String, Long> getArchivosPorCategoria() {
    return archivosPorCategoria;
  }

  /**
   * Cambia los archivos por categoría.
   *
   * @param archivosPorCategoria archivos por categoría
   */
  public void setArchivosPorCategoria(Map<String, Long> archivosPorCategoria) {
    this.archivosPorCategoria = archivosPorCategoria;
  }

  /**
   * Devuelve los archivos por nivel de IA.
   *
   * @return archivos por nivel de IA
   */
  public Map<String, Long> getArchivosPorNivel() {
    return archivosPorNivel;
  }

  /**
   * Cambia los archivos por nivel de IA.
   *
   * @param archivosPorNivel archivos por nivel de IA
   */
  public void setArchivosPorNivel(Map<String, Long> archivosPorNivel) {
    this.archivosPorNivel = archivosPorNivel;
  }

  /**
   * Devuelve el porcentaje promedio que da cada IA.
   *
   * @return porcentaje promedio que da cada IA
   */
  public Map<String, Double> getPromedioPorProveedor() {
    return promedioPorProveedor;
  }

  /**
   * Cambia el porcentaje promedio que da cada IA.
   *
   * @param promedioPorProveedor porcentaje promedio que da cada IA
   */
  public void setPromedioPorProveedor(Map<String, Double> promedioPorProveedor) {
    this.promedioPorProveedor = promedioPorProveedor;
  }

  /**
   * Devuelve las veces que cada IA no respondió.
   *
   * @return veces que cada IA no respondió
   */
  public Map<String, Long> getFallosPorProveedor() {
    return fallosPorProveedor;
  }

  /**
   * Cambia las veces que cada IA no respondió.
   *
   * @param fallosPorProveedor veces que cada IA no respondió
   */
  public void setFallosPorProveedor(Map<String, Long> fallosPorProveedor) {
    this.fallosPorProveedor = fallosPorProveedor;
  }

  /**
   * Devuelve los usuarios con más análisis.
   *
   * @return usuarios con más análisis
   */
  public Map<String, Long> getTopUsuarios() {
    return topUsuarios;
  }

  /**
   * Cambia los usuarios con más análisis.
   *
   * @param topUsuarios usuarios con más análisis
   */
  public void setTopUsuarios(Map<String, Long> topUsuarios) {
    this.topUsuarios = topUsuarios;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(totalUsuarios, usuariosActivos, usuariosVerificados, totalAnalisis,
            totalArchivos, promedioGeneralIA, analisisPorDia, loginsPorDia, usoPorDiaSemana,
            archivosPorCategoria, archivosPorNivel, promedioPorProveedor, fallosPorProveedor,
            topUsuarios);
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
    StatsDTO other = (StatsDTO) obj;
    return totalUsuarios == other.totalUsuarios
        && usuariosActivos == other.usuariosActivos
        && usuariosVerificados == other.usuariosVerificados
        && totalAnalisis == other.totalAnalisis
        && totalArchivos == other.totalArchivos
        && Objects.equals(promedioGeneralIA, other.promedioGeneralIA)
        && Objects.equals(analisisPorDia, other.analisisPorDia)
        && Objects.equals(loginsPorDia, other.loginsPorDia)
        && Objects.equals(usoPorDiaSemana, other.usoPorDiaSemana)
        && Objects.equals(archivosPorCategoria, other.archivosPorCategoria)
        && Objects.equals(archivosPorNivel, other.archivosPorNivel)
        && Objects.equals(promedioPorProveedor, other.promedioPorProveedor)
        && Objects.equals(fallosPorProveedor, other.fallosPorProveedor)
        && Objects.equals(topUsuarios, other.topUsuarios);
  }

  /**
   * Pasa el objeto a texto para depurar. Deja por fuera contraseñas y códigos de correo, y de
   * los contenidos largos solo muestra el tamaño.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "StatsDTO [totalUsuarios="
        + totalUsuarios
        + ", usuariosActivos="
        + usuariosActivos
        + ", usuariosVerificados="
        + usuariosVerificados
        + ", totalAnalisis="
        + totalAnalisis
        + ", totalArchivos="
        + totalArchivos
        + ", promedioGeneralIA="
        + promedioGeneralIA
        + ", analisisPorDia="
        + analisisPorDia
        + ", loginsPorDia="
        + loginsPorDia
        + ", usoPorDiaSemana="
        + usoPorDiaSemana
        + ", archivosPorCategoria="
        + archivosPorCategoria
        + ", archivosPorNivel="
        + archivosPorNivel
        + ", promedioPorProveedor="
        + promedioPorProveedor
        + ", fallosPorProveedor="
        + fallosPorProveedor
        + ", topUsuarios="
        + topUsuarios
        + "]";
  }
}
