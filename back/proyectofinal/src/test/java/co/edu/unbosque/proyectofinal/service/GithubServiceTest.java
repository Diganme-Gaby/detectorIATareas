package co.edu.unbosque.proyectofinal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.unbosque.proyectofinal.dto.GithubRepositoryDTO;
import co.edu.unbosque.proyectofinal.exception.AnalysisRequestException;
import org.junit.jupiter.api.Test;

/** Pruebas de la interpretación de URLs de GitHub, incluida la protección contra SSRF. */
class GithubServiceTest {

  /** Servicio sin red: solo se prueban métodos que no descargan. */
  private final GithubService service = new GithubService(15, 400, "java,ts");

  /** Constructor vacío de la clase de pruebas. */
  GithubServiceTest() {}

  /**
   * URL básica.
   *
   * @throws AnalysisRequestException si la URL no es válida
   */
  @Test
  void interpretaUrlSimple() throws AnalysisRequestException {
    GithubRepositoryDTO r = service.interpretarUrl("https://github.com/usuario-demo/proyecto-final");
    assertEquals("usuario-demo", r.getDueno());
    assertEquals("proyecto-final", r.getNombre());
    assertNull(r.getRama());
  }

  /**
   * URL con .git, rama y carpeta.
   *
   * @throws AnalysisRequestException si la URL no es válida
   */
  @Test
  void interpretaRamaYCarpeta() throws AnalysisRequestException {
    GithubRepositoryDTO r =
        service.interpretarUrl("https://github.com/usuario/repo.git/tree/back/src/main/java/");
    assertEquals("repo", r.getNombre());
    assertEquals("back", r.getRama());
    assertEquals("src/main/java", r.getCarpeta());
  }

  /** Cualquier dominio distinto de github.com se rechaza. */
  @Test
  void rechazaUrlsAjenas() {
    String[] ajenas = {
      "https://github.com.evil.com/usuario/repo",
      "http://169.254.169.254/latest/meta-data",
      "https://gitlab.com/usuario/repo",
      "file:///etc/passwd",
      "https://github.com/usuario",
      "javascript:alert(1)"
    };
    for (String url : ajenas) {
      assertThrows(AnalysisRequestException.class, () -> service.interpretarUrl(url), url);
    }
  }

  /** Sin URL también se rechaza. */
  @Test
  void rechazaNulo() {
    assertThrows(AnalysisRequestException.class, () -> service.interpretarUrl(null));
  }
}
