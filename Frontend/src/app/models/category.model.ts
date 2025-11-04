
export interface Categoria {
  id: number;
  nombre: string;
  // Debe coincidir con el JSON del backend: "hijos"
  hijos: Categoria[];
}