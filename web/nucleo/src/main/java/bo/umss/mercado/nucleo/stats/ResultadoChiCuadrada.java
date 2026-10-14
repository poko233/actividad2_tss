package bo.umss.mercado.nucleo.stats;

import java.util.List;

// ResultadoChiCuadrada: el veredicto de cruzar dos preguntas.
// chi2 = distancia a "no hay relación"; gradosLibertad = tamaño de tabla;
// valorP = probabilidad de ver esto por azar (p < 0.05 = SÍ hay relación).
// observados[fila][columna] = la matriz de conteos para dibujarla.
public record ResultadoChiCuadrada(
        double chi2,
        int gradosLibertad,
        double valorP,
        boolean hayDependencia,
        List<String> filas,
        List<String> columnas,
        int[][] observados) {
}
