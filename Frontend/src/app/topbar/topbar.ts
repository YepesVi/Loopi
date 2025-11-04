import { Component, OnInit } from '@angular/core';
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
export class Topbar implements OnInit {
  searchQuery = '';
  nombreUsuario = 'Usuario';
  fotoUsuario = 'https://cdn-icons-png.flaticon.com/512/4140/4140048.png';
  sesionActiva = false;

  constructor(private router: Router, private usuarioService: UsuarioService) {}

  ngOnInit(): void {
    this.usuarioService.nombreUsuario$.subscribe(nombre => {
      this.nombreUsuario = nombre || 'Usuario';
    });

    this.usuarioService.fotoUsuario$.subscribe(foto => {
      this.fotoUsuario = foto || 'https://cdn-icons-png.flaticon.com/512/4140/4140048.png';
    });

    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.actualizarSesion();
      });

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
    this.router.navigateByUrl('/home');
  }
}
