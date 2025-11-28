import { TestBed } from '@angular/core/testing';

import { HistorialCompraService } from './historial-compra-service';

describe('HistorialCompraService', () => {
  let service: HistorialCompraService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(HistorialCompraService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
