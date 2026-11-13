package bo.umss.mercado.nucleo.stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// HolmCorreccion: ajusta p-valores de multiples cruces para evitar
// falsos positivos. Formula: p_adj_(i) = min(1, max(p_adj_(i-1), (m-i+1) * p_(i)))
// con p-valores ordenados ascendentemente. Es mas potente que Bonferroni
// y la familia de 15 pruebas del cap. 3 ya lo usa.
public class HolmCorreccion {

    public record ItemHolm(int indice, double valorPOriginal,
            double valorPAjustado, boolean significativo) {}

    public static List<ItemHolm> ajustar(List<Double> valoresP) {
        int m = valoresP.size();
        Integer[] orden = new Integer[m];
        for (int i = 0; i < m; i++) orden[i] = i;
        Arrays.sort(orden, (a, b) -> Double.compare(valoresP.get(a), valoresP.get(b)));

        double[] ajustado = new double[m];
        double acumulado = 0;
        for (int k = 0; k < m; k++) {
            int idx = orden[k];
            double crudo = valoresP.get(idx);
            double corregido = Math.min(1.0, (m - k) * (Double.isNaN(crudo) ? 1.0 : crudo));
            corregido = Math.max(corregido, acumulado);
            acumulado = corregido;
            ajustado[idx] = Double.isNaN(crudo) ? Double.NaN : corregido;
        }

        List<ItemHolm> salida = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            salida.add(new ItemHolm(i, valoresP.get(i), ajustado[i],
                    !Double.isNaN(ajustado[i]) && ajustado[i] < 0.05));
        }
        return salida;
    }
}
