import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Principalcomponent } from './components/principalcomponent/principalcomponent';
import { Logincomponent } from './components/logincomponent/logincomponent';
import { Registrocomponent } from './components/registrocomponent/registrocomponent';
import { Verificarcorreocomponent } from './components/verificarcorreocomponent/verificarcorreocomponent';
import { Recuperarcomponent } from './components/recuperarcomponent/recuperarcomponent';
import { Restablecercomponent } from './components/restablecercomponent/restablecercomponent';
import { Analizarcomponent } from './components/analizarcomponent/analizarcomponent';
import { Historialcomponent } from './components/historialcomponent/historialcomponent';
import { Detalleanalisiscomponent } from './components/detalleanalisiscomponent/detalleanalisiscomponent';
import { Perfilcomponent } from './components/perfilcomponent/perfilcomponent';
import { Adminusuarioscomponent } from './components/adminusuarioscomponent/adminusuarioscomponent';
import { Adminanalisiscomponent } from './components/adminanalisiscomponent/adminanalisiscomponent';
import { Auditoriacomponent } from './components/auditoriacomponent/auditoriacomponent';
import { Estadisticascomponent } from './components/estadisticascomponent/estadisticascomponent';
import { Noencontradocomponent } from './components/noencontradocomponent/noencontradocomponent';
import { authGuardService } from './services/authguardservice';
import { adminGuardService } from './services/adminguardservice';

/**
 * Rutas del aplicativo.
 *
 * `verificar-correo`, `restablecer-contrasena` e `historial` son las que usa el back en los
 * enlaces de los correos, así que no se deben cambiar.
 */
const routes: Routes = [
  { path: '', component: Principalcomponent },
  { path: 'login', component: Logincomponent },
  { path: 'registro', component: Registrocomponent },
  { path: 'verificar-correo', component: Verificarcorreocomponent },
  { path: 'recuperar-contrasena', component: Recuperarcomponent },
  { path: 'restablecer-contrasena', component: Restablecercomponent },

  // Requieren sesión iniciada.
  { path: 'analizar', component: Analizarcomponent, canActivate: [authGuardService] },
  { path: 'historial', component: Historialcomponent, canActivate: [authGuardService] },
  { path: 'analisis/:id', component: Detalleanalisiscomponent, canActivate: [authGuardService] },
  { path: 'perfil', component: Perfilcomponent, canActivate: [authGuardService] },

  // Solo para el rol ADMIN.
  { path: 'admin/usuarios', component: Adminusuarioscomponent, canActivate: [authGuardService, adminGuardService] },
  { path: 'admin/analisis', component: Adminanalisiscomponent, canActivate: [authGuardService, adminGuardService] },
  { path: 'admin/auditoria', component: Auditoriacomponent, canActivate: [authGuardService, adminGuardService] },
  { path: 'admin/estadisticas', component: Estadisticascomponent, canActivate: [authGuardService, adminGuardService] },

  // Cualquier dirección que no exista muestra la página 404.
  { path: '**', component: Noencontradocomponent },
];

/**
 * Módulo de rutas. Al cambiar de página se vuelve al inicio del scroll.
 */
@NgModule({
  imports: [RouterModule.forRoot(routes, { scrollPositionRestoration: 'top' })],
  exports: [RouterModule],
})
export class AppRoutingModule {}
