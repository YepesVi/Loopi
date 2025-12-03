import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HistorialCompraService } from './historial-compra-service';

describe('HistorialCompraService', () => {
  let service: HistorialCompraService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [HistorialCompraService]
    });

    service = TestBed.inject(HistorialCompraService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('debería crearse el servicio', () => {
    expect(service).toBeTruthy();
  });

  it('debería obtener historial por cedula', () => {
    const mockResponse = [
      { id: 1, productos: [{ precio: 1000 }] },
      { id: 2, productos: [{ precio: 2000 }] }
    ];

    service.obtenerHistorial('123').subscribe(res => {
      expect(res.length).toBe(2);
      expect(res[0].id).toBe(1);
      expect(res[1].productos[0].precio).toBe(2000);
    });

    const req = httpMock.expectOne('http://localhost:8081/api/historial-compra/123');
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('debería manejar respuesta vacía', () => {
    const mockResponse: any[] = [];

    service.obtenerHistorial('999').subscribe(res => {
      expect(res).toEqual([]);
    });

    const req = httpMock.expectOne('http://localhost:8081/api/historial-compra/999');
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });
});
