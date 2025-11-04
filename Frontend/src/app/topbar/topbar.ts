<<<<<<< HEAD
import { Component } from '@angular/core';
=======
import { Component, OnInit } from '@angular/core';
>>>>>>> f9003cf33cfd3f7456d03fc3156dcc45d35f17fe
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UsuarioService } from '../services/usuario.service';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './topbar.html',
  styleUrls: ['./topbar.css']
})
<<<<<<< HEAD
export class Topbar {
  searchQuery = '';
  nombreUsuario = 'Usuario';
  sesionActiva = false;

  constructor(private router: Router) {}

  ngOnInit(): void {
    // Detecta navegación para actualizar sesión sin recargar
=======
export class Topbar implements OnInit {
  searchQuery = '';
  nombreUsuario = 'Usuario';
  fotoUsuario = 'https://cdn-icons-png.flaticon.com/512/4140/4140048.png';
  sesionActiva = false;

  constructor(private router: Router, private usuarioService: UsuarioService) {}

  ngOnInit(): void {
    // 🔄 Escucha cambios en nombre y foto
    this.usuarioService.nombreUsuario$.subscribe(nombre => {
      this.nombreUsuario = nombre || 'Usuario';
    });

    this.usuarioService.fotoUsuario$.subscribe(foto => {
      this.fotoUsuario = foto || 'https://cdn-icons-png.flaticon.com/512/4140/4140048.png';
    });

    // Detecta navegación para actualizar sesión
>>>>>>> f9003cf33cfd3f7456d03fc3156dcc45d35f17fe
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.actualizarSesion();
      });

<<<<<<< HEAD
    // También ejecuta al cargar el componente
=======
>>>>>>> f9003cf33cfd3f7456d03fc3156dcc45d35f17fe
    this.actualizarSesion();
  }

  actualizarSesion() {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('token');
<<<<<<< HEAD
      const nombre = localStorage.getItem('nombreUsuario');
      this.sesionActiva = !!token;
      this.nombreUsuario = nombre || 'Usuario';
=======
      this.sesionActiva = !!token;
>>>>>>> f9003cf33cfd3f7456d03fc3156dcc45d35f17fe
    }
  }

  searchProduct() {
    if (this.searchQuery.trim()) {
      console.log('Buscando:', this.searchQuery);
<<<<<<< HEAD
      // Puedes activar navegación si tienes una ruta de búsqueda
      // this.router.navigate(['/buscar'], { queryParams: { q: this.searchQuery } });
=======
>>>>>>> f9003cf33cfd3f7456d03fc3156dcc45d35f17fe
    }
  }

  irALogin() {
    this.router.navigateByUrl('/login-register');
  }

  logout() {
    if (typeof window !== 'undefined') {
<<<<<<< HEAD
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

   dashboard(){
    this.router.navigateByUrl('/dashboard');
  }

  home(){
=======
      localStorage.clear();
    }

    this.usuarioService.actualizarNombre('Usuario');
    this.usuarioService.actualizarFoto('https://cdn-icons-png.flaticon.com/512/4140/4140048.png');

    this.actualizarSesion();
    this.router.navigate(['/home']);
  }

  edit() {
    this.router.navigateByUrl('/editar-perfil');
  }

  dashboard() {
    this.router.navigateByUrl('/dashboard');
  }

  home() {
>>>>>>> f9003cf33cfd3f7456d03fc3156dcc45d35f17fe
    this.router.navigateByUrl('/home');
  }
}

