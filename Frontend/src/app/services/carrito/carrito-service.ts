import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CarritoService {
  private baseUrl = 'http://localhost:8081/api/carrito'; 

  constructor(private http: HttpClient) {}

  getCarrito(): Observable<any> {
    const cedula = localStorage.getItem('cedula');
    return this.http.get(`${this.baseUrl}/${cedula}`);
  }
  
  crearCarrito(cedula: String): Observable<any> {
    return this.http.post(`${this.baseUrl}/crear/${cedula}`, {});
  }

  agregarProducto(productoId: number): Observable<any> {
    const cedula = localStorage.getItem('cedula');
    return this.http.post(`${this.baseUrl}/agregar/${cedula}/${productoId}`, {});
  }

  eliminarProducto(itemId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/item/${itemId}`);
  }

  vaciarCarrito(): Observable<any> {
    const cedula = localStorage.getItem('cedula');
    return this.http.delete(`${this.baseUrl}/vaciar/${cedula}`);
  }

  crearPago(carrito: any) {
    console.log(carrito);
    return this.http.post<any>('http://localhost:8081/api/pago/crear', carrito);
  }

  comprar(carrito: any) {
    const cedula = localStorage.getItem('cedula');
    return this.http.post<any>(`${this.baseUrl}/comprar/${cedula}`, null);
  }
  
}
