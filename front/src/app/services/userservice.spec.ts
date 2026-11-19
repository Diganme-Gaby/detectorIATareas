import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { URL_BACK } from './authservice';
import { Userservice } from './userservice';

/**
 * Pruebas de las peticiones del perfil.
 */
describe('Userservice', () => {
  let service: Userservice;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(Userservice);
    http = TestBed.inject(HttpTestingController);
  });

  /** Al final no puede quedar ninguna petición pendiente. */
  afterEach(() => {
    http.verify();
  });

  /** Revisa que el servicio se cree. */
  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  /** El cambio de contraseña es un PUT con la actual y la nueva, y la respuesta es texto. */
  it('cambia la contraseña', () => {
    const datos = { passwordActual: 'Clave#2026', passwordNueva: 'Nueva#2027' };
    service.cambiarContrasena(datos).subscribe((respuesta: string) => {
      expect(respuesta).toBe('Contraseña actualizada exitosamente');
    });
    const peticion = http.expectOne(URL_BACK + '/user/changepassword');
    expect(peticion.request.method).toBe('PUT');
    expect(peticion.request.body).toEqual(datos);
    expect(peticion.request.responseType).toBe('text');
    peticion.flush('Contraseña actualizada exitosamente');
  });
});
