import { Component } from '@angular/core';

/** Pie de página que aparece en todas las páginas, con los datos del proyecto. */
@Component({
  selector: 'app-footercomponent',
  standalone: false,
  styleUrl: './footercomponent.css',
  templateUrl: './footercomponent.html',
})
export class Footercomponent {
  /** Año actual para el aviso de derechos. */
  anio: number = new Date().getFullYear();
}
