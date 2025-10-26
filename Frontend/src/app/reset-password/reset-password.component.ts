import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent {
  form: FormGroup;
  mensaje: string = '';
  enviado: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.form = this.fb.group({
      correo: ['', [Validators.required, Validators.email]],
      cedula: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  submit(): void {
    if (this.form.invalid) return;

    const { correo, cedula, newPassword } = this.form.value;

    this.authService.resetPasswordDirecto({ correo, cedula, newPassword }).subscribe({
      next: () => {
        this.mensaje = '✅ Contraseña actualizada correctamente';
        this.enviado = true;
        this.router.navigate(['/login-register']);
      },
      error: err => {
        const mensajeError = typeof err.error === 'string' ? err.error : 'Error inesperado';
        this.mensaje = '❌ ' + mensajeError;
        this.enviado = false;
      }
    });
  }
}
