package co.edu.unbosque.proyectofinal.repository;

import co.edu.unbosque.proyectofinal.model.AnalyzedFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de los archivos analizados. */
public interface AnalyzedFileRepository extends JpaRepository<AnalyzedFile, Long> {

  /**
   * Archivos de un análisis en el orden en que se procesaron.
   *
   * @param analysisId id del análisis
   * @return archivos del análisis
   */
  public List<AnalyzedFile> findByAnalysisIdOrderByIdAsc(Long analysisId);
}
