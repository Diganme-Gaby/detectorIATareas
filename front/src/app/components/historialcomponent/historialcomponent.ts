import { Component, inject, OnDestroy, OnInit, signal, WritableSignal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { AnalysisModel, FiltroAnalisisModel } from '../../models/analysis.model';
import { Analysisservice } from '../../services/analysisservice';
import { Authservice } from '../../services/authservice';
import { Formatoutil } from '../../util/formatoutil';
import { Pdfutil } from '../../util/pdfutil';
import { Toastrutil } from '../../util/toastrutil';

/**
 * Historial de análisis del usuario, con filtros.
 *
 * Mientras haya análisis en proceso, la lista se actualiza sola cada 5 segundos.
 */
@Component({
  selector: 'app-historialcomponent',
  standalone: false,
  styleUrl: './historialcomponent.css',
  templateUrl: './historialcomponent.html',
})
export class Historialcomponent implements OnInit, OnDestroy {
  /** Análisis. */
  private analysisService: Analysisservice = inject(Analysisservice);
  /** Autenticación, para leer los mensajes de error. */
  private auth: Authservice = inject(Authservice);
  /** Mensajes tipo toast. */
  private toastr: Toastrutil = inject(Toastrutil);
  /** Visualización de PDF. */
  private pdf: Pdfutil = inject(Pdfutil);
  /** Formato de fechas, porcentajes y estados para la plantilla. */
  formato: Formatoutil = inject(Formatoutil);

  /** Análisis que cumplen los filtros. */
  analisis: WritableSignal<AnalysisModel[]> = signal([]);
  /** Indica si la consulta está en curso. */
  cargando: WritableSignal<boolean> = signal(true);
  /** Página actual de la tabla, empezando en 1. */
  pagina: WritableSignal<number> = signal(1);
  /** Filas por página. */
  porPagina: WritableSignal<number> = signal(10);

  /** Filtros del formulario. */
  filtro: FiltroAnalisisModel = this.filtroVacio();

  /** Opciones del filtro de tipo de archivo. */
  categorias: string[] = ['TEXTO', 'CODIGO', 'PDF', 'DOCUMENTO', 'IMAGEN', 'AUDIO', 'VIDEO', 'OTRO'];
  /** Opciones del filtro de estado. */
  estados: string[] = ['PENDIENTE', 'PROCESANDO', 'COMPLETADO', 'COMPLETADO_PARCIAL', 'FALLIDO'];
  /** Opciones del filtro de nivel. */
  niveles: string[] = ['ALTO', 'MEDIO', 'BAJO', 'SIN_RESULTADO'];

  /** Temporizador de la actualización automática. */
  private temporizador: ReturnType<typeof setInterval> | null = null;

  /** Carga el historial al entrar. */
  ngOnInit(): void {
    this.buscar();
  }

  /** Detiene la actualización automática al salir. */
  ngOnDestroy(): void {
    this.detenerActualizacion();
  }

  /** Consulta el historial con los filtros actuales y vuelve a la primera página. */
  buscar(): void {
    this.cargando.set(true);
    this.pagina.set(1);
    this.cargar();
  }

  /** Quita los filtros y vuelve a consultar. */
  limpiar(): void {
    this.filtro = this.filtroVacio();
    this.buscar();
  }

  /** Pide el historial al back. */
  private cargar(): void {
    this.analysisService.historial(this.filtro).subscribe({
      next: (lista: AnalysisModel[]) => {
        this.analisis.set(lista ?? []);
        this.cargando.set(false);
        this.ajustarPagina();
        this.programarActualizacion();
      },
      error: (error: HttpErrorResponse) => {
        this.cargando.set(false);
        this.toastr.error(this.auth.mensajeError(error, 'No se pudo cargar el historial.'));
      },
    });
  }

  /**
   * @returns filas de la página actual
   */
  paginados(): AnalysisModel[] {
    const inicio: number = (this.pagina() - 1) * this.porPagina();
    return this.analisis().slice(inicio, inicio + this.porPagina());
  }

  /**
   * Cambia la cantidad de filas por página y vuelve a la primera.
   *
   * @param cantidad filas por página
   */
  cambiarPorPagina(cantidad: number): void {
    this.porPagina.set(cantidad);
    this.pagina.set(1);
  }

  /** Si la lista se achicó al actualizarse, vuelve a la última página que existe. */
  private ajustarPagina(): void {
    const ultima: number = Math.max(1, Math.ceil(this.analisis().length / this.porPagina()));
    if (this.pagina() > ultima) {
      this.pagina.set(ultima);
    }
  }

  /**
   * @param a análisis
   * @returns `true` si todavía está en cola o procesando
   */
  enProceso(a: AnalysisModel): boolean {
    return a.estado === 'PENDIENTE' || a.estado === 'PROCESANDO';
  }

  /** Activa la actualización automática si hay análisis en proceso, o la detiene si no. */
  private programarActualizacion(): void {
    const hayPendientes: boolean = this.analisis().some((a: AnalysisModel) => this.enProceso(a));
    if (hayPendientes && this.temporizador === null) {
      this.temporizador = setInterval(() => this.cargar(), 5000);
    } else if (!hayPendientes) {
      this.detenerActualizacion();
    }
  }

  /** Detiene la actualización automática. */
  private detenerActualizacion(): void {
    if (this.temporizador !== null) {
      clearInterval(this.temporizador);
      this.temporizador = null;
    }
  }

  /**
   * Abre el reporte consolidado en una pestaña nueva.
   *
   * @param a análisis
   */
  verPdf(a: AnalysisModel): void {
    const pestana: Window | null = this.pdf.abrirPestana();
    this.analysisService.reportePdf(a.id, null, false).subscribe({
      next: (archivo: Blob) => this.pdf.mostrar(archivo, pestana),
      error: () => {
        pestana?.close();
        this.toastr.error('No se pudo generar el reporte.');
      },
    });
  }

  /**
   * @returns filtros vacíos
   */
  private filtroVacio(): FiltroAnalisisModel {
    return { desde: '', hasta: '', categoria: '', estado: '', nivel: '', texto: '' };
  }
}
