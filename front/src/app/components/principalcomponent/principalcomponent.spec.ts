import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { Principalcomponent } from './principalcomponent';

/**
 * Prueba de creación de la portada.
 */
describe('Principalcomponent', () => {
  let component: Principalcomponent;
  let fixture: ComponentFixture<Principalcomponent>;

  beforeEach(async () => {
    // El navegador de pruebas (jsdom) no trae matchMedia ni dibuja en canvas: se simula
    // "reducir movimiento" para que la constelación no se anime y el canvas no devuelve contexto.
    window.matchMedia = vi.fn().mockReturnValue({ matches: true }) as unknown as typeof window.matchMedia;
    vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockReturnValue(null);

    await TestBed.configureTestingModule({
      declarations: [Principalcomponent],
      imports: [RouterModule.forRoot([])],
      providers: [provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(Principalcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  /** Revisa que el componente se cree. */
  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
