import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { AccionModel, AuditModel, FiltroAuditoriaModel } from '../../models/audit.model';
import { Auditservice } from '../../services/auditservice';
import { Authservice } from '../../services/authservice';
import { Formatoutil } from '../../util/formatoutil';
import { Pdfutil } from '../../util/pdfutil';
import { Toastrutil } from '../../util/toastrutil';

/**
 * Auditoría del sistema: todas las acciones registradas, con IP, ubicación aproximada y enlace
 * a Google Maps. Se puede filtrar y exportar a PDF. Los registros no se pueden modificar.
 */
@Component({
  selector: 'app-auditoriacomponent',
  standalone: false,
  styleUrl: './auditoriacomponent.css',
  templateUrl: './auditoriacomponent.html',
})
export class Auditoriacomponent implements OnInit {
  /** Auditoría. */
  private auditService: Auditservice = inject(Auditservice);
  /** Autenticación, para leer los mensajes de error. */
  private auth: Authservice = inject(Authservice);
  /** Mensajes tipo toast. */
  private toastr: Toastrutil = inject(Toastrutil);
  /** Visualización y descarga de PDF. */
  private pdf: Pdfutil = inject(Pdfutil);
  /** Formato de fechas y acciones para la plantilla. */
  formato: Formatoutil = inject(Formatoutil);

  /** Registros que cumplen los filtros. */
  registros: WritableSignal<AuditModel[]> = signal([]);
  /** Página actual de la tabla, empezando en 1. */
  pagina: WritableSignal<number> = signal(1);
  /** Filas por página. */
  porPagina: WritableSignal<number> = signal(10);
  /** Indica si la consulta está en curso. */
  cargando: WritableSignal<boolean> = signal(true);
  /** Filtros del formulario. */
  filtro: FiltroAuditoriaModel = this.filtroVacio();

  /** Opciones del filtro de acción. */
  acciones: AccionModel[] = [
    'REGISTRO',
    'VERIFICACION_CORREO',
    'REENVIO_VERIFICACION',
    'LOGIN_EXITOSO',
    'LOGIN_FALLIDO',
    'CUENTA_BLOQUEADA',
    'SOLICITUD_RECUPERACION',
    'CAMBIO_CONTRASENA',
    'ACTUALIZACION_PERFIL',
    'ANALISIS_CREADO',
    'ANALISIS_FINALIZADO',
    'DESCARGA_REPORTE',
    'REENVIO_REPORTE',
    'ADMIN_ACTUALIZA_USUARIO',
    'ADMIN_ACTIVA_USUARIO',
    'ADMIN_DESACTIVA_USUARIO',
    'ADMIN_DESBLOQUEA_USUARIO',
    'ADMIN_DESCARGA_REPORTE',
  ];

  /** Carga la auditoría al entrar. */
  ngOnInit(): void {
    this.buscar();
  }

  /** Consulta la auditoría con los filtros actuales. */
  buscar(): void {
    this.cargando.set(true);
    this.auditService.buscar(this.filtro).subscribe({
      next: (lista: AuditModel[]) => {
        this.registros.set(lista ?? []);
        this.pagina.set(1);
        this.cargando.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.cargando.set(false);
        this.toastr.error(this.auth.mensajeError(error, 'No se pudo cargar la auditoría.'));
      },
    });
  }

  /** Quita los filtros y vuelve a consultar. */
  limpiar(): void {
    this.filtro = this.filtroVacio();
    this.buscar();
  }

  /**
   * @returns filas de la página actual
   */
  paginados(): AuditModel[] {
    const inicio: number = (this.pagina() - 1) * this.porPagina();
    return this.registros().slice(inicio, inicio + this.porPagina());
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

  /**
   * @param r registro
   * @returns ciudad y país, o "Sin ubicación"
   */
  ubicacion(r: AuditModel): string {
    const partes: string[] = [];
    if (r.ciudad !== null && r.ciudad !== '') {
      partes.push(r.ciudad);
    }
    if (r.pais !== null && r.pais !== '') {
      partes.push(r.pais);
    }
    return partes.length > 0 ? partes.join(', ') : 'Sin ubicación';
  }

  /**
   * @param r registro
   * @returns latitud y longitud con cuatro decimales
   */
  coordenadas(r: AuditModel): string {
    if (r.latitud === null || r.longitud === null) {
      return '';
    }
    return r.latitud.toFixed(4) + ', ' + r.longitud.toFixed(4);
  }

  /** Abre el PDF de la auditoría (con los filtros actuales) en una pestaña nueva. */
  verPdf(): void {
    const pestana: Window | null = this.pdf.abrirPestana();
    this.auditService.reportePdf(this.filtro, false).subscribe({
      next: (archivo: Blob) => this.pdf.mostrar(archivo, pestana),
      error: () => {
        pestana?.close();
        this.toastr.error('No se pudo generar el reporte.');
      },
    });
  }

  /** Descarga el PDF de la auditoría con los filtros actuales. */
  descargarPdf(): void {
    this.auditService.reportePdf(this.filtro, true).subscribe({
      next: (archivo: Blob) => this.pdf.descargar(archivo, 'reporte-auditoria.pdf'),
      error: () => this.toastr.error('No se pudo descargar el reporte.'),
    });
  }

  /**
   * @returns filtros vacíos
   */
  private filtroVacio(): FiltroAuditoriaModel {
    return { username: '', accion: '', desde: '', hasta: '' };
  }
}
