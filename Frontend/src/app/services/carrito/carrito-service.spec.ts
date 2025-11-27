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

 

 it('debería agregar producto con cedula en body', () => {
  const mockResponse = { mensaje: 'Producto agregado' };

  service.agregarProducto(12345).subscribe(res => {
    expect(res.mensaje).toBe('Producto agregado');
  });

  const req = httpMock.expectOne('http://localhost:8081/api/carrinho/agregar/12345');
  expect(req.request.method).toBe('POST');
  expect(req.request.body).toEqual({ cedula: '123' });
  req.flush(mockResponse);
});


  it('debería eliminar producto por itemId', () => {
    const mockResponse = { mensaje: 'Producto eliminado' };

    service.eliminarProducto(7).subscribe(res => {
      expect(res.mensaje).toBe('Producto eliminado');
    });

    const req = httpMock.expectOne('http://localhost:8081/api/carrito/item/7');
    expect(req.request.method).toBe('DELETE');
    req.flush(mockResponse);
  });
});
