import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Adminservice } from './adminservice';
import { URL_BACK } from './authservice';

/**
 * Pruebas de las peticiones de administración de usuarios.
 */
describe('Adminservice', () => {
  let service: Adminservice;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(Adminservice);
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

  /** Editar un usuario es un PUT al id; solo viajan nombre, teléfono y rol. */
  it('actualiza un usuario', () => {
    const datos = { nombreCompleto: 'Laura Gomez', telefono: '3001234567', role: 'ADMIN' as const };
    service.actualizarUsuario(2, datos).subscribe();
    const peticion = http.expectOne(URL_BACK + '/admin/user/updatejson/2');
    expect(peticion.request.method).toBe('PUT');
    expect(peticion.request.body).toEqual(datos);
    peticion.flush('Usuario actualizado exitosamente');
  });

  /** Si no hay usuarios el back responde 204 sin cuerpo; se recibe la respuesta completa. */
  it('lista usuarios aunque el back responda 204', () => {
    service.usuarios().subscribe((respuesta) => {
      expect(respuesta.status).toBe(204);
      expect(respuesta.body).toBeNull();
    });
    http.expectOne(URL_BACK + '/admin/user/getall').flush(null, { status: 204, statusText: 'No Content' });
  });
});
