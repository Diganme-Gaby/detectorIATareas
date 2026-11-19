import { inject, Service } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ChangePasswordModel, UpdateProfileModel, UserModel } from '../models/user.model';
import { URL_BACK } from './authservice';

/**
 * Perfil del usuario que inició sesión (endpoints de `/user`).
 */
@Service()
export class Userservice {
  /** Cliente HTTP de Angular. */
  private cliente: HttpClient = inject(HttpClient);
  /** URL de los endpoints del perfil. */
  private readonly urlBase: string = URL_BACK + '/user';

  /**
   * @returns observable con los datos de la cuenta en sesión
   */
  miPerfil() {
    return this.cliente.get<UserModel>(this.urlBase + '/me');
  }

  /**
   * Actualiza el nombre y el teléfono.
   *
   * @param datos datos nuevos
   * @returns observable con el mensaje del back
   */
  actualizarPerfil(datos: UpdateProfileModel) {
    return this.cliente.put(this.urlBase + '/updateprofile', datos, { responseType: 'text' });
  }

  /**
   * Cambia la contraseña. Si la actual no coincide, el back responde 401.
   *
   * @param datos contraseña actual y nueva
   * @returns observable con el mensaje del back
   */
  cambiarContrasena(datos: ChangePasswordModel) {
    return this.cliente.put(this.urlBase + '/changepassword', datos, { responseType: 'text' });
  }
}
