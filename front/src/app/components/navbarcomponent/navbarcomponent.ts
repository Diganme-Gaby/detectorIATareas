import { Component, inject, signal, WritableSignal } from '@angular/core';
import { Authservice } from '../../services/authservice';

/**
 * Barra de navegación que aparece en todas las páginas.
 *
 * Muestra enlaces distintos según haya sesión y según el rol: el administrador ve además
 * Usuarios, Análisis, Auditoría y Estadísticas. En pantallas pequeñas los enlaces se
 * despliegan con un botón.
 */
@Component({
  selector: 'app-navbarcomponent',
  standalone: false,
  styleUrl: './navbarcomponent.css',
  templateUrl: './navbarcomponent.html',
})
export class Navbarcomponent {
  /** Sesión actual; la plantilla la usa para decidir qué enlaces mostrar. */
  auth: Authservice = inject(Authservice);

  /** Indica si el menú está desplegado en pantallas pequeñas. */
  menuAbierto: WritableSignal<boolean> = signal(false);

  /** Abre o cierra el menú en pantallas pequeñas. */
  alternarMenu(): void {
    this.menuAbierto.set(!this.menuAbierto());
  }

  /** Cierra el menú; se llama al elegir un enlace. */
  cerrarMenu(): void {
    this.menuAbierto.set(false);
  }

  /** Cierra el menú y la sesión. */
  cerrarSesion(): void {
    this.cerrarMenu();
    this.auth.cerrarSesion();
  }
}
