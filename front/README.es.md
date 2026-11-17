# Detector de contenido generado con IA - Frontend

[English](README.md) | **Español**

Este es el frontend de nuestro proyecto final de *Desarrollo de Sistemas de Información 3* en la Universidad El Bosque. Es una aplicación de Angular donde los usuarios suben archivos o un repositorio de GitHub y ven qué tan probable es que cada archivo haya sido generado con IA.

Solo se comunica con nuestro propio backend (la API de Spring Boot de la carpeta `back`). Todas las llamadas a los proveedores de IA y a otras APIs externas se hacen desde el backend.

## Qué se puede hacer

- Crear una cuenta, confirmarla por correo, iniciar sesión y recuperar la contraseña.
- Enviar archivos (incluidos `.zip`) o un repositorio público de GitHub para analizar.
- Seguir el avance de cada análisis y ver qué respondió cada IA sobre cada archivo, con sus motivos.
- Abrir o descargar los reportes PDF y pedir que lleguen otra vez al correo.
- Revisar el historial con filtros y paginación.
- Cambiar el nombre, el teléfono y la contraseña desde el perfil.
- Como administrador: gestionar usuarios, ver todos los análisis, revisar la auditoría con un enlace al mapa en cada registro y ver estadísticas de uso con gráficas. La lista de usuarios, la auditoría y las estadísticas se pueden exportar a PDF.

## Tecnologías

- Angular 22 (con módulos, señales y el nuevo control de flujo `@if` / `@for`)
- ngx-toastr para las notificaciones
- Chart.js para las gráficas de estadísticas
- Vitest para las pruebas unitarias (`ng test`)

## Cómo empezar

### Requisitos

- Node.js 22 o superior
- El backend corriendo (por defecto en `http://localhost:8080`)

### Instalar y ejecutar

```bash
npm install
npm start
```

La aplicación abre en [http://localhost:4200](http://localhost:4200).

La URL del backend está en un solo lugar: `URL_BACK` en `src/app/services/authservice.ts`. Ahí se cambia cuando el backend esté desplegado.

### Pruebas

```bash
npm test
```

Hay 71 pruebas entre componentes, servicios, guards, el interceptor y las utilidades. También corren solas en GitHub Actions en cada pull request.

### Compilar

```bash
npm run build
```

El resultado queda en `dist/`. El archivo `public/_redirects` hace que las rutas de Angular funcionen cuando el sitio se despliega en Netlify.

## Páginas

| Ruta | Página | Acceso |
|---|---|---|
| `/` | Inicio | Todos |
| `/login`, `/registro` | Inicio de sesión y registro | Todos |
| `/verificar-correo`, `/recuperar-contrasena`, `/restablecer-contrasena` | Confirmación de correo y recuperación de contraseña (a estas llevan los enlaces de los correos) | Todos |
| `/analizar` | Nuevo análisis | Con sesión |
| `/historial`, `/analisis/:id` | Historial y detalle de un análisis | Con sesión |
| `/perfil` | Perfil | Con sesión |
| `/admin/usuarios`, `/admin/analisis`, `/admin/auditoria`, `/admin/estadisticas` | Módulo de administración | Rol `ADMIN` |

Cualquier otra ruta muestra la página 404.

## Estructura del proyecto

```
src/app
├── components   Una carpeta por componente (xcomponent: .ts, .html, .css y .spec.ts)
├── models       Interfaces y tipos que replican los DTOs del backend (x.model.ts)
├── services     Servicios HTTP, los dos guards de rutas y el interceptor del JWT (xservice.ts)
└── util         Utilidades compartidas: notificaciones, formato de valores y manejo de PDF
```

El JWT se guarda en `localStorage` y el interceptor lo agrega a cada petición protegida. Si el backend responde 401 o 403, se cierra la sesión y el usuario vuelve al inicio de sesión.
