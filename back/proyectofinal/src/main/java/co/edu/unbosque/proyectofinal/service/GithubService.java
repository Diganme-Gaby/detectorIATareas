package co.edu.unbosque.proyectofinal.service;

import co.edu.unbosque.proyectofinal.dto.GithubAnalysisDTO;
import co.edu.unbosque.proyectofinal.dto.GithubRepoInfoDTO;
import co.edu.unbosque.proyectofinal.dto.GithubRepositoryDTO;
import co.edu.unbosque.proyectofinal.exception.AnalysisRequestException;
import co.edu.unbosque.proyectofinal.util.ZipUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Descarga un repositorio público de GitHub como zip y selecciona los archivos a analizar según las
 * restricciones del usuario: carpeta, extensiones y cantidad máxima.
 *
 * <p>Solo se aceptan URLs de github.com; la URL de descarga la arma el servidor, así que no se
 * puede usar este endpoint para hacer peticiones a otros dominios.
 */
@Service
public class GithubService {

  /** Logger para dejar registro en consola. */
  private static final Logger log = LoggerFactory.getLogger(GithubService.class);

  /** Formato aceptado: https://github.com/dueno/repo[.git][/tree/rama[/carpeta]]. */
  private static final Pattern URL_GITHUB =
      Pattern.compile(
          "^https?://(?:www\\.)?github\\.com/([A-Za-z0-9_.-]+)/([A-Za-z0-9_.-]+?)(?:\\.git)?"
              + "(?:/tree/([^/?#]+)(/[^?#]*)?)?/?(?:[?#].*)?$");

  /** Formato válido de nombre de rama. */
  private static final Pattern RAMA = Pattern.compile("^[A-Za-z0-9._/-]{1,100}$");

  /** Carpetas que nunca se analizan. */
  private static final Set<String> CARPETAS_EXCLUIDAS =
      Set.of(
          "node_modules", "target", "build", "dist", ".git", ".idea", ".vscode", ".angular", ".mvn",
          "vendor", "out", "bin", "obj", "coverage", "__pycache__", ".gradle", ".settings", "doc");

  /** Archivos generados que no aportan al análisis. */
  private static final Set<String> ARCHIVOS_EXCLUIDOS =
      Set.of("package-lock.json", "yarn.lock", "pnpm-lock.yaml", "mvnw", "mvnw.cmd");

  /** Tamaño máximo del zip descargado. */
  private static final long MAX_ZIP = 60L * 1024 * 1024;

  /** Máximo de archivos por repositorio. */
  private final int maxArchivos;

  /** Tamaño máximo de cada archivo del repositorio. */
  private final long maxBytesArchivo;

  /** Extensiones que se usan si el usuario no envía ninguna. */
  private final Set<String> extensionesPorDefecto;

  /**
   * Spring inyecta aquí las dependencias.
   *
   * @param maxArchivos máximo de archivos por repositorio
   * @param maxKb tamaño máximo de cada archivo en KB
   * @param extensiones extensiones por defecto separadas por coma
   */
  public GithubService(
      @Value("${app.analysis.github-max-files:15}") int maxArchivos,
      @Value("${app.analysis.github-max-file-kb:400}") long maxKb,
      @Value("${app.analysis.github-default-extensions:java,ts,html,css}") String extensiones) {
    this.maxArchivos = maxArchivos;
    this.maxBytesArchivo = maxKb * 1024;
    this.extensionesPorDefecto = normalizar(Arrays.asList(extensiones.split(",")));
  }

  /**
   * Interpreta una URL de GitHub.
   *
   * @param url URL enviada por el usuario
   * @return repositorio identificado
   * @throws AnalysisRequestException si la URL no es de un repositorio de github.com
   */
  public GithubRepositoryDTO interpretarUrl(String url) throws AnalysisRequestException {
    if (url == null) {
      throw new AnalysisRequestException("Debes enviar la URL del repositorio.");
    }
    Matcher m = URL_GITHUB.matcher(url.trim());
    if (!m.matches()) {
      throw new AnalysisRequestException(
          "La URL debe ser de un repositorio público de GitHub, por ejemplo https://github.com/usuario/proyecto");
    }
    String carpeta = m.group(4) == null ? null : m.group(4).replaceAll("^/+|/+$", "");
    return new GithubRepositoryDTO(m.group(1), m.group(2), m.group(3), carpeta);
  }

  /**
   * Descarga el repositorio y devuelve los archivos que cumplen las restricciones.
   *
   * @param solicitud URL y restricciones
   * @return archivos seleccionados y conteo de omitidos
   * @throws AnalysisRequestException si el repositorio no existe, es privado o no hay archivos
   */
  public ZipUtil.Resultado descargar(GithubAnalysisDTO solicitud) throws AnalysisRequestException {
    GithubRepositoryDTO repo = interpretarUrl(solicitud.getUrl());
    String rama = elegirRama(solicitud.getRama(), repo);
    String carpeta = solicitud.getCarpeta() != null && !solicitud.getCarpeta().isBlank()
        ? solicitud.getCarpeta().trim().replaceAll("^/+|/+$", "")
        : repo.getCarpeta();
    Set<String> extensiones = solicitud.getExtensiones() == null || solicitud.getExtensiones().isEmpty()
        ? extensionesPorDefecto
        : normalizar(solicitud.getExtensiones());
    int limite = solicitud.getMaxArchivos() == null
        ? maxArchivos
        : Math.max(1, Math.min(maxArchivos, solicitud.getMaxArchivos()));

    byte[] zip = descargarZip(repo, rama);
    try {
      ZipUtil.Resultado r =
          ZipUtil.descomprimir(zip, limite, maxBytesArchivo, 300L * 1024 * 1024,
              ruta -> aceptar(ruta, carpeta, extensiones));
      if (r.getArchivos().isEmpty()) {
        throw new AnalysisRequestException(
            "El repositorio no tiene archivos que cumplan las restricciones (carpeta "
                + (carpeta == null ? "raíz" : carpeta) + ", extensiones " + extensiones + ").");
      }
      return r;
    } catch (IOException e) {
      throw new AnalysisRequestException("No se pudo leer el zip del repositorio: " + e.getMessage());
    }
  }

  /**
   * Decide la rama: la que envió el usuario, la de la URL o la rama por defecto del repositorio.
   *
   * @param ramaSolicitud rama enviada en la solicitud
   * @param repo repositorio
   * @return rama a descargar
   * @throws AnalysisRequestException si la rama tiene caracteres inválidos o el repositorio no existe
   */
  private String elegirRama(String ramaSolicitud, GithubRepositoryDTO repo) throws AnalysisRequestException {
    String rama = ramaSolicitud != null && !ramaSolicitud.isBlank() ? ramaSolicitud.trim() : repo.getRama();
    if (rama != null) {
      if (!RAMA.matcher(rama).matches() || rama.contains("..")) {
        throw new AnalysisRequestException("El nombre de la rama no es válido.");
      }
      return rama;
    }
    try {
      GithubRepoInfoDTO info =
          ExternalHTTPRequestHandler.doGetAndConvertToGithubRepoDto(
              "https://api.github.com/repos/" + repo.getDueno() + "/" + repo.getNombre());
      if (info.getStatuscode() == 404) {
        throw new AnalysisRequestException("El repositorio no existe o es privado.");
      }
      if (info.getStatuscode() == 200 && info.getDefaultBranch() != null) {
        return info.getDefaultBranch();
      }
      log.warn("La API de GitHub respondió {} al buscar la rama por defecto", info.getStatuscode());
    } catch (IOException | RuntimeException e) {
      log.warn("No se pudo consultar la rama por defecto: {}", e.getMessage());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return null;
  }

  /**
   * Descarga el zip de la rama. Si no se conoce la rama intenta main y luego master.
   *
   * @param repo repositorio
   * @param rama rama o null
   * @return bytes del zip
   * @throws AnalysisRequestException si no se pudo descargar
   */
  private byte[] descargarZip(GithubRepositoryDTO repo, String rama) throws AnalysisRequestException {
    List<String> candidatas = rama != null ? List.of(rama) : List.of("main", "master");
    for (String r : candidatas) {
      String url = "https://codeload.github.com/" + repo.getDueno() + "/" + repo.getNombre() + "/zip/refs/heads/" + r;
      try {
        return ExternalHTTPRequestHandler.doGetBytes(url, MAX_ZIP, 60);
      } catch (IOException e) {
        log.warn("No se pudo descargar {}: {}", url, e.getMessage());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
    throw new AnalysisRequestException(
        "No se pudo descargar el repositorio. Verifica que sea público y que la rama exista.");
  }

  /**
   * Filtro de rutas dentro del zip. GitHub pone todo dentro de una carpeta "repo-rama/".
   *
   * @param rutaZip ruta dentro del zip
   * @param carpeta carpeta permitida o null
   * @param extensiones extensiones permitidas
   * @return true si el archivo se analiza
   */
  private boolean aceptar(String rutaZip, String carpeta, Set<String> extensiones) {
    int barra = rutaZip.indexOf('/');
    String ruta = barra >= 0 ? rutaZip.substring(barra + 1) : rutaZip;
    if (ruta.isEmpty()) {
      return false;
    }
    if (carpeta != null && !carpeta.isEmpty() && !(ruta.startsWith(carpeta + "/") || ruta.equals(carpeta))) {
      return false;
    }
    String[] partes = ruta.split("/");
    for (int i = 0; i < partes.length - 1; i++) {
      if (CARPETAS_EXCLUIDAS.contains(partes[i].toLowerCase(Locale.ROOT))) {
        return false;
      }
    }
    String nombre = partes[partes.length - 1];
    if (ARCHIVOS_EXCLUIDOS.contains(nombre.toLowerCase(Locale.ROOT))) {
      return false;
    }
    return extensiones.contains(FileContentService.extension(nombre));
  }

  /**
   * Devuelve las extensiones en minúsculas sin punto.
   *
   * @param lista extensiones con o sin punto
   * @return extensiones en minúsculas sin punto
   */
  private static Set<String> normalizar(List<String> lista) {
    Set<String> resultado = new HashSet<>();
    for (String e : lista) {
      if (e != null && !e.isBlank()) {
        resultado.add(e.trim().toLowerCase(Locale.ROOT).replaceFirst("^\\.", ""));
      }
    }
    return resultado;
  }

  /**
   * Devuelve el máximo de archivos por repositorio.
   *
   * @return máximo de archivos por repositorio
   */
  public int getMaxArchivos() {
    return maxArchivos;
  }

  /**
   * Devuelve las extensiones por defecto.
   *
   * @return extensiones por defecto
   */
  public Set<String> getExtensionesPorDefecto() {
    return extensionesPorDefecto;
  }
}
