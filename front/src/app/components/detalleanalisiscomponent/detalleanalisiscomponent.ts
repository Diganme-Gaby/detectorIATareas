import { Component, inject, OnDestroy, OnInit, signal, WritableSignal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { AnalysisModel, AnalyzedFileModel, ModelResultModel } from '../../models/analysis.model';
import { Analysisservice } from '../../services/analysisservice';
import { Authservice } from '../../services/authservice';
import { Formatoutil } from '../../util/formatoutil';
import { Pdfutil } from '../../util/pdfutil';
import { Toastrutil } from '../../util/toastrutil';

/**
 * Detalle de un análisis.
 *
 * Muestra el promedio, el nivel y, por cada archivo, lo que respondió cada IA con sus motivos.
 * Cuando una IA principal no respondió y lo hizo su respaldo, lo indica. Mientras el análisis
 * está en proceso, consulta al back cada 3 segundos y muestra el avance. También permite ver,
 * descargar y reenviar los reportes PDF.
 */
@Component({
  selector: 'app-detalleanalisiscomponent',
  standalone: false,
  styleUrl: './detalleanalisiscomponent.css',
  templateUrl: './detalleanalisiscomponent.html',
})
export class Detalleanalisiscomponent implements OnInit, OnDestroy {
  /** Análisis. */
  private analysisService: Analysisservice = inject(Analysisservice);
  /** Ruta actual, para leer el id. */
  private ruta: ActivatedRoute = inject(ActivatedRoute);
  /** Mensajes tipo toast. */
  private toastr: Toastrutil = inject(Toastrutil);
  /** Visualización y descarga de PDF. */
  private pdf: Pdfutil = inject(Pdfutil);
  /** Sesión actual; el administrador ve también el dueño del análisis. */
  auth: Authservice = inject(Authservice);
  /** Formato de fechas, porcentajes y estados para la plantilla. */
  formato: Formatoutil = inject(Formatoutil);

  /** Análisis cargado, o `null` mientras carga. */
  analisis: WritableSignal<AnalysisModel | null> = signal(null);
  /** Se activa si el análisis no existe o es de otra cuenta. */
  noEncontrado: WritableSignal<boolean> = signal(false);
  /** Id del archivo que está desplegado. */
  abierto: WritableSignal<number | null> = signal(null);
  /** Página actual de la lista de archivos (un .zip o un repositorio puede traer muchos). */
  pagina: WritableSignal<number> = signal(1);
  /** Archivos por página. */
  porPagina: WritableSignal<number> = signal(10);
  /** Indica si el reenvío del correo está en curso. */
  reenviando: WritableSignal<boolean> = signal(false);

  /** Id del análisis tomado de la URL. */
  private id: number = 0;
  /** Temporizador de la consulta periódica. */
  private temporizador: ReturnType<typeof setInterval> | null = null;

  /** Lee el id de la URL y carga el análisis. */
  ngOnInit(): void {
    this.id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.cargar();
  }

  /** Detiene la consulta periódica al salir. */
  ngOnDestroy(): void {
    this.detener();
  }

  /**
   * Pide el análisis al back. Si sigue en proceso, programa la siguiente consulta; cuando
   * termina, avisa al usuario.
   */
  private cargar(): void {
    this.analysisService.detalle(this.id).subscribe({
      next: (analisis: AnalysisModel) => {
        const antes: AnalysisModel | null = this.analisis();
        this.analisis.set(analisis);
        // Con un solo archivo se muestra abierto desde el principio.
        if (this.abierto() === null && analisis.archivos.length === 1) {
          this.abierto.set(analisis.archivos[0].id);
        }
        if (this.enProceso()) {
          this.programar();
        } else {
          this.detener();
          if (antes !== null && antes.estado !== analisis.estado) {
            this.toastr.exito('El reporte también se envió a tu correo.', 'Análisis terminado');
          }
        }
      },
      error: (error: HttpErrorResponse) => {
        this.detener();
        if (error.status === 404) {
          this.noEncontrado.set(true);
        } else {
          this.toastr.error(this.auth.mensajeError(error, 'No se pudo cargar el análisis.'));
        }
      },
    });
  }

  /** Activa la consulta periódica. */
  private programar(): void {
    if (this.temporizador === null) {
      this.temporizador = setInterval(() => this.cargar(), 3000);
    }
  }

  /** Detiene la consulta periódica. */
  private detener(): void {
    if (this.temporizador !== null) {
      clearInterval(this.temporizador);
      this.temporizador = null;
    }
  }

  /**
   * @returns `true` si el análisis todavía está en cola o procesando
   */
  enProceso(): boolean {
    const a: AnalysisModel | null = this.analisis();
    return a !== null && (a.estado === 'PENDIENTE' || a.estado === 'PROCESANDO');
  }

  /**
   * @returns porcentaje de archivos ya procesados
   */
  progreso(): number {
    const a: AnalysisModel | null = this.analisis();
    if (a === null || a.totalArchivos === 0) {
      return 0;
    }
    return Math.round((a.archivos.length / a.totalArchivos) * 100);
  }

  /**
   * @param a análisis cargado
   * @returns archivos de la página actual
   */
  archivosPaginados(a: AnalysisModel): AnalyzedFileModel[] {
    const inicio: number = (this.pagina() - 1) * this.porPagina();
    return a.archivos.slice(inicio, inicio + this.porPagina());
  }

  /**
   * Cambia la cantidad de archivos por página y vuelve a la primera.
   *
   * @param cantidad archivos por página
   */
  cambiarPorPagina(cantidad: number): void {
    this.porPagina.set(cantidad);
    this.pagina.set(1);
  }

  /**
   * Despliega o recoge un archivo.
   *
   * @param archivo archivo elegido
   */
  alternar(archivo: AnalyzedFileModel): void {
    this.abierto.set(this.abierto() === archivo.id ? null : archivo.id);
  }

  /**
   * Nombre de la IA que respondió; si fue un respaldo, se aclara.
   *
   * @param r resultado de una posición
   * @returns texto para mostrar
   */
  quienRespondio(r: ModelResultModel): string {
    if (!r.exitoso) {
      return 'No disponible';
    }
    return r.respaldoDe !== null ? r.proveedor + ' (respaldo)' : r.proveedor;
  }

  /**
   * Abre un reporte en una pestaña nueva.
   *
   * @param archivoId archivo, o `null` para el consolidado
   */
  verPdf(archivoId: number | null): void {
    const pestana: Window | null = this.pdf.abrirPestana();
    this.analysisService.reportePdf(this.id, archivoId, false).subscribe({
      next: (archivo: Blob) => this.pdf.mostrar(archivo, pestana),
      error: () => {
        pestana?.close();
        this.toastr.error('No se pudo generar el reporte.');
      },
    });
  }

  /**
   * Descarga un reporte.
   *
   * @param archivoId archivo, o `null` para el consolidado
   * @param nombre nombre del archivo descargado
   */
  descargarPdf(archivoId: number | null, nombre: string): void {
    this.analysisService.reportePdf(this.id, archivoId, true).subscribe({
      next: (archivo: Blob) => this.pdf.descargar(archivo, nombre),
      error: () => this.toastr.error('No se pudo descargar el reporte.'),
    });
  }

  /**
   * @param archivo archivo, o `null` para el consolidado
   * @returns nombre del PDF descargado
   */
  nombrePdf(archivo: AnalyzedFileModel | null): string {
    if (archivo === null) {
      return 'reporte-analisis-' + this.id + '.pdf';
    }
    return 'reporte-' + archivo.id + '-' + archivo.nombreArchivo.replace(/[^\w.-]+/g, '_') + '.pdf';
  }

  /** Vuelve a enviar los reportes al correo del usuario. */
  reenviarCorreo(): void {
    this.reenviando.set(true);
    this.analysisService.reenviarCorreo(this.id).subscribe({
      next: (respuesta: string) => {
        this.reenviando.set(false);
        this.toastr.exito(respuesta);
      },
      error: (error: HttpErrorResponse) => {
        this.reenviando.set(false);
        this.toastr.error(this.auth.mensajeError(error, 'No se pudo enviar el correo.'));
      },
    });
  }
}
