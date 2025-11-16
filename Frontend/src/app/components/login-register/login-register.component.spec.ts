import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginRegisterComponent } from './login-register.component';
import { AuthService } from '../services/auth.service';
import { UsuarioService } from '../services/usuario.service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

describe('LoginRegisterComponent', () => {
  let component: LoginRegisterComponent;
  let fixture: ComponentFixture<LoginRegisterComponent>;
  let mockAuth: jasmine.SpyObj<AuthService>;
  let mockUsuario: jasmine.SpyObj<UsuarioService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockAuth = jasmine.createSpyObj('AuthService', ['login', 'register', 'recoverPassword']);
    mockUsuario = jasmine.createSpyObj('UsuarioService', ['actualizarNombre', 'actualizarFoto']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [LoginRegisterComponent, FormsModule], // ✅ standalone component goes in imports
      providers: [
        { provide: AuthService, useValue: mockAuth },
        { provide: UsuarioService, useValue: mockUsuario },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginRegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería mostrar error si login se envía vacío', () => {
    component.user.correo = '';
    component.user.password = '';
    component.login();
    expect(component.message).toContain('❌');
  });

  it('debería llamar login() y navegar al home si es exitoso', () => {
    const mockResponse = {
      token: 'abc123',
      id: 1,
      nombre: 'Derinson',
      apellido: 'Dev',
      correo: 'derinson@mail.com',
      telefono: '123456789',
      cedula: '123456',
      direccion: 'Calle 123',
      password: 'secure',
      fotoUrl: 'avatar.png'
    };

    component.user.correo = mockResponse.correo;
    component.user.password = mockResponse.password;
    mockAuth.login.and.returnValue(of(mockResponse));

    component.login();

    expect(mockAuth.login).toHaveBeenCalled();
    expect(mockUsuario.actualizarNombre).toHaveBeenCalledWith('Derinson');
    expect(mockUsuario.actualizarFoto).toHaveBeenCalledWith('avatar.png');
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/home']);
    expect(component.message).toContain('✅');
  });

  it('debería manejar error de login incorrecto', () => {
    mockAuth.login.and.returnValue(throwError(() => ({ status: 401 })));
    component.user.correo = 'fail@mail.com';
    component.user.password = 'wrong';
    component.login();
    expect(component.message).toContain('❌ Usuario o contraseña incorrectos');
  });

  it('debería validar campos antes de registrar', () => {
    component.user.nombre = '';
    component.user.correo = 'invalid';
    component.user.password = '';
    component.register();
    expect(component.message).toContain('❌');
  });
/*
  it('debería llamar register() si los datos son válidos', () => {
  component.user = {
    nombre: 'Derinson',
    apellido: 'Dev',
    cedula: '123',
    telefono: '456',
    correo: 'derinson@mail.com',
    direccion: 'Calle 123',
    password: 'secure',
    foto: null
  };

  mockAuth.register.and.returnValue(of({}));

  component.register();

  expect(mockAuth.register).toHaveBeenCalled();
  expect(component.message).toContain('✅ Registro exitoso'); // ✅ símbolo correcto
  expect(component.mode).toBe('login');
});
*/

  it('debería manejar error en registro', () => {
    mockAuth.register.and.returnValue(throwError(() => new Error('Error')));
    component.user.correo = 'derinson@mail.com';
    component.user.password = 'secure';
    component.register();
    expect(component.message).toContain('❌');
  });

  it('debería validar correo antes de recuperar contraseña', () => {
    component.user.correo = '';
    component.recoverPassword();
    expect(component.message).toContain('❌');
  });

  it('debería llamar recoverPassword() si el correo es válido', () => {
    component.user.correo = 'derinson@mail.com';
    mockAuth.recoverPassword.and.returnValue(of('Correo enviado'));
    component.recoverPassword();
    expect(mockAuth.recoverPassword).toHaveBeenCalledWith('derinson@mail.com');
    expect(component.message).toContain('✅');
  });

  it('debería manejar error en recuperación de contraseña', () => {
    mockAuth.recoverPassword.and.returnValue(throwError(() => ({ error: 'Correo no encontrado' })));
    component.user.correo = 'noexiste@mail.com';
    component.recoverPassword();
    expect(component.message).toContain('Correo no encontrado');
  });
});
