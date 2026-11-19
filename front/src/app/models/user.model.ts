/**
 * Roles que maneja el back.
 */
export type RolModel = 'USER' | 'ADMIN';

/**
 * Cuenta de usuario tal como la devuelve el back.
 *
 * Las fechas llegan como texto ISO, por ejemplo `2026-11-11T17:39:00`.
 */
export interface UserModel {
  /** Identificador de la cuenta. */
  id: number;
  /** Nombre de usuario; no se puede cambiar. */
  username: string;
  /** Correo; no se puede cambiar. */
  email: string;
  /** Nombre completo. */
  nombreCompleto: string;
  /** Teléfono, o `null` si no lo registró. */
  telefono: string | null;
  /** Rol de la cuenta. */
  role: RolModel;
  /** `false` si un administrador desactivó la cuenta. */
  activo: boolean;
  /** `true` cuando la persona confirmó su correo. */
  correoVerificado: boolean;
  /** `true` mientras dura el bloqueo por intentos fallidos. */
  bloqueado: boolean;
  /** Intentos fallidos seguidos de inicio de sesión. */
  intentosFallidos: number;
  /** Fecha de registro. */
  fechaRegistro: string | null;
  /** Último inicio de sesión. */
  ultimoAcceso: string | null;
}

/**
 * Datos que se pueden actualizar del perfil. El administrador también puede cambiar el rol.
 */
export interface UpdateProfileModel {
  /** Nombre completo. */
  nombreCompleto: string;
  /** Teléfono; vacío si no se quiere registrar. */
  telefono: string;
  /** Rol nuevo; solo lo usa el administrador. */
  role?: RolModel;
}

/**
 * Datos para cambiar la contraseña desde el perfil.
 */
export interface ChangePasswordModel {
  /** Contraseña que tiene hoy la cuenta. */
  passwordActual: string;
  /** Contraseña nueva. */
  passwordNueva: string;
}
