import { Component, inject, signal, WritableSignal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthResponseModel, LoginModel } from '../../models/auth.model';
import { Authservice } from '../../services/authservice';
import { Toastrutil } from '../../util/toastrutil';

/**
 * Inicio de sesión.
 *
 * Al entrar guarda la sesión y vuelve a la página que se quería abrir (parámetro `volver`,
 * que pone el guard) o a `/analizar`. Si la cuenta no ha confirmado el correo, ofrece reenviar
 * el enlace.
 */
@Component({
  selector: 'app-logincomponent',
  standalone: false,
  styleUrl: './logincomponent.css',
  templateUrl: './logincomponent.html',
})
export class Logincomponent {
  /** Autenticación. */
  private auth: Authservice = inject(Authservice);
  /** Enrutador. */
  private router: Router = inject(Router);
  /** Ruta actual, para leer el parámetro `volver`. */
  private ruta: ActivatedRoute = inject(ActivatedRoute);
  /** Mensajes tipo toast. */
  private toastr: Toastrutil = inject(Toastrutil);

  /** Datos del formulario, enlazados con `ngModel`. */
  datos: LoginModel = { username: '', password: '' };
  /** Indica si la petición está en curso. */
  cargando: WritableSignal<boolean> = signal(false);
  /** Se activa cuando el back responde que falta confirmar el correo. */
  sinVerificar: WritableSignal<boolean> = signal(false);
  /** Muestra u oculta la contraseña. */
  verPassword: WritableSignal<boolean> = signal(false);

  /** Envía el usuario y la contraseña al back. */
  iniciarSesion(): void {
    this.cargando.set(true);
    this.sinVerificar.set(false);
    this.auth.login(this.datos).subscribe({
      next: (respuesta: AuthResponseModel) => {
        this.auth.guardarSesion(respuesta);
        this.cargando.set(false);
        this.toastr.exito('Hola, ' + respuesta.username + '.', 'Sesión iniciada');
        const volver: string | null = this.ruta.snapshot.queryParamMap.get('volver');
        this.router.navigateByUrl(volver !== null && volver.startsWith('/') ? volver : '/analizar');
      },
      error: (error: HttpErrorResponse) => {
        this.cargando.set(false);
        const mensaje: string = this.auth.mensajeError(error, 'No se pudo iniciar sesión.');
        this.sinVerificar.set(mensaje.toLowerCase().includes('confirmar tu correo'));
        this.toastr.error(mensaje);
      },
    });
  }
}
