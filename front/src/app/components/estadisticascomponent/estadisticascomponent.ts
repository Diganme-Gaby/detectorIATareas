import { Component, inject, OnDestroy, OnInit, signal, WritableSignal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Chart, ChartConfiguration, registerables } from 'chart.js';
import { ChartImageModel, StatsModel } from '../../models/stats.model';
import { Authservice } from '../../services/authservice';
import { Statsservice } from '../../services/statsservice';
import { Formatoutil } from '../../util/formatoutil';
import { Pdfutil } from '../../util/pdfutil';
import { Toastrutil } from '../../util/toastrutil';

Chart.register(...registerables);

/** Una gráfica de la página. */
interface Grafica {
  /** Id del canvas. */
  id: string;
  /** Título que se muestra y que va en el PDF. */
  titulo: string;
  /** Tipo de gráfica de Chart.js. */
  tipo: 'bar' | 'line';
  /** `true` para barras horizontales. */
  horizontal: boolean;
  /** Saca de las estadísticas los datos de esta gráfica. */
  datos: (s: StatsModel) => Record<string, number | null>;
}

/**
 * Estadísticas de uso para el administrador.
 *
 * Muestra las cifras generales y dibuja las gráficas con Chart.js. Para el reporte PDF, cada
 * gráfica se copia sobre fondo blanco y se envía al back como imagen PNG en base64.
 */
@Component({
  selector: 'app-estadisticascomponent',
  standalone: false,
  styleUrl: './estadisticascomponent.css',
  templateUrl: './estadisticascomponent.html',
})
export class Estadisticascomponent implements OnInit, OnDestroy {
  /** Estadísticas. */
  private statsService: Statsservice = inject(Statsservice);
  /** Autenticación, para leer los mensajes de error. */
  private auth: Authservice = inject(Authservice);
  /** Mensajes tipo toast. */
  private toastr: Toastrutil = inject(Toastrutil);
  /** Visualización y descarga de PDF. */
  private pdf: Pdfutil = inject(Pdfutil);
  /** Formato de porcentajes y categorías. */
  formato: Formatoutil = inject(Formatoutil);

  /** Estadísticas cargadas, o `null` mientras cargan. */
  stats: WritableSignal<StatsModel | null> = signal(null);
  /** Indica si se está generando el PDF. */
  generando: WritableSignal<boolean> = signal(false);

  /** Gráficas de la página, en el orden en que se muestran y van en el PDF. */
  graficas: Grafica[] = [
    { id: 'g-dias', titulo: 'Análisis por día (últimos 30 días)', tipo: 'line', horizontal: false, datos: (s) => this.porFecha(s.analisisPorDia) },
    { id: 'g-logins', titulo: 'Inicios de sesión por día', tipo: 'line', horizontal: false, datos: (s) => this.porFecha(s.loginsPorDia) },
    { id: 'g-semana', titulo: 'Uso por día de la semana', tipo: 'bar', horizontal: false, datos: (s) => s.usoPorDiaSemana },
    { id: 'g-tipos', titulo: 'Archivos por tipo', tipo: 'bar', horizontal: false, datos: (s) => this.legibles(s.archivosPorCategoria) },
    { id: 'g-niveles', titulo: 'Archivos por nivel de IA', tipo: 'bar', horizontal: false, datos: (s) => this.legibles(s.archivosPorNivel) },
    { id: 'g-proveedores', titulo: 'Porcentaje promedio que da cada IA', tipo: 'bar', horizontal: true, datos: (s) => s.promedioPorProveedor },
    { id: 'g-fallos', titulo: 'Veces que cada IA no respondió', tipo: 'bar', horizontal: true, datos: (s) => s.fallosPorProveedor },
    { id: 'g-usuarios', titulo: 'Usuarios con más análisis', tipo: 'bar', horizontal: true, datos: (s) => s.topUsuarios },
  ];

  /** Gráficas de Chart.js creadas, para destruirlas al salir. */
  private instancias: Chart[] = [];

  /** Colores de las gráficas, tomados de la paleta del diseño. */
  private readonly colores: string[] = ['#8052ff', '#ffb829', '#3cc5a2', '#a68bff', '#15846e', '#ff7a6b'];

  /** Configura los colores por defecto de Chart.js y carga las estadísticas. */
  ngOnInit(): void {
    Chart.defaults.color = '#9a9a9a';
    Chart.defaults.font.family = "'Inter', system-ui, sans-serif";
    Chart.defaults.borderColor = '#2a2a2a';
    this.statsService.resumen().subscribe({
      next: (stats: StatsModel) => {
        this.stats.set(stats);
        // Se espera un ciclo para que los canvas ya estén en la página.
        setTimeout(() => this.dibujar(stats));
      },
      error: (error: HttpErrorResponse) =>
        this.toastr.error(this.auth.mensajeError(error, 'No se pudieron cargar las estadísticas.')),
    });
  }

  /** Destruye las gráficas al salir. */
  ngOnDestroy(): void {
    this.destruir();
  }

  /** Destruye las gráficas creadas. */
  private destruir(): void {
    for (const grafica of this.instancias) {
      grafica.destroy();
    }
    this.instancias = [];
  }

  /**
   * Crea una gráfica de Chart.js en cada canvas.
   *
   * @param stats estadísticas del back
   */
  private dibujar(stats: StatsModel): void {
    this.destruir();
    this.graficas.forEach((g: Grafica, i: number) => {
      const canvas: HTMLCanvasElement | null = document.getElementById(g.id) as HTMLCanvasElement | null;
      if (canvas === null) {
        return;
      }
      const datos: Record<string, number | null> = g.datos(stats);
      const color: string = this.colores[i % this.colores.length];
      const configuracion: ChartConfiguration = {
        type: g.tipo,
        data: {
          labels: Object.keys(datos),
          datasets: [
            {
              label: g.titulo,
              data: Object.values(datos).map((v: number | null) => v ?? 0),
              backgroundColor: g.tipo === 'line' ? 'rgba(128, 82, 255, 0.12)' : color,
              borderColor: color,
              borderWidth: g.tipo === 'line' ? 2 : 0,
              borderRadius: g.tipo === 'bar' ? 6 : 0,
              fill: g.tipo === 'line',
              tension: 0.3,
              cubicInterpolationMode: 'monotone',
              pointRadius: 0,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          indexAxis: g.horizontal ? 'y' : 'x',
          plugins: { legend: { display: false } },
          scales: {
            x: { grid: { display: g.horizontal }, ticks: { maxRotation: 0, autoSkip: true } },
            y: { grid: { display: !g.horizontal }, beginAtZero: true, ticks: { precision: 0 } },
          },
        },
      };
      this.instancias.push(new Chart(canvas, configuracion));
    });
  }

  /**
   * Acorta las fechas de las claves: `2026-11-11` pasa a `11/11`.
   *
   * @param datos datos por fecha
   * @returns datos con la fecha corta
   */
  private porFecha(datos: Record<string, number>): Record<string, number> {
    const resultado: Record<string, number> = {};
    for (const clave of Object.keys(datos)) {
      const partes: string[] = clave.split('-');
      resultado[partes.length === 3 ? partes[2] + '/' + partes[1] : clave] = datos[clave];
    }
    return resultado;
  }

  /**
   * Convierte las claves en texto legible, por ejemplo `CODIGO` pasa a `Código`.
   *
   * @param datos datos con claves del back
   * @returns datos con claves legibles
   */
  private legibles(datos: Record<string, number>): Record<string, number> {
    const resultado: Record<string, number> = {};
    for (const clave of Object.keys(datos)) {
      resultado[this.formato.categoria(clave)] = datos[clave];
    }
    return resultado;
  }

  /**
   * Copia cada gráfica sobre fondo blanco para que se lea bien en el PDF.
   *
   * @returns imágenes PNG en base64 con su título
   */
  private imagenes(): ChartImageModel[] {
    const lista: ChartImageModel[] = [];
    this.instancias.forEach((grafica: Chart, i: number) => {
      const origen: HTMLCanvasElement = grafica.canvas;
      const copia: HTMLCanvasElement = document.createElement('canvas');
      copia.width = origen.width;
      copia.height = origen.height;
      const ctx: CanvasRenderingContext2D | null = copia.getContext('2d');
      if (ctx === null) {
        return;
      }
      ctx.fillStyle = '#ffffff';
      ctx.fillRect(0, 0, copia.width, copia.height);
      ctx.drawImage(origen, 0, 0);
      lista.push({ titulo: this.graficas[i].titulo, imagenBase64: copia.toDataURL('image/png') });
    });
    return lista;
  }

  /** Abre el reporte de estadísticas en una pestaña nueva. */
  verPdf(): void {
    const pestana: Window | null = this.pdf.abrirPestana();
    this.generando.set(true);
    this.statsService.reportePdf(this.imagenes(), false).subscribe({
      next: (archivo: Blob) => {
        this.generando.set(false);
        this.pdf.mostrar(archivo, pestana);
      },
      error: () => {
        this.generando.set(false);
        pestana?.close();
        this.toastr.error('No se pudo generar el reporte.');
      },
    });
  }

  /** Descarga el reporte de estadísticas. */
  descargarPdf(): void {
    this.generando.set(true);
    this.statsService.reportePdf(this.imagenes(), true).subscribe({
      next: (archivo: Blob) => {
        this.generando.set(false);
        this.pdf.descargar(archivo, 'reporte-estadisticas.pdf');
      },
      error: () => {
        this.generando.set(false);
        this.toastr.error('No se pudo descargar el reporte.');
      },
    });
  }
}
