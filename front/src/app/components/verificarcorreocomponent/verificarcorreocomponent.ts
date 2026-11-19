import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Authservice } from '../../services/authservice';
import { Toastrutil } from '../../util/toastrutil';

/**
 * Confirmación de correo.
 *
 * Es la página a la que lleva el enlace del correo de registro (`/verificar-correo?token=...`).
 * Si se entra sin token, o el enlace venció, permite pedir uno nuevo.
 */
@Component({
  selector: 'app-verificarcorreocomponent',
  standalone: false,
  styleUrl: './verificarcorreocomponent.css',
  templateUrl: './verificarcorreocomponent.html',
})
export class Verificarcorreocomponent implements OnInit {
  /** Autenticación. */
  private auth: Authservice = inject(Authservice);
  /** Ruta actual, para leer el token. */
  private ruta: ActivatedRoute = inject(ActivatedRoute);
  /** Mensajes tipo toast. */
  private toastr: Toastrutil = inject(Toastrutil);

  /** Estado de la página: `VERIFICANDO`, `CONFIRMADO`, `FALLIDO` o `REENVIAR` (sin token). */
  estado: WritableSignal<string> = signal('VERIFICANDO');
  /** Mensaje que devolvió el back. */
  mensaje: WritableSignal<string> = signal('');
  /** Correo para reenviar la confirmación. */
  email: string = '';
  /** Indica si el reenvío está en curso. */
  enviando: WritableSignal<boolean> = signal(false);

  /** Lee el token de la URL y lo envía al back. */
  ngOnInit(): void {
    const token: string | null = this.ruta.snapshot.queryParamMap.get('token');
    if (token === null || token === '') {
      this.estado.set('REENVIAR');
      return;
    }
    this.auth.verificarCorreo(token).subscribe({
      next: (respuesta: string) => {
        this.estado.set('CONFIRMADO');
        this.mensaje.set(respuesta);
      },
      error: (error: HttpErrorResponse) => {
        this.estado.set('FALLIDO');
        this.mensaje.set(this.auth.mensajeError(error, 'No pudimos confirmar el correo.'));
      },
    });
  }

  /** Pide un enlace de confirmación nuevo. */
  reenviar(): void {
    this.enviando.set(true);
    this.auth.reenviarVerificacion(this.email.trim()).subscribe({
      next: (respuesta: string) => {
        this.enviando.set(false);
        this.toastr.info(respuesta, 'Correo en camino');
      },
      error: (error: HttpErrorResponse) => {
        this.enviando.set(false);
        this.toastr.error(this.auth.mensajeError(error, 'No se pudo enviar el correo.'));
      },
    });
  }
}
