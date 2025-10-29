import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Producto {
  id?: number;
  titulo: string;
  descripcion: string;
  categoria: string;
  precio: number;
  estado: string;
  propietarioId: number;
  fotos?: string;
  fechaCreacion?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductosService {
  private apiUrl = 'http://localhost:8081/api/productos';

  constructor(private http: HttpClient) {}

  getProductos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.apiUrl);
  }

  crearProductoConImagen(formData: FormData): Observable<Producto> {
    return this.http.post<Producto>(`${this.apiUrl}/crear-con-imagen`, formData);
  }

  
  actualizarProducto(id: number, formData: FormData): Observable<Producto> {
    return this.http.put<Producto>(`${this.apiUrl}/actualizar/${id}`, formData);
  }


  eliminarProducto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/eliminar/${id}`);
  }

  getHistorialPorUsuario(propietarioId: number, estado?: string): Observable<Producto[]> {
  let url = `${this.apiUrl}/usuario/${propietarioId}/historial`;
  if (estado) {
    url += `?estado=${estado}`;
  }
  return this.http.get<Producto[]>(url);
}

}
