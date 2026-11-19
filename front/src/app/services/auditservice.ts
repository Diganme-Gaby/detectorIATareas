import { inject, Service } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { AuditModel, FiltroAuditoriaModel } from '../models/audit.model';
import { URL_BACK } from './authservice';

/**
 * Consulta y exportación de la auditoría (endpoints de `/admin/audit`).
 */
@Service()
export class Auditservice {
  /** Cliente HTTP de Angular. */
  private cliente: HttpClient = inject(HttpClient);
  /** URL de los endpoints de auditoría. */
  private readonly urlBase: string = URL_BACK + '/admin/audit';

  /**
   * @param filtro filtros de la consulta
   * @returns observable con los registros, del más reciente al más antiguo
   */
  buscar(filtro: FiltroAuditoriaModel) {
    return this.cliente.get<AuditModel[]>(this.urlBase + '/getall', {
      params: this.parametros(filtro),
    });
  }

  /**
   * @param filtro los mismos filtros de la consulta
   * @param descargar `true` para descarga, `false` para verlo en el navegador
   * @returns observable con el PDF como `Blob`
   */
  reportePdf(filtro: FiltroAuditoriaModel, descargar: boolean) {
    return this.cliente.get(this.urlBase + '/reportpdf', {
      params: this.parametros(filtro).set('descargar', descargar),
      responseType: 'blob',
    });
  }

  /**
   * Convierte los filtros en parámetros de la URL. Solo se envían los que tienen valor.
   *
   * @param filtro filtros de la consulta
   * @returns parámetros HTTP
   */
  private parametros(filtro: FiltroAuditoriaModel): HttpParams {
    let parametros: HttpParams = new HttpParams();
    if (filtro.username.trim() !== '') {
      parametros = parametros.set('username', filtro.username.trim());
    }
    if (filtro.accion !== '') {
      parametros = parametros.set('accion', filtro.accion);
    }
    if (filtro.desde !== '') {
      parametros = parametros.set('desde', filtro.desde);
    }
    if (filtro.hasta !== '') {
      parametros = parametros.set('hasta', filtro.hasta);
    }
    return parametros;
  }
}
