import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginRegisterComponent } from './login-register.component';
import { AuthService } from '../../services/auth.service';
import { UsuarioService } from '../../services/usuario.service';
import { CarritoService } from '../../services/carrito/carrito-service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

describe('LoginRegisterComponent', () => {
  let component: LoginRegisterComponent;
  let fixture: ComponentFixture<LoginRegisterComponent>;
  let mockAuth: jasmine.SpyObj<AuthService>;
  let mockUsuario: jasmine.SpyObj<UsuarioService>;
  let mockCarrito: jasmine.SpyObj<CarritoService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockAuth = jasmine.createSpyObj('AuthService', ['login', 'register', 'recoverPassword']);
    mockUsuario = jasmine.createSpyObj('UsuarioService', ['actualizarNombre', 'actualizarFoto']);
    mockCarrito = jasmine.createSpyObj('CarritoService', ['crearCarrito']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [LoginRegisterComponent],
      providers: [
        { provide: AuthService, useValue: mockAuth },
        { provide: UsuarioService, useValue: mockUsuario },
        { provide: CarritoService, useValue: mockCarrito },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginRegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crear el componente', () => {
    expect(component).toBeTruthy();
  });

  // LOGIN
  it('debería mostrar error si login se envía vacío', () => {
    component.user.correo = '';
    component.user.password = '';
    component.login();
    expect(component.message).toContain('❌');
  });

  it('debería hacer login exitoso y navegar al home', () => {
    const mockResponse = {
      token: 'abc123',
      id: 1,
      nombre: 'Derinson',
      apellido: 'Dev',
      correo: 'derinson@mail.com',
      telefono: '123456',
      cedula: '123',
      direccion: 'Calle 123',
      password: 'secure',
      fotoUrl: 'avatar.png'
    };

    component.user.correo = mockResponse.correo;
    component.user.password = mockResponse.password;
    mockAuth.login.and.returnValue(of(mockResponse));

    component.login();

    expect(mockAuth.login).toHaveBeenCalledWith({ correo: mockResponse.correo, password: mockResponse.password });
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

  // REGISTER
  it('debería validar campos antes de registrar', () => {
    component.user.nombre = '';
    component.user.correo = 'invalid';
    component.user.password = '';
    component.register();
    expect(component.message).toContain('❌');
  });

  it('debería manejar error en registro', () => {
    mockAuth.register.and.returnValue(throwError(() => new Error('Error')));
    component.user.nombre = 'Derinson';
    component.user.correo = 'derinson@mail.com';
    component.user.password = 'secure';
    component.register();
    expect(component.message).toContain('❌ Error al registrar');
  });

  // RECOVER PASSWORD
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
