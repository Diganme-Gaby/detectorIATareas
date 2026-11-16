# AI Content Detector

**English** | [Español](README.es.md)

Final project for *Desarrollo de Sistemas de Información 3* at Universidad El Bosque (2026-2).

A web application that estimates how likely it is that a file was generated with artificial intelligence. Users upload files (text, code, PDF, Office documents, images, audio, video or `.zip` archives) or the link of a public GitHub repository. Each file is sent to three different AI models, and the application averages their answers, keeps a history and emails a PDF report.

## Repository structure

| Folder | Contents | Documentation |
|---|---|---|
| [`back/proyectofinal`](back/proyectofinal) | REST API with Spring Boot, JWT and MySQL | [README](back/proyectofinal/README.md) |
| [`front`](front) | Angular application | [README](front/README.md) |

Each README explains how to configure and run its part.

## Workflow

- `main` holds the version that is delivered. Nobody commits to it directly.
- `ramaBack` and `ramaFront` are the integration branches for each part.
- Every change goes in its own branch (`feature/...`, `docs/...`, `ci/...`) and enters through a pull request.
- On every pull request, GitHub Actions runs the backend and frontend tests, and DeepSource analyzes code quality. A pull request can only be merged when both pass.

## Team

- Laura Gabriela Diaz Amaya
- Vivian Sofia Sierra Rivera
- Sergio Alejandro Parra Rivera
- Jesús David Vega Malaver
