import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, RouterModule } from '@angular/router';
import { provideToastr } from 'ngx-toastr';
import { AnalysisModel, ModelResultModel } from '../../models/analysis.model';
import { URL_BACK } from '../../services/authservice';
import { Paginacioncomponent } from '../paginacioncomponent/paginacioncomponent';
import { Detalleanalisiscomponent } from './detalleanalisiscomponent';

/**
 * Pruebas del detalle: avance del análisis y resultados que dio un modelo de respaldo.
 */
describe('Detalleanalisiscomponent', () => {
  let component: Detalleanalisiscomponent;
  let fixture: ComponentFixture<Detalleanalisiscomponent>;
  let http: HttpTestingController;

  /** Resultado que dio GitHub Models en lugar de OpenRouter. */
  const respaldo: ModelResultModel = {
    id: 3,
    proveedor: 'GitHub Models',
    respaldoDe: 'OpenRouter',
    posicion: 'OpenRouter',
    modelo: 'openai/gpt-4.1-mini',
    porcentajeIA: 77,
    veredicto: 'PROBABLEMENTE_IA',
    motivos: ['Párrafos de longitud muy pareja.'],
    exitoso: true,
    mensajeUsuario: null,
    tiempoMs: 1200,
    desdeCache: false,
  };

  /** Análisis con uno de sus dos archivos ya procesado. */
  const analisis: AnalysisModel = {
    id: 12,
    username: 'laura.gomez',
    titulo: 'Ensayo final',
    origen: 'ARCHIVOS',
    urlRepositorio: null,
    estado: 'COMPLETADO',
    fechaCreacion: '2026-11-11T17:39:00',
    fechaFinalizacion: '2026-11-11T17:41:00',
    promedioIA: 77,
    nivel: 'ALTO',
    totalArchivos: 2,
    correoEnviado: true,
    mensaje: null,
    archivos: [
      {
        id: 31,
        nombreArchivo: 'ensayo.pdf',
        ruta: 'ensayo.pdf',
        categoria: 'PDF',
        mimeType: 'application/pdf',
        tamanoBytes: 1024,
        promedioIA: 77,
        nivel: 'ALTO',
        resultados: [respaldo],
        avisos: [],
      },
    ],
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Detalleanalisiscomponent, Paginacioncomponent],
      imports: [RouterModule.forRoot([])],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideToastr(),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: '12' }) } } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Detalleanalisiscomponent);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    await fixture.whenStable();
    http.expectOne(URL_BACK + '/analysis/getbyid/12').flush(analisis);
    await fixture.whenStable();
  });

  /** El componente se crea y carga el análisis de la URL. */
  it('should create', () => {
    expect(component).toBeTruthy();
    expect(component.analisis()?.titulo).toBe('Ensayo final');
  });

  /** Con 1 de 2 archivos procesados el avance es 50 %. */
  it('calcula el avance', () => {
    expect(component.progreso()).toBe(50);
  });

  /** Cuando respondió un respaldo, se aclara en la tabla y en la página. */
  it('indica que respondió el modelo de respaldo', () => {
    expect(component.quienRespondio(respaldo)).toBe('GitHub Models (respaldo)');
    expect(fixture.nativeElement.textContent).toContain('GitHub Models (respaldo)');
  });
});
