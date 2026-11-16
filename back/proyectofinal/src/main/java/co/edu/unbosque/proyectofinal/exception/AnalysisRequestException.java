package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción que se lanza cuando una solicitud de análisis no se puede aceptar.
 *
 * <p>Se usa cuando no llegan archivos, llegan demasiados, la URL de GitHub no es válida o el
 * repositorio no se puede descargar. El mensaje se muestra tal cual al usuario.
 *
 * @author Universidad El Bosque
 * @version 1.0
 */
public class AnalysisRequestException extends Exception {

  /** Número de serie para la serialización. */
  private static final long serialVersionUID = -6023180144958421379L;

  /**
   * Crea la excepción con un mensaje para el usuario.
   *
   * @param mensaje explicación del problema
   */
  public AnalysisRequestException(String mensaje) {
    super(mensaje);
  }
}
