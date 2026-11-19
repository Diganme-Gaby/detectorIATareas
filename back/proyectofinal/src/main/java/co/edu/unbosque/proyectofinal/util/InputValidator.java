package co.edu.unbosque.proyectofinal.util;

import co.edu.unbosque.proyectofinal.exception.PasswordNotValidException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Validaciones de los datos que llegan desde el front. Se aplican en los controllers antes de
 * llamar a los servicios.
 */
public final class InputValidator {

  /** Letras, números, punto, guion y guion bajo; de 4 a 30 caracteres. */
  private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._-]{4,30}$");

  /** Formato básico de correo. */
  private static final Pattern EMAIL =
      Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

  /** Digitos, espacios, guiones y el signo +; de 7 a 20 caracteres. */
  private static final Pattern TELEFONO = Pattern.compile("^[0-9+\\- ]{7,20}$");

  /** Al menos una minúscula, una mayúscula, un número y un símbolo; de 8 a 64 caracteres. */
  private static final Pattern PASSWORD =
      Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,64}$");

  /** Solo tiene métodos estáticos, así que no se instancia. */
  private InputValidator() {}

  /**
   * Devuelve true si tiene el formato permitido.
   *
   * @param username nombre de usuario
   * @return true si tiene el formato permitido
   */
  public static boolean usernameValido(String username) {
    return username != null && USERNAME.matcher(username).matches();
  }

  /**
   * Devuelve true si tiene formato de correo y no supera 120 caracteres.
   *
   * @param email correo
   * @return true si tiene formato de correo y no supera 120 caracteres
   */
  public static boolean emailValido(String email) {
    return email != null && email.length() <= 120 && EMAIL.matcher(email).matches();
  }

  /**
   * El teléfono es opcional: vacío también es válido.
   *
   * @param telefono teléfono
   * @return true si está vacío o tiene formato de teléfono
   */
  public static boolean telefonoValido(String telefono) {
    return telefono == null || telefono.isBlank() || TELEFONO.matcher(telefono).matches();
  }

  /**
   * Un nombre válido no está vacío, no pasa de 100 caracteres y no trae caracteres de HTML.
   *
   * @param nombre nombre completo
   * @return true si se puede guardar
   */
  public static boolean nombreValido(String nombre) {
    return nombre != null && !nombre.isBlank() && nombre.length() <= 100 && !tieneHtml(nombre);
  }

  /**
   * Detecta caracteres que se usan para inyectar HTML o scripts.
   *
   * @param texto texto a revisar
   * @return true si contiene alguno de los caracteres &lt; &gt; " '
   */
  public static boolean tieneHtml(String texto) {
    if (texto == null) {
      return false;
    }
    return texto.contains("<") || texto.contains(">") || texto.contains("\"") || texto.contains("'");
  }

  /**
   * Devuelve true si cumple el estándar.
   *
   * @param password contraseña en texto plano
   * @return true si cumple el estándar
   */
  public static boolean passwordValida(String password) {
    return password != null && PASSWORD.matcher(password).matches();
  }

  /**
   * Lanza la excepción de la guía si la contraseña no cumple el estándar.
   *
   * @param password contraseña en texto plano
   * @throws PasswordNotValidException si no cumple
   */
  public static void validarPassword(String password) throws PasswordNotValidException {
    if (!passwordValida(password)) {
      throw new PasswordNotValidException();
    }
  }

  /**
   * Convierte un filtro de fecha recibido como texto (yyyy-MM-dd).
   *
   * @param texto fecha en formato ISO, o null
   * @return la fecha, o null si viene vacía o con un formato que no es válido
   */
  public static LocalDate fecha(String texto) {
    if (texto == null || texto.isBlank()) {
      return null;
    }
    try {
      return LocalDate.parse(texto.trim());
    } catch (DateTimeParseException e) {
      return null;
    }
  }
}
