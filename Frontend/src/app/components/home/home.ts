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

  ngOnInit(): void {
    this.productosService.getProductos().subscribe((productos: Producto[]) => {
      this.agruparPorCategoria(productos);
    });
  }

  agruparPorCategoria(productos: Producto[]): void {
    this.productosPorCategoria = productos.reduce((grupo: Record<string, Producto[]>, producto: Producto) => {
      const categoria = producto.categoria || 'Otros';
      if (!grupo[categoria]) grupo[categoria] = [];
      grupo[categoria].push(producto);
      return grupo;
    }, {});

    this.categorias = Object.entries(this.productosPorCategoria).map(
      ([key, value]) => ({ key, value })
    );
  }

  dividirEnGrupos(arr: Producto[], tamano: number): Producto[][] {
    const grupos: Producto[][] = [];
    for (let i = 0; i < arr.length; i += tamano) {
      grupos.push(arr.slice(i, i + tamano));
    }
    return grupos;
  }

  scrollLeft(id: string): void {
    const el = document.getElementById(id);
    if (el) el.scrollBy({ left: -300, behavior: 'smooth' });
  }

  scrollRight(id: string): void {
    const el = document.getElementById(id);
    if (el) el.scrollBy({ left: 300, behavior: 'smooth' });
  }
}
