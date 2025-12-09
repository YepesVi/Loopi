import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class HistorialCompraService {
  private baseUrl = 'https://loopi-back.onrender.com/api/historial-compra';

  constructor(private http: HttpClient) {}

  obtenerHistorial(cedula: string) {
    return this.http.get<any[]>(`${this.baseUrl}/${cedula}`);
  }
}
