package bo.umss.mercado.servidor.servicio;

import bo.umss.mercado.nucleo.stats.PruebaChiCuadrada;
import bo.umss.mercado.nucleo.stats.ResultadoChiCuadrada;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias y de Caja Negra - ServicioCruces")
class ServicioCrucesTest {

    @Mock
    private ServicioAnalisis analisis;

    private ServicioCruces servicio;

    @BeforeEach
    void setUp() {
        servicio = new ServicioCruces(analisis);
    }

    @Test
    @DisplayName("Caja Negra [Valores Límite]: wilsonPorFila con observadas nulas retorna lista vacía")
    void wilsonPorFila_observadasNulas_retornaVacio() {
        ResultadoChiCuadrada vacio = ResultadoChiCuadrada.vacio();
        List<double[]> ic = servicio.wilsonPorFila(vacio);
        assertTrue(ic.isEmpty());
    }

    @Test
    @DisplayName("Unitaria: wilsonPorFila calcula intervalos Wilson acotados entre 0 y 1")
    void wilsonPorFila_tablaValida_calculaIntervalosValidos() {
        List<String> colA = List.of("JUNIOR", "JUNIOR", "SENIOR", "SENIOR");
        List<String> colB = List.of("TRABAJA", "DESEMPLEADO", "TRABAJA", "TRABAJA");
        ResultadoChiCuadrada r = PruebaChiCuadrada.cruzar(colA, colB);

        List<double[]> ics = servicio.wilsonPorFila(r);

        assertFalse(ics.isEmpty());
        for (double[] ic : ics) {
            assertEquals(2, ic.length);
            assertTrue(ic[0] >= 0.0 && ic[0] <= 1.0, "Límite inferior debe estar en [0, 1]");
            assertTrue(ic[1] >= 0.0 && ic[1] <= 1.0, "Límite superior debe estar en [0, 1]");
            assertTrue(ic[0] <= ic[1], "Límite inferior debe ser <= límite superior");
        }
    }

    @Test
    @DisplayName("Caja Negra [Valores Límite]: interpretarCruceDinamico sin datos reporta datos insuficientes")
    void interpretarCruceDinamico_sinDatos_retornaMensajeInsuficiente() {
        FichaCruce cruceVacio = new FichaCruce(
                "Cruce Vacio", "colA", "colB", ResultadoChiCuadrada.vacio(), "Sin frase");

        String interpretacion = servicio.interpretarCruceDinamico(cruceVacio);
        assertTrue(interpretacion.contains("No hay datos suficientes"));
    }

    @Test
    @DisplayName("Caja Negra [Partición Equivalente]: interpretarCruceDinamico cuando no es estadísticamente significativo")
    void interpretarCruceDinamico_noSignificativo_explicaIndependencia() {
        // Generar datos simétricos / independientes
        List<String> a = new ArrayList<>();
        List<String> b = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            a.add("M"); b.add("SI");
            a.add("M"); b.add("NO");
            a.add("F"); b.add("SI");
            a.add("F"); b.add("NO");
        }
        ResultadoChiCuadrada r = PruebaChiCuadrada.cruzar(a, b);
        FichaCruce cruce = new FichaCruce("Genero x Empleo", "genero", "empleo", r, "Frase guia");

        String texto = servicio.interpretarCruceDinamico(cruce);
        assertTrue(texto.contains("No se encontró evidencia estadística"),
                "Debe indicar que no hay evidencia estadística de asociación");
        assertTrue(texto.contains("independientes"));
    }

    @Test
    @DisplayName("Caja Negra [Partición Equivalente]: interpretarCruceDinamico cuando es estadísticamente significativo")
    void interpretarCruceDinamico_significativo_explicaAsociacion() {
        // Generar datos con asociación determinante
        List<String> a = new ArrayList<>();
        List<String> b = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            a.add("CAT_1"); b.add("RESP_X");
            a.add("CAT_2"); b.add("RESP_Y");
        }
        ResultadoChiCuadrada r = PruebaChiCuadrada.cruzar(a, b);
        FichaCruce cruce = new FichaCruce("Cruce Fuerte", "cat", "resp", r, "Frase guia");

        String texto = servicio.interpretarCruceDinamico(cruce);
        assertTrue(texto.contains("Existe una asociación estadísticamente significativa"),
                "Debe indicar que existe asociación estadísticamente significativa");
        assertTrue(texto.contains("V Cramér"));
    }

    @Test
    @DisplayName("Unitaria: crucesTitulados y matrizResumen generan 15 cruces con corrección de Holm")
    void crucesTitulados_genera15CrucesYMatriz() {
        // Datos mock de prueba
        List<Map<String, String>> datos = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Map<String, String> f = new HashMap<>();
            f.put("genero", i % 2 == 0 ? "MASCULINO" : "FEMENINO");
            f.put("situacion_laboral_actual", i % 3 == 0 ? "TRABAJA" : "NO_TRABAJA");
            f.put("edad_rango", "23-26");
            datos.add(f);
        }
        when(analisis.mapasTitulados()).thenReturn(datos);
        when(analisis.valorDerivado(any(), any())).thenReturn(null);

        List<FichaCruce> cruces = servicio.crucesTitulados();
        assertEquals(15, cruces.size(), "Deben generarse los 15 cruces canónicos de titulados");

        List<ServicioCruces.FilaMatrizBivariada> matriz = servicio.matrizResumenTitulados();
        assertEquals(15, matriz.size(), "La matriz resumen debe tener 15 filas con Holm aplicado");
        for (ServicioCruces.FilaMatrizBivariada fila : matriz) {
            assertNotNull(fila.etiqueta());
            // pHolm no debe ser inferior a pOriginal si ambos son válidos
            if (!Double.isNaN(fila.pHolm()) && !Double.isNaN(fila.pOriginal())) {
                assertTrue(fila.pHolm() >= fila.pOriginal() - 1e-6,
                        "El p-valor ajustado por Holm debe ser >= p-valor original");
            }
        }
    }
}
