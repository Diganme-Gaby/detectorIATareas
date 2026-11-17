package co.edu.unbosque.proyectofinal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unbosque.proyectofinal.dto.AiVerdictDTO;
import co.edu.unbosque.proyectofinal.dto.FileContentDTO;
import co.edu.unbosque.proyectofinal.model.AnalyzedFile.Category;
import org.junit.jupiter.api.Test;

/** Pruebas de la instrucción enviada a las IAs y de la lectura de sus respuestas. */
class AiPromptUtilTest {

  /** Constructor vacío de la clase de pruebas. */
  AiPromptUtilTest() {}

  /** Respuesta JSON limpia. */
  @Test
  void interpretaJsonLimpio() {
    AiVerdictDTO v = AiPromptUtil.interpretarRespuesta(
        "{\"porcentaje\": 82, \"veredicto\": \"probablemente_ia\", \"motivos\": [\"a\", \"b\"]}", "m1");
    assertEquals(82.0, v.getPorcentaje());
    assertEquals("PROBABLEMENTE_IA", v.getVeredicto());
    assertEquals(2, v.getMotivos().size());
    assertEquals("m1", v.getModelo());
  }

  /** Algunos modelos envuelven el JSON en un bloque de markdown o agregan texto. */
  @Test
  void interpretaJsonConTextoAlrededor() {
    AiVerdictDTO v = AiPromptUtil.interpretarRespuesta(
        "Claro, aqui esta:\n```json\n{\"porcentaje\": \"35%\", \"motivos\": [\"tono personal\"]}\n```", "m2");
    assertEquals(35.0, v.getPorcentaje());
    assertEquals("PROBABLEMENTE_HUMANO", v.getVeredicto());
  }

  /** Probabilidad entre 0 y 1 se lleva a porcentaje; valores fuera de rango se acotan. */
  @Test
  void normalizaPorcentaje() {
    assertEquals(75.0, AiPromptUtil.interpretarRespuesta("{\"porcentaje\": 0.75}", "m").getPorcentaje());
    assertEquals(100.0, AiPromptUtil.interpretarRespuesta("{\"porcentaje\": 140}", "m").getPorcentaje());
    assertEquals(0.0, AiPromptUtil.interpretarRespuesta("{\"porcentaje\": -5}", "m").getPorcentaje());
  }

  /** Sin motivos queda un motivo por defecto. */
  @Test
  void motivoPorDefecto() {
    AiVerdictDTO v = AiPromptUtil.interpretarRespuesta("{\"porcentaje\": 50}", "m");
    assertEquals(1, v.getMotivos().size());
  }

  /** Respuestas sin JSON o sin porcentaje fallan. */
  @Test
  void rechazaRespuestasInvalidas() {
    assertThrows(IllegalArgumentException.class, () -> AiPromptUtil.interpretarRespuesta("no se", "m"));
    assertThrows(IllegalArgumentException.class, () -> AiPromptUtil.interpretarRespuesta("{\"motivos\": []}", "m"));
    assertThrows(IllegalArgumentException.class, () -> AiPromptUtil.interpretarRespuesta(null, "m"));
  }

  /** Niveles por promedio. */
  @Test
  void niveles() {
    assertEquals("ALTO", AiPromptUtil.nivel(70.0));
    assertEquals("MEDIO", AiPromptUtil.nivel(40.0));
    assertEquals("BAJO", AiPromptUtil.nivel(39.9));
    assertEquals("SIN_RESULTADO", AiPromptUtil.nivel(null));
  }

  /** La instrucción incluye el contenido y el formato de respuesta. */
  @Test
  void construyePrompt() {
    FileContentDTO c = new FileContentDTO();
    c.setNombre("Main.java");
    c.setRuta("src/Main.java");
    c.setCategoria(Category.CODIGO);
    c.setMimeType("text/x-java-source");
    c.setTexto("public class Main {}");
    String prompt = AiPromptUtil.construirPrompt(c, false);
    assertTrue(prompt.contains("public class Main {}"));
    assertTrue(prompt.contains("\"porcentaje\""));
    assertTrue(prompt.contains("comentarios genéricos"));
  }
}
