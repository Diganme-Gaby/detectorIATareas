import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { Noencontradocomponent } from './noencontradocomponent';

/**
 * Prueba de la página 404: una dirección que no existe muestra esta página.
 */
describe('Noencontradocomponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Noencontradocomponent],
      imports: [RouterModule.forRoot([{ path: '**', component: Noencontradocomponent }])],
      providers: [provideHttpClient()],
    }).compileComponents();
  });

  /** Al abrir una dirección inexistente se ve el 404 con la dirección que se pidió. */
  it('muestra la página 404 con la dirección pedida', async () => {
    const harness: RouterTestingHarness = await RouterTestingHarness.create();
    const component = await harness.navigateByUrl('/no-existe', Noencontradocomponent);
    expect(component.ruta).toBe('/no-existe');
    expect(harness.routeNativeElement?.textContent).toContain('Esta página no existe.');
  });
});
