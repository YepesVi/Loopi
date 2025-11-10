import { Component, ViewChild, ElementRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductosService } from '../services/producto.service';
import { Producto } from '../models/producto.model';
import { CategoryService } from '../services/categorias/category.service';
import { Categoria } from '../models/category.model';
import { Page } from '../models/page.model';

import { RouterModule, Router } from '@angular/router';
import { loadavg } from 'node:os';

declare var bootstrap: any;

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit{
  @ViewChild('modalProducto') modalProducto!: ElementRef;

  productos: Producto[] = [];
  estadoFiltro: string = '';
  categoriaFiltro: number | null = null;
  propietarioId: number | null = null;
  estados = ['Publicado', 'Borrador', 'Oculto', 'Vendido'];
  categorias: Categoria[] = [];
  imagenesSeleccionadas: File[] = [];
  imagenesPreviewUrl: string[] = [];
  imagenesInvalidas = false;

  nuevoProducto: {
    id?: number;
    titulo: string;
    descripcion: string;
    categoriaId: number | null; 
    precio: number;
    estado: string;
    propietarioId: number;
  } = this.getResetProducto();

  editando: boolean = false;
  constructor(
    private productosService: ProductosService, 
    private categoryService: CategoryService, 
    private router: Router
    ) {
  }

  ngOnInit(): void {
    this.aplicarFiltros(); // Carga los productos al iniciar
    this.cargarCategorias(); // Carga las categorías al iniciar
    this.propietarioId = Number(localStorage.getItem('userId')) || null;
  }

  cargarCategorias() {
    this.categoryService.getCategoriesTree().subscribe(data => {
      // Usamos el helper para aplanar el árbol (ej. "— Subcategoría")
      this.categorias = this.aplanarCategorias(data);
    });
  }

  private aplanarCategorias(categorias: Categoria[], nivel = 0): Categoria[] {
    let listaPlana: Categoria[] = [];
    for (const cat of categorias) {
      // Creamos una copia para no mutar el nombre original en otros componentes
      const catCopia = { ...cat }; 
      catCopia.nombre = '-'.repeat(nivel) + ' ' + cat.nombre;
      listaPlana.push(catCopia);
      
      if (cat.hijos && cat.hijos.length > 0) {
        listaPlana = listaPlana.concat(this.aplanarCategorias(cat.hijos, nivel + 1));
      }
    }
    return listaPlana;
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
    // Añadir campos de texto
    formData.append('titulo', this.nuevoProducto.titulo);
    formData.append('descripcion', this.nuevoProducto.descripcion);
    formData.append('precio', String(this.nuevoProducto.precio));
    formData.append('estado', this.nuevoProducto.estado);
    formData.append('propietarioId', localStorage.getItem('userId') || '0');
    
    
    if (this.nuevoProducto.categoriaId) {
      formData.append('categoriaId', String(this.nuevoProducto.categoriaId));
    }

    if (this.imagenesSeleccionadas.length > 0) {
      this.imagenesSeleccionadas.forEach(img => formData.append('file', img));
    }

    if (this.editando && this.nuevoProducto.id) {
      this.productosService.actualizarProducto(this.nuevoProducto.id, formData).subscribe({
        next: () => {
          alert('Producto actualizado correctamente');
          this.resetFormulario();
          this.aplicarFiltros();
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
    this.nuevoProducto = { 
      ...producto,
      categoriaId: producto.categoria.id // 👈 CAMBIO
    };
    
    this.editando = true;
    this.imagenesSeleccionadas = [];
    this.imagenesPreviewUrl = [];
    
    // Mostrar las imágenes existentes (Mejora de UX)
    if (producto.fotos) {
      this.imagenesPreviewUrl = producto.fotos.split(',').map(fotoUrl => 
          'http://localhost:8081/api/productos' + fotoUrl);
    }

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
          this.aplicarFiltros();
        },
        error: (e) => console.error('Error al eliminar', e)
      });
    }
  }

  resetFormulario(): void {
    this.nuevoProducto = this.getResetProducto();
    this.imagenesSeleccionadas = [];
    this.imagenesPreviewUrl = [];
    this.imagenesInvalidas = false;
    this.editando = false;
  }

  getResetProducto() {
    return {
      id: undefined, // Usar undefined en lugar de 0 para 'id'
      titulo: '',
      descripcion: '',
      categoriaId: null, // 👈 CAMBIO
      precio: 0,
      estado: 'Borrador', // Valor por defecto
      propietarioId: Number(localStorage.getItem('userId')) || 0
    };
  }

  logout() {
    localStorage.removeItem('token');
    this.router.navigate(['/login-register']);
  }

  filtrarPorEstado() {
    this.aplicarFiltros();
  }

  aplicarFiltros() {
    // Llamar al servicio con los filtros seleccionados
    console.log(`Filtrando con: CategoriaID=${this.categoriaFiltro}, Estado=${this.estadoFiltro}`);
   this.propietarioId = Number(localStorage.getItem('userId')) || null; 
   this.productosService.buscarProductos(
      this.categoriaFiltro, 
      this.estadoFiltro,
      this.propietarioId
    ).subscribe({
      next: (page: Page<Producto>) => { 
        console.log('Respuesta del Backend:', page); // <-- Revisa la consola del navegador
        this.productos = page.content;
      },
      error: (err) => {
        console.error('Error al llamar a buscarProductos:', err); // <-- Revisa si hay error
      }
    });
  }

  scrollLeft(id: string) {
    document.getElementById(id)?.scrollBy({ left: -200, behavior: 'smooth' });
  }

  scrollRight(id: string) {
    document.getElementById(id)?.scrollBy({ left: 200, behavior: 'smooth' });
  }
}
