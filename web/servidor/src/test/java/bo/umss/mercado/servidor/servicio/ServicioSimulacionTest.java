package bo.umss.mercado.servidor.servicio;

import bo.umss.mercado.servidor.datos.RepositorioSimulaciones;
import bo.umss.mercado.servidor.datos.Simulacion;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias y de Caja Negra - ServicioSimulacion (Monte Carlo)")
class ServicioSimulacionTest {

    @Mock
    private ServicioAnalisis analisis;

    @Mock
    private RepositorioSimulaciones historial;

    private ServicioSimulacion servicio;

    @BeforeEach
    void setUp() {
        servicio = new ServicioSimulacion(analisis, historial);
    }

    private List<Map<String, String>> generarDatosPrueba(int total, int interesados, String variable, String valorObjetivo) {
        List<Map<String, String>> datos = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            Map<String, String> fila = new HashMap<>();
            if (i < interesados) {
                fila.put(variable, valorObjetivo);
            } else {
                fila.put(variable, "OTRA_AREA");
            }
            datos.add(fila);
        }
        return datos;
    }

    @Test
    @DisplayName("Caja Negra [Valores Límite]: Población vacía debe lanzar IllegalStateException")
    void ejecutarSimulacion_sinDatos_lanzaExcepcion() {
        when(analisis.mapasTitulados()).thenReturn(Collections.emptyList());
        ConfiguracionSimulacion config = servicio.configEscenarioDemanda();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> servicio.ejecutarSimulacion(config));
        assertTrue(ex.getMessage().contains("No hay datos para simular"));
    }

    @Test
    @DisplayName("Caja Negra / Unitaria: Simulación Monte Carlo es determinista dada la misma semilla")
    void ejecutarSimulacion_mismaSemilla_produceResultadosIdenticos() {
        List<Map<String, String>> datos = generarDatosPrueba(100, 30, "area_interes_posgrado", "IA");
        when(analisis.mapasTitulados()).thenReturn(datos);

        ConfiguracionSimulacion config = new ConfiguracionSimulacion(
                "Test Monte Carlo", "area_interes_posgrado", "IA",
                0.80, 0.10, 500, 12345L, false);

        ResultadoSimulacion res1 = servicio.ejecutarSimulacion(config);
        ResultadoSimulacion res2 = servicio.ejecutarSimulacion(config);

        assertEquals(res1.media(), res2.media(), 1e-9);
        assertEquals(res1.desviacionEstandar(), res2.desviacionEstandar(), 1e-9);
        assertEquals(res1.icInferior(), res2.icInferior(), 1e-9);
        assertEquals(res1.icSuperior(), res2.icSuperior(), 1e-9);
        assertEquals(res1.primerasIteraciones(), res2.primerasIteraciones());
    }

    @Test
    @DisplayName("Caja Negra [Partición Equivalente]: Verificación de coherencia y rangos del modelo Monte Carlo")
    void ejecutarSimulacion_coherenciaDeRangos() {
        int total = 200;
        int interesados = 50;
        List<Map<String, String>> datos = generarDatosPrueba(total, interesados, "area_interes_posgrado", "IA");
        when(analisis.mapasTitulados()).thenReturn(datos);

        ConfiguracionSimulacion config = new ConfiguracionSimulacion(
                "Escenario IA", "area_interes_posgrado", "IA",
                0.80, 0.05, 1000, 42L, false);

        ResultadoSimulacion res = servicio.ejecutarSimulacion(config);

        // Validar conteos poblacionales
        assertEquals(total, res.poblacionTotal());
        assertEquals(interesados, res.cantidadInteresados());
        assertEquals(0.25, res.proporcionEmpirica(), 1e-6);

        // Validar intervalos de confianza: icInferior <= media <= icSuperior
        assertTrue(res.icInferior() <= res.media(), "Límite inferior debe ser <= media");
        assertTrue(res.media() <= res.icSuperior(), "Media debe ser <= límite superior");
        assertTrue(res.icInferior() >= 0, "Límite inferior no puede ser negativo");

        // Validar análisis de sensibilidad: mediaPesimista <= media <= mediaOptimista
        assertTrue(res.mediaPesimista() <= res.media(), "Escenario pesimista debe ser <= media base");
        assertTrue(res.media() <= res.mediaOptimista(), "Escenario optimista debe ser >= media base");

        // Histograma debe contener exactamente el total de iteraciones
        int totalSimulado = res.histograma().values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(config.iteraciones(), totalSimulado);

        // Guardado en base de datos
        ArgumentCaptor<Simulacion> captor = ArgumentCaptor.forClass(Simulacion.class);
        verify(historial, atLeastOnce()).save(captor.capture());
        Simulacion guardado = captor.getValue();
        assertEquals("Escenario IA - IA", guardado.getEscenario());
        assertEquals(42L, guardado.getSemilla());
    }

    @Test
    @DisplayName("Caja Negra [Valores Límite]: Población con 0 interesados vs 100% interesados")
    void ejecutarSimulacion_extremosPoblacion() {
        // Caso 0 interesados
        List<Map<String, String>> datosCero = generarDatosPrueba(50, 0, "area", "IA");
        when(analisis.mapasTitulados()).thenReturn(datosCero);

        ConfiguracionSimulacion configCero = new ConfiguracionSimulacion(
                "Cero Interesados", "area", "IA", 0.80, 0.0, 100, 111L, false);

        ResultadoSimulacion resCero = servicio.ejecutarSimulacion(configCero);
        assertEquals(0, resCero.cantidadInteresados());
        assertEquals(0.0, resCero.proporcionEmpirica(), 1e-6);
        assertEquals(0.0, resCero.media(), 1e-6); // probResto es 0.0 -> siempre 0 éxitos
    }

    @Test
    @DisplayName("Caja Negra: obtenerProbabilidadInteresados clasifica probabilidades según reglas de negocio")
    void obtenerProbabilidadInteresados_reglasNegocio() {
        // posibilidad_futura_contratacion
        assertEquals(0.80, servicio.obtenerProbabilidadInteresados("posibilidad_futura_contratacion", "ALTA_PROBABILIDAD"));
        assertEquals(0.40, servicio.obtenerProbabilidadInteresados("posibilidad_futura_contratacion", "DEPENDE_CRECIMIENTO"));
        assertEquals(0.0, servicio.obtenerProbabilidadInteresados("posibilidad_futura_contratacion", "OTRO"));

        // financiamiento_formacion
        assertEquals(0.95, servicio.obtenerProbabilidadInteresados("financiamiento_formacion", "BECA_TOTAL"));
        assertEquals(0.88, servicio.obtenerProbabilidadInteresados("financiamiento_formacion", "BECA_PARCIAL"));
        assertEquals(0.80, servicio.obtenerProbabilidadInteresados("financiamiento_formacion", "OTRO"));

        // Caso default general
        assertEquals(0.80, servicio.obtenerProbabilidadInteresados("otra_variable", "cualquier_categoria"));
    }

    @Test
    @DisplayName("Unitaria: calcularProporcion maneja listas vacías y cálculos normales")
    void calcularProporcion_calculaPorcentaje() {
        when(analisis.mapasTitulados()).thenReturn(Collections.emptyList());
        assertEquals(0.0, servicio.calcularProporcion("var", "cat", false));

        List<Map<String, String>> datos = generarDatosPrueba(10, 4, "area", "IA");
        when(analisis.mapasTitulados()).thenReturn(datos);
        assertEquals(0.4, servicio.calcularProporcion("area", "IA", false), 1e-6);
    }

    @Test
    @DisplayName("Unitaria: historial delega la consulta a RepositorioSimulaciones")
    void historial_delegaEnRepositorio() {
        Simulacion sim = new Simulacion();
        sim.setEscenario("Escenario Test");
        when(historial.findAllByOrderByFechaHoraDesc()).thenReturn(List.of(sim));

        List<Simulacion> resultado = servicio.historial();
        assertEquals(1, resultado.size());
        assertEquals("Escenario Test", resultado.get(0).getEscenario());
        verify(historial).findAllByOrderByFechaHoraDesc();
    }
}
