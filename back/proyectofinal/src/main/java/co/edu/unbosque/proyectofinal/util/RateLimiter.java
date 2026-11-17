package co.edu.unbosque.proyectofinal.util;

/**
 * Separa las peticiones a un proveedor de IA por un tiempo mínimo. Así se respetan los límites por
 * minuto de los planes gratuitos aunque haya varios análisis al mismo tiempo.
 */
public class RateLimiter {

  /** Milisegundos minimos entre dos peticiones. */
  private final long intervaloMs;

  /** Momento en que se puede hacer la siguiente petición. */
  private long siguienteTurno;

  /**
   * Constructor con los datos indicados.
   *
   * @param intervaloMs milisegundos minimos entre peticiones; 0 desactiva la espera
   */
  public RateLimiter(long intervaloMs) {
    this.intervaloMs = Math.max(0, intervaloMs);
    this.siguienteTurno = 0;
  }

  /**
   * Bloquea el hilo hasta que sea su turno.
   *
   * @throws InterruptedException si el hilo se interrumpe mientras espera
   */
  public void esperarTurno() throws InterruptedException {
    long espera;
    synchronized (this) {
      long ahora = System.currentTimeMillis();
      long turno = Math.max(ahora, siguienteTurno);
      siguienteTurno = turno + intervaloMs;
      espera = turno - ahora;
    }
    if (espera > 0) {
      Thread.sleep(espera);
    }
  }

  /**
   * Devuelve el intervalo configurado en milisegundos.
   *
   * @return intervalo configurado en milisegundos
   */
  public long getIntervaloMs() {
    return intervaloMs;
  }
}
