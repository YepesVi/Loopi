import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductosService } from '../../services/producto.service';
import { Producto } from '../../models/producto.model';
import { ProductCard } from '../product-card/product-card'; // Reutilizamos tu card
import { Page } from '../../models/page.model';
import { CategoryService } from '../../services/categorias/category.service';


@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, ProductCard],
  templateUrl: './product-list.html',
  styleUrl: './product-list.css'
})
export class ProductList implements OnInit{

  productos: Producto[] = [];
  loading = false;
  
  // Filtros actuales para mostrar en la UI
  filtroTexto: string | null = null;
  filtroCategoriaId: number | null = null;
  nombreCategoria: string | null = null; // Opcional: si quieres mostrar el nombre

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productosService: ProductosService,
    private categoriaService: CategoryService
  ) {}

  ngOnInit(): void {
    // 🌟 CLAVE: Suscribirse a los queryParams. 
    // Esto se dispara cada vez que la URL cambia (?q=... o ?categoriaId=...)
    this.route.queryParams.subscribe(params => {
      this.filtroTexto = params['q'] || null;
      this.filtroCategoriaId = params['categoriaId'] ? Number(params['categoriaId']) : null;
      if (this.filtroCategoriaId) {
        this.categoriaService.getCategoriaPorId(this.filtroCategoriaId).subscribe({
          next: (cat) => { 
            this.nombreCategoria = cat.nombre;
          },
          error: () => { this.nombreCategoria = null; }
        });
      } else {
        this.nombreCategoria = '';
      }
      this.cargarProductos();
    });
  }

  cargarProductos() {
    this.loading = true;
    
    // Llamar a tu servicio existente buscarProductos
    this.productosService.buscarProductos(
      this.filtroTexto,      // título
      this.filtroCategoriaId, // categoriaId
      'publicado',           // estado (fijo para clientes)
      null                   // propietarioId
    ).subscribe({
      next: (page: Page<Producto>) => {
        this.productos = page.content;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error buscando productos', err);
        this.loading = false;
      }
    });
  }

  // --- Métodos para limpiar filtros desde la UI ---

  limpiarBusqueda() {
    // Navegar a la misma ruta pero sin el param 'q'
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { q: null },
      queryParamsHandling: 'merge', // Mantiene otros filtros (ej. categoría)
    });
  }

  limpiarCategoria() {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { categoriaId: null },
      queryParamsHandling: 'merge',
    });
  }
  
  limpiarTodo() {
      this.router.navigate(['/productos']);
  }

}
