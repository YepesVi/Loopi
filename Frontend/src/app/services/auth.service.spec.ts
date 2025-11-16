import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const baseUrl = 'http://localhost:8081/api/auth';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('debería hacer login y actualizar estado', () => {
    const credentials = { correo: 'test@mail.com', password: '123456' };
    const mockResponse = { token: 'abc123' };

    service.login(credentials).subscribe(res => {
      expect(res.token).toBe('abc123');
      expect(service.isLoggedIn()).toBeTrue();
      expect(localStorage.getItem('loggedIn')).toBe('true');
    });

    const req = httpMock.expectOne(`${baseUrl}/login`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('debería hacer registro con FormData', () => {
    const formData = new FormData();
    formData.append('correo', 'nuevo@mail.com');
    formData.append('password', 'secure');

    service.register(formData).subscribe(res => {
      expect(res).toEqual({ mensaje: 'Registrado' });
    });

    const req = httpMock.expectOne(`${baseUrl}/register`);
    expect(req.request.method).toBe('POST');
    req.flush({ mensaje: 'Registrado' });
  });

  it('debería enviar correo para recuperación de contraseña', () => {
    service.recoverPassword('test@mail.com').subscribe(res => {
      expect(res).toBe('Correo enviado');
    });

    const req = httpMock.expectOne(`${baseUrl}/recover`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ correo: 'test@mail.com' });
    req.flush('Correo enviado');
  });

  it('debería enviar datos para restablecer contraseña', () => {
    const payload = { token: 'abc123', nuevaPassword: 'secure' };

    service.resetPasswordDirecto(payload).subscribe(res => {
      expect(res).toBe('Contraseña actualizada');
    });

    const req = httpMock.expectOne(`${baseUrl}/reset-direct`);
    expect(req.request.method).toBe('POST');
    req.flush('Contraseña actualizada');
  });

  it('debería actualizar perfil', () => {
    const data = { nombre: 'Derinson', telefono: '123456' };

    service.updateProfile(data).subscribe(res => {
      expect(res).toBe('Perfil actualizado');
    });

    const req = httpMock.expectOne(`${baseUrl}/update`);
    expect(req.request.method).toBe('PUT');
    req.flush('Perfil actualizado');
  });

  it('debería cambiar estado a false al hacer logout', () => {
    localStorage.setItem('loggedIn', 'true');
    service.logout();
    expect(service.isLoggedIn()).toBeFalse();
    expect(localStorage.getItem('loggedIn')).toBeNull();
  });
});
