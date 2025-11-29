
import { Component, ElementRef, Input, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Producto } from '../../models/producto.model';
import { AuthService } from '../../services/auth.service';
import { CarritoService } from '../../services/carrito/carrito-service';
import Swal from 'sweetalert2'; 

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-card.html',
  styleUrl: './product-card.css'
})
export class ProductCard {

  @Input() producto!: Producto; 

  @ViewChild('scrollContainer', { static: false }) scrollContainer!: ElementRef;

  constructor(
    private auth: AuthService,
    private carritoService: CarritoService,
    private router: Router 
  ) {}

  get isLoggedIn(): boolean {
    return this.auth.loggedInSignal();
  }

  // --- 1. Lógica de Navegación (Click en la tarjeta) ---
  irAlDetalle(): void {
    if (this.producto.id) {
      // Navega a la página de detalle del producto
      this.router.navigate(['/producto', this.producto.id]);
    }
  }

  // --- 2. Lógica de Compra Autónoma ---
  comprarProducto(event: Event): void {
    event.stopPropagation();

    if (typeof window === 'undefined') return;

    if (!this.isLoggedIn) {
      this.manejarLoginRequerido();
      return;
    }

    if (this.producto.id) {
      this.agregarProductoAlCarrito(this.producto.id);
    }
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

  // --- Métodos Privados de Ayuda ---


  private manejarLoginRequerido() {

    if (this.producto.id) {
      localStorage.setItem('productoPendiente', this.producto.id.toString());
    }

    Swal.fire({
      title: '🔒 Acceso requerido',
      text: 'Debes iniciar sesión para agregar productos al carrito.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#6f42c1',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Iniciar Sesión',
      cancelButtonText: 'Cancelar',
      background: '#fff',
      color: '#333'
    }).then((result) => {
      if (result.isConfirmed) {
        this.router.navigate(['/login-register']); // Redirigir al login
      }
    });
  }

  private agregarProductoAlCarrito(productoId: number) {
    this.carritoService.agregarProducto(productoId).subscribe({
      next: () => {
        const Toast = Swal.mixin({
          toast: true,
          position: 'top-end',
          showConfirmButton: false,
          timer: 3000,
          timerProgressBar: true,
        });
        
        Toast.fire({
          icon: 'success',
          title: 'Producto agregado al carrito'
        });
      },
      error: () => {
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudo agregar al carrito',
        });
      }
    });
  }

  // --- Lógica Visual (Scroll de imágenes) ---
 scrollLeft(event: Event) {
    event.stopPropagation();
    this.smoothScroll(-200);
  }

  scrollRight(event: Event) {
    event.stopPropagation();
    this.smoothScroll(200);
  }

  private smoothScroll(amount: number) {
    const container = this.scrollContainer?.nativeElement;
    if (container) {
      container.scrollBy({ left: amount, behavior: 'smooth' });
    }
  }

}
