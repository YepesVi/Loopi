import { Component } from '@angular/core';
import { HistorialCompraService } from '../../services/historialCompra/historial-compra-service';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-historial-compra',
  imports: [RouterModule, CommonModule],
  templateUrl: './historial-compra.html',
  styleUrl: './historial-compra.css'
})
export class HistorialCompra {
  historial: any[] = [];
  cargando = true;
  total: number = 0;

  constructor(private historialService: HistorialCompraService) {}

  ngOnInit(): void {
    const cedula = localStorage.getItem('cedula');
    if (cedula) {
      this.historialService.obtenerHistorial(cedula).subscribe({
        next: (data) => {
          this.historial = data;
          this.cargando = false;
        },
        error: (err) => {
          console.error('Error obteniendo historial', err);
          this.cargando = false;
        }
      });
    }
  }

  trackById(index: number, compra: any) {
    return compra.id;
  }

  calcularTotal(compra: any): string {
    const total = compra.productos.reduce(
      (sum: number, p: any) => sum + (p.precio ?? 0),
      0
    );
  
    return total.toLocaleString('es-CO');
  }  

}
