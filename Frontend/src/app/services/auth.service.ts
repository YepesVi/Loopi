import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private baseUrl = 'http://localhost:8081/api/auth';

  constructor(private http: HttpClient) {}

  // Login por correo y contraseña
  login(credentials: { correo: string; password: string }): Observable<any> {
    return this.http.post(`${this.baseUrl}/login`, credentials);
  }

  // Registro usando FormData (multipart/form-data)
  register(formData: FormData): Observable<any> {
    return this.http.post(`${this.baseUrl}/register`, formData);
  }
}
