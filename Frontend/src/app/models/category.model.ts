
export interface Categoria {
  id: number;
  nombre: string;
  // Debe coincidir con el JSON del backend: "hijos"
  hijos?: Categoria[];
}

export interface CrudCategoria {
  id: number | null; // Null al crear
  nombre: string;
  parent?: ParentReference | null;
}

export interface ParentReference {
  id: number;
}