import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { HistorialCompra } from './historial-compra';
import { HistorialCompraService } from '../../services/historialCompra/historial-compra-service';

describe('HistorialCompraComponent', () => {
  let component: HistorialCompra;
  let fixture: ComponentFixture<HistorialCompra>;
  let mockHistorialService: jasmine.SpyObj<HistorialCompraService>;

  beforeEach(async () => {
    mockHistorialService = jasmine.createSpyObj('HistorialCompraService', ['obtenerHistorial']);

    // ✅ Simular localStorage ANTES de detectChanges
    spyOn(localStorage, 'getItem').and.callFake((key: string) => {
      if (key === 'cedula') return '123';
      return null;
    });

    await TestBed.configureTestingModule({
      imports: [HistorialCompra], // ✅ standalone component
      providers: [
        { provide: HistorialCompraService, useValue: mockHistorialService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HistorialCompra);
    component = fixture.componentInstance;
  });

  it('debería crear el componente', () => {
    expect(component).toBeTruthy();
  });

  it('debería cargar historial exitosamente en ngOnInit', () => {
    const mockData = [{ id: 1, productos: [{ precio: 100 }] }];
    mockHistorialService.obtenerHistorial.and.returnValue(of(mockData));

    component.ngOnInit();

    expect(mockHistorialService.obtenerHistorial).toHaveBeenCalledWith('123');
    expect(component.historial).toEqual(mockData);
    expect(component.cargando).toBeFalse();
  });

  it('debería manejar error al cargar historial', () => {
    mockHistorialService.obtenerHistorial.and.returnValue(throwError(() => new Error('Error')));

    component.ngOnInit();

    expect(mockHistorialService.obtenerHistorial).toHaveBeenCalledWith('123');
    expect(component.cargando).toBeFalse();
  });

  it('no debería llamar al servicio si no hay cedula en localStorage', () => {
    (localStorage.getItem as jasmine.Spy).and.returnValue(null);

    component.ngOnInit();

    expect(mockHistorialService.obtenerHistorial).not.toHaveBeenCalled();
  });

  it('debería retornar id en trackById', () => {
    const compra = { id: 456 };
    const result = component.trackById(0, compra);
    expect(result).toBe(456);
  });

  it('debería calcular total correctamente y formateado', () => {
    const compra = {
      productos: [
        { precio: 1000 },
        { precio: 2000 },
        { precio: 3000 }
      ]
    };

    const result = component.calcularTotal(compra);

    expect(result).toBe('6.000');
  });

  it('debería calcular total con valores nulos', () => {
    const compra = {
      productos: [
        { precio: 1000 },
        { precio: null },
        {}
      ]
    };

    const result = component.calcularTotal(compra);

    expect(result).toBe('1.000');
  });
});
