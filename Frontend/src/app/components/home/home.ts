import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe, NgFor, KeyValuePipe } from '@angular/common';
import { ProductosService, Producto } from '../../services/productos';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [NgFor, KeyValuePipe, DecimalPipe, CommonModule],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class Home implements OnInit {
  productosPorCategoria: Record<string, Producto[]> = {};
  categorias: { key: string; value: Producto[] }[] = [];

  constructor(private productosService: ProductosService) {}

  ngOnInit() {
    this.cargarProductosPublicados();
  }

  cargarProductosPublicados() {
    this.productosService.getProductosPublicados().subscribe({
      next: (productos: Producto[]) => {
        // Filtrar solo los productos publicados
        const publicados = productos.filter(p => p.estado?.toLowerCase() === 'publicado');
        this.agruparPorCategoria(publicados);
      },
      error: (err) => console.error('Error al cargar productos', err)
    });
  }

  agruparPorCategoria(productos: Producto[]) {
    this.productosPorCategoria = productos.reduce((grupo, producto) => {
      const categoria = producto.categoria || 'Otros';
      if (!grupo[categoria]) grupo[categoria] = [];
      grupo[categoria].push(producto);
      return grupo;
    }, {} as Record<string, Producto[]>);

    
    this.categorias = Object.entries(this.productosPorCategoria).map(
      ([key, value]) => ({ key, value })
    );
  }

  dividirEnGrupos<T>(array: T[], tamaño: number): T[][] {
    const grupos: T[][] = [];
    for (let i = 0; i < array.length; i += tamaño) {
      grupos.push(array.slice(i, i + tamaño));
    }
    return grupos;
  }

 scrollLeft(elementId: string) {
  const el = document.getElementById(elementId);
  if (el) el?.scrollBy({ left: -200, behavior: 'smooth' });
  
}

scrollRight(elementId: string) {
  const el = document.getElementById(elementId);
  if (el) el?.scrollBy({ left: 200, behavior: 'smooth' });
   
}


}