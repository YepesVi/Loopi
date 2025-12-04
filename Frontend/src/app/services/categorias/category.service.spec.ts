import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CategoryService } from './category.service';
import { Categoria, CrudCategoria } from '../../models/category.model';

describe('CategoryService', () => {
  let service: CategoryService;
  let httpMock: HttpTestingController;
  const baseUrl = 'http://localhost:8081/api/categorias';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CategoryService]
    });
    service = TestBed.inject(CategoryService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // Verifica que no queden peticiones pendientes
  });

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('debería obtener el árbol de categorías (GET)', () => {
    const mockCategories: Categoria[] = [
      { id: 1, nombre: 'Raiz', hijos: [] }
    ];

    service.getCategoriesTree().subscribe(categories => {
      expect(categories.length).toBe(1);
      expect(categories).toEqual(mockCategories);
    });

    const req = httpMock.expectOne(`${baseUrl}/roots`);
    expect(req.request.method).toBe('GET');
    req.flush(mockCategories);
  });

  it('debería obtener una categoría por ID (GET)', () => {
    const mockCategory: Categoria = { id: 1, nombre: 'Test', hijos: [] };

    service.getCategoriaPorId(1).subscribe(cat => {
      expect(cat).toEqual(mockCategory);
    });

    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockCategory);
  });

  it('debería crear una categoría (POST)', () => {
    const newCategory: CrudCategoria = { id: null, nombre: 'Nueva', parent: null };
    const responseCategory: Categoria = { id: 2, nombre: 'Nueva', hijos: [] };

    service.createCategory(newCategory).subscribe(cat => {
      expect(cat).toEqual(responseCategory);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newCategory);
    req.flush(responseCategory);
  });

  it('debería actualizar una categoría (PUT)', () => {
    const updateData: CrudCategoria = { id: 1, nombre: 'Editado', parent: null };
    const responseCategory: Categoria = { id: 1, nombre: 'Editado', hijos: [] };

    service.updateCategory(updateData).subscribe(cat => {
      expect(cat).toEqual(responseCategory);
    });

    // Ojo: tu servicio usa la ruta `${baseUrl}/${category.id}/name`
    const req = httpMock.expectOne(`${baseUrl}/1/name`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateData);
    req.flush(responseCategory);
  });

  it('debería eliminar una categoría (DELETE)', () => {
    service.deleteCategory(1).subscribe(res => {
      expect(res).toBeNull(); // delete devuelve void/null
    });

    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null); // Respuesta vacía exitosa
  });
});