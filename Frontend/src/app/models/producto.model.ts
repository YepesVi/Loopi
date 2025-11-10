import { Categoria } from "./category.model";

export interface Producto {
  id?: number;
  titulo: string;
  descripcion: string;
  categoria: Categoria;
  precio: number;
  estado: string;
  propietarioId: number;
  fotos?: string;
  fechaCreacion?: string;
}