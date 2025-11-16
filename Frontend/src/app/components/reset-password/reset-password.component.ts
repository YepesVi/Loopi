import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';

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
  cargando: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    const correoGuardado = localStorage.getItem('correoRecuperacion') || '';

    this.form = this.fb.group({
      correo: [correoGuardado, [Validators.required, Validators.email]],
      cedula: ['', [Validators.required, Validators.pattern(/^\d+$/)]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  private mostrarMensaje(texto: string, tiempo: number = 3000): void {
    this.mensaje = texto;
    setTimeout(() => {
      this.mensaje = '';
    }, tiempo);
  }

  submit(): void {
    if (this.form.invalid) {
      this.mostrarMensaje('❌ Verifica los campos antes de continuar');
      return;
    }

    this.cargando = true;
    this.mostrarMensaje('🔄 Actualizando contraseña...', 5000);

    const { correo, cedula, newPassword } = this.form.value;

    this.authService.resetPasswordDirecto({ correo, cedula, newPassword }).subscribe({
      next: () => {
        this.mostrarMensaje('✅ Contraseña actualizada correctamente', 4000);
        this.enviado = true;
        this.cargando = false;

        localStorage.clear();
        this.router.navigate(['/login-register']);
      },
      error: err => {
        const mensajeError = typeof err.error === 'string' ? err.error : 'Error inesperado';
        this.mostrarMensaje('❌ ' + mensajeError, 4000);
        this.enviado = false;
        this.cargando = false;
      }
    });
  }
}
