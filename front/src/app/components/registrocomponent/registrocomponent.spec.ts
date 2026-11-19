import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { provideToastr } from 'ngx-toastr';
import { Verclavecomponent } from '../verclavecomponent/verclavecomponent';
import { Registrocomponent } from './registrocomponent';

/**
 * Pruebas del registro: la contraseña se valida con las mismas reglas del back.
 */
describe('Registrocomponent', () => {
  let component: Registrocomponent;
  let fixture: ComponentFixture<Registrocomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Registrocomponent, Verclavecomponent],
      imports: [FormsModule, RouterModule.forRoot([])],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideToastr()],
    }).compileComponents();

    fixture = TestBed.createComponent(Registrocomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  /** Revisa que el componente se cree. */
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  /** La contraseña necesita mayúscula, minúscula, número, símbolo y 8 caracteres. */
  it('valida la contraseña', () => {
    component.datos.password = 'clave123';
    expect(component.passwordValida()).toBe(false);
    component.datos.password = 'Clave#2026';
    expect(component.passwordValida()).toBe(true);
  });

  /** Las dos contraseñas deben ser iguales. */
  it('compara la confirmación', () => {
    component.datos.password = 'Clave#2026';
    component.confirmacion = 'Clave#2025';
    expect(component.coinciden()).toBe(false);
    component.confirmacion = 'Clave#2026';
    expect(component.coinciden()).toBe(true);
  });
});
