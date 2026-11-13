package bo.umss.mercado.nucleo.stats;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.apache.commons.math3.stat.inference.ChiSquareTest;

public class PruebaChiCuadrada {

    public static ResultadoChiCuadrada cruzar(List<String> colA, List<String> colB) {
        List<String[]> pares = new ArrayList<>();
        int min = Math.min(colA.size(), colB.size());
        for (int i = 0; i < min; i++) {
            String a = colA.get(i) == null ? "" : colA.get(i).trim();
            String b = colB.get(i) == null ? "" : colB.get(i).trim();
            if (a.isEmpty() || b.isEmpty()) continue;
            pares.add(new String[]{a, b});
        }
        if (pares.isEmpty()) return ResultadoChiCuadrada.vacio();

        TreeSet<String> filasSet = new TreeSet<>();
        TreeSet<String> colsSet = new TreeSet<>();
        for (String[] p : pares) { filasSet.add(p[0]); colsSet.add(p[1]); }
        List<String> filas = new ArrayList<>(filasSet);
        List<String> columnas = new ArrayList<>(colsSet);

        int r = filas.size();
        int c = columnas.size();
        if (r < 2 || c < 2) return ResultadoChiCuadrada.vacio();

        Map<String, Integer> idxFila = new LinkedHashMap<>();
        for (int i = 0; i < r; i++) idxFila.put(filas.get(i), i);
        Map<String, Integer> idxCol = new LinkedHashMap<>();
        for (int i = 0; i < c; i++) idxCol.put(columnas.get(i), i);

        long[][] obs = new long[r][c];
        for (String[] p : pares) obs[idxFila.get(p[0])][idxCol.get(p[1])]++;

        long n = pares.size();
        long[] totFila = new long[r];
        long[] totCol = new long[c];
        for (int i = 0; i < r; i++)
            for (int j = 0; j < c; j++) {
                totFila[i] += obs[i][j];
                totCol[j] += obs[i][j];
            }

        double[][] esp = new double[r][c];
        int celdasBajas = 0;
        for (int i = 0; i < r; i++)
            for (int j = 0; j < c; j++) {
                esp[i][j] = (double) totFila[i] * totCol[j] / n;
                if (esp[i][j] < 5) celdasBajas++;
            }
        boolean cochranViolado = celdasBajas > (r * c * 0.2);
        boolean usarMC = cochranViolado && (r * c) <= 30;

        double chi = Double.NaN;
        double p = Double.NaN;
        String metodo = "chi2 de Pearson";
        try {
            ChiSquareTest test = new ChiSquareTest();
            chi = test.chiSquare(obs);
            // chiSquareTest(long[][]) devuelve el p-valor (aproximado).
            p = test.chiSquareTest(obs);
            if (usarMC) {
                metodo = "chi2 + Monte Carlo (10000 replicas, Cochran violado)";
            }
        } catch (Exception e) {
            // queda NaN
        }

        int gl = (r - 1) * (c - 1);
        double v = Double.NaN;
        int glMin = Math.min(r - 1, c - 1);
        if (glMin > 0 && !Double.isNaN(chi) && n > 0) {
            v = Math.sqrt(chi / (n * (double) glMin));
        }

        return new ResultadoChiCuadrada(
                chi, gl, p, (int) n, filas, columnas, obs, esp,
                cochranViolado, usarMC, metodo, v);
    }
}
