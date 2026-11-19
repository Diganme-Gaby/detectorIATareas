import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Paginacioncomponent } from './paginacioncomponent';

/**
 * Pruebas de la paginación: rango visible, páginas que se muestran y eventos.
 */
describe('Paginacioncomponent', () => {
  let component: Paginacioncomponent;
  let fixture: ComponentFixture<Paginacioncomponent>;

  /**
   * Crea la paginación con los valores indicados.
   *
   * @param total filas en total
   * @param pagina página actual
   * @param porPagina filas por página
   */
  async function crear(total: number, pagina: number, porPagina: number): Promise<void> {
    fixture = TestBed.createComponent(Paginacioncomponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('total', total);
    fixture.componentRef.setInput('pagina', pagina);
    fixture.componentRef.setInput('porPagina', porPagina);
    await fixture.whenStable();
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Paginacioncomponent],
    }).compileComponents();
  });

  /** Revisa que el componente se cree. */
  it('should create', async () => {
    await crear(54, 1, 10);
    expect(component).toBeTruthy();
  });

  /** Con 54 filas de a 10, la página 6 muestra de la 51 a la 54. */
  it('calcula el rango visible', async () => {
    await crear(54, 6, 10);
    expect(component.totalPaginas()).toBe(6);
    expect(component.desde()).toBe(51);
    expect(component.hasta()).toBe(54);
    expect(fixture.nativeElement.textContent).toContain('Mostrando 51-54 de 54');
  });

  /** Se muestran hasta cinco páginas alrededor de la actual. */
  it('muestra las páginas cercanas', async () => {
    await crear(200, 10, 10);
    expect(component.paginas()).toEqual([8, 9, 10, 11, 12]);
  });

  /** Solo avisa cambios a páginas válidas y distintas de la actual. */
  it('emite la página elegida', async () => {
    await crear(30, 1, 10);
    const emitidas: number[] = [];
    component.paginaCambiada.subscribe((p: number) => emitidas.push(p));
    component.ir(2);
    component.ir(1);
    component.ir(9);
    expect(emitidas).toEqual([2]);
  });
});
