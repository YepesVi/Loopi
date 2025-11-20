// src/app/services/category.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Categoria, CrudCategoria } from '../../models/category.model';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  
  // URL base para todos los métodos del servicio
  private baseUrl = 'http://localhost:8081/api/categorias'; 

  constructor(private http: HttpClient) { }


  getCategoriesTree(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${this.baseUrl}/roots`);
  }

  getCategoriaPorId(id: number): Observable<Categoria> {
    return this.http.get<Categoria>(`${this.baseUrl}/${id}`);
  }

  createCategory(category: CrudCategoria): Observable<Categoria> {
    return this.http.post<Categoria>(this.baseUrl, category);
  }

 
  updateCategory(category: CrudCategoria): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.baseUrl}/${category.id}/name`, category);
  }


  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  
}