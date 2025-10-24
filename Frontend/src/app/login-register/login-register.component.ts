import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../services/auth.service';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-login-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login-register.component.html',
  styleUrls: ['./login-register.component.css']
})
export class LoginRegisterComponent {
  mode: 'login' | 'register' = 'login';
  message = '';
  datosEnviados = '';

  user = {
    nombre: '',
    apellido: '',
    cedula: '',
    telefono: '',
    correo: '',
    direccion: '',
    password: '',
    foto: null as File | null
  };

  constructor(private authService: AuthService, private router: Router) {}

  goToRegister() {
    this.mode = 'register';
    this.clearMessage();
  }

  goToLogin() {
    this.mode = 'login';
    this.clearMessage();
  }

  handleFile(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.user.foto = input.files[0];
    }
  }

  private clearMessageAfterDelay(ms: number = 3000): void {
    setTimeout(() => {
      this.message = '';
    }, ms);
  }

  private clearMessage(): void {
    this.message = '';
  }

  private resetUser(): void {
    this.user = {
      nombre: '',
      apellido: '',
      cedula: '',
      telefono: '',
      correo: '',
      direccion: '',
      password: '',
      foto: null
    };
  }

  private clearLoginFields(): void {
  this.user.correo = '';
  this.user.password = '';
}


  login() {
  if (!this.user.correo || !this.user.password) {
    this.message = '❌ Por favor ingresa correo y contraseña';
    this.clearMessageAfterDelay();
    return;
  }

  const loginPayload = {
    correo: this.user.correo,
    password: this.user.password
  };

  this.datosEnviados = JSON.stringify(loginPayload);

  this.authService.login(loginPayload).subscribe({
    next: res => {
      localStorage.setItem('token', res.token);
      this.message = '✅ Login exitoso';
      this.clearMessageAfterDelay();
      this.router.navigate(['/dashboard']);
    },
    error: err => {
      console.error('Error en login:', err);
      this.message = err.status === 401
        ? '❌ Usuario o contraseña incorrectos'
        : '❌ Error del servidor';
      this.clearLoginFields(); // ✅ Limpia correo y contraseña si hay error
      this.clearMessageAfterDelay();
    }
  });
}


  register() {
    const correoValido = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.user.correo);

    if (
      !this.user.nombre ||
      !this.user.correo ||
      !correoValido ||
      !this.user.password
    ) {
      this.message = '❌ Por favor ingresa un correo válido y completa los campos obligatorios';
      this.clearMessageAfterDelay();
      return;
    }

    const formData = new FormData();
    for (const key in this.user) {
      const value = (this.user as any)[key];
      if (key === 'foto' && value) {
        formData.append('foto', value);
      } else {
        formData.append(key, value);
      }
    }

    this.datosEnviados = JSON.stringify({
      nombre: this.user.nombre,
      apellido: this.user.apellido,
      cedula: this.user.cedula,
      telefono: this.user.telefono,
      correo: this.user.correo,
      direccion: this.user.direccion,
      password: this.user.password
    });

    this.authService.register(formData).subscribe({
      next: () => {
        this.message = '✅ Registro exitoso';
        this.clearMessageAfterDelay();
        this.resetUser();
        this.goToLogin();
      },
      error: err => {
        console.error('Error en registro:', err);
        this.message = '❌ Error al registrar';
        this.clearMessageAfterDelay();
      }
    });
  }

 recoverPassword() {
  if (!this.user.correo) {
    this.message = '❌ Ingresa tu correo para recuperar la contraseña';
    this.clearMessageAfterDelay();
    return;
  }

  this.authService.recoverPassword(this.user.correo).subscribe({
    next: res => {
      this.message = '✅ ' + res;
      this.clearMessageAfterDelay();
    },
    error: err => {
      console.error('Error al recuperar contraseña:', err);
      const mensajeError = typeof err.error === 'string' ? err.error : '❌ Error inesperado';
      this.message = mensajeError;
      this.clearMessageAfterDelay();
       this.clearLoginFields(); // ✅ Limpia correo y contraseña si hay error

    }
  });
}

}
