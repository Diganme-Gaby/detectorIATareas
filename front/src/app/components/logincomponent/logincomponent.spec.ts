import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, convertToParamMap, Router, RouterModule } from '@angular/router';
import { provideToastr } from 'ngx-toastr';
import { URL_BACK } from '../../services/authservice';
import { Verclavecomponent } from '../verclavecomponent/verclavecomponent';
import { Logincomponent } from './logincomponent';

/**
 * Pruebas del inicio de sesión: a dónde lleva al entrar y el aviso de correo sin confirmar.
 */
describe('Logincomponent', () => {
  let component: Logincomponent;
  let fixture: ComponentFixture<Logincomponent>;
  let http: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      declarations: [Logincomponent, Verclavecomponent],
      imports: [FormsModule, RouterModule.forRoot([])],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideToastr(),
        // Simula que el guard mandó al login desde el historial.
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: convertToParamMap({ volver: '/historial' }) } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Logincomponent);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    await fixture.whenStable();
  });

  /** Revisa que el componente se cree. */
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  /** El botón del ojo muestra la contraseña y la vuelve a ocultar. */
  it('muestra y oculta la contraseña', async () => {
    const campo: HTMLInputElement = fixture.nativeElement.querySelector('#password');
    const ojo: HTMLButtonElement = fixture.nativeElement.querySelector('app-verclavecomponent button');
    expect(campo.type).toBe('password');
    ojo.click();
    await fixture.whenStable();
    expect(component.verPassword()).toBe(true);
    expect(campo.type).toBe('text');
    ojo.click();
    await fixture.whenStable();
    expect(campo.type).toBe('password');
  });

  /** Con un login correcto guarda la sesión y vuelve a la página que se quería abrir. */
  it('guarda la sesión y regresa a la página pedida', () => {
    const navegar = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    component.datos = { username: 'laura.gomez', password: 'Clave#2026' };
    component.iniciarSesion();
    http.expectOne(URL_BACK + '/auth/login').flush({ token: 'abc', role: 'USER', username: 'laura.gomez' });
    expect(localStorage.getItem('token')).toBe('abc');
    expect(navegar).toHaveBeenCalledWith('/historial');
  });

  /** Si la cuenta no ha confirmado el correo, se ofrece reenviar el enlace. */
  it('detecta la cuenta sin confirmar', () => {
    component.iniciarSesion();
    http
      .expectOne(URL_BACK + '/auth/login')
      .flush('Debes confirmar tu correo antes de iniciar sesión', { status: 403, statusText: 'Forbidden' });
    expect(component.sinVerificar()).toBe(true);
    expect(component.cargando()).toBe(false);
  });
});
