import { Categoria } from "./category.model";
import { Imagen } from "./imagen.model";

export interface Producto {
  id?: number;
  titulo: string;
  descripcion: string;
  categoria: Categoria;
  precio: number;
  estado: string;
  propietarioId: number; 
  imagenes: Imagen[];
  fechaCreacion?: string;
}