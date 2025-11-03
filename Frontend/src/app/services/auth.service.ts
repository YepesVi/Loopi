import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private baseUrl = 'http://localhost:8081/api/auth';

  constructor(private http: HttpClient) {}

  // 🔐 Login por correo y contraseña
  login(credentials: { correo: string; password: string }): Observable<any> {
    return this.http.post(`${this.baseUrl}/login`, credentials);
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

}
