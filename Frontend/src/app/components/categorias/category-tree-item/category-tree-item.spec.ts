import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CategoryTreeItem } from './category-tree-item';
import { CategoryService } from '../../../services/categorias/category.service';
import { PopupService } from '../../../services/categorias/popup';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('CategoryTreeItem', () => {
  let component: CategoryTreeItem;
  let fixture: ComponentFixture<CategoryTreeItem>;
  
  // Mocks
  let mockCategoryService: jasmine.SpyObj<CategoryService>;
  let mockPopupService: jasmine.SpyObj<PopupService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockCategoryService = jasmine.createSpyObj('CategoryService', ['updateCategory']);
    mockPopupService = jasmine.createSpyObj('PopupService', ['closeCategoryPopup']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [CategoryTreeItem],
      providers: [
        { provide: CategoryService, useValue: mockCategoryService },
        { provide: PopupService, useValue: mockPopupService },
        { provide: Router, useValue: mockRouter }
      ],
      schemas: [NO_ERRORS_SCHEMA] 
    }).compileComponents();

    fixture = TestBed.createComponent(CategoryTreeItem);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse correctamente', () => {
    expect(component).toBeTruthy();
  });

  // --- Pruebas de Navegación ---

  it('debería navegar a productos filtrados y cerrar el popup', () => {
    const catId = 5;
    component.filtrarPorCategoria(catId);

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/productos'], { 
      queryParams: { categoriaId: catId } 
    });
    expect(mockPopupService.closeCategoryPopup).toHaveBeenCalled();
  });

  // --- Pruebas de Lógica de UI (Expansión) ---

  it('debería alternar la expansión de categorías', () => {
    const catId = 10;
    
    // Inicialmente no expandido
    expect(component.isExpanded(catId)).toBeFalse();

    // Expandir
    component.toggleExpansion(catId);
    expect(component.isExpanded(catId)).toBeTrue();

    // Colapsar
    component.toggleExpansion(catId);
    expect(component.isExpanded(catId)).toBeFalse();
  });

  // --- Pruebas de Inputs y Outputs (Añadir Hijo) ---

  it('debería emitir addChild cuando se añade una subcategoría válida', () => {
    // Espiamos el EventEmitter
    spyOn(component.addChild, 'emit');

    component.newCategoryName = '  Nueva Sub  '; // Con espacios para probar trim()
    component.emitAddChild(20);

    expect(component.addChild.emit).toHaveBeenCalledWith({
      parentId: 20,
      name: 'Nueva Sub'
    });
    expect(component.newCategoryName).toBe(''); // Debe limpiarse
  });

  it('NO debería emitir addChild si el nombre está vacío', () => {
    spyOn(component.addChild, 'emit');

    component.newCategoryName = '';
    component.emitAddChild(20);

    expect(component.addChild.emit).not.toHaveBeenCalled();
  });

  // --- Pruebas de Añadir Raíz (ID 0) ---

  it('debería emitir addChild con ID 0 al crear raíz', () => {
    spyOn(component.addChild, 'emit');
    spyOn(component.actionComplete, 'emit');

    component.newRootCategoryName = 'Raiz Test';
    component.emitAddRoot();

    expect(component.addChild.emit).toHaveBeenCalledWith({
      parentId: 0,
      name: 'Raiz Test'
    });
    expect(component.actionComplete.emit).toHaveBeenCalled();
  });

  // --- Pruebas de Edición (Llamada al Servicio) ---

  it('debería guardar nombre editado llamando al servicio', () => {
    const catMock = { id: 1, nombre: 'Viejo', hijos: [] };
    const nuevoNombre = 'Editado';
    
    component.newCategoryName = nuevoNombre;
    component.activeEditId = 1; // Simulamos que estamos editando este ID

    mockCategoryService.updateCategory.and.returnValue(of({ ...catMock, nombre: nuevoNombre }));
    spyOn(component.actionComplete, 'emit');

    component.saveEditedName(catMock);

    expect(mockCategoryService.updateCategory).toHaveBeenCalledWith(jasmine.objectContaining({
      id: 1,
      nombre: nuevoNombre
    }));
    // Verificamos que se actualizó el objeto local
    expect(catMock.nombre).toBe(nuevoNombre);
    expect(component.actionComplete.emit).toHaveBeenCalled();
  });

  it('NO debería llamar al servicio si el nombre no cambió', () => {
    const catMock = { id: 1, nombre: 'Igual', hijos: [] };
    component.newCategoryName = 'Igual';
    
    component.saveEditedName(catMock);

    expect(mockCategoryService.updateCategory).not.toHaveBeenCalled();
  });
});