/**
 * Acciones que registra la auditoría del back.
 */
export type AccionModel =
  | 'REGISTRO'
  | 'VERIFICACION_CORREO'
  | 'REENVIO_VERIFICACION'
  | 'LOGIN_EXITOSO'
  | 'LOGIN_FALLIDO'
  | 'CUENTA_BLOQUEADA'
  | 'SOLICITUD_RECUPERACION'
  | 'CAMBIO_CONTRASENA'
  | 'ACTUALIZACION_PERFIL'
  | 'ANALISIS_CREADO'
  | 'ANALISIS_FINALIZADO'
  | 'DESCARGA_REPORTE'
  | 'REENVIO_REPORTE'
  | 'ADMIN_ACTUALIZA_USUARIO'
  | 'ADMIN_ACTIVA_USUARIO'
  | 'ADMIN_DESACTIVA_USUARIO'
  | 'ADMIN_DESBLOQUEA_USUARIO'
  | 'ADMIN_DESCARGA_REPORTE';

/**
 * Un registro de la auditoría. No se puede modificar ni borrar.
 */
export interface AuditModel {
  /** Identificador del registro. */
  id: number;
  /** Usuario que hizo la acción (o `anonimo`). */
  username: string;
  /** Acción realizada. */
  accion: AccionModel;
  /** Detalle de la acción. */
  descripcion: string;
  /** Fecha y hora. */
  fecha: string;
  /** IP desde la que se hizo la petición. */
  ip: string;
  /** Latitud aproximada de la IP. */
  latitud: number | null;
  /** Longitud aproximada de la IP. */
  longitud: number | null;
  /** Ciudad aproximada. */
  ciudad: string | null;
  /** País. */
  pais: string | null;
  /** Enlace de Google Maps con las coordenadas. */
  mapaUrl: string | null;
}

/**
 * Filtros de la auditoría. Los campos vacíos no se envían.
 */
export interface FiltroAuditoriaModel {
  /** Usuario exacto. */
  username: string;
  /** Acción registrada. */
  accion: string;
  /** Fecha inicial en formato `yyyy-MM-dd`. */
  desde: string;
  /** Fecha final en formato `yyyy-MM-dd`. */
  hasta: string;
}
