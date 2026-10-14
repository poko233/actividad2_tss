package bo.umss.mercado.nucleo.stats;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.math3.stat.inference.ChiSquareTest;

// PruebaChiCuadrada: ¿dos preguntas categóricas están relacionadas?
// Ejemplo HU3: ¿la fuente de financiamiento depende del área elegida?
// El cálculo (estadístico + p-valor) lo hace Apache Commons Math;
// aquí solo se arma la tablita de conteos en orden de aparición.
public class PruebaChiCuadrada {

    // Nivel clásico de la guía: p menor a 5% = dependencia real.
    public static final double UMBRAL_SIGNIFICANCIA = 0.05;

    // cruzar(): recibe dos listas paralelas (una respuesta por fila) y
    // devuelve la matriz + el veredicto. Los vacíos van como "(VACIO)".
    public static ResultadoChiCuadrada cruzar(List<String> columnaA,
            List<String> columnaB) {
        Set<String> categoriasA = new LinkedHashSet<>();
        Set<String> categoriasB = new LinkedHashSet<>();
        List<String> valoresA = new ArrayList<>();
        List<String> valoresB = new ArrayList<>();
        for (int i = 0; i < columnaA.size(); i++) {
            String valorA = textoOCategoria(columnaA.get(i));
            String valorB = textoOCategoria(columnaB.get(i));
            if (valorA.equals(Frecuencias.ETIQUETA_VACIO)
                    && valorB.equals(Frecuencias.ETIQUETA_VACIO)) {
                continue; // fila sin información: no aporta al cruce
            }
            valoresA.add(valorA);
            valoresB.add(valorB);
            categoriasA.add(valorA);
            categoriasB.add(valorB);
        }
        List<String> filas = new ArrayList<>(categoriasA);
        List<String> columnas = new ArrayList<>(categoriasB);
        long[][] conteos = new long[filas.size()][columnas.size()];
        for (int i = 0; i < valoresA.size(); i++) {
            conteos[filas.indexOf(valoresA.get(i))][columnas.indexOf(valoresB.get(i))]++;
        }
        ChiSquareTest prueba = new ChiSquareTest();
        double estadistico = prueba.chiSquare(conteos);
        double valorP;
        try {
            valorP = prueba.chiSquareTest(conteos);
        } catch (Exception tablaMuyChica) {
            valorP = Double.NaN; // tabla degenerada: no calculable
        }
        int gradosLibertad =
                Math.max(0, (filas.size() - 1) * (columnas.size() - 1));
        int[][] observados = new int[filas.size()][columnas.size()];
        for (int f = 0; f < filas.size(); f++) {
            for (int c = 0; c < columnas.size(); c++) {
                observados[f][c] = (int) conteos[f][c];
            }
        }
        return new ResultadoChiCuadrada(estadistico, gradosLibertad, valorP,
                !Double.isNaN(valorP) && valorP < UMBRAL_SIGNIFICANCIA,
                filas, columnas, observados);
    }

    private static String textoOCategoria(String valor) {
        return (valor == null || valor.isEmpty())
                ? Frecuencias.ETIQUETA_VACIO
                : valor;
    }
}
