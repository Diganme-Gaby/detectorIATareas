package co.edu.unbosque.proyectofinal.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cifra los datos sensibles con {@link AESUtil} usando un IV nuevo en cada cifrado.
 *
 * <p>AES en modo GCM nunca debe repetir el IV con la misma llave: si todos los datos usan el
 * mismo, quien vea la base de datos puede descifrarlos comparando registros. Por eso cada valor
 * se guarda como {@code iv:textoCifrado}, y al descifrar se usa el IV de ese mismo valor. La
 * llave sí es una sola y se lee del application.properties.
 */
public final class CifradoUtil {

  /** Separa el IV del texto cifrado. No aparece en Base64, así que no se confunde. */
  private static final String SEPARADOR = ":";

  /** Bytes aleatorios del IV, el tamaño recomendado para GCM. En Base64 son 16 caracteres. */
  private static final int BYTES_IV = 12;

  /** Generador seguro para los IV. */
  private static final SecureRandom ALEATORIO = new SecureRandom();

  /** Solo tiene métodos estáticos, así que no se instancia. */
  private CifradoUtil() {}

  /**
   * Cifra un texto con un IV aleatorio.
   *
   * @param llave llave AES de 16 caracteres
   * @param texto texto plano
   * @return {@code iv:textoCifrado}, o el mismo valor si viene vacío o null
   */
  public static String cifrar(String llave, String texto) {
    if (texto == null || texto.isBlank()) {
      return texto;
    }
    byte[] bytes = new byte[BYTES_IV];
    ALEATORIO.nextBytes(bytes);
    String iv = Base64.getEncoder().encodeToString(bytes);
    return iv + SEPARADOR + AESUtil.encrypt(llave, iv, texto);
  }

  /**
   * Descifra un valor guardado con {@link #cifrar(String, String)}.
   *
   * @param llave llave AES de 16 caracteres
   * @param guardado valor como está en la base de datos
   * @return texto plano, o null si el valor no tiene el formato {@code iv:textoCifrado} o no se
   *     pudo descifrar
   */
  public static String descifrar(String llave, String guardado) {
    if (!tieneFormato(guardado)) {
      return null;
    }
    int corte = guardado.indexOf(SEPARADOR);
    String texto = AESUtil.decrypt(llave, guardado.substring(0, corte), guardado.substring(corte + 1));
    return texto.isEmpty() ? null : texto;
  }

  /**
   * Indica si un valor se guardó con {@link #cifrar(String, String)}.
   *
   * @param guardado valor como está en la base de datos
   * @return true si tiene la forma {@code iv:textoCifrado}
   */
  public static boolean tieneFormato(String guardado) {
    return guardado != null && guardado.indexOf(SEPARADOR) > 0 && !guardado.endsWith(SEPARADOR);
  }
}
