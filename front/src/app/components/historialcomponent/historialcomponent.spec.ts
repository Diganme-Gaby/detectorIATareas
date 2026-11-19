import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { provideToastr } from 'ngx-toastr';
import { Paginacioncomponent } from '../paginacioncomponent/paginacioncomponent';
import { Historialcomponent } from './historialcomponent';

/**
 * Prueba de creación de el historial de análisis. Las peticiones al back quedan simuladas.
 */
describe('Historialcomponent', () => {
  let component: Historialcomponent;
  let fixture: ComponentFixture<Historialcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Historialcomponent, Paginacioncomponent],
      imports: [FormsModule, RouterModule.forRoot([])],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideToastr()],
    }).compileComponents();

    fixture = TestBed.createComponent(Historialcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  /** Revisa que el componente se cree. */
  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
