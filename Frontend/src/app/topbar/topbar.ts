import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UsuarioService } from '../services/usuario.service';
import { PopupService } from '../services/categorias/popup';

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

  constructor(private router: Router, private usuarioService: UsuarioService,private popupService: PopupService) { }


   toggleCategoryPopup(): void {
    this.popupService.toggleCategoryPopup();
  }

  ngOnInit(): void {
    // 🔄 Escucha cambios en nombre y foto
    this.usuarioService.nombreUsuario$.subscribe(nombre => {
      this.nombreUsuario = nombre || 'Usuario';
    });

    this.usuarioService.fotoUsuario$.subscribe(foto => {
      this.fotoUsuario = foto || 'https://cdn-icons-png.flaticon.com/512/4140/4140048.png';
    });

    // Detecta navegación para actualizar sesión
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
      this.sesionActiva = !!token;
    }
  }

  searchProduct() {
    if (this.searchQuery.trim()) {
      console.log('Buscando:', this.searchQuery);
    }
  }

  irALogin() {
    this.router.navigateByUrl('/login-register');
  }

  logout() {
    if (typeof window !== 'undefined') {
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
    this.router.navigateByUrl('/home');
  }
}
