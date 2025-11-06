import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private baseUrl = 'http://localhost:8081/api/auth';

  // Estado reactivo de sesión
  private readonly loggedIn = signal(false);
  readonly loggedInSignal = this.loggedIn; // ✅ acceso público al signal

  constructor(private http: HttpClient) {
    if (typeof window !== 'undefined') {
      const stored = localStorage.getItem('loggedIn');
      this.loggedIn.set(stored === 'true');
    }
  }

  // 🔐 Login por correo y contraseña
  login(credentials: { correo: string; password: string }): Observable<any> {
    return this.http.post(`${this.baseUrl}/login`, credentials).pipe(
      tap(() => {
        this.loggedIn.set(true);
        localStorage.setItem('loggedIn', 'true');
      })
    );
  }

  // 📝 Registro usando FormData (multipart/form-data)
  register(formData: FormData): Observable<any> {
    return this.http.post(`${this.baseUrl}/register`, formData);
  }

  // 📧 Recuperación de contraseña por correo
  recoverPassword(correo: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/recover`, { correo }, { responseType: 'text' });
  }

  // 🔒 Restablecer contraseña 
  resetPasswordDirecto(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/reset-direct`, payload, { responseType: 'text' });
  }

  // 🔄 Actualizar perfil
  updateProfile(data: any): Observable<string> {
    return this.http.put(`${this.baseUrl}/update`, data, { responseType: 'text' });
  }

  // ✅ Estado de sesión
  isLoggedIn(): boolean {
    return this.loggedIn();
  }

  logout(): void {
    this.loggedIn.set(false);
    localStorage.removeItem('loggedIn');
  }
}
