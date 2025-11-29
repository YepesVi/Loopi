import { Component, OnInit } from '@angular/core';
import { CarritoService } from '../../services/carrito/carrito-service';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-carrito',
  imports: [RouterModule, CommonModule],
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

  trackById(index: number, item: any) {
    return item.id;
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
    const items = this.carrito || [];
  
    this.total = items.reduce(
      (sum: number, item: any) => sum + (item.producto?.precio ?? 0),
      0
    );
  }  

  eliminar(item: any) {
    this.carritoService.eliminarProducto(item).subscribe(() => {
  
      Swal.fire({
        title: 'Eliminado',
        text: 'Producto eliminado del carrito',
        icon: 'success',
        timer: 1200,
        showConfirmButton: false
      });
  
      this.cargarCarrito();
    });
  }

  vaciar() {
    this.carritoService.vaciarCarrito().subscribe(() => {
  
      Swal.fire({
        title: 'Carrito vaciado',
        text: 'Se han eliminado todos los productos',
        icon: 'success',
        timer: 1500,
        showConfirmButton: false
      });
  
      this.cargarCarrito();
    });
  }

  comprarPasarelaDePago() {

    const carritoDTO = {
      items: this.carrito.map((i: any) => ({
        productoId: i.producto.id,
        titulo: i.producto.titulo,
        precio: i.producto.precio,
      }))
    };
  
    this.carritoService.crearPago(carritoDTO).subscribe({
      next: (res) => {
        console.log("Respuesta completa MP:", res);
        if (res.initPoint) {
          window.location.href = res.sandboxInitPoint;
        } else {
          alert("No se recibió la URL de pago.");
        }
      },
      error: (err) => {
        console.error(err);
        alert("Error al generar el pago.");
      }
    });
  }

  comprar() {
    this.carritoService.comprar(this.carrito).subscribe({
      next: (resp) => {
        Swal.fire({
          icon: 'success',
          title: '¡Compra exitosa!',
          text: 'Tu compra fue procesada correctamente.',
          confirmButtonText: 'Aceptar'
        });
  
        this.carrito.productos = [];
  
        this.cargarCarrito();
      },
  
      error: (err) => {
        console.error('Error al comprar', err);
  
        Swal.fire({
          icon: 'error',
          title: 'Error en la compra',
          text: err.error?.message || 'Hubo un problema al procesar la compra.',
          confirmButtonText: 'Aceptar'
        });
      }
    });
  }
  
}
