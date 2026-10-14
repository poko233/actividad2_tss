package bo.umss.mercado.nucleo.limpieza;

import java.util.Map;
import java.util.Set;

// LimpiezaDatos: fase "Data Wrangling". Deja cada celda comparable:
// recorta espacios, convierte nulos escritos ("null", "n/a", "-") en "",
// unifica SI/NO ("Si", "sí", "YES" -> "SI") y marca los números
// inválidos como "" para que no rompan los cálculos.
// Sin esto, "Si" y "SI" contarían como categorías distintas.
public class LimpiezaDatos {

    // Columnas que deben ser número (edad y antigüedades).
    private static final Set<String> COLUMNAS_NUMERICAS = Set.of(
            "edad", "anios_vida_profesional", "anios_desempleado");

    // Columnas que solo admiten SI o NO.
    private static final Set<String> COLUMNAS_SI_NO = Set.of(
            "interes_posgrado", "realizo_formacion_complementaria",
            "red_contactos", "primer_empleo_actual", "contrato_ultimos_5_anios");

    // limpiarFila(): normaliza UNA respuesta completa (se llama por fila).
    public static void limpiarFila(Map<String, String> fila) {
        for (String columna : fila.keySet().toArray(new String[0])) {
            String valor = fila.get(columna);
            if (valor == null) {
                valor = "";
            }
            valor = valor.trim();
            if (esTextoNulo(valor)) {
                valor = "";
            } else if (COLUMNAS_NUMERICAS.contains(columna)
                    && !esNumero(valor)) {
                valor = "";
            } else if (COLUMNAS_SI_NO.contains(columna) && !valor.isEmpty()) {
                valor = unificarSiNo(valor);
            }
            fila.put(columna, valor);
        }
    }

    // esTextoNulo(): formas típicas de escribir "sin dato" en Excel/Forms.
    private static boolean esTextoNulo(String valor) {
        return valor.equalsIgnoreCase("null")
                || valor.equalsIgnoreCase("n/a")
                || valor.equalsIgnoreCase("na")
                || valor.equals("-");
    }

    // esNumero(): true si el texto se puede convertir a número
    // (acepta coma decimal: "3,5" vale como 3.5).
    private static boolean esNumero(String valor) {
        try {
            Double.parseDouble(valor.replace(",", "."));
            return true;
        } catch (NumberFormatException noEsNumero) {
            return false;
        }
    }

    // unificarSiNo(): "Si", "SÍ", "YES", "1" -> "SI"; "No", "0" -> "NO".
    // Lo demás se deja igual (llegará como "" o tal cual).
    private static String unificarSiNo(String valor) {
        String normalizado = normalizar(valor);
        if (normalizado.startsWith("SI") || normalizado.equals("YES")
                || normalizado.equals("1")) {
            return "SI";
        }
        if (normalizado.startsWith("NO") || normalizado.equals("0")) {
            return "NO";
        }
        return valor;
    }

    // normalizar(): mayúsculas, sin tildes, espacios -> "_".
    // Así "Redes de Datos y Seguridad" se compara sin importar
    // cómo la escribió cada encuestado.
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.trim().toUpperCase()
                .replace("Á", "A").replace("É", "E").replace("Í", "I")
                .replace("Ó", "O").replace("Ú", "U").replace("Ü", "U")
                .replace("Ñ", "N")
                .replaceAll("\\s+", "_");
    }
}
