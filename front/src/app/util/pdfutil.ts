import { Service } from '@angular/core';

/**
 * Muestra o descarga los reportes PDF.
 *
 * Los PDF llegan como `Blob` porque la petición lleva el token en el encabezado; por eso no se
 * pueden abrir con un enlace normal y hay que crear una URL temporal en el navegador.
 */
@Service()
export class Pdfutil {
  /**
   * Abre una pestaña vacía. Se llama antes de pedir el PDF para que el navegador no la bloquee
   * como ventana emergente.
   *
   * @returns la pestaña abierta, o `null` si el navegador no la dejó abrir
   */
  abrirPestana(): Window | null {
    return window.open('', '_blank');
  }

  /**
   * Muestra el PDF en la pestaña abierta antes, o en una nueva si no hay.
   *
   * @param pdf contenido del PDF
   * @param pestana pestaña donde mostrarlo
   */
  mostrar(pdf: Blob, pestana: Window | null): void {
    const url: string = URL.createObjectURL(pdf);
    if (pestana !== null) {
      pestana.location.href = url;
    } else {
      window.open(url, '_blank');
    }
    setTimeout(() => URL.revokeObjectURL(url), 60000);
  }

  /**
   * Descarga el PDF con el nombre indicado.
   *
   * @param pdf contenido del PDF
   * @param nombre nombre del archivo
   */
  descargar(pdf: Blob, nombre: string): void {
    const url: string = URL.createObjectURL(pdf);
    const enlace: HTMLAnchorElement = document.createElement('a');
    enlace.href = url;
    enlace.download = nombre;
    enlace.click();
    setTimeout(() => URL.revokeObjectURL(url), 10000);
  }
}
