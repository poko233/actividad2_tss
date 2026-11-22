package bo.umss.mercado.nucleo.likert;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias y de Caja Negra - EscalaLikert")
class EscalaLikertTest {

    @ParameterizedTest(name = "Caja Negra: {0} debe mapearse a {1}")
    @CsvSource({
        "Totalmente en desacuerdo, 1.0",
        "Totalmente desacuerdo, 1.0",
        "Desacuerdo, 2.0",
        "Parcialmente desacuerdo, 2.0",
        "Acuerdo, 3.0",
        "Parcialmente acuerdo, 3.0",
        "Totalmente acuerdo, 4.0",
        "No importante, 1.0",
        "Poco importante, 2.0",
        "Importante, 3.0",
        "Muy importante, 4.0",
        "Insatisfecho, 1.0",
        "Algo satisfecho, 2.0",
        "Satisfecho, 3.0"
    })
    @DisplayName("Caja Negra [Partición Equivalente]: Mapeo de escala 4 (Titulados)")
    void traducir_escala4Valida(String texto, double esperado) {
        assertEquals(esperado, EscalaLikert.traducir(texto), 1e-6);
    }

    @ParameterizedTest
    @ValueSource(strings = {"No sabe", "NO_SABE", "No responde", "xyz", ""})
    @DisplayName("Caja Negra [Valores Límite]: Valores no reconocidos o 'No sabe' deben retornar NaN")
    void traducir_valoresInvalidos_retornaNan(String invalido) {
        assertTrue(Double.isNaN(EscalaLikert.traducir(invalido)));
    }

    @ParameterizedTest(name = "Caja Negra Escala 3: {0} debe mapearse a {1}")
    @CsvSource({
        "Totalmente desacuerdo, 1.0",
        "Parcialmente acuerdo, 2.0",
        "Totalmente acuerdo, 3.0"
    })
    @DisplayName("Caja Negra [Partición Equivalente]: Mapeo de escala 3 (Empleadores)")
    void traducir3_escala3Valida(String texto, double esperado) {
        assertEquals(esperado, EscalaLikert.traducir3(texto), 1e-6);
    }

    @Test
    @DisplayName("Unitaria: Despachador traducir con nombre de columna elige la escala correcta")
    void traducir_despachador_seleccionaEscalaSegunColumna() {
        // Columna de empleadores (escala 3): "Totalmente acuerdo" = 3.0
        double puntajeEmp = EscalaLikert.traducir("Totalmente de acuerdo", "likert_competencias_laborales");
        assertEquals(3.0, puntajeEmp, 1e-6);

        // Columna de titulados (escala 4): "Totalmente acuerdo" = 4.0
        double puntajeTit = EscalaLikert.traducir("Totalmente acuerdo", "satisfaccion_formacion");
        assertEquals(4.0, puntajeTit, 1e-6);
    }

    @Test
    @DisplayName("Caja Negra: Verificación de etiquetas de consenso según desviación estándar")
    void etiquetaDeConsenso_clasificaCorrectamente() {
        assertEquals("sin dato", EscalaLikert.etiquetaDeConsenso(Double.NaN));
        assertEquals("consenso alto", EscalaLikert.etiquetaDeConsenso(0.35));
        assertEquals("consenso moderado", EscalaLikert.etiquetaDeConsenso(0.65));
        assertEquals("opiniones divididas", EscalaLikert.etiquetaDeConsenso(0.95));
    }
}
