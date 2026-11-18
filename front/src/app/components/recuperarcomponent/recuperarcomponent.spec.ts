import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { provideToastr } from 'ngx-toastr';
import { Recuperarcomponent } from './recuperarcomponent';

/**
 * Prueba de creación de la página de recuperación de contraseña. Las peticiones al back quedan simuladas.
 */
describe('Recuperarcomponent', () => {
  let component: Recuperarcomponent;
  let fixture: ComponentFixture<Recuperarcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Recuperarcomponent],
      imports: [FormsModule, RouterModule.forRoot([])],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideToastr()],
    }).compileComponents();

    fixture = TestBed.createComponent(Recuperarcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  /** Revisa que el componente se cree. */
  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
