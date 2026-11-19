import { inject, Service } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ChartImageModel, StatsModel } from '../models/stats.model';
import { URL_BACK } from './authservice';

/**
 * Estadísticas de uso (endpoints de `/admin/stats`).
 */
@Service()
export class Statsservice {
  /** Cliente HTTP de Angular. */
  private cliente: HttpClient = inject(HttpClient);
  /** URL de los endpoints de estadísticas. */
  private readonly urlBase: string = URL_BACK + '/admin/stats';

  /**
   * @returns observable con las estadísticas calculadas por el back
   */
  resumen() {
    return this.cliente.get<StatsModel>(this.urlBase + '/summary');
  }

  /**
   * Pide el PDF de estadísticas. Las gráficas viajan como imágenes PNG en base64 para que el
   * back las ponga en el reporte.
   *
   * @param graficas imágenes de las gráficas de Chart.js
   * @param descargar `true` para descarga, `false` para verlo en el navegador
   * @returns observable con el PDF como `Blob`
   */
  reportePdf(graficas: ChartImageModel[], descargar: boolean) {
    const parametros: HttpParams = new HttpParams().set('descargar', descargar);
    return this.cliente.post(this.urlBase + '/reportpdf', graficas, {
      params: parametros,
      responseType: 'blob',
    });
  }
}
