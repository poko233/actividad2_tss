package bo.umss.mercado.nucleo.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

// LectorCsv: convierte archivos CSV en listas de filas.
// Cada fila es un mapa COLUMNA -> VALOR, así el resto del código
// lee fila.get("edad") en vez de contar posiciones.
// Todo el parseo difícil (comillas, comas dentro de texto, tildes)
// lo hace la librería Apache Commons CSV, no nosotros.
public class LectorCsv {

    // Formato que aceptamos: primera fila = encabezados, comas, UTF-8.
    private static final CSVFormat FORMATO_LECTURA = CSVFormat.DEFAULT
            .builder()
            .setHeader()          // la primera fila son los nombres de columna
            .setSkipHeaderRecord(true)
            .setTrim(true)        // quita espacios alrededor de cada valor
            .setIgnoreEmptyLines(true)
            .get();

    // leer(): devuelve una fila por encuestado. Las celdas vacías llegan
    // como "" (nunca null) para no romper el código que sigue.
    public static List<Map<String, String>> leer(InputStream archivoCsv) throws IOException {
        List<Map<String, String>> filas = new ArrayList<>();
        InputStreamReader lectorTexto =
                new InputStreamReader(archivoCsv, StandardCharsets.UTF_8);
        try (CSVParser parser = new CSVParser(lectorTexto, FORMATO_LECTURA)) {
            List<String> columnas = parser.getHeaderNames();
            for (var registro : parser) {
                Map<String, String> fila = new LinkedHashMap<>();
                for (String columna : columnas) {
                    String valor = registro.get(columna);
                    fila.put(columna, valor == null ? "" : valor.trim());
                }
                filas.add(fila);
            }
        }
        return filas;
    }

    // columnasDe(): solo los encabezados (para validar el formato).
    public static List<String> columnasDe(List<Map<String, String>> filas) {
        if (filas.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(filas.get(0).keySet());
    }

    // escribir(): guarda filas de vuelta a CSV (para exportar resultados).
    public static void escribir(OutputStream destino, List<String> columnas,
            List<List<String>> filas) throws IOException {
        OutputStreamWriter escritor =
                new OutputStreamWriter(destino, StandardCharsets.UTF_8);
        try (CSVPrinter impresora = new CSVPrinter(escritor,
                CSVFormat.DEFAULT.builder().setHeader(columnas.toArray(new String[0])).get())) {
            for (List<String> fila : filas) {
                impresora.printRecord(fila);
            }
        }
    }
}
