package co.edu.unbosque.proyectofinal.service;

import co.edu.unbosque.proyectofinal.dto.AnalysisDTO;
import co.edu.unbosque.proyectofinal.dto.AnalyzedFileDTO;
import co.edu.unbosque.proyectofinal.dto.GithubAnalysisDTO;
import co.edu.unbosque.proyectofinal.dto.GithubRepositoryDTO;
import co.edu.unbosque.proyectofinal.dto.IncomingFileDTO;
import co.edu.unbosque.proyectofinal.exception.AnalysisRequestException;
import co.edu.unbosque.proyectofinal.model.Analysis;
import co.edu.unbosque.proyectofinal.model.Analysis.Source;
import co.edu.unbosque.proyectofinal.model.Analysis.Status;
import co.edu.unbosque.proyectofinal.model.AnalyzedFile.Category;
import co.edu.unbosque.proyectofinal.model.Audit.Action;
import co.edu.unbosque.proyectofinal.repository.AnalysisRepository;
import co.edu.unbosque.proyectofinal.repository.UserRepository;
import co.edu.unbosque.proyectofinal.service.AnalysisReportService.PdfFile;
import co.edu.unbosque.proyectofinal.util.ZipUtil;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Reglas de negocio de los análisis: qué se puede enviar, cómo se arma la lista de archivos (un zip
 * genera un reporte por cada archivo que trae), el historial con filtros y la descarga de
 * reportes. El historial no se puede borrar.
 */
@Service
public class AnalysisService {

  /** Tamaño máximo de cada archivo dentro de un zip. */
  private static final long MAX_BYTES_EN_ZIP = 20L * 1024 * 1024;

  /** Tamaño máximo descomprimido de un zip. */
  private static final long MAX_TOTAL_ZIP = 200L * 1024 * 1024;

  /** Repositorio de análisis. */
  private final AnalysisRepository analysisRepo;

  /** Repositorio de usuarios. */
  private final UserRepository userRepo;

  /** Procesamiento en segundo plano. */
  private final AnalysisProcessor processor;

  /** Reportes y DTOs. */
  private final AnalysisReportService reportService;

  /** Descarga de repositorios. */
  private final GithubService githubService;

  /** Auditoría. */
  private final AuditService auditService;

  /** Máximo de archivos sueltos por análisis. */
  private final int maxArchivos;

  /** Máximo de archivos que puede generar un análisis al abrir los zip. */
  private final int maxEntradasZip;

  /**
   * Spring inyecta aquí las dependencias.
   *
   * @param analysisRepo repositorio de análisis
   * @param userRepo repositorio de usuarios
   * @param processor procesamiento en segundo plano
   * @param reportService reportes y DTOs
   * @param githubService descarga de repositorios
   * @param auditService auditoría
   * @param maxArchivos máximo de archivos sueltos
   * @param maxEntradasZip máximo de archivos al abrir los zip
   */
  public AnalysisService(
      AnalysisRepository analysisRepo,
      UserRepository userRepo,
      AnalysisProcessor processor,
      AnalysisReportService reportService,
      GithubService githubService,
      AuditService auditService,
      @Value("${app.analysis.max-files:10}") int maxArchivos,
      @Value("${app.analysis.max-zip-entries:30}") int maxEntradasZip) {
    this.analysisRepo = analysisRepo;
    this.userRepo = userRepo;
    this.processor = processor;
    this.reportService = reportService;
    this.githubService = githubService;
    this.auditService = auditService;
    this.maxArchivos = maxArchivos;
    this.maxEntradasZip = maxEntradasZip;
  }

  /**
   * Crea un análisis con archivos subidos. Los .zip se abren y cada archivo interno se analiza por
   * separado.
   *
   * @param username usuario en sesión
   * @param archivos archivos subidos
   * @param titulo título opcional
   * @param ip IP de origen
   * @return análisis creado en estado PENDIENTE
   * @throws AnalysisRequestException si no hay archivos, son demasiados o un zip está dañado
   */
  public AnalysisDTO crearDesdeArchivos(String username, MultipartFile[] archivos, String titulo, String ip)
      throws AnalysisRequestException {
    if (archivos == null || archivos.length == 0) {
      throw new AnalysisRequestException("Debes adjuntar al menos un archivo.");
    }
    if (archivos.length > maxArchivos) {
      throw new AnalysisRequestException("Puedes enviar máximo " + maxArchivos + " archivos por análisis.");
    }
    List<IncomingFileDTO> lista = new ArrayList<>();
    boolean hayZip = false;
    int omitidos = 0;
    for (MultipartFile mf : archivos) {
      if (mf == null || mf.isEmpty()) {
        continue;
      }
      String nombre = nombreSeguro(mf.getOriginalFilename());
      byte[] bytes = leer(mf);
      if ("zip".equals(FileContentService.extension(nombre))) {
        hayZip = true;
        int cupo = maxEntradasZip - lista.size();
        if (cupo <= 0) {
          omitidos++;
          continue;
        }
        try {
          ZipUtil.Resultado r = ZipUtil.descomprimir(bytes, cupo, MAX_BYTES_EN_ZIP, MAX_TOTAL_ZIP, ruta -> true);
          for (IncomingFileDTO interno : r.getArchivos()) {
            interno.setRuta(nombre + "/" + interno.getRuta());
            lista.add(interno);
          }
          omitidos += r.getOmitidosPorLimite() + r.getOmitidosPorTamano();
        } catch (IOException e) {
          throw new AnalysisRequestException("No se pudo abrir el archivo " + nombre + ": " + e.getMessage());
        }
      } else {
        if (lista.size() >= maxEntradasZip) {
          omitidos++;
          continue;
        }
        lista.add(new IncomingFileDTO(nombre, nombre, bytes));
      }
    }
    if (lista.isEmpty()) {
      throw new AnalysisRequestException("Los archivos enviados están vacíos o el zip no trae archivos analizables.");
    }
    String tituloFinal = titulo == null || titulo.isBlank()
        ? (lista.size() == 1 ? lista.get(0).getNombre() : "Análisis de " + lista.size() + " archivos")
        : titulo.trim();
    String mensaje = omitidos > 0
        ? omitidos + " archivo(s) no se analizaron por superar el límite de cantidad o de tamaño."
        : null;
    return iniciar(username, tituloFinal, hayZip ? Source.ZIP : Source.ARCHIVOS, null, lista, mensaje, ip);
  }

  /**
   * Crea un análisis a partir de un repositorio público de GitHub.
   *
   * @param username usuario en sesión
   * @param solicitud URL y restricciones
   * @param ip IP de origen
   * @return análisis creado en estado PENDIENTE
   * @throws AnalysisRequestException si la URL o el repositorio no son válidos
   */
  public AnalysisDTO crearDesdeGithub(String username, GithubAnalysisDTO solicitud, String ip)
      throws AnalysisRequestException {
    if (solicitud == null) {
      throw new AnalysisRequestException("Debes enviar la URL del repositorio.");
    }
    GithubRepositoryDTO repo = githubService.interpretarUrl(solicitud.getUrl());
    ZipUtil.Resultado r = githubService.descargar(solicitud);
    String titulo = solicitud.getTitulo() == null || solicitud.getTitulo().isBlank()
        ? repo.getDueno() + "/" + repo.getNombre()
        : solicitud.getTitulo().trim();
    String mensaje = r.getOmitidosPorLimite() > 0
        ? "Se analizaron los primeros " + r.getArchivos().size() + " archivos; " + r.getOmitidosPorLimite()
            + " quedaron por fuera del límite."
        : null;
    return iniciar(username, titulo, Source.GITHUB, solicitud.getUrl().trim(), r.getArchivos(), mensaje, ip);
  }

  /**
   * Guarda el análisis y lanza el procesamiento en segundo plano.
   *
   * @param username usuario
   * @param titulo título
   * @param origen origen
   * @param url URL del repositorio o null
   * @param archivos archivos a analizar
   * @param mensaje aviso para el usuario o null
   * @param ip IP de origen
   * @return análisis creado
   * @throws AnalysisRequestException si el usuario no existe
   */
  private AnalysisDTO iniciar(String username, String titulo, Source origen, String url,
      List<IncomingFileDTO> archivos, String mensaje, String ip) throws AnalysisRequestException {
    if (!userRepo.existsByUsername(username)) {
      throw new AnalysisRequestException("Usuario no encontrado.");
    }
    Analysis a = new Analysis(username, recortar(titulo, 250), origen);
    a.setTotalArchivos(archivos.size());
    a.setUrlRepositorio(url);
    a.setIpOrigen(ip);
    a.setMensaje(mensaje);
    Analysis guardado = analysisRepo.save(a);
    auditService.registrar(username, Action.ANALISIS_CREADO,
        "Análisis " + guardado.getId() + " (" + origen + ") con " + archivos.size() + " archivo(s)", ip);
    processor.procesar(guardado.getId(), username, archivos);
    AnalysisDTO dto = reportService.buildDto(guardado.getId(), false);
    if (dto != null) {
      dto.setTotalArchivos(archivos.size());
    }
    return dto;
  }

  /**
   * Historial del usuario con filtros opcionales.
   *
   * @param username usuario
   * @param desde fecha mínima de envío
   * @param hasta fecha máxima de envío
   * @param categoria análisis que tengan al menos un archivo de esta categoría
   * @param estado estado del análisis
   * @param nivel nivel general ALTO, MEDIO, BAJO o SIN_RESULTADO
   * @param texto texto en el título o en el nombre de algún archivo
   * @return análisis que cumplen todos los filtros
   */
  public List<AnalysisDTO> historial(String username, LocalDate desde, LocalDate hasta, Category categoria,
      Status estado, String nivel, String texto) {
    return filtrar(reportService.listByUser(username), desde, hasta, categoria, estado, nivel, texto);
  }

  /**
   * Todos los análisis con los mismos filtros, para el administrador.
   *
   * @param desde fecha mínima
   * @param hasta fecha máxima
   * @param categoria categoría de archivo
   * @param estado estado
   * @param nivel nivel general
   * @param texto texto buscado
   * @return análisis filtrados
   */
  public List<AnalysisDTO> todos(LocalDate desde, LocalDate hasta, Category categoria, Status estado,
      String nivel, String texto) {
    return filtrar(reportService.listAll(), desde, hasta, categoria, estado, nivel, texto);
  }

  /**
   * Devuelve los análisis filtrados.
   *
   * @param lista análisis
   * @param desde fecha mínima
   * @param hasta fecha máxima
   * @param categoria categoría
   * @param estado estado
   * @param nivel nivel
   * @param texto texto
   * @return análisis filtrados
   */
  private List<AnalysisDTO> filtrar(List<AnalysisDTO> lista, LocalDate desde, LocalDate hasta, Category categoria,
      Status estado, String nivel, String texto) {
    String buscado = texto == null || texto.isBlank() ? null : texto.trim().toLowerCase(Locale.ROOT);
    List<AnalysisDTO> filtrados = new ArrayList<>();
    for (AnalysisDTO a : lista) {
      LocalDate dia = a.getFechaCreacion().toLocalDate();
      if (desde != null && dia.isBefore(desde)) {
        continue;
      }
      if (hasta != null && dia.isAfter(hasta)) {
        continue;
      }
      if (estado != null && a.getEstado() != estado) {
        continue;
      }
      if (nivel != null && !nivel.isBlank() && !nivel.equalsIgnoreCase(a.getNivel())) {
        continue;
      }
      boolean tieneCategoria = categoria == null;
      boolean tieneTexto = buscado == null || a.getTitulo().toLowerCase(Locale.ROOT).contains(buscado);
      for (AnalyzedFileDTO f : a.getArchivos()) {
        if (f.getCategoria() == categoria) {
          tieneCategoria = true;
        }
        if (!tieneTexto && f.getRuta().toLowerCase(Locale.ROOT).contains(buscado)) {
          tieneTexto = true;
        }
      }
      if (tieneCategoria && tieneTexto) {
        filtrados.add(a);
      }
    }
    return filtrados;
  }

  /**
   * Devuelve el detalle o null si no existe o no es del usuario.
   *
   * @param id análisis
   * @param username usuario en sesión
   * @param admin true si es administrador
   * @return detalle o null si no existe o no es del usuario
   */
  public AnalysisDTO detalle(Long id, String username, boolean admin) {
    return reportService.buildDtoFor(id, username, admin);
  }

  /**
   * Devuelve el PDF consolidado o null.
   *
   * @param id análisis
   * @param username usuario en sesión
   * @param admin true si es administrador
   * @param ip IP de origen
   * @return PDF consolidado o null
   */
  public PdfFile pdfAnalisis(Long id, String username, boolean admin, String ip) {
    PdfFile pdf = reportService.analysisPdf(id, username, admin);
    if (pdf != null) {
      auditService.registrar(username, Action.DESCARGA_REPORTE, "Reporte consolidado del análisis " + id, ip);
    }
    return pdf;
  }

  /**
   * Devuelve el PDF del archivo o null si no existe, no es del usuario o no pertenece al análisis.
   *
   * @param id análisis al que pertenece el archivo
   * @param fileId archivo
   * @param username usuario en sesión
   * @param admin true si es administrador
   * @param ip IP de origen
   * @return PDF del archivo o null si no existe, no es del usuario o no pertenece al análisis
   */
  public PdfFile pdfArchivo(Long id, Long fileId, String username, boolean admin, String ip) {
    PdfFile pdf = reportService.filePdf(id, fileId, username, admin);
    if (pdf != null) {
      auditService.registrar(username, Action.DESCARGA_REPORTE, "Reporte del archivo " + fileId, ip);
    }
    return pdf;
  }

  /**
   * Vuelve a enviar el correo con los reportes.
   *
   * @param id análisis
   * @param username usuario en sesión
   * @param admin true si es administrador
   * @param ip IP de origen
   * @return 0 si se envió, 1 si no existe, 2 si aún no termina, 3 si falló el envío
   */
  public int reenviarCorreo(Long id, String username, boolean admin, String ip) {
    Optional<Analysis> found = admin ? analysisRepo.findById(id) : analysisRepo.findByIdAndUsername(id, username);
    if (found.isEmpty()) {
      return 1;
    }
    Status estado = found.get().getEstado();
    if (estado == Status.PENDIENTE || estado == Status.PROCESANDO) {
      return 2;
    }
    boolean enviado = reportService.sendReportEmail(id);
    auditService.registrar(username, Action.REENVIO_REPORTE, "Reenvío del reporte del análisis " + id, ip);
    return enviado ? 0 : 3;
  }

  /**
   * Límites vigentes, para que el front valide antes de enviar.
   *
   * @return límites y extensiones por defecto
   */
  public Map<String, Object> limites() {
    Map<String, Object> l = new LinkedHashMap<>();
    l.put("maxArchivosPorAnalisis", maxArchivos);
    l.put("maxArchivosDesdeZip", maxEntradasZip);
    l.put("maxArchivosRepositorio", githubService.getMaxArchivos());
    l.put("extensionesRepositorioPorDefecto", githubService.getExtensionesPorDefecto());
    l.put("tiposAceptados", "Cualquier archivo: texto, código, PDF, Word, Excel, PowerPoint, imágenes, audio, video y .zip");
    return l;
  }

  /**
   * Lee el contenido de un archivo subido.
   *
   * @param mf archivo subido
   * @return bytes
   * @throws AnalysisRequestException si no se puede leer
   */
  private byte[] leer(MultipartFile mf) throws AnalysisRequestException {
    try {
      return mf.getBytes();
    } catch (IOException e) {
      throw new AnalysisRequestException("No se pudo leer el archivo " + mf.getOriginalFilename());
    }
  }

  /**
   * Quita rutas y caracteres de control del nombre que envía el navegador.
   *
   * @param original nombre original
   * @return nombre limpio
   */
  private String nombreSeguro(String original) {
    if (original == null || original.isBlank()) {
      return "archivo";
    }
    String nombre = original.replace('\\', '/');
    nombre = nombre.substring(nombre.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}<>\"]", "_");
    return nombre.isBlank() ? "archivo" : recortar(nombre, 200);
  }

  /**
   * Devuelve el texto recortado.
   *
   * @param texto texto
   * @param max longitud máxima
   * @return texto recortado
   */
  private String recortar(String texto, int max) {
    return texto.length() > max ? texto.substring(0, max) : texto;
  }
}
