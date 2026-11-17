package co.edu.unbosque.proyectofinal.repository;

import co.edu.unbosque.proyectofinal.model.EmailToken;
import co.edu.unbosque.proyectofinal.model.EmailToken.TokenType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de los códigos que se envían por correo. */
public interface EmailTokenRepository extends JpaRepository<EmailToken, Long> {

  /**
   * Busca un código por su valor.
   *
   * @param token valor recibido en el enlace
   * @return el código si existe
   */
  public Optional<EmailToken> findByToken(String token);

  /**
   * Códigos sin usar de un usuario para un tipo dado. Se usan para invalidar los anteriores cuando
   * se genera uno nuevo.
   *
   * @param userId id del usuario dueño
   * @param tipo tipo de código
   * @return códigos pendientes
   */
  public List<EmailToken> findByUserIdAndTipoAndUsadoFalse(Long userId, TokenType tipo);
}
