
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Categoria, CrudCategoria } from '../../../models/category.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoryService } from '../../../services/categorias/category.service';
import { Router } from '@angular/router';
import { PopupService } from '../../../services/categorias/popup';

@Component({
  selector: 'app-category-tree-item', // <--- Se llamará a sí mismo
  standalone: true,
  imports: [CommonModule, FormsModule, CategoryTreeItem],
  templateUrl: './category-tree-item.html',
  styleUrls: ['./category-tree-item.css']
})
export class CategoryTreeItem {
  
  @Input() categories: Categoria[] = []; 
  @Input() level: number = 0; 
  @Input() editingMode: boolean = false;

  @Output() addChild = new EventEmitter<{ parentId: number, name: string }>();
  @Output() editCategory = new EventEmitter<Categoria>(); // Emite el objeto a editar
  @Output() deleteCategory = new EventEmitter<number>(); // Emite el ID a eliminar

  @Input() activeEditId: number | null = null; 
  @Input() activeAddChildId: number | null = null; 
  
  @Output() requestEditFocus = new EventEmitter<number>();
  @Output() requestAddChildFocus = new EventEmitter<number>();
  @Output() actionComplete = new EventEmitter<void>();

  newCategoryName: string = ''; // Nombre temporal
  newRootCategoryName: string = '';
  expandedCategories: Set<number> = new Set<number>();

  constructor(private categoryService: CategoryService, private router: Router, private popupService: PopupService) {}
  
  filtrarPorCategoria(id: number): void {
    this.router.navigate(['/productos'], { 
      queryParams: { categoriaId: id }
      // queryParamsHandling: 'merge' // Si quieres mantener el texto de búsqueda
    });
    this.popupService.closeCategoryPopup(); // Cerrar el popup al seleccionar
  }

  // Calcula el margen izquierdo basado en la profundidad (por ejemplo, 20px por nivel)
  getIndentationStyle() {
    return { 
      'margin-left': (this.level * 25) + 'px' 
    };
  }

  toggleExpansion(categoryId: number): void {
    if (this.expandedCategories.has(categoryId)) {
      this.expandedCategories.delete(categoryId);
    } else {
      this.expandedCategories.add(categoryId);
    }
  }

  isExpanded(categoryId: number): boolean {
    return this.expandedCategories.has(categoryId);
  }

  toggleAddChildForm(id: number): void {
    if (this.activeAddChildId === id) {
          this.requestAddChildFocus.emit(undefined);
      } else {
          this.requestAddChildFocus.emit(id);
      }
    this.newCategoryName = ''; // Resetear el nombre al abrir/cerrar
  }

  emitAddChild(parentId: number): void {
    if (this.newCategoryName.trim()) {
      this.addChild.emit({ parentId, name: this.newCategoryName.trim() });
      this.newCategoryName = '';
    }
  }

  toggleEditName(category: Categoria): void {
   if (this.activeEditId === category.id) {
        this.saveEditedName(category); 
        
    } else {
        this.newCategoryName = category.nombre; 
        this.requestEditFocus.emit(category.id); 
    }
  }

  toggleAddRootForm(): void {
    // Si estamos en modo adición de raíz, lo limpiamos; si no, lo activamos.
    if (this.activeAddChildId === 0) {
      this.requestAddChildFocus.emit(undefined);
    } else {
      // Usamos el ID especial 0 (ROOT_ADD_ID)
      this.newRootCategoryName = '';
      this.requestAddChildFocus.emit(0);
    }
  }

  emitAddRoot(): void {
    if (!this.newRootCategoryName.trim()) {
      alert('El nombre no puede estar vacío.');
      return;
    }
    this.addChild.emit({ parentId: 0, name: this.newRootCategoryName.trim() }); 
    
    this.newRootCategoryName = '';
    this.requestAddChildFocus.emit(undefined); 
    this.actionComplete.emit(); 
  }

  saveEditedName(category: Categoria): void {
    if (this.newCategoryName.trim() === category.nombre) {
      this.requestEditFocus.emit(undefined);
        return;
    }
    
    // Llamada directa al servicio (podrías emitir también si quieres que el padre lo maneje)
    const updatedCategory: CrudCategoria = {
        id: category.id,
        nombre: this.newCategoryName.trim(),
    };

    this.categoryService.updateCategory(updatedCategory).subscribe({
        next: (res) => {
            category.nombre = res.nombre; // Actualizar localmente el nombre
            this.requestEditFocus.emit(undefined); // Limpiar el foco global
            this.actionComplete.emit();
        },
        error: (err) => {
            alert('Error al actualizar: ' + err.message);
            this.requestEditFocus.emit(undefined); // Limpiar el foco global
        }
    });
  }

  emitDeleteCategory(id: number): void {
      this.deleteCategory.emit(id);
  }

  onChildAdded(event: any): void {
      this.addChild.emit(event); // Re-emite al padre
  }
  onChildDeleted(id: number): void {
      this.deleteCategory.emit(id); // Re-emite al padre
  }

  onChildRequestEditFocus(id: number): void {
      this.requestEditFocus.emit(id);
  }
  onChildRequestAddChildFocus(id: number): void {
      this.requestAddChildFocus.emit(id);
  }
  onChildActionComplete(): void {
      this.actionComplete.emit();
  }
}