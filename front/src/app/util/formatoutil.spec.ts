import { TestBed } from '@angular/core/testing';
import { Formatoutil } from './formatoutil';

/**
 * Pruebas del formato de los valores que manda el back (fechas, porcentajes, niveles y estados).
 */
describe('Formatoutil', () => {
  let service: Formatoutil;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Formatoutil);
  });

  /** Revisa que el servicio se cree. */
  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  /** Porcentajes con un decimal y coma; sin valor se muestra una raya. */
  it('formatea porcentajes', () => {
    expect(service.porcentaje(76.5)).toBe('76,5 %');
    expect(service.porcentaje(0)).toBe('0,0 %');
    expect(service.porcentaje(null)).toBe('-');
  });

  /** Fechas ISO del back y fechas en forma de arreglo. */
  it('formatea fechas', () => {
    expect(service.fecha('2026-11-11T17:39:00')).toBe('11 nov 2026, 17:39');
    expect(service.fecha([2026, 1, 5, 8, 3])).toBe('05 ene 2026, 08:03');
    expect(service.fecha(null)).toBe('-');
  });

  /** Tamaños en B, KB y MB. */
  it('formatea tamaños', () => {
    expect(service.tamano(512)).toBe('512 B');
    expect(service.tamano(2048)).toBe('2,0 KB');
    expect(service.tamano(5 * 1024 * 1024)).toBe('5,0 MB');
  });

  /** Los valores del back en mayúsculas se vuelven texto normal. */
  it('vuelve legibles los valores del back', () => {
    expect(service.legible('COMPLETADO_PARCIAL')).toBe('Completado parcial');
    expect(service.veredicto('PROBABLEMENTE_IA')).toBe('Probablemente IA');
    expect(service.origen('GITHUB')).toBe('GitHub');
    expect(service.categoria('CODIGO')).toBe('Código');
  });

  /** Cada nivel y cada estado tiene su texto y su color. */
  it('asigna texto y clase a niveles y estados', () => {
    expect(service.nivel('ALTO')).toBe('Alto');
    expect(service.nivel('SIN_RESULTADO')).toBe('Sin resultado');
    expect(service.claseNivel('BAJO')).toBe('chip nivel-bajo');
    expect(service.claseEstado('FALLIDO')).toBe('chip estado-error');
    expect(service.claseEstado('PROCESANDO')).toBe('chip estado-proceso');
  });
});
