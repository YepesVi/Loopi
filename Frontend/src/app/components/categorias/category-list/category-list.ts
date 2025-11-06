import { Component, OnInit } from '@angular/core';
import { Categoria, CrudCategoria, ParentReference } from '../../../models/category.model';
import { CategoryService } from '../../../services/categorias/category.service';
import { CommonModule } from '@angular/common';
import { CategoryTreeItem } from '../category-tree-item/category-tree-item';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [CommonModule,CategoryTreeItem, FormsModule],
  templateUrl: './category-list.html', // Usaremos un HTML externo
  styleUrls: ['./category-list.css']
})
export class CategoryList implements OnInit {
  // Este array almacenará solo las categorías raíz (el primer nivel)
  categoryTree: Categoria[] = []; 
  isLoading: boolean = true;
  error: string | null = null;
  editingMode: boolean = false;

  readonly ROOT_ADD_ID: number = 0; 
  newRootCategoryName: string = '';
  categoryToEdit: CrudCategoria | null = null;

  activeEditId: number | null = null; 
  activeAddChildId: number | null = null;

  constructor(private categoryService: CategoryService) {}

  clearFocus(): void {
    this.activeEditId = null;
    this.activeAddChildId = null;
  }

  ngOnInit() {
    this.categoryService.getCategoriesTree().subscribe({
        next: (tree) => {
            this.categoryTree = tree;
            this.isLoading = false;
            this.clearFocus();
        },
        error: (err) => {
            console.error('Error al cargar las categorías:', err);
            this.error = 'No se pudieron cargar las categorías desde el servidor.';
            this.isLoading = false;
            this.clearFocus();
        }
    });
  }

  

  toggleEditingMode(): void {
    this.editingMode = !this.editingMode;
    this.categoryToEdit = null;
  }

  loadCategories() {
    this.categoryService.getCategoriesTree().subscribe({
        next: (tree) => {
            this.categoryTree = tree;
            this.isLoading = false;
            this.clearFocus();
        },
        error: (err) => {
            console.error('Error al cargar las categorías:', err);
            this.error = 'No se pudieron cargar las categorías desde el servidor.';
            this.isLoading = false;
            this.clearFocus();
        }
    });
  }

  

  onChildAdded(event: { parentId: number, name: string }): void {
    let newCategory: CrudCategoria;

    if (event.parentId === this.ROOT_ADD_ID) {
      newCategory = {
        id: null,
        nombre: event.name,
        parent: null // Raíz
      };
    } else {
      const parentRef: ParentReference = { id: event.parentId };
      newCategory = {
        id: null,
        nombre: event.name,
        parent: parentRef
      };
    }
 
    this.categoryService.createCategory(newCategory).subscribe({
      next: () => {
              this.clearFocus(); 
              this.loadCategories(); 
          }, 
      error: (err) => alert('Error al crear subcategoría.')
    });
  }

  onCategoryUpdated(category: Categoria): void {
      this.categoryToEdit = {
        id: category.id,
        nombre: category.nombre,
      };
      const updatedName = prompt('Editar nombre de la categoría:', category.nombre);
      if (updatedName !== null && updatedName.trim() !== '') {
        const updatedCategory: CrudCategoria = {
          id: category.id,
          nombre: updatedName.trim()
        };
        this.categoryService.updateCategory(updatedCategory).subscribe({
          next: () => this.loadCategories(), // Recargar después de éxito
          error: (err) => alert('Error al actualizar categoría.')
        });
      }
  }

  onCategoryDeleted(id: number): void {
    if (confirm('¿Estás seguro de que quieres eliminar esta categoría y todos sus hijos?')) {
      this.categoryService.deleteCategory(id).subscribe({
        next: () => this.loadCategories(), // Recargar después de éxito
        error: (err) => alert('Error al eliminar categoría.')
      });
    }
  }

  startEditing(id: number): void {
      this.clearFocus(); 
      this.activeEditId = id;
  }
  
  startAddingChild(id: number): void {
      this.clearFocus(); 
      this.activeAddChildId = id;
  }

  onActionComplete(): void {
      this.clearFocus();
      this.loadCategories();
  }

}