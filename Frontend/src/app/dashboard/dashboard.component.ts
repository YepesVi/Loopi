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
  estados = ['Publicado', 'Borrador', 'Oculto', 'vendido'];
  categorias: string[] = ['Electrónica','Celulares y Accesorios','Computadoras y Tablets','Audio y Video','Fotografía y Cámaras','Electrodomésticos','Hogar y Muebles','Decoración','Cocina y Menaje','Ropa y Calzado','Bolsos y Accesorios','Joyería y Relojes','Deporte y Fitness','Bicicletas y Scooters','Instrumentos Musicales','Libros y Revistas','Cine, Música y Series','Videojuegos y Consolas','Juguetes y Juegos de Mesa','Coleccionismo','Antigüedades y Arte','Jardín y Herramientas','Mascotas y Accesorios','Salud y Belleza','Productos para Bebés','Coche y Moto','Industria y Oficina','Papelería y Material Escolar','Material de Construcción','Servicios','Otros'];
imagenesSeleccionadas: File[] = [];
imagenesPreviewUrl: string[] = [];
imagenesInvalidas = false;

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
  onFilesSelected(event: any) {
  const files: FileList = event.target.files;

  if (files && files.length > 0) {
    const validFormats = ['image/png', 'image/jpeg', 'image/webp'];
    const maxSize = 2 * 1024 * 1024; // 2 MB

    const imagenesValidas = Array.from(files).filter(
      (file) => validFormats.includes(file.type) && file.size <= maxSize
    );

    this.imagenesInvalidas = imagenesValidas.length !== files.length;
    this.imagenesSeleccionadas = imagenesValidas;

    // 👇 Previsualización en tiempo real
    this.imagenesPreviewUrl = [];
    imagenesValidas.forEach(file => {
      const reader = new FileReader();
      reader.onload = (e: any) => this.imagenesPreviewUrl.push(e.target.result);
      reader.readAsDataURL(file);
    });
  }

  }




  crearProducto() {
  // Validación previa de imágenes
  if (!this.editando && this.imagenesSeleccionadas.length === 0) {
    this.imagenesInvalidas = true;
    return;
  }

  const formData = new FormData();
  Object.entries(this.nuevoProducto).forEach(([k, v]) => {
    if (v !== null && v !== undefined) formData.append(k, String(v));
  });

  // Solo agregas imágenes al FormData si hay imágenes nuevas seleccionadas
  if (this.imagenesSeleccionadas.length > 0) {
  this.imagenesSeleccionadas.forEach(img => formData.append('file', img));

}

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

editarProducto(producto: Producto): void {
  this.nuevoProducto = { ...producto };
  this.editando = true;
  this.imagenesSeleccionadas = []; // Solo cargas nuevas si el usuario cambia
  this.imagenesPreviewUrl = [];
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
  this.imagenesSeleccionadas = [];
  this.imagenesPreviewUrl = [];
  this.imagenesInvalidas = false;
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

scrollLeft(elementId: string) {
  const el = document.getElementById(elementId);
  if (el) el.scrollBy({ left: -200, behavior: 'smooth' });
}

scrollRight(elementId: string) {
  const el = document.getElementById(elementId);
  if (el) el.scrollBy({ left: 200, behavior: 'smooth' });
}



}

