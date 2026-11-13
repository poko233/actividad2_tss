package bo.umss.mercado.nucleo.stats;

import java.util.List;

public record ResultadoChiCuadrada(
        double chiCuadrada,
        int gradosLibertad,
        double valorP,
        int n,
        List<String> filas,
        List<String> columnas,
        long[][] observadas,
        double[][] esperadas,
        double vCramer) {

    public static ResultadoChiCuadrada vacio() {
        return new ResultadoChiCuadrada(Double.NaN, 0, Double.NaN, 0,
                List.of(), List.of(), new long[0][0], new double[0][0], Double.NaN);
    }

    public boolean significativa() {
        return !Double.isNaN(valorP) && valorP < 0.05;
    }

    public String intensidadVCramer() {
        if (Double.isNaN(vCramer)) return "sin dato";
        int glMin = Math.min(filas.size() - 1, columnas.size() - 1);
        if (glMin <= 0) return "sin dato";
        double pequenio, mediano, grande;
        switch (glMin) {
            case 1 -> { pequenio = 0.10; mediano = 0.30; grande = 0.50; }
            case 2 -> { pequenio = 0.07; mediano = 0.21; grande = 0.35; }
            case 3 -> { pequenio = 0.06; mediano = 0.17; grande = 0.29; }
            default -> { pequenio = 0.05; mediano = 0.15; grande = 0.25; }
        }
        if (vCramer < pequenio) return "despreciable";
        if (vCramer < mediano) return "pequeno";
        if (vCramer < grande) return "mediano";
        return "grande";
    }
}