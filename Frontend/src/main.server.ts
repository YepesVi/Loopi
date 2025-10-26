import { BootstrapContext, bootstrapApplication } from '@angular/platform-browser';
import { App } from './app/app';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { routes } from './app/app.routes';

const bootstrap = (context: BootstrapContext) =>
  bootstrapApplication(App, {
    providers: [
      provideRouter(routes),
      provideHttpClient(withFetch())
    ]
  }, context);

export default bootstrap;
