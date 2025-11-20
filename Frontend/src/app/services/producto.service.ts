import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Producto } from '../models/producto.model';
import { Page } from '../models/page.model';

@Injectable({
  providedIn: 'root'
})
export class ProductosService {
  private apiUrl = 'http://localhost:8081/api/productos';

  constructor(private http: HttpClient) {}

  getProductos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.apiUrl);
  }
  
  getProductoPorId(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.apiUrl}/${id}`);
  }

  getProductosPublicados(): Observable<Producto[]> {
  return this.http.get<Producto[]>(`${this.apiUrl}/publicados`);
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

actualizarEstado(id: number, estado: string): Observable<any> {
    return this.http.put(
      `${this.apiUrl}/${id}/estado`, 
      JSON.stringify(estado), // Enviar el string "Vendido" como body JSON
      { 
        headers: { 'Content-Type': 'application/json' },
        responseType: 'json' 
      }
    );
  }

buscarProductos(
    titulo: string | null,
    categoriaId: number | null, 
    estado: string | null,
    propietarioId: number | null,
  ): Observable<Page<Producto>> { // 👈 Devuelve un Page
    
    let params = new HttpParams();
    if (titulo && titulo.trim() !== '') {
      params = params.append('titulo', titulo);
    }
    if (categoriaId) {
      params = params.append('categoriaId', categoriaId.toString());
    }
    if (estado && estado.trim() !== '') {
      params = params.append('estado', estado);
    }
    if (propietarioId) {
      params = params.append('propietarioId', propietarioId.toString());
    }
    // Añadir paginación si es necesario
    params = params.append('page', '0');
    params = params.append('size', '50'); // Cargar 50 por defecto

    return this.http.get<Page<Producto>>(`${this.apiUrl}/buscar`, { params });
  }

}
