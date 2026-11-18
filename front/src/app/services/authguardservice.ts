import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Authservice } from './authservice';

/**
 * Guard de las páginas que necesitan sesión.
 *
 * Si no hay sesión, lleva al login y guarda en `volver` la página que se quería abrir, para
 * regresar a ella después de iniciar sesión (por ejemplo desde el enlace del correo).
 *
 * @param _ruta ruta que se quiere abrir (no se usa)
 * @param estado estado del enrutador con la URL completa
 * @returns `true` si hay sesión; si no, la redirección al login
 */
export const authGuardService: CanActivateFn = (_ruta, estado) => {
  const auth: Authservice = inject(Authservice);
  const router: Router = inject(Router);
  return (
    auth.estaAutenticado() ||
    router.createUrlTree(['/login'], { queryParams: { volver: estado.url } })
  );
};
