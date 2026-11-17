package co.edu.unbosque.proyectofinal.service;

import co.edu.unbosque.proyectofinal.dto.FileContentDTO;
import co.edu.unbosque.proyectofinal.dto.ImagePartDTO;
import co.edu.unbosque.proyectofinal.dto.IncomingFileDTO;
import co.edu.unbosque.proyectofinal.exception.AiProviderException;
import co.edu.unbosque.proyectofinal.model.AnalyzedFile.Category;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import javax.imageio.ImageIO;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.tika.Tika;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Convierte cualquier archivo en algo que las tres IAs puedan leer.
 *
 * <ul>
 *   <li>Texto y código: se envía el texto.
 *   <li>PDF y documentos de Office: se extrae el texto con Apache Tika. Si el PDF es escaneado se
 *       envían imágenes de las primeras páginas.
 *   <li>Imágenes: se reducen a 1280 px y se envían a los modelos con visión.
 *   <li>Audio: Gemini recibe el audio original y las demás la transcripción de Whisper.
 *   <li>Video: Gemini recibe el video; las demás, cuatro fotogramas y la transcripción.
 *   <li>Otros binarios: el texto que Tika logre sacar y los metadatos.
 * </ul>
 */
@Service
public class FileContentService {

  /** Logger para dejar registro en consola. */
  private static final Logger log = LoggerFactory.getLogger(FileContentService.class);

  /** Lado mayor máximo de las imágenes enviadas. */
  private static final int MAX_LADO = 1280;

  /** Segundos de los que se toman fotogramas en los videos. */
  private static final double[] SEGUNDOS_FOTOGRAMA = {1, 4, 8, 15};

  /** Extensiones que se tratan como código fuente o configuración. */
  private static final Set<String> CODIGO =
      Set.of(
          "java", "ts", "tsx", "js", "jsx", "mjs", "cjs", "py", "c", "h", "cpp", "hpp", "cc", "cs",
          "go", "rb", "php", "kt", "kts", "swift", "scala", "rs", "dart", "r", "sql", "sh", "bat",
          "ps1", "html", "htm", "css", "scss", "sass", "less", "vue", "svelte", "xml", "json",
          "yml", "yaml", "properties", "gradle", "toml", "ini", "cfg", "conf", "svg", "ipynb",
          "dockerfile", "makefile");

  /** Extensiones de texto plano. */
  private static final Set<String> TEXTO =
      Set.of("txt", "md", "markdown", "csv", "tsv", "log", "rst", "tex", "srt", "vtt");

  /** Extensiones de documentos de oficina. */
  private static final Set<String> DOCUMENTO =
      Set.of("doc", "docx", "odt", "rtf", "ppt", "pptx", "odp", "xls", "xlsx", "ods", "epub", "pages");

  /** Detector de tipos y extractor de texto. */
  private final Tika tika = new Tika();

  /** Cliente de Groq, usado para transcribir audio. */
  private final GroqDetector groq;

  /** Máximo de caracteres de texto que se envían a las IAs. */
  private final int maxCaracteres;

  /**
   * Spring inyecta aquí las dependencias.
   *
   * @param groq cliente de Groq para transcripciones
   * @param maxCaracteres máximo de caracteres por archivo
   */
  public FileContentService(GroqDetector groq, @Value("${app.analysis.max-text-chars:12000}") int maxCaracteres) {
    this.groq = groq;
    this.maxCaracteres = maxCaracteres;
    this.tika.setMaxStringLength(maxCaracteres * 4);
  }

  /**
   * Prepara un archivo para las IAs.
   *
   * @param archivo archivo leído en memoria
   * @return contenido preparado
   */
  public FileContentDTO preparar(IncomingFileDTO archivo) {
    byte[] bytes = archivo.getContenido();
    FileContentDTO dto = new FileContentDTO();
    dto.setNombre(archivo.getNombre());
    dto.setRuta(archivo.getRuta() == null ? archivo.getNombre() : archivo.getRuta());
    dto.setTamanoBytes(bytes.length);
    dto.setHashSha256(DigestUtils.sha256Hex(bytes));
    String mime = detectarMime(bytes, archivo.getNombre());
    Category categoria = clasificar(archivo.getNombre(), mime);
    dto.setCategoria(categoria);
    dto.setMimeType(normalizarMime(mime));

    switch (categoria) {
      case TEXTO, CODIGO -> asignarTexto(dto, decodificar(bytes));
      case PDF -> prepararPdf(dto, bytes);
      case DOCUMENTO -> prepararDocumento(dto, bytes);
      case IMAGEN -> prepararImagen(dto, bytes);
      case AUDIO -> prepararAudio(dto, bytes);
      case VIDEO -> prepararVideo(dto, bytes);
      default -> prepararOtro(dto, bytes);
    }
    return dto;
  }

  /**
   * Clasifica un archivo por extensión y tipo MIME.
   *
   * @param nombre nombre del archivo
   * @param mime tipo MIME detectado
   * @return categoría
   */
  public Category clasificar(String nombre, String mime) {
    String ext = extension(nombre);
    String base = nombre == null ? "" : nombre.toLowerCase(Locale.ROOT);
    if (CODIGO.contains(ext) || base.equals("dockerfile") || base.equals("makefile")) {
      return Category.CODIGO;
    }
    if (TEXTO.contains(ext)) {
      return Category.TEXTO;
    }
    if ("pdf".equals(ext) || "application/pdf".equals(mime)) {
      return Category.PDF;
    }
    if (DOCUMENTO.contains(ext)) {
      return Category.DOCUMENTO;
    }
    if (mime != null) {
      if (mime.startsWith("image/")) {
        return Category.IMAGEN;
      }
      if (mime.startsWith("audio/")) {
        return Category.AUDIO;
      }
      if (mime.startsWith("video/")) {
        return Category.VIDEO;
      }
      if (mime.startsWith("text/")) {
        return Category.TEXTO;
      }
      if (mime.contains("officedocument") || mime.contains("opendocument") || mime.contains("msword")) {
        return Category.DOCUMENTO;
      }
    }
    return Category.OTRO;
  }

  /**
   * Devuelve la extensión en minúsculas sin punto, o vacío.
   *
   * @param nombre nombre del archivo
   * @return extensión en minúsculas sin punto, o vacío
   */
  public static String extension(String nombre) {
    if (nombre == null) {
      return "";
    }
    int punto = nombre.lastIndexOf('.');
    return punto < 0 || punto == nombre.length() - 1 ? "" : nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
  }

  /**
   * Devuelve el tipo MIME detectado por Tika.
   *
   * @param bytes contenido
   * @param nombre nombre del archivo
   * @return tipo MIME detectado por Tika
   */
  private String detectarMime(byte[] bytes, String nombre) {
    try {
      return tika.detect(bytes, nombre);
    } catch (RuntimeException e) {
      return "application/octet-stream";
    }
  }

  /**
   * Ajusta los tipos MIME a los nombres que acepta Gemini.
   *
   * @param mime tipo detectado
   * @return tipo normalizado
   */
  private String normalizarMime(String mime) {
    if (mime == null) {
      return "application/octet-stream";
    }
    return switch (mime) {
      case "audio/mpeg" -> "audio/mp3";
      case "audio/vnd.wave", "audio/x-wav", "audio/wave" -> "audio/wav";
      case "audio/x-flac" -> "audio/flac";
      case "audio/x-aiff" -> "audio/aiff";
      case "video/quicktime" -> "video/mov";
      case "video/x-msvideo" -> "video/avi";
      case "video/x-ms-wmv" -> "video/wmv";
      default -> mime;
    };
  }

  /**
   * Devuelve el texto UTF-8 sin caracteres nulos.
   *
   * @param bytes contenido de texto
   * @return texto UTF-8 sin caracteres nulos
   */
  private String decodificar(byte[] bytes) {
    return new String(bytes, StandardCharsets.UTF_8).replace("\0", "");
  }

  /**
   * Guarda el texto recortando el centro si es muy largo: se conserva el inicio y el final, que es
   * donde suelen notarse las plantillas y conclusiones.
   *
   * @param dto contenido preparado
   * @param texto texto completo
   */
  private void asignarTexto(FileContentDTO dto, String texto) {
    if (texto == null) {
      dto.setTexto("");
      return;
    }
    String limpio = texto.strip();
    if (limpio.length() <= maxCaracteres) {
      dto.setTexto(limpio);
      return;
    }
    int inicio = maxCaracteres * 2 / 3;
    int fin = maxCaracteres - inicio;
    dto.setTexto(limpio.substring(0, inicio) + "\n[... contenido omitido por longitud ...]\n"
        + limpio.substring(limpio.length() - fin));
    dto.setTextoRecortado(true);
  }

  /**
   * Prepara un PDF: guarda el original para Gemini, extrae el texto y, si está escaneado, convierte las primeras páginas en imágenes.
   *
   * @param dto contenido preparado
   * @param bytes PDF original
   */
  private void prepararPdf(FileContentDTO dto, byte[] bytes) {
    dto.setBytesOriginales(bytes);
    String texto = extraerTexto(bytes);
    asignarTexto(dto, texto);
    if (texto == null || texto.isBlank()) {
      dto.setNota("PDF sin texto seleccionable (posiblemente escaneado); se envían imágenes de las primeras páginas.");
      try (PDDocument pdf = Loader.loadPDF(bytes)) {
        PDFRenderer renderer = new PDFRenderer(pdf);
        int paginas = Math.min(3, pdf.getNumberOfPages());
        for (int i = 0; i < paginas; i++) {
          agregarImagen(dto, renderer.renderImageWithDPI(i, 80));
        }
      } catch (IOException | RuntimeException e) {
        log.warn("No se pudieron renderizar páginas de {}: {}", dto.getNombre(), e.getMessage());
      }
    }
  }

  /**
   * Prepara un documento de oficina (Word, Excel, PowerPoint y similares) extrayendo su texto.
   *
   * @param dto contenido preparado
   * @param bytes documento original
   */
  private void prepararDocumento(FileContentDTO dto, byte[] bytes) {
    String texto = extraerTexto(bytes);
    asignarTexto(dto, texto);
    if (texto == null || texto.isBlank()) {
      dto.setNota("No se pudo extraer texto del documento.");
    }
  }

  /**
   * Prepara una imagen para enviarla a las IAs con visión.
   *
   * @param dto contenido preparado
   * @param bytes imagen original
   */
  private void prepararImagen(FileContentDTO dto, byte[] bytes) {
    try {
      BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
      if (img != null) {
        agregarImagen(dto, img);
        dto.setNota("Imagen de " + img.getWidth() + "x" + img.getHeight() + " px.");
        return;
      }
    } catch (IOException | RuntimeException e) {
      log.warn("No se pudo leer la imagen {}: {}", dto.getNombre(), e.getMessage());
    }
    // Formatos que Java no lee (webp, heic): se envían tal cual.
    dto.getImagenes().add(new ImagePartDTO(dto.getMimeType(), Base64.getEncoder().encodeToString(bytes)));
  }

  /**
   * Prepara un audio: guarda el original y su transcripción.
   *
   * @param dto contenido preparado
   * @param bytes audio original
   */
  private void prepararAudio(FileContentDTO dto, byte[] bytes) {
    dto.setBytesOriginales(bytes);
    String transcripcion = transcribir(dto, bytes);
    if (transcripcion != null && !transcripcion.isBlank()) {
      asignarTexto(dto, transcripcion);
      dto.setNota("El texto es la transcripción automática del audio.");
    } else {
      dto.setTexto("");
      dto.setNota("No fue posible transcribir el audio; se analiza con los metadatos disponibles.");
    }
  }

  /**
   * Prepara un video: guarda el original, toma fotogramas y transcribe el audio.
   *
   * @param dto contenido preparado
   * @param bytes video original
   */
  private void prepararVideo(FileContentDTO dto, byte[] bytes) {
    dto.setBytesOriginales(bytes);
    File temporal = null;
    try {
      temporal = Files.createTempFile("detectoria-", "." + extension(dto.getNombre())).toFile();
      Files.write(temporal.toPath(), bytes);
      for (double segundo : SEGUNDOS_FOTOGRAMA) {
        try {
          Picture cuadro = FrameGrab.getFrameAtSec(temporal, segundo);
          if (cuadro == null) {
            break;
          }
          agregarImagen(dto, AWTUtil.toBufferedImage(cuadro));
        } catch (Exception e) {
          // El video es más corto que este segundo o el codec no es compatible.
          break;
        }
      }
    } catch (IOException e) {
      log.warn("No se pudo preparar el video {}: {}", dto.getNombre(), e.getMessage());
    } finally {
      if (temporal != null) {
        try {
          Files.deleteIfExists(temporal.toPath());
        } catch (IOException e) {
          log.debug("No se borro el temporal {}", temporal);
        }
      }
    }
    String transcripcion = transcribir(dto, bytes);
    asignarTexto(dto, transcripcion == null ? "" : transcripcion);
    dto.setNota("Video: se adjuntan " + dto.getImagenes().size() + " fotogramas"
        + (transcripcion == null || transcripcion.isBlank() ? " y no hay transcripción." : " y la transcripción del audio."));
  }

  /**
   * Prepara un archivo de un tipo no reconocido con el texto que se pueda extraer.
   *
   * @param dto contenido preparado
   * @param bytes archivo original
   */
  private void prepararOtro(FileContentDTO dto, byte[] bytes) {
    String texto = extraerTexto(bytes);
    if (texto != null && !texto.isBlank()) {
      asignarTexto(dto, texto);
      dto.setNota("Formato no reconocido; se envía el texto que se pudo extraer.");
    } else {
      dto.setTexto("");
      dto.setNota("Archivo binario sin texto legible; solo se envían nombre, tipo y tamaño.");
    }
  }

  /**
   * Devuelve el texto extraido por Tika, o null.
   *
   * @param bytes archivo
   * @return texto extraido por Tika, o null
   */
  private String extraerTexto(byte[] bytes) {
    try {
      return tika.parseToString(new ByteArrayInputStream(bytes));
    } catch (Exception e) {
      log.debug("Tika no extrajo texto: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Devuelve la transcripción o null si falla.
   *
   * @param dto contenido preparado
   * @param bytes audio o video
   * @return transcripción o null si falla
   */
  private String transcribir(FileContentDTO dto, byte[] bytes) {
    try {
      return groq.transcribir(bytes, dto.getNombre(), dto.getMimeType());
    } catch (AiProviderException e) {
      log.warn("Transcripción fallida para {}: {}", dto.getNombre(), e.getMessage());
      return null;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    }
  }

  /**
   * Reduce la imagen, la pasa a RGB y la agrega como JPEG en base64.
   *
   * @param dto contenido preparado
   * @param original imagen original
   */
  private void agregarImagen(FileContentDTO dto, BufferedImage original) {
    if (original == null) {
      return;
    }
    double escala = Math.min(1.0, (double) MAX_LADO / Math.max(original.getWidth(), original.getHeight()));
    int ancho = Math.max(1, (int) Math.round(original.getWidth() * escala));
    int alto = Math.max(1, (int) Math.round(original.getHeight() * escala));
    BufferedImage rgb = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = rgb.createGraphics();
    try {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g.setColor(Color.WHITE);
      g.fillRect(0, 0, ancho, alto);
      g.drawImage(original, 0, 0, ancho, alto, null);
    } finally {
      g.dispose();
    }
    try {
      ByteArrayOutputStream salida = new ByteArrayOutputStream();
      ImageIO.write(rgb, "jpg", salida);
      dto.getImagenes().add(new ImagePartDTO("image/jpeg", Base64.getEncoder().encodeToString(salida.toByteArray())));
    } catch (IOException e) {
      log.warn("No se pudo codificar una imagen de {}", dto.getNombre());
    }
  }
}
