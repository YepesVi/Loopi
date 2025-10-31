import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductosService, Producto } from '../services/productos';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent {
  productos: Producto[] = [];
  estadoFiltro: string = '';
  fechaFiltro: string = '';
  todosLosProductos: Producto[] = [];
  nuevoProducto: Producto = {
    id: 0, // <- importante para saber si es edición o creación
    titulo: '',
    descripcion: '',
    categoria: '',
    precio: 0,
    estado: '',
    propietarioId: 1
  };
  imagenSeleccionada: File | null = null;
  editando: boolean = false;

  constructor(private productosService: ProductosService, private router: Router) {
    this.cargarProductos();
  }

  // ====== Cargar todos los productos ======
  cargarProductos() {
  this.productosService.getProductos().subscribe(data => {
    this.productos = data;
    this.todosLosProductos = data;
  });
}



  // ====== Seleccionar archivo ======
  onFileSelected(event: any) {
    this.imagenSeleccionada = event.target.files[0];
  }

  // ====== Crear o actualizar producto ======
  crearProducto() {
    const formData = new FormData();
    Object.entries(this.nuevoProducto).forEach(([k, v]) => {
  if (v !== null && v !== undefined) formData.append(k, String(v));
});
    if (this.imagenSeleccionada) formData.append('file', this.imagenSeleccionada);

    if (this.editando && this.nuevoProducto.id) {
      // ACTUALIZAR
      this.productosService.actualizarProducto(this.nuevoProducto.id, formData).subscribe({
        next: () => {
          alert('Producto actualizado correctamente');
          this.resetFormulario();
          this.cargarProductos();
        },
        error: (e) => console.error('Error al actualizar', e)
      });
    } else {
      // CREAR NUEVO
      this.productosService.crearProductoConImagen(formData).subscribe({
        next: (p) => {
          alert('Producto agregado correctamente');
          this.productos.push(p);
          this.resetFormulario();
        },
        error: (e) => console.error('Error al crear', e)
      });
    }
  }

  // ====== Editar producto ======
  editarProducto(producto: Producto): void {
    this.nuevoProducto = { ...producto };
    this.editando = true;
    this.imagenSeleccionada = null;
  }

  // ====== Eliminar producto ======
  eliminarProducto(id: number): void {
    if (confirm('¿Seguro que deseas eliminar este producto?')) {
      this.productosService.eliminarProducto(id)
        .subscribe({
          next: () => {
            alert('Producto eliminado');
            this.cargarProductos();
          },
          error: (e) => console.error('Error al eliminar', e)
        });
    }
  }

  // ====== Resetear formulario ======
  resetFormulario(): void {
    this.nuevoProducto = {
      id: 0,
      titulo: '',
      descripcion: '',
      categoria: '',
      precio: 0,
      estado: '',
      propietarioId: 1
    };
    this.imagenSeleccionada = null;
    this.editando = false;
  }

  // ====== Logout ======
  logout() {

    localStorage.removeItem('token'); // Limpia el token
    this.router.navigate(['/login-register']); // Redirige al login

  }

filtrarPorEstado() {
  // Al cambiar estado llamamos al filtrado combinado
  this.aplicarFiltros();
}

filtrarPorFecha() {
  // Al cambiar fecha llamamos al filtrado combinado
  this.aplicarFiltros();
}

aplicarFiltros() {
  // Empieza desde la lista completa
  let lista = [...this.todosLosProductos];

  // 1) Filtrar por estado si hay filtro
  if (this.estadoFiltro && this.estadoFiltro.trim() !== '') {
    lista = lista.filter(p => (p.estado ?? '').toLowerCase() === this.estadoFiltro.toLowerCase());
  }

  // 2) Filtrar por fecha si hay fechaFiltro (número de días)
  const dias = parseInt(this.fechaFiltro, 10);
  if (!isNaN(dias) && dias > 0) {
    const ahora = new Date();
    const fechaLimite = new Date(ahora.getTime() - dias * 24 * 60 * 60 * 1000);

    lista = lista.filter(p => {
      // tomar fechaCreacion, si no existe tomar fechaPublicacion (defensivo)
      const fechaStr = p.fechaCreacion ?? (p as any).fechaPublicacion; // 'as any' por si aún viene otro nombre
      if (!fechaStr) return false; // si no hay fecha, no incluir
      const fechaProducto = new Date(fechaStr); // ya es seguro: fechaStr es string
      return fechaProducto >= fechaLimite;
    });
  }

  // Finalmente asigna la lista filtrada
  this.productos = lista;
}
}

