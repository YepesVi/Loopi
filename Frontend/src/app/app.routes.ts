import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guards';
import { Home } from './components/home/home';


export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' }, // Ruta por defecto redirige a 'home'

  {
    path: 'login',
    loadComponent: () =>
      import('./auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./auth/register/register.component').then(m => m.RegisterComponent)
  },
  { path: 'home', component: Home},

 {
  path: 'dashboard',
  canActivate: [authGuard],
  loadComponent: () =>
    import('./dashboard/dashboard.component').then(m => m.DashboardComponent)
}



];
