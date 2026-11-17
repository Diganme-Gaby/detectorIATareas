import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ToastrModule } from 'ngx-toastr';
import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { Navbarcomponent } from './components/navbarcomponent/navbarcomponent';
import { Footercomponent } from './components/footercomponent/footercomponent';
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
import { Paginacioncomponent } from './components/paginacioncomponent/paginacioncomponent';
import { Verclavecomponent } from './components/verclavecomponent/verclavecomponent';
import { authInterceptorService } from './services/authinterceptorservice';

/**
 * Módulo raíz del aplicativo.
 *
 * Declara todos los componentes, configura ngx-toastr para los mensajes y registra el
 * interceptor que agrega el token JWT a las peticiones.
 */
@NgModule({
  declarations: [
    App,
    Navbarcomponent,
    Footercomponent,
    Principalcomponent,
    Logincomponent,
    Registrocomponent,
    Verificarcorreocomponent,
    Recuperarcomponent,
    Restablecercomponent,
    Analizarcomponent,
    Historialcomponent,
    Detalleanalisiscomponent,
    Perfilcomponent,
    Adminusuarioscomponent,
    Adminanalisiscomponent,
    Auditoriacomponent,
    Estadisticascomponent,
    Noencontradocomponent,
    Paginacioncomponent,
    Verclavecomponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ToastrModule.forRoot({
      timeOut: 4500,
      positionClass: 'toast-top-right',
      progressBar: true,
      preventDuplicates: true,
      closeButton: false,
    }),
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(withInterceptors([authInterceptorService])),
  ],
  bootstrap: [App],
})
export class AppModule {}
