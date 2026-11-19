import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { AnalysisModel, GithubAnalysisModel, LimitesModel } from '../../models/analysis.model';
import { Analysisservice } from '../../services/analysisservice';
import { Authservice } from '../../services/authservice';
import { Formatoutil } from '../../util/formatoutil';
import { Toastrutil } from '../../util/toastrutil';

/**
 * Envío de un análisis nuevo.
 *
 * Tiene dos pestañas: archivos (se arrastran o se eligen del equipo, incluidos .zip) y
 * repositorio de GitHub. Valida en pantalla la cantidad y el tamaño de los archivos y, al
 * enviar, lleva al detalle del análisis para ver su avance.
 */
@Component({
  selector: 'app-analizarcomponent',
  standalone: false,
  styleUrl: './analizarcomponent.css',
  templateUrl: './analizarcomponent.html',
})
export class Analizarcomponent implements OnInit {
  /** Análisis. */
  private analysisService: Analysisservice = inject(Analysisservice);
  /** Autenticación, para leer los mensajes de error. */
  private auth: Authservice = inject(Authservice);
  /** Enrutador, para ir al detalle. */
  private router: Router = inject(Router);
  /** Mensajes tipo toast. */
  private toastr: Toastrutil = inject(Toastrutil);
  /** Formato de tamaños para la plantilla. */
  formato: Formatoutil = inject(Formatoutil);

  /** Tamaño máximo por archivo; es el mismo `max-file-size` del back. */
  readonly maxBytesArchivo: number = 20 * 1024 * 1024;

  /** Pestaña activa: `ARCHIVOS` o `GITHUB`. */
  modo: WritableSignal<string> = signal('ARCHIVOS');
  /** Límites que publica el back. */
  limites: WritableSignal<LimitesModel | null> = signal(null);
  /** Archivos elegidos. */
  archivos: WritableSignal<File[]> = signal([]);
  /** Se activa mientras se arrastra un archivo sobre la zona. */
  arrastrando: WritableSignal<boolean> = signal(false);
  /** Indica si el envío está en curso. */
  enviando: WritableSignal<boolean> = signal(false);

  /** Título opcional del análisis de archivos. */
  titulo: string = '';
  /** Datos del formulario de repositorio. */
  repositorio: GithubAnalysisModel = this.repositorioVacio();
  /** Extensiones escritas por el usuario, separadas por coma. */
  extensionesTexto: string = '';

  /** Consulta los límites de envío. */
  ngOnInit(): void {
    this.analysisService.limites().subscribe({
      next: (limites: LimitesModel) => this.limites.set(limites),
      error: () => this.limites.set(null),
    });
  }

  /**
   * @returns máximo de archivos por envío
   */
  maxArchivos(): number {
    return this.limites()?.maxArchivosPorAnalisis ?? 10;
  }

  /**
   * Cambia de pestaña.
   *
   * @param modo `ARCHIVOS` o `GITHUB`
   */
  cambiarModo(modo: string): void {
    this.modo.set(modo);
  }

  /**
   * Agrega los archivos elegidos con el selector del sistema.
   *
   * @param evento evento `change` del input
   */
  alSeleccionar(evento: Event): void {
    const entrada: HTMLInputElement = evento.target as HTMLInputElement;
    if (entrada.files !== null) {
      this.agregar(Array.from(entrada.files));
    }
    // Permite volver a elegir el mismo archivo después de quitarlo.
    entrada.value = '';
  }

  /**
   * Permite soltar archivos sobre la zona.
   *
   * @param evento evento `dragover`
   */
  alArrastrar(evento: DragEvent): void {
    evento.preventDefault();
    this.arrastrando.set(true);
  }

  /** Quita el resaltado cuando el archivo sale de la zona. */
  alSalir(): void {
    this.arrastrando.set(false);
  }

  /**
   * Agrega los archivos soltados sobre la zona.
   *
   * @param evento evento `drop`
   */
  alSoltar(evento: DragEvent): void {
    evento.preventDefault();
    this.arrastrando.set(false);
    if (evento.dataTransfer !== null) {
      this.agregar(Array.from(evento.dataTransfer.files));
    }
  }

  /**
   * Agrega archivos a la lista, sin repetidos y respetando los límites.
   *
   * @param nuevos archivos a agregar
   */
  private agregar(nuevos: File[]): void {
    const lista: File[] = [...this.archivos()];
    for (const archivo of nuevos) {
      if (archivo.size > this.maxBytesArchivo) {
        this.toastr.advertencia(archivo.name + ' pesa más de 20 MB y no se agregó.');
      } else if (lista.length >= this.maxArchivos()) {
        this.toastr.advertencia('Puedes enviar máximo ' + this.maxArchivos() + ' archivos por análisis.');
        break;
      } else if (!lista.some((a: File) => a.name === archivo.name && a.size === archivo.size)) {
        lista.push(archivo);
      }
    }
    this.archivos.set(lista);
  }

  /**
   * Quita un archivo de la lista.
   *
   * @param indice posición en la lista
   */
  quitar(indice: number): void {
    this.archivos.set(this.archivos().filter((_a: File, i: number) => i !== indice));
  }

  /**
   * @param archivo archivo elegido
   * @returns `true` si es un .zip
   */
  esZip(archivo: File): boolean {
    return archivo.name.toLowerCase().endsWith('.zip');
  }

  /** Envía los archivos al back. */
  enviarArchivos(): void {
    this.enviando.set(true);
    this.analysisService.subirArchivos(this.archivos(), this.titulo).subscribe({
      next: (analisis: AnalysisModel) => this.alCrear(analisis),
      error: (error: HttpErrorResponse) => this.alFallar(error),
    });
  }

  /** Envía el repositorio al back. Las extensiones se separan por coma y se les quita el punto. */
  enviarRepositorio(): void {
    const solicitud: GithubAnalysisModel = {
      ...this.repositorio,
      url: this.repositorio.url.trim(),
      extensiones: this.extensionesTexto
        .split(',')
        .map((e: string) => e.trim().replace(/^\./, ''))
        .filter((e: string) => e !== ''),
    };
    this.enviando.set(true);
    this.analysisService.analizarRepositorio(solicitud).subscribe({
      next: (analisis: AnalysisModel) => this.alCrear(analisis),
      error: (error: HttpErrorResponse) => this.alFallar(error),
    });
  }

  /**
   * Lleva al detalle del análisis recién creado.
   *
   * @param analisis análisis creado
   */
  private alCrear(analisis: AnalysisModel): void {
    this.enviando.set(false);
    this.toastr.info('Te avisaremos por correo cuando termine.', 'Análisis en proceso');
    this.router.navigate(['/analisis', analisis.id]);
  }

  /**
   * Muestra el error del envío.
   *
   * @param error error de la petición
   */
  private alFallar(error: HttpErrorResponse): void {
    this.enviando.set(false);
    if (error.status === 413) {
      this.toastr.error('Los archivos superan el tamaño permitido por el servidor.');
      return;
    }
    this.toastr.error(this.auth.mensajeError(error, 'No se pudo iniciar el análisis.'));
  }

  /**
   * @returns formulario de repositorio vacío
   */
  private repositorioVacio(): GithubAnalysisModel {
    return { url: '', rama: '', carpeta: '', extensiones: [], maxArchivos: null, titulo: '' };
  }
}
