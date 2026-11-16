package co.edu.unbosque.proyectofinal.repository;

import co.edu.unbosque.proyectofinal.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de cuentas de usuario. Agrega las búsquedas por nombre de usuario y correo que usan
 * el login, el registro y la recuperación de contraseña.
 */
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Busca un usuario por su nombre de usuario.
   *
   * @param username nombre de usuario
   * @return el usuario si existe
   */
  public Optional<User> findByUsername(String username);

  /**
   * Busca un usuario por su correo.
   *
   * @param email correo en minúsculas
   * @return el usuario si existe
   */
  public Optional<User> findByEmail(String email);

  /**
   * Indica si el nombre de usuario ya está tomado.
   *
   * @param username nombre de usuario
   * @return true si existe
   */
  public boolean existsByUsername(String username);

  /**
   * Indica si el correo ya está registrado.
   *
   * @param email correo en minúsculas
   * @return true si existe
   */
  public boolean existsByEmail(String email);

  /**
   * Lista los usuarios por fecha de registro, del más reciente al más antiguo.
   *
   * @return lista de usuarios
   */
  public List<User> findAllByOrderByFechaRegistroDesc();

  /**
   * Cuenta los usuarios según su estado de activación.
   *
   * @param activo estado buscado
   * @return cantidad de usuarios
   */
  public long countByActivo(boolean activo);

  /**
   * Cuenta los usuarios según la verificación de correo.
   *
   * @param correoVerificado estado buscado
   * @return cantidad de usuarios
   */
  public long countByCorreoVerificado(boolean correoVerificado);
}
