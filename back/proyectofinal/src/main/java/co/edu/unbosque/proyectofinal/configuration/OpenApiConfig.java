package co.edu.unbosque.proyectofinal.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI (Swagger) para documentar la API REST. Esta clase define la información
 * general de la API, esquemas de seguridad, y componentes reutilizables para la documentación.
 */
@Configuration
public class OpenApiConfig {

  /** Constructor vacío. */
  public OpenApiConfig() {}

  /**
   * Define la información general de la API, el esquema de seguridad JWT y respuestas de error
   * reutilizables.
   *
   * @return Configuración personalizada de OpenAPI
   */
  @Bean
  public OpenAPI customOpenAPI() {
    String mainDescription =
        "<h2>API del Detector de contenido generado con IA</h2>"
            + "<p>Recibe archivos (texto, código, PDF, Word, imágenes, audio, video o .zip) o la URL"
            + " de un repositorio de GitHub, los analiza con <strong>tres inteligencias"
            + " artificiales</strong> (Gemini, Groq y OpenRouter) y entrega, por cada archivo, el"
            + " porcentaje de probabilidad de que haya sido generado con IA y los motivos que dio"
            + " cada modelo. Si alguna no responde, la reemplaza un modelo de respaldo (NVIDIA,"
            + " Cohere o GitHub Models).</p>"
            + "<h3>Flujo básico</h3><ol>"
            + "<li>Crea una cuenta con <code>/auth/register</code>. Llega un correo para"
            + " confirmarla.</li>"
            + "<li>Confirma el correo (<code>/auth/verifyemail</code>) e inicia sesión con"
            + " <code>/auth/login</code> para obtener el token JWT.</li>"
            + "<li>Pulsa <strong>Authorize</strong> y pega el token.</li>"
            + "<li>Envía archivos con <code>/analysis/uploadfiles</code> o un repositorio con"
            + " <code>/analysis/github</code>. La respuesta trae el id del análisis.</li>"
            + "<li>Consulta <code>/analysis/getbyid/{id}</code> hasta que el estado sea"
            + " COMPLETADO. El reporte PDF llega también al correo.</li></ol>"
            + "<h3>Roles</h3><ul>"
            + "<li><strong>USER</strong>: analiza archivos, consulta su historial y descarga sus"
            + " reportes.</li>"
            + "<li><strong>ADMIN</strong>: además administra usuarios, consulta la auditoría y"
            + " descarga reportes y estadísticas en PDF.</li></ul>"
            + "<h3>Codigos de estado HTTP comunes</h3><ul>"
            + "<li><strong>200/201/202</strong>: Operación exitosa (202 = análisis aceptado y en"
            + " proceso)</li>"
            + "<li><strong>400</strong>: Datos incorrectos</li>"
            + "<li><strong>401</strong>: Credenciales invalidas</li>"
            + "<li><strong>403</strong>: Sin token, token vencido o sin permisos</li>"
            + "<li><strong>404</strong>: Recurso no encontrado</li>"
            + "<li><strong>409</strong>: Conflicto (usuario o correo ya registrados)</li>"
            + "<li><strong>423</strong>: Cuenta bloqueada temporalmente por intentos"
            + " fallidos</li></ul>";

    String securityDescription =
        "Autenticación mediante JWT (JSON Web Token)."
            + "<p>Para autenticarte, sigue estos pasos:</p>"
            + "<ol>"
            + "    <li>Obtén un token JWT usando el endpoint <code>/auth/login</code></li>"
            + "    <li>Copia el token recibido en la respuesta</li>"
            + "    <li>Haz clic en el botón \"Authorize\" en la parte superior de esta página</li>"
            + "    <li>Pega el token en el campo \"Value\" (sin escribir Bearer)</li>"
            + "    <li>Haz clic en \"Authorize\" y luego en \"Close\"</li>"
            + "</ol>"
            + "<p>Ahora podrás acceder a los endpoints protegidos.</p>";

    io.swagger.v3.oas.models.info.Info info =
        new io.swagger.v3.oas.models.info.Info()
            .title("API Detector de IA - Universidad El Bosque")
            .version("1.0")
            .description(mainDescription)
            .contact(
                new io.swagger.v3.oas.models.info.Contact()
                    .name("Equipo de Desarrollo - Desarrollo de Sistemas de Información 3"))
            .license(
                new io.swagger.v3.oas.models.info.License()
                    .name("Licencia MIT")
                    .url("https://opensource.org/licenses/MIT"));

    io.swagger.v3.oas.models.security.SecurityScheme securityScheme =
        new io.swagger.v3.oas.models.security.SecurityScheme()
            .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description(securityDescription);

    return new OpenAPI()
        .info(info)
        .components(
            new Components()
                .addSecuritySchemes("bearerAuth", securityScheme)
                .addResponses(
                    "UnauthorizedError",
                    errorResponse(
                        "No autenticado - Token JWT inválido o expirado",
                        "{\"error\": \"No autorizado\", \"mensaje\": \"Token inválido o expirado\"}"))
                .addResponses(
                    "ForbiddenError",
                    errorResponse(
                        "Acceso prohibido - No tienes permisos suficientes",
                        "{\"error\": \"Acceso prohibido\", \"mensaje\": \"No tienes permisos para"
                            + " esta operación\"}"))
                .addResponses(
                    "NotFoundError",
                    errorResponse(
                        "Recurso no encontrado",
                        "{\"error\": \"No encontrado\", \"mensaje\": \"El recurso solicitado no"
                            + " existe\"}")));
  }

  /**
   * Arma una respuesta de error de ejemplo.
   *
   * @param descripcion descripción de la respuesta
   * @param ejemplo cuerpo JSON de ejemplo
   * @return respuesta para los componentes de OpenAPI
   */
  private ApiResponse errorResponse(String descripcion, String ejemplo) {
    return new ApiResponse()
        .description(descripcion)
        .content(
            new Content()
                .addMediaType(
                    "application/json",
                    new MediaType().addExamples("error", new Example().value(ejemplo))));
  }
}
