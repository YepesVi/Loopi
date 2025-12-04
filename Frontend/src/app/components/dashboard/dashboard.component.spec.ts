// -------------------------------------------------------------
// MOCK GLOBAL DE BOOTSTRAP
// -------------------------------------------------------------
(globalThis as any).bootstrap = {
  Modal: class {
    static getInstance() {
      return { hide() {} };
    }
    constructor(el: any) {}
    show() {}
    hide() {}
  }
};

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardComponent } from './dashboard.component';
import { ProductosService } from '../../services/producto.service';
import { CategoryService } from '../../services/categorias/category.service';
import { of } from 'rxjs';
import Swal from 'sweetalert2';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

describe('DashboardComponent', () => {   // 👈 SOLO CORRE DASHBOARD
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let productosServiceMock: any;
  let categoryServiceMock: any;

  beforeEach(async () => {
    productosServiceMock = {
      buscarProductos: jasmine.createSpy('buscarProductos').and.returnValue(of({ content: [] })),
      crearProductoConImagen: jasmine.createSpy('crearProductoConImagen').and.returnValue(of({})),
      actualizarProducto: jasmine.createSpy('actualizarProducto').and.returnValue(of({})),
      eliminarProducto: jasmine.createSpy('eliminarProducto').and.returnValue(of({}))
    };

    categoryServiceMock = {
      getCategoriesTree: jasmine.createSpy('getCategoriesTree').and.returnValue(of([]))
    };

    spyOn(Swal, 'fire');

    await TestBed.configureTestingModule({
      imports: [
        DashboardComponent   // 👈 Standalone
      ],
      providers: [
        provideHttpClient(),
        provideRouter([]),   // 👈 Router para pruebas (NO mocks extra)
        { provide: ProductosService, useValue: productosServiceMock },
        { provide: CategoryService, useValue: categoryServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;

    // Simular modal
    component.modalProducto = {
      nativeElement: document.createElement('div')
    } as any;

    fixture.detectChanges();
  });

  // -------------------------------------------------------------
  // TESTS
  // -------------------------------------------------------------

  it('Debe crearse el componente', () => {
    expect(component).toBeTruthy();
  });

  it('ngOnInit debe cargar categorías y aplicar filtros', () => {
    component.ngOnInit();
    expect(categoryServiceMock.getCategoriesTree).toHaveBeenCalled();
    expect(productosServiceMock.buscarProductos).toHaveBeenCalled();
  });

  it('aplanarCategorias debe devolver una lista plana', () => {
    const input = [
      {
        id: 1,
        nombre: 'Ropa',
        hijos: [{ id: 2, nombre: 'Camisas', hijos: [] }]
      }
    ];

    const result = (component as any)['aplanarCategorias'](input);

    expect(result.length).toBe(2);
    expect(result[1].nombre.trim()).toContain('Camisas');
  });

  it('aplicarFiltros debe llamar a buscarProductos', () => {
    component.aplicarFiltros();
    expect(productosServiceMock.buscarProductos).toHaveBeenCalled();
  });

  it('crearProducto debe mostrar error si falta imagen y no está editando', () => {
    component.editando = false;
    component.imagenesSeleccionadas = [];

    component.crearProducto();

    expect(Swal.fire).toHaveBeenCalled();
  });

  it('crearProducto debe llamar a crearProductoConImagen', () => {
    const fakeImage = new File(['dummy'], 'img.png', { type: 'image/png' });

    component.imagenesSeleccionadas = [fakeImage];
    component.editando = false;
    component.nuevoProducto.titulo = 'Test';

    component.crearProducto();

    expect(productosServiceMock.crearProductoConImagen).toHaveBeenCalled();
  });

  it('editarProducto debe activar edición y cargar datos', () => {
    const producto = {
      id: 1,
      titulo: 'Test',
      descripcion: 'Desc',
      categoria: { id: 5 },
      imagenes: [{ secureUrl: 'url1' }]
    } as any;

    component.editarProducto(producto);

    expect(component.editando).toBeTrue();
    expect(component.nuevoProducto.titulo).toBe('Test');
    expect(component.imagenesPreviewUrl.length).toBe(1);
  });

  it('cancelarEdicion debe resetear el formulario', () => {
    component.editando = true;
    component.cancelarEdicion();
    expect(component.editando).toBeFalse();
  });

  it('eliminarProducto debe llamar a eliminarProducto cuando confirma', async () => {
    (Swal.fire as jasmine.Spy).and.returnValue(Promise.resolve({ isConfirmed: true }));

    await component.eliminarProducto(1);

    expect(productosServiceMock.eliminarProducto).toHaveBeenCalled();
  });

  it('scrollLeft no debe fallar aunque el elemento no exista', () => {
    spyOn(document, 'getElementById').and.returnValue(null);
    component.scrollLeft('id');
    expect(true).toBeTrue();
  });
});