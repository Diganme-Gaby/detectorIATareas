import { inject, Service } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {
  AnalysisModel,
  FiltroAnalisisModel,
  GithubAnalysisModel,
  LimitesModel,
} from '../models/analysis.model';
import { URL_BACK } from './authservice';

/**
 * Análisis del usuario en sesión (endpoints de `/analysis`): envío de archivos o repositorios,
 * historial, detalle, reportes PDF y reenvío del correo.
 */
@Service()
export class Analysisservice {
  /** Cliente HTTP de Angular. */
  private cliente: HttpClient = inject(HttpClient);
  /** URL de los endpoints de análisis. */
  private readonly urlBase: string = URL_BACK + '/analysis';

  /**
   * Envía uno o varios archivos como `multipart/form-data`.
   *
   * @param archivos archivos elegidos por el usuario
   * @param titulo título opcional; si va vacío no se envía
   * @returns observable con el análisis creado en estado `PENDIENTE`
   */
  subirArchivos(archivos: File[], titulo: string) {
    const datos: FormData = new FormData();
    for (const archivo of archivos) {
      datos.append('archivos', archivo, archivo.name);
    }
    if (titulo.trim() !== '') {
      datos.append('titulo', titulo.trim());
    }
    return this.cliente.post<AnalysisModel>(this.urlBase + '/uploadfiles', datos);
  }

  /**
   * Envía un repositorio público de GitHub para analizar.
   *
   * @param solicitud enlace y restricciones
   * @returns observable con el análisis creado
   */
  analizarRepositorio(solicitud: GithubAnalysisModel) {
    return this.cliente.post<AnalysisModel>(this.urlBase + '/github', solicitud);
  }

  /**
   * @param filtro filtros del historial
   * @returns observable con los análisis del usuario
   */
  historial(filtro: FiltroAnalisisModel) {
    return this.cliente.get<AnalysisModel[]>(this.urlBase + '/history', {
      params: this.parametrosFiltro(filtro),
    });
  }

  /**
   * @param id identificador del análisis
   * @returns observable con el análisis, sus archivos y los resultados de cada IA
   */
  detalle(id: number) {
    return this.cliente.get<AnalysisModel>(this.urlBase + '/getbyid/' + id);
  }

  /**
   * Pide un reporte PDF. Sin `archivoId` es el consolidado; con `archivoId`, el de ese archivo.
   *
   * @param id identificador del análisis
   * @param archivoId archivo del análisis, o `null` para el consolidado
   * @param descargar `true` para descarga, `false` para verlo en el navegador
   * @returns observable con el PDF como `Blob`
   */
  reportePdf(id: number, archivoId: number | null, descargar: boolean) {
    let parametros: HttpParams = new HttpParams().set('descargar', descargar);
    if (archivoId !== null) {
      parametros = parametros.set('archivoId', archivoId);
    }
    return this.cliente.get(this.urlBase + '/reportpdf/' + id, {
      params: parametros,
      responseType: 'blob',
    });
  }

  /**
   * Vuelve a enviar el reporte al correo del usuario.
   *
   * @param id identificador del análisis
   * @returns observable con el mensaje del back
   */
  reenviarCorreo(id: number) {
    return this.cliente.post(this.urlBase + '/resendemail/' + id, null, { responseType: 'text' });
  }

  /**
   * @returns observable con los límites de envío del servidor
   */
  limites() {
    return this.cliente.get<LimitesModel>(this.urlBase + '/limits');
  }

  /**
   * Convierte los filtros en parámetros de la URL. Solo se envían los que tienen valor.
   *
   * @param filtro filtros del historial
   * @returns parámetros HTTP
   */
  parametrosFiltro(filtro: FiltroAnalisisModel): HttpParams {
    let parametros: HttpParams = new HttpParams();
    if (filtro.desde !== '') {
      parametros = parametros.set('desde', filtro.desde);
    }
    if (filtro.hasta !== '') {
      parametros = parametros.set('hasta', filtro.hasta);
    }
    if (filtro.categoria !== '') {
      parametros = parametros.set('categoria', filtro.categoria);
    }
    if (filtro.estado !== '') {
      parametros = parametros.set('estado', filtro.estado);
    }
    if (filtro.nivel !== '') {
      parametros = parametros.set('nivel', filtro.nivel);
    }
    if (filtro.texto.trim() !== '') {
      parametros = parametros.set('texto', filtro.texto.trim());
    }
    return parametros;
  }
}
