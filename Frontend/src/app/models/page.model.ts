/**
 * Interfaz genérica para la paginación de Spring Boot.
 * <T> representa el tipo de contenido en la página (ej. Producto).
 */
export interface Page<T> {
  /**
   * El array de elementos para la página actual.
   */
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}