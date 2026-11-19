import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  provideRouter,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';
import { adminGuardService } from './adminguardservice';

/**
 * Pruebas del guard de administración.
 */
describe('adminGuardService', () => {
  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => adminGuardService(...guardParameters));

  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem('token', 'abc');
  });

  /**
   * Configura el módulo de pruebas con el rol indicado. El rol se lee al crear el servicio de
   * autenticación, por eso se guarda antes.
   *
   * @param rol rol del usuario en sesión
   */
  function configurar(rol: string): void {
    localStorage.setItem('rol', rol);
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideRouter([])] });
  }

  /** El guard existe. */
  it('should be created', () => {
    configurar('USER');
    expect(executeGuard).toBeTruthy();
  });

  /** Un usuario normal se lleva a /analizar. */
  it('no deja entrar al rol USER', () => {
    configurar('USER');
    const resultado = executeGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot) as UrlTree;
    expect(resultado.toString()).toBe('/analizar');
  });

  /** El administrador entra. */
  it('deja entrar al rol ADMIN', () => {
    configurar('ADMIN');
    expect(executeGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)).toBe(true);
  });
});
