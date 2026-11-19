import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Footercomponent } from './footercomponent';

/**
 * Prueba de creación de el pie de página.
 */
describe('Footercomponent', () => {
  let component: Footercomponent;
  let fixture: ComponentFixture<Footercomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Footercomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Footercomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  /** Revisa que el componente se cree. */
  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
