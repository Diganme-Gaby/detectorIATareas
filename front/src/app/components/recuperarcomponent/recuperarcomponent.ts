import { Component, inject, signal, WritableSignal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Authservice } from '../../services/authservice';
import { Toastrutil } from '../../util/toastrutil';

/** Recuperación de contraseña: pide el correo y el back envía el enlace para cambiarla. */
@Component({
  selector: 'app-recuperarcomponent',
  standalone: false,
  styleUrl: './recuperarcomponent.css',
  templateUrl: './recuperarcomponent.html',
})
export class Recuperarcomponent {
  /** Autenticación. */
  private auth: Authservice = inject(Authservice);
  /** Mensajes tipo toast. */
  private toastr: Toastrutil = inject(Toastrutil);

  /** Correo de la cuenta. */
  email: string = '';
  /** Indica si la petición está en curso. */
  enviando: WritableSignal<boolean> = signal(false);
  /** Se activa cuando el back recibió la solicitud. */
  enviado: WritableSignal<boolean> = signal(false);

  /** Envía la solicitud de recuperación. */
  solicitar(): void {
    this.enviando.set(true);
    this.auth.olvideContrasena(this.email.trim()).subscribe({
      next: (respuesta: string) => {
        this.enviando.set(false);
        this.enviado.set(true);
        this.toastr.info(respuesta, 'Solicitud enviada');
      },
      error: (error: HttpErrorResponse) => {
        this.enviando.set(false);
        this.toastr.error(this.auth.mensajeError(error, 'No se pudo enviar la solicitud.'));
      },
    });
  }
}
