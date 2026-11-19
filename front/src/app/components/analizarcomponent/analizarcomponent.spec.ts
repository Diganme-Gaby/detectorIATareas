import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { provideRouter } from '@angular/router';
import { provideToastr } from 'ngx-toastr';
import { Analizarcomponent } from './analizarcomponent';

/**
 * Pruebas de la selección de archivos: límite de tamaño, repetidos y quitar.
 */
describe('Analizarcomponent', () => {
  let component: Analizarcomponent;
  let fixture: ComponentFixture<Analizarcomponent>;

  /**
   * Simula que el usuario soltó archivos sobre la zona.
   *
   * @param archivos archivos soltados
   */
  function soltar(archivos: File[]): void {
    const evento = { preventDefault: () => undefined, dataTransfer: { files: archivos } };
    component.alSoltar(evento as unknown as DragEvent);
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Analizarcomponent],
      imports: [FormsModule],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([]), provideToastr()],
    }).compileComponents();

    fixture = TestBed.createComponent(Analizarcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  /** Revisa que el componente se cree. */
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  /** Un archivo de más de 20 MB no se agrega y el mismo archivo no se repite. */
  it('no agrega archivos muy grandes ni repetidos', () => {
    const ensayo: File = new File(['texto'], 'ensayo.txt');
    const grande: File = new File(['x'], 'video.mp4');
    Object.defineProperty(grande, 'size', { value: 21 * 1024 * 1024 });
    soltar([ensayo, grande, ensayo]);
    expect(component.archivos().map((a: File) => a.name)).toEqual(['ensayo.txt']);
  });

  /** Se puede quitar un archivo de la lista. */
  it('quita un archivo', () => {
    soltar([new File(['a'], 'a.txt'), new File(['b'], 'b.txt')]);
    component.quitar(0);
    expect(component.archivos().map((a: File) => a.name)).toEqual(['b.txt']);
  });
});
