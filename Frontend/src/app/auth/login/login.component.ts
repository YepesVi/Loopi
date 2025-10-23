import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  user = { correo: '', password: '' };
  message = '';
  datosEnviados = ''; // para mostrar los datos en pantalla

  constructor(private authService: AuthService, private router: Router) {}

  login() {
    // Validación básica
    if (!this.user.correo || !this.user.password) {
      this.message = '❌ Por favor ingresa correo y contraseña';
      return;
    }

    // Mostrar los datos que se van a enviar
   

    this.authService.login(this.user).subscribe({
      next: res => {
        localStorage.setItem('token', res.token); // ← si usas token
        this.message = '✅ Login exitoso';
        this.router.navigate(['/dashboard']);
      },
      error: err => {
        console.error('Error en login:', err);
        if (err.status === 401) {
          this.message = '❌ Usuario o contraseña incorrectos';
        } else {
          this.message = '❌ Error del servidor';
        }
      }
    });
  }

  goToRegister() {
    this.router.navigate(['/register']);
  }
}
