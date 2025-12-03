import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Carrito } from './carrito';
import { CarritoService } from '../../services/carrito/carrito-service';
import { of, throwError } from 'rxjs';
import Swal from 'sweetalert2';

describe('CarritoComponent', () => {
  let component: Carrito;
  let fixture: ComponentFixture<Carrito>;
  let mockCarrito: jasmine.SpyObj<CarritoService>;

  beforeEach(async () => {
    mockCarrito = jasmine.createSpyObj('CarritoService', [
      'getCarrito',
      'eliminarProducto',
      'vaciarCarrito',
      'crearPago',
      'comprar'
    ]);

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

  it('debería comprar por pasarela y redirigir si initPoint existe', () => {
    Object.defineProperty(window, 'location', {
      value: { href: '' },
      writable: true
    });

    mockCarrito.crearPago.and.returnValue(of({ initPoint: true, sandboxInitPoint: 'http://sandbox' }));

    component.carrito = [{ producto: { id: 1, titulo: 'Test', precio: 100 } }];
    component.comprarPasarelaDePago();

    expect(mockCarrito.crearPago).toHaveBeenCalled();
    expect(window.location.href).toBe('http://sandbox');
  });

  it('debería mostrar alerta si no hay initPoint en pasarela', () => {
    spyOn(window, 'alert');
    mockCarrito.crearPago.and.returnValue(of({}));

    component.carrito = [{ producto: { id: 1, titulo: 'Test', precio: 100 } }];
    component.comprarPasarelaDePago();

    expect(window.alert).toHaveBeenCalledWith('No se recibió la URL de pago.');
  });

  it('debería manejar error en pasarela de pago', () => {
    spyOn(window, 'alert');
    mockCarrito.crearPago.and.returnValue(throwError(() => new Error('Error')));

    component.comprarPasarelaDePago();

    expect(window.alert).toHaveBeenCalledWith('Error al generar el pago.');
  });

  it('debería comprar correctamente y vaciar carrito', () => {
    spyOn(Swal, 'fire');
    mockCarrito.comprar.and.returnValue(of({}));
    mockCarrito.getCarrito.and.returnValue(of({ items: [] }));

    component.carrito = [{ producto: { id: 1, precio: 100 } }];
    component.comprar();

    expect(mockCarrito.comprar).toHaveBeenCalled();
    expect(Swal.fire).toHaveBeenCalledWith(jasmine.objectContaining({ icon: 'success' }));
    expect(mockCarrito.getCarrito).toHaveBeenCalled();
  });

  it('debería manejar error al comprar', () => {
    spyOn(Swal, 'fire');
    mockCarrito.comprar.and.returnValue(throwError(() => ({ error: { message: 'Error compra' } })));

    component.comprar();

    expect(Swal.fire).toHaveBeenCalledWith(jasmine.objectContaining({ icon: 'error', text: 'Error compra' }));
  });

  it('debería retornar id en trackById', () => {
    const item = { id: 123 };
    const result = component.trackById(0, item);
    expect(result).toBe(123);
  });
});
