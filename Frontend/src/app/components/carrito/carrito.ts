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
}
