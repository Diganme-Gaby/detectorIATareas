import { TestBed } from '@angular/core/testing';
import { HttpParams, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Analysisservice } from './analysisservice';
import { URL_BACK } from './authservice';

/**
 * Pruebas de las peticiones de análisis: envío de archivos, filtros del historial y reportes.
 */
describe('Analysisservice', () => {
  let service: Analysisservice;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(Analysisservice);
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

  /** Los archivos viajan como multipart en el campo `archivos`; el título vacío no se envía. */
  it('sube los archivos como FormData', () => {
    const archivo: File = new File(['hola'], 'ensayo.txt', { type: 'text/plain' });
    service.subirArchivos([archivo], '  ').subscribe();
    const peticion = http.expectOne(URL_BACK + '/analysis/uploadfiles');
    const cuerpo = peticion.request.body as FormData;
    expect(peticion.request.method).toBe('POST');
    expect(cuerpo.getAll('archivos').length).toBe(1);
    expect(cuerpo.has('titulo')).toBe(false);
    peticion.flush({ id: 1 });
  });

  /** Solo los filtros con valor se convierten en parámetros. */
  it('envía solo los filtros que tienen valor', () => {
    const parametros: HttpParams = service.parametrosFiltro({
      desde: '2026-11-01',
      hasta: '',
      categoria: 'PDF',
      estado: '',
      nivel: '',
      texto: '  ensayo ',
    });
    expect(parametros.keys()).toEqual(['desde', 'categoria', 'texto']);
    expect(parametros.get('texto')).toBe('ensayo');
  });

  /** El reporte de un archivo lleva `archivoId`; el consolidado no. */
  it('pide el reporte de un archivo o el consolidado', () => {
    service.reportePdf(12, 31, true).subscribe();
    service.reportePdf(12, null, false).subscribe();
    const peticiones = http.match((r) => r.url === URL_BACK + '/analysis/reportpdf/12');
    expect(peticiones[0].request.params.get('archivoId')).toBe('31');
    expect(peticiones[0].request.params.get('descargar')).toBe('true');
    expect(peticiones[1].request.params.has('archivoId')).toBe(false);
    expect(peticiones[1].request.responseType).toBe('blob');
    peticiones.forEach((p) => p.flush(new Blob()));
  });
});
