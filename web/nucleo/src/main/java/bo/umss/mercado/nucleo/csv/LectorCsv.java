package bo.umss.mercado.nucleo.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

public class LectorCsv {

    private static final CSVFormat FORMATO_LECTURA = CSVFormat.DEFAULT
            .builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setTrim(true)
            .setIgnoreEmptyLines(true)
            .get();

    private static String decodificar(byte[] bytes) {
        int inicio = (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xEF
                && (bytes[1] & 0xFF) == 0xBB
                && (bytes[2] & 0xFF) == 0xBF) ? 3 : 0;
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes, inicio, bytes.length - inicio))
                    .toString();
        } catch (CharacterCodingException noEsUtf8) {
            return new String(bytes, inicio, bytes.length - inicio,
                    Charset.forName("windows-1252"));
        }
    }

    public static List<Map<String, String>> leer(InputStream archivoCsv) throws IOException {
        String texto = decodificar(archivoCsv.readAllBytes());
        List<Map<String, String>> filas = new ArrayList<>();
        try (CSVParser parser = new CSVParser(new StringReader(texto), FORMATO_LECTURA)) {
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

    public static List<String> columnasDe(List<Map<String, String>> filas) {
        if (filas.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(filas.get(0).keySet());
    }

    public static void escribir(OutputStream destino, List<String> columnas,
            List<List<String>> filas) throws IOException {
        OutputStreamWriter escritor = new OutputStreamWriter(destino, StandardCharsets.UTF_8);
        try (CSVPrinter impresora = new CSVPrinter(escritor,
                CSVFormat.DEFAULT.builder().setHeader(columnas.toArray(new String[0])).get())) {
            for (List<String> fila : filas) impresora.printRecord(fila);
        }
    }
}
