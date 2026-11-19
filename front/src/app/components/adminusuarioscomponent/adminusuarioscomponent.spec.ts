import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { provideRouter } from '@angular/router';
import { provideToastr } from 'ngx-toastr';
import { Paginacioncomponent } from '../paginacioncomponent/paginacioncomponent';
import { Adminusuarioscomponent } from './adminusuarioscomponent';

/**
 * Prueba de creación de la página de administración de usuarios. Las peticiones al back quedan simuladas.
 */
describe('Adminusuarioscomponent', () => {
  let component: Adminusuarioscomponent;
  let fixture: ComponentFixture<Adminusuarioscomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Adminusuarioscomponent, Paginacioncomponent],
      imports: [FormsModule],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([]), provideToastr()],
    }).compileComponents();

    fixture = TestBed.createComponent(Adminusuarioscomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  /** Revisa que el componente se cree. */
  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
