import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { URL_BACK } from './authservice';
import { Statsservice } from './statsservice';

/**
 * Pruebas del reporte de estadísticas.
 */
describe('Statsservice', () => {
  let service: Statsservice;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(Statsservice);
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

  /** Las gráficas viajan en el cuerpo del POST y el PDF se recibe como Blob. */
  it('envía las gráficas para el PDF', () => {
    const graficas = [{ titulo: 'Archivos por tipo', imagenBase64: 'data:image/png;base64,AAA' }];
    service.reportePdf(graficas, false).subscribe();
    const peticion = http.expectOne((r) => r.url === URL_BACK + '/admin/stats/reportpdf');
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual(graficas);
    expect(peticion.request.responseType).toBe('blob');
    peticion.flush(new Blob());
  });
});
