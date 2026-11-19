import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { provideToastr } from 'ngx-toastr';
import { Verificarcorreocomponent } from './verificarcorreocomponent';

/**
 * Prueba de creación de la página de confirmación de correo. Las peticiones al back quedan simuladas.
 */
describe('Verificarcorreocomponent', () => {
  let component: Verificarcorreocomponent;
  let fixture: ComponentFixture<Verificarcorreocomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Verificarcorreocomponent],
      imports: [FormsModule, RouterModule.forRoot([])],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideToastr()],
    }).compileComponents();

    fixture = TestBed.createComponent(Verificarcorreocomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  /** Revisa que el componente se cree. */
  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
