import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  provideRouter,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';
import { authGuardService } from './authguardservice';

/**
 * Pruebas del guard de sesión.
 */
describe('authGuardService', () => {
  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => authGuardService(...guardParameters));

  /** Estado del enrutador con la página que se quería abrir. */
  const estado = { url: '/historial' } as RouterStateSnapshot;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideRouter([])] });
  });

  /** El guard existe. */
  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });

  /** Sin sesión lleva al login y guarda la página en `volver`. */
  it('manda al login si no hay sesión', () => {
    const resultado = executeGuard({} as ActivatedRouteSnapshot, estado) as UrlTree;
    expect(resultado.toString()).toBe('/login?volver=%2Fhistorial');
  });

  /** Con sesión deja pasar. */
  it('deja pasar si hay sesión', () => {
    localStorage.setItem('token', 'abc');
    expect(executeGuard({} as ActivatedRouteSnapshot, estado)).toBe(true);
  });
});
