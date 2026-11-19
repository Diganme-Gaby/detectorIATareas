import { TestBed } from '@angular/core/testing';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { App } from './app';
import { AppModule } from './app-module';

/**
 * Prueba del módulo raíz: con todo lo que declara, el aplicativo arranca y muestra siempre la
 * barra de navegación y el pie de página.
 */
describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppModule],
      providers: [provideHttpClientTesting()],
    }).compileComponents();
  });

  /** El componente raíz se crea. */
  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  /** La barra de navegación y el pie de página siempre están. */
  it('should render navbar and footer', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('app-navbarcomponent')).not.toBeNull();
    expect(compiled.querySelector('app-footercomponent')).not.toBeNull();
  });
});
