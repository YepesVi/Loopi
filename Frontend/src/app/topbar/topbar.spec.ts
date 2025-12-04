import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Topbar } from './topbar';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UsuarioService } from '../services/usuario.service';
import { PopupService } from '../services/categorias/popup';
import { CarritoService } from '../services/carrito/carrito-service';
import { of, BehaviorSubject } from 'rxjs';
import { FormsModule } from '@angular/forms'; // Para ngModel

describe('Topbar', () => {
  let component: Topbar;
  let fixture: ComponentFixture<Topbar>;
  
  // Spies para dependencias
  let mockRouter: jasmine.SpyObj<Router>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockUsuarioService: jasmine.SpyObj<UsuarioService>;
  let mockPopupService: jasmine.SpyObj<PopupService>;
  let mockCarritoService: jasmine.SpyObj<CarritoService>;

  // Subjects para simular observables
  const nombreUsuarioSubject = new BehaviorSubject<string>('Usuario Test');
  const fotoUsuarioSubject = new BehaviorSubject<string>('foto.jpg');
  const cantidadCarritoSubject = new BehaviorSubject<number>(5);

  beforeEach(async () => {
    // 1. Configurar Mocks
    mockRouter = jasmine.createSpyObj('Router', ['navigate', 'navigateByUrl'], {
      events: of(null) // Mockear router.events
    });
    mockAuthService = jasmine.createSpyObj('AuthService', ['loggedInSignal', 'logout']);
    
    mockUsuarioService = jasmine.createSpyObj('UsuarioService', ['actualizarNombre', 'actualizarFoto'], {
      nombreUsuario$: nombreUsuarioSubject.asObservable(),
      fotoUsuario$: fotoUsuarioSubject.asObservable()
    });

    mockPopupService = jasmine.createSpyObj('PopupService', ['toggleCategoryPopup']);
    
    mockCarritoService = jasmine.createSpyObj('CarritoService', ['getCarrito', 'resetCantidad'], {
      cantidad$: cantidadCarritoSubject.asObservable()
    });
    mockCarritoService.getCarrito.and.returnValue(of(null)); // Retorno dummy

    // 2. Configurar TestBed
    await TestBed.configureTestingModule({
      imports: [Topbar, FormsModule], // Componente Standalone
      providers: [
        { provide: Router, useValue: mockRouter },
        { provide: AuthService, useValue: mockAuthService },
        { provide: UsuarioService, useValue: mockUsuarioService },
        { provide: PopupService, useValue: mockPopupService },
        { provide: CarritoService, useValue: mockCarritoService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Topbar);
    component = fixture.componentInstance;
    
    // Simular estado de login inicial
    mockAuthService.loggedInSignal.and.returnValue(true);
    
    fixture.detectChanges(); // ngOnInit
  });

  it('debería crearse e inicializar suscripciones', () => {
    expect(component).toBeTruthy();
    expect(component.nombreUsuario).toBe('Usuario Test');
    expect(component.fotoUsuario).toBe('foto.jpg');
    expect(component.cantidadCarrito).toBe(5);
    expect(component.sesionActiva).toBeTrue();
  });

  it('debería navegar a productos con query params al buscar', () => {
    component.searchQuery = ' laptop ';
    component.searchProduct();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/productos'], { 
      queryParams: { q: 'laptop' },
      queryParamsHandling: 'merge'
    });
  });

  it('debería limpiar búsqueda si el query está vacío', () => {
    component.searchQuery = '';
    component.searchProduct();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/productos'], { 
      queryParams: { q: null },
      queryParamsHandling: 'merge'
    });
  });

  it('debería cerrar sesión correctamente (logout)', fakeAsync(() => {
    component.logout();
    
    expect(component.cerrandoSesion).toBeTrue(); // Toast visible
    
    // Simular paso del tiempo (1500ms)
    tick(1500);

    expect(mockAuthService.logout).toHaveBeenCalled();
    expect(mockUsuarioService.actualizarNombre).toHaveBeenCalledWith('Usuario');
    expect(mockCarritoService.resetCantidad).toHaveBeenCalled();
    expect(component.cerrandoSesion).toBeFalse();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/home']);
  }));

  it('debería alternar el popup de categorías', () => {
    component.toggleCategoryPopup();
    expect(mockPopupService.toggleCategoryPopup).toHaveBeenCalled();
  });

  it('irAlCarrito debería redirigir a login si no hay cédula', () => {
    spyOn(localStorage, 'getItem').and.returnValue(null);
    component.irAlCarrito();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/login-register']);
  });

  it('irAlCarrito debería navegar al carrito si hay cédula', () => {
    spyOn(localStorage, 'getItem').and.returnValue('12345');
    component.irAlCarrito();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/carrito']);
  });
});