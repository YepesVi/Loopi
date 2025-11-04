// src/app/views/category-list/category-list.component.ts

import { Component, OnInit } from '@angular/core';
import { Categoria } from '../../../models/category.model';
import { CategoryService } from '../../../services/category.service';
import { CommonModule } from '@angular/common';
import { CategoryTreeItem } from '../category-tree-item/category-tree-item';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [CommonModule,CategoryTreeItem],
  templateUrl: './category-list.html', // Usaremos un HTML externo
  styleUrls: ['./category-list.css']
})
export class CategoryList implements OnInit {
  // Este array almacenará solo las categorías raíz (el primer nivel)
  categoryTree: Categoria[] = []; 
  isLoading: boolean = true;
  error: string | null = null;

  constructor(private categoryService: CategoryService) {}

  ngOnInit() {
    this.categoryService.getCategoriesTree().subscribe({
        next: (tree) => {
            this.categoryTree = tree;
            this.isLoading = false;
        },
        error: (err) => {
            console.error('Error al cargar las categorías:', err);
            this.error = 'No se pudieron cargar las categorías desde el servidor.';
            this.isLoading = false;
        }
    });
  }
}