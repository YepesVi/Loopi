import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Home } from './home';
import { ProductosService } from '../../services/producto.service';
import { CategoryService } from '../../services/categorias/category.service';
import { AuthService } from '../../services/auth.service';
import { CarritoService } from '../../services/carrito/carrito-service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { Categoria } from '../../models/category.model';
import { Producto } from '../../models/producto.model';
import { Page } from '../../models/page.model';

describe('Home', () => {
  let component: Home;
  let fixture: ComponentFixture<Home>;

  // Mocks
  let mockProductosService: jasmine.SpyObj<ProductosService>;
  let mockCategoryService: jasmine.SpyObj<CategoryService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockCarritoService: jasmine.SpyObj<CarritoService>;
  let mockRouter: jasmine.SpyObj<Router>;

  // Datos de prueba
  const mockCategorias: Categoria[] = [
    { id: 1, nombre: 'Tecnología', hijos: [] },
    { id: 2, nombre: 'Hogar', hijos: [] }
  ];

  const mockProductos: Producto[] = [
    { id: 101, titulo: 'Laptop', descripcion: '...', precio: 1000, estado: 'Publicado', categoria: mockCategorias[0], propietarioId: 1, imagenes: [] },
    { id: 102, titulo: 'Mouse', descripcion: '...', precio: 20, estado: 'Publicado', categoria: mockCategorias[0], propietarioId: 1, imagenes: [] },
    { id: 103, titulo: 'Teclado', descripcion: '...', precio: 50, estado: 'Publicado', categoria: mockCategorias[0], propietarioId: 1, imagenes: [] },
    { id: 104, titulo: 'Monitor', descripcion: '...', precio: 200, estado: 'Publicado', categoria: mockCategorias[0], propietarioId: 1, imagenes: [] },
    { id: 105, titulo: 'Silla', descripcion: '...', precio: 150, estado: 'Publicado', categoria: mockCategorias[1], propietarioId: 1, imagenes: [] }
  ];

  const mockPage: Page<Producto> = {
    content: mockProductos,
    totalPages: 1,
    totalElements: 5,
    size: 10,
    number: 0,
    first: true,
    last: true,
    numberOfElements: 5,
    empty: false
  };

  beforeEach(async () => {
    // 1. Crear Spies
    mockProductosService = jasmine.createSpyObj('ProductosService', ['buscarProductos']);
    mockCategoryService = jasmine.createSpyObj('CategoryService', ['getCategoriesTree']);
    mockAuthService = jasmine.createSpyObj('AuthService', ['loggedInSignal']);
    mockCarritoService = jasmine.createSpyObj('CarritoService', ['agregarProducto']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    // 2. Configurar retornos por defecto
    mockCategoryService.getCategoriesTree.and.returnValue(of(mockCategorias));
    mockProductosService.buscarProductos.and.returnValue(of(mockPage));
    mockAuthService.loggedInSignal.and.returnValue(false); // Por defecto no logueado

    await TestBed.configureTestingModule({
      imports: [Home], // Componente Standalone
      providers: [
        { provide: ProductosService, useValue: mockProductosService },
        { provide: CategoryService, useValue: mockCategoryService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: CarritoService, useValue: mockCarritoService },
        { provide: Router, useValue: mockRouter }
      ],
      schemas: [NO_ERRORS_SCHEMA] // Ignorar app-product-card en el HTML
    }).compileComponents();

    fixture = TestBed.createComponent(Home);
    component = fixture.componentInstance;
    // No llamamos a detectChanges() aquí para poder controlar ngOnInit en cada test
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });

  // --- 1. Test de Carga de Datos (cargarDatosHome) ---

  it('debería cargar categorías y productos al iniciar', () => {
    fixture.detectChanges(); // Ejecuta ngOnInit -> cargarDatosHome

    expect(mockCategoryService.getCategoriesTree).toHaveBeenCalled();
    // Se debe llamar a buscarProductos por cada categoría raíz (2 veces)
    expect(mockProductosService.buscarProductos).toHaveBeenCalledTimes(2);
    
    // Verificar que se llenaron las secciones
    expect(component.secciones.length).toBe(2);
    expect(component.secciones[0].categoria.nombre).toBe('Tecnología');
    expect(component.loading).toBeFalse();
  });

  it('debería manejar error al cargar categorías', () => {
    mockCategoryService.getCategoriesTree.and.returnValue(throwError(() => new Error('Error')));
    spyOn(console, 'error'); // Espiar consola para que no ensucie el output

    fixture.detectChanges();

    expect(component.secciones.length).toBe(0);
    expect(component.loading).toBeFalse();
    expect(console.error).toHaveBeenCalled();
  });

  it('NO debería crear sección si la categoría no tiene productos', () => {
    const pageVacia = { ...mockPage, content: [] };
    mockProductosService.buscarProductos.and.returnValue(of(pageVacia));

    fixture.detectChanges();

    expect(component.secciones.length).toBe(0);
  });

  // --- 2. Test de Helpers (dividirEnGrupos) ---

  it('debería dividir un array en grupos del tamaño especificado', () => {
    const array = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
    const grupos = component.dividirEnGrupos(array as any, 4);

    expect(grupos.length).toBe(3); // [1,2,3,4], [5,6,7,8], [9,10]
    expect(grupos[0].length).toBe(4);
    expect(grupos[2].length).toBe(2);
  });

  // --- 3. Test de Navegación (verMas) ---

  it('debería navegar a la página de productos con el filtro de categoría', () => {
    component.verMas(5);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/productos'], { 
      queryParams: { categoriaId: 5 } 
    });
  });

  // --- 4. Test de Producto Pendiente (verificarProductoPendiente) ---

  it('debería agregar producto al carrito si hay pendiente y usuario logueado', () => {
    // Configurar escenario
    mockAuthService.loggedInSignal.and.returnValue(true);
    spyOn(localStorage, 'getItem').and.callFake((key) => {
      if (key === 'productoPendiente') return '99';
      if (key === 'cedula') return '12345';
      return null;
    });
    spyOn(localStorage, 'removeItem');
    mockCarritoService.agregarProducto.and.returnValue(of(null));

    fixture.detectChanges(); // ngOnInit

    expect(mockCarritoService.agregarProducto).toHaveBeenCalledWith(99);
    expect(localStorage.removeItem).toHaveBeenCalledWith('productoPendiente');
  });

  it('NO debería agregar producto si NO hay sesión iniciada', () => {
    mockAuthService.loggedInSignal.and.returnValue(false);
    spyOn(localStorage, 'getItem').and.returnValue('99'); // Hay pendiente
    
    fixture.detectChanges();

    expect(mockCarritoService.agregarProducto).not.toHaveBeenCalled();
  });

  it('NO debería hacer nada si no hay producto pendiente', () => {
    mockAuthService.loggedInSignal.and.returnValue(true);
    spyOn(localStorage, 'getItem').and.returnValue(null);

    fixture.detectChanges();

    expect(mockCarritoService.agregarProducto).not.toHaveBeenCalled();
  });
});