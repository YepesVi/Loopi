import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CarritoService } from './carrito-service';

describe('CarritoService', () => {
  let service: CarritoService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CarritoService]
    });

    service = TestBed.inject(CarritoService);
    httpMock = TestBed.inject(HttpTestingController);

    // ✅ Simular localStorage
    spyOn(localStorage, 'getItem').and.callFake((key: string) => {
      if (key === 'cedula') return '123';
      return null;
    });
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('debería obtener carrito y actualizar cantidad', () => {
    const mockResponse = { items: [{ id: 1 }, { id: 2 }] };

    service.getCarrito().subscribe(res => {
      expect(res.items.length).toBe(2);
    });

    const req = httpMock.expectOne('http://localhost:8081/api/carrito/123');
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('debería devolver carrito vacío si no hay cedula', (done) => {
    (localStorage.getItem as jasmine.Spy).and.returnValue(null);

    service.getCarrito().subscribe(res => {
      expect(res.items).toEqual([]);
      done();
    });
  });

  it('debería crear carrito', () => {
    const mockResponse = { mensaje: 'Carrito creado' };

    service.crearCarrito('123').subscribe(res => {
      expect(res.mensaje).toBe('Carrito creado');
    });

    const req = httpMock.expectOne('http://localhost:8081/api/carrito/crear/123');
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });


  it('debería vaciar carrito', () => {
    const mockResponse = { mensaje: 'Carrito vaciado' };

    service.vaciarCarrito().subscribe(res => {
      expect(res.mensaje).toBe('Carrito vaciado');
    });

    const req = httpMock.expectOne('http://localhost:8081/api/carrito/vaciar/123');
    expect(req.request.method).toBe('DELETE');
    req.flush(mockResponse);
  });

  it('debería resetear cantidad', () => {
    let cantidad: number | undefined;
    service.cantidad$.subscribe(val => cantidad = val);

    service.resetCantidad();
    expect(cantidad).toBe(0);
  });

  it('debería crear pago', () => {
    const mockResponse = { initPoint: 'url' };
    const carrito = { items: [{ productoId: 1 }] };

    service.crearPago(carrito).subscribe(res => {
      expect(res.initPoint).toBe('url');
    });

    const req = httpMock.expectOne('http://localhost:8081/api/pago/crear');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(carrito);
    req.flush(mockResponse);
  });

  it('debería comprar carrito', () => {
    const mockResponse = { mensaje: 'Compra realizada' };

    service.comprar({ items: [] }).subscribe(res => {
      expect(res.mensaje).toBe('Compra realizada');
    });

    const req = httpMock.expectOne('http://localhost:8081/api/carrito/comprar/123');
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('debería no comprar si no hay cedula', (done) => {
    (localStorage.getItem as jasmine.Spy).and.returnValue(null);

    service.comprar({ items: [] }).subscribe({
      complete: () => {
        // No debería emitir nada
        done();
      }
    });

    httpMock.expectNone('http://localhost:8081/api/carrito/comprar/123');
  });
});
