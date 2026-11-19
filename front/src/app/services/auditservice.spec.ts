import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Auditservice } from './auditservice';
import { URL_BACK } from './authservice';

/**
 * Pruebas de la consulta y exportación de la auditoría.
 */
describe('Auditservice', () => {
  let service: Auditservice;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(Auditservice);
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

  /** El PDF lleva los mismos filtros de la consulta y el parámetro `descargar`. */
  it('exporta el PDF con los filtros', () => {
    service
      .reportePdf({ username: ' laura.gomez ', accion: 'LOGIN_FALLIDO', desde: '', hasta: '' }, true)
      .subscribe();
    const peticion = http.expectOne((r) => r.url === URL_BACK + '/admin/audit/reportpdf');
    expect(peticion.request.params.get('username')).toBe('laura.gomez');
    expect(peticion.request.params.get('accion')).toBe('LOGIN_FALLIDO');
    expect(peticion.request.params.has('desde')).toBe(false);
    expect(peticion.request.params.get('descargar')).toBe('true');
    peticion.flush(new Blob());
  });
});
