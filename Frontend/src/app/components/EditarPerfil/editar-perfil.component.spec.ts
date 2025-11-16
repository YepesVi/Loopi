import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EditarPerfilComponent } from './editar-perfil.component';
import { AuthService } from '../../services/auth.service';
import { UsuarioService } from '../../services/usuario.service';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

describe('EditarPerfilComponent', () => {
  let component: EditarPerfilComponent;
  let fixture: ComponentFixture<EditarPerfilComponent>;
  let mockAuth: jasmine.SpyObj<AuthService>;
  let mockUsuario: jasmine.SpyObj<UsuarioService>;

  beforeEach(async () => {
    mockAuth = jasmine.createSpyObj('AuthService', ['updateProfile']);
    mockUsuario = jasmine.createSpyObj('UsuarioService', ['actualizarNombre', 'actualizarFoto']);

    await TestBed.configureTestingModule({
      imports: [EditarPerfilComponent, ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: AuthService, useValue: mockAuth },
        { provide: UsuarioService, useValue: mockUsuario }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EditarPerfilComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crear el formulario con campos iniciales', () => {
    expect(component.form).toBeDefined();
    expect(component.form.contains('nombre')).toBeTrue();
    expect(component.form.contains('correo')).toBeTrue();
    expect(component.form.contains('fotoUrl')).toBeTrue();
  });
/*
  it('debería actualizar perfil exitosamente', () => {
    const datos = {
      nombre: 'Derinson',
      apellido: 'Dev',
      cedula: '123',
      telefono: '456',
      correo: 'derinson@mail.com',
      direccion: 'Calle 123',
      password: 'secure',
      fotoUrl: 'avatar.png'
    };

    component.form.setValue(datos);

    mockAuth.updateProfile.and.returnValue(of({ mensaje: 'Perfil actualizado' }));

    component.actualizar();

    expect(mockAuth.updateProfile).toHaveBeenCalledWith(datos);
    expect(component.mensaje).toContain('✅');
    expect(component.error).toBeFalse();
    expect(mockUsuario.actualizarNombre).toHaveBeenCalledWith('Derinson');
    expect(mockUsuario.actualizarFoto).toHaveBeenCalledWith('avatar.png');
  });
  */

  it('debería manejar error al actualizar perfil', () => {
    const datos = {
      nombre: 'Derinson',
      apellido: 'Dev',
      cedula: '123',
      telefono: '456',
      correo: 'derinson@mail.com',
      direccion: 'Calle 123',
      password: 'secure',
      fotoUrl: 'avatar.png'
    };

    component.form.setValue(datos);

    mockAuth.updateProfile.and.returnValue(throwError(() => new Error('Error')));

    component.actualizar();

    expect(component.mensaje).toContain('❌');
    expect(component.error).toBeTrue();
  });

  it('debería alternar visibilidad de avatares', () => {
    expect(component.mostrarAvatares).toBeFalse();
    component.toggleAvatars();
    expect(component.mostrarAvatares).toBeTrue();
    component.toggleAvatars();
    expect(component.mostrarAvatares).toBeFalse();
  });

  it('debería establecer avatar y ocultar selector', () => {
    const url = 'https://example.com/avatar.png';
    component.setAvatar(url);
    expect(component.form.value.fotoUrl).toBe(url);
    expect(component.mostrarAvatares).toBeFalse();
  });
});
