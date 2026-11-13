package bo.umss.mercado.nucleo.stats;

import java.util.List;

// CronbachAlpha: consistencia interna de un conjunto de items Likert.
// Formula: alpha = (k / (k-1)) * (1 - Sum var(item) / var(total))
// Solo considera filas completas (todas las columnas del bloque).
// Interpretacion estandar: alpha>=0.9 excelente, >=0.8 buena, >=0.7 aceptable.
public class CronbachAlpha {

    public static double calcular(List<List<Double>> filasCompletas, int k) {
        if (filasCompletas.isEmpty() || k < 2) return Double.NaN;
        int n = filasCompletas.size();

        double[] totales = new double[n];
        for (int i = 0; i < n; i++) {
            List<Double> fila = filasCompletas.get(i);
            if (fila.size() != k) return Double.NaN;
            double s = 0;
            for (double v : fila) s += v;
            totales[i] = s;
        }
        double varTotal = varianza(totales);
        if (varTotal == 0) return Double.NaN;

        double sumVarItems = 0;
        for (int j = 0; j < k; j++) {
            double[] col = new double[n];
            for (int i = 0; i < n; i++) col[i] = filasCompletas.get(i).get(j);
            sumVarItems += varianza(col);
        }
        double alpha = (k / (double) (k - 1)) * (1 - sumVarItems / varTotal);
        return Math.max(0, Math.min(1, alpha)); // acotado
    }

    private static double varianza(double[] v) {
        if (v.length < 2) return 0;
        double media = 0;
        for (double x : v) media += x;
        media /= v.length;
        double s = 0;
        for (double x : v) s += (x - media) * (x - media);
        return s / (v.length - 1);
    }

    public static String descripcion(double alpha) {
        if (Double.isNaN(alpha)) return "sin datos";
        if (alpha >= 0.9) return "excelente (alpha >= 0.90)";
        if (alpha >= 0.8) return "buena (0.80 <= alpha < 0.90)";
        if (alpha >= 0.7) return "aceptable (0.70 <= alpha < 0.80)";
        if (alpha >= 0.6) return "cuestionable (0.60 <= alpha < 0.70)";
        return "pobre (alpha < 0.60)";
    }
}
