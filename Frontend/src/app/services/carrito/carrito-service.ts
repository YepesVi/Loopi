import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CarritoService {
  private baseUrl = 'http://localhost:8080/api/carrito'; 
  // Ajusta la URL según tu backend

  constructor(private http: HttpClient) {}

  /**
   * Obtiene el carrito del usuario
   */
  getCarrito(): Observable<any> {
    return this.http.get(`${this.baseUrl}/mi-carrito`);
  }

  /**
   * Agrega un producto al carrito del usuario
   */
  agregarProducto(productoId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/agregar/${productoId}`, {});
  }

  /**
   * Disminuye la cantidad de un producto del carrito
   */
  disminuirProducto(productoId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/disminuir/${productoId}`, {});
  }

  /**
   * Elimina un producto del carrito completamente
   */
  eliminarProducto(productoId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/eliminar/${productoId}`);
  }

  /**
   * Vaciar carrito por completo
   */
  vaciarCarrito(): Observable<any> {
    return this.http.delete(`${this.baseUrl}/vaciar`);
  }
}
