package bo.umss.mercado.nucleo.csv;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias y de Caja Negra - LectorCsv")
class LectorCsvTest {

    @Test
    @DisplayName("Caja Negra [Partición Equivalente]: Lee CSV UTF-8 estándar con trim y comillas")
    void leer_csvUtf8Estandar_retornaFilasCorrectas() throws IOException {
        String csv = "id,nombre,area\n"
                   + "1,\"Juan Perez\",IA\n"
                   + "2,\"Maria Lopez\",WEB\n";
        ByteArrayInputStream in = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        List<Map<String, String>> filas = LectorCsv.leer(in);

        assertEquals(2, filas.size());
        assertEquals("1", filas.get(0).get("id"));
        assertEquals("Juan Perez", filas.get(0).get("nombre"));
        assertEquals("IA", filas.get(0).get("area"));
        assertEquals("Maria Lopez", filas.get(1).get("nombre"));
    }

    @Test
    @DisplayName("Caja Negra: Lee correctamente CSV con marca de orden de bytes (BOM UTF-8)")
    void leer_csvConBomUtf8_eliminaBomSinCorromperHeader() throws IOException {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] contenido = "codigo,categoria\n101,Sistemas\n".getBytes(StandardCharsets.UTF_8);

        byte[] total = new byte[bom.length + contenido.length];
        System.arraycopy(bom, 0, total, 0, bom.length);
        System.arraycopy(contenido, 0, total, bom.length, contenido.length);

        ByteArrayInputStream in = new ByteArrayInputStream(total);
        List<Map<String, String>> filas = LectorCsv.leer(in);

        assertEquals(1, filas.size());
        assertTrue(filas.get(0).containsKey("codigo"), "El encabezado no debe contener residuos del BOM");
        assertEquals("101", filas.get(0).get("codigo"));
        assertEquals("Sistemas", filas.get(0).get("categoria"));
    }

    @Test
    @DisplayName("Caja Negra: Decodifica correctamente fallback a Windows-1252 con tildes y eñes")
    void leer_csvWindows1252_decodificaCaracteresEspeciales() throws IOException {
        String texto = "columna,descripcion\n1,Año de graduación\n";
        byte[] bytes1252 = texto.getBytes(Charset.forName("windows-1252"));

        ByteArrayInputStream in = new ByteArrayInputStream(bytes1252);
        List<Map<String, String>> filas = LectorCsv.leer(in);

        assertEquals(1, filas.size());
        assertEquals("Año de graduación", filas.get(0).get("descripcion"));
    }

    @Test
    @DisplayName("Caja Negra [Valores Límite]: columnasDe con lista vacía retorna lista vacía")
    void columnasDe_listaVacia_retornaVacio() {
        assertTrue(LectorCsv.columnasDe(List.of()).isEmpty());
    }

    @Test
    @DisplayName("Unitaria: escribir y leer en ciclo cerrado (Round-trip) mantiene integridad")
    void escribirYLeer_roundTrip_mantieneDatos() throws IOException {
        List<String> columnas = List.of("id", "carrera", "promedio");
        List<List<String>> filasAEscribir = List.of(
            List.of("1", "Ingeniería en Sistemas", "85.5"),
            List.of("2", "Informática", "90.0")
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        LectorCsv.escribir(out, columnas, filasAEscribir);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        List<Map<String, String>> filasLeidas = LectorCsv.leer(in);

        assertEquals(2, filasLeidas.size());
        assertEquals("Ingeniería en Sistemas", filasLeidas.get(0).get("carrera"));
        assertEquals("85.5", filasLeidas.get(0).get("promedio"));
        assertEquals("Informática", filasLeidas.get(1).get("carrera"));
    }
}
