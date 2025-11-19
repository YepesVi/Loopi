import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { Router } from '@angular/router';
import { ProductosService, } from '../../services/producto.service';
import { Producto } from '../../models/producto.model';
import { AuthService } from '../../services/auth.service';
import { CarritoService } from '../../services/carrito/carrito-service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, DecimalPipe],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class Home implements OnInit {
  productosPorCategoria: Record<string, Producto[]> = {};
  categorias: { key: string; value: Producto[] }[] = [];

  mostrarAdvertencia = false;

  constructor(
    private productosService: ProductosService,
    private carritoService: CarritoService,
    private auth: AuthService,
    private router: Router
  ) {}

  // ✅ Getter reactivo para estado de sesión
  get isLoggedIn(): boolean {
    return this.auth.loggedInSignal();
  }

  ngOnInit(): void {
    this.cargarProductosPublicados();
    this.verificarProductoPendiente();
  }

  cargarProductosPublicados(): void {
    this.productosService.getProductosPublicados().subscribe({
      next: (productos: Producto[]) => {
        this.agruparPorCategoria(productos);
      },
      error: (err) => console.error('Error al cargar productos', err)
    });
  }

  agruparPorCategoria(productos: Producto[]): void {
    this.productosPorCategoria = productos.reduce((grupo, producto) => {
      
      // 🌟 CAMBIO CRÍTICO: Acceder a 'producto.categoria.nombre'
      const categoriaNombre = (producto.categoria && producto.categoria.nombre) 
                              ? producto.categoria.nombre 
                              : 'Otros';
      
      if (!grupo[categoriaNombre]) {
        grupo[categoriaNombre] = [];
      }
      grupo[categoriaNombre].push(producto);
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

  scrollLeft(elementId: string): void {
    const el = document.getElementById(elementId);
    if (el) el.scrollBy({ left: -200, behavior: 'smooth' });
  }

  scrollRight(elementId: string): void {
    const el = document.getElementById(elementId);
    if (el) el.scrollBy({ left: 200, behavior: 'smooth' });
  }

  seleccionarProducto(producto: Producto): void {
    if (!this.isLoggedIn) {
      this.mostrarAdvertencia = true;
      return;
    }

    console.log('Producto seleccionado:', producto);
  }

  cerrarAdvertencia(): void {
    this.mostrarAdvertencia = false;
  }

  irALogin(): void {
    this.router.navigateByUrl('/login-register');
  }

  comprarProducto(id: number, event: Event) {
    event.stopPropagation();

    if (typeof window === 'undefined') return;
  
    const usuarioCedula = localStorage.getItem('cedula');
  
    if (!usuarioCedula) {
      localStorage.setItem('productoPendiente', id.toString());
  
      this.mostrarAdvertencia = true;
      return;
    }

    this.agregarProductoAlCarrito(id);
  }

  agregarProductoAlCarrito(productoId: number) {

    if (typeof window === 'undefined') return;
  
    this.carritoService.agregarProducto(productoId).subscribe({
      next: () => {
        Swal.fire({
          icon: 'success',
          title: 'Producto agregado',
          text: '✔ El producto fue agregado al carrito',
          confirmButtonColor: '#6a5af9'
        });
      },
      error: () => {
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: '❌ No se pudo agregar al carrito',
          confirmButtonColor: '#d33'
        });
      }
    });
  }  
  
  verificarProductoPendiente() {

    if (typeof window === 'undefined') return;

    const pendiente = localStorage.getItem('productoPendiente');
    const cedula = localStorage.getItem('cedula');
  
    if (pendiente && cedula) {
      const idProducto = parseInt(pendiente);
      this.agregarProductoAlCarrito(idProducto);
  
      localStorage.removeItem('productoPendiente');
    }
  }
  
}

