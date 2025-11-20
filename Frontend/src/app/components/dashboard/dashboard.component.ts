// src/app/components/dashboard/dashboard.component.ts

import { Component, ViewChild, ElementRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { ProductosService } from '../../services/producto.service';
import { CategoryService } from '../../services/categorias/category.service';
import { Producto } from '../../models/producto.model';
import { Categoria } from '../../models/category.model';
import { Page } from '../../models/page.model';
import Swal from 'sweetalert2'; // 👈 IMPORTAR SWEETALERT2

declare var bootstrap: any;

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  @ViewChild('modalProducto') modalProducto!: ElementRef;

  productos: Producto[] = [];
  tituloFiltro: string = '';
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
  ) {}

  ngOnInit(): void {
    this.aplicarFiltros();
    this.cargarCategorias();
    this.propietarioId = Number(localStorage.getItem('userId')) || null;
  }

  cargarCategorias() {
    this.categoryService.getCategoriesTree().subscribe(data => {
      this.categorias = this.aplanarCategorias(data);
    });
  }

  private aplanarCategorias(categorias: Categoria[], nivel = 0): Categoria[] {
    let listaPlana: Categoria[] = [];
    for (const cat of categorias) {
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

    if (imagenesValidas.length !== files.length) {
        // 🌟 ALERTA DE IMÁGENES INVÁLIDAS
        Swal.fire({
            icon: 'warning',
            title: 'Archivos ignorados',
            text: 'Algunos archivos no eran imágenes válidas o excedían 2MB.',
            confirmButtonColor: '#6d5d9a'
        });
    }

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

      Swal.fire({
        icon: 'error',
        title: 'Faltan imágenes',
        text: 'Debes seleccionar al menos una imagen para el producto.',
        confirmButtonColor: '#d33'
      });
      return;
    }

    
    Swal.fire({
        title: this.editando ? 'Actualizando...' : 'Creando...',
        text: 'Por favor espera',
        allowOutsideClick: false,
        didOpen: () => {
            Swal.showLoading();
        }
    });

    const formData = new FormData();
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

    const request = this.editando && this.nuevoProducto.id
      ? this.productosService.actualizarProducto(this.nuevoProducto.id, formData)
      : this.productosService.crearProductoConImagen(formData);

    request.subscribe({
      next: (p) => {
        this.resetFormulario();
        this.aplicarFiltros();
        bootstrap.Modal.getInstance(this.modalProducto.nativeElement)?.hide();
        
        // 🌟 ALERTA DE ÉXITO
        Swal.fire({
            icon: 'success',
            title: this.editando ? '¡Actualizado!' : '¡Creado!',
            text: `El producto ha sido ${this.editando ? 'actualizado' : 'creado'} exitosamente.`,
            timer: 2000,
            showConfirmButton: false
        });
      },
      error: (e) => {
        console.error('Error:', e);
        // 🌟 ALERTA DE ERROR
        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'Ocurrió un error al procesar la solicitud.',
            confirmButtonColor: '#d33'
        });
      }
    });
  }

  editarProducto(producto: Producto): void {
    this.nuevoProducto = { 
      ...producto,
      categoriaId: producto.categoria.id 
    };
    
    this.editando = true;
    this.imagenesSeleccionadas = [];
    this.imagenesPreviewUrl = [];
    
    if (producto.imagenes && producto.imagenes.length > 0) {
      this.imagenesPreviewUrl = producto.imagenes.map(imagen => imagen.secureUrl);
    }

    const modal = new bootstrap.Modal(this.modalProducto.nativeElement);
    modal.show();
  }

  cancelarEdicion(): void {
    this.resetFormulario();
  }

  eliminarProducto(id: number): void {
    // 🌟 ALERTA DE CONFIRMACIÓN (SweetAlert2)
    Swal.fire({
      title: '¿Estás seguro?',
      text: "No podrás revertir esta acción. El producto será eliminado permanentemente.",
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#6d5d9a',
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      background: '#fff',
      color: '#333'
    }).then((result) => {
      if (result.isConfirmed) {
        // Mostrar loading mientras elimina
        Swal.fire({
            title: 'Eliminando...',
            didOpen: () => Swal.showLoading()
        });

        this.productosService.eliminarProducto(id).subscribe({
          next: () => {
            this.aplicarFiltros();
            // 🌟 ALERTA DE ÉXITO
            Swal.fire(
              '¡Eliminado!',
              'El producto ha sido eliminado.',
              'success'
            );
          },
          error: (e) => {
            console.error('Error al eliminar', e);
            // 🌟 ALERTA DE ERROR
            Swal.fire(
              'Error',
              'No se pudo eliminar el producto.',
              'error'
            );
          }
        });
      }
    });
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
      id: undefined,
      titulo: '',
      descripcion: '',
      categoriaId: null,
      precio: 0,
      estado: 'Borrador',
      propietarioId: Number(localStorage.getItem('userId')) || 0
    };
  }

  logout() {
    // 🌟 ALERTA DE CONFIRMACIÓN DE LOGOUT (Opcional)
    Swal.fire({
        title: 'Cerrar sesión',
        text: '¿Deseas salir de tu cuenta?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#6d5d9a',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Sí, salir'
    }).then((result) => {
        if (result.isConfirmed) {
            localStorage.removeItem('token');
            this.router.navigate(['/login-register']);
        }
    });
  }

  filtrarPorEstado() {
    this.aplicarFiltros();
  }

  aplicarFiltros() {
    this.propietarioId = Number(localStorage.getItem('userId')) || null; 
    this.productosService.buscarProductos(
      this.tituloFiltro || null,
      this.categoriaFiltro, 
      this.estadoFiltro,
      this.propietarioId
    ).subscribe({
      next: (page: Page<Producto>) => { 
        this.productos = page.content;
      },
      error: (err) => {
        console.error('Error al llamar a buscarProductos:', err);
        // Opcional: Mostrar toast de error discreto
        const Toast = Swal.mixin({
            toast: true,
            position: 'top-end',
            showConfirmButton: false,
            timer: 3000
        });
        Toast.fire({
            icon: 'error',
            title: 'Error al cargar productos'
        });
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