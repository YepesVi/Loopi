package com.example.demo.service;

import com.example.demo.entity.Categoria;
import com.example.demo.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoriaPadre;
    private Categoria categoriaHija;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        categoriaPadre = new Categoria();
        categoriaPadre.setId(1L);
        categoriaPadre.setNombre("Electrónica");

        categoriaHija = new Categoria();
        categoriaHija.setId(2L);
        categoriaHija.setNombre("Celulares");
        categoriaHija.setParent(categoriaPadre);
    }

    // --- TEST: findAll ---
    @Test
    void findAll_retornaLista() {
        when(categoriaRepository.findAll()).thenReturn(List.of(categoriaPadre));
        List<Categoria> resultado = categoriaService.findAll();
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
    }

    // --- TEST: findById ---
    @Test
    void findById_existe_retornaCategoria() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaPadre));
        Optional<Categoria> resultado = categoriaService.findById(1L);
        assertTrue(resultado.isPresent());
        assertEquals("Electrónica", resultado.get().getNombre());
    }

    // --- TEST: save (Crear Raíz) ---
    @Test
    void save_raiz_ok() {
        Categoria nueva = new Categoria();
        nueva.setNombre("Ropa");
        
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(nueva);
        
        Categoria resultado = categoriaService.save(nueva);
        assertNull(resultado.getParent());
    }

    // --- TEST: save (Crear Hija) ---
    @Test
    void save_hija_ok() {
        Categoria nuevaHija = new Categoria();
        nuevaHija.setNombre("Camisetas");
        nuevaHija.setParent(categoriaPadre); // ID 1

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaPadre));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(nuevaHija);

        Categoria resultado = categoriaService.save(nuevaHija);
        assertEquals(categoriaPadre, resultado.getParent());
    }

    @Test
    void save_hija_padreNoExiste_lanzarExcepcion() {
        Categoria huérfana = new Categoria();
        Categoria padreInexistente = new Categoria();
        padreInexistente.setId(99L);
        huérfana.setParent(padreInexistente);

        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> categoriaService.save(huérfana));
    }

    // --- TEST: update (Cambiar Nombre) ---
    @Test
    void update_nombre_ok() {
        Categoria updates = new Categoria();
        updates.setNombre("Electrónica Nueva");
        
        // Simular que no cambia de padre (parent es null en updates y en existente)
        // Ojo: Tu lógica compara IDs. Si el existente es raíz (parent null) y el update es null, no entra al bloque de movimiento.
        Categoria existenteRaiz = new Categoria();
        existenteRaiz.setId(1L);
        existenteRaiz.setNombre("Viejo");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existenteRaiz));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(i -> i.getArgument(0));

        Categoria resultado = categoriaService.update(1L, updates);
        assertEquals("Electrónica Nueva", resultado.getNombre());
    }

    // --- TEST: update (Mover Categoría - Movimiento Seguro) ---
    @Test
    void update_mover_ok() {
        // Mover "Celulares" (2) para que sea hija de "Ropa" (3) - Supongamos que es válido
        Categoria ropa = new Categoria(); ropa.setId(3L);
        
        Categoria updates = new Categoria();
        updates.setParent(ropa); // Nuevo padre

        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoriaHija)); // La que se mueve
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(ropa)); // El destino
        
        // Simular que NO es descendiente (movimiento seguro)
        when(categoriaRepository.isDescendantOf(2L, 3L)).thenReturn(false);
        when(categoriaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Categoria resultado = categoriaService.update(2L, updates);
        assertEquals(3L, resultado.getParent().getId());
    }

    // --- TEST: update (Mover Categoría - Ciclo Detectado) ---
    @Test
    void update_mover_ciclo_lanzarExcepcion() {
        // Intentar mover Padre (1) para que sea hijo de su Hija (2)
        Categoria updates = new Categoria();
        updates.setParent(categoriaHija);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaPadre));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoriaHija));
        
        // Simular que 2 ES descendiente de 1 (Ciclo!)
        when(categoriaRepository.isDescendantOf(1L, 2L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> categoriaService.update(1L, updates));
    }

    // --- TEST: delete ---
    @Test
    void deleteById_ok() {
        doNothing().when(categoriaRepository).deleteById(1L);
        categoriaService.deleteById(1L);
        verify(categoriaRepository, times(1)).deleteById(1L);
    }

    // --- TEST: Consultas de Árbol ---
    @Test
    void findRootCategories_ok() {
        when(categoriaRepository.findByParentIsNull()).thenReturn(List.of(categoriaPadre));
        List<Categoria> roots = categoriaService.findRootCategories();
        assertEquals(1, roots.size());
    }
}