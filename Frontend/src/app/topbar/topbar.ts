import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd, RouterModule } from '@angular/router';
import { filter } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UsuarioService } from '../services/usuario.service';
import { PopupService } from '../services/categorias/popup';
import { AuthService } from '../services/auth.service';
import { CarritoService } from '../services/carrito/carrito-service';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './topbar.html',
  styleUrls: ['./topbar.css']
})
export class Topbar implements OnInit {
  searchQuery = '';
  nombreUsuario = 'Usuario';
  fotoUsuario = 'https://cdn-icons-png.flaticon.com/512/4140/4140048.png';
  sesionActiva = false;
  cerrandoSesion = false;

  // 🔹 Nueva propiedad para el carrito
  cantidadCarrito: number = 0;

  constructor(
    private router: Router,
    private usuarioService: UsuarioService,
    private popupService: PopupService,
    private auth: AuthService,
    private carritoService: CarritoService
  ) {}

  ngOnInit(): void {
    // Suscripción al nombre y foto del usuario
    this.usuarioService.nombreUsuario$.subscribe(nombre => {
      this.nombreUsuario = nombre || 'Usuario';
    });

    this.usuarioService.fotoUsuario$.subscribe(foto => {
      this.fotoUsuario = foto || 'https://cdn-icons-png.flaticon.com/512/4140/4140048.png';
    });

    // Actualizar sesión en cada navegación
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.actualizarSesion();
      });

    this.actualizarSesion();

    // 🔹 Suscripción a la cantidad del carrito
    this.carritoService.cantidad$.subscribe(cantidad => {
      this.cantidadCarrito = cantidad;
    });

    // 🔹 Cargar carrito inicial para mostrar badge
    this.carritoService.getCarrito().subscribe();
  }

  actualizarSesion(): void {
  this.sesionActiva = this.auth.loggedInSignal();

  if (this.sesionActiva) {
    // 🔹 Refrescar carrito al iniciar sesión
    this.carritoService.getCarrito().subscribe();
  } else {
    // 🔹 Si no hay sesión, resetear contador
    this.carritoService.resetCantidad();
  }
}


  toggleCategoryPopup(): void {
    this.popupService.toggleCategoryPopup();
  }

  searchProduct(): void {
    if (this.searchQuery.trim()) {
      console.log('Buscando:', this.searchQuery);
    }
  }

  irALogin(): void {
    this.router.navigateByUrl('/login-register');
    localStorage.clear();
  }

  logout(): void {
    this.cerrandoSesion = true;

    setTimeout(() => {
      this.auth.logout();
      this.usuarioService.actualizarNombre('Usuario');
      localStorage.clear();
      this.usuarioService.actualizarFoto('https://cdn-icons-png.flaticon.com/512/4140/4140048.png');
      this.carritoService.resetCantidad();
      this.cerrandoSesion = false;
      this.actualizarSesion();
      this.router.navigate(['/home']);
    }, 1500);
  }

  edit(): void {
    this.router.navigateByUrl('/editar-perfil');
  }

  dashboard(): void {
    this.router.navigateByUrl('/dashboard');
  }

  home(): void {
    this.router.navigateByUrl('/home');
  }

  irAlCarrito() {
    const cedula = localStorage.getItem('cedula');

    if (!cedula) {
      this.router.navigate(['/login-register']);
      return;
    }

    this.router.navigate(['/carrito']);
  }
}
