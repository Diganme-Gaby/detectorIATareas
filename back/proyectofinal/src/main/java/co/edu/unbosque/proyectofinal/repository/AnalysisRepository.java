package co.edu.unbosque.proyectofinal.repository;

import co.edu.unbosque.proyectofinal.model.Analysis;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de análisis. No se usa ningún método de borrado: el historial es permanente. */
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

  /**
   * Historial de un usuario.
   *
   * @param username dueño
   * @return análisis del más reciente al más antiguo
   */
  public List<Analysis> findByUsernameOrderByFechaCreacionDesc(String username);

  /**
   * Busca un análisis solo si pertenece al usuario.
   *
   * @param id identificador
   * @param username dueño
   * @return el análisis si existe y es del usuario
   */
  public Optional<Analysis> findByIdAndUsername(Long id, String username);

  /**
   * Todos los análisis, para el administrador.
   *
   * @return análisis del más reciente al más antiguo
   */
  public List<Analysis> findAllByOrderByFechaCreacionDesc();
}
