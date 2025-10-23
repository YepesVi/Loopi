import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guards';


export const routes: Routes = [
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
  { path: '', redirectTo: 'login', pathMatch: 'full' },

 {
  path: 'dashboard',
  canActivate: [authGuard],
  loadComponent: () =>
    import('./dashboard/dashboard.component').then(m => m.DashboardComponent)
}



];
