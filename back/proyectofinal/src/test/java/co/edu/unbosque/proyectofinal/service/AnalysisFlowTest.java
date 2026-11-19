package co.edu.unbosque.proyectofinal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import co.edu.unbosque.proyectofinal.dto.AiVerdictDTO;
import co.edu.unbosque.proyectofinal.dto.AnalysisDTO;
import co.edu.unbosque.proyectofinal.dto.AnalyzedFileDTO;
import co.edu.unbosque.proyectofinal.dto.ModelResultDTO;
import co.edu.unbosque.proyectofinal.exception.AiProviderException;
import co.edu.unbosque.proyectofinal.exception.AnalysisRequestException;
import co.edu.unbosque.proyectofinal.model.Analysis.Status;
import co.edu.unbosque.proyectofinal.service.AnalysisReportService.PdfFile;
import co.edu.unbosque.proyectofinal.util.EmailSender;
import co.edu.unbosque.proyectofinal.util.ZipUtil;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

/**
 * Prueba de punta a punta del análisis con las tres IAs y sus respaldos simulados: varios archivos,
 * zip con un reporte por archivo, una IA que falla, un respaldo que responde y generación de los PDF.
 */
@SpringBootTest
class AnalysisFlowTest {

  /** Servicio de análisis. */
  @Autowired private AnalysisService analysisService;

  /** Gemini simulado. */
  @MockitoBean private GeminiDetector gemini;

  /** Groq simulado. */
  @MockitoBean private GroqDetector groq;

  /** OpenRouter simulado. */
  @MockitoBean private OpenRouterDetector openRouter;

  /** NVIDIA simulado (respaldo de Gemini). */
  @MockitoBean private NvidiaDetector nvidia;

  /** Cohere simulado (respaldo de Groq). */
  @MockitoBean private CohereDetector cohere;

  /** GitHub Models simulado (respaldo de OpenRouter). */
  @MockitoBean private GithubModelsDetector githubModels;

  /** Envío de correos simulado para no mandar correos reales. */
  @MockitoBean private EmailSender emailSender;

  /** Constructor vacío de la clase de pruebas. */
  AnalysisFlowTest() {}

  /**
   * Devuelve el veredicto simulado.
   *
   * @param porcentaje porcentaje simulado
   * @param modelo modelo simulado
   * @return veredicto simulado
   */
  private AiVerdictDTO veredicto(double porcentaje, String modelo) {
    AiVerdictDTO v = new AiVerdictDTO();
    v.setPorcentaje(porcentaje);
    v.setVeredicto(porcentaje >= 70 ? "PROBABLEMENTE_IA" : "PROBABLEMENTE_HUMANO");
    v.setMotivos(List.of("Motivo de prueba de " + modelo));
    v.setModelo(modelo);
    return v;
  }

  /**
   * Configura las IAs: Gemini 90, Groq 60, OpenRouter falla y los respaldos también fallan.
   *
   * @throws Exception si falla la petición simulada o una aserción interna
   */
  @BeforeEach
  void setUp() throws Exception {
    when(gemini.getProveedor()).thenReturn("Gemini");
    when(groq.getProveedor()).thenReturn("Groq");
    when(openRouter.getProveedor()).thenReturn("OpenRouter");
    when(gemini.modeloPara(any())).thenReturn("gemini-test");
    when(groq.modeloPara(any())).thenReturn("groq-test");
    when(openRouter.modeloPara(any())).thenReturn("openrouter-test");
    when(gemini.analizar(any())).thenReturn(veredicto(90, "gemini-test"));
    when(groq.analizar(any())).thenReturn(veredicto(60, "groq-test"));
    when(openRouter.analizar(any())).thenThrow(new AiProviderException("OpenRouter", 429, "rate limit tokens"));
    when(nvidia.getProveedor()).thenReturn("NVIDIA");
    when(cohere.getProveedor()).thenReturn("Cohere");
    when(githubModels.getProveedor()).thenReturn("GitHub Models");
    when(nvidia.modeloPara(any())).thenReturn("nvidia-test");
    when(cohere.modeloPara(any())).thenReturn("cohere-test");
    when(githubModels.modeloPara(any())).thenReturn("github-test");
    when(nvidia.analizar(any())).thenThrow(new AiProviderException("NVIDIA", 503, "sin servicio"));
    when(cohere.analizar(any())).thenThrow(new AiProviderException("Cohere", 429, "rate limit"));
    when(githubModels.analizar(any())).thenThrow(new AiProviderException("GitHub Models", 429, "rate limit"));
    when(emailSender.enviarReporte(anyString(), anyString(), any(), anyMap())).thenReturn(true);
  }

  /**
   * Espera a que el procesamiento en segundo plano termine.
   *
   * @param id análisis
   * @return detalle final
   * @throws InterruptedException si se interrumpe la espera
   */
  private AnalysisDTO esperar(Long id) throws InterruptedException {
    for (int i = 0; i < 100; i++) {
      AnalysisDTO dto = analysisService.detalle(id, "normaluser", false);
      if (dto.getEstado() != Status.PENDIENTE && dto.getEstado() != Status.PROCESANDO) {
        return dto;
      }
      Thread.sleep(100);
    }
    throw new AssertionError("El análisis no terminó a tiempo");
  }

  /**
   * Dos archivos sueltos: promedio de las que respondieron y aviso de la que falló.
   *
   * @throws Exception si falla la petición simulada o una aserción interna
   */
  @Test
  void analizaVariosArchivosConUnaIaCaida() throws Exception {
    MultipartFile[] archivos = {
      new MockMultipartFile("archivos", "ensayo.txt", "text/plain", "Un ensayo cualquiera".getBytes(StandardCharsets.UTF_8)),
      new MockMultipartFile("archivos", "Main.java", "text/plain", "class Main {}".getBytes(StandardCharsets.UTF_8))
    };
    AnalysisDTO creado = analysisService.crearDesdeArchivos("normaluser", archivos, "Prueba", "127.0.0.1");
    assertNotNull(creado.getId());

    AnalysisDTO fin = esperar(creado.getId());
    assertEquals(Status.COMPLETADO_PARCIAL, fin.getEstado());
    assertEquals(2, fin.getArchivos().size());
    AnalyzedFileDTO archivo = fin.getArchivos().get(0);
    assertEquals(75.0, archivo.getPromedioIA());
    assertEquals("ALTO", archivo.getNivel());
    assertEquals(3, archivo.getResultados().size());
    assertFalse(archivo.getAvisos().isEmpty());
    String aviso = archivo.getAvisos().get(0).toLowerCase();
    assertTrue(aviso.contains("openrouter"));
    assertFalse(aviso.contains("token") || aviso.contains("cuota") || aviso.contains("429"),
        "El aviso al usuario no debe hablar de tokens ni cuotas");

    PdfFile pdf = analysisService.pdfAnalisis(fin.getId(), "normaluser", false, "127.0.0.1");
    assertTrue(new String(pdf.getContenido(), 0, 5, StandardCharsets.US_ASCII).startsWith("%PDF"));
    PdfFile pdfArchivo = analysisService.pdfArchivo(fin.getId(), archivo.getId(), "normaluser", false, "127.0.0.1");
    assertNotNull(pdfArchivo);
    assertEquals(null, analysisService.pdfArchivo(fin.getId(), archivo.getId(), "admin-falso", false, "127.0.0.1"));
    assertEquals(null, analysisService.pdfArchivo(fin.getId() + 1000, archivo.getId(), "normaluser", false, "127.0.0.1"),
        "El archivo debe pertenecer al análisis indicado");
  }

  /**
   * Si OpenRouter no responde, su respaldo GitHub Models ocupa su lugar y el análisis queda completo.
   *
   * @throws Exception si falla la petición simulada o una aserción interna
   */
  @Test
  void respaldoRespondeCuandoFallaLaPrincipal() throws Exception {
    doReturn(veredicto(30, "github-test")).when(githubModels).analizar(any());
    String texto = "Texto unico para el respaldo " + System.nanoTime();
    MultipartFile[] archivos = {
      new MockMultipartFile("archivos", "respaldo.txt", "text/plain", texto.getBytes(StandardCharsets.UTF_8))
    };
    AnalysisDTO fin = esperar(analysisService.crearDesdeArchivos("normaluser", archivos, null, "127.0.0.1").getId());
    assertEquals(Status.COMPLETADO, fin.getEstado());
    AnalyzedFileDTO archivo = fin.getArchivos().get(0);
    assertEquals(60.0, archivo.getPromedioIA());
    assertTrue(archivo.getAvisos().isEmpty());
    ModelResultDTO respaldo = archivo.getResultados().get(2);
    assertTrue(respaldo.isExitoso());
    assertEquals("GitHub Models", respaldo.getProveedor());
    assertEquals("OpenRouter", respaldo.getRespaldoDe());
    assertEquals("OpenRouter", respaldo.getPosicion());
    PdfFile pdf = analysisService.pdfAnalisis(fin.getId(), "normaluser", false, "127.0.0.1");
    assertTrue(new String(pdf.getContenido(), 0, 5, StandardCharsets.US_ASCII).startsWith("%PDF"));
  }

  /**
   * Un zip genera un archivo analizado (y un reporte) por cada archivo que trae.
   *
   * @throws Exception si falla la petición simulada o una aserción interna
   */
  @Test
  void zipGeneraUnReportePorArchivo() throws Exception {
    Map<String, byte[]> contenido = new LinkedHashMap<>();
    contenido.put("a.txt", "Primero".getBytes(StandardCharsets.UTF_8));
    contenido.put("carpeta/b.py", "print('hola')".getBytes(StandardCharsets.UTF_8));
    contenido.put("carpeta/c.md", "# Titulo".getBytes(StandardCharsets.UTF_8));
    MultipartFile[] archivos = {
      new MockMultipartFile("archivos", "tarea.zip", "application/zip", ZipUtil.comprimir(contenido))
    };
    AnalysisDTO fin = esperar(analysisService.crearDesdeArchivos("normaluser", archivos, null, "127.0.0.1").getId());
    assertEquals(3, fin.getArchivos().size());
    assertEquals("tarea.zip/carpeta/b.py", fin.getArchivos().get(1).getRuta());
    assertFalse(analysisService.historial("normaluser", null, null, null, null, "ALTO", "tarea").isEmpty());
  }

  /** Validaciones de la solicitud. */
  @Test
  void validaSolicitud() {
    assertThrows(AnalysisRequestException.class,
        () -> analysisService.crearDesdeArchivos("normaluser", new MultipartFile[0], null, "127.0.0.1"));
    MultipartFile[] muchos = new MultipartFile[11];
    for (int i = 0; i < muchos.length; i++) {
      muchos[i] = new MockMultipartFile("archivos", "f" + i + ".txt", "text/plain", new byte[] {'x'});
    }
    assertThrows(AnalysisRequestException.class,
        () -> analysisService.crearDesdeArchivos("normaluser", muchos, null, "127.0.0.1"));
  }
}
