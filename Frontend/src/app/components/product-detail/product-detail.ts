import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductosService } from '../../services/producto.service';
import { Producto } from '../../models/producto.model';
import { AuthService } from '../../services/auth.service';
import { CarritoService } from '../../services/carrito/carrito-service';
import { Imagen } from '../../models/imagen.model';
import Swal from 'sweetalert2';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-detail.html',
  styleUrl: './product-detail.css'
})
export class ProductDetail implements OnInit {

  producto: Producto | null = null;
  imagenSeleccionada: string = '';
  loading = true;

  // Modal personalizado
  mostrarModalReporte: boolean = false;
  mensajeReporte: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productoService: ProductosService,
    private auth: AuthService,
    private carritoService: CarritoService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      if (id) this.cargarProducto(id);
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
        this.router.navigate(['/home']);
      }
    });
  }

  seleccionarImagen(img: Imagen) {
    this.imagenSeleccionada = img.secureUrl;
  }

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
      }).then(result => {
        if (result.isConfirmed) {
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
          this.mostrarModalReporte = false; // 1️⃣ Cerrar modal primero
        
          Promise.resolve().then(() => {     // 2️⃣ Esperar a que Angular actualice el DOM
            Swal.fire({
              title: 'Éxito',
              text: 'El reporte fue enviado',
              icon: 'success'
            });
          });
        }
      });
    }
  }

  enviarReporte() {
    console.log("➡ Enviando reporte...");
  
    if (!this.producto?.id) {
      console.log("❌ No hay ID de producto");
      return;
    }
  
    if (!this.mensajeReporte.trim()) {
      Swal.fire('Advertencia', 'Debes escribir un mensaje', 'warning');
      return;
    }
  
    const payload = {
      productId: this.producto.id,
      reporterMessage: this.mensajeReporte
    };
  
    this.http.post('http://localhost:8081/api/notificacion-reporte', payload)
      .subscribe({
        next: (res) => {
  
          this.mostrarModalReporte = false;
          this.mensajeReporte = '';
  
          setTimeout(() => {
            Swal.fire('Éxito', 'El reporte fue enviado', 'success');
          }, 100);
        },
        error: (err) => {
  
          setTimeout(() => {
            Swal.fire('Error', 'No se pudo enviar', 'error');
          }, 100);
        }
      });
  }
  
}

