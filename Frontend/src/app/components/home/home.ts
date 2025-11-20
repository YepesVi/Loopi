// src/app/components/home/home.ts

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { ProductosService } from '../../services/producto.service';
import { CategoryService } from '../../services/categorias/category.service'; // 👈 Servicio de categorías
import { AuthService } from '../../services/auth.service';
import { CarritoService } from '../../services/carrito/carrito-service';
import { Producto } from '../../models/producto.model';
import { Categoria } from '../../models/category.model';
import { ProductCard } from '../product-card/product-card'; // 👈 Tu card reutilizable
import Swal from 'sweetalert2';

// Interfaz auxiliar para organizar la vista
interface SeccionCategoria {
  categoria: Categoria;
  productos: Producto[];
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, ProductCard],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class Home implements OnInit {
  
  secciones: SeccionCategoria[] = []; // 👈 Aquí guardamos las categorías raíz y sus productos
  loading = true;

  constructor(
    private productosService: ProductosService,
    private categoryService: CategoryService,
    private carritoService: CarritoService,
    private auth: AuthService,
    private router: Router
  ) {}

  // ✅ Getter reactivo para estado de sesión
  get isLoggedIn(): boolean {
    return this.auth.loggedInSignal();
  }

  ngOnInit(): void {
    this.cargarDatosHome();
    this.verificarProductoPendiente(); // 👈 Funcionalidad preservada
  }

  /**
   * 1. Obtiene el árbol de categorías.
   * 2. Filtra solo las raíces.
   * 3. Busca productos para cada raíz.
   */
  cargarDatosHome() {
    this.loading = true;
    
    this.categoryService.getCategoriesTree().subscribe({
      next: (raices: Categoria[]) => {
        
        if (raices.length === 0) {
            this.loading = false;
            return;
        }

        let processedCount = 0;

        raices.forEach(cat => {
          // Llamamos al endpoint de búsqueda del backend (que ya busca descendientes)
          this.productosService.buscarProductos(
            null,       // titulo
            cat.id,     // categoriaId
            'publicado',// estado (Solo mostramos publicados en el home)
            null        // propietarioId
          ).subscribe(page => {
            
            // Solo creamos la sección si hay productos para mostrar
            if (page.content.length > 0) {
              this.secciones.push({
                categoria: cat,
                productos: page.content.slice(0, 12) // Limitamos a 12 para el carrusel
              });
            }

            processedCount++;
            // Cuando terminamos de procesar todas las raíces
            if (processedCount === raices.length) {
              // Ordenamos por ID para que las secciones no "bailen" al recargar
              this.secciones.sort((a, b) => a.categoria.id - b.categoria.id);
              this.loading = false;
            }
          });
        });
      },
      error: (err) => {
        console.error('Error cargando categorías', err);
        this.loading = false;
      }
    });
  }

  // 🔄 Helper para agrupar productos en el carrusel (4 por slide)
  dividirEnGrupos(array: Producto[], tamaño: number): Producto[][] {
    const grupos: Producto[][] = [];
    for (let i = 0; i < array.length; i += tamaño) {
      grupos.push(array.slice(i, i + tamaño));
    }
    return grupos;
  }

  // 🔗 Navegación a la lista filtrada
  verMas(categoriaId: number) {
    this.router.navigate(['/productos'], { queryParams: { categoriaId: categoriaId } });
  }

  // --- Lógica de Producto Pendiente (Preservada) ---

  verificarProductoPendiente() {
    if (typeof window === 'undefined') return;

    const pendiente = localStorage.getItem('productoPendiente');
    const cedula = localStorage.getItem('cedula'); // O userId, según tu auth
  
    // Si hay un producto pendiente y el usuario ya inició sesión
    if (pendiente && this.isLoggedIn) {
      const idProducto = parseInt(pendiente);
      this.agregarProductoAlCarrito(idProducto);
      localStorage.removeItem('productoPendiente');
    }
  }

  private agregarProductoAlCarrito(productoId: number) {
    this.carritoService.agregarProducto(productoId).subscribe({
      next: () => {
        const Toast = Swal.mixin({
            toast: true, position: 'top-end', showConfirmButton: false, timer: 3000, timerProgressBar: true
        });
        Toast.fire({ icon: 'success', title: 'Producto pendiente agregado al carrito' });
      },
      error: () => {
        // Manejo silencioso o alerta discreta
        console.error('No se pudo agregar el producto pendiente');
      }
    });
  }
}