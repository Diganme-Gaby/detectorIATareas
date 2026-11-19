import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { provideToastr } from 'ngx-toastr';
import { Paginacioncomponent } from '../paginacioncomponent/paginacioncomponent';
import { Adminanalisiscomponent } from './adminanalisiscomponent';

/**
 * Prueba de creación de la página de análisis de todos los usuarios. Las peticiones al back quedan simuladas.
 */
describe('Adminanalisiscomponent', () => {
  let component: Adminanalisiscomponent;
  let fixture: ComponentFixture<Adminanalisiscomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Adminanalisiscomponent, Paginacioncomponent],
      imports: [FormsModule, RouterModule.forRoot([])],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideToastr()],
    }).compileComponents();

    fixture = TestBed.createComponent(Adminanalisiscomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  /** Revisa que el componente se cree. */
  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
