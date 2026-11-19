import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideToastr } from 'ngx-toastr';
import { Estadisticascomponent } from './estadisticascomponent';

/**
 * Prueba de creación de la página de estadísticas. Las peticiones al back quedan simuladas.
 */
describe('Estadisticascomponent', () => {
  let component: Estadisticascomponent;
  let fixture: ComponentFixture<Estadisticascomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Estadisticascomponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([]), provideToastr()],
    }).compileComponents();

    fixture = TestBed.createComponent(Estadisticascomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  /** Revisa que el componente se cree. */
  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
