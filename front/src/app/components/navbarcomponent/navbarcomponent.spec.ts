import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { RouterModule } from '@angular/router';
import { Navbarcomponent } from './navbarcomponent';

/**
 * Prueba de creación de la barra de navegación. Las peticiones al back quedan simuladas.
 */
describe('Navbarcomponent', () => {
  let component: Navbarcomponent;
  let fixture: ComponentFixture<Navbarcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Navbarcomponent],
      imports: [RouterModule.forRoot([])],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(Navbarcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  /** Revisa que el componente se cree. */
  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
