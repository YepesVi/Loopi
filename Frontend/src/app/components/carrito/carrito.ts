import { Component, OnInit } from '@angular/core';
import { CarritoService } from '../../services/carrito/carrito-service';

@Component({
  selector: 'app-carrito',
  imports: [],
  templateUrl: './carrito.html',
  styleUrl: './carrito.css'
})
export class Carrito implements OnInit {
  carrito: any = [];
  total: number = 0;
  cargando: boolean = true;

  constructor(private carritoService: CarritoService) {}

  ngOnInit(): void {
    this.cargarCarrito();
  }

  cargarCarrito() {
    this.cargando = true;
    this.carritoService.getCarrito().subscribe(
      (data) => {
        this.carrito = data.items;
        this.calcularTotal();
        this.cargando = false;
      },
      (error) => {
        console.error('Error obteniendo carrito', error);
        this.cargando = false;
      }
    );
  }

  calcularTotal() {
    this.total = this.carrito.reduce((acc: number, item: any) => acc + item.subtotal, 0);
  }

  aumentar(item: any) {
    this.carritoService.agregarProducto(item.productoId).subscribe(() => {
      item.cantidad++;
      item.subtotal = item.cantidad * item.precio;
      this.calcularTotal();
    });
  }

  disminuir(item: any) {
    if (item.cantidad > 1) {
      this.carritoService.disminuirProducto(item.productoId).subscribe(() => {
        item.cantidad--;
        item.subtotal = item.cantidad * item.precio;
        this.calcularTotal();
      });
    }
  }

  eliminar(item: any) {
    this.carritoService.eliminarProducto(item.productoId).subscribe(() => {
      this.carrito = this.carrito.filter((x: any) => x.productoId !== item.productoId);
      this.calcularTotal();
    });
  }

  vaciar() {
    this.carritoService.vaciarCarrito().subscribe(() => {
      this.carrito = [];
      this.total = 0;
    });
  }
}
