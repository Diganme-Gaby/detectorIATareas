import { Service } from '@angular/core';

/**
 * Convierte los valores que manda el back (fechas, porcentajes, estados, niveles) en textos para
 * mostrar. Los componentes lo inyectan como `formato` y lo usan en las interpolaciones.
 */
@Service()
export class Formatoutil {
  /** Nombres cortos de los meses. */
  private readonly meses: string[] = [
    'ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic',
  ];

  /**
   * Formatea una fecha como `11 nov 2026, 17:39`. El back la envía como texto ISO; si llega como
   * arreglo de números (otra forma en que json serializa fechas), también se entiende.
   *
   * @param valor fecha del back
   * @returns fecha legible, o una raya si no hay fecha
   */
  fecha(valor: string | number[] | null | undefined): string {
    if (valor === null || valor === undefined || valor === '') {
      return '-';
    }
    let fecha: Date;
    if (Array.isArray(valor)) {
      fecha = new Date(valor[0], valor[1] - 1, valor[2], valor[3] ?? 0, valor[4] ?? 0);
    } else {
      fecha = new Date(valor);
    }
    if (isNaN(fecha.getTime())) {
      return String(valor);
    }
    const dia: string = String(fecha.getDate()).padStart(2, '0');
    const hora: string = String(fecha.getHours()).padStart(2, '0');
    const minutos: string = String(fecha.getMinutes()).padStart(2, '0');
    return dia + ' ' + this.meses[fecha.getMonth()] + ' ' + fecha.getFullYear() + ', ' + hora + ':' + minutos;
  }

  /**
   * @param valor porcentaje de 0 a 100
   * @returns porcentaje con un decimal, por ejemplo `76,5 %`
   */
  porcentaje(valor: number | null | undefined): string {
    if (valor === null || valor === undefined) {
      return '-';
    }
    return valor.toFixed(1).replace('.', ',') + ' %';
  }

  /**
   * @param bytes tamaño en bytes
   * @returns tamaño en B, KB o MB
   */
  tamano(bytes: number): string {
    if (bytes < 1024) {
      return bytes + ' B';
    }
    if (bytes < 1024 * 1024) {
      return (bytes / 1024).toFixed(1).replace('.', ',') + ' KB';
    }
    return (bytes / (1024 * 1024)).toFixed(1).replace('.', ',') + ' MB';
  }

  /**
   * Convierte un valor en mayúsculas con guiones bajos en texto normal:
   * `COMPLETADO_PARCIAL` pasa a `Completado parcial`.
   *
   * @param valor valor del back
   * @returns texto legible
   */
  legible(valor: string | null | undefined): string {
    if (valor === null || valor === undefined || valor === '') {
      return '-';
    }
    const texto: string = valor.replaceAll('_', ' ').toLowerCase();
    return texto.charAt(0).toUpperCase() + texto.slice(1);
  }

  /**
   * @param nivel `ALTO`, `MEDIO`, `BAJO` o `SIN_RESULTADO`
   * @returns nivel para mostrar
   */
  nivel(nivel: string | null | undefined): string {
    switch (nivel) {
      case 'ALTO':
        return 'Alto';
      case 'MEDIO':
        return 'Medio';
      case 'BAJO':
        return 'Bajo';
      default:
        return 'Sin resultado';
    }
  }

  /**
   * @param nivel nivel de IA
   * @returns clases CSS de la etiqueta con el color del nivel
   */
  claseNivel(nivel: string | null | undefined): string {
    switch (nivel) {
      case 'ALTO':
        return 'chip nivel-alto';
      case 'MEDIO':
        return 'chip nivel-medio';
      case 'BAJO':
        return 'chip nivel-bajo';
      default:
        return 'chip nivel-ninguno';
    }
  }

  /**
   * @param estado estado del análisis
   * @returns clases CSS de la etiqueta con el color del estado
   */
  claseEstado(estado: string | null | undefined): string {
    switch (estado) {
      case 'COMPLETADO':
        return 'chip estado-ok';
      case 'COMPLETADO_PARCIAL':
        return 'chip estado-parcial';
      case 'FALLIDO':
        return 'chip estado-error';
      default:
        return 'chip estado-proceso';
    }
  }

  /**
   * @param origen `ARCHIVOS`, `ZIP` o `GITHUB`
   * @returns origen para mostrar
   */
  origen(origen: string | null | undefined): string {
    switch (origen) {
      case 'GITHUB':
        return 'GitHub';
      case 'ZIP':
        return 'Archivo ZIP';
      case 'ARCHIVOS':
        return 'Archivos';
      default:
        return this.legible(origen);
    }
  }

  /**
   * @param categoria tipo de archivo (también sirve para las claves de las estadísticas)
   * @returns tipo para mostrar
   */
  categoria(categoria: string | null | undefined): string {
    switch (categoria) {
      case 'CODIGO':
        return 'Código';
      case 'PDF':
        return 'PDF';
      case 'IMAGEN':
        return 'Imagen';
      default:
        return this.legible(categoria);
    }
  }

  /**
   * @param veredicto veredicto de la IA, por ejemplo `PROBABLEMENTE_IA`
   * @returns veredicto para mostrar
   */
  veredicto(veredicto: string | null | undefined): string {
    switch (veredicto) {
      case 'PROBABLEMENTE_IA':
        return 'Probablemente IA';
      case 'PROBABLEMENTE_HUMANO':
        return 'Probablemente humano';
      default:
        return this.legible(veredicto);
    }
  }
}
