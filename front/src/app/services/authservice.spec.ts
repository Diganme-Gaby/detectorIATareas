import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { Authservice, URL_BACK } from './authservice';

/**
 * Pruebas de la sesión: login contra el back, guardado en localStorage, cierre de sesión y
 * mensajes de error.
 */
describe('Authservice', () => {
  let service: Authservice;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    service = TestBed.inject(Authservice);
    http = TestBed.inject(HttpTestingController);
  });

  /** Al final no puede quedar ninguna petición pendiente. */
  afterEach(() => {
    http.verify();
  });

  /** El servicio se crea sin sesión. */
  it('should be created', () => {
    expect(service).toBeTruthy();
    expect(service.estaAutenticado()).toBe(false);
  });

  /** El login hace POST a /auth/login con el usuario y la contraseña. */
  it('envía el login al back', () => {
    service.login({ username: 'laura.gomez', password: 'Clave#2026' }).subscribe();
    const peticion = http.expectOne(URL_BACK + '/auth/login');
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual({ username: 'laura.gomez', password: 'Clave#2026' });
    peticion.flush({ token: 'abc', role: 'USER', username: 'laura.gomez' });
  });

  /** Guardar la sesión la deja en localStorage y en las señales; cerrarla la borra y va al login. */
  it('guarda y cierra la sesión', () => {
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    service.guardarSesion({ token: 'abc', role: 'ADMIN', username: 'admin' });
    expect(localStorage.getItem('token')).toBe('abc');
    expect(service.esAdmin()).toBe(true);

    service.cerrarSesion();
    expect(localStorage.getItem('token')).toBeNull();
    expect(service.estaAutenticado()).toBe(false);
    expect(navegar).toHaveBeenCalledWith(['/login']);
  });

  /** Usa el texto del back si viene; si no hay conexión o no hay texto, usa otro mensaje. */
  it('arma el mensaje de error', () => {
    const delBack = new HttpErrorResponse({ status: 401, error: 'Nombre de usuario o contraseña inválidos' });
    const sinTexto = new HttpErrorResponse({ status: 500, error: null });
    const sinConexion = new HttpErrorResponse({ status: 0 });
    expect(service.mensajeError(delBack, 'x')).toBe('Nombre de usuario o contraseña inválidos');
    expect(service.mensajeError(sinTexto, 'Por defecto')).toBe('Por defecto');
    expect(service.mensajeError(sinConexion, 'x')).toBe('No se pudo conectar con el servidor.');
  });
});
