package co.edu.unbosque.proyectofinal.service;

import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Cliente REST de OpenRouter, la plataforma que reúne muchos modelos (varios gratuitos) detrás de un
 * mismo endpoint compatible con OpenAI. Con el modelo {@code openrouter/free} la plataforma escoge
 * un modelo gratuito disponible que soporte lo que se envía (texto o imágenes).
 */
@Service
public class OpenRouterDetector extends OpenAiCompatibleDetector {

  /** Nombre visible del proveedor. */
  public static final String PROVEEDOR = "OpenRouter";

  /**
   * Spring inyecta aquí las dependencias.
   *
   * @param apiKey API key de OpenRouter
   * @param url URL base
   * @param modeloTexto modelo para texto
   * @param modeloVision modelo para imágenes
   * @param intervaloMs milisegundos minimos entre peticiones
   * @param imagenComoObjeto formato del campo image_url
   * @param modoJson true para pedir respuesta en modo JSON
   * @param timeout tiempo máximo de respuesta en segundos
   */
  public OpenRouterDetector(
      @Value("${ai.openrouter.api-key}") String apiKey,
      @Value("${ai.openrouter.url}") String url,
      @Value("${ai.openrouter.model-text}") String modeloTexto,
      @Value("${ai.openrouter.model-vision}") String modeloVision,
      @Value("${ai.openrouter.min-interval-ms:3500}") long intervaloMs,
      @Value("${ai.openrouter.image-url-as-object:true}") boolean imagenComoObjeto,
      @Value("${ai.openrouter.json-mode:false}") boolean modoJson,
      @Value("${ai.timeout-seconds:90}") int timeout) {
    super(PROVEEDOR, url, apiKey, modeloTexto, modeloVision, intervaloMs, imagenComoObjeto, modoJson, timeout);
  }

  /**
   * {@code openrouter/free} a veces escoge un modelo que razona antes de responder. Se le pide un
   * razonamiento corto y que no lo incluya en la respuesta, para que no se gaste todo el cupo de
   * tokens pensando y deje el JSON vacío. Los modelos que no razonan ignoran este parámetro.
   *
   * @param cuerpo cuerpo de la petición
   * @param modelo modelo que se va a usar
   */
  @Override
  protected void opcionesExtra(JsonObject cuerpo, String modelo) {
    JsonObject razonamiento = new JsonObject();
    razonamiento.addProperty("effort", "low");
    razonamiento.addProperty("exclude", true);
    cuerpo.add("reasoning", razonamiento);
  }
}
