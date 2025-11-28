import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Carrito } from './carrito';
import { CarritoService } from '../../services/carrito/carrito-service';
import { of, throwError } from 'rxjs';

describe('CarritoComponent', () => {
  let component: Carrito;
  let fixture: ComponentFixture<Carrito>;
  let mockCarrito: jasmine.SpyObj<CarritoService>;

  beforeEach(async () => {
    mockCarrito = jasmine.createSpyObj('CarritoService', [
      'getCarrito',
      'eliminarProducto',
      'vaciarCarrito',
      'crearPago'
    ]);

    // ✅ Simular getCarrito antes de crear el componente
    mockCarrito.getCarrito.and.returnValue(of({ items: [] }));

    await TestBed.configureTestingModule({
      imports: [Carrito],
      providers: [
        { provide: CarritoService, useValue: mockCarrito }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Carrito);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crear el componente', () => {
    expect(component).toBeTruthy();
  });

  it('debería cargar carrito exitosamente', () => {
    const mockData = { items: [{ id: 1, producto: { precio: 100 } }] };
    mockCarrito.getCarrito.and.returnValue(of(mockData));

    component.cargarCarrito();

    expect(mockCarrito.getCarrito).toHaveBeenCalled();
    expect(component.carrito.length).toBe(1);
    expect(component.total).toBe(100);
    expect(component.cargando).toBeFalse();
  });

  it('debería manejar error al cargar carrito', () => {
    mockCarrito.getCarrito.and.returnValue(throwError(() => new Error('Error')));

    component.cargarCarrito();

    expect(mockCarrito.getCarrito).toHaveBeenCalled();
    expect(component.cargando).toBeFalse();
  });

  it('debería calcular total correctamente', () => {
    component.carrito = [
      { producto: { precio: 50 } },
      { producto: { precio: 150 } }
    ];
    component.calcularTotal();
    expect(component.total).toBe(200);
  });

it('debería eliminar producto y recargar carrito', () => {
  mockCarrito.eliminarProducto.and.returnValue(of(void 0));
  mockCarrito.getCarrito.and.returnValue(of({ items: [] }));

  component.eliminar(1);

  expect(mockCarrito.eliminarProducto).toHaveBeenCalledWith(1);
  expect(mockCarrito.getCarrito).toHaveBeenCalled();
});


  it('debería vaciar carrito y recargar', () => {
    mockCarrito.vaciarCarrito.and.returnValue(of(void 0));
    mockCarrito.getCarrito.and.returnValue(of({ items: [] }));

    component.vaciar();

    expect(mockCarrito.vaciarCarrito).toHaveBeenCalled();
    expect(mockCarrito.getCarrito).toHaveBeenCalled();
  });

  it('debería comprar y redirigir si initPoint existe', () => {
  Object.defineProperty(window, 'location', {
    value: { href: '' },
    writable: true
  });

  mockCarrito.crearPago.and.returnValue(of({ initPoint: true, sandboxInitPoint: 'http://sandbox' }));

  component.carrito = [{ producto: { id: 1, titulo: 'Test', precio: 100 } }];
  component.comprar();

  expect(mockCarrito.crearPago).toHaveBeenCalled();
  expect(window.location.href).toBe('http://sandbox');
});


  it('debería mostrar alerta si no hay initPoint', () => {
    spyOn(window, 'alert');
    mockCarrito.crearPago.and.returnValue(of({}));

    component.carrito = [{ producto: { id: 1, titulo: 'Test', precio: 100 } }];
    component.comprar();

    expect(window.alert).toHaveBeenCalledWith('No se recibió la URL de pago.');
  });
  
  
  it('debería manejar error al comprar', () => {
    spyOn(window, 'alert');
    mockCarrito.crearPago.and.returnValue(throwError(() => new Error('Error')));

    component.carrito = [{ producto: { id: 1, titulo: 'Test', precio: 100 } }];
    component.comprar();

    expect(window.alert).toHaveBeenCalledWith('Error al generar el pago.');
  });
});
