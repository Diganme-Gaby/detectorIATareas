import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { NgForm } from '@angular/forms';
import { ChangePasswordModel, UpdateProfileModel, UserModel } from '../../models/user.model';
import { Authservice } from '../../services/authservice';
import { Userservice } from '../../services/userservice';
import { Formatoutil } from '../../util/formatoutil';
import { Toastrutil } from '../../util/toastrutil';

/**
 * Perfil del usuario en sesión: datos de la cuenta, cambio de nombre y teléfono, y cambio de
 * contraseña. El usuario y el correo no se pueden modificar.
 */
@Component({
  selector: 'app-perfilcomponent',
  standalone: false,
  styleUrl: './perfilcomponent.css',
  templateUrl: './perfilcomponent.html',
})
export class Perfilcomponent implements OnInit {
  /** Perfil. */
  private userService: Userservice = inject(Userservice);
  /** Autenticación, para leer los mensajes de error. */
  private auth: Authservice = inject(Authservice);
  /** Mensajes tipo toast. */
  private toastr: Toastrutil = inject(Toastrutil);
  /** Formato de fechas para la plantilla. */
  formato: Formatoutil = inject(Formatoutil);

  /** Datos de la cuenta, o `null` mientras cargan. */
  perfil: WritableSignal<UserModel | null> = signal(null);
  /** Indica si se están guardando los datos personales. */
  guardando: WritableSignal<boolean> = signal(false);
  /** Indica si se está cambiando la contraseña. */
  cambiando: WritableSignal<boolean> = signal(false);

  /** Formulario de datos personales. */
  datos: UpdateProfileModel = { nombreCompleto: '', telefono: '' };
  /** Formulario de contraseña. */
  claves: ChangePasswordModel = { passwordActual: '', passwordNueva: '' };
  /** Contraseña nueva repetida. */
  confirmacion: string = '';

  /** Misma regla de contraseña que valida el back. */
  private readonly reglaPassword: RegExp = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,64}$/;
  /** Muestra u oculta la contraseña actual. */
  verActual: WritableSignal<boolean> = signal(false);
  /** Muestra u oculta la contraseña nueva. */
  verNueva: WritableSignal<boolean> = signal(false);
  /** Muestra u oculta la confirmación de la contraseña. */
  verConfirmacion: WritableSignal<boolean> = signal(false);

  /** Carga el perfil al entrar. */
  ngOnInit(): void {
    this.cargar();
  }

  /** Pide el perfil al back y llena el formulario. */
  private cargar(): void {
    this.userService.miPerfil().subscribe({
      next: (usuario: UserModel) => {
        this.perfil.set(usuario);
        this.datos = { nombreCompleto: usuario.nombreCompleto, telefono: usuario.telefono ?? '' };
      },
      error: (error: HttpErrorResponse) =>
        this.toastr.error(this.auth.mensajeError(error, 'No se pudo cargar tu perfil.')),
    });
  }

  /** Guarda el nombre y el teléfono. */
  guardarPerfil(): void {
    this.guardando.set(true);
    this.userService.actualizarPerfil(this.datos).subscribe({
      next: (respuesta: string) => {
        this.guardando.set(false);
        this.toastr.exito(respuesta);
        this.cargar();
      },
      error: (error: HttpErrorResponse) => {
        this.guardando.set(false);
        this.toastr.error(this.auth.mensajeError(error, 'No se pudo actualizar el perfil.'));
      },
    });
  }

  /**
   * @returns `true` si la contraseña nueva cumple la regla
   */
  passwordValida(): boolean {
    return this.reglaPassword.test(this.claves.passwordNueva);
  }

  /**
   * @returns `true` si las dos contraseñas nuevas son iguales
   */
  coinciden(): boolean {
    return this.claves.passwordNueva === this.confirmacion;
  }

  /**
   * Cambia la contraseña.
   *
   * @param formulario formulario, para limpiarlo al terminar
   */
  cambiarContrasena(formulario: NgForm): void {
    this.cambiando.set(true);
    this.userService.cambiarContrasena(this.claves).subscribe({
      next: (respuesta: string) => {
        this.cambiando.set(false);
        this.toastr.exito(respuesta);
        formulario.resetForm();
        this.claves = { passwordActual: '', passwordNueva: '' };
        this.confirmacion = '';
      },
      error: (error: HttpErrorResponse) => {
        this.cambiando.set(false);
        this.toastr.error(this.auth.mensajeError(error, 'No se pudo cambiar la contraseña.'));
      },
    });
  }
}
