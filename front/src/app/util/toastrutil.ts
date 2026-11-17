import { inject, Service } from '@angular/core';
import { ToastrService } from 'ngx-toastr';

/**
 * Mensajes emergentes para el usuario con ngx-toastr.
 *
 * Todos los componentes muestran sus avisos por aquí, para que tengan el mismo estilo y los
 * mismos títulos por defecto.
 */
@Service()
export class Toastrutil {
  /** Servicio de ngx-toastr. */
  private toastr: ToastrService = inject(ToastrService);

  /**
   * Mensaje de éxito (borde verde).
   *
   * @param mensaje texto del mensaje
   * @param titulo título; por defecto "Listo"
   */
  exito(mensaje: string, titulo: string = 'Listo'): void {
    this.toastr.success(mensaje, titulo);
  }

  /**
   * Mensaje de error (borde coral).
   *
   * @param mensaje texto del mensaje
   * @param titulo título; por defecto "Algo salió mal"
   */
  error(mensaje: string, titulo: string = 'Algo salió mal'): void {
    this.toastr.error(mensaje, titulo);
  }

  /**
   * Mensaje informativo (borde violeta).
   *
   * @param mensaje texto del mensaje
   * @param titulo título; por defecto "Información"
   */
  info(mensaje: string, titulo: string = 'Información'): void {
    this.toastr.info(mensaje, titulo);
  }

  /**
   * Mensaje de advertencia (borde ámbar).
   *
   * @param mensaje texto del mensaje
   * @param titulo título; por defecto "Atención"
   */
  advertencia(mensaje: string, titulo: string = 'Atención'): void {
    this.toastr.warning(mensaje, titulo);
  }
}
