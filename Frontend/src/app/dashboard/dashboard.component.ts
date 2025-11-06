import { Component, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductosService, Producto } from '../services/productos';
import { RouterModule, Router } from '@angular/router';

declare var bootstrap: any;

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent {
  @ViewChild('modalProducto') modalProducto!: ElementRef;

  productos: Producto[] = [];
  todosLosProductos: Producto[] = [];
  estadoFiltro: string = '';
  fechaFiltro: string = '';
  estados = ['Publicado', 'Borrador', 'Oculto', 'vendido'];
  categorias: string[] = ['Electrónica','Celulares y Accesorios','Computadoras y Tablets','Audio y Video','Fotografía y Cámaras','Electrodomésticos','Hogar y Muebles','Decoración','Cocina y Menaje','Ropa y Calzado','Bolsos y Accesorios','Joyería y Relojes','Deporte y Fitness','Bicicletas y Scooters','Instrumentos Musicales','Libros y Revistas','Cine, Música y Series','Videojuegos y Consolas','Juguetes y Juegos de Mesa','Coleccionismo','Antigüedades y Arte','Jardín y Herramientas','Mascotas y Accesorios','Salud y Belleza','Productos para Bebés','Coche y Moto','Industria y Oficina','Papelería y Material Escolar','Material de Construcción','Servicios','Otros'];
  imagenesSeleccionadas: File[] = [];
  imagenesPreviewUrl: string[] = [];
  imagenesInvalidas = false;

  nuevoProducto: Producto = {
    id: 0,
    titulo: '',
    descripcion: '',
    categoria: '',
    precio: 0,
    estado: '',
    propietarioId: 1
  };

  editando: boolean = false;

  constructor(private productosService: ProductosService, private router: Router) {
    this.cargarProductos();
  }

  cargarProductos() {
    this.productosService.getProductos().subscribe(data => {
      this.productos = data;
      this.todosLosProductos = data;
    });
  }

  onFilesSelected(event: any) {
    const files: FileList = event.target.files;
    const validFormats = ['image/png', 'image/jpeg', 'image/webp'];
    const maxSize = 2 * 1024 * 1024;

    const imagenesValidas = Array.from(files).filter(
      (file) => validFormats.includes(file.type) && file.size <= maxSize
    );

    this.imagenesInvalidas = imagenesValidas.length !== files.length;
    this.imagenesSeleccionadas = imagenesValidas;
    this.imagenesPreviewUrl = [];

    imagenesValidas.forEach(file => {
      const reader = new FileReader();
      reader.onload = (e: any) => this.imagenesPreviewUrl.push(e.target.result);
      reader.readAsDataURL(file);
    });
  }

  crearProducto() {
    if (!this.editando && this.imagenesSeleccionadas.length === 0) {
      this.imagenesInvalidas = true;
      return;
    }

    const formData = new FormData();
    Object.entries(this.nuevoProducto).forEach(([k, v]) => {
      if (v !== null && v !== undefined) formData.append(k, String(v));
    });

    if (this.imagenesSeleccionadas.length > 0) {
      this.imagenesSeleccionadas.forEach(img => formData.append('file', img));
    }

    if (this.editando && this.nuevoProducto.id) {
      this.productosService.actualizarProducto(this.nuevoProducto.id, formData).subscribe({
        next: () => {
          alert('Producto actualizado correctamente');
          this.resetFormulario();
          this.cargarProductos();
          bootstrap.Modal.getInstance(this.modalProducto.nativeElement)?.hide();
        },
        error: (e) => console.error('Error al actualizar', e)
      });
    } else {
      this.productosService.crearProductoConImagen(formData).subscribe({
        next: (p) => {
          alert('Producto agregado correctamente');
          this.productos.push(p);
          this.resetFormulario();
          bootstrap.Modal.getInstance(this.modalProducto.nativeElement)?.hide();
        },
        error: (e) => console.error('Error al crear', e)
      });
    }
  }

  editarProducto(producto: Producto): void {
    this.nuevoProducto = { ...producto };
    this.editando = true;
    this.imagenesSeleccionadas = [];
    this.imagenesPreviewUrl = [];

    const modal = new bootstrap.Modal(this.modalProducto.nativeElement);
    modal.show();
  }

  cancelarEdicion(): void {
    this.resetFormulario();
  }

  eliminarProducto(id: number): void {
    if (confirm('¿Seguro que deseas eliminar este producto?')) {
      this.productosService.eliminarProducto(id).subscribe({
        next: () => {
          alert('Producto eliminado');
          this.cargarProductos();
        },
        error: (e) => console.error('Error al eliminar', e)
      });
    }
  }

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
    this.imagenesSeleccionadas = [];
    this.imagenesPreviewUrl = [];
    this.imagenesInvalidas = false;
    this.editando = false;
  }

  logout() {
    localStorage.removeItem('token');
    this.router.navigate(['/login-register']);
  }

  filtrarPorEstado() {
    this.aplicarFiltros();
  }

  filtrarPorFecha() {
    this.aplicarFiltros();
  }

  aplicarFiltros() {
    let lista = [...this.todosLosProductos];

    if (this.estadoFiltro.trim()) {
      lista = lista.filter(p => (p.estado ?? '').toLowerCase() === this.estadoFiltro.toLowerCase());
    }

    const dias = parseInt(this.fechaFiltro, 10);
    if (!isNaN(dias) && dias > 0) {
      const fechaLimite = new Date(Date.now() - dias * 86400000);
      lista = lista.filter(p => {
        const fechaStr = p.fechaCreacion ?? (p as any).fechaPublicacion;
        if (!fechaStr) return false;
        return new Date(fechaStr) >= fechaLimite;
      });
    }

    this.productos = lista;
  }

  scrollLeft(id: string) {
    document.getElementById(id)?.scrollBy({ left: -200, behavior: 'smooth' });
  }

  scrollRight(id: string) {
    document.getElementById(id)?.scrollBy({ left: 200, behavior: 'smooth' });
  }
}
