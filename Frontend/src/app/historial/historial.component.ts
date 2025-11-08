import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';


@Component({
    standalone: true,
  imports: [CommonModule],
  selector: 'app-historial',
  templateUrl: './historial.component.html',
  styleUrls: ['./historial.component.css']
})
export class HistorialProductoComponent implements OnInit {

  productoId!: number;
  historial: any[] = [];
  cargando: boolean = true;
  sinDatos: boolean = false;

  constructor(private route: ActivatedRoute, private http: HttpClient) {}

  ngOnInit(): void {
this.productoId = Number(this.route.snapshot.paramMap.get('id'));
    this.obtenerHistorial();
  }

  obtenerHistorial() {
// Ahora (correcto)
this.http.get<any[]>(`http://localhost:8081/api/historiales/producto/${this.productoId}`)
      .subscribe({
        next: (data) => {
          this.historial = data;
          this.sinDatos = data.length === 0;
          this.cargando = false;
        },
        error: (err) => {
          console.error('Error al cargar historial:', err);
          this.cargando = false;
          this.sinDatos = true;
        }
      });
  }

  registrarEventoHistorial(productoId: number, estado: string, accion: string, usuario: string) {
  return this.http.post(
    'http://localhost:8081/api/historiales',
    null,
    { params: { productoId, estado, accion, usuario } }
  );
}


}
