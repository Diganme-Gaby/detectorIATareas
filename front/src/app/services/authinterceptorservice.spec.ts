import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Authservice, URL_BACK } from './authservice';
import { authInterceptorService } from './authinterceptorservice';

/**
 * Pruebas del interceptor: token en las peticiones protegidas y cierre de sesión cuando el back
 * rechaza el token.
 */
describe('authInterceptorService', () => {
  let cliente: HttpClient;
  let http: HttpTestingController;
  let auth: Authservice;

  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem('token', 'abc');
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptorService])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    cliente = TestBed.inject(HttpClient);
    http = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(Authservice);
  });

  /** Al final no puede quedar ninguna petición pendiente. */
  afterEach(() => {
    http.verify();
  });

  /** Las peticiones protegidas llevan el token; las de /auth no. */
  it('agrega el token excepto en /auth', () => {
    cliente.get(URL_BACK + '/user/me').subscribe();
    cliente.post(URL_BACK + '/auth/login', {}).subscribe();
    const protegida = http.expectOne(URL_BACK + '/user/me');
    const publica = http.expectOne(URL_BACK + '/auth/login');
    expect(protegida.request.headers.get('Authorization')).toBe('Bearer abc');
    expect(publica.request.headers.has('Authorization')).toBe(false);
    protegida.flush({});
    publica.flush({});
  });

  /** Un 401 cierra la sesión. */
  it('cierra la sesión si el token ya no sirve', () => {
    const cerrar = vi.spyOn(auth, 'cerrarSesion').mockImplementation(() => undefined);
    cliente.get(URL_BACK + '/analysis/history').subscribe({ error: () => undefined });
    http.expectOne(URL_BACK + '/analysis/history').flush('', { status: 401, statusText: 'Unauthorized' });
    expect(cerrar).toHaveBeenCalled();
  });

  /** En cambiar contraseña, 401 significa contraseña actual incorrecta: no se cierra la sesión. */
  it('no cierra la sesión si falla el cambio de contraseña', () => {
    const cerrar = vi.spyOn(auth, 'cerrarSesion').mockImplementation(() => undefined);
    cliente.put(URL_BACK + '/user/changepassword', {}).subscribe({ error: () => undefined });
    http
      .expectOne(URL_BACK + '/user/changepassword')
      .flush('La contraseña actual no es correcta', { status: 401, statusText: 'Unauthorized' });
    expect(cerrar).not.toHaveBeenCalled();
  });
});
