
import { Component, Input } from '@angular/core';
import { Categoria } from '../../../models/category.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-category-tree-item', // <--- Se llamará a sí mismo
  standalone: true,
  imports: [CommonModule,],
  templateUrl: './category-tree-item.html',
  styleUrls: ['./category-tree-item.css']
})
export class CategoryTreeItem {
  // Recibe la lista de categorías (o subcategorías) a renderizar
  @Input() categories: Categoria[] = []; 
  
  // Nivel de profundidad para manejar la indentación
  @Input() level: number = 0; 

  // Calcula el margen izquierdo basado en la profundidad (por ejemplo, 20px por nivel)
  getIndentationStyle() {
    return { 
      'margin-left': (this.level * 20) + 'px' 
    };
  }
}