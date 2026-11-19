import { TestBed } from '@angular/core/testing';
import { Pdfutil } from './pdfutil';

/**
 * Pruebas de la descarga de reportes PDF.
 */
describe('Pdfutil', () => {
  let service: Pdfutil;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Pdfutil);
  });

  /** Revisa que el servicio se cree. */
  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  /** La descarga crea un enlace temporal con el nombre del archivo y le hace clic. */
  it('descarga el PDF con el nombre indicado', () => {
    URL.createObjectURL = vi.fn(() => 'blob:prueba');
    URL.revokeObjectURL = vi.fn();
    const clic = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined);
    const crear = vi.spyOn(document, 'createElement');

    service.descargar(new Blob(['%PDF'], { type: 'application/pdf' }), 'reporte-12.pdf');

    const enlace = crear.mock.results[0].value as HTMLAnchorElement;
    expect(enlace.download).toBe('reporte-12.pdf');
    expect(enlace.href).toBe('blob:prueba');
    expect(clic).toHaveBeenCalled();
  });
});
