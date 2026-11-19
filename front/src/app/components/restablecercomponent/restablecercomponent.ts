import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { Authservice } from '../../services/authservice';
import { Toastrutil } from '../../util/toastrutil';

/**
 * Contraseña nueva.
 *
 * Es la página a la que lleva el enlace de recuperación (`/restablecer-contrasena?token=...`).
 */
@Component({
  selector: 'app-restablecercomponent',
  standalone: false,
  styleUrl: './restablecercomponent.css',
  templateUrl: './restablecercomponent.html',
})
export class Restablecercomponent implements OnInit {
  /** Autenticación. */
  private auth: Authservice = inject(Authservice);
  /** Ruta actual, para leer el token. */
  private ruta: ActivatedRoute = inject(ActivatedRoute);
  /** Enrutador, para ir al login al terminar. */
  private router: Router = inject(Router);
  /** Mensajes tipo toast. */
  private toastr: Toastrutil = inject(Toastrutil);

  /** Código del enlace; vacío si se entró sin él. */
  token: string = '';
  /** Contraseña nueva. */
  passwordNueva: string = '';
  /** Contraseña repetida. */
  confirmacion: string = '';
  /** Indica si la petición está en curso. */
  guardando: WritableSignal<boolean> = signal(false);

  /** Misma regla de contraseña que valida el back. */
  private readonly reglaPassword: RegExp = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,64}$/;
  /** Muestra u oculta la contraseña nueva. */
  verNueva: WritableSignal<boolean> = signal(false);
  /** Muestra u oculta la confirmación de la contraseña. */
  verConfirmacion: WritableSignal<boolean> = signal(false);

  /** Lee el token de la URL. */
  ngOnInit(): void {
    this.token = this.ruta.snapshot.queryParamMap.get('token') ?? '';
  }

  /**
   * @returns `true` si la contraseña cumple la regla
   */
  passwordValida(): boolean {
    return this.reglaPassword.test(this.passwordNueva);
  }

  /**
   * @returns `true` si las dos contraseñas son iguales
   */
  coinciden(): boolean {
    return this.passwordNueva === this.confirmacion;
  }

  /** Envía la contraseña nueva al back. */
  guardar(): void {
    this.guardando.set(true);
    this.auth
      .restablecerContrasena({ token: this.token, passwordNueva: this.passwordNueva })
      .subscribe({
        next: (respuesta: string) => {
          this.guardando.set(false);
          this.toastr.exito(respuesta, 'Contraseña actualizada');
          this.router.navigate(['/login']);
        },
        error: (error: HttpErrorResponse) => {
          this.guardando.set(false);
          this.toastr.error(this.auth.mensajeError(error, 'No se pudo cambiar la contraseña.'));
        },
      });
  }
}
