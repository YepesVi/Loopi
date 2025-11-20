import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class CarritoService {
  private baseUrl = 'http://localhost:8081/api/carrito'; 

  // 🔹 Subject para cantidad
  private cantidadSubject = new BehaviorSubject<number>(0);
  cantidad$ = this.cantidadSubject.asObservable();

  constructor(private http: HttpClient) {}

  getCarrito(): Observable<any> {
    const cedula = localStorage.getItem('cedula');
    return this.http.get(`${this.baseUrl}/${cedula}`).pipe(
      tap((data: any) => {
        this.cantidadSubject.next(data.items?.length ?? 0);
      })
    );
  }
  
  crearCarrito(cedula: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/crear/${cedula}`, {});
  }

  agregarProducto(productoId: number): Observable<any> {
    const cedula = localStorage.getItem('cedula');
    return this.http.post(`${this.baseUrl}/agregar/${cedula}/${productoId}`, {}).pipe(
      tap(() => this.actualizarCantidad())
    );
  }

  eliminarProducto(itemId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/item/${itemId}`).pipe(
      tap(() => this.actualizarCantidad())
    );
  }

  vaciarCarrito(): Observable<any> {
    const cedula = localStorage.getItem('cedula');
    return this.http.delete(`${this.baseUrl}/vaciar/${cedula}`).pipe(
      tap(() => this.cantidadSubject.next(0))
    );
  }

  // 🔹 Método para refrescar cantidad
  private actualizarCantidad() {
    this.getCarrito().subscribe();
  }

  
resetCantidad() {
  this.cantidadSubject.next(0);
}

}
