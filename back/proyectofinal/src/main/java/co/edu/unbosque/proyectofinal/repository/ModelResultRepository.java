package co.edu.unbosque.proyectofinal.repository;

import co.edu.unbosque.proyectofinal.model.ModelResult;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de las respuestas de cada IA. */
public interface ModelResultRepository extends JpaRepository<ModelResult, Long> {

  /**
   * Resultados de un archivo en el orden en que se guardaron.
   *
   * @param archivoId id del archivo analizado
   * @return resultados de las tres IAs
   */
  public List<ModelResult> findByArchivoIdOrderByIdAsc(Long archivoId);

  /**
   * Último resultado válido de un modelo para un contenido idéntico. Permite no gastar llamadas a
   * la IA cuando se vuelve a subir el mismo archivo.
   *
   * @param hashSha256 huella del contenido
   * @param proveedor proveedor de la IA
   * @param modelo modelo usado
   * @return el resultado más reciente si existe
   */
  public Optional<ModelResult> findFirstByHashSha256AndProveedorAndModeloAndExitosoTrueOrderByFechaDesc(
      String hashSha256, String proveedor, String modelo);

  /**
   * Resultados de un proveedor, para las estadísticas.
   *
   * @param proveedor proveedor de la IA
   * @return resultados encontrados
   */
  public List<ModelResult> findByProveedor(String proveedor);
}
