import { BootstrapContext, bootstrapApplication } from '@angular/platform-browser';
import { App } from './app/app';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { routes } from './app/app.routes';

const bootstrap = (context: BootstrapContext) =>
  bootstrapApplication(App, {
    providers: [
      provideRouter(routes),
      provideHttpClient()
    ]
  }, context);

export default bootstrap;
