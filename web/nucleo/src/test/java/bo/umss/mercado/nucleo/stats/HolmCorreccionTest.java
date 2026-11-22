package bo.umss.mercado.nucleo.stats;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias y de Caja Negra - HolmCorreccion")
class HolmCorreccionTest {

    @Test
    @DisplayName("Caja Negra [Valores Límite]: lista vacía retorna lista vacía")
    void ajustar_listaVacia_retornaListaVacia() {
        List<HolmCorreccion.ItemHolm> res = HolmCorreccion.ajustar(Collections.emptyList());
        assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("Caja Negra [Valores Límite]: un solo p-valor se mantiene igual")
    void ajustar_unSoloPValor_mantieneValor() {
        List<HolmCorreccion.ItemHolm> res = HolmCorreccion.ajustar(List.of(0.03));
        assertEquals(1, res.size());
        assertEquals(0.03, res.get(0).valorPAjustado(), 1e-6);
        assertTrue(res.get(0).significativo());
    }

    @Test
    @DisplayName("Unitaria: ajuste de Holm preserva posiciones y multiplica por factores descendentes")
    void ajustar_multiplesPValores_aplicaCorreccionCorrecta() {
        // m = 3 pruebas: p0 = 0.01, p1 = 0.04, p2 = 0.03
        // Orden ascendente:
        // k=0: p=0.01 -> factor (3 - 0) = 3 -> 0.01 * 3 = 0.03
        // k=1: p=0.03 -> factor (3 - 1) = 2 -> 0.03 * 2 = 0.06
        // k=2: p=0.04 -> factor (3 - 2) = 1 -> 0.04 * 1 = 0.04 -> pero max con acumulado (0.06) => 0.06
        List<Double> ps = List.of(0.01, 0.04, 0.03);
        List<HolmCorreccion.ItemHolm> res = HolmCorreccion.ajustar(ps);

        assertEquals(3, res.size());
        // Índice 0 (original 0.01)
        assertEquals(0.01, res.get(0).valorPOriginal(), 1e-6);
        assertEquals(0.03, res.get(0).valorPAjustado(), 1e-6);
        assertTrue(res.get(0).significativo());

        // Índice 1 (original 0.04)
        assertEquals(0.04, res.get(1).valorPOriginal(), 1e-6);
        assertEquals(0.06, res.get(1).valorPAjustado(), 1e-6);
        assertFalse(res.get(1).significativo());

        // Índice 2 (original 0.03)
        assertEquals(0.03, res.get(2).valorPOriginal(), 1e-6);
        assertEquals(0.06, res.get(2).valorPAjustado(), 1e-6);
        assertFalse(res.get(2).significativo());
    }

    @Test
    @DisplayName("Caja Negra [Valores Límite]: p-valores altos quedan acotados en 1.0")
    void ajustar_pvaloresAltos_noExcedenUno() {
        List<Double> ps = List.of(0.8, 0.9);
        List<HolmCorreccion.ItemHolm> res = HolmCorreccion.ajustar(ps);

        for (HolmCorreccion.ItemHolm item : res) {
            assertTrue(item.valorPAjustado() <= 1.0);
            assertFalse(item.significativo());
        }
    }
}
