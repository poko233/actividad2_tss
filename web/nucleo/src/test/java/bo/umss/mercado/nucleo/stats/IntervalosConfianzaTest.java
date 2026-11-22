package bo.umss.mercado.nucleo.stats;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias y de Caja Negra - IntervalosConfianza")
class IntervalosConfianzaTest {

    @Test
    @DisplayName("Caja Negra [Valores Límite]: paraMedia con lista vacía retorna NaN")
    void paraMedia_listaVacia_retornaNan() {
        RangoConfianza r = IntervalosConfianza.paraMedia(Collections.emptyList());
        assertTrue(Double.isNaN(r.limiteInferior()));
        assertTrue(Double.isNaN(r.limiteSuperior()));
    }

    @Test
    @DisplayName("Caja Negra [Valores Límite]: paraMedia con un solo dato retorna límites iguales a la media")
    void paraMedia_unSoloElemento_retornaMismoValor() {
        RangoConfianza r = IntervalosConfianza.paraMedia(List.of(15.0));
        assertEquals(15.0, r.limiteInferior(), 1e-6);
        assertEquals(15.0, r.limiteSuperior(), 1e-6);
    }

    @Test
    @DisplayName("Unitaria: paraMedia calcula t-Student 95% con muestra representativa")
    void paraMedia_muestraValida_calculaRangoEsperado() {
        List<Double> muestra = List.of(10.0, 12.0, 14.0, 16.0, 18.0);
        RangoConfianza r = IntervalosConfianza.paraMedia(muestra);

        // media = 14.0, desv = 3.162277, n = 5, t(4, 0.975) = 2.7764
        // margen = 2.7764 * 3.162277 / sqrt(5) = 3.9248
        assertEquals(14.0, (r.limiteInferior() + r.limiteSuperior()) / 2.0, 1e-4);
        assertTrue(r.limiteInferior() < 14.0);
        assertTrue(r.limiteSuperior() > 14.0);
        assertEquals(10.075, r.limiteInferior(), 0.05);
        assertEquals(17.925, r.limiteSuperior(), 0.05);
    }

    @Test
    @DisplayName("Caja Negra [Valores Límite]: paraProporcion con casos inválidos o NaN")
    void paraProporcion_casosInvalidos_retornaNan() {
        RangoConfianza r1 = IntervalosConfianza.paraProporcion(0.5, 0);
        assertTrue(Double.isNaN(r1.limiteInferior()));
        assertTrue(Double.isNaN(r1.limiteSuperior()));

        RangoConfianza r2 = IntervalosConfianza.paraProporcion(Double.NaN, 50);
        assertTrue(Double.isNaN(r2.limiteInferior()));
        assertTrue(Double.isNaN(r2.limiteSuperior()));

        RangoConfianza r3 = IntervalosConfianza.paraProporcion(0.5, -10);
        assertTrue(Double.isNaN(r3.limiteInferior()));
    }

    @Test
    @DisplayName("Caja Negra [Valores Límite]: paraProporcion acota límites estrictamente entre 0 y 1")
    void paraProporcion_valoresExtremos_acotaEntreCeroYUno() {
        RangoConfianza rZero = IntervalosConfianza.paraProporcion(0.0, 100);
        assertEquals(0.0, rZero.limiteInferior(), 1e-6);
        assertEquals(0.0, rZero.limiteSuperior(), 1e-6);

        RangoConfianza rUno = IntervalosConfianza.paraProporcion(1.0, 100);
        assertEquals(1.0, rUno.limiteInferior(), 1e-6);
        assertEquals(1.0, rUno.limiteSuperior(), 1e-6);
    }

    @Test
    @DisplayName("Unitaria: paraProporcion con p=0.5 y n=100 calcula margen 1.96 * 0.05 = 0.098")
    void paraProporcion_casoTipico_calculaCorrectamente() {
        RangoConfianza r = IntervalosConfianza.paraProporcion(0.5, 100);
        assertEquals(0.5 - 0.098, r.limiteInferior(), 1e-3);
        assertEquals(0.5 + 0.098, r.limiteSuperior(), 1e-3);
    }

    @Test
    @DisplayName("Caja Negra [Valores Límite]: paraDiferencia requiere mínimo 2 elementos por grupo")
    void paraDiferencia_gruposInsuficientes_retornaNan() {
        RangoConfianza r1 = IntervalosConfianza.paraDiferencia(List.of(1.0), List.of(2.0, 3.0));
        assertTrue(Double.isNaN(r1.limiteInferior()));

        RangoConfianza r2 = IntervalosConfianza.paraDiferencia(List.of(1.0, 2.0), List.of(3.0));
        assertTrue(Double.isNaN(r2.limiteInferior()));
    }

    @Test
    @DisplayName("Unitaria: paraDiferencia con dos grupos calcula diferencia y margen")
    void paraDiferencia_dosGruposValidos_calculaIC() {
        List<Double> gA = List.of(20.0, 22.0, 24.0); // media 22
        List<Double> gB = List.of(10.0, 12.0, 14.0); // media 12
        RangoConfianza r = IntervalosConfianza.paraDiferencia(gA, gB);

        // Diferencia de medias = 10.0
        double centro = (r.limiteInferior() + r.limiteSuperior()) / 2.0;
        assertEquals(10.0, centro, 1e-4);
        assertTrue(r.limiteInferior() > 0, "Al ser grupos claramente separados, 0 debe quedar fuera del IC");
    }
}
