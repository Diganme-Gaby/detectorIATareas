package co.edu.unbosque.proyectofinal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Cliente REST de GitHub Models, IA de respaldo de OpenRouter. Se autentica con un token personal
 * de GitHub con permiso de lectura de modelos y solo se consulta cuando la IA principal no responde.
 */
@Service
public class GithubModelsDetector extends OpenAiCompatibleDetector {

  /** Nombre visible del proveedor. */
  public static final String PROVEEDOR = "GitHub Models";

  /**
   * Spring inyecta aquí las dependencias.
   *
   * @param apiKey token personal de GitHub
   * @param url URL base
   * @param modeloTexto modelo para texto
   * @param modeloVision modelo para imágenes
   * @param intervaloMs milisegundos minimos entre peticiones
   * @param timeout tiempo máximo de respuesta en segundos
   */
  public GithubModelsDetector(
      @Value("${ai.github-models.api-key:}") String apiKey,
      @Value("${ai.github-models.url:https://models.github.ai/inference}") String url,
      @Value("${ai.github-models.model-text:openai/gpt-4.1-mini}") String modeloTexto,
      @Value("${ai.github-models.model-vision:openai/gpt-4.1-mini}") String modeloVision,
      @Value("${ai.github-models.min-interval-ms:4500}") long intervaloMs,
      @Value("${ai.timeout-seconds:90}") int timeout) {
    super(PROVEEDOR, url, apiKey, modeloTexto, modeloVision, intervaloMs, true, true, timeout);
  }

  /**
   * Devuelve el plan gratuito limita el tamaño de la petición, por eso se envían pocas imágenes.
   *
   * @return el plan gratuito limita el tamaño de la petición, por eso se envían pocas imágenes
   */
  @Override
  protected int maxImagenes() {
    return 2;
  }
}
