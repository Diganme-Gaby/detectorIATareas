/**
 * De dónde vienen los archivos de un análisis.
 */
export type OrigenModel = 'ARCHIVOS' | 'ZIP' | 'GITHUB';

/**
 * Estados por los que pasa un análisis mientras las IAs trabajan.
 */
export type EstadoModel =
  | 'PENDIENTE'
  | 'PROCESANDO'
  | 'COMPLETADO'
  | 'COMPLETADO_PARCIAL'
  | 'FALLIDO';

/**
 * Tipo de archivo que detecta el back.
 */
export type CategoriaModel =
  | 'TEXTO'
  | 'CODIGO'
  | 'PDF'
  | 'DOCUMENTO'
  | 'IMAGEN'
  | 'AUDIO'
  | 'VIDEO'
  | 'OTRO';

/**
 * Lo que respondió una IA sobre un archivo.
 *
 * Si la IA principal no respondió y lo hizo su respaldo, `proveedor` es el respaldo y
 * `respaldoDe` la IA principal a la que reemplazó.
 */
export interface ModelResultModel {
  /** Identificador del resultado. */
  id: number;
  /** IA que respondió (Gemini, Groq, OpenRouter o uno de los respaldos). */
  proveedor: string;
  /** IA principal a la que reemplazó el respaldo, o `null` si respondió la principal. */
  respaldoDe: string | null;
  /** Columna del reporte en la que va el resultado: siempre una de las tres IAs principales. */
  posicion: string;
  /** Modelo exacto que se usó. */
  modelo: string;
  /** Probabilidad de que el archivo sea generado con IA (0 a 100). */
  porcentajeIA: number | null;
  /** Veredicto corto, por ejemplo `PROBABLEMENTE_IA`. */
  veredicto: string | null;
  /** Razones que dio la IA. */
  motivos: string[];
  /** `false` si ni la IA ni sus respaldos respondieron. */
  exitoso: boolean;
  /** Aviso para el usuario cuando no hubo respuesta. */
  mensajeUsuario: string | null;
  /** Tiempo que tardó la respuesta, en milisegundos. */
  tiempoMs: number;
  /** `true` si se reutilizó el resultado de un archivo idéntico. */
  desdeCache: boolean;
}

/**
 * Un archivo dentro de un análisis, con el resultado de cada IA.
 */
export interface AnalyzedFileModel {
  /** Identificador del archivo; se usa para pedir su reporte PDF. */
  id: number;
  /** Nombre del archivo. */
  nombreArchivo: string;
  /** Ruta completa, por ejemplo `tarea.zip/src/Main.java`. */
  ruta: string;
  /** Tipo de archivo. */
  categoria: CategoriaModel;
  /** Tipo MIME detectado. */
  mimeType: string;
  /** Tamaño en bytes. */
  tamanoBytes: number;
  /** Promedio de las IAs que respondieron. */
  promedioIA: number | null;
  /** `ALTO`, `MEDIO`, `BAJO` o `SIN_RESULTADO`. */
  nivel: string | null;
  /** Resultado de cada una de las tres posiciones. */
  resultados: ModelResultModel[];
  /** Avisos para el usuario sobre este archivo. */
  avisos: string[];
}

/**
 * Un análisis completo: uno o varios archivos enviados juntos.
 */
export interface AnalysisModel {
  /** Identificador del análisis. */
  id: number;
  /** Dueño del análisis. */
  username: string;
  /** Título que puso el usuario o el que generó el back. */
  titulo: string;
  /** Origen de los archivos. */
  origen: OrigenModel;
  /** Enlace del repositorio cuando el origen es GitHub. */
  urlRepositorio: string | null;
  /** Estado del procesamiento. */
  estado: EstadoModel;
  /** Fecha de envío. */
  fechaCreacion: string | null;
  /** Fecha en que terminó, o `null` si sigue en proceso. */
  fechaFinalizacion: string | null;
  /** Promedio general de todos los archivos. */
  promedioIA: number | null;
  /** Nivel del promedio general. */
  nivel: string | null;
  /** Cantidad de archivos que se van a analizar. */
  totalArchivos: number;
  /** `true` cuando el reporte ya salió por correo. */
  correoEnviado: boolean;
  /** Aviso general, por ejemplo cuando una IA no respondió. */
  mensaje: string | null;
  /** Archivos procesados hasta ahora. */
  archivos: AnalyzedFileModel[];
}

/**
 * Solicitud para analizar un repositorio público de GitHub.
 */
export interface GithubAnalysisModel {
  /** Enlace del repositorio (obligatorio). */
  url: string;
  /** Rama; si va vacía se usa la principal. */
  rama: string;
  /** Carpeta del repositorio a la que se limita el análisis. */
  carpeta: string;
  /** Extensiones a incluir, sin punto. */
  extensiones: string[];
  /** Máximo de archivos a tomar del repositorio. */
  maxArchivos: number | null;
  /** Título opcional. */
  titulo: string;
}

/**
 * Filtros del historial. Los campos vacíos no se envían; las fechas van en formato `yyyy-MM-dd`.
 */
export interface FiltroAnalisisModel {
  /** Fecha inicial. */
  desde: string;
  /** Fecha final. */
  hasta: string;
  /** Tipo de archivo. */
  categoria: string;
  /** Estado del análisis. */
  estado: string;
  /** Nivel de IA. */
  nivel: string;
  /** Texto a buscar en el título o en el nombre de los archivos. */
  texto: string;
}

/**
 * Límites de envío que publica el back en `GET /analysis/limits`.
 */
export interface LimitesModel {
  /** Archivos sueltos por análisis. */
  maxArchivosPorAnalisis: number;
  /** Archivos que se toman de los .zip. */
  maxArchivosDesdeZip: number;
  /** Archivos que se toman de un repositorio. */
  maxArchivosRepositorio: number;
  /** Extensiones que se toman de un repositorio si no se indican otras. */
  extensionesRepositorioPorDefecto: string[];
  /** Descripción de los tipos de archivo aceptados. */
  tiposAceptados: string;
}
