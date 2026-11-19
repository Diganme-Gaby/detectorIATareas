import { inject, Service, signal, WritableSignal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import {
  AuthResponseModel,
  LoginModel,
  RegisterModel,
  ResetPasswordModel,
} from '../models/auth.model';
import { RolModel } from '../models/user.model';

/**
 * URL base del back. Es el único lugar donde se define; al desplegar se cambia por la del Tomcat.
 */
export const URL_BACK: string = 'http://localhost:8080';

/**
 * Autenticación y sesión del usuario.
 *
 * Consume los endpoints públicos de `/auth` (login, registro, confirmación de correo y
 * recuperación de contraseña) y guarda el token, el usuario y el rol en `localStorage` para que
 * la sesión siga abierta al recargar la página.
 */
@Service()
export class Authservice {
  /** Cliente HTTP de Angular. */
  private cliente: HttpClient = inject(HttpClient);
  /** Enrutador, para llevar al login al cerrar sesión. */
  private router: Router = inject(Router);
  /** URL de los endpoints de autenticación. */
  private readonly urlBase: string = URL_BACK + '/auth';

  /** Token JWT de la sesión, o `null` si no hay sesión. */
  token: WritableSignal<string | null> = signal(localStorage.getItem('token'));
  /** Nombre del usuario en sesión. */
  usuario: WritableSignal<string | null> = signal(localStorage.getItem('usuario'));
  /** Rol del usuario en sesión. */
  rol: WritableSignal<RolModel | null> = signal(localStorage.getItem('rol') as RolModel | null);

  /**
   * Inicia sesión.
   *
   * @param datos usuario y contraseña
   * @returns observable con el token, el rol y el usuario
   */
  login(datos: LoginModel) {
    return this.cliente.post<AuthResponseModel>(this.urlBase + '/login', datos);
  }

  /**
   * Crea una cuenta nueva. El back responde texto plano, no JSON.
   *
   * @param datos datos del formulario de registro
   * @returns observable con el mensaje del back
   */
  registrar(datos: RegisterModel) {
    return this.cliente.post(this.urlBase + '/register', datos, { responseType: 'text' });
  }

  /**
   * Confirma el correo con el código que llega en el enlace.
   *
   * @param token código del enlace
   * @returns observable con el mensaje del back
   */
  verificarCorreo(token: string) {
    const parametros: HttpParams = new HttpParams().set('token', token);
    return this.cliente.get(this.urlBase + '/verifyemail', {
      params: parametros,
      responseType: 'text',
    });
  }

  /**
   * Pide un enlace de confirmación nuevo.
   *
   * @param email correo de la cuenta
   * @returns observable con el mensaje del back
   */
  reenviarVerificacion(email: string) {
    const parametros: HttpParams = new HttpParams().set('email', email);
    return this.cliente.post(this.urlBase + '/resendverification', null, {
      params: parametros,
      responseType: 'text',
    });
  }

  /**
   * Pide el enlace para restablecer la contraseña.
   *
   * @param email correo de la cuenta
   * @returns observable con el mensaje del back
   */
  olvideContrasena(email: string) {
    const parametros: HttpParams = new HttpParams().set('email', email);
    return this.cliente.post(this.urlBase + '/forgotpassword', null, {
      params: parametros,
      responseType: 'text',
    });
  }

  /**
   * Guarda la contraseña nueva usando el código del enlace de recuperación.
   *
   * @param datos código y contraseña nueva
   * @returns observable con el mensaje del back
   */
  restablecerContrasena(datos: ResetPasswordModel) {
    return this.cliente.put(this.urlBase + '/resetpassword', datos, { responseType: 'text' });
  }

  /**
   * Guarda la sesión en `localStorage` y en las señales.
   *
   * @param respuesta respuesta del login
   */
  guardarSesion(respuesta: AuthResponseModel): void {
    localStorage.setItem('token', respuesta.token);
    localStorage.setItem('usuario', respuesta.username);
    localStorage.setItem('rol', respuesta.role);
    this.token.set(respuesta.token);
    this.usuario.set(respuesta.username);
    this.rol.set(respuesta.role);
  }

  /**
   * Borra la sesión y lleva al login.
   */
  cerrarSesion(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('usuario');
    localStorage.removeItem('rol');
    this.token.set(null);
    this.usuario.set(null);
    this.rol.set(null);
    this.router.navigate(['/login']);
  }

  /**
   * @returns `true` si hay un token guardado
   */
  estaAutenticado(): boolean {
    return this.token() !== null;
  }

  /**
   * @returns `true` si el usuario en sesión es administrador
   */
  esAdmin(): boolean {
    return this.rol() === 'ADMIN';
  }

  /**
   * Devuelve el mensaje que mandó el back; si no hay, usa el texto por defecto.
   *
   * @param error error de la petición
   * @param porDefecto mensaje a usar si el back no envió uno
   * @returns mensaje para mostrar al usuario
   */
  mensajeError(error: HttpErrorResponse, porDefecto: string): string {
    if (error.status === 0) {
      return 'No se pudo conectar con el servidor.';
    }
    return typeof error.error === 'string' && error.error !== '' ? error.error : porDefecto;
  }
}
