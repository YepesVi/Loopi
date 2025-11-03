import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private nombreUsuarioSubject = new BehaviorSubject<string>('');
  private fotoUsuarioSubject = new BehaviorSubject<string>('');

  nombreUsuario$ = this.nombreUsuarioSubject.asObservable();
  fotoUsuario$ = this.fotoUsuarioSubject.asObservable();

  constructor() {
    if (typeof window !== 'undefined') {
      const nombre = localStorage.getItem('nombreUsuario') || '';
      const foto = localStorage.getItem('foto') || 'https://cdn-icons-png.flaticon.com/512/4140/4140048.png';

      this.nombreUsuarioSubject.next(nombre);
      this.fotoUsuarioSubject.next(foto);
    }
  }

  actualizarNombre(nombre: string): void {
    if (typeof window !== 'undefined') {
      localStorage.setItem('nombreUsuario', nombre);
    }
    this.nombreUsuarioSubject.next(nombre);
  }

  actualizarFoto(fotoUrl: string): void {
    if (typeof window !== 'undefined') {
      localStorage.setItem('foto', fotoUrl);
    }
    this.fotoUsuarioSubject.next(fotoUrl);
  }
}
