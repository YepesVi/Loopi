import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private nombreUsuarioSubject = new BehaviorSubject<string>('');
  nombreUsuario$ = this.nombreUsuarioSubject.asObservable();

  constructor() {
    if (typeof window !== 'undefined') {
      const nombre = localStorage.getItem('nombreUsuario') || '';
      this.nombreUsuarioSubject.next(nombre);
    }
  }

  actualizarNombre(nombre: string): void {
    if (typeof window !== 'undefined') {
      localStorage.setItem('nombreUsuario', nombre);
    }
    this.nombreUsuarioSubject.next(nombre);
  }
}
