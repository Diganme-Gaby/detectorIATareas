# AI Content Detector - Backend

**English** | [Español](README.es.md)

This is the backend of our final project for *Desarrollo de Sistemas de Información 3* at Universidad El Bosque. It is a REST API built with Spring Boot that estimates how likely it is that a file was generated with artificial intelligence.

You upload one or more files (or point it to a public GitHub repository), and the API sends each file to three different AI models. Each model returns a percentage and the reasons behind it, and the API averages the results, stores everything in your history and emails you a PDF report.

The frontend is a separate Angular application that consumes this API.

## What it does

- Accepts almost any kind of file: plain text, source code, PDF, Word, Excel, PowerPoint, images, audio, video and `.zip` archives. Every file inside a zip is analyzed on its own and gets its own report.
- Analyzes public GitHub repositories, with optional filters for branch, folder, file extensions and number of files.
- Asks three AI models for an opinion on every file and averages their answers.
- Generates PDF reports that can be viewed in the browser, downloaded, and are also sent by email as attachments.
- Keeps a permanent history per user, with filters by date range, file type, status, AI level and text.
- Handles accounts with email verification, password recovery, a 15 minute lock after 5 failed logins and JWT authentication.
- Records every relevant action in an audit table that cannot be edited or deleted. Each entry stores the IP address, its approximate latitude and longitude, and a Google Maps link. The audit log can be exported to PDF.
- Includes an admin module to list, edit, enable, disable and unlock users (username and email cannot be changed), plus usage statistics exported to PDF with the charts sent by the frontend.

## How the analysis works

1. The request is stored as `PENDIENTE` and the API answers right away with `202 Accepted` and the analysis id.
2. In the background each file is prepared: text is extracted with Apache Tika, audio is transcribed with Whisper, and a few frames are taken from videos.
3. The prepared content is sent in parallel to the three AI providers.
4. When everything is done the analysis becomes `COMPLETADO`, `COMPLETADO_PARCIAL` or `FALLIDO`, and the report is emailed to the user.

The client can poll `GET /analysis/getbyid/{id}` to follow the progress.

If the same file was already analyzed by the same model, the previous result is reused instead of calling the API again.

### Models and fallbacks

Free AI APIs fail more often than you would like (rate limits, retired models, timeouts), so each main provider has a backup. If the main one does not answer, its backup is tried, and then the other backups. A file only ends up without a result for a position when all of them fail, and in that case the report shows a short notice that does not mention tokens or quotas.

| Position | Main provider | Backup |
|---|---|---|
| 1 | Gemini (`gemini-flash-latest`) | NVIDIA (`meta/llama-3.3-70b-instruct`, `meta/llama-3.2-90b-vision-instruct` for images) |
| 2 | Groq (`openai/gpt-oss-120b`, `qwen/qwen3.8-27b` for images, Whisper for audio) | Cohere (`command-a-03-2025`) |
| 3 | OpenRouter (`openrouter/free`) | GitHub Models (`openai/gpt-4.1-mini`) |

The reports always show three columns. When a backup answered, the report says which one it was.

All models are configured in `application.properties`, so a retired model can be replaced without touching the code.

## Tech stack

- Java 21 and Spring Boot 4.1.2 (packaged as a `war`)
- Spring Security 7 with JWT (jjwt 0.11.5)
- Spring Data JPA with MySQL
- springdoc-openapi 3.1.0 (Swagger UI)
- Apache Tika 3.3.1 for text extraction and JCodec for video frames
- OpenPDF 2.0.3 for the reports
- Gson and ModelMapper
- JUnit 5, Mockito and Spring Security Test

## Getting started

### Requirements

- JDK 21
- Maven 3.9 or newer (or the included `mvnw` wrapper)
- A MySQL database
- A Gmail account with an app password, used to send emails
- API keys for the AI providers. All of them have a free option: [Google AI Studio](https://aistudio.google.com/app/apikey), [Groq](https://console.groq.com/keys), [OpenRouter](https://openrouter.ai/keys), [NVIDIA](https://build.nvidia.com/settings/api-keys), [Cohere](https://dashboard.cohere.com/api-keys) and a [GitHub fine-grained token](https://github.com/settings/personal-access-tokens/new) with the *Models: Read-only* permission.

### Configuration

Everything is configured in `src/main/resources/application.properties`. That file holds real keys, so it is not in the repository. Copy `application.properties.example` (same folder) to `application.properties` and fill in these values:

```properties
# Database
spring.datasource.url=jdbc:mysql://HOST:3306/DATABASE
spring.datasource.username=
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update

# Security: JWT secret (long and random) and a 16 character AES key
jwt.secret=
app.aes.key=

# Email (a Gmail account with an app password)
spring.mail.username=
spring.mail.password=
app.frontend-url=http://localhost:4200

# Admin account created on startup
app.admin.username=
app.admin.email=
app.admin.password=

# AI provider keys
ai.gemini.api-key=
ai.groq.api-key=
ai.openrouter.api-key=
ai.nvidia.api-key=
ai.cohere.api-key=
ai.github-models.api-key=
```

The rest of the file (models, limits, timeouts, Swagger options) already has working values.

The tables are created by Hibernate on the first run. On startup the application also creates the admin account defined above and a regular test user called `normaluser`.

`application.properties` is listed in `.gitignore`. Never commit it: real keys only live on your machine and on the server.

### Running it locally

```bash
./mvnw spring-boot:run
```

Or import it in Eclipse or IntelliJ as a Maven project and run `ProyectofinalApplication`.

Swagger UI opens at the root: [http://localhost:8080/](http://localhost:8080/).

1. Log in with `POST /auth/login`.
2. Click **Authorize** and paste the token. Tokens last 24 hours.
3. Try the rest of the endpoints.

### Deploying to Tomcat

```bash
./mvnw clean package -DskipTests
```

This produces `target/proyectofinal-1.0.war`, which can be deployed to any Tomcat 11 server. `ServletInitializer` takes care of starting Spring inside Tomcat.

## API overview

Every endpoint except the ones under `/auth` needs the header `Authorization: Bearer <token>`. Endpoints under `/admin` also need the `ADMIN` role. The full documentation, with request and response examples, is in Swagger.

| Area | Endpoints |
|---|---|
| Auth | `POST /auth/login`, `POST /auth/register`, `GET /auth/verifyemail`, `POST /auth/resendverification`, `POST /auth/forgotpassword`, `PUT /auth/resetpassword` |
| Profile | `GET /user/me`, `PUT /user/updateprofile`, `PUT /user/changepassword` |
| Analysis | `POST /analysis/uploadfiles`, `POST /analysis/github`, `GET /analysis/history`, `GET /analysis/getbyid/{id}`, `GET /analysis/reportpdf/{id}`, `POST /analysis/resendemail/{id}`, `GET /analysis/limits` |
| Admin | `GET /admin/user/getall`, `GET /admin/user/count`, `GET /admin/user/getbyid/{id}`, `PUT /admin/user/updatejson/{id}`, `PUT /admin/user/enable/{id}`, `PUT /admin/user/disable/{id}`, `PUT /admin/user/unlock/{id}`, `GET /admin/user/reportpdf`, `GET /admin/analysis/getall` |
| Audit | `GET /admin/audit/getall`, `GET /admin/audit/reportpdf` |
| Statistics | `GET /admin/stats/summary`, `POST /admin/stats/reportpdf` |

`GET /analysis/reportpdf/{id}` returns the consolidated report. Add `?archivoId=31` to get the report of a single file, and `?descargar=true` to download it instead of opening it in the browser.

Analyzing a repository looks like this:

```json
POST /analysis/github
{
  "url": "https://github.com/owner/repository",
  "rama": "main",
  "carpeta": "src/main/java",
  "extensiones": ["java"],
  "maxArchivos": 10,
  "titulo": "Final project review"
}
```

Only `url` is required. This is also how we test the project against itself: we send the link of this same repository.

## Default limits

| Limit | Value |
|---|---|
| Files per request | 10 |
| Size per file | 20 MB |
| Files taken from zip archives | 30 |
| Files taken from a repository | 15 (400 KB each) |
| Characters of text sent to each model | 12,000 |

All of them can be changed in `application.properties`.

## Tests

```bash
./mvnw test
```

The suite covers unit tests for the utilities and the JWT logic, integration tests for registration, login, account locking and password recovery, a complete analysis flow with mocked AI providers (including a failing provider and a backup that takes its place), security tests (requests without a token or with an invalid one, users trying to reach admin endpoints or other people's analyses, disabled accounts, SQL injection in the login, passwords never exposed in responses, and repository URLs outside GitHub) and a stress test with concurrent requests and logins.

The integration tests use the database configured in `application.properties`, so the users and audit entries they create stay in that database.

The same suite runs automatically on GitHub Actions for every pull request. There it uses a temporary MySQL database and test values instead of the real keys, so nothing is written to the shared database.

## Project structure

```
src/main/java/co/edu/unbosque/proyectofinal
├── configuration   OpenAPI setup and initial data
├── controller      REST controllers
├── dto             Objects sent and received by the API
├── exception       Custom exceptions
├── model           JPA entities
├── repository      Spring Data repositories
├── security        JWT filter, utilities and Spring Security setup
├── service         Business logic, AI clients and background processing
└── util            PDF generation, email, validation and helpers
```

## Notes

- Passwords are hashed with BCrypt. Phone numbers and the IP address stored in the audit log are encrypted with AES-GCM (`AESUtil`) using the key from `application.properties` and a new random IV for every value (`CifradoUtil`), and decrypted only when they are sent to the front end. Phone numbers saved before encryption was added are still readable. Values encrypted by an earlier version, which used a fixed IV, can no longer be read and show up empty. The encrypted IP lives in the `ip_cifrada` column, which Hibernate adds on startup.
- All calls to external APIs go through `ExternalHTTPRequestHandler`, which converts each JSON response into its own DTO with Gson. Data from those APIs that is not stored has a DTO only, with no entity or repository.
- The Javadoc is in the `docs` folder (open `docs/index.html`).
- The percentages are estimates made by AI models. They are not proof of authorship and should be read together with other criteria.
- The location in the audit log comes from the public IP address, so it is approximate and usually only accurate to the city. Requests made from `localhost` are located using the server's public IP.
- The PDF reports use the Carlito font, included under the SIL Open Font License (`src/main/resources/fonts/OFL.txt`).
