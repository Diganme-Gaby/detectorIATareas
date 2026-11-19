/**
 * Estadísticas de uso que calcula el back en `GET /admin/stats/summary`.
 */
export interface StatsModel {
  /** Cuentas registradas. */
  totalUsuarios: number;
  /** Cuentas activas. */
  usuariosActivos: number;
  /** Cuentas con el correo confirmado. */
  usuariosVerificados: number;
  /** Análisis realizados. */
  totalAnalisis: number;
  /** Archivos analizados. */
  totalArchivos: number;
  /** Promedio de IA de todos los archivos. */
  promedioGeneralIA: number | null;
  /** Análisis por día de los últimos 30 días; la clave es la fecha `yyyy-MM-dd`. */
  analisisPorDia: Record<string, number>;
  /** Inicios de sesión por día de los últimos 30 días. */
  loginsPorDia: Record<string, number>;
  /** Análisis por día de la semana. */
  usoPorDiaSemana: Record<string, number>;
  /** Archivos por tipo. */
  archivosPorCategoria: Record<string, number>;
  /** Archivos por nivel de IA. */
  archivosPorNivel: Record<string, number>;
  /** Porcentaje promedio que da cada IA. */
  promedioPorProveedor: Record<string, number | null>;
  /** Veces que cada IA no respondió. */
  fallosPorProveedor: Record<string, number>;
  /** Usuarios con más análisis. */
  topUsuarios: Record<string, number>;
}

/**
 * Imagen de una gráfica de Chart.js que se envía al back para ponerla en el PDF.
 */
export interface ChartImageModel {
  /** Título de la gráfica. */
  titulo: string;
  /** Imagen PNG en base64, con el prefijo `data:image/png;base64,`. */
  imagenBase64: string;
}
