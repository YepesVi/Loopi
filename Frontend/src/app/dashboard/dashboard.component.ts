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
    this.productosService.getProductos().subscribe(data => this.productos = data);
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
    localStorage.removeItem('token');
    this.router.navigate(['/login']);
  }
}
