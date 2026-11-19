package co.edu.unbosque.proyectofinal.model;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Código de un solo uso que viaja en los enlaces de correo: confirmar la cuenta o restablecer la
 * contraseña.
 */
@Entity
@Table(name = "token_correo")
public class EmailToken {

  /** Identificador generado por la base de datos. */
  private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

  /** Valor aleatorio que va en el enlace. */
  @Column(unique = true, nullable = false, length = 80)
  private String token;

  /** Para que sirve el código. */
  @Enumerated(EnumType.STRING)
  private TokenType tipo;

  /** Id del usuario dueño del código. */
  @Column(nullable = false)
  private Long userId;

  /** Momento a partir del cual el código ya no sirve. */
  private LocalDateTime expiraEn;

  /** El código ya se uso. */
  private boolean usado;

  /** Momento de creación. */
  private LocalDateTime creadoEn;

  /** Tipos de código que se envían por correo. */
  public enum TokenType {
    /** Confirmación de la cuenta después del registro. */
    VERIFICACION,
    /** Restablecimiento de contraseña olvidada. */
    RECUPERACION
  }

  /** Constructor por defecto requerido por JPA. */
  public EmailToken() {
    this.creadoEn = LocalDateTime.now();
  }

  /**
   * Crea un código listo para guardar.
   *
   * @param token valor aleatorio
   * @param tipo uso del código
   * @param userId id del usuario dueño
   * @param expiraEn vencimiento
   */
  public EmailToken(String token, TokenType tipo, Long userId, LocalDateTime expiraEn) {
    this();
    this.token = token;
    this.tipo = tipo;
    this.userId = userId;
    this.expiraEn = expiraEn;
  }

  /**
   * Un código sirve si no se ha usado y no ha vencido.
   *
   * @return true si todavía se puede usar
   */
  public boolean esValido() {
    return !usado && expiraEn != null && expiraEn.isAfter(LocalDateTime.now());
  }

  /**
   * Devuelve el identificador.
   *
   * @return identificador
   */
  public Long getId() {
    return id;
  }

  /**
   * Cambia el identificador.
   *
   * @param id identificador
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Devuelve el valor del código.
   *
   * @return valor del código
   */
  public String getToken() {
    return token;
  }

  /**
   * Cambia el valor del código.
   *
   * @param token valor del código
   */
  public void setToken(String token) {
    this.token = token;
  }

  /**
   * Devuelve el tipo de código.
   *
   * @return tipo de código
   */
  public TokenType getTipo() {
    return tipo;
  }

  /**
   * Cambia el tipo de código.
   *
   * @param tipo tipo de código
   */
  public void setTipo(TokenType tipo) {
    this.tipo = tipo;
  }

  /**
   * Devuelve el id del usuario dueño.
   *
   * @return id del usuario dueño
   */
  public Long getUserId() {
    return userId;
  }

  /**
   * Cambia el id del usuario dueño.
   *
   * @param userId id del usuario dueño
   */
  public void setUserId(Long userId) {
    this.userId = userId;
  }

  /**
   * Devuelve la fecha de vencimiento.
   *
   * @return fecha de vencimiento
   */
  public LocalDateTime getExpiraEn() {
    return expiraEn;
  }

  /**
   * Cambia la fecha de vencimiento.
   *
   * @param expiraEn fecha de vencimiento
   */
  public void setExpiraEn(LocalDateTime expiraEn) {
    this.expiraEn = expiraEn;
  }

  /**
   * Devuelve true si ya se uso.
   *
   * @return true si ya se uso
   */
  public boolean isUsado() {
    return usado;
  }

  /**
   * Cambia la marca de uso.
   *
   * @param usado marca de uso
   */
  public void setUsado(boolean usado) {
    this.usado = usado;
  }

  /**
   * Devuelve la fecha de creación.
   *
   * @return fecha de creación
   */
  public LocalDateTime getCreadoEn() {
    return creadoEn;
  }

  /**
   * Cambia la fecha de creación.
   *
   * @param creadoEn fecha de creación
   */
  public void setCreadoEn(LocalDateTime creadoEn) {
    this.creadoEn = creadoEn;
  }

  /**
   * Hash calculado con los mismos campos que compara equals.
   *
   * @return código hash
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, tipo, expiraEn);
  }

  /**
   * Dos registros son iguales si coinciden id, tipo, expiraEn.
   *
   * @param obj objeto a comparar
   * @return true si son iguales
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    EmailToken other = (EmailToken) obj;
    return Objects.equals(id, other.id)
        && Objects.equals(tipo, other.tipo)
        && Objects.equals(expiraEn, other.expiraEn);
  }

  /**
   * Pasa el objeto a texto para depurar. Deja por fuera contraseñas y códigos de correo, y de
   * los contenidos largos solo muestra el tamaño.
   *
   * @return texto con los campos del objeto
   */
  @Override
  public String toString() {
    return "EmailToken [id="
        + id
        + ", tipo="
        + tipo
        + ", expiraEn="
        + expiraEn
        + ", usado="
        + usado
        + ", creadoEn="
        + creadoEn
        + "]";
  }
}
