package bo.umss.mercado.nucleo.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

// Numeros: resume UNA lista de números (edad, antigüedad...).
// Todos los cálculos los hace Apache Commons Math; aquí solo
// se ordenan y se empaquetan en un ResumenNumerico.
// Con menos de 2 datos no hay dispersión: desv llega como NaN.
public class Numeros {

    // resumir(): calcula los 9 estadísticos de una sola pasada.
    public static ResumenNumerico resumir(List<Double> valores) {
        if (valores.isEmpty()) {
            double vacio = Double.NaN;
            return new ResumenNumerico(0, vacio, vacio, vacio, vacio,
                    vacio, vacio, vacio, vacio);
        }
        DescriptiveStatistics calculadora = new DescriptiveStatistics();
        for (double valor : valores) {
            calculadora.addValue(valor);
        }
        return new ResumenNumerico(
                valores.size(),
                calculadora.getMean(),
                calculadora.getPercentile(50),
                calculadora.getStandardDeviation(),
                calculadora.getMin(),
                calculadora.getMax(),
                calculadora.getPercentile(25),
                calculadora.getPercentile(50),
                calculadora.getPercentile(75));
    }

    // promedio(): atajo para cuando solo se necesita la media.
    public static double promedio(List<Double> valores) {
        if (valores.isEmpty()) {
            return Double.NaN;
        }
        return resumir(valores).media();
    }

    // ordenar(): copia ordenada (para medianas manuales si hicieran falta).
    public static List<Double> ordenar(List<Double> valores) {
        List<Double> copia = new ArrayList<>(valores);
        Collections.sort(copia);
        return copia;
    }
}
