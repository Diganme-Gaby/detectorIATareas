package co.edu.unbosque.proyectofinal;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Punto de entrada del detector de contenido generado con IA.
 *
 * @author Universidad El Bosque
 * @version 1.0
 */
@SpringBootApplication
public class ProyectofinalApplication {

  /** Constructor vacío. */
  public ProyectofinalApplication() {}

  /**
   * Método principal que inicia la aplicación Spring Boot.
   *
   * @param args Argumentos de línea de comandos pasados a la aplicación
   */
  public static void main(String[] args) {
    SpringApplication.run(ProyectofinalApplication.class, args);
  }

  /**
   * Crea y configura un bean ModelMapper para el mapeo de objetos. Este bean se utiliza en toda la
   * aplicación para mapear entre DTOs y objetos de entidad.
   *
   * @return Instancia configurada de ModelMapper
   */
  @Bean
  public ModelMapper getModelMapper() {
    return new ModelMapper();
  }
}
