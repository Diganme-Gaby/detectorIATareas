# AI Content Detector - Frontend

**English** | [Español](README.es.md)

This is the frontend of our final project for *Desarrollo de Sistemas de Información 3* at Universidad El Bosque. It is an Angular application where users upload files or a GitHub repository and see how likely it is that each file was generated with AI.

It only talks to our own backend (the Spring Boot API in the `back` folder). All calls to AI providers and other external APIs happen on the backend.

## What you can do

- Create an account, confirm it by email, log in and recover your password.
- Send files (including `.zip` archives) or a public GitHub repository for analysis.
- Follow the progress of each analysis and see what every AI answered for each file, with its reasons.
- Open or download the PDF reports and have them sent again by email.
- Browse your history with filters and pagination.
- Edit your name, phone number and password from your profile.
- As an admin: manage users, see every analysis, check the audit log with a map link for each entry, and view usage statistics with charts. The user list, the audit log and the statistics can be exported to PDF.

## Tech stack

- Angular 22 (NgModule based, with signals and the new `@if` / `@for` control flow)
- ngx-toastr for notifications
- Chart.js for the statistics charts
- Vitest for the unit tests (`ng test`)

## Getting started

### Requirements

- Node.js 22 or newer
- The backend running (by default on `http://localhost:8080`)

### Install and run

```bash
npm install
npm start
```

The app opens at [http://localhost:4200](http://localhost:4200).

The backend URL is defined in a single place: `URL_BACK` in `src/app/services/authservice.ts`. Change it there when the backend is deployed.

### Tests

```bash
npm test
```

There are 71 tests across components, services, guards, the interceptor and the utilities. They also run automatically on GitHub Actions for every pull request.

### Build

```bash
npm run build
```

The output goes to `dist/`. The `public/_redirects` file makes Angular routes work when the site is deployed on Netlify.

## Pages

| Route | Page | Access |
|---|---|---|
| `/` | Home | Everyone |
| `/login`, `/registro` | Log in and sign up | Everyone |
| `/verificar-correo`, `/recuperar-contrasena`, `/restablecer-contrasena` | Email confirmation and password recovery (linked from the emails) | Everyone |
| `/analizar` | New analysis | Logged in |
| `/historial`, `/analisis/:id` | History and analysis detail | Logged in |
| `/perfil` | Profile | Logged in |
| `/admin/usuarios`, `/admin/analisis`, `/admin/auditoria`, `/admin/estadisticas` | Admin module | `ADMIN` role |

Any other route shows a 404 page.

## Project structure

```
src/app
├── components   One folder per component (xcomponent: .ts, .html, .css and .spec.ts)
├── models       Interfaces and types that mirror the backend DTOs (x.model.ts)
├── services     HTTP services, the two route guards and the JWT interceptor (xservice.ts)
└── util         Shared helpers: notifications, value formatting and PDF handling
```

The JWT is stored in `localStorage` and added to every protected request by the interceptor. If the backend answers 401 or 403, the session is closed and the user goes back to the login page.
