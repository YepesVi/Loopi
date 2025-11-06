import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UsuarioService } from '../services/usuario.service';
import { PopupService } from '../services/categorias/popup';
import { AuthService } from '../services/auth.service';

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
  cerrandoSesion = false; // ✅ NUEVO

  constructor(
    private router: Router,
    private usuarioService: UsuarioService,
    private popupService: PopupService,
    private auth: AuthService
  ) {}

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

  actualizarSesion(): void {
    this.sesionActiva = this.auth.loggedInSignal();
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
      this.cerrandoSesion = false;
      this.actualizarSesion();
      this.router.navigate(['/home']);
    }, 1500); // ⏳ duración del mensaje
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
}
