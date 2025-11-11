import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { Router } from '@angular/router';
import { ProductosService, } from '../../services/producto.service';
import { Producto } from '../../models/producto.model';
import { AuthService } from '../../services/auth.service';

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
    private auth: AuthService,
    private router: Router
  ) {}

  // ✅ Getter reactivo para estado de sesión
  get isLoggedIn(): boolean {
    return this.auth.loggedInSignal();
  }

  ngOnInit(): void {
    this.cargarProductosPublicados();
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


comprarProducto(id: number) {
  this.productosService.actualizarEstado(id, 'Vendido').subscribe({
    next: () => {
      // Refresca el listado o recarga datos, muestra mensaje de éxito, etc.
      this.cargarProductosPublicados();
      alert('¡Producto comprado!');
    },
    error: () => {
      alert('No se pudo completar la compra.');
    }
  });
}
}

