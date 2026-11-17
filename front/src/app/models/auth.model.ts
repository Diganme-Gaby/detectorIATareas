import { RolModel } from './user.model';

/**
 * Datos que se envían a `POST /auth/login` para iniciar sesión.
 */
export interface LoginModel {
  /** Nombre de usuario. */
  username: string;
  /** Contraseña en texto plano; viaja solo por HTTPS hacia el back. */
  password: string;
}

/**
 * Respuesta del back cuando el inicio de sesión es correcto.
 */
export interface AuthResponseModel {
  /** Token JWT que se envía en el encabezado `Authorization` de las demás peticiones. */
  token: string;
  /** Rol del usuario: `USER` o `ADMIN`. */
  role: RolModel;
  /** Nombre de usuario con el que inició sesión. */
  username: string;
}

/**
 * Datos del formulario de registro que se envían a `POST /auth/register`.
 */
export interface RegisterModel {
  /** Nombre de usuario: de 4 a 30 caracteres (letras, números, punto, guion o guion bajo). */
  username: string;
  /** Correo al que llega el enlace de confirmación. */
  email: string;
  /** Nombre completo de la persona. */
  nombreCompleto: string;
  /** Teléfono opcional, de 7 a 20 dígitos. */
  telefono: string;
  /** Contraseña: mínimo 8 caracteres con mayúscula, minúscula, número y símbolo. */
  password: string;
}

/**
 * Datos para crear una contraseña nueva con el enlace de recuperación (`PUT /auth/resetpassword`).
 */
export interface ResetPasswordModel {
  /** Código que llega en el enlace del correo. */
  token: string;
  /** Contraseña nueva. */
  passwordNueva: string;
}
