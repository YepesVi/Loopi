import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guards';
import { Home } from './components/home/home';
import { Carrito } from './components/carrito/carrito';
import { ProductDetail } from './components/product-detail/product-detail';
import { ProductList } from './components/product-list/product-list';
import { HistorialCompra } from './components/historial-compra/historial-compra';


export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },

  {  
  path: 'login-register',
    loadComponent: () =>
      import('./components/login-register/login-register.component').then(m => m.LoginRegisterComponent)
  },
  { path: 'home',
    component: Home
  },
  {
  path: 'dashboard',
  canActivate: [authGuard],
  loadComponent: () =>
    import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'reset-password',
    loadComponent: () =>
      import('./components/reset-password/reset-password.component').then(m => m.ResetPasswordComponent)
  },
  
  {
    path: 'historial/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./historial/historial.component').then(m => m.HistorialProductoComponent)
  },

  {
    path: 'editar-perfil',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./components/EditarPerfil/editar-perfil.component').then(m => m.EditarPerfilComponent)
  },

  {
    path: 'historial-general',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./historial-general/historial-general.component')
        .then(m => m.HistorialGeneralComponent)
  },
  { path: 'carrito',
    component: Carrito
  },

  { path: 'producto/:id', component: ProductDetail },

  { path: 'productos', component: ProductList },

  { path: 'historial-compras', component: HistorialCompra },

];
