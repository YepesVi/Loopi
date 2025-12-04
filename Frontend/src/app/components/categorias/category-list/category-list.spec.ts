import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CategoryList } from './category-list';
import { CategoryService } from '../../../services/categorias/category.service';
import { of, throwError } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core'; // Para ignorar componentes hijos en el template

describe('CategoryList', () => {
  let component: CategoryList;
  let fixture: ComponentFixture<CategoryList>;
  let mockCategoryService: jasmine.SpyObj<CategoryService>;

  // Datos simulados
  const mockTree = [
    { id: 1, nombre: 'Tecnología', hijos: [] },
    { id: 2, nombre: 'Hogar', hijos: [] }
  ];

  beforeEach(async () => {
    // 1. Crear el Mock del Servicio
    mockCategoryService = jasmine.createSpyObj('CategoryService', [
      'getCategoriesTree',
      'createCategory',
      'updateCategory',
      'deleteCategory'
    ]);

    // Configurar retornos por defecto
    mockCategoryService.getCategoriesTree.and.returnValue(of(mockTree));

    await TestBed.configureTestingModule({
      imports: [CategoryList], // Componente Standalone va en imports
      providers: [
        { provide: CategoryService, useValue: mockCategoryService }
      ],
      // Usamos NO_ERRORS_SCHEMA para evitar errores por el componente hijo 'app-category-tree-item'
      // ya que estamos probando solo la lógica de la lista padre.
      schemas: [NO_ERRORS_SCHEMA] 
    }).compileComponents();

    fixture = TestBed.createComponent(CategoryList);
    component = fixture.componentInstance;
    fixture.detectChanges(); // Dispara ngOnInit
  });

  it('debería crearse e inicializar el árbol de categorías', () => {
    expect(component).toBeTruthy();
    expect(mockCategoryService.getCategoriesTree).toHaveBeenCalled();
    expect(component.categoryTree.length).toBe(2);
    expect(component.categoryTree[0].nombre).toBe('Tecnología');
    expect(component.isLoading).toBeFalse();
  });

  it('debería alternar el modo de edición', () => {
    expect(component.editingMode).toBeFalse();
    component.toggleEditingMode();
    expect(component.editingMode).toBeTrue();
    component.toggleEditingMode();
    expect(component.editingMode).toBeFalse();
  });

  // --- Pruebas de Lógica de Creación ---

  it('debería crear una categoría RAÍZ cuando parentId es 0', () => {
    const evento = { parentId: 0, name: 'Nueva Raíz' };
    const nuevaCategoria = { id: 3, nombre: 'Nueva Raíz' }; // Simulación de respuesta

    mockCategoryService.createCategory.and.returnValue(of(nuevaCategoria as any));

    component.onChildAdded(evento);

    // Verificamos que se llamó con parent: null
    expect(mockCategoryService.createCategory).toHaveBeenCalledWith(jasmine.objectContaining({
      nombre: 'Nueva Raíz',
      parent: null
    }));
    // Verificamos que recarga el árbol
    expect(mockCategoryService.getCategoriesTree).toHaveBeenCalledTimes(2); // 1 en init + 1 en success
  });

  it('debería crear una SUBCATEGORÍA cuando parentId es distinto de 0', () => {
    const evento = { parentId: 5, name: 'Subcategoria' };
    
    mockCategoryService.createCategory.and.returnValue(of({} as any));

    component.onChildAdded(evento);

    // Verificamos que se llamó con la referencia al padre
    expect(mockCategoryService.createCategory).toHaveBeenCalledWith(jasmine.objectContaining({
      nombre: 'Subcategoria',
      parent: { id: 5 }
    }));
  });

  // --- Pruebas con window.prompt y window.confirm ---

  it('debería actualizar categoría si el usuario ingresa un nombre en el prompt', () => {
    const categoriaMock = { id: 10, nombre: 'Viejo Nombre', hijos: [] };
    
    // Espiamos window.prompt y simulamos que el usuario escribe "Nuevo Nombre"
    spyOn(window, 'prompt').and.returnValue('Nuevo Nombre');
    mockCategoryService.updateCategory.and.returnValue(of(categoriaMock));

    component.onCategoryUpdated(categoriaMock);

    expect(mockCategoryService.updateCategory).toHaveBeenCalledWith(jasmine.objectContaining({
      id: 10,
      nombre: 'Nuevo Nombre'
    }));
    expect(mockCategoryService.getCategoriesTree).toHaveBeenCalled();
  });

  it('NO debería actualizar si el usuario cancela el prompt', () => {
    const categoriaMock = { id: 10, nombre: 'Viejo', hijos: [] };
    
    // Simulamos cancelar (null)
    spyOn(window, 'prompt').and.returnValue(null);

    component.onCategoryUpdated(categoriaMock);

    expect(mockCategoryService.updateCategory).not.toHaveBeenCalled();
  });

  it('debería eliminar categoría si el usuario confirma', () => {
    // Espiamos window.confirm y simulamos "Aceptar" (true)
    spyOn(window, 'confirm').and.returnValue(true);
    mockCategoryService.deleteCategory.and.returnValue(of(void 0));

    component.onCategoryDeleted(123);

    expect(mockCategoryService.deleteCategory).toHaveBeenCalledWith(123);
    expect(mockCategoryService.getCategoriesTree).toHaveBeenCalled();
  });

  it('NO debería eliminar si el usuario cancela la confirmación', () => {
    spyOn(window, 'confirm').and.returnValue(false);

    component.onCategoryDeleted(123);

    expect(mockCategoryService.deleteCategory).not.toHaveBeenCalled();
  });
});