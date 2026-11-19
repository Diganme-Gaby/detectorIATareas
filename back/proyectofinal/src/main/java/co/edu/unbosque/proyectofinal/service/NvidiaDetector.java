package co.edu.unbosque.proyectofinal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Cliente REST de NVIDIA (build.nvidia.com), IA de respaldo de Gemini. Tiene modelos de texto y de
 * visión con el formato de OpenAI y solo se consulta cuando la IA principal no responde.
 */
@Service
public class NvidiaDetector extends OpenAiCompatibleDetector {

  /** Nombre visible del proveedor. */
  public static final String PROVEEDOR = "NVIDIA";

  /**
   * Spring inyecta aquí las dependencias.
   *
   * @param apiKey API key de NVIDIA
   * @param url URL base
   * @param modeloTexto modelo para texto
   * @param modeloVision modelo para imágenes
   * @param intervaloMs milisegundos minimos entre peticiones
   * @param timeout tiempo máximo de respuesta en segundos
   */
  public NvidiaDetector(
      @Value("${ai.nvidia.api-key:}") String apiKey,
      @Value("${ai.nvidia.url:https://integrate.api.nvidia.com/v1}") String url,
      @Value("${ai.nvidia.model-text:meta/llama-3.3-70b-instruct}") String modeloTexto,
      @Value("${ai.nvidia.model-vision:meta/llama-3.2-90b-vision-instruct}") String modeloVision,
      @Value("${ai.nvidia.min-interval-ms:2000}") long intervaloMs,
      @Value("${ai.timeout-seconds:90}") int timeout) {
    super(PROVEEDOR, url, apiKey, modeloTexto, modeloVision, intervaloMs, true, false, timeout);
  }

  /**
   * Devuelve el modelo de visión de Llama 3.2 recibe una sola imagen por petición.
   *
   * @return el modelo de visión de Llama 3.2 recibe una sola imagen por petición
   */
  @Override
  protected int maxImagenes() {
    return 1;
  }
}
