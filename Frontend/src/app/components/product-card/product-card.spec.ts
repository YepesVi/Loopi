import { TestBed, ComponentFixture } from '@angular/core/testing';
import Swal from 'sweetalert2';
import { of, throwError } from 'rxjs';

import { ProductCard } from './product-card'; // Ajusta la ruta a donde tengas el component

import { AuthService } from '../../services/auth.service';
import { CarritoService } from '../../services/carrito/carrito-service';
import { Router } from '@angular/router';

import { Producto } from '../../models/producto.model';
import { Categoria } from '../../models/category.model';
import { Imagen } from '../../models/imagen.model';


describe('ProductCard', () => {
  let component: ProductCard;
  let fixture: ComponentFixture<ProductCard>;

  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let carritoServiceSpy: jasmine.SpyObj<CarritoService>;
  let routerSpy: jasmine.SpyObj<Router>;

  // --------- Mocks de modelos ---------

  const categoriaMock: Categoria = {
    id: 1,
    nombre: 'Tecnología',
    hijos: []
  };

  const imagenesMock: Imagen[] = []; // ajusta si Imagen tiene campos obligatorios

  const productoMock: Producto = {
    id: 123,
    titulo: 'Producto test',
    descripcion: 'Descripción test',
    categoria: categoriaMock,
    precio: 1000,
    estado: 'DISPONIBLE',
    propietarioId: 1,
    imagenes: imagenesMock,
    fechaCreacion: '2024-01-01'
  };

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['loggedInSignal']);
    carritoServiceSpy = jasmine.createSpyObj('CarritoService', ['agregarProducto']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [ProductCard],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: CarritoService, useValue: carritoServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductCard);
    component = fixture.componentInstance;
    component.producto = productoMock;
    fixture.detectChanges();
  });

  // 1. Navegación al detalle
  it('debería navegar al detalle si el producto tiene id', () => {
    component.irAlDetalle();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/producto', productoMock.id]);
  });

  it('no debería navegar si el producto no tiene id', () => {
    component.producto = { ...productoMock, id: undefined };
    component.irAlDetalle();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });

  // 2. comprarProducto
  it('debería parar la propagación del evento en comprarProducto', () => {
    const eventMock = { stopPropagation: jasmine.createSpy('stopPropagation') } as any;

    authServiceSpy.loggedInSignal.and.returnValue(true);
    carritoServiceSpy.agregarProducto.and.returnValue(of({}));

    component.comprarProducto(eventMock);

    expect(eventMock.stopPropagation).toHaveBeenCalled();
  });

  it('debería llamar manejarLoginRequerido si no está logueado', () => {
    const eventMock = { stopPropagation: () => {} } as any;

    authServiceSpy.loggedInSignal.and.returnValue(false);
    const manejarLoginSpy = spyOn<any>(component, 'manejarLoginRequerido');

    component.comprarProducto(eventMock);

    expect(manejarLoginSpy).toHaveBeenCalled();
    expect(carritoServiceSpy.agregarProducto).not.toHaveBeenCalled();
  });

  it('debería llamar agregarProductoAlCarrito si está logueado y hay id', () => {
    const eventMock = { stopPropagation: () => {} } as any;

    authServiceSpy.loggedInSignal.and.returnValue(true);
    carritoServiceSpy.agregarProducto.and.returnValue(of({}));

    const agregarSpy = spyOn<any>(component, 'agregarProductoAlCarrito').and.callThrough();

    component.comprarProducto(eventMock);

    expect(agregarSpy).toHaveBeenCalledWith(productoMock.id);
  });

  // 3. verificarProductoPendiente / localStorage
  describe('verificarProductoPendiente', () => {
    let getItemSpy: jasmine.Spy;
    let removeItemSpy: jasmine.Spy;

    beforeEach(() => {
      getItemSpy = spyOn(localStorage, 'getItem');
      removeItemSpy = spyOn(localStorage, 'removeItem');
    });

    it('debería agregar producto pendiente si existe en localStorage', () => {
      getItemSpy.withArgs('productoPendiente').and.returnValue('123');
      getItemSpy.withArgs('cedula').and.returnValue('1111');
      carritoServiceSpy.agregarProducto.and.returnValue(of({}));

      const agregarSpy = spyOn<any>(component, 'agregarProductoAlCarrito').and.callThrough();

      component.verificarProductoPendiente();

      expect(agregarSpy).toHaveBeenCalledWith(123);
      expect(removeItemSpy).toHaveBeenCalledWith('productoPendiente');
    });

    it('no debería hacer nada si no hay productoPendiente o cedula', () => {
      getItemSpy.withArgs('productoPendiente').and.returnValue(null);
      getItemSpy.withArgs('cedula').and.returnValue(null);

      const agregarSpy = spyOn<any>(component, 'agregarProductoAlCarrito');

      component.verificarProductoPendiente();

      expect(agregarSpy).not.toHaveBeenCalled();
      expect(removeItemSpy).not.toHaveBeenCalled();
    });
  });

  // 4. manejarLoginRequerido + SweetAlert + localStorage
  describe('manejarLoginRequerido', () => {
    let setItemSpy: jasmine.Spy;
    let swalFireSpy: jasmine.Spy;

    beforeEach(() => {
      setItemSpy = spyOn(localStorage, 'setItem');
      swalFireSpy = spyOn(Swal, 'fire').and.returnValue(
        Promise.resolve({ isConfirmed: true } as any)
      );
    });

    it('debería guardar productoPendiente en localStorage', async () => {
      await (component as any).manejarLoginRequerido();

      expect(setItemSpy).toHaveBeenCalledWith(
        'productoPendiente',
        productoMock.id!.toString()
      );
      expect(swalFireSpy).toHaveBeenCalled();
    });

    it('debería navegar a /login-register si se confirma la alerta', async () => {
      swalFireSpy.and.returnValue(
        Promise.resolve({ isConfirmed: true } as any)
      );

      await (component as any).manejarLoginRequerido();

      expect(routerSpy.navigate).toHaveBeenCalledWith(['/login-register']);
    });

    it('no debería navegar si el usuario cancela', async () => {
      swalFireSpy.and.returnValue(
        Promise.resolve({ isConfirmed: false } as any)
      );

      await (component as any).manejarLoginRequerido();

      expect(routerSpy.navigate).not.toHaveBeenCalled();
    });
  });

  // 5. agregarProductoAlCarrito + SweetAlert
  describe('agregarProductoAlCarrito', () => {
    let swalFireSpy: jasmine.Spy;
    let swalMixinSpy: jasmine.Spy;

    beforeEach(() => {
      swalFireSpy = spyOn(Swal, 'fire');
      swalMixinSpy = spyOn(Swal, 'mixin').and.returnValue({
        fire: jasmine.createSpy('fire')
      } as any);
    });

    it('debería mostrar toast de éxito cuando el servicio responde OK', () => {
      carritoServiceSpy.agregarProducto.and.returnValue(of({}));

      (component as any).agregarProductoAlCarrito(123);

      expect(carritoServiceSpy.agregarProducto).toHaveBeenCalledWith(123);
      expect(swalMixinSpy).toHaveBeenCalled();
      const toastInstance = swalMixinSpy.calls.mostRecent().returnValue as any;
      expect(toastInstance.fire).toHaveBeenCalledWith({
        icon: 'success',
        title: 'Producto agregado al carrito'
      });
    });

    it('debería mostrar alerta de error cuando el servicio falla', () => {
      carritoServiceSpy.agregarProducto.and.returnValue(
        throwError(() => new Error('error'))
      );

      (component as any).agregarProductoAlCarrito(123);

      expect(carritoServiceSpy.agregarProducto).toHaveBeenCalledWith(123);
      expect(swalFireSpy).toHaveBeenCalledWith({
        icon: 'error',
        title: 'Error',
        text: 'No se pudo agregar al carrito'
      });
    });
  });

  // 6. Scroll de imágenes
  describe('scroll de imágenes', () => {
    it('scrollLeft debería llamar a smoothScroll con -200', () => {
      const eventMock = { stopPropagation: jasmine.createSpy('stopPropagation') } as any;
      const smoothSpy = spyOn<any>(component, 'smoothScroll');

      component.scrollLeft(eventMock);

      expect(eventMock.stopPropagation).toHaveBeenCalled();
      expect(smoothSpy).toHaveBeenCalledWith(-200);
    });

    it('scrollRight debería llamar a smoothScroll con 200', () => {
      const eventMock = { stopPropagation: jasmine.createSpy('stopPropagation') } as any;
      const smoothSpy = spyOn<any>(component, 'smoothScroll');

      component.scrollRight(eventMock);

      expect(eventMock.stopPropagation).toHaveBeenCalled();
      expect(smoothSpy).toHaveBeenCalledWith(200);
    });

    it('smoothScroll debería llamar a scrollBy del contenedor si existe', () => {
      const scrollBySpy = jasmine.createSpy('scrollBy');
      (component as any).scrollContainer = {
        nativeElement: { scrollBy: scrollBySpy }
      };

      (component as any).smoothScroll(150);

      expect(scrollBySpy).toHaveBeenCalledWith({ left: 150, behavior: 'smooth' });
    });

    it('smoothScroll no debería fallar si no hay contenedor', () => {
      (component as any).scrollContainer = undefined;
      expect(() => (component as any).smoothScroll(100)).not.toThrow();
    });
  });
});