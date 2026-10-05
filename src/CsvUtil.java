import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

// ============================================================================
// CsvUtil — Entrada/Salida de archivos CSV sin librerías externas.
// ----------------------------------------------------------------------------
// RESPONSABILIDAD: es la "puerta de entrada" del pipeline (Data Wrangling).
//   1) leer()    : carga un CSV a memoria como objeto Tabla.
//   2) escribir(): vuelca una Tabla (o matrices de resultados) a disco.
//   3) normalizar(): deja los textos comparables (mayúsculas, sin tildes,
//                  espacios -> "_"), clave para cruzar categorías como
//                  "Redes de Datos y Seguridad" vs "REDES_SEGURIDAD".
// FORMATO ACEPTADO: separador coma, comillas dobles como escape ("..." y
// "" dentro de comillas = una comilla literal), saltos de línea dentro de
// campos entrecomillados y codificación UTF-8 (con o sin BOM de Excel).
// ============================================================================
public class CsvUtil {

    // ------------------------------------------------------------------------
    // Tabla: representación en memoria de un CSV.
    //   - headers : nombres de columna en orden (fila 1 del archivo).
    //   - filas   : una Map<columna, valor> por encuestado (fila = registro).
    // Se usa Map y no índices para que el código sea legible:
    //   fila.get("area_interes_posgrado") en vez de fila[9].
    // ------------------------------------------------------------------------
    public static class Tabla {
        public List<String> headers = new ArrayList<>();
        public List<Map<String, String>> filas = new ArrayList<>();
    }

    // ------------------------------------------------------------------------
    // leer(): lee TODO el archivo a String y lo convierte en Tabla.
    // Pasos: (a) quitar BOM, (b) parsear celdas con autómata, (c) descartar
    // filas 100% vacías, (d) usar la primera fila como encabezado y mapear
    // cada fila de datos a Map<columna,valor> (con "" si faltan celdas).
    // ------------------------------------------------------------------------
    public static Tabla leer(Path path) throws IOException {
        // Se lee como UTF-8 porque las encuestas traen tildes y eñes.
        String texto = Files.readString(path, StandardCharsets.UTF_8);
        // Quitar BOM si existe (Excel antepone \uFEFF y rompería el 1er header)
        if (!texto.isEmpty() && texto.charAt(0) == '\uFEFF') texto = texto.substring(1);
        Tabla t = new Tabla();
        List<List<String>> registros = parsear(texto);
        // Eliminar líneas totalmente vacías (típicas al final del archivo)
        List<List<String>> limpios = new ArrayList<>();
        for (List<String> r : registros) {
            boolean vacia = true;
            for (String c : r) if (c != null && !c.trim().isEmpty()) { vacia = false; break; }
            if (!vacia) limpios.add(r);
        }
        if (limpios.isEmpty()) return t; // archivo vacío: tabla sin columnas
        // La primera fila no vacía define los nombres de columna (se recortan)
        for (String h : limpios.get(0)) t.headers.add(h.trim());
        // El resto son datos: se alinean por posición con el encabezado
        for (int i = 1; i < limpios.size(); i++) {
            List<String> r = limpios.get(i);
            Map<String, String> m = new LinkedHashMap<>(); // orden = orden CSV
            for (int j = 0; j < t.headers.size(); j++) {
                String v = j < r.size() ? r.get(j).trim() : ""; // fila corta -> ""
                m.put(t.headers.get(j), v);
            }
            t.filas.add(m);
        }
        return t;
    }

    // ------------------------------------------------------------------------
    // parsear(): autómata de 2 estados (dentro/fuera de comillas) que recorre
    // el texto carácter por carácter y corta campos/filas.
    // Reglas: ',' fuera de comillas = nuevo campo; '\n' fuera de comillas =
    // nueva fila; '"' abre/cierra comillas; '""' dentro de comillas = '"' real;
    // '\r' se ignora (archivos Windows traen "\r\n"); todo lo demás se acumula.
    // Soporta comas y saltos de línea DENTRO de campos entrecomillados.
    // ------------------------------------------------------------------------
    private static List<List<String>> parsear(String texto) {
        List<List<String>> out = new ArrayList<>(); // todas las filas
        List<String> fila = new ArrayList<>();      // fila en construcción
        StringBuilder campo = new StringBuilder();  // campo en construcción
        boolean enComillas = false;                 // estado del autómata
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (enComillas) {
                if (c == '"') {
                    // ¿Es comilla de escape ("") o de cierre? Se mira 1 adelante
                    if (i + 1 < texto.length() && texto.charAt(i + 1) == '"') {
                        campo.append('"'); i++; // escape: se guarda una y se salta la otra
                    } else {
                        enComillas = false;     // cierre del campo entrecomillado
                    }
                } else {
                    campo.append(c); // cualquier char (incluido '\n') vale dentro
                }
            } else {
                if (c == '"') enComillas = true;      // abre entrecomillado
                else if (c == ',') { fila.add(campo.toString()); campo.setLength(0); } // fin campo
                else if (c == '\r') { /* ignorar, espera \n */ }
                else if (c == '\n') { fila.add(campo.toString()); campo.setLength(0); out.add(fila); fila = new ArrayList<>(); } // fin fila
                else campo.append(c);
            }
        }
        // último campo/fila (cuando el archivo no termina en '\n')
        if (campo.length() > 0 || !fila.isEmpty()) {
            fila.add(campo.toString());
            out.add(fila);
        }
        return out;
    }

    // ------------------------------------------------------------------------
    // escapar(): envuelve un valor en comillas SOLO si lo necesita (contiene
    // coma, comilla o salto de línea), duplicando comillas internas.
    // Es la operación inversa del parser y evita CSVs rotos al exportar.
    // ------------------------------------------------------------------------
    public static String escapar(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    // ------------------------------------------------------------------------
    // escribir(): vuelca encabezado + filas a disco en UTF-8. Usado para los
    // exportables del módulo 4.9 (matriz_contingencia, histogramas,
    // datos_limpios) y por Registro.java al guardar nuevas encuestas.
    // ------------------------------------------------------------------------
    public static void escribir(Path path, List<String> headers, List<List<String>> filas) throws IOException {
        StringBuilder sb = new StringBuilder();
        // Fila 1: encabezado
        for (int i = 0; i < headers.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(escapar(headers.get(i)));
        }
        sb.append("\n");
        // Filas de datos (si una fila trae menos celdas, se rellena con "")
        for (List<String> f : filas) {
            for (int i = 0; i < headers.size(); i++) {
                if (i > 0) sb.append(',');
                sb.append(escapar(i < f.size() ? f.get(i) : ""));
            }
            sb.append("\n");
        }
        Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
    }

    // ------------------------------------------------------------------------
    // normalizar(): canoniza un texto para comparar categorías sin importar
    // cómo se escribió ("Redes de Datos y Seguridad" -> "REDES_DE_DATOS_Y...").
    // Sin esto, "IA" e "Ia" contarían como áreas distintas y los cruces χ²
    // saldrían fragmentados. También quita tildes (Á->A, Ñ->N).
    // ------------------------------------------------------------------------
    public static String normalizar(String s) {
        if (s == null) return "";
        String t = s.trim().toUpperCase()
                .replace("Á", "A").replace("É", "E").replace("Í", "I")
                .replace("Ó", "O").replace("Ú", "U").replace("Ü", "U").replace("Ñ", "N");
        t = t.replaceAll("\\s+", "_"); // uno o más espacios -> un solo "_"
        return t;
    }
}
