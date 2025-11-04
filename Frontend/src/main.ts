import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter, withHashLocation } from '@angular/router';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { routes } from './app/app.routes';
import { App } from './app/app';

bootstrapApplication(App, {
  providers: [
    provideRouter(routes, withHashLocation()), // ✅ Evita "Cannot GET /dashboard"
    provideHttpClient(withFetch())             // ✅ Habilita HttpClient con fetch
  ]
});
