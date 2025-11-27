import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResetPasswordComponent } from './reset-password.component';
import { AuthService } from '../../services/auth.service';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

describe('ResetPasswordComponent', () => {
  let component: ResetPasswordComponent;
  let fixture: ComponentFixture<ResetPasswordComponent>;
  let mockAuth: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockRoute: Partial<ActivatedRoute>;

  beforeEach(async () => {
    mockAuth = jasmine.createSpyObj('AuthService', ['resetPasswordDirecto']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockRoute = {};

    await TestBed.configureTestingModule({
      imports: [ResetPasswordComponent, ReactiveFormsModule], // ✅ standalone
      providers: [
        FormBuilder,
        { provide: AuthService, useValue: mockAuth },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockRoute }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ResetPasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crear el componente', () => {
    expect(component).toBeTruthy();
  });

  it('debería inicializar el formulario con correo guardado en localStorage', () => {
    localStorage.setItem('correoRecuperacion', 'test@mail.com');
    const comp = new ResetPasswordComponent(new FormBuilder(), mockAuth, mockRouter, {} as any);
    expect(comp.form.get('correo')?.value).toBe('test@mail.com');
  });

  it('debería mostrar mensaje de error si el formulario es inválido', () => {
    component.form.setValue({ correo: '', cedula: '', newPassword: '' });
    component.submit();
    expect(component.mensaje).toContain('❌');
    expect(component.enviado).toBeFalse();
  });

  it('debería llamar a AuthService y navegar en caso de éxito', () => {
    mockAuth.resetPasswordDirecto.and.returnValue(of({}));
    component.form.setValue({
      correo: 'derinson@mail.com',
      cedula: '123456',
      newPassword: 'securepass'
    });

    component.submit();

    expect(mockAuth.resetPasswordDirecto).toHaveBeenCalledWith({
      correo: 'derinson@mail.com',
      cedula: '123456',
      newPassword: 'securepass'
    });

    expect(component.enviado).toBeTrue();
    expect(component.cargando).toBeFalse();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/login-register']);
  });

  it('debería manejar error y mostrar mensaje', () => {
    mockAuth.resetPasswordDirecto.and.returnValue(throwError(() => ({ error: 'Correo no válido' })));
    component.form.setValue({
      correo: 'fail@mail.com',
      cedula: '999999',
      newPassword: '123456'
    });

    component.submit();

    expect(component.enviado).toBeFalse();
    expect(component.cargando).toBeFalse();
    expect(component.mensaje).toContain('❌ Correo no válido');
  });

  it('debería limpiar mensaje después de tiempo', (done) => {
    component['mostrarMensaje']('Mensaje temporal', 1000);
    expect(component.mensaje).toBe('Mensaje temporal');

    setTimeout(() => {
      expect(component.mensaje).toBe('');
      done();
    }, 1100);
  });
});
