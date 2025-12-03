import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductDetail } from './product-detail';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductosService } from '../../services/producto.service';
import { AuthService } from '../../services/auth.service';
import { CarritoService } from '../../services/carrito/carrito-service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import Swal from 'sweetalert2';

describe('ProductDetailComponent', () => {
  let component: ProductDetail;
  let fixture: ComponentFixture<ProductDetail>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockProductoService: jasmine.SpyObj<ProductosService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockCarritoService: jasmine.SpyObj<CarritoService>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockProductoService = jasmine.createSpyObj('ProductosService', ['getProductoPorId']);
    mockAuthService = jasmine.createSpyObj('AuthService', ['loggedInSignal']);
    mockCarritoService = jasmine.createSpyObj('CarritoService', ['agregarProducto']);

    await TestBed.configureTestingModule({
      imports: [ProductDetail, HttpClientTestingModule],
      providers: [
        { provide: Router, useValue: mockRouter },
        { provide: ProductosService, useValue: mockProductoService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: CarritoService, useValue: mockCarritoService },
        { provide: ActivatedRoute, useValue: { paramMap: of(new Map([['id', '1']])) } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductDetail);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('debería crear el componente', () => {
    expect(component).toBeTruthy();
  });

  it('debería cargar producto exitosamente', () => {
    const mockProducto: any = {
      id: 1,
      imagenes: [{ secureUrl: 'url1' }]
    };

    mockProductoService.getProductoPorId.and.returnValue(of(mockProducto));

    component.cargarProducto(1);

    expect(mockProductoService.getProductoPorId).toHaveBeenCalledWith(1);
    expect(component.producto).toEqual(mockProducto);
    expect(component.imagenSeleccionada).toBe('url1');
    expect(component.loading).toBeFalse();
  });

  it('debería manejar error al cargar producto', () => {
    spyOn(Swal, 'fire');
    mockProductoService.getProductoPorId.and.returnValue(throwError(() => new Error('Error')));

    component.cargarProducto(1);

    expect(Swal.fire).toHaveBeenCalledWith('Error', 'No se pudo cargar el producto', 'error');
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/home']);
  });

  it('debería redirigir a home', () => {
    component.redirigirAHome();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/home']);
  });

  it('debería seleccionar imagen', () => {
    const imagen: any = { secureUrl: 'imgX' };
    component.seleccionarImagen(imagen);
    expect(component.imagenSeleccionada).toBe('imgX');
  });

  it('no debería agregar al carrito si no hay producto', () => {
    component.producto = null;
    component.agregarAlCarrito();
    expect(mockCarritoService.agregarProducto).not.toHaveBeenCalled();
  });

  it('debería mostrar alerta si no está logueado', async () => {
    spyOn(Swal, 'fire').and.returnValue(Promise.resolve({
      isConfirmed: true,
      isDenied: false,
      isDismissed: false
    }));
    mockAuthService.loggedInSignal.and.returnValue(false);
    component.producto = { id: 99 } as any;

    await component.agregarAlCarrito();

    expect(Swal.fire).toHaveBeenCalledWith(jasmine.objectContaining({
      title: '🔒 Acceso requerido'
    }));
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/login-register']);
  });

 
  it('no debería enviar reporte si no hay producto', () => {
    spyOn(console, 'log');
    component.producto = null;
    component.enviarReporte();
    expect(console.log).toHaveBeenCalledWith('❌ No hay ID de producto');
  });

  it('no debería enviar reporte si mensaje está vacío', () => {
    spyOn(Swal, 'fire');
    component.producto = { id: 1 } as any;
    component.mensajeReporte = '   ';
    component.enviarReporte();
    expect(Swal.fire).toHaveBeenCalledWith('Advertencia', 'Debes escribir un mensaje', 'warning');
  });

  it('debería enviar reporte correctamente', () => {
    spyOn(Swal, 'fire');
    component.producto = { id: 1 } as any;
    component.mensajeReporte = 'Hay un error';

    component.enviarReporte();

    const req = httpMock.expectOne('http://localhost:8081/api/notificacion-reporte');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      productId: 1,
      reporterMessage: 'Hay un error'
    });

    req.flush({});
    expect(component.mostrarModalReporte).toBeFalse();
    expect(component.mensajeReporte).toBe('');
  });

  
});
