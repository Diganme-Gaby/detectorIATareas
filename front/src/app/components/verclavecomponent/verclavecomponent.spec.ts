import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Verclavecomponent } from './verclavecomponent';

/**
 * Pruebas del botón para mostrar u ocultar la contraseña.
 */
describe('Verclavecomponent', () => {
  let component: Verclavecomponent;
  let fixture: ComponentFixture<Verclavecomponent>;

  /**
   * Crea el botón con el estado indicado.
   *
   * @param visible true si la contraseña empieza visible
   */
  async function crear(visible: boolean): Promise<void> {
    fixture = TestBed.createComponent(Verclavecomponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('visible', visible);
    await fixture.whenStable();
  }

  /**
   * @returns botón del componente
   */
  function boton(): HTMLButtonElement {
    return fixture.nativeElement.querySelector('button');
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Verclavecomponent],
    }).compileComponents();
  });

  /** Revisa que el componente se cree. */
  it('should create', async () => {
    await crear(false);
    expect(component).toBeTruthy();
  });

  /** Con la contraseña oculta, el botón ofrece mostrarla y no envía el formulario. */
  it('ofrece mostrar la contraseña', async () => {
    await crear(false);
    expect(boton().type).toBe('button');
    expect(boton().getAttribute('aria-label')).toBe('Mostrar contraseña');
    expect(boton().getAttribute('aria-pressed')).toBe('false');
  });

  /** Cada clic alterna el estado y lo avisa a la página. */
  it('alterna entre mostrar y ocultar', async () => {
    await crear(false);
    const emitidos: boolean[] = [];
    component.visible.subscribe((v: boolean) => emitidos.push(v));
    boton().click();
    await fixture.whenStable();
    expect(component.visible()).toBe(true);
    expect(boton().getAttribute('aria-label')).toBe('Ocultar contraseña');
    boton().click();
    await fixture.whenStable();
    expect(component.visible()).toBe(false);
    expect(emitidos).toEqual([true, false]);
  });
});
