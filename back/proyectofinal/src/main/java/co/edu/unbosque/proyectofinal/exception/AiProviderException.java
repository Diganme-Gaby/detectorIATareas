package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción que se lanza cuando una IA externa no responde o responde con error.
 *
 * <p>El mensaje trae el detalle técnico (proveedor, código HTTP y respuesta) y solo se escribe en el
 * log. Al usuario le llega un aviso genérico que no menciona cuotas ni tokens.
 *
 * @author Universidad El Bosque
 * @version 1.0
 */
public class AiProviderException extends Exception {

  /** Número de serie para la serialización. */
  private static final long serialVersionUID = 4127730981142250761L;

  /**
   * Crea la excepción con el detalle técnico.
   *
   * @param proveedor proveedor que falló
   * @param codigoHttp código HTTP recibido, o 0 si no hubo respuesta
   * @param detalle detalle técnico para el log
   */
  public AiProviderException(String proveedor, int codigoHttp, String detalle) {
    super(proveedor + " (" + codigoHttp + "): " + detalle);
  }
}
