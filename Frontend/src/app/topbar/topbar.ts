import { Component } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './topbar.html',
  styleUrls: ['./topbar.css']
})
export class Topbar {
  searchQuery = '';
  nombreUsuario = 'Usuario';
  sesionActiva = false;

  constructor(private router: Router) {}

  ngOnInit(): void {
    // Detecta navegación para actualizar sesión sin recargar
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.actualizarSesion();
      });

    // También ejecuta al cargar el componente
    this.actualizarSesion();
  }

  actualizarSesion() {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('token');
      const nombre = localStorage.getItem('nombreUsuario');
      this.sesionActiva = !!token;
      this.nombreUsuario = nombre || 'Usuario';
    }
  }

  searchProduct() {
    if (this.searchQuery.trim()) {
      console.log('Buscando:', this.searchQuery);
      // Puedes activar navegación si tienes una ruta de búsqueda
      // this.router.navigate(['/buscar'], { queryParams: { q: this.searchQuery } });
    }
  }

  irALogin() {
    this.router.navigateByUrl('/login-register');
  }

  logout() {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('token');
      localStorage.removeItem('nombreUsuario');
      localStorage.removeItem('userId');
    }
    this.actualizarSesion(); // ✅ Refresca el estado local
    this.router.navigate(['/home']);
  }

  edit(){
    this.router.navigateByUrl('/editar-perfil');
  }
}
