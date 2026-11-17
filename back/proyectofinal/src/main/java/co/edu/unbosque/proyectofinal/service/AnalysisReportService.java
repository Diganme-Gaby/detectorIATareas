package co.edu.unbosque.proyectofinal.service;

import co.edu.unbosque.proyectofinal.dto.AnalysisDTO;
import co.edu.unbosque.proyectofinal.dto.AnalyzedFileDTO;
import co.edu.unbosque.proyectofinal.dto.ModelResultDTO;
import co.edu.unbosque.proyectofinal.dto.UserDTO;
import co.edu.unbosque.proyectofinal.model.Analysis;
import co.edu.unbosque.proyectofinal.model.AnalyzedFile;
import co.edu.unbosque.proyectofinal.model.ModelResult;
import co.edu.unbosque.proyectofinal.repository.AnalysisRepository;
import co.edu.unbosque.proyectofinal.repository.AnalyzedFileRepository;
import co.edu.unbosque.proyectofinal.repository.ModelResultRepository;
import co.edu.unbosque.proyectofinal.util.AiPromptUtil;
import co.edu.unbosque.proyectofinal.util.EmailSender;
import co.edu.unbosque.proyectofinal.util.PdfReportGenerator;
import co.edu.unbosque.proyectofinal.util.ZipUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Convierte los análisis guardados en DTOs, genera sus PDF y envía el correo con los reportes.
 * Las tablas se unen por id: un archivo guarda el id de su análisis y cada resultado el id de su
 * archivo, así que los datos se arman consultando cada repositorio.
 */
@Service
public class AnalysisReportService {

  /** Logger para dejar registro en consola. */
  private static final Logger log = LoggerFactory.getLogger(AnalysisReportService.class);

  /** Hasta esta cantidad de archivos se adjunta un PDF por archivo; con más, van en un zip. */
  private static final int MAX_ADJUNTOS_SUELTOS = 5;

  /** Repositorio de análisis. */
  private final AnalysisRepository analysisRepo;

  /** Repositorio de archivos analizados. */
  private final AnalyzedFileRepository fileRepo;

  /** Repositorio de las respuestas de cada IA. */
  private final ModelResultRepository resultRepo;

  /** Servicio de usuarios, para el DTO del dueño. */
  private final UserService userService;

  /** Generador de PDF. */
  private final PdfReportGenerator pdf;

  /** Envío de correos. */
  private final EmailSender emailSender;

  /**
   * Spring inyecta aquí las dependencias.
   *
   * @param analysisRepo repositorio de análisis
   * @param fileRepo repositorio de archivos
   * @param resultRepo repositorio de resultados
   * @param userService servicio de usuarios
   * @param pdf generador de PDF
   * @param emailSender envío de correos
   */
  public AnalysisReportService(
      AnalysisRepository analysisRepo,
      AnalyzedFileRepository fileRepo,
      ModelResultRepository resultRepo,
      UserService userService,
      PdfReportGenerator pdf,
      EmailSender emailSender) {
    this.analysisRepo = analysisRepo;
    this.fileRepo = fileRepo;
    this.resultRepo = resultRepo;
    this.userService = userService;
    this.pdf = pdf;
    this.emailSender = emailSender;
  }

  /** PDF listo para enviar al navegador. */
  public static class PdfFile {
    /** Nombre sugerido del archivo. */
    private final String nombre;
    /** Bytes del PDF. */
    private final byte[] contenido;

    /**
     * Spring inyecta aquí las dependencias.
     *
     * @param nombre nombre sugerido
     * @param contenido bytes del PDF
     */
    public PdfFile(String nombre, byte[] contenido) {
      this.nombre = nombre;
      this.contenido = contenido;
    }

    /**
     * Devuelve el nombre sugerido.
     *
     * @return nombre sugerido
     */
    public String getNombre() {
      return nombre;
    }

    /**
     * Devuelve los bytes del PDF.
     *
     * @return bytes del PDF
     */
    public byte[] getContenido() {
      return contenido;
    }
  }

  /**
   * Devuelve el DTO o null si no existe.
   *
   * @param id análisis
   * @param detalle true para incluir los resultados de cada IA
   * @return DTO o null si no existe
   */
  public AnalysisDTO buildDto(Long id, boolean detalle) {
    return analysisRepo.findById(id).map(a -> toDto(a, detalle)).orElse(null);
  }

  /**
   * Devuelve el DTO con detalle, o null si no existe o no es del usuario.
   *
   * @param id análisis
   * @param username usuario que lo pide
   * @param admin true si es administrador (puede ver todos)
   * @return DTO con detalle, o null si no existe o no es del usuario
   */
  public AnalysisDTO buildDtoFor(Long id, String username, boolean admin) {
    Optional<Analysis> found = admin ? analysisRepo.findById(id) : analysisRepo.findByIdAndUsername(id, username);
    return found.map(a -> toDto(a, true)).orElse(null);
  }

  /**
   * Devuelve el historial resumido del usuario.
   *
   * @param username dueño
   * @return historial resumido del usuario
   */
  public List<AnalysisDTO> listByUser(String username) {
    List<AnalysisDTO> lista = new ArrayList<>();
    analysisRepo.findByUsernameOrderByFechaCreacionDesc(username).forEach(a -> lista.add(toDto(a, false)));
    return lista;
  }

  /**
   * Devuelve todos los análisis resumidos, para el administrador.
   *
   * @return todos los análisis resumidos, para el administrador
   */
  public List<AnalysisDTO> listAll() {
    List<AnalysisDTO> lista = new ArrayList<>();
    analysisRepo.findAllByOrderByFechaCreacionDesc().forEach(a -> lista.add(toDto(a, false)));
    return lista;
  }

  /**
   * PDF consolidado de un análisis.
   *
   * @param id análisis
   * @param username usuario que lo pide
   * @param admin true si es administrador
   * @return PDF o null si no existe o no es del usuario
   */
  public PdfFile analysisPdf(Long id, String username, boolean admin) {
    Optional<Analysis> found = admin ? analysisRepo.findById(id) : analysisRepo.findByIdAndUsername(id, username);
    if (found.isEmpty()) {
      return null;
    }
    Analysis a = found.get();
    byte[] bytes = pdf.reporteAnalisis(toDto(a, true), userService.getByUsername(a.getUsername()));
    return new PdfFile("reporte-analisis-" + a.getId() + ".pdf", bytes);
  }

  /**
   * PDF de un archivo. El archivo debe pertenecer al análisis indicado.
   *
   * @param analysisId análisis al que pertenece el archivo
   * @param fileId archivo
   * @param username usuario que lo pide
   * @param admin true si es administrador
   * @return PDF o null si no existe o no es del usuario
   */
  public PdfFile filePdf(Long analysisId, Long fileId, String username, boolean admin) {
    Optional<AnalyzedFile> found = fileRepo.findById(fileId);
    if (found.isEmpty() || !found.get().getAnalysisId().equals(analysisId)) {
      return null;
    }
    AnalyzedFile f = found.get();
    Optional<Analysis> padre = admin
        ? analysisRepo.findById(f.getAnalysisId())
        : analysisRepo.findByIdAndUsername(f.getAnalysisId(), username);
    if (padre.isEmpty()) {
      return null;
    }
    Analysis a = padre.get();
    UserDTO dueno = userService.getByUsername(a.getUsername());
    byte[] bytes = pdf.reporteArchivo(toDto(a, false), toFileDto(f, true), dueno);
    return new PdfFile(nombrePdf(f), bytes);
  }

  /**
   * Genera los reportes y los envía al correo del dueño. Siempre va el consolidado. Si el análisis
   * tiene entre dos y cinco archivos se adjunta además un PDF por archivo; con más, van en un zip.
   *
   * @param id análisis
   * @return true si el correo salio
   */
  public boolean sendReportEmail(Long id) {
    Optional<Analysis> found = analysisRepo.findById(id);
    if (found.isEmpty()) {
      return false;
    }
    Analysis a = found.get();
    AnalysisDTO dto = toDto(a, true);
    UserDTO user = userService.getByUsername(a.getUsername());
    if (user == null) {
      return false;
    }
    Map<String, byte[]> adjuntos = new LinkedHashMap<>();
    try {
      adjuntos.put("reporte-analisis-" + a.getId() + ".pdf", pdf.reporteAnalisis(dto, user));
      Map<String, byte[]> porArchivo = new LinkedHashMap<>();
      // Con un solo archivo el consolidado ya trae su detalle completo; no se repite el PDF.
      if (dto.getArchivos().size() > 1) {
        for (AnalyzedFileDTO f : dto.getArchivos()) {
          porArchivo.put(nombreUnico(porArchivo, f), pdf.reporteArchivo(dto, f, user));
        }
      }
      if (porArchivo.size() <= MAX_ADJUNTOS_SUELTOS) {
        adjuntos.putAll(porArchivo);
      } else {
        adjuntos.put("reportes-por-archivo-" + a.getId() + ".zip", ZipUtil.comprimir(porArchivo));
      }
    } catch (IOException | RuntimeException e) {
      log.error("No se pudieron generar los adjuntos del análisis {}: {}", id, e.getMessage());
    }
    boolean enviado = emailSender.enviarReporte(user.getEmail(), user.getNombreCompleto(), dto, adjuntos);
    if (enviado) {
      a.setCorreoEnviado(true);
      analysisRepo.save(a);
    }
    return enviado;
  }

  /**
   * Convierte un análisis en DTO.
   *
   * @param a entidad
   * @param detalle true para incluir los resultados de cada IA
   * @return DTO
   */
  public AnalysisDTO toDto(Analysis a, boolean detalle) {
    AnalysisDTO dto = new AnalysisDTO();
    dto.setId(a.getId());
    dto.setUsername(a.getUsername());
    dto.setTitulo(a.getTitulo());
    dto.setOrigen(a.getOrigen());
    dto.setUrlRepositorio(a.getUrlRepositorio());
    dto.setEstado(a.getEstado());
    dto.setFechaCreacion(a.getFechaCreacion());
    dto.setFechaFinalizacion(a.getFechaFinalizacion());
    dto.setPromedioIA(a.getPromedioIA());
    dto.setNivel(AiPromptUtil.nivel(a.getPromedioIA()));
    dto.setCorreoEnviado(a.isCorreoEnviado());
    dto.setMensaje(a.getMensaje());
    List<AnalyzedFileDTO> archivos = new ArrayList<>();
    for (AnalyzedFile f : fileRepo.findByAnalysisIdOrderByIdAsc(a.getId())) {
      archivos.add(toFileDto(f, detalle));
    }
    dto.setArchivos(archivos);
    dto.setTotalArchivos(archivos.isEmpty() ? a.getTotalArchivos() : archivos.size());
    return dto;
  }

  /**
   * Convierte un archivo en DTO.
   *
   * @param f entidad
   * @param detalle true para incluir los resultados de cada IA
   * @return DTO
   */
  public AnalyzedFileDTO toFileDto(AnalyzedFile f, boolean detalle) {
    AnalyzedFileDTO dto = new AnalyzedFileDTO();
    dto.setId(f.getId());
    dto.setNombreArchivo(f.getNombreArchivo());
    dto.setRuta(f.getRuta());
    dto.setCategoria(f.getCategoria());
    dto.setMimeType(f.getMimeType());
    dto.setTamanoBytes(f.getTamanoBytes());
    dto.setPromedioIA(f.getPromedioIA());
    dto.setNivel(f.getNivel());
    List<ModelResultDTO> resultados = new ArrayList<>();
    List<String> avisos = new ArrayList<>();
    for (ModelResult r : resultRepo.findByArchivoIdOrderByIdAsc(f.getId())) {
      if (!r.isExitoso() && r.getMensajeUsuario() != null) {
        avisos.add(r.getMensajeUsuario());
      }
      if (detalle) {
        resultados.add(toResultDto(r));
      }
    }
    dto.setResultados(resultados);
    dto.setAvisos(avisos);
    return dto;
  }

  /**
   * Devuelve el DTO con los motivos separados en lista.
   *
   * @param r entidad
   * @return DTO con los motivos separados en lista
   */
  private ModelResultDTO toResultDto(ModelResult r) {
    ModelResultDTO dto = new ModelResultDTO();
    dto.setId(r.getId());
    dto.setProveedor(r.getProveedor());
    dto.setRespaldoDe(r.getRespaldoDe());
    dto.setModelo(r.getModelo());
    dto.setPorcentajeIA(r.getPorcentajeIA());
    dto.setVeredicto(r.getVeredicto());
    if (r.getMotivos() != null && !r.getMotivos().isBlank()) {
      dto.setMotivos(new ArrayList<>(Arrays.asList(r.getMotivos().split("\n"))));
    }
    dto.setExitoso(r.isExitoso());
    dto.setMensajeUsuario(r.getMensajeUsuario());
    dto.setTiempoMs(r.getTiempoMs());
    dto.setDesdeCache(r.isDesdeCache());
    return dto;
  }

  /**
   * Devuelve el nombre del PDF de un archivo.
   *
   * @param f archivo
   * @return nombre del PDF de un archivo
   */
  private String nombrePdf(AnalyzedFile f) {
    return "reporte-" + f.getId() + "-" + limpiarNombre(f.getNombreArchivo()) + ".pdf";
  }

  /**
   * Devuelve el nombre de PDF que no se repite dentro del correo.
   *
   * @param usados nombres ya usados
   * @param f archivo
   * @return nombre de PDF que no se repite dentro del correo
   */
  private String nombreUnico(Map<String, byte[]> usados, AnalyzedFileDTO f) {
    String base = "reporte-" + f.getId() + "-" + limpiarNombre(f.getNombreArchivo());
    String nombre = base + ".pdf";
    int i = 2;
    while (usados.containsKey(nombre)) {
      nombre = base + "-" + i++ + ".pdf";
    }
    return nombre;
  }

  /**
   * Devuelve el nombre sin caracteres problematicos para un archivo.
   *
   * @param nombre nombre original
   * @return nombre sin caracteres problematicos para un archivo
   */
  private String limpiarNombre(String nombre) {
    String limpio = nombre == null ? "archivo" : nombre.replaceAll("[^A-Za-z0-9._-]", "_");
    return limpio.length() > 60 ? limpio.substring(0, 60) : limpio;
  }
}
