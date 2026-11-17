package co.edu.unbosque.proyectofinal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Cliente REST de Cohere, IA de respaldo de Groq. Usa el endpoint compatible con OpenAI de Cohere
 * con una clave de prueba gratuita y solo se consulta cuando la IA principal no responde.
 */
@Service
public class CohereDetector extends OpenAiCompatibleDetector {

  /** Nombre visible del proveedor. */
  public static final String PROVEEDOR = "Cohere";

  /**
   * Spring inyecta aquí las dependencias.
   *
   * @param apiKey API key de prueba de Cohere
   * @param url URL base del endpoint compatible con OpenAI
   * @param modeloTexto modelo para texto
   * @param modeloVision modelo para imágenes (vacío para enviar solo texto)
   * @param intervaloMs milisegundos minimos entre peticiones
   * @param timeout tiempo máximo de respuesta en segundos
   */
  public CohereDetector(
      @Value("${ai.cohere.api-key:}") String apiKey,
      @Value("${ai.cohere.url:https://api.cohere.ai/compatibility/v1}") String url,
      @Value("${ai.cohere.model-text:command-a-03-2025}") String modeloTexto,
      @Value("${ai.cohere.model-vision:}") String modeloVision,
      @Value("${ai.cohere.min-interval-ms:3500}") long intervaloMs,
      @Value("${ai.timeout-seconds:90}") int timeout) {
    super(PROVEEDOR, url, apiKey, modeloTexto, modeloVision, intervaloMs, true, false, timeout);
  }

  /**
   * Devuelve la clave de prueba permite pocas imágenes por petición.
   *
   * @return la clave de prueba permite pocas imágenes por petición
   */
  @Override
  protected int maxImagenes() {
    return 1;
  }
}
