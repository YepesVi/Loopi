// src/app/services/category.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Categoria } from '../models/category.model';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  
  // URL base para todos los métodos del servicio
  private baseUrl = 'http://localhost:8081/api/categorias'; 

  constructor(private http: HttpClient) { }


  getCategoriesTree(): Observable<Categoria[]> {
    console.log(this.http.get<Categoria[]>(`${this.baseUrl}/roots`));
    return this.http.get<Categoria[]>(`${this.baseUrl}/roots`);
  }

  
}