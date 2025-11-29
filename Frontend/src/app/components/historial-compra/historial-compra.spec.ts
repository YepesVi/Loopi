import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HistorialCompra } from './historial-compra';

describe('HistorialCompra', () => {
  let component: HistorialCompra;
  let fixture: ComponentFixture<HistorialCompra>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HistorialCompra]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HistorialCompra);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
