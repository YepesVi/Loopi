import { Component } from '@angular/core';
import { CommonModule, DecimalPipe, NgFor, KeyValuePipe } from '@angular/common';
import { productos } from './data';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [NgFor, KeyValuePipe, DecimalPipe, CommonModule],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class Home {
   // Tipado explícito: cada categoría es un array de productos
   productosPorCategoria: Record<string, any[]> = {};

   // Arreglo que usa *ngFor en el template
   categorias: { key: string; value: any[] }[] = [];
 
   constructor() {
     this.agruparPorCategoria();
   }
 
   agruparPorCategoria() {
     // rellenamos el objeto agrupado
     this.productosPorCategoria = productos.reduce((grupo: Record<string, any[]>, producto: any) => {
       const categoria = producto.categoria || 'Otros';
       if (!grupo[categoria]) grupo[categoria] = [];
       grupo[categoria].push(producto);
       return grupo;
     }, {});
 
     // Convertimos a arreglo con tipos claros y hacemos un "cast" seguro del value
     this.categorias = Object.entries(this.productosPorCategoria || {}).map(
       ([key, value]) => ({ key, value: value as any[] })
     );
   }
 
   // funciones de scroll (si las usas)
   scrollLeft(id: string) {
     const el = document.getElementById(id);
     if (el) el.scrollBy({ left: -300, behavior: 'smooth' });
   }
 
   scrollRight(id: string) {
     const el = document.getElementById(id);
     if (el) el.scrollBy({ left: 300, behavior: 'smooth' });
   }

   dividirEnGrupos(arr: any[], tamano: number): any[][] {
    const grupos = [];
    for (let i = 0; i < arr.length; i += tamano) {
      grupos.push(arr.slice(i, i + tamano));
    }
    return grupos;
  }
  
}