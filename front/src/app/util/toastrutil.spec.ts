import { TestBed } from '@angular/core/testing';
import { provideToastr, ToastrService } from 'ngx-toastr';
import { Toastrutil } from './toastrutil';

/**
 * Pruebas de los mensajes emergentes: cada tipo llama a ngx-toastr con su título por defecto.
 */
describe('Toastrutil', () => {
  let service: Toastrutil;
  let toastr: ToastrService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideToastr()] });
    service = TestBed.inject(Toastrutil);
    toastr = TestBed.inject(ToastrService);
  });

  /** Revisa que el servicio se cree. */
  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  /** Usa los títulos por defecto y respeta el título que se le pase. */
  it('muestra los mensajes con su título', () => {
    const exito = vi.spyOn(toastr, 'success');
    const error = vi.spyOn(toastr, 'error');
    service.exito('Perfil actualizado');
    service.error('Sin conexión', 'Error de red');
    expect(exito).toHaveBeenCalledWith('Perfil actualizado', 'Listo');
    expect(error).toHaveBeenCalledWith('Sin conexión', 'Error de red');
  });
});
