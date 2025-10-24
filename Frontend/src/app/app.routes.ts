import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guards';


export const routes: Routes = [
  {  
  path: 'login-register',
    loadComponent: () =>
      import('./login-register/login-register.component').then(m => m.LoginRegisterComponent)
  },
  {
    path: '',
    redirectTo: 'login-register',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'reset-password',
    loadComponent: () =>
      import('./reset-password/reset-password.component').then(m => m.ResetPasswordComponent)
  }



];
