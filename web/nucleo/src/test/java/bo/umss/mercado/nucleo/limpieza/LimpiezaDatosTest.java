package bo.umss.mercado.nucleo.limpieza;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias y de Caja Negra - LimpiezaDatos (Data Wrangling)")
class LimpiezaDatosTest {

    @ParameterizedTest(name = "Caja Negra normalizar: \"{0}\" -> \"{1}\"")
    @CsvSource({
        "' Redes de Datos y Seguridad ', 'REDES_DE_DATOS_Y_SEGURIDAD'",
        "'Área de Análisis, Diseño y Programación', 'AREA_DE_ANALISIS,_DISENO_Y_PROGRAMACION'",
        "'Año 2026', 'ANO_2026'",
        "'MÜLLER', 'MULLER'"
    })
    @DisplayName("Caja Negra [Partición Equivalente]: normalizar elimina tildes, eñes y espacios repetidos")
    void normalizar_casosValidos(String entrada, String esperado) {
        assertEquals(esperado, LimpiezaDatos.normalizar(entrada));
    }

    @Test
    @DisplayName("Caja Negra [Valores Límite]: normalizar con null retorna cadena vacía")
    void normalizar_null_retornaVacio() {
        assertEquals("", LimpiezaDatos.normalizar(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "NULL", "n/a", "N/A", "na", "NA", "-"})
    @DisplayName("Caja Negra: Textos que representan valor nulo deben transformarse en vacío")
    void limpiarFila_textosNulos_losConvierteEnVacio(String textoNulo) {
        Map<String, String> fila = new HashMap<>();
        fila.put("cargo", textoNulo);

        LimpiezaDatos.limpiarFila(fila);

        assertEquals("", fila.get("cargo"));
    }

    @ParameterizedTest(name = "Caja Negra SI/NO: \"{0}\" -> \"{1}\"")
    @CsvSource({
        "Si, SI",
        "SÍ, SI",
        "sí, SI",
        "YES, SI",
        "1, SI",
        "siempre, SI",
        "No, NO",
        "NO, NO",
        "0, NO"
    })
    @DisplayName("Caja Negra [Clases de Equivalencia]: unifica variantes de SI y NO")
    void limpiarFila_unificarSiNo(String entrada, String esperado) {
        Map<String, String> fila = new HashMap<>();
        fila.put("interes_posgrado", entrada);

        LimpiezaDatos.limpiarFila(fila);

        assertEquals(esperado, fila.get("interes_posgrado"));
    }

    @Test
    @DisplayName("Caja Negra: Columnas numéricas rechazan valores de texto no numéricos")
    void limpiarFila_columnasNumericas_rechazanTexto() {
        Map<String, String> fila = new HashMap<>();
        fila.put("edad", "veinticinco");
        fila.put("anios_vida_profesional", "3,5"); // coma decimal aceptable
        fila.put("anios_desempleado", "4.0");

        LimpiezaDatos.limpiarFila(fila);

        assertEquals("", fila.get("edad"), "Texto no numérico debe limpiarse a vacío");
        assertEquals("3,5", fila.get("anios_vida_profesional"), "Número con coma decimal es válido");
        assertEquals("4.0", fila.get("anios_desempleado"));
    }
}
