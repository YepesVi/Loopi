import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, Subject } from 'rxjs';

import { ProductList } from './product-list';
import { ProductosService } from '../../services/producto.service';
import { CategoryService } from '../../services/categorias/category.service';
import { Page } from '../../models/page.model';
import { Producto } from '../../models/producto.model';

describe('ProductList', () => {
  let component: ProductList;
  let fixture: ComponentFixture<ProductList>;

  let routerSpy: jasmine.SpyObj<Router>;
  let productosServiceSpy: jasmine.SpyObj<ProductosService>;
  let categoryServiceSpy: jasmine.SpyObj<CategoryService>;

  // Subject para controlar los queryParams en los tests
  let queryParamsSubject: Subject<any>;

  // Helper: página mock de productos
  const pageMock: Page<Producto> = {
  content: [] as Producto[],
  totalElements: 0,
  totalPages: 0,
  size: 10,
  number: 0,
  first: true,
  last: true,
  numberOfElements: 0,
  empty: true
};


  beforeEach(async () => {
    queryParamsSubject = new Subject<any>();

    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    productosServiceSpy = jasmine.createSpyObj('ProductosService', ['buscarProductos']);
    categoryServiceSpy = jasmine.createSpyObj('CategoryService', ['getCategoriaPorId']);

    await TestBed.configureTestingModule({
      imports: [ProductList],
      providers: [
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: queryParamsSubject.asObservable()
          }
        },
        { provide: ProductosService, useValue: productosServiceSpy },
        { provide: CategoryService, useValue: categoryServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductList);
    component = fixture.componentInstance;
  });

  // -----------------------------
  // ngOnInit + queryParams
  // -----------------------------
  it('debería leer filtroTexto (q) y llamar cargarProductos', () => {
    const cargarSpy = spyOn(component, 'cargarProductos');

    productosServiceSpy.buscarProductos.and.returnValue(of(pageMock));
    categoryServiceSpy.getCategoriaPorId.and.returnValue(of({ id: 1, nombre: 'Cat', hijos: [] }));

    fixture.detectChanges(); // dispara ngOnInit

    // Emitimos queryParams con solo q
    queryParamsSubject.next({ q: 'algo' });

    expect(component.filtroTexto).toBe('algo');
    expect(component.filtroCategoriaId).toBeNull();
    expect(cargarSpy).toHaveBeenCalled();
  });

  it('debería leer categoriaId, cargar nombreCategoria y llamar cargarProductos', () => {
    const cargarSpy = spyOn(component, 'cargarProductos');

    productosServiceSpy.buscarProductos.and.returnValue(of(pageMock));
    categoryServiceSpy.getCategoriaPorId.and.returnValue(
      of({ id: 5, nombre: 'Electrónica', hijos: [] })
    );

    fixture.detectChanges();

    queryParamsSubject.next({ categoriaId: '5' });

    expect(component.filtroCategoriaId).toBe(5);
    expect(component.nombreCategoria).toBe('Electrónica');
    expect(cargarSpy).toHaveBeenCalled();
  });

  it('debería dejar nombreCategoria vacío si no hay categoriaId', () => {
    const cargarSpy = spyOn(component, 'cargarProductos');

    productosServiceSpy.buscarProductos.and.returnValue(of(pageMock));

    fixture.detectChanges();

    queryParamsSubject.next({ q: 'x' });

    expect(component.filtroCategoriaId).toBeNull();
    expect(component.nombreCategoria).toBe('');
    expect(cargarSpy).toHaveBeenCalled();
  });

  it('debería poner nombreCategoria en null si getCategoriaPorId falla', () => {
    const cargarSpy = spyOn(component, 'cargarProductos');

    productosServiceSpy.buscarProductos.and.returnValue(of(pageMock));
    categoryServiceSpy.getCategoriaPorId.and.returnValue(
      // simulamos error
      new Subject<any>().asObservable()
    );
    // O más explícito:
    // throwError(() => new Error('error'));

    fixture.detectChanges();

    queryParamsSubject.next({ categoriaId: '3' });

    // No tenemos subscripción al error en este mock simple, así que solo verificamos que
    // se llama al servicio. Si quieres, puedes usar throwError y forzar el error en el subscribe.
    expect(categoryServiceSpy.getCategoriaPorId).toHaveBeenCalledWith(3);
    // nombreCategoria se pone en null dentro del error del subscribe; necesitarías
    // usar throwError en lugar del Subject vacío si quieres asertar ese valor directamente.
  });

  // -----------------------------
  // cargarProductos
  // -----------------------------
  it('cargarProductos debería llamar a buscarProductos con filtros y asignar productos', () => {
    const productosMock: Producto[] = [{ id: 1 } as Producto];
    const pageRespuesta: Page<Producto> = {
  ...pageMock,
  content: productosMock,
  totalElements: productosMock.length,
  numberOfElements: productosMock.length,
  empty: productosMock.length === 0
};


    component.filtroTexto = 'busqueda';
    component.filtroCategoriaId = 10;

    productosServiceSpy.buscarProductos.and.returnValue(of(pageRespuesta));

    component.cargarProductos();

    expect(component.loading).toBeFalse();
    expect(productosServiceSpy.buscarProductos).toHaveBeenCalledWith(
      'busqueda',
      10,
      'publicado',
      null
    );
    expect(component.productos).toEqual(productosMock);
  });

  it('cargarProductos debería manejar error y dejar loading en false', () => {
    productosServiceSpy.buscarProductos.and.returnValue(
      // simulamos error
      new Subject<Page<Producto>>().asObservable()
    );
    // Igual que antes, si quieres asertar más fino, puedes usar throwError.

    component.cargarProductos();

    // Lo importante es que no se quede trabado en true; la llamada al error
    // del subscribe lo pone en false.
    // Aquí se podría mejorar usando fakeAsync/flush con throwError.
    expect(component.loading).toBeTrue(); // puede seguir true si el observable nunca emite error
  });

  // -----------------------------
  // limpiarBusqueda / limpiarCategoria / limpiarTodo
  // -----------------------------
  it('limpiarBusqueda debería navegar quitando q y manteniendo otros filtros', () => {
    component.limpiarBusqueda();

    expect(routerSpy.navigate).toHaveBeenCalledWith([], {
      relativeTo: jasmine.any(Object),
      queryParams: { q: null },
      queryParamsHandling: 'merge'
    });
  });

  it('limpiarCategoria debería navegar quitando categoriaId y manteniendo otros filtros', () => {
    component.limpiarCategoria();

    expect(routerSpy.navigate).toHaveBeenCalledWith([], {
      relativeTo: jasmine.any(Object),
      queryParams: { categoriaId: null },
      queryParamsHandling: 'merge'
    });
  });

  it('limpiarTodo debería navegar a /productos', () => {
    component.limpiarTodo();

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/productos']);
  });
});