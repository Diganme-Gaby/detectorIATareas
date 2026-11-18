import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { Authservice } from './authservice';

/**
 * Interceptor HTTP que agrega el token JWT a las peticiones protegidas.
 *
 * Las peticiones a `/auth` van sin token. Si el back responde 401 o 403, el token venció o no
 * sirve y se cierra la sesión; la única excepción es cambiar la contraseña, donde 401 significa
 * que la contraseña actual no coincide.
 *
 * @param req petición original
 * @param next siguiente manejador de la cadena
 * @returns observable con la respuesta
 */
export const authInterceptorService: HttpInterceptorFn = (req, next) => {
  const auth: Authservice = inject(Authservice);
  const token: string | null = auth.token();

  if (token === null || req.url.includes('/auth/')) {
    return next(req);
  }

  const peticion = req.clone({ setHeaders: { Authorization: 'Bearer ' + token } });
  return next(peticion).pipe(
    catchError((error: HttpErrorResponse) => {
      const esCambioContrasena: boolean = req.url.includes('/user/changepassword');
      if ((error.status === 401 || error.status === 403) && !esCambioContrasena) {
        auth.cerrarSesion();
      }
      return throwError(() => error);
    }),
  );
};
