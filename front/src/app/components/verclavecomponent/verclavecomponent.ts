import { Component, model, ModelSignal } from '@angular/core';

/**
 * Botón con el ícono de ojo que muestra u oculta la contraseña de un campo.
 *
 * Se enlaza en doble vía con una señal de la página que lo usa: `[(visible)]="verPassword"`.
 * La página cambia el `type` de su input según esa señal, así el campo sigue dentro del
 * formulario y conserva sus validaciones.
 */
@Component({
  selector: 'app-verclavecomponent',
  standalone: false,
  styleUrl: './verclavecomponent.css',
  templateUrl: './verclavecomponent.html',
})
export class Verclavecomponent {
  /** True si la contraseña se está mostrando. */
  visible: ModelSignal<boolean> = model.required<boolean>();

  /** Cambia entre mostrar y ocultar la contraseña. */
  alternar(): void {
    this.visible.set(!this.visible());
  }
}
