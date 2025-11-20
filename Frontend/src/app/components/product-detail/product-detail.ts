import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductosService } from '../../services/producto.service';
import { Producto } from '../../models/producto.model';
import { AuthService } from '../../services/auth.service';
import { CarritoService } from '../../services/carrito/carrito-service';
import { Imagen } from '../../models/imagen.model';
import Swal from 'sweetalert2';


@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, DecimalPipe],
  templateUrl: './product-detail.html',
  styleUrl: './product-detail.css'
})
export class ProductDetail implements OnInit{

producto: Producto | null = null;
  imagenSeleccionada: string = ''; // URL de la imagen principal actual
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productoService: ProductosService,
    private auth: AuthService,
    private carritoService: CarritoService
  ) {}

  ngOnInit(): void {
    // Obtener el ID de la URL
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      if (id) {
        this.cargarProducto(id);
      }
    });
  }

  redirigirAHome() {
    this.router.navigate(['/home']);
  }

  cargarProducto(id: number) {
    this.loading = true;
    this.productoService.getProductoPorId(id).subscribe({
      next: (prod) => {
        this.producto = prod;
        // Seleccionar la primera imagen por defecto si existe
        if (prod.imagenes && prod.imagenes.length > 0) {
          this.imagenSeleccionada = prod.imagenes[0].secureUrl;
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar producto', err);
        Swal.fire('Error', 'No se pudo cargar el producto', 'error');
        this.router.navigate(['/home']); // Volver si falla
      }
    });
  }

  // 🖼️ Cambiar la imagen principal al hacer click en una miniatura
  seleccionarImagen(img: Imagen) {
    this.imagenSeleccionada = img.secureUrl;
  }

  // 🛒 Lógica de Compra (Reutilizada y Adaptada)
  agregarAlCarrito() {
    if (!this.producto) return;

    if (!this.auth.loggedInSignal()) {
      Swal.fire({
        title: '🔒 Acceso requerido',
        text: 'Inicia sesión para comprar este producto.',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Ir al Login',
        cancelButtonText: 'Cancelar',
        confirmButtonColor: '#6f42c1'
      }).then((result) => {
        if (result.isConfirmed) {
          // Guardar intención y redirigir
          if (this.producto?.id) {
            localStorage.setItem('productoPendiente', this.producto.id.toString());
          }
          this.router.navigate(['/login-register']);
        }
      });
      return;
    }

    // Si está logueado:
    if (this.producto.id) {
      this.carritoService.agregarProducto(this.producto.id).subscribe({
        next: () => {
          Swal.fire({
            icon: 'success',
            title: '¡Agregado!',
            text: 'El producto está en tu carrito',
            confirmButtonColor: '#6a5af9',
            timer: 2000
          });
        },
        error: () => Swal.fire('Error', 'No se pudo agregar al carrito', 'error')
      });
    }
  }

}
