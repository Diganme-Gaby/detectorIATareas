package co.edu.unbosque.proyectofinal.util;

import co.edu.unbosque.proyectofinal.dto.AnalysisDTO;
import co.edu.unbosque.proyectofinal.dto.AnalyzedFileDTO;
import co.edu.unbosque.proyectofinal.dto.AuditDTO;
import co.edu.unbosque.proyectofinal.dto.ChartImageDTO;
import co.edu.unbosque.proyectofinal.dto.ModelResultDTO;
import co.edu.unbosque.proyectofinal.dto.StatsDTO;
import co.edu.unbosque.proyectofinal.dto.UserDTO;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Genera todos los PDF del aplicativo con OpenPDF: el reporte por archivo, el consolidado de un
 * análisis y los reportes administrativos (usuarios, auditoría y estadísticas).
 *
 * <p>El diseño es sobrio, como un documento institucional: fondo blanco, líneas finas y los colores
 * de la Universidad El Bosque (verde y naranja) solo en títulos y detalles. Usa la fuente Carlito,
 * que va dentro del proyecto en {@code resources/fonts} con su licencia libre (SIL Open Font
 * License). Si por algún motivo no se puede cargar, los reportes salen con Helvetica.
 */
@Component
public class PdfReportGenerator {

  /** Logger para dejar registro en consola. */
  private static final Logger log = LoggerFactory.getLogger(PdfReportGenerator.class);

  /** Formato de fecha de los reportes. */
  private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  /** Verde institucional: títulos y línea del encabezado. */
  private static final Color VERDE = new Color(0, 107, 63);

  /** Naranja institucional: línea fina del encabezado y del pie. */
  private static final Color NARANJA = new Color(232, 119, 34);

  /** Naranja oscuro: avisos, legible sobre fondo blanco. */
  private static final Color NARANJA_TEXTO = new Color(176, 82, 10);

  /** Rojo oscuro: texto del nivel alto. */
  private static final Color ROJO_TEXTO = new Color(160, 40, 30);

  /** Color del texto normal. */
  private static final Color TEXTO = new Color(33, 33, 33);

  /** Color del texto secundario. */
  private static final Color TEXTO_SUAVE = new Color(105, 105, 105);

  /** Color de las líneas finas de las tablas. */
  private static final Color LINEA = new Color(205, 205, 205);

  /** Carlito normal. */
  private static final BaseFont BASE_NORMAL = cargarFuente("Carlito-Regular.ttf");

  /** Carlito en negrita. */
  private static final BaseFont BASE_NEGRITA = cargarFuente("Carlito-Bold.ttf");

  /** Carlito en cursiva. */
  private static final BaseFont BASE_CURSIVA = cargarFuente("Carlito-Italic.ttf");

  /** Fuente de la línea institucional encima del título. */
  private static final Font INSTITUCION = fuente(BASE_NEGRITA, Font.BOLD, 8.5f, VERDE);

  /** Fuente del título principal. */
  private static final Font TITULO = fuente(BASE_NEGRITA, Font.BOLD, 17, TEXTO);

  /** Fuente del subtítulo del encabezado. */
  private static final Font SUBTITULO_ENCABEZADO = fuente(BASE_NORMAL, Font.NORMAL, 11, TEXTO_SUAVE);

  /** Fuente de los subtítulos de sección. */
  private static final Font SUBTITULO = fuente(BASE_NEGRITA, Font.BOLD, 11.5f, VERDE);

  /** Fuente normal. */
  private static final Font NORMAL = fuente(BASE_NORMAL, Font.NORMAL, 10, TEXTO);

  /** Fuente pequeña. */
  private static final Font PEQUENA = fuente(BASE_NORMAL, Font.NORMAL, 8, TEXTO_SUAVE);

  /** Fuente en negrita. */
  private static final Font NEGRITA = fuente(BASE_NEGRITA, Font.BOLD, 10, TEXTO);

  /** Fuente de las etiquetas de las tablas de datos. */
  private static final Font ETIQUETA = fuente(BASE_NEGRITA, Font.BOLD, 9.5f, TEXTO_SUAVE);

  /** Fuente de las celdas de encabezado. */
  private static final Font ENCABEZADO = fuente(BASE_NEGRITA, Font.BOLD, 9, VERDE);

  /** Fuente de las celdas de tablas grandes. */
  private static final Font CELDA = fuente(BASE_NORMAL, Font.NORMAL, 9, TEXTO);

  /** Fuente de los avisos. */
  private static final Font AVISO_FUENTE = fuente(BASE_CURSIVA, Font.ITALIC, 9.5f, NARANJA_TEXTO);

  /** Las tres posiciones del reporte, una por IA principal. */
  private static final String[] POSICIONES = {"Gemini", "Groq", "OpenRouter"};

  /** Aviso sobre el alcance de los resultados. */
  private static final String AVISO =
      "Los porcentajes son estimaciones de modelos de inteligencia artificial y no constituyen una"
          + " prueba concluyente de autoría. Se recomienda revisarlos junto con otros criterios.";

  /** Constructor vacío. */
  public PdfReportGenerator() {}

  /**
   * Reporte de un solo archivo.
   *
   * @param analisis análisis al que pertenece
   * @param archivo archivo con sus resultados
   * @param usuario dueño del análisis
   * @return bytes del PDF
   */
  public byte[] reporteArchivo(AnalysisDTO analisis, AnalyzedFileDTO archivo, UserDTO usuario) {
    return generar(
        false,
        "Reporte de archivo",
        doc -> {
          encabezado(doc, "Reporte de detección de IA", archivo.getNombreArchivo());
          seccionArchivo(doc, analisis, archivo, usuario);
          pie(doc);
        });
  }

  /**
   * Reporte consolidado: resumen de todos los archivos y luego el detalle de cada uno.
   *
   * @param analisis análisis completo
   * @param usuario dueño del análisis
   * @return bytes del PDF
   */
  public byte[] reporteAnalisis(AnalysisDTO analisis, UserDTO usuario) {
    return generar(
        false,
        "Reporte de análisis",
        doc -> {
          encabezado(doc, "Reporte consolidado de detección de IA", analisis.getTitulo());
          PdfPTable info = tablaDatos();
          filaDato(info, "Usuario", usuario.getNombreCompleto() + " (" + usuario.getUsername() + ")");
          filaDato(info, "Correo", usuario.getEmail());
          filaDato(info, "Origen", origen(String.valueOf(analisis.getOrigen())));
          if (analisis.getUrlRepositorio() != null) {
            filaDato(info, "Repositorio", analisis.getUrlRepositorio());
          }
          filaDato(info, "Enviado", fecha(analisis.getFechaCreacion()));
          filaDato(info, "Finalizado", fecha(analisis.getFechaFinalizacion()));
          filaDato(info, "Estado", legible(String.valueOf(analisis.getEstado())));
          filaDato(info, "Archivos", String.valueOf(analisis.getTotalArchivos()));

          filaResultado(info, "Promedio general de IA", analisis.getPromedioIA(), analisis.getNivel());
          doc.add(info);
          if (analisis.getMensaje() != null) {
            Paragraph aviso = new Paragraph(analisis.getMensaje(), AVISO_FUENTE);
            aviso.setSpacingBefore(6f);
            doc.add(aviso);
          }

          doc.add(subtitulo("Resumen por archivo"));
          PdfPTable resumen = new PdfPTable(new float[] {4.2f, 1.4f, 1.3f, 1.3f, 1.7f, 1.6f});
          resumen.setWidthPercentage(100);
          resumen.setHeaderRows(1);
          resumen.addCell(celdaEncabezado("Archivo"));
          resumen.addCell(celdaEncabezado("Tipo"));
          for (String h : new String[] {"Gemini", "Groq", "OpenRouter", "Promedio"}) {
            PdfPCell c = celdaEncabezado(h);
            c.setHorizontalAlignment(Element.ALIGN_RIGHT);
            resumen.addCell(c);
          }
          for (AnalyzedFileDTO a : analisis.getArchivos()) {
            resumen.addCell(celda(a.getRuta(), Element.ALIGN_LEFT));
            resumen.addCell(celda(categoria(String.valueOf(a.getCategoria())), Element.ALIGN_LEFT));
            for (String proveedor : POSICIONES) {
              resumen.addCell(celda(porcentajeDe(a, proveedor), Element.ALIGN_RIGHT));
            }
            PdfPCell promedio = celda(porcentaje(a.getPromedioIA()), Element.ALIGN_RIGHT);
            promedio.setPhrase(new Phrase(porcentaje(a.getPromedioIA()), fuenteNivel(a.getNivel())));
            resumen.addCell(promedio);
          }
          doc.add(resumen);
          Paragraph nd = new Paragraph("N/D: ni la IA ni sus respaldos estuvieron disponibles para ese archivo."
              + "  * Respondió el modelo de respaldo de esa IA.", PEQUENA);
          nd.setSpacingBefore(4f);
          doc.add(nd);

          for (AnalyzedFileDTO a : analisis.getArchivos()) {
            doc.newPage();
            Paragraph nombre = subtitulo(a.getRuta());
            nombre.setSpacingBefore(0f);
            nombre.setSpacingAfter(8f);
            doc.add(nombre);
            seccionArchivo(doc, analisis, a, usuario);
          }
          pie(doc);
        });
  }

  /**
   * Reporte de todos los usuarios para el administrador.
   *
   * @param usuarios usuarios a listar
   * @param generadoPor administrador que lo pide
   * @return bytes del PDF
   */
  public byte[] reporteUsuarios(List<UserDTO> usuarios, String generadoPor) {
    return generar(
        true,
        "Reporte de usuarios",
        doc -> {
          encabezado(doc, "Reporte de usuarios", "Generado por " + generadoPor);
          doc.add(new Paragraph("Total de usuarios: " + usuarios.size(), NEGRITA));
          doc.add(Chunk.NEWLINE);
          PdfPTable t = new PdfPTable(new float[] {0.6f, 1.6f, 2.4f, 2.8f, 1.4f, 0.9f, 1f, 1f, 1.6f, 1.6f});
          t.setWidthPercentage(100);
          t.setHeaderRows(1);
          for (String h :
              new String[] {
                "ID", "Usuario", "Nombre", "Correo", "Teléfono", "Rol", "Activo", "Verificado",
                "Registro", "Último acceso"
              }) {
            t.addCell(celdaEncabezado(h));
          }
          for (UserDTO u : usuarios) {
            t.addCell(celda(String.valueOf(u.getId()), Element.ALIGN_LEFT));
            t.addCell(celda(u.getUsername(), Element.ALIGN_LEFT));
            t.addCell(celda(u.getNombreCompleto(), Element.ALIGN_LEFT));
            t.addCell(celda(u.getEmail(), Element.ALIGN_LEFT));
            t.addCell(celda(u.getTelefono(), Element.ALIGN_LEFT));
            t.addCell(celda(String.valueOf(u.getRole()), Element.ALIGN_LEFT));
            t.addCell(celda(u.isActivo() ? "Sí" : "No", Element.ALIGN_LEFT));
            t.addCell(celda(u.isCorreoVerificado() ? "Sí" : "No", Element.ALIGN_LEFT));
            t.addCell(celda(fecha(u.getFechaRegistro()), Element.ALIGN_LEFT));
            t.addCell(celda(fecha(u.getUltimoAcceso()), Element.ALIGN_LEFT));
          }
          doc.add(t);
        });
  }

  /**
   * Reporte de la auditoría con los filtros que usó el administrador.
   *
   * @param registros registros de auditoría
   * @param filtros descripción de los filtros aplicados
   * @param generadoPor administrador que lo pide
   * @return bytes del PDF
   */
  public byte[] reporteAuditoria(List<AuditDTO> registros, String filtros, String generadoPor) {
    return generar(
        true,
        "Reporte de auditoría",
        doc -> {
          encabezado(doc, "Reporte de auditoría", "Generado por " + generadoPor);
          doc.add(new Paragraph("Filtros: " + filtros, NORMAL));
          doc.add(new Paragraph("Registros: " + registros.size(), NEGRITA));
          doc.add(Chunk.NEWLINE);
          PdfPTable t = new PdfPTable(new float[] {1.5f, 1.4f, 2f, 4.2f, 1.6f, 2.4f});
          t.setWidthPercentage(100);
          t.setHeaderRows(1);
          for (String h : new String[] {"Fecha", "Usuario", "Acción", "Descripción", "IP", "Ubicación"}) {
            t.addCell(celdaEncabezado(h));
          }
          for (AuditDTO a : registros) {
            t.addCell(celda(fecha(a.getFecha()), Element.ALIGN_LEFT));
            t.addCell(celda(a.getUsername(), Element.ALIGN_LEFT));
            t.addCell(celda(legible(String.valueOf(a.getAccion())), Element.ALIGN_LEFT));
            t.addCell(celda(a.getDescripcion(), Element.ALIGN_LEFT));
            t.addCell(celda(a.getIp(), Element.ALIGN_LEFT));
            t.addCell(celda(ubicacion(a), Element.ALIGN_LEFT));
          }
          doc.add(t);
        });
  }

  /**
   * Reporte de estadísticas con tablas y, si el front las envía, las gráficas de Chart.js.
   *
   * @param stats estadísticas calculadas
   * @param graficas imágenes de las gráficas, puede ser vacía
   * @param generadoPor administrador que lo pide
   * @return bytes del PDF
   */
  public byte[] reporteEstadisticas(StatsDTO stats, List<ChartImageDTO> graficas, String generadoPor) {
    return generar(
        false,
        "Reporte de estadísticas",
        doc -> {
          encabezado(doc, "Estadísticas de uso", "Generado por " + generadoPor);
          PdfPTable kpi = tablaDatos();
          filaDato(kpi, "Usuarios registrados", String.valueOf(stats.getTotalUsuarios()));
          filaDato(kpi, "Usuarios activos", String.valueOf(stats.getUsuariosActivos()));
          filaDato(kpi, "Correos verificados", String.valueOf(stats.getUsuariosVerificados()));
          filaDato(kpi, "Análisis realizados", String.valueOf(stats.getTotalAnalisis()));
          filaDato(kpi, "Archivos analizados", String.valueOf(stats.getTotalArchivos()));
          filaDato(kpi, "Promedio general de IA", porcentaje(stats.getPromedioGeneralIA()));
          doc.add(kpi);

          if (graficas != null) {
            for (ChartImageDTO g : graficas) {
              agregarGrafica(doc, g);
            }
          }
          tablaMapa(doc, "Análisis por día (últimos 30 días)", "Día", stats.getAnalisisPorDia());
          tablaMapa(doc, "Uso por día de la semana", "Día", stats.getUsoPorDiaSemana());
          tablaMapa(doc, "Inicios de sesión por día", "Día", stats.getLoginsPorDia());
          tablaMapa(doc, "Archivos por tipo", "Tipo", stats.getArchivosPorCategoria());
          tablaMapa(doc, "Archivos por nivel de IA", "Nivel", stats.getArchivosPorNivel());
          tablaMapa(doc, "Usuarios con más análisis", "Usuario", stats.getTopUsuarios());
          tablaMapa(doc, "Veces que cada IA no respondió", "IA", stats.getFallosPorProveedor());

          doc.add(subtitulo("Porcentaje promedio que da cada IA"));
          PdfPTable t = new PdfPTable(2);
          t.setWidthPercentage(60);
          t.setHorizontalAlignment(Element.ALIGN_LEFT);
          t.addCell(celdaEncabezado("IA"));
          t.addCell(celdaEncabezado("Promedio"));
          for (Map.Entry<String, Double> e : stats.getPromedioPorProveedor().entrySet()) {
            t.addCell(celda(e.getKey(), Element.ALIGN_LEFT));
            t.addCell(celda(porcentaje(e.getValue()), Element.ALIGN_LEFT));
          }
          doc.add(t);
        });
  }

  /** Bloque de contenido que se escribe dentro de un documento abierto. */
  private interface Contenido {
    /**
     * Escribe el contenido del reporte dentro del documento.
     *
     * @param doc documento abierto
     * @throws Exception si OpenPDF no puede escribir
     */
    void escribir(Document doc) throws Exception;
  }

  /**
   * Abre el documento, escribe el contenido y devuelve los bytes.
   *
   * @param horizontal true para hoja horizontal
   * @param titulo título de los metadatos del PDF
   * @param contenido contenido a escribir
   * @return bytes del PDF
   */
  private byte[] generar(boolean horizontal, String titulo, Contenido contenido) {
    ByteArrayOutputStream salida = new ByteArrayOutputStream();
    Document doc = new Document(horizontal ? PageSize.A4.rotate() : PageSize.A4, 36, 36, 36, 54);
    try {
      PdfWriter writer = PdfWriter.getInstance(doc, salida);
      writer.setPageEvent(new PiePagina());
      doc.addTitle(titulo);
      doc.addAuthor("Detector de IA - Universidad El Bosque");
      doc.open();
      contenido.escribir(doc);
    } catch (Exception e) {
      log.error("Error generando el PDF '{}': {}", titulo, e.getMessage());
      throw new IllegalStateException("No se pudo generar el PDF", e);
    } finally {
      if (doc.isOpen()) {
        doc.close();
      }
    }
    return salida.toByteArray();
  }

  /**
   * Datos, resultado, tabla de cada IA y motivos de un archivo.
   *
   * @param doc documento abierto
   * @param analisis análisis al que pertenece
   * @param archivo archivo a describir
   * @param usuario dueño
   * @throws Exception si OpenPDF no puede escribir
   */
  private void seccionArchivo(Document doc, AnalysisDTO analisis, AnalyzedFileDTO archivo, UserDTO usuario)
      throws Exception {
    PdfPTable info = tablaDatos();
    filaDato(info, "Usuario", usuario.getNombreCompleto() + " (" + usuario.getUsername() + ")");
    filaDato(info, "Análisis", analisis.getTitulo() + " - " + fecha(analisis.getFechaCreacion()));
    filaDato(info, "Archivo", archivo.getNombreArchivo());
    if (!archivo.getRuta().equals(archivo.getNombreArchivo())) {
      filaDato(info, "Ruta", archivo.getRuta());
    }
    filaDato(info, "Tipo", categoria(String.valueOf(archivo.getCategoria())) + " (" + archivo.getMimeType() + ")");
    filaDato(info, "Tamaño", tamano(archivo.getTamanoBytes()));
    filaResultado(info, "Promedio de IA", archivo.getPromedioIA(), archivo.getNivel());
    doc.add(info);

    doc.add(subtitulo("Resultado de cada IA"));
    PdfPTable t = new PdfPTable(new float[] {1.3f, 1.9f, 3f, 1f, 2f});
    t.setWidthPercentage(100);
    t.setHeaderRows(1);
    for (String h : new String[] {"IA", "Respondió", "Modelo", "% IA", "Veredicto"}) {
      PdfPCell c = celdaEncabezado(h);
      if ("% IA".equals(h)) {
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
      }
      t.addCell(c);
    }
    for (ModelResultDTO r : archivo.getResultados()) {
      t.addCell(celda(r.getPosicion(), Element.ALIGN_LEFT));
      t.addCell(celda(quienRespondio(r), Element.ALIGN_LEFT));
      t.addCell(celda(r.isExitoso() ? r.getModelo() : "-", Element.ALIGN_LEFT));
      t.addCell(celda(r.isExitoso() ? porcentaje(r.getPorcentajeIA()) : "-", Element.ALIGN_RIGHT));
      PdfPCell v = celda(r.isExitoso() ? veredicto(r.getVeredicto()) : "-", Element.ALIGN_LEFT);
      v.setPaddingLeft(12f);
      t.addCell(v);
    }
    doc.add(t);

    for (ModelResultDTO r : archivo.getResultados()) {
      String titulo = "Motivos de " + r.getPosicion();
      if (r.getRespaldoDe() != null) {
        titulo += " (respondió " + r.getProveedor() + ")";
      }
      doc.add(subtitulo(titulo));
      if (!r.isExitoso()) {
        doc.add(new Paragraph(r.getMensajeUsuario(), AVISO_FUENTE));
        continue;
      }
      com.lowagie.text.List lista = new com.lowagie.text.List(true, 14f);
      lista.setListSymbol(new Chunk("", NORMAL));
      for (String motivo : r.getMotivos()) {
        com.lowagie.text.ListItem item = new com.lowagie.text.ListItem(motivo, NORMAL);
        item.setSpacingAfter(2f);
        lista.add(item);
      }
      doc.add(lista);
    }
  }

  /**
   * Devuelve la IA principal, el respaldo que la reemplazó o "No disponible".
   *
   * @param r resultado de una posición
   * @return la IA principal, el respaldo que la reemplazó o "No disponible"
   */
  private String quienRespondio(ModelResultDTO r) {
    if (!r.isExitoso()) {
      return "No disponible";
    }
    if (r.getRespaldoDe() != null) {
      return r.getProveedor() + " (respaldo)";
    }
    return r.getProveedor();
  }

  /**
   * Encabezado del documento: nombre de la institución, título, subtítulo, una línea verde con una
   * línea naranja fina debajo y la fecha de generación.
   *
   * @param doc documento abierto
   * @param titulo título
   * @param subtitulo subtítulo
   * @throws Exception si OpenPDF no puede escribir
   */
  private void encabezado(Document doc, String titulo, String subtitulo) throws Exception {
    Paragraph institucion = new Paragraph("UNIVERSIDAD EL BOSQUE  ·  DETECTOR DE CONTENIDO GENERADO CON IA", INSTITUCION);
    institucion.setSpacingAfter(4f);
    doc.add(institucion);
    doc.add(new Paragraph(titulo, TITULO));
    if (subtitulo != null && !subtitulo.isBlank()) {
      Paragraph sub = new Paragraph(subtitulo, SUBTITULO_ENCABEZADO);
      sub.setSpacingBefore(1f);
      doc.add(sub);
    }
    PdfPTable lineas = new PdfPTable(1);
    lineas.setWidthPercentage(100);
    lineas.setSpacingBefore(8f);
    lineas.addCell(linea(VERDE, 1.4f));
    lineas.addCell(linea(NARANJA, 0.6f));
    doc.add(lineas);

    Paragraph generado = new Paragraph("Generado el " + LocalDateTime.now().format(FECHA), PEQUENA);
    generado.setAlignment(Element.ALIGN_RIGHT);
    generado.setSpacingAfter(8f);
    doc.add(generado);
  }

  /**
   * Devuelve la celda vacía que solo dibuja una línea horizontal.
   *
   * @param color color de la línea
   * @param grosor grosor en puntos
   * @return celda vacía que solo dibuja una línea horizontal
   */
  private PdfPCell linea(Color color, float grosor) {
    PdfPCell c = new PdfPCell();
    c.setBorder(Rectangle.BOTTOM);
    c.setBorderColor(color);
    c.setBorderWidth(grosor);
    c.setFixedHeight(2f);
    c.setPadding(0f);
    return c;
  }

  /**
   * Agrega a la tabla de datos la fila del resultado: porcentaje y nivel, con el nivel en el color
   * que le corresponde.
   *
   * @param t tabla de datos
   * @param etiqueta texto de la etiqueta
   * @param promedio promedio o null
   * @param nivel nivel ALTO, MEDIO, BAJO o SIN_RESULTADO
   */
  private void filaResultado(PdfPTable t, String etiqueta, Double promedio, String nivel) {
    PdfPCell e = new PdfPCell(new Phrase(etiqueta, ETIQUETA));
    estiloDato(e);
    t.addCell(e);
    Phrase valor = new Phrase();
    valor.add(new Chunk(porcentaje(promedio), NEGRITA));
    if (promedio != null) {
      valor.add(new Chunk("    Nivel: ", NORMAL));
      valor.add(new Chunk(nivel(nivel), fuenteNivel(nivel)));
    }
    PdfPCell v = new PdfPCell(valor);
    estiloDato(v);
    t.addCell(v);
  }

  /**
   * Aviso final sobre el alcance de los resultados.
   *
   * @param doc documento abierto
   * @throws Exception si OpenPDF no puede escribir
   */
  private void pie(Document doc) throws Exception {
    Paragraph p = new Paragraph(AVISO, PEQUENA);
    p.setSpacingBefore(16f);
    doc.add(p);
  }

  /**
   * Devuelve el párrafo verde con una línea gris fina debajo.
   *
   * @param texto texto del subtítulo
   * @return párrafo verde con una línea gris fina debajo
   */
  private Paragraph subtitulo(String texto) {
    Paragraph p = new Paragraph(texto, SUBTITULO);
    p.setSpacingBefore(14f);
    p.add(Chunk.NEWLINE);
    p.add(new Chunk(new com.lowagie.text.pdf.draw.LineSeparator(0.5f, 100f, LINEA, Element.ALIGN_LEFT, 4f)));
    p.setSpacingAfter(2f);
    return p;
  }

  /**
   * Devuelve la tabla de dos columnas para pares etiqueta-valor.
   *
   * @return tabla de dos columnas para pares etiqueta-valor
   */
  private PdfPTable tablaDatos() {
    PdfPTable t = new PdfPTable(new float[] {1.3f, 4f});
    t.setWidthPercentage(100);
    return t;
  }

  /**
   * Agrega a la tabla una fila con una etiqueta y su valor.
   *
   * @param t tabla de datos
   * @param etiqueta etiqueta
   * @param valor valor
   */
  private void filaDato(PdfPTable t, String etiqueta, String valor) {
    PdfPCell e = new PdfPCell(new Phrase(etiqueta, ETIQUETA));
    estiloDato(e);
    t.addCell(e);
    PdfPCell v = new PdfPCell(new Phrase(valor == null ? "-" : valor, NORMAL));
    estiloDato(v);
    t.addCell(v);
  }

  /**
   * Celdas de la tabla de datos: sin fondo y solo con una línea fina abajo.
   *
   * @param c celda
   */
  private void estiloDato(PdfPCell c) {
    c.setBorder(Rectangle.BOTTOM);
    c.setBorderColor(LINEA);
    c.setBorderWidth(0.5f);
    c.setPaddingTop(4f);
    c.setPaddingBottom(5f);
    c.setPaddingLeft(2f);
  }

  /**
   * Devuelve la celda de encabezado con el texto en verde y una línea verde abajo.
   *
   * @param texto texto del encabezado
   * @return celda de encabezado con el texto en verde y una línea verde abajo
   */
  private PdfPCell celdaEncabezado(String texto) {
    PdfPCell c = new PdfPCell(new Phrase(texto, ENCABEZADO));
    c.setBorder(Rectangle.BOTTOM);
    c.setBorderColor(VERDE);
    c.setBorderWidth(1f);
    c.setHorizontalAlignment(Element.ALIGN_LEFT);
    c.setVerticalAlignment(Element.ALIGN_BOTTOM);
    c.setPaddingTop(4f);
    c.setPaddingBottom(5f);
    c.setPaddingLeft(3f);
    c.setPaddingRight(6f);
    return c;
  }

  /**
   * Devuelve la celda de datos sin fondo, con una línea fina abajo.
   *
   * @param texto contenido
   * @param alineacion alineación horizontal
   * @return celda de datos sin fondo, con una línea fina abajo
   */
  private PdfPCell celda(String texto, int alineacion) {
    PdfPCell c = new PdfPCell(new Phrase(texto == null ? "-" : texto, CELDA));
    c.setBorder(Rectangle.BOTTOM);
    c.setBorderColor(LINEA);
    c.setBorderWidth(0.5f);
    c.setHorizontalAlignment(alineacion);
    c.setVerticalAlignment(Element.ALIGN_MIDDLE);
    c.setPaddingTop(4f);
    c.setPaddingBottom(5f);
    c.setPaddingLeft(3f);
    c.setPaddingRight(6f);
    return c;
  }

  /**
   * Agrega una tabla con los pares de un mapa.
   *
   * @param doc documento abierto
   * @param titulo título de la tabla
   * @param columna nombre de la primera columna
   * @param datos datos a mostrar
   * @throws Exception si OpenPDF no puede escribir
   */
  private void tablaMapa(Document doc, String titulo, String columna, Map<String, Long> datos)
      throws Exception {
    if (datos == null || datos.isEmpty()) {
      return;
    }
    doc.add(subtitulo(titulo));
    PdfPTable t = new PdfPTable(2);
    t.setWidthPercentage(60);
    t.setHorizontalAlignment(Element.ALIGN_LEFT);
    t.setHeaderRows(1);
    t.addCell(celdaEncabezado(columna));
    t.addCell(celdaEncabezado("Cantidad"));
    for (Map.Entry<String, Long> e : datos.entrySet()) {
      t.addCell(celda(categoria(e.getKey()), Element.ALIGN_LEFT));
      t.addCell(celda(String.valueOf(e.getValue()), Element.ALIGN_LEFT));
    }
    doc.add(t);
  }

  /**
   * Inserta una gráfica de Chart.js. Si la imagen viene dañada se omite y se deja una nota.
   *
   * @param doc documento abierto
   * @param grafica título e imagen en base64
   * @throws Exception si OpenPDF no puede escribir
   */
  private void agregarGrafica(Document doc, ChartImageDTO grafica) throws Exception {
    if (grafica == null || grafica.getImagenBase64() == null) {
      return;
    }
    String datos = grafica.getImagenBase64();
    int coma = datos.indexOf(',');
    if (datos.startsWith("data:") && coma > 0) {
      datos = datos.substring(coma + 1);
    }
    doc.add(subtitulo(grafica.getTitulo() == null ? "Gráfica" : grafica.getTitulo()));
    try {
      Image img = Image.getInstance(Base64.getDecoder().decode(datos));
      img.scaleToFit(500f, 280f);
      img.setAlignment(Element.ALIGN_CENTER);
      doc.add(img);
    } catch (Exception e) {
      doc.add(new Paragraph("No se pudo incluir la gráfica.", PEQUENA));
    }
  }

  /**
   * Devuelve el porcentaje de esa posición (con * si respondió el respaldo), "N/D" o "-".
   *
   * @param a archivo
   * @param posicion IA principal de la columna
   * @return porcentaje de esa posición (con * si respondió el respaldo), "N/D" o "-"
   */
  private String porcentajeDe(AnalyzedFileDTO a, String posicion) {
    for (ModelResultDTO r : a.getResultados()) {
      if (posicion.equalsIgnoreCase(r.getPosicion())) {
        if (!r.isExitoso()) {
          return "N/D";
        }
        return porcentaje(r.getPorcentajeIA()) + (r.getRespaldoDe() != null ? " *" : "");
      }
    }
    return "-";
  }

  /**
   * Devuelve la fuente en negrita con el color del nivel.
   *
   * @param nivel nivel ALTO, MEDIO, BAJO o SIN_RESULTADO
   * @return fuente en negrita con el color del nivel
   */
  private Font fuenteNivel(String nivel) {
    Color color = TEXTO;
    if ("ALTO".equals(nivel)) {
      color = ROJO_TEXTO;
    } else if ("MEDIO".equals(nivel)) {
      color = NARANJA_TEXTO;
    } else if ("BAJO".equals(nivel)) {
      color = VERDE;
    }
    return fuente(BASE_NEGRITA, Font.BOLD, 9.5f, color);
  }

  /**
   * Devuelve el nivel para mostrar.
   *
   * @param nivel nivel ALTO, MEDIO, BAJO o SIN_RESULTADO
   * @return nivel para mostrar
   */
  private String nivel(String nivel) {
    if ("ALTO".equals(nivel)) {
      return "Alto";
    }
    if ("MEDIO".equals(nivel)) {
      return "Medio";
    }
    if ("BAJO".equals(nivel)) {
      return "Bajo";
    }
    return "Sin resultado";
  }

  /**
   * Devuelve el veredicto para mostrar.
   *
   * @param veredicto veredicto de la IA
   * @return veredicto para mostrar
   */
  private String veredicto(String veredicto) {
    if (veredicto == null) {
      return "-";
    }
    switch (veredicto) {
      case "PROBABLEMENTE_IA":
        return "Probablemente IA";
      case "PROBABLEMENTE_HUMANO":
        return "Probablemente humano";
      default:
        return legible(veredicto);
    }
  }

  /**
   * Devuelve el texto para mostrar.
   *
   * @param categoria categoría del archivo (o cualquier clave de las tablas de estadísticas)
   * @return texto para mostrar
   */
  private String categoria(String categoria) {
    switch (categoria) {
      case "PDF":
        return "PDF";
      case "CODIGO":
        return "Código";
      default:
        return legible(categoria);
    }
  }

  /**
   * Devuelve el origen para mostrar.
   *
   * @param origen origen del análisis
   * @return origen para mostrar
   */
  private String origen(String origen) {
    if ("GITHUB".equals(origen)) {
      return "GitHub";
    }
    if ("ZIP".equals(origen)) {
      return "Archivo ZIP";
    }
    return legible(origen);
  }

  /**
   * Convierte un valor como COMPLETADO_PARCIAL en "Completado parcial".
   *
   * @param valor texto en mayúsculas con guiones bajos
   * @return texto para mostrar
   */
  private String legible(String valor) {
    if (valor == null || valor.isBlank() || "null".equals(valor)) {
      return "-";
    }
    if (!valor.equals(valor.toUpperCase())) {
      return valor;
    }
    String texto = valor.replace('_', ' ').toLowerCase();
    return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
  }

  /**
   * Devuelve la ciudad, país y coordenadas.
   *
   * @param a registro de auditoría
   * @return ciudad, país y coordenadas
   */
  private String ubicacion(AuditDTO a) {
    if (a.getLatitud() == null || a.getLongitud() == null) {
      return "Sin ubicación";
    }
    String lugar = (a.getCiudad() == null ? "" : a.getCiudad() + ", ") + (a.getPais() == null ? "" : a.getPais());
    return lugar + " (" + a.getLatitud() + ", " + a.getLongitud() + ")";
  }

  /**
   * Devuelve el porcentaje con un decimal.
   *
   * @param valor porcentaje o null
   * @return porcentaje con un decimal
   */
  private String porcentaje(Double valor) {
    return valor == null ? "Sin resultado" : String.format("%.1f %%", valor);
  }

  /**
   * Devuelve la fecha formateada o "-".
   *
   * @param f fecha o null
   * @return fecha formateada o "-"
   */
  private String fecha(LocalDateTime f) {
    return f == null ? "-" : f.format(FECHA);
  }

  /**
   * Devuelve el tamaño legible.
   *
   * @param bytes tamaño en bytes
   * @return tamaño legible
   */
  private String tamano(long bytes) {
    if (bytes < 1024) {
      return bytes + " B";
    }
    if (bytes < 1024 * 1024) {
      return String.format("%.1f KB", bytes / 1024.0);
    }
    return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
  }

  /**
   * Carga una variante de Carlito desde {@code resources/fonts}.
   *
   * @param archivo nombre del archivo .ttf
   * @return la fuente lista para incrustar, o null si no se pudo cargar
   */
  private static BaseFont cargarFuente(String archivo) {
    try (InputStream entrada = PdfReportGenerator.class.getResourceAsStream("/fonts/" + archivo)) {
      if (entrada == null) {
        log.warn("No se encontró la fuente {}; se usará Helvetica", archivo);
        return null;
      }
      byte[] bytes = entrada.readAllBytes();
      return BaseFont.createFont(archivo, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes, null);
    } catch (Exception e) {
      log.warn("No se pudo cargar la fuente {}: {}", archivo, e.getMessage());
      return null;
    }
  }

  /**
   * Crea una fuente con Carlito o, si no se cargó, con Helvetica.
   *
   * @param base variante de Carlito, puede ser null
   * @param estiloRespaldo estilo de Helvetica a usar si no hay Carlito
   * @param tamano tamaño en puntos
   * @param color color del texto
   * @return fuente
   */
  private static Font fuente(BaseFont base, int estiloRespaldo, float tamano, Color color) {
    if (base != null) {
      return new Font(base, tamano, Font.NORMAL, color);
    }
    return new Font(Font.HELVETICA, tamano, estiloRespaldo, color);
  }

  /** Escribe al pie de cada hoja una línea fina, el nombre del aplicativo y el número de página. */
  private static class PiePagina extends PdfPageEventHelper {

    /** Constructor vacío. */
    PiePagina() {}

    /**
     * Dibuja el pie de cada página: una línea, el nombre de la universidad y el número de página.
     *
     * @param writer escritor del PDF
     * @param document documento
     */
    @Override
    public void onEndPage(PdfWriter writer, Document document) {
      PdfContentByte lienzo = writer.getDirectContent();
      float y = document.bottom() - 18;
      lienzo.saveState();
      lienzo.setColorStroke(LINEA);
      lienzo.setLineWidth(0.5f);
      lienzo.moveTo(document.left(), y + 12);
      lienzo.lineTo(document.right(), y + 12);
      lienzo.stroke();
      lienzo.restoreState();
      ColumnText.showTextAligned(
          lienzo,
          Element.ALIGN_LEFT,
          new Phrase("Universidad El Bosque  ·  Detector de contenido generado con IA", PEQUENA),
          document.left(),
          y,
          0);
      ColumnText.showTextAligned(
          lienzo,
          Element.ALIGN_RIGHT,
          new Phrase("Página " + writer.getPageNumber(), PEQUENA),
          document.right(),
          y,
          0);
    }
  }
}
