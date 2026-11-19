import { inject, Service } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { AnalysisModel, FiltroAnalisisModel } from '../models/analysis.model';
import { UpdateProfileModel, UserModel } from '../models/user.model';
import { Analysisservice } from './analysisservice';
import { URL_BACK } from './authservice';

/**
 * Módulo de administración (endpoints de `/admin`): gestión de usuarios y consulta de los
 * análisis de todos. Solo responde al rol `ADMIN`.
 */
@Service()
export class Adminservice {
  /** Cliente HTTP de Angular. */
  private cliente: HttpClient = inject(HttpClient);
  /** Se reutiliza para convertir los filtros en parámetros. */
  private analysisService: Analysisservice = inject(Analysisservice);
  /** URL de los endpoints de administración. */
  private readonly urlBase: string = URL_BACK + '/admin';

  /**
   * Lista todas las cuentas. Si no hay ninguna, el back responde 204 sin cuerpo, por eso se pide
   * la respuesta completa.
   *
   * @returns observable con la respuesta HTTP
   */
  usuarios() {
    return this.cliente.get<UserModel[]>(this.urlBase + '/user/getall', { observe: 'response' });
  }

  /**
   * @returns observable con la cantidad de cuentas
   */
  contarUsuarios() {
    return this.cliente.get<number>(this.urlBase + '/user/count');
  }

  /**
   * @param id identificador de la cuenta
   * @returns observable con la cuenta; 404 si no existe
   */
  usuarioPorId(id: number) {
    return this.cliente.get<UserModel>(this.urlBase + '/user/getbyid/' + id);
  }

  /**
   * Actualiza el nombre, el teléfono y el rol de una cuenta. El usuario y el correo no se pueden
   * cambiar.
   *
   * @param id identificador de la cuenta
   * @param datos datos nuevos
   * @returns observable con el mensaje del back
   */
  actualizarUsuario(id: number, datos: UpdateProfileModel) {
    return this.cliente.put(this.urlBase + '/user/updatejson/' + id, datos, {
      responseType: 'text',
    });
  }

  /**
   * @param id identificador de la cuenta
   * @returns observable con el mensaje del back
   */
  activar(id: number) {
    return this.cliente.put(this.urlBase + '/user/enable/' + id, null, { responseType: 'text' });
  }

  /**
   * @param id identificador de la cuenta
   * @returns observable con el mensaje del back
   */
  desactivar(id: number) {
    return this.cliente.put(this.urlBase + '/user/disable/' + id, null, { responseType: 'text' });
  }

  /**
   * Quita el bloqueo de 15 minutos por intentos fallidos.
   *
   * @param id identificador de la cuenta
   * @returns observable con el mensaje del back
   */
  desbloquear(id: number) {
    return this.cliente.put(this.urlBase + '/user/unlock/' + id, null, { responseType: 'text' });
  }

  /**
   * @param descargar `true` para descarga, `false` para verlo en el navegador
   * @returns observable con el PDF de usuarios como `Blob`
   */
  reporteUsuarios(descargar: boolean) {
    const parametros: HttpParams = new HttpParams().set('descargar', descargar);
    return this.cliente.get(this.urlBase + '/user/reportpdf', {
      params: parametros,
      responseType: 'blob',
    });
  }

  /**
   * @param filtro filtros iguales a los del historial
   * @returns observable con los análisis de todos los usuarios
   */
  todosLosAnalisis(filtro: FiltroAnalisisModel) {
    return this.cliente.get<AnalysisModel[]>(this.urlBase + '/analysis/getall', {
      params: this.analysisService.parametrosFiltro(filtro),
    });
  }
}
