package bo.umss.mercado.nucleo.stats;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias y de Caja Negra - Numeros")
class NumerosTest {

    @Test
    @DisplayName("Caja Negra [Valores Límite]: Lista vacía debe retornar n=0 y estadísticos en NaN")
    void resumir_listaVacia_retornaNanYConteoCero() {
        ResumenNumerico r = Numeros.resumir(Collections.emptyList());

        assertEquals(0, r.totalDatos());
        assertTrue(Double.isNaN(r.media()));
        assertTrue(Double.isNaN(r.mediana()));
        assertTrue(Double.isNaN(r.desv()));
        assertTrue(Double.isNaN(r.minimo()));
        assertTrue(Double.isNaN(r.maximo()));
        assertTrue(Double.isNaN(r.q1()));
        assertTrue(Double.isNaN(r.q2()));
        assertTrue(Double.isNaN(r.q3()));
    }

    @Test
    @DisplayName("Caja Negra [Valores Límite]: Un solo elemento retorna estadísticas base (desv=0.0)")
    void resumir_unSoloElemento_desviacionNan() {
        ResumenNumerico r = Numeros.resumir(List.of(25.0));

        assertEquals(1, r.totalDatos());
        assertEquals(25.0, r.media(), 1e-6);
        assertEquals(25.0, r.mediana(), 1e-6);
        assertEquals(0.0, r.desv(), 1e-6);
        assertEquals(25.0, r.minimo(), 1e-6);
        assertEquals(25.0, r.maximo(), 1e-6);
    }

    @Test
    @DisplayName("Caja Negra [Partición Equivalente]: Conjunto con valores válidos calcula estadísticos esperados")
    void resumir_conjuntoConocido_calculaEstadisticosCorrectamente() {
        List<Double> datos = List.of(10.0, 20.0, 30.0, 40.0, 50.0);
        ResumenNumerico r = Numeros.resumir(datos);

        assertEquals(5, r.totalDatos());
        assertEquals(30.0, r.media(), 1e-6);
        assertEquals(30.0, r.mediana(), 1e-6);
        assertEquals(10.0, r.minimo(), 1e-6);
        assertEquals(50.0, r.maximo(), 1e-6);
        assertEquals(15.811388, r.desv(), 1e-4);
        assertEquals(30.0, r.q2(), 1e-6);
    }

    @Test
    @DisplayName("Unitaria: Valores idénticos deben tener desviación estándar cero")
    void resumir_valoresIdenticos_desviacionCero() {
        List<Double> datos = List.of(7.0, 7.0, 7.0, 7.0);
        ResumenNumerico r = Numeros.resumir(datos);

        assertEquals(4, r.totalDatos());
        assertEquals(7.0, r.media(), 1e-6);
        assertEquals(0.0, r.desv(), 1e-6);
        assertEquals(7.0, r.minimo(), 1e-6);
        assertEquals(7.0, r.maximo(), 1e-6);
    }

    @Test
    @DisplayName("Caja Negra [Valores Límite]: promedio con lista vacía retorna NaN")
    void promedio_listaVacia_retornaNan() {
        assertTrue(Double.isNaN(Numeros.promedio(Collections.emptyList())));
    }

    @Test
    @DisplayName("Unitaria: promedio calcula correctamente con positivos y negativos")
    void promedio_valoresPositivosYNegativos_calculaMediaCorrecta() {
        List<Double> datos = List.of(-10.0, 10.0, 20.0, -20.0, 5.0);
        assertEquals(1.0, Numeros.promedio(datos), 1e-6);
    }

    @Test
    @DisplayName("Caja Negra: ordenar no debe mutar la lista original y debe ordenar ascendentemente")
    void ordenar_listaDesordenada_ordenaAscendentemente() {
        List<Double> original = Arrays.asList(50.0, 10.0, 30.0, 20.0);
        List<Double> ordenada = Numeros.ordenar(original);

        assertEquals(List.of(10.0, 20.0, 30.0, 50.0), ordenada);
        // Verificar que la lista original no fue modificada
        assertEquals(50.0, original.get(0));
    }
}
