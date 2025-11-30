import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class CarritoService {
  private baseUrl = 'http://localhost:8081/api/carrito'; 

  private cantidadSubject = new BehaviorSubject<number>(0);
  cantidad$ = this.cantidadSubject.asObservable();

  constructor(private http: HttpClient) {}

  // ✅ Método seguro para obtener cedula
  private getCedula(): string | null {
    return (typeof window !== 'undefined' && window.localStorage)
      ? localStorage.getItem('cedula')
      : null;
  }

  getCarrito(): Observable<any> {
    const cedula = this.getCedula();
    if (!cedula) {
      // Si no hay cedula, devolvemos un observable vacío
      this.cantidadSubject.next(0);
      return new Observable(observer => {
        observer.next({ items: [] });
        observer.complete();
      });
    }

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
    const cedula = this.getCedula();
    if (!cedula) return new Observable(observer => observer.complete());

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
    const cedula = this.getCedula();
    if (!cedula) return new Observable(observer => observer.complete());

    return this.http.delete(`${this.baseUrl}/vaciar/${cedula}`).pipe(
      tap(() => this.cantidadSubject.next(0))
    );
  }

  resetCantidad() {
    this.cantidadSubject.next(0);
  }

  // 🔹 Método para refrescar cantidad
  private actualizarCantidad() {
    this.getCarrito().subscribe();
  }

  crearPago(carrito: any) {
    console.log(carrito);
    return this.http.post<any>('http://localhost:8081/api/pago/crear', carrito);
  }

  comprar(carrito: any) {
    const cedula = this.getCedula();
    if (!cedula) return new Observable(observer => observer.complete());

    return this.http.post<any>(`${this.baseUrl}/comprar/${cedula}`, null);
  }
}
