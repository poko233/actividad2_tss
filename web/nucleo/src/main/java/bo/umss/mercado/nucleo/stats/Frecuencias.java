package bo.umss.mercado.nucleo.stats;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Frecuencias: cuenta categorías ("cuántos eligieron IA") y arma
// histogramas (rangos para números). Los vacíos se agrupan como
// "(VACIO)" para que la no-respuesta se vea en vez de ocultarse.
public class Frecuencias {

    public static final String ETIQUETA_VACIO = "(VACIO)";

    // contar(): mapa CATEGORÍA -> cantidad, en orden de aparición.
    public static Map<String, Integer> contar(List<String> respuestas) {
        Map<String, Integer> conteoPorCategoria = new LinkedHashMap<>();
        for (String respuesta : respuestas) {
            String categoria = (respuesta == null || respuesta.isEmpty())
                    ? ETIQUETA_VACIO
                    : respuesta;
            conteoPorCategoria.put(categoria,
                    conteoPorCategoria.getOrDefault(categoria, 0) + 1);
        }
        return conteoPorCategoria;
    }

    // moda(): la categoría más repetida (el "típico" de la muestra).
    public static String moda(Map<String, Integer> conteoPorCategoria) {
        String ganadora = "";
        int mejorConteo = -1;
        for (var categoria : conteoPorCategoria.entrySet()) {
            if (categoria.getValue() > mejorConteo) {
                mejorConteo = categoria.getValue();
                ganadora = categoria.getKey();
            }
        }
        return ganadora;
    }

    // histograma(): divide [mínimo, máximo] en K intervalos iguales y
    // cuenta datos por intervalo. El valor máximo siempre cae en el
    // último intervalo (detalle anti-redondeo).
    public static List<IntervaloHistograma> histograma(List<Double> valores,
            int cantidadIntervalos) {
        List<IntervaloHistograma> barras = new ArrayList<>();
        if (valores.isEmpty() || cantidadIntervalos <= 0) {
            return barras;
        }
        double minimo = valores.stream().mapToDouble(d -> d).min().orElse(0);
        double maximo = valores.stream().mapToDouble(d -> d).max().orElse(0);
        if (minimo == maximo) {
            barras.add(new IntervaloHistograma(minimo, maximo, valores.size()));
            return barras;
        }
        double ancho = (maximo - minimo) / cantidadIntervalos;
        int[] conteos = new int[cantidadIntervalos];
        for (double valor : valores) {
            int indice = (int) Math.floor((valor - minimo) / ancho);
            if (indice >= cantidadIntervalos) {
                indice = cantidadIntervalos - 1;
            }
            if (indice < 0) {
                indice = 0;
            }
            conteos[indice]++;
        }
        for (int i = 0; i < cantidadIntervalos; i++) {
            barras.add(new IntervaloHistograma(minimo + i * ancho,
                    minimo + (i + 1) * ancho, conteos[i]));
        }
        return barras;
    }
}
