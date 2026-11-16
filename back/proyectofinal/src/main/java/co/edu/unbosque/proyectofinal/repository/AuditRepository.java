package co.edu.unbosque.proyectofinal.repository;

import co.edu.unbosque.proyectofinal.model.Audit;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de la auditoría. Solo se usan operaciones de inserción y consulta; el servicio no
 * llama ningún método de borrado.
 */
public interface AuditRepository extends JpaRepository<Audit, Long> {

  /**
   * Todos los registros, del más reciente al más antiguo.
   *
   * @return registros de auditoría
   */
  public List<Audit> findAllByOrderByFechaDesc();

  /**
   * Registros posteriores a una fecha, para las estadísticas.
   *
   * @param desde fecha mínima
   * @return registros encontrados
   */
  public List<Audit> findByFechaAfter(LocalDateTime desde);
}
