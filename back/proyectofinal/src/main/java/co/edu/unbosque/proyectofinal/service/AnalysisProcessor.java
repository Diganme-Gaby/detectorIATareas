package co.edu.unbosque.proyectofinal.service;

import co.edu.unbosque.proyectofinal.dto.AiVerdictDTO;
import co.edu.unbosque.proyectofinal.dto.FileContentDTO;
import co.edu.unbosque.proyectofinal.dto.IncomingFileDTO;
import co.edu.unbosque.proyectofinal.exception.AiProviderException;
import co.edu.unbosque.proyectofinal.model.Analysis;
import co.edu.unbosque.proyectofinal.model.Analysis.Status;
import co.edu.unbosque.proyectofinal.model.Audit.Action;
import co.edu.unbosque.proyectofinal.model.AnalyzedFile;
import co.edu.unbosque.proyectofinal.model.ModelResult;
import co.edu.unbosque.proyectofinal.repository.AnalysisRepository;
import co.edu.unbosque.proyectofinal.repository.AnalyzedFileRepository;
import co.edu.unbosque.proyectofinal.repository.ModelResultRepository;
import co.edu.unbosque.proyectofinal.util.AiPromptUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Procesa un análisis en segundo plano: prepara cada archivo, consulta las tres IAs en paralelo,
 * guarda los resultados, calcula promedios y al final envía el correo con los reportes.
 *
 * <p>Cada IA principal tiene una IA de respaldo: Gemini → NVIDIA, Groq → Cohere y OpenRouter →
 * GitHub Models. Si la principal no responde se consulta su respaldo y, si este tampoco, los otros
 * respaldos. Solo cuando ninguno responde el archivo queda sin ese resultado y el usuario recibe un
 * aviso que no menciona cuotas ni tokens.
 */
@Service
public class AnalysisProcessor {

  /** Logger para dejar registro en consola. */
  private static final Logger log = LoggerFactory.getLogger(AnalysisProcessor.class);

  /** Repositorio de análisis. */
  private final AnalysisRepository analysisRepo;

  /** Repositorio de archivos analizados. */
  private final AnalyzedFileRepository fileRepo;

  /** Repositorio de resultados, usado como cache. */
  private final ModelResultRepository resultRepo;

  /** Preparación de archivos. */
  private final FileContentService contentService;

  /** Reportes y correo. */
  private final AnalysisReportService reportService;

  /** Auditoría. */
  private final AuditService auditService;

  /** Las tres IAs principales. */
  private final List<AiDetector> detectores;

  /** IAs de respaldo, en la misma posición que la principal a la que reemplazan. */
  private final List<AiDetector> respaldos;

  /**
   * Hilos donde corre cada análisis. Son pocos para no saturar a las IAs; si llegan más análisis
   * esperan su turno.
   */
  private final ExecutorService analisisPool = Executors.newFixedThreadPool(3);

  /**
   * Hilos para consultar las tres IAs de un archivo en paralelo. Es un pool distinto al de los
   * análisis para que un análisis nunca quede esperando una tarea encolada detrás de si mismo.
   */
  private final ExecutorService iaPool = Executors.newFixedThreadPool(9);

  /**
   * Fallos seguidos de una IA dentro del mismo análisis a partir de los cuales ya no se le envían
   * más archivos. Evita que un zip con muchos archivos espere el tiempo máximo en cada uno cuando la
   * IA está caída o sin conexión.
   */
  private static final int MAX_FALLOS_SEGUIDOS = 2;

  /** Reutilizar resultados de archivos identicos. */
  @Value("${ai.cache.enabled:true}")
  private boolean cacheHabilitada;

  /**
   * Spring inyecta aquí las dependencias.
   *
   * @param analysisRepo repositorio de análisis
   * @param fileRepo repositorio de archivos
   * @param resultRepo repositorio de resultados
   * @param contentService preparación de archivos
   * @param reportService reportes y correo
   * @param auditService auditoría
   * @param gemini cliente de Gemini
   * @param groq cliente de Groq
   * @param openRouter cliente de OpenRouter
   * @param nvidia respaldo de Gemini
   * @param cohere respaldo de Groq
   * @param githubModels respaldo de OpenRouter
   */
  public AnalysisProcessor(
      AnalysisRepository analysisRepo,
      AnalyzedFileRepository fileRepo,
      ModelResultRepository resultRepo,
      FileContentService contentService,
      AnalysisReportService reportService,
      AuditService auditService,
      GeminiDetector gemini,
      GroqDetector groq,
      OpenRouterDetector openRouter,
      NvidiaDetector nvidia,
      CohereDetector cohere,
      GithubModelsDetector githubModels) {
    this.analysisRepo = analysisRepo;
    this.fileRepo = fileRepo;
    this.resultRepo = resultRepo;
    this.contentService = contentService;
    this.reportService = reportService;
    this.auditService = auditService;
    this.detectores = List.of(gemini, groq, openRouter);
    this.respaldos = List.of(nvidia, cohere, githubModels);
  }

  /**
   * Deja el análisis en cola y retorna de inmediato. El controller responde al usuario con el id y
   * el front consulta el estado mientras las IAs trabajan.
   *
   * @param analysisId análisis a procesar
   * @param username dueño
   * @param archivos archivos leídos en memoria
   */
  public void procesar(Long analysisId, String username, List<IncomingFileDTO> archivos) {
    analisisPool.execute(() -> ejecutar(analysisId, username, archivos));
  }

  /**
   * Procesa todos los archivos de un análisis dentro de un hilo del pool.
   *
   * @param analysisId análisis a procesar
   * @param username dueño
   * @param archivos archivos leídos en memoria
   */
  private void ejecutar(Long analysisId, String username, List<IncomingFileDTO> archivos) {
    Optional<Analysis> found = analysisRepo.findById(analysisId);
    if (found.isEmpty()) {
      log.error("No existe el análisis {}", analysisId);
      return;
    }
    Analysis analysis = found.get();
    String ip = analysis.getIpOrigen();
    analysis.setEstado(Status.PROCESANDO);
    analysisRepo.save(analysis);

    Set<String> proveedoresConFallas = new LinkedHashSet<>();
    Map<String, Integer> fallosSeguidos = new ConcurrentHashMap<>();
    List<Double> promedios = new ArrayList<>();
    int archivosConResultado = 0;
    try {
      for (IncomingFileDTO archivo : archivos) {
        AnalyzedFile af = procesarArchivo(analysis, archivo, proveedoresConFallas, fallosSeguidos);
        if (af.getPromedioIA() != null) {
          promedios.add(af.getPromedioIA());
          archivosConResultado++;
        }
      }
      Analysis actual = analysisRepo.findById(analysisId).orElse(analysis);
      actual.setFechaFinalizacion(LocalDateTime.now());
      actual.setPromedioIA(AiPromptUtil.promedio(promedios));
      if (archivosConResultado == 0) {
        actual.setEstado(Status.FALLIDO);
        actual.setMensaje("Ninguno de los modelos pudo analizar los archivos en este momento. Intenta de nuevo más tarde.");
      } else if (!proveedoresConFallas.isEmpty()) {
        actual.setEstado(Status.COMPLETADO_PARCIAL);
        actual.setMensaje("No todos los modelos respondieron (" + String.join(", ", proveedoresConFallas)
            + "). El reporte incluye los resultados de los modelos que sí respondieron.");
      } else {
        actual.setEstado(Status.COMPLETADO);
      }
      analysisRepo.save(actual);
      auditService.registrar(username, Action.ANALISIS_FINALIZADO,
          "Análisis " + analysisId + " terminado en estado " + actual.getEstado(), ip);
    } catch (RuntimeException e) {
      log.error("Falló el análisis {}", analysisId, e);
      analysisRepo.findById(analysisId).ifPresent(a -> {
        a.setEstado(Status.FALLIDO);
        a.setFechaFinalizacion(LocalDateTime.now());
        a.setMensaje("Ocurrió un error inesperado procesando el análisis.");
        analysisRepo.save(a);
      });
    }
    if (!reportService.sendReportEmail(analysisId)) {
      log.warn("El reporte del análisis {} no se pudo enviar por correo", analysisId);
    }
  }

  /**
   * Prepara un archivo, consulta las tres IAs y guarda el resultado.
   *
   * @param analysis análisis al que pertenece
   * @param archivo archivo en memoria
   * @param proveedoresConFallas acumula los proveedores que no respondieron
   * @param fallosSeguidos fallos seguidos de cada IA en este análisis
   * @return archivo guardado
   */
  private AnalyzedFile procesarArchivo(Analysis analysis, IncomingFileDTO archivo, Set<String> proveedoresConFallas,
      Map<String, Integer> fallosSeguidos) {
    AnalyzedFile af = new AnalyzedFile();
    af.setAnalysisId(analysis.getId());
    af.setNombreArchivo(recortar(archivo.getNombre(), 250));
    af.setRuta(recortar(archivo.getRuta() == null ? archivo.getNombre() : archivo.getRuta(), 700));
    FileContentDTO contenido;
    try {
      contenido = contentService.preparar(archivo);
    } catch (RuntimeException e) {
      log.error("No se pudo preparar {}: {}", archivo.getNombre(), e.getMessage());
      af.setCategoria(AnalyzedFile.Category.OTRO);
      af.setMimeType("application/octet-stream");
      af.setTamanoBytes(archivo.getContenido().length);
      return fileRepo.save(af);
    }
    af.setCategoria(contenido.getCategoria());
    af.setMimeType(recortar(contenido.getMimeType(), 250));
    af.setTamanoBytes(contenido.getTamanoBytes());
    af.setHashSha256(contenido.getHashSha256());

    final FileContentDTO preparado = contenido;
    List<CompletableFuture<ModelResult>> tareas = new ArrayList<>();
    for (int i = 0; i < detectores.size(); i++) {
      final int posicion = i;
      tareas.add(CompletableFuture.supplyAsync(() -> evaluar(posicion, preparado, fallosSeguidos), iaPool));
    }
    List<ModelResult> resultados = new ArrayList<>();
    List<Double> porcentajes = new ArrayList<>();
    for (CompletableFuture<ModelResult> tarea : tareas) {
      ModelResult r = tarea.join();
      r.setHashSha256(contenido.getHashSha256());
      resultados.add(r);
      if (r.isExitoso()) {
        porcentajes.add(r.getPorcentajeIA());
      } else {
        proveedoresConFallas.add(r.getProveedor());
      }
    }
    Double promedio = AiPromptUtil.promedio(porcentajes);
    af.setPromedioIA(promedio);
    af.setNivel(AiPromptUtil.nivel(promedio));
    AnalyzedFile guardado = fileRepo.save(af);
    for (ModelResult r : resultados) {
      r.setArchivoId(guardado.getId());
      resultRepo.save(r);
    }
    return guardado;
  }

  /**
   * Obtiene el resultado de una posición: primero la IA principal, luego su respaldo y por último
   * los otros respaldos, hasta que alguno responda.
   *
   * @param posicion posición de la IA principal (0 Gemini, 1 Groq, 2 OpenRouter)
   * @param contenido archivo preparado
   * @param fallosSeguidos fallos seguidos de cada IA en este análisis
   * @return resultado listo para guardar (exitoso o con aviso)
   */
  private ModelResult evaluar(int posicion, FileContentDTO contenido, Map<String, Integer> fallosSeguidos) {
    AiDetector principal = detectores.get(posicion);
    List<AiDetector> candidatos = new ArrayList<>();
    candidatos.add(principal);
    for (int i = 0; i < respaldos.size(); i++) {
      candidatos.add(respaldos.get((posicion + i) % respaldos.size()));
    }
    long inicio = System.currentTimeMillis();
    for (AiDetector candidato : candidatos) {
      if (Thread.currentThread().isInterrupted()) {
        break;
      }
      ModelResult r = intentar(candidato, contenido, fallosSeguidos);
      if (r != null) {
        if (candidato != principal) {
          r.setRespaldoDe(principal.getProveedor());
        }
        if (!r.isDesdeCache()) {
          r.setTiempoMs(System.currentTimeMillis() - inicio);
        }
        return r;
      }
    }
    ModelResult fallido = new ModelResult();
    fallido.setProveedor(principal.getProveedor());
    fallido.setModelo(principal.modeloPara(contenido));
    fallido.setExitoso(false);
    fallido.setMensajeUsuario(mensajeNoDisponible(principal.getProveedor()));
    fallido.setTiempoMs(System.currentTimeMillis() - inicio);
    return fallido;
  }

  /**
   * Consulta una IA. Si el mismo contenido ya lo evaluó el mismo modelo, reutiliza ese resultado.
   * Si la IA ya falló varias veces seguidas en este análisis, no se le envía el archivo.
   *
   * @param detector IA a consultar
   * @param contenido archivo preparado
   * @param fallosSeguidos fallos seguidos de cada IA en este análisis
   * @return resultado exitoso, o null si la IA no respondió
   */
  private ModelResult intentar(AiDetector detector, FileContentDTO contenido, Map<String, Integer> fallosSeguidos) {
    String proveedor = detector.getProveedor();
    String modelo = detector.modeloPara(contenido);
    ModelResult r = new ModelResult();
    r.setProveedor(proveedor);
    r.setModelo(modelo);

    if (cacheHabilitada) {
      Optional<ModelResult> previo =
          resultRepo.findFirstByHashSha256AndProveedorAndModeloAndExitosoTrueOrderByFechaDesc(
              contenido.getHashSha256(), proveedor, modelo);
      if (previo.isPresent()) {
        r.setExitoso(true);
        r.setPorcentajeIA(previo.get().getPorcentajeIA());
        r.setVeredicto(previo.get().getVeredicto());
        r.setMotivos(previo.get().getMotivos());
        r.setDesdeCache(true);
        return r;
      }
    }

    if (fallosSeguidos.getOrDefault(proveedor, 0) >= MAX_FALLOS_SEGUIDOS) {
      return null;
    }

    try {
      AiVerdictDTO v = detector.analizar(contenido);
      r.setExitoso(true);
      r.setPorcentajeIA(v.getPorcentaje());
      r.setVeredicto(v.getVeredicto());
      r.setMotivos(String.join("\n", v.getMotivos()));
      fallosSeguidos.put(proveedor, 0);
      return r;
    } catch (AiProviderException e) {
      log.warn("{} no respondió para {}: {}", proveedor, contenido.getNombre(), e.getMessage());
      fallosSeguidos.merge(proveedor, 1, Integer::sum);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (RuntimeException e) {
      log.error("Error inesperado con {}: {}", proveedor, e.getMessage(), e);
      fallosSeguidos.merge(proveedor, 1, Integer::sum);
    }
    return null;
  }

  /**
   * Aviso para el usuario cuando una IA no responde. No menciona cuotas, tokens ni códigos HTTP.
   *
   * @param proveedor IA que no respondió
   * @return mensaje para el reporte
   */
  private String mensajeNoDisponible(String proveedor) {
    return "El modelo " + proveedor + " y sus modelos de respaldo no están disponibles en este momento,"
        + " por eso este archivo no tiene ese resultado. Puedes volver a enviarlo más tarde.";
  }


  /**
   * Devuelve el texto recortado.
   *
   * @param texto texto
   * @param max longitud máxima
   * @return texto recortado
   */
  private String recortar(String texto, int max) {
    if (texto == null) {
      return null;
    }
    return texto.length() > max ? texto.substring(0, max) : texto;
  }
}
