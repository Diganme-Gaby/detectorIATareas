import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Authservice } from '../../services/authservice';

/**
 * Página 404. La muestra el enrutador cuando la URL no coincide con ninguna ruta.
 */
@Component({
  selector: 'app-noencontradocomponent',
  standalone: false,
  styleUrl: './noencontradocomponent.css',
  templateUrl: './noencontradocomponent.html',
})
export class Noencontradocomponent {
  /** Sesión actual, para ofrecer el enlace adecuado. */
  auth: Authservice = inject(Authservice);

  /** Enrutador, para mostrar la dirección que no existe. */
  private router: Router = inject(Router);

  /** Dirección que se intentó abrir. */
  ruta: string = this.router.url;
}
