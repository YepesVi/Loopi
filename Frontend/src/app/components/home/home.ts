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
    this.cargarProductos();
  }

  cargarProductos() {
    this.productosService.getProductos().subscribe({
      next: (productos) => {
        this.agruparPorCategoria(productos);
      },
      error: (error) => {
        console.error('Error al cargar productos:', error);
      }
    });
  }

  agruparPorCategoria(productos: Producto[]) {
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
}
