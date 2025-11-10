import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Location } from '@angular/common';

@Component({
  standalone: true,
  imports: [CommonModule],
  selector: 'app-historial-general',
  templateUrl: './historial-general.component.html',
  styleUrls: ['./historial-general.component.css']
})
export class HistorialGeneralComponent implements OnInit {

  usuarioId = 1; // TODO: remplazar por el obtenido del login
  historial: any[] = [];
  cargando = true;
  vacio = false;

  constructor(private http: HttpClient, private location: Location) {}

  ngOnInit(): void {
    this.http.get<any[]>(`http://localhost:8081/api/historiales/usuario/${this.usuarioId}`)
      .subscribe({
        next: (data) => {
          this.historial = data;
          this.vacio = data.length === 0;
          this.cargando = false;
        },
        error: () => {
          this.cargando = false;
          this.vacio = true;
        }
      });
  }

  volver() {
    this.location.back();
  }
}
