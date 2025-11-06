import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { Router } from '@angular/router';
import { ProductosService, Producto } from '../../services/productos';
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
        const publicados = productos.filter(p => p.estado?.toLowerCase() === 'publicado');
        this.agruparPorCategoria(publicados);
      },
      error: (err) => console.error('Error al cargar productos', err)
    });
  }

  agruparPorCategoria(productos: Producto[]): void {
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

