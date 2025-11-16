import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { UsuarioService } from '../../services/usuario.service';

@Component({
  selector: 'app-editar-perfil',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './editar-perfil.component.html',
  styleUrls: ['./editar-perfil.component.css']
})
export class EditarPerfilComponent implements OnInit {
  form!: FormGroup;
  mensaje: string = '';
  error: boolean = false;
  mostrarAvatares: boolean = false;
  defaultFoto = 'https://cdn-icons-png.flaticon.com/512/4140/4140048.png';

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private usuarioService: UsuarioService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      cedula: [''],
      nombre: [''],
      apellido: [''],
      telefono: [''],
      correo: [''],
      direccion: [''],
      password: [''],
      fotoUrl: ['']
    });

    this.form.patchValue({
      nombre: localStorage.getItem('nombreUsuario'),
      apellido: localStorage.getItem('apellido'),
      cedula: localStorage.getItem('cedula'),
      correo: localStorage.getItem('correo'),
      telefono: localStorage.getItem('telefono'),
      direccion: localStorage.getItem('direccion'),
      password: localStorage.getItem('password'),
      fotoUrl: localStorage.getItem('foto') || this.defaultFoto
    });
  }

  actualizar(): void {
    const datos = this.form.value;

    this.auth.updateProfile(datos).subscribe({
      next: (res: any) => {
        this.mensaje = res.mensaje || '✅ Perfil actualizado exitosamente.';
        this.error = false;

        localStorage.clear();
        localStorage.setItem('nombreUsuario', datos.nombre);
        localStorage.setItem('apellido', datos.apellido);
        localStorage.setItem('cedula', datos.cedula);
        localStorage.setItem('telefono', datos.telefono);
        localStorage.setItem('correo', datos.correo);
        localStorage.setItem('direccion', datos.direccion);
        localStorage.setItem('password', datos.password);
        localStorage.setItem('foto', datos.fotoUrl);

        this.usuarioService.actualizarNombre(datos.nombre);
        this.usuarioService.actualizarFoto(datos.fotoUrl); 


        setTimeout(() => {
          this.mensaje = '';
        }, 3000);
      },
      error: () => {
        this.mensaje = '❌ Error al actualizar el perfil.';
        this.error = true;

        setTimeout(() => {
          this.mensaje = '';
        }, 3000);
      }
    });
  }

  toggleAvatars(): void {
    this.mostrarAvatares = !this.mostrarAvatares;
  }

  setAvatar(url: string): void {
    this.form.patchValue({ fotoUrl: url });
    this.mostrarAvatares = false;
  }
}
