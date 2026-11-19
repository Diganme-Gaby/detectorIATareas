import { Component, inject, signal, WritableSignal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { NgForm } from '@angular/forms';
import { RegisterModel } from '../../models/auth.model';
import { Authservice } from '../../services/authservice';
import { Toastrutil } from '../../util/toastrutil';

/**
 * Registro de cuentas nuevas.
 *
 * Valida en pantalla las mismas reglas del back (usuario, correo, teléfono y contraseña) y, al
 * terminar, muestra el aviso de que llegó un correo para confirmar la cuenta.
 */
@Component({
  selector: 'app-registrocomponent',
  standalone: false,
  styleUrl: './registrocomponent.css',
  templateUrl: './registrocomponent.html',
})
export class Registrocomponent {
  /** Autenticación. */
  private auth: Authservice = inject(Authservice);
  /** Mensajes tipo toast. */
  private toastr: Toastrutil = inject(Toastrutil);

  /** Datos del formulario. */
  datos: RegisterModel = this.datosVacios();
  /** Contraseña repetida. */
  confirmacion: string = '';
  /** Indica si la petición está en curso. */
  cargando: WritableSignal<boolean> = signal(false);
  /** Correo al que se envió la confirmación; mientras esté vacío se muestra el formulario. */
  correoEnviado: WritableSignal<string> = signal('');

  /** Misma regla de contraseña que valida el back. */
  private readonly reglaPassword: RegExp = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,64}$/;
  /** Muestra u oculta la contraseña. */
  verPassword: WritableSignal<boolean> = signal(false);
  /** Muestra u oculta la confirmación de la contraseña. */
  verConfirmacion: WritableSignal<boolean> = signal(false);

  /**
   * @returns `true` si la contraseña cumple la regla
   */
  passwordValida(): boolean {
    return this.reglaPassword.test(this.datos.password);
  }

  /**
   * @returns `true` si las dos contraseñas son iguales
   */
  coinciden(): boolean {
    return this.datos.password === this.confirmacion;
  }

  /**
   * Envía el registro al back.
   *
   * @param formulario formulario, para limpiarlo al terminar
   */
  registrar(formulario: NgForm): void {
    if (!this.passwordValida() || !this.coinciden()) {
      this.toastr.advertencia('Revisa la contraseña antes de continuar.');
      return;
    }
    this.cargando.set(true);
    this.auth.registrar(this.datos).subscribe({
      next: (respuesta: string) => {
        this.cargando.set(false);
        this.correoEnviado.set(this.datos.email);
        this.toastr.exito(respuesta, 'Cuenta creada');
        formulario.resetForm();
        this.datos = this.datosVacios();
        this.confirmacion = '';
      },
      error: (error: HttpErrorResponse) => {
        this.cargando.set(false);
        this.toastr.error(this.auth.mensajeError(error, 'No se pudo crear la cuenta.'));
      },
    });
  }

  /**
   * @returns formulario vacío
   */
  private datosVacios(): RegisterModel {
    return { username: '', email: '', nombreCompleto: '', telefono: '', password: '' };
  }
}
