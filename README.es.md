# Detector de contenido generado con IA

[English](README.md) | **Español**

Proyecto final de *Desarrollo de Sistemas de Información 3* en la Universidad El Bosque (2026-2).

Aplicación web que estima qué tan probable es que un archivo haya sido generado con inteligencia artificial. Los usuarios suben archivos (texto, código, PDF, documentos de Office, imágenes, audio, video o `.zip`) o el enlace de un repositorio público de GitHub. Cada archivo se envía a tres modelos de IA distintos, y la aplicación promedia sus respuestas, guarda un historial y envía un reporte PDF al correo.

## Estructura del repositorio

| Carpeta | Contenido | Documentación |
|---|---|---|
| [`back/proyectofinal`](back/proyectofinal) | API REST con Spring Boot, JWT y MySQL | [README](back/proyectofinal/README.es.md) |
| [`front`](front) | Aplicación de Angular | [README](front/README.es.md) |

Cada README explica cómo configurar y ejecutar su parte.

## Forma de trabajo

- `main` tiene la versión que se entrega. Nadie hace commits directos en ella.
- `ramaBack` y `ramaFront` son las ramas de integración de cada parte.
- Cada cambio va en su propia rama (`feature/...`, `docs/...`, `ci/...`) y entra con un pull request.
- En cada pull request, GitHub Actions corre las pruebas del backend y del frontend, y DeepSource revisa la calidad del código. Un pull request solo se puede unir cuando las dos cosas pasan.

## Equipo

- Laura Gabriela Diaz Amaya
- Vivian Sofia Sierra Rivera
- Sergio Alejandro Parra Rivera
- Jesús David Vega Malaver
