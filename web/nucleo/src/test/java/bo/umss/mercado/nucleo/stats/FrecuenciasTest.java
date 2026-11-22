package bo.umss.mercado.nucleo.stats;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias y de Caja Negra - Frecuencias")
class FrecuenciasTest {

    @Test
    @DisplayName("Caja Negra [Partición Equivalente]: contar agrupa nulos y vacíos como (VACIO)")
    void contar_conNulosYVacios_agrupaComoVacio() {
        List<String> respuestas = Arrays.asList("IA", null, "", "WEB", "IA", "  ", null);
        // Note: Frecuencias.contar treats null and empty string as (VACIO)
        Map<String, Integer> conteo = Frecuencias.contar(respuestas);

        assertEquals(2, conteo.get("IA"));
        assertEquals(1, conteo.get("WEB"));
        assertEquals(3, conteo.get(Frecuencias.ETIQUETA_VACIO));
        assertEquals(1, conteo.get("  ")); // whitespace without trim
    }

    @Test
    @DisplayName("Caja Negra [Valores Límite]: contar con lista vacía retorna mapa vacío")
    void contar_listaVacia_retornaMapaVacio() {
        Map<String, Integer> conteo = Frecuencias.contar(Collections.emptyList());
        assertTrue(conteo.isEmpty());
    }

    @Test
    @DisplayName("Caja Negra: moda identifica la categoría más frecuente")
    void moda_mapaValido_retornaCategoriaMasFrecuente() {
        Map<String, Integer> conteo = Map.of("IA", 15, "DATA", 8, "WEB", 22);
        assertEquals("WEB", Frecuencias.moda(conteo));
    }

    @Test
    @DisplayName("Caja Negra [Valores Límite]: moda con mapa vacío retorna cadena vacía")
    void moda_mapaVacio_retornaCadenaVacia() {
        assertEquals("", Frecuencias.moda(Collections.emptyMap()));
    }

    @Test
    @DisplayName("Caja Negra [Valores Límite]: histograma con lista vacía o intervalos <= 0")
    void histograma_casosLimite_retornaListaVacia() {
        assertTrue(Frecuencias.histograma(Collections.emptyList(), 5).isEmpty());
        assertTrue(Frecuencias.histograma(List.of(1.0, 2.0, 3.0), 0).isEmpty());
        assertTrue(Frecuencias.histograma(List.of(1.0, 2.0, 3.0), -2).isEmpty());
    }

    @Test
    @DisplayName("Caja Negra [Valores Límite]: histograma con todos los valores idénticos")
    void histograma_valoresIdenticos_retornaUnaBarraConTotal() {
        List<Double> valores = List.of(10.0, 10.0, 10.0);
        List<IntervaloHistograma> barras = Frecuencias.histograma(valores, 4);

        assertEquals(1, barras.size());
        assertEquals(10.0, barras.get(0).inicio(), 1e-6);
        assertEquals(10.0, barras.get(0).fin(), 1e-6);
        assertEquals(3, barras.get(0).conteo());
    }

    @Test
    @DisplayName("Unitaria: histograma distribuye correctamente y el máximo cae en el último intervalo")
    void histograma_distribucionNormal_distribuyeCorrectamente() {
        List<Double> valores = List.of(0.0, 2.5, 5.0, 7.5, 10.0);
        int intervalos = 5; // ancho = 2.0 -> [0-2), [2-4), [4-6), [6-8), [8-10]
        List<IntervaloHistograma> barras = Frecuencias.histograma(valores, intervalos);

        assertEquals(5, barras.size());
        int totalConteo = barras.stream().mapToInt(IntervaloHistograma::conteo).sum();
        assertEquals(5, totalConteo);

        // 10.0 debe estar en la última barra
        assertEquals(1, barras.get(4).conteo());
    }
}
