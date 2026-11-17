import { platformBrowser } from '@angular/platform-browser';
import { AppModule } from './app/app-module';

/** Arranca el aplicativo con el módulo raíz. */
platformBrowser().bootstrapModule(AppModule)
  .catch(err => console.error(err));
