package bo.umss.mercado.nucleo.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias y de Caja Negra - PruebaChiCuadrada")
class PruebaChiCuadradaTest {

    @Test
    @DisplayName("Caja Negra [Valores Límite]: cruzar con listas vacías o tamaño 0")
    void cruzar_listasVacias_retornaResultadoVacio() {
        ResultadoChiCuadrada r = PruebaChiCuadrada.cruzar(Collections.emptyList(), Collections.emptyList());
        assertEquals(0, r.n());
        assertTrue(Double.isNaN(r.chiCuadrada()));
        assertTrue(Double.isNaN(r.valorP()));
    }

    @Test
    @DisplayName("Caja Negra [Valores Límite]: cruzar con una sola categoría (r < 2 o c < 2)")
    void cruzar_categoriaUnica_retornaVacio() {
        List<String> colA = List.of("MASCULINO", "MASCULINO", "MASCULINO");
        List<String> colB = List.of("SI", "NO", "SI");

        ResultadoChiCuadrada r = PruebaChiCuadrada.cruzar(colA, colB);
        // r < 2 -> no se puede formar tabla de contingencia 2x2 mínima
        assertEquals(0, r.n());
        assertTrue(Double.isNaN(r.chiCuadrada()));
    }

    @Test
    @DisplayName("Caja Negra [Partición Equivalente]: ignora pares con datos nulos o vacíos")
    void cruzar_conValoresVaciosONulos_losIgnora() {
        List<String> colA = List.of("A", "", "B", "A", "B");
        List<String> colB = List.of("X", "Y", "", "Y", "X");

        // Pares válidos: (A, X), (A, Y), (B, X) -> total = 3
        ResultadoChiCuadrada r = PruebaChiCuadrada.cruzar(colA, colB);
        assertEquals(3, r.n());
        assertEquals(2, r.filas().size());
        assertEquals(2, r.columnas().size());
    }

    @Test
    @DisplayName("Unitaria: tabla 2x2 con asociación perfecta produce chi alto y p-valor significativo")
    void cruzar_asociacionFuerte_calculaChiCuadradaYVCramer() {
        // Asociación perfecta: Grupo A siempre elige X, Grupo B siempre elige Y
        List<String> colA = new ArrayList<>();
        List<String> colB = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            colA.add("GRUPO_A");
            colB.add("OPCION_X");
        }
        for (int i = 0; i < 50; i++) {
            colA.add("GRUPO_B");
            colB.add("OPCION_Y");
        }

        ResultadoChiCuadrada r = PruebaChiCuadrada.cruzar(colA, colB);

        assertEquals(100, r.n());
        assertEquals(1, r.gradosLibertad()); // (2-1)*(2-1) = 1
        assertTrue(r.chiCuadrada() > 50.0, "Chi cuadrada debe ser muy alta para correlación perfecta");
        assertTrue(r.valorP() < 0.001, "El p-valor debe ser altamente significativo");
        assertEquals(1.0, r.vCramer(), 1e-2, "V de Cramer debe ser 1.0 para asociación perfecta");
        assertEquals("grande", r.intensidadVCramer());
    }

    @Test
    @DisplayName("Unitaria: tabla 2x2 equilibrada produce chi cercano a 0 y p-valor no significativo")
    void cruzar_variablesIndependientes_producePValorAlto() {
        // Distribución uniforme e independiente
        List<String> colA = new ArrayList<>();
        List<String> colB = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            colA.add("A"); colB.add("X");
            colA.add("A"); colB.add("Y");
            colA.add("B"); colB.add("X");
            colA.add("B"); colB.add("Y");
        }

        ResultadoChiCuadrada r = PruebaChiCuadrada.cruzar(colA, colB);
        assertEquals(100, r.n());
        assertEquals(0.0, r.chiCuadrada(), 1e-4);
        assertTrue(r.valorP() > 0.9, "Para distribución perfectamente simétrica, p-valor ~ 1.0");
        assertEquals("despreciable", r.intensidadVCramer());
    }
}
