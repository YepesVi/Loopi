import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guards';
import { Home } from './components/home/home';


export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' }, // Ruta por defecto redirige a 'home'

  {  
  path: 'login-register',
    loadComponent: () =>
      import('./login-register/login-register.component').then(m => m.LoginRegisterComponent)
  },
  { path: 'home',
    component: Home
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
  },

  {
  path: 'editar-perfil',
  canActivate: [authGuard],
  loadComponent: () =>
<<<<<<< HEAD
    import('./EditarPerfil/editar-perfil.component').then(m => m.EditarPerfilComponent)
=======
    import('./EditarPerfil/editar-perfil.component').then(m => m.EditarPerfilComponent )
>>>>>>> f9003cf33cfd3f7456d03fc3156dcc45d35f17fe
}

];
