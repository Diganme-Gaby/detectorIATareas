package co.edu.unbosque.proyectofinal.service;

import co.edu.unbosque.proyectofinal.dto.AiVerdictDTO;
import co.edu.unbosque.proyectofinal.dto.FileContentDTO;
import co.edu.unbosque.proyectofinal.exception.AiProviderException;

/**
 * Contrato común de las IAs (las tres principales y sus respaldos). Cada implementación arma la
 * petición REST de su proveedor y devuelve el veredicto con el mismo formato.
 */
public interface AiDetector {

  /**
   * Devuelve el nombre visible del proveedor (Gemini, Groq, OpenRouter, NVIDIA, Cohere o GitHub Models).
   *
   * @return nombre visible del proveedor (Gemini, Groq, OpenRouter, NVIDIA, Cohere o GitHub Models)
   */
  String getProveedor();

  /**
   * Modelo que se usara para un contenido. Algunos proveedores usan un modelo distinto cuando hay
   * imágenes.
   *
   * @param contenido archivo preparado
   * @return identificador del modelo
   */
  String modeloPara(FileContentDTO contenido);

  /**
   * Envía el contenido a la IA y devuelve su veredicto.
   *
   * @param contenido archivo preparado
   * @return veredicto interpretado
   * @throws AiProviderException si el proveedor falla o responde algo que no se puede interpretar
   * @throws InterruptedException si se interrumpe el hilo
   */
  AiVerdictDTO analizar(FileContentDTO contenido) throws AiProviderException, InterruptedException;
}
