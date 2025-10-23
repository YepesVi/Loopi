import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
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

  message = '';

  constructor(private authService: AuthService, private router: Router) {}

  register() {
    const formData = new FormData();
    for (const key in this.user) {
      if (key === 'foto' && this.user.foto) {
        formData.append('foto', this.user.foto);
      } else {
        formData.append(key, (this.user as any)[key]);
      }
    }

    this.authService.register(formData).subscribe({
      next: () => {
        this.message = '✅ Registro exitoso';
        this.router.navigate(['/login']);
      },
      error: () => {
        this.message = '❌ Error al registrar';
      }
    });
  }

  handleFile(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.user.foto = input.files[0];
    }
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}
