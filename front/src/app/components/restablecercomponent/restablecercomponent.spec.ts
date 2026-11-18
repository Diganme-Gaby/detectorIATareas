import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { provideToastr } from 'ngx-toastr';
import { Verclavecomponent } from '../verclavecomponent/verclavecomponent';
import { Restablecercomponent } from './restablecercomponent';

/**
 * Prueba de creación de la página de contraseña nueva. Las peticiones al back quedan simuladas.
 */
describe('Restablecercomponent', () => {
  let component: Restablecercomponent;
  let fixture: ComponentFixture<Restablecercomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Restablecercomponent, Verclavecomponent],
      imports: [FormsModule, RouterModule.forRoot([])],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideToastr()],
    }).compileComponents();

    fixture = TestBed.createComponent(Restablecercomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  /** Revisa que el componente se cree. */
  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
