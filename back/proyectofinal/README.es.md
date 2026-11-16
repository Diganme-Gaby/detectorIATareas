# Detector de contenido generado con IA - Backend

[English](README.md) | **Español**

Este es el backend de nuestro proyecto final de *Desarrollo de Sistemas de Información 3* en la Universidad El Bosque. Es una API REST hecha con Spring Boot que estima qué tan probable es que un archivo haya sido generado con inteligencia artificial.

Subes uno o varios archivos (o el enlace de un repositorio público de GitHub) y la API manda cada archivo a tres modelos de IA distintos. Cada modelo devuelve un porcentaje y sus razones; la API promedia los resultados, guarda todo en tu historial y te envía un reporte en PDF al correo.

El frontend es una aplicación de Angular aparte que consume esta API.

## Qué hace

- Acepta casi cualquier tipo de archivo: texto plano, código fuente, PDF, Word, Excel, PowerPoint, imágenes, audio, video y archivos `.zip`. Cada archivo de un zip se analiza por separado y tiene su propio reporte.
- Analiza repositorios públicos de GitHub, con filtros opcionales de rama, carpeta, extensiones y cantidad de archivos.
- Le pide su opinión a tres modelos de IA sobre cada archivo y promedia sus respuestas.
- Genera reportes PDF que se pueden ver en el navegador, descargar, y que además llegan al correo como adjuntos.
- Guarda un historial permanente por usuario, con filtros por rango de fechas, tipo de archivo, estado, nivel de IA y texto.
- Maneja cuentas con verificación de correo, recuperación de contraseña, bloqueo de 15 minutos después de 5 intentos fallidos y autenticación con JWT.
- Registra cada acción importante en una tabla de auditoría que no se puede editar ni borrar. Cada registro guarda la IP, su latitud y longitud aproximadas y un enlace a Google Maps. La auditoría se puede exportar a PDF.
- Incluye un módulo de administración para listar, editar, activar, desactivar y desbloquear usuarios (el usuario y el correo no se pueden cambiar), además de estadísticas de uso exportables a PDF con las gráficas que envía el frontend.

## Cómo funciona el análisis

1. La solicitud se guarda como `PENDIENTE` y la API responde de una vez con `202 Accepted` y el id del análisis.
2. En segundo plano se prepara cada archivo: el texto se extrae con Apache Tika, el audio se transcribe con Whisper y de los videos se toman algunos fotogramas.
3. El contenido preparado se envía en paralelo a los tres proveedores de IA.
4. Cuando todo termina, el análisis queda en `COMPLETADO`, `COMPLETADO_PARCIAL` o `FALLIDO`, y el reporte se envía al correo del usuario.

El cliente puede consultar `GET /analysis/getbyid/{id}` para seguir el progreso.

Si el mismo archivo ya fue analizado por el mismo modelo, se reutiliza el resultado anterior en lugar de volver a llamar la API.

### Modelos y respaldos

Las APIs gratuitas de IA fallan más de lo que uno quisiera (límites de uso, modelos retirados, tiempos de espera), así que cada proveedor principal tiene un respaldo. Si el principal no responde se prueba su respaldo, y después los demás respaldos. Un archivo solo se queda sin resultado en una posición cuando todos fallan, y en ese caso el reporte muestra un aviso corto que no habla de tokens ni de cuotas.

| Posición | Proveedor principal | Respaldo |
|---|---|---|
| 1 | Gemini (`gemini-flash-latest`) | NVIDIA (`meta/llama-3.3-70b-instruct`, `meta/llama-3.2-90b-vision-instruct` para imágenes) |
| 2 | Groq (`openai/gpt-oss-120b`, `qwen/qwen3.8-27b` para imágenes, Whisper para audio) | Cohere (`command-a-03-2025`) |
| 3 | OpenRouter (`openrouter/free`) | GitHub Models (`openai/gpt-4.1-mini`) |

Los reportes siempre muestran tres columnas. Cuando respondió un respaldo, el reporte dice cuál fue.

Todos los modelos se configuran en `application.properties`, así que si retiran uno se puede cambiar sin tocar el código.

## Tecnologías

- Java 21 y Spring Boot 4.1.2 (empaquetado como `war`)
- Spring Security 7 con JWT (jjwt 0.11.5)
- Spring Data JPA con MySQL
- springdoc-openapi 3.1.0 (Swagger UI)
- Apache Tika 3.3.1 para extraer texto y JCodec para los fotogramas de video
- OpenPDF 2.0.3 para los reportes
- Gson y ModelMapper
- JUnit 5, Mockito y Spring Security Test

## Cómo empezar

### Requisitos

- JDK 21
- Maven 3.9 o superior (o el `mvnw` que viene incluido)
- Una base de datos MySQL
- Una cuenta de Gmail con contraseña de aplicación, para enviar los correos
- API keys de los proveedores de IA. Todos tienen opción gratuita: [Google AI Studio](https://aistudio.google.com/app/apikey), [Groq](https://console.groq.com/keys), [OpenRouter](https://openrouter.ai/keys), [NVIDIA](https://build.nvidia.com/settings/api-keys), [Cohere](https://dashboard.cohere.com/api-keys) y un [token de GitHub de tipo fine-grained](https://github.com/settings/personal-access-tokens/new) con el permiso *Models: Read-only*.

### Configuración

Todo se configura en `src/main/resources/application.properties`. Ese archivo tiene claves reales, así que no está en el repositorio. Copia `application.properties.example` (en la misma carpeta) como `application.properties` y llena estos valores:

```properties
spring.datasource.url=jdbc:mysql://HOST:3306/BASE_DE_DATOS
spring.datasource.username=USUARIO_BD
spring.datasource.password=CLAVE_BD
spring.jpa.hibernate.ddl-auto=update

jwt.secret=UN_SECRETO_LARGO_Y_ALEATORIO
app.aes.key=LLAVE16CARACTER_
app.aes.iv=VECTOR16CARACTER

spring.mail.username=tu.cuenta@gmail.com
spring.mail.password=CLAVE_DE_APLICACION_GMAIL
app.frontend-url=http://localhost:4200

app.admin.username=USUARIO_ADMIN
app.admin.email=CORREO_ADMIN
app.admin.password=CLAVE_ADMIN

ai.gemini.api-key=...
ai.groq.api-key=...
ai.openrouter.api-key=...
ai.nvidia.api-key=...
ai.cohere.api-key=...
ai.github-models.api-key=...
```

El resto del archivo (modelos, límites, tiempos de espera, opciones de Swagger) ya trae valores que funcionan.

Hibernate crea las tablas la primera vez que se ejecuta. Al arrancar, la aplicación también crea la cuenta de administrador definida arriba y un usuario normal de pruebas llamado `normaluser`.

`application.properties` está en el `.gitignore`. Nunca lo subas: las claves reales solo deben estar en tu equipo y en el servidor.

### Ejecutarlo en local

```bash
./mvnw spring-boot:run
```

O impórtalo en Eclipse o IntelliJ como proyecto Maven y ejecuta `ProyectofinalApplication`.

Swagger UI abre en la raíz: [http://localhost:8080/](http://localhost:8080/).

1. Inicia sesión con `POST /auth/login`.
2. Haz clic en **Authorize** y pega el token. Los tokens duran 24 horas.
3. Prueba el resto de endpoints.

### Desplegar en Tomcat

```bash
./mvnw clean package -DskipTests
```

Esto genera `target/proyectofinal-1.0.war`, que se puede desplegar en cualquier servidor Tomcat 11. `ServletInitializer` se encarga de arrancar Spring dentro de Tomcat.

## Resumen de la API

Todos los endpoints, menos los de `/auth`, necesitan el encabezado `Authorization: Bearer <token>`. Los de `/admin` además piden el rol `ADMIN`. La documentación completa, con ejemplos de peticiones y respuestas, está en Swagger.

| Área | Endpoints |
|---|---|
| Autenticación | `POST /auth/login`, `POST /auth/register`, `GET /auth/verifyemail`, `POST /auth/resendverification`, `POST /auth/forgotpassword`, `PUT /auth/resetpassword` |
| Perfil | `GET /user/me`, `PUT /user/updateprofile`, `PUT /user/changepassword` |
| Análisis | `POST /analysis/uploadfiles`, `POST /analysis/github`, `GET /analysis/history`, `GET /analysis/getbyid/{id}`, `GET /analysis/reportpdf/{id}`, `POST /analysis/resendemail/{id}`, `GET /analysis/limits` |
| Administración | `GET /admin/user/getall`, `GET /admin/user/count`, `GET /admin/user/getbyid/{id}`, `PUT /admin/user/updatejson/{id}`, `PUT /admin/user/enable/{id}`, `PUT /admin/user/disable/{id}`, `PUT /admin/user/unlock/{id}`, `GET /admin/user/reportpdf`, `GET /admin/analysis/getall` |
| Auditoría | `GET /admin/audit/getall`, `GET /admin/audit/reportpdf` |
| Estadísticas | `GET /admin/stats/summary`, `POST /admin/stats/reportpdf` |

`GET /analysis/reportpdf/{id}` devuelve el reporte consolidado. Agrega `?archivoId=31` para el reporte de un solo archivo, y `?descargar=true` para descargarlo en vez de abrirlo en el navegador.

Así se analiza un repositorio:

```json
POST /analysis/github
{
  "url": "https://github.com/dueno/repositorio",
  "rama": "main",
  "carpeta": "src/main/java",
  "extensiones": ["java"],
  "maxArchivos": 10,
  "titulo": "Revisión del proyecto final"
}
```

Solo `url` es obligatorio. Así también probamos el proyecto consigo mismo: le mandamos el enlace de este mismo repositorio.

## Límites por defecto

| Límite | Valor |
|---|---|
| Archivos por solicitud | 10 |
| Tamaño por archivo | 20 MB |
| Archivos que se toman de un zip | 30 |
| Archivos que se toman de un repositorio | 15 (400 KB cada uno) |
| Caracteres de texto enviados a cada modelo | 12.000 |

Todos se pueden cambiar en `application.properties`.

## Pruebas

```bash
./mvnw test
```

Las pruebas incluyen pruebas unitarias de las utilidades y de la lógica de JWT; pruebas de integración del registro, el inicio de sesión, el bloqueo de cuentas y la recuperación de contraseña; un flujo completo de análisis con las IAs simuladas (incluido un proveedor que falla y un respaldo que toma su lugar); pruebas de seguridad (peticiones sin token o con uno inválido, usuarios que intentan entrar a endpoints de administración o a análisis ajenos, cuentas desactivadas, inyección SQL en el login, contraseñas que nunca salen en las respuestas y URLs de repositorios fuera de GitHub), y una prueba de estrés con peticiones e inicios de sesión concurrentes.

Las pruebas de integración usan la base de datos configurada en `application.properties`, así que los usuarios y registros de auditoría que crean se quedan en esa base.

Las mismas pruebas corren solas en GitHub Actions en cada pull request. Allí usan una base MySQL temporal y valores de prueba en lugar de las claves reales, así que no escriben nada en la base compartida.

## Estructura del proyecto

```
src/main/java/co/edu/unbosque/proyectofinal
├── configuration   Configuración de OpenAPI y datos iniciales
├── controller      Controllers REST
├── dto             Objetos que envía y recibe la API
├── exception       Excepciones propias
├── model           Entidades JPA
├── repository      Repositorios de Spring Data
├── security        Filtro JWT, utilidades y configuración de Spring Security
├── service         Lógica de negocio, clientes de las IAs y procesamiento en segundo plano
└── util            Generación de PDF, correo, validaciones y utilidades
```

## Notas

- Las contraseñas se guardan con hash BCrypt. Los teléfonos y la IP de la auditoría se cifran con AES-GCM (`AESUtil`) usando la llave y el IV del `application.properties`, y solo se descifran al enviarlos al frontend. Los teléfonos guardados antes del cifrado se siguen leyendo bien. La IP cifrada va en una columna nueva, `ip_cifrada`, que Hibernate agrega al arrancar; por eso los registros de auditoría anteriores a ese cambio no muestran IP.
- Todas las llamadas a APIs externas pasan por `ExternalHTTPRequestHandler`, que convierte cada respuesta JSON en su propio DTO con Gson. Los datos de esas APIs que no se guardan solo tienen DTO, sin entidad ni repositorio.
- El Javadoc está en la carpeta `docs` (abre `docs/index.html`).
- Los porcentajes son estimaciones hechas por modelos de IA. No son prueba de autoría y deben leerse junto con otros criterios.
- La ubicación de la auditoría sale de la IP pública, así que es aproximada y normalmente solo acierta la ciudad. Las peticiones hechas desde `localhost` se ubican con la IP pública del servidor.
- Los reportes PDF usan la fuente Carlito, incluida bajo la licencia SIL Open Font License (`src/main/resources/fonts/OFL.txt`).
