import { Component, input, InputSignal, output, OutputEmitterRef } from '@angular/core';

/**
 * Controles de paginación para las tablas: "Mostrando 1-10 de 54", páginas anterior y
 * siguiente, números de página y cantidad de filas por página (10 o 15).
 *
 * No guarda la lista; la página que la usa recibe los cambios y corta sus datos.
 */
@Component({
  selector: 'app-paginacioncomponent',
  standalone: false,
  styleUrl: './paginacioncomponent.css',
  templateUrl: './paginacioncomponent.html',
})
export class Paginacioncomponent {
  /** Cantidad total de filas. */
  total: InputSignal<number> = input.required<number>();
  /** Página actual, empezando en 1. */
  pagina: InputSignal<number> = input.required<number>();
  /** Filas por página. */
  porPagina: InputSignal<number> = input.required<number>();

  /** Se emite con la página elegida. */
  paginaCambiada: OutputEmitterRef<number> = output<number>();
  /** Se emite con la nueva cantidad de filas por página. */
  porPaginaCambiado: OutputEmitterRef<number> = output<number>();

  /** Opciones de filas por página. */
  readonly opciones: number[] = [10, 15];

  /**
   * @returns cantidad de páginas (mínimo 1)
   */
  totalPaginas(): number {
    return Math.max(1, Math.ceil(this.total() / this.porPagina()));
  }

  /**
   * @returns número de la primera fila visible
   */
  desde(): number {
    return this.total() === 0 ? 0 : (this.pagina() - 1) * this.porPagina() + 1;
  }

  /**
   * @returns número de la última fila visible
   */
  hasta(): number {
    return Math.min(this.pagina() * this.porPagina(), this.total());
  }

  /**
   * Números de página que se muestran: hasta cinco alrededor de la actual.
   *
   * @returns páginas visibles
   */
  paginas(): number[] {
    const total: number = this.totalPaginas();
    const inicio: number = Math.max(1, Math.min(this.pagina() - 2, total - 4));
    const fin: number = Math.min(total, inicio + 4);
    const lista: number[] = [];
    for (let i = inicio; i <= fin; i++) {
      lista.push(i);
    }
    return lista;
  }

  /**
   * Pide ir a una página, si es válida y distinta de la actual.
   *
   * @param pagina página elegida
   */
  ir(pagina: number): void {
    if (pagina >= 1 && pagina <= this.totalPaginas() && pagina !== this.pagina()) {
      this.paginaCambiada.emit(pagina);
    }
  }

  /**
   * Cambia la cantidad de filas por página.
   *
   * @param evento evento `change` del selector
   */
  cambiarTamano(evento: Event): void {
    this.porPaginaCambiado.emit(Number((evento.target as HTMLSelectElement).value));
  }
}
