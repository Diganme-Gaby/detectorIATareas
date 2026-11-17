package co.edu.unbosque.proyectofinal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import co.edu.unbosque.proyectofinal.dto.FileContentDTO;
import co.edu.unbosque.proyectofinal.dto.IncomingFileDTO;
import co.edu.unbosque.proyectofinal.exception.AiProviderException;
import co.edu.unbosque.proyectofinal.model.AnalyzedFile.Category;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Pruebas de la preparación de archivos para las IAs. */
class FileContentServiceTest {

  /** Groq simulado para no llamar a la API real. */
  private final GroqDetector groq = Mockito.mock(GroqDetector.class);

  /** Servicio con un límite pequeño de texto. */
  private final FileContentService service = new FileContentService(groq, 300);

  /** Constructor vacío de la clase de pruebas. */
  FileContentServiceTest() {}

  /** Código fuente: se envía el texto y se calcula el hash. */
  @Test
  void preparaCodigo() {
    byte[] bytes = "public class Hola { }".getBytes(StandardCharsets.UTF_8);
    FileContentDTO c = service.preparar(new IncomingFileDTO("Hola.java", "src/Hola.java", bytes));
    assertEquals(Category.CODIGO, c.getCategoria());
    assertEquals("public class Hola { }", c.getTexto());
    assertEquals(64, c.getHashSha256().length());
    assertEquals("src/Hola.java", c.getRuta());
  }

  /** Textos largos se recortan conservando inicio y final. */
  @Test
  void recortaTextoLargo() {
    String largo = "A".repeat(500) + "FINAL";
    FileContentDTO c = service.preparar(new IncomingFileDTO("ensayo.txt", null, largo.getBytes(StandardCharsets.UTF_8)));
    assertTrue(c.isTextoRecortado());
    assertTrue(c.getTexto().endsWith("FINAL"));
    assertTrue(c.getTexto().contains("omitido"));
  }

  /**
   * Imágenes: se reducen y se envían como JPEG.
   *
   * @throws Exception si falla la petición simulada o una aserción interna
   */
  @Test
  void preparaImagen() throws Exception {
    BufferedImage img = new BufferedImage(2000, 1000, BufferedImage.TYPE_INT_ARGB);
    ByteArrayOutputStream salida = new ByteArrayOutputStream();
    ImageIO.write(img, "png", salida);
    FileContentDTO c = service.preparar(new IncomingFileDTO("foto.png", null, salida.toByteArray()));
    assertEquals(Category.IMAGEN, c.getCategoria());
    assertEquals(1, c.getImagenes().size());
    assertEquals("image/jpeg", c.getImagenes().get(0).getMimeType());
  }

  /**
   * Audio: el texto es la transcripción de Whisper.
   *
   * @throws Exception si falla la petición simulada o una aserción interna
   */
  @Test
  void preparaAudioConTranscripcion() throws Exception {
    when(groq.transcribir(any(), anyString(), anyString())).thenReturn("hola esta es una prueba");
    byte[] mp3 = new byte[] {'I', 'D', '3', 3, 0, 0, 0, 0, 0, 0};
    FileContentDTO c = service.preparar(new IncomingFileDTO("nota.mp3", null, mp3));
    assertEquals(Category.AUDIO, c.getCategoria());
    assertEquals("hola esta es una prueba", c.getTexto());
    assertTrue(c.getBytesOriginales().length > 0);
  }

  /**
   * Si la transcripción falla el archivo igual queda listo, con una nota.
   *
   * @throws Exception si falla la petición simulada o una aserción interna
   */
  @Test
  void audioSinTranscripcion() throws Exception {
    when(groq.transcribir(any(), anyString(), anyString())).thenThrow(new AiProviderException("Groq", 500, "x"));
    FileContentDTO c = service.preparar(new IncomingFileDTO("nota.wav", null, new byte[] {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'A', 'V', 'E'}));
    assertEquals(Category.AUDIO, c.getCategoria());
    assertFalse(c.getNota().isBlank());
  }

  /** Clasificación por extensión. */
  @Test
  void clasifica() {
    assertEquals(Category.DOCUMENTO, service.clasificar("informe.docx", "application/zip"));
    assertEquals(Category.PDF, service.clasificar("informe.pdf", "application/pdf"));
    assertEquals(Category.TEXTO, service.clasificar("notas.md", "text/plain"));
    assertEquals(Category.VIDEO, service.clasificar("clip.mp4", "video/mp4"));
    assertEquals(Category.OTRO, service.clasificar("programa.exe", "application/x-msdownload"));
  }
}
