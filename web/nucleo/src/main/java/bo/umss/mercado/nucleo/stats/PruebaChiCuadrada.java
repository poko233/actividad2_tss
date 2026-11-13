package bo.umss.mercado.nucleo.stats;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;
import org.apache.commons.math3.stat.inference.ChiSquareTest;

public class PruebaChiCuadrada {

    private static final int REPLICAS_MC = 10000;
    private static final long SEMILLA_MC = 20261113L;

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
        boolean necesitaMonteCarlo = celdasBajas > (r * c * 0.2);

        double chi = Double.NaN;
        double p = Double.NaN;
        try {
            ChiSquareTest test = new ChiSquareTest();
            chi = test.chiSquare(obs);
            if (necesitaMonteCarlo) {
                p = monteCarloPermutacion(obs, chi, REPLICAS_MC);
            } else {
                p = test.chiSquareTest(obs);
            }
        } catch (Exception ex) {
            try {
                if (Double.isNaN(chi)) {
                    chi = new ChiSquareTest().chiSquare(obs);
                }
                p = monteCarloPermutacion(obs, chi, REPLICAS_MC);
            } catch (Exception ex2) {
                // queda NaN
            }
        }

        int gl = (r - 1) * (c - 1);
        double v = Double.NaN;
        int glMin = Math.min(r - 1, c - 1);
        if (glMin > 0 && !Double.isNaN(chi) && n > 0) {
            v = Math.sqrt(chi / (n * (double) glMin));
        }

        return new ResultadoChiCuadrada(
                chi, gl, p, (int) n, filas, columnas, obs, esp, v);
    }

    private static double monteCarloPermutacion(long[][] obs, double chiObservado, int replicas) {
        int r = obs.length;
        int c = obs[0].length;

        List<Integer> filas = new ArrayList<>();
        List<Integer> cols = new ArrayList<>();
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                long v = obs[i][j];
                for (long k = 0; k < v; k++) {
                    filas.add(i);
                    cols.add(j);
                }
            }
        }
        if (filas.isEmpty()) return Double.NaN;

        int[] colArr = new int[cols.size()];
        for (int i = 0; i < cols.size(); i++) colArr[i] = cols.get(i);
        Random rnd = new Random(SEMILLA_MC);
        ChiSquareTest test = new ChiSquareTest();
        int coincidencias = 0;

        for (int rep = 0; rep < replicas; rep++) {
            shuffleInt(colArr, rnd);
            long[][] permObs = new long[r][c];
            for (int i = 0; i < filas.size(); i++) {
                permObs[filas.get(i)][colArr[i]]++;
            }
            try {
                double chiPerm = test.chiSquare(permObs);
                if (chiPerm >= chiObservado) coincidencias++;
            } catch (Exception excepcionNumerica) {
                // Se ignora esa réplica (margen degenerado raro)
            }
        }
        return (coincidencias + 1.0) / (replicas + 1.0);
    }

    private static void shuffleInt(int[] a, Random rnd) {
        for (int i = a.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int tmp = a[i]; a[i] = a[j]; a[j] = tmp;
        }
    }
}