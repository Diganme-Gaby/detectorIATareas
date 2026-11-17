package co.edu.unbosque.proyectofinal.service;

import co.edu.unbosque.proyectofinal.dto.StatsDTO;
import co.edu.unbosque.proyectofinal.model.Analysis;
import co.edu.unbosque.proyectofinal.model.AnalyzedFile;
import co.edu.unbosque.proyectofinal.model.Audit;
import co.edu.unbosque.proyectofinal.model.Audit.Action;
import co.edu.unbosque.proyectofinal.model.ModelResult;
import co.edu.unbosque.proyectofinal.repository.AnalysisRepository;
import co.edu.unbosque.proyectofinal.repository.AnalyzedFileRepository;
import co.edu.unbosque.proyectofinal.repository.AuditRepository;
import co.edu.unbosque.proyectofinal.repository.ModelResultRepository;
import co.edu.unbosque.proyectofinal.repository.UserRepository;
import co.edu.unbosque.proyectofinal.util.AiPromptUtil;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Calcula las estadísticas del módulo administrativo: cuántas personas usan el aplicativo, qué días
 * se usa más, qué tipos de archivo se analizan y cómo se comporta cada IA.
 */
@Service
public class StatsService {

  /** Días que cubren las series por día. */
  private static final int DIAS = 30;

  /** Proveedores en el orden en que se muestran: las tres principales y luego sus respaldos. */
  private static final String[] PROVEEDORES = {
    GeminiDetector.PROVEEDOR, GroqDetector.PROVEEDOR, OpenRouterDetector.PROVEEDOR,
    NvidiaDetector.PROVEEDOR, CohereDetector.PROVEEDOR, GithubModelsDetector.PROVEEDOR
  };

  /** Repositorio de usuarios. */
  private final UserRepository userRepo;

  /** Repositorio de análisis. */
  private final AnalysisRepository analysisRepo;

  /** Repositorio de archivos analizados. */
  private final AnalyzedFileRepository fileRepo;

  /** Repositorio de auditoría. */
  private final AuditRepository auditRepo;

  /** Repositorio de resultados. */
  private final ModelResultRepository resultRepo;

  /**
   * Spring inyecta aquí las dependencias.
   *
   * @param userRepo repositorio de usuarios
   * @param analysisRepo repositorio de análisis
   * @param fileRepo repositorio de archivos
   * @param auditRepo repositorio de auditoría
   * @param resultRepo repositorio de resultados
   */
  public StatsService(UserRepository userRepo, AnalysisRepository analysisRepo, AnalyzedFileRepository fileRepo,
      AuditRepository auditRepo, ModelResultRepository resultRepo) {
    this.userRepo = userRepo;
    this.analysisRepo = analysisRepo;
    this.fileRepo = fileRepo;
    this.auditRepo = auditRepo;
    this.resultRepo = resultRepo;
  }

  /**
   * Devuelve las estadísticas actuales.
   *
   * @return estadísticas actuales
   */
  public StatsDTO calcular() {
    StatsDTO s = new StatsDTO();
    s.setTotalUsuarios(userRepo.count());
    s.setUsuariosActivos(userRepo.countByActivo(true));
    s.setUsuariosVerificados(userRepo.countByCorreoVerificado(true));

    List<Analysis> analisis = analysisRepo.findAll();
    s.setTotalAnalisis(analisis.size());

    LocalDate hoy = LocalDate.now();
    Map<String, Long> porDia = serieVacia(hoy);
    Map<String, Long> porSemana = new LinkedHashMap<>();
    for (DayOfWeek d : DayOfWeek.values()) {
      porSemana.put(nombreDia(d), 0L);
    }
    Map<String, Long> porUsuario = new HashMap<>();
    Map<String, Long> porCategoria = new LinkedHashMap<>();
    Map<String, Long> porNivel = new LinkedHashMap<>();
    for (String n : new String[] {"ALTO", "MEDIO", "BAJO", "SIN_RESULTADO"}) {
      porNivel.put(n, 0L);
    }
    List<Double> promedios = new ArrayList<>();
    long totalArchivos = 0;

    for (Analysis a : analisis) {
      LocalDate dia = a.getFechaCreacion().toLocalDate();
      porDia.computeIfPresent(dia.toString(), (k, v) -> v + 1);
      porSemana.merge(nombreDia(dia.getDayOfWeek()), 1L, Long::sum);
      porUsuario.merge(a.getUsername(), 1L, Long::sum);
    }
    for (AnalyzedFile f : fileRepo.findAll()) {
      totalArchivos++;
      porCategoria.merge(String.valueOf(f.getCategoria()), 1L, Long::sum);
      porNivel.merge(AiPromptUtil.nivel(f.getPromedioIA()), 1L, Long::sum);
      if (f.getPromedioIA() != null) {
        promedios.add(f.getPromedioIA());
      }
    }
    s.setTotalArchivos(totalArchivos);
    s.setPromedioGeneralIA(AiPromptUtil.promedio(promedios));
    s.setAnalisisPorDia(porDia);
    s.setUsoPorDiaSemana(porSemana);
    s.setArchivosPorCategoria(porCategoria);
    s.setArchivosPorNivel(porNivel);
    s.setTopUsuarios(topUsuarios(porUsuario, 10));

    Map<String, Long> logins = serieVacia(hoy);
    for (Audit au : auditRepo.findByFechaAfter(hoy.minusDays(DIAS).atStartOfDay())) {
      if (au.getAccion() == Action.LOGIN_EXITOSO) {
        logins.computeIfPresent(au.getFecha().toLocalDate().toString(), (k, v) -> v + 1);
      }
    }
    s.setLoginsPorDia(logins);

    Map<String, Double> promedioProveedor = new LinkedHashMap<>();
    Map<String, Long> fallos = new LinkedHashMap<>();
    for (String p : PROVEEDORES) {
      List<ModelResult> resultados = resultRepo.findByProveedor(p);
      long fallidos = 0;
      List<Double> porcentajes = new ArrayList<>();
      for (ModelResult r : resultados) {
        if (!r.isExitoso()) {
          fallidos++;
        } else if (r.getPorcentajeIA() != null) {
          porcentajes.add(r.getPorcentajeIA());
        }
      }
      fallos.put(p, fallidos);
      promedioProveedor.put(p, AiPromptUtil.promedio(porcentajes));
    }
    s.setPromedioPorProveedor(promedioProveedor);
    s.setFallosPorProveedor(fallos);
    return s;
  }

  /**
   * Devuelve el mapa con los últimos 30 días en cero.
   *
   * @param hoy fecha final
   * @return mapa con los últimos 30 días en cero
   */
  private Map<String, Long> serieVacia(LocalDate hoy) {
    Map<String, Long> serie = new LinkedHashMap<>();
    for (int i = DIAS - 1; i >= 0; i--) {
      serie.put(hoy.minusDays(i).toString(), 0L);
    }
    return serie;
  }

  /**
   * Devuelve el nombre en español.
   *
   * @param d día de la semana
   * @return nombre en español
   */
  private String nombreDia(DayOfWeek d) {
    return d.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-CO"));
  }

  /**
   * Los usuarios con más análisis, de mayor a menor.
   *
   * @param porUsuario análisis por usuario
   * @param limite cuántos devolver
   * @return mapa ordenado
   */
  private Map<String, Long> topUsuarios(Map<String, Long> porUsuario, int limite) {
    Map<String, Long> pendientes = new HashMap<>(porUsuario);
    Map<String, Long> top = new LinkedHashMap<>();
    while (!pendientes.isEmpty() && top.size() < limite) {
      String mayor = null;
      for (Map.Entry<String, Long> e : pendientes.entrySet()) {
        if (mayor == null || e.getValue() > pendientes.get(mayor)) {
          mayor = e.getKey();
        }
      }
      top.put(mayor, pendientes.remove(mayor));
    }
    return top;
  }
}
