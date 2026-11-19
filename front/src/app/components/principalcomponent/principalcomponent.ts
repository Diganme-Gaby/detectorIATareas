import {
  AfterViewInit,
  Component,
  ElementRef,
  inject,
  OnDestroy,
  ViewChild,
} from '@angular/core';
import { Authservice } from '../../services/authservice';

/** Un triángulo de la constelación de la portada. */
interface Particula {
  /** Posición horizontal base. */
  x: number;
  /** Posición vertical base. */
  y: number;
  /** Radio del triángulo en píxeles. */
  tamano: number;
  /** Ángulo inicial. */
  giro: number;
  /** Velocidad con la que rota. */
  velocidadGiro: number;
  /** Desfase para que no todos se muevan igual. */
  fase: number;
  /** Color del contorno. */
  color: string;
  /** Opacidad base. */
  opacidad: number;
}

/**
 * Portada del aplicativo.
 *
 * Presenta qué hace el detector y lleva a crear cuenta o a analizar. A la derecha dibuja en un
 * `canvas` una constelación animada de triángulos con forma de cerebro, la imagen central del
 * diseño. Si el sistema pide reducir el movimiento, se dibuja quieta.
 */
@Component({
  selector: 'app-principalcomponent',
  standalone: false,
  styleUrl: './principalcomponent.css',
  templateUrl: './principalcomponent.html',
})
export class Principalcomponent implements AfterViewInit, OnDestroy {
  /** Sesión actual, para cambiar los botones si ya hay sesión. */
  auth: Authservice = inject(Authservice);

  /** Canvas donde se dibuja la constelación. */
  @ViewChild('lienzo') lienzo!: ElementRef<HTMLCanvasElement>;

  /** Pasos de la sección "Cómo funciona". */
  pasos: { numero: string; titulo: string; texto: string }[] = [
    {
      numero: '01',
      titulo: 'Sube lo que quieras revisar',
      texto:
        'Documentos, código, imágenes, audios, videos o un .zip con todo junto. También puedes pegar el enlace de un repositorio público de GitHub.',
    },
    {
      numero: '02',
      titulo: 'Tres modelos lo leen por separado',
      texto:
        'Cada archivo pasa por Gemini, Groq y OpenRouter. Si alguno no responde, entra un modelo de respaldo para que siempre tengas las tres opiniones.',
    },
    {
      numero: '03',
      titulo: 'Recibes el reporte',
      texto:
        'Un porcentaje por modelo, el promedio y los motivos de cada uno. El PDF llega a tu correo y queda guardado en tu historial.',
    },
  ];

  /** Triángulos que forman el cerebro. */
  private particulas: Particula[] = [];
  /** Triángulos sueltos alrededor, más tenues. */
  private ambiente: Particula[] = [];
  /** Identificador del cuadro de animación, para detenerla. */
  private cuadro: number = 0;
  /** Ancho del canvas en píxeles CSS. */
  private ancho: number = 0;
  /** Alto del canvas en píxeles CSS. */
  private alto: number = 0;
  /** Función que se registra en el evento `resize` de la ventana. */
  private alRedimensionar = (): void => this.ajustarTamano();

  /** Colores de los triángulos, tomados de la paleta del diseño. */
  private readonly colores: string[] = [
    '#8052ff',
    '#a68bff',
    '#ffb829',
    '#15846e',
    '#3cc5a2',
    '#c85cff',
    '#4f7bff',
    '#ff5fa2',
  ];

  /** Prepara el canvas y arranca la animación cuando la vista ya existe. */
  ngAfterViewInit(): void {
    this.ajustarTamano();
    window.addEventListener('resize', this.alRedimensionar);
    const reducido: boolean = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (reducido) {
      this.dibujar(0);
    } else {
      this.animar();
    }
  }

  /** Detiene la animación y quita el evento al salir de la página. */
  ngOnDestroy(): void {
    cancelAnimationFrame(this.cuadro);
    window.removeEventListener('resize', this.alRedimensionar);
  }

  /**
   * Ajusta el canvas a su tamaño en pantalla y a la densidad de píxeles, y vuelve a
   * repartir los triángulos.
   */
  private ajustarTamano(): void {
    const canvas: HTMLCanvasElement = this.lienzo.nativeElement;
    const escala: number = window.devicePixelRatio || 1;
    this.ancho = canvas.clientWidth;
    this.alto = canvas.clientHeight;
    canvas.width = this.ancho * escala;
    canvas.height = this.alto * escala;
    canvas.getContext('2d')?.setTransform(escala, 0, 0, escala, 0, 0);
    this.crearParticulas();
  }

  /**
   * Reparte los triángulos dentro de una silueta de cerebro: dos lóbulos, el cerebelo y el
   * tallo. Hay más densidad hacia el borde para que se note la forma.
   */
  private crearParticulas(): void {
    const cx: number = this.ancho / 2;
    const cy: number = this.alto / 2;
    const r: number = Math.min(this.ancho, this.alto) * 0.36;
    const formas: { x: number; y: number; rx: number; ry: number; peso: number }[] = [
      { x: cx - r * 0.42, y: cy - r * 0.12, rx: r * 0.62, ry: r * 0.58, peso: 0.38 },
      { x: cx + r * 0.38, y: cy - r * 0.16, rx: r * 0.66, ry: r * 0.56, peso: 0.38 },
      { x: cx + r * 0.52, y: cy + r * 0.5, rx: r * 0.34, ry: r * 0.22, peso: 0.14 },
      { x: cx + r * 0.08, y: cy + r * 0.62, rx: r * 0.12, ry: r * 0.3, peso: 0.1 },
    ];
    const total: number = this.ancho < 500 ? 520 : 900;
    this.particulas = [];
    for (let i = 0; i < total; i++) {
      let forma = formas[0];
      let azar: number = Math.random();
      for (const f of formas) {
        if (azar < f.peso) {
          forma = f;
          break;
        }
        azar -= f.peso;
      }
      // Más densidad hacia el borde para que se note la silueta.
      const angulo: number = Math.random() * Math.PI * 2;
      const distancia: number = Math.pow(Math.random(), 0.45);
      this.particulas.push(
        this.nuevaParticula(
          forma.x + Math.cos(angulo) * forma.rx * distancia,
          forma.y + Math.sin(angulo) * forma.ry * distancia,
          0.55 + Math.random() * 0.45,
        ),
      );
    }
    this.ambiente = [];
    for (let i = 0; i < 90; i++) {
      this.ambiente.push(
        this.nuevaParticula(Math.random() * this.ancho, Math.random() * this.alto, 0.15 + Math.random() * 0.2),
      );
    }
  }

  /**
   * Crea un triángulo con tamaño, giro y color al azar.
   *
   * @param x posición horizontal
   * @param y posición vertical
   * @param opacidad opacidad base
   * @returns triángulo nuevo
   */
  private nuevaParticula(x: number, y: number, opacidad: number): Particula {
    return {
      x,
      y,
      tamano: 1.6 + Math.random() * 3.2,
      giro: Math.random() * Math.PI * 2,
      velocidadGiro: (Math.random() - 0.5) * 0.01,
      fase: Math.random() * Math.PI * 2,
      color: this.colores[Math.floor(Math.random() * this.colores.length)],
      opacidad,
    };
  }

  /** Dibuja un cuadro tras otro con `requestAnimationFrame`. */
  private animar(): void {
    const paso = (tiempo: number): void => {
      this.dibujar(tiempo);
      this.cuadro = requestAnimationFrame(paso);
    };
    this.cuadro = requestAnimationFrame(paso);
  }

  /**
   * Dibuja todos los triángulos en el instante indicado.
   *
   * @param tiempo milisegundos desde que arrancó la página
   */
  private dibujar(tiempo: number): void {
    const ctx: CanvasRenderingContext2D | null = this.lienzo.nativeElement.getContext('2d');
    if (ctx === null) {
      return;
    }
    ctx.clearRect(0, 0, this.ancho, this.alto);
    ctx.lineWidth = 1;
    const t: number = tiempo / 1000;
    for (const p of this.ambiente) {
      this.triangulo(ctx, p, t, 6);
    }
    for (const p of this.particulas) {
      this.triangulo(ctx, p, t, 2.2);
    }
  }

  /**
   * Dibuja el contorno de un triángulo con un pequeño vaivén.
   *
   * @param ctx contexto 2D del canvas
   * @param p triángulo a dibujar
   * @param t tiempo en segundos
   * @param amplitud cuánto se desplaza
   */
  private triangulo(ctx: CanvasRenderingContext2D, p: Particula, t: number, amplitud: number): void {
    const x: number = p.x + Math.sin(t * 0.6 + p.fase) * amplitud;
    const y: number = p.y + Math.cos(t * 0.5 + p.fase) * amplitud;
    const giro: number = p.giro + t * p.velocidadGiro * 60;
    ctx.globalAlpha = p.opacidad * (0.75 + Math.sin(t * 1.4 + p.fase) * 0.25);
    ctx.strokeStyle = p.color;
    ctx.beginPath();
    for (let i = 0; i < 3; i++) {
      const a: number = giro + (i * Math.PI * 2) / 3;
      const px: number = x + Math.cos(a) * p.tamano;
      const py: number = y + Math.sin(a) * p.tamano;
      if (i === 0) {
        ctx.moveTo(px, py);
      } else {
        ctx.lineTo(px, py);
      }
    }
    ctx.closePath();
    ctx.stroke();
    ctx.globalAlpha = 1;
  }
}
