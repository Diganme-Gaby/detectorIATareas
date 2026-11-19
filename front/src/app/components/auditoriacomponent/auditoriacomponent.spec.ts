import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { provideRouter } from '@angular/router';
import { provideToastr } from 'ngx-toastr';
import { Paginacioncomponent } from '../paginacioncomponent/paginacioncomponent';
import { Auditoriacomponent } from './auditoriacomponent';

/**
 * Prueba de creación de la página de auditoría. Las peticiones al back quedan simuladas.
 */
describe('Auditoriacomponent', () => {
  let component: Auditoriacomponent;
  let fixture: ComponentFixture<Auditoriacomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Auditoriacomponent, Paginacioncomponent],
      imports: [FormsModule],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([]), provideToastr()],
    }).compileComponents();

    fixture = TestBed.createComponent(Auditoriacomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  /** Revisa que el componente se cree. */
  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
