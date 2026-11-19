import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Authservice } from './authservice';

/**
 * Guard de las páginas de administración: solo deja pasar al rol `ADMIN`.
 * Un usuario normal que intente entrar se lleva a la página de análisis.
 *
 * @returns `true` si es administrador; si no, la redirección a `/analizar`
 */
export const adminGuardService: CanActivateFn = () => {
  const auth: Authservice = inject(Authservice);
  const router: Router = inject(Router);
  return auth.esAdmin() || router.createUrlTree(['/analizar']);
};
