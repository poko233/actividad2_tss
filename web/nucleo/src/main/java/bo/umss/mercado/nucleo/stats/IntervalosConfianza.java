package bo.umss.mercado.nucleo.stats;

import java.util.List;
import org.apache.commons.math3.distribution.TDistribution;

// IntervalosConfianza: cuantifica la INCERTIDUMBRE de cada indicador.
//   paraMedia      -> t de Student (ideal con muestras chicas).
//   paraProporcion -> Z normal (tasas e intenciones en %).
//   paraDiferencia -> t con varianzas agrupadas (junior VS consolidado).
// La t-Student la calcula Apache Commons Math, sin tablas a mano.
public class IntervalosConfianza {

    // Z para 95% de confianza en proporciones (valor clásico 1.96).
    private static final double Z_95 = 1.96;

    // paraMedia(): x̄ ± t·s/√n. Con 1 solo dato no hay dispersión.
    public static RangoConfianza paraMedia(List<Double> valores) {
        if (valores.isEmpty()) {
            return new RangoConfianza(Double.NaN, Double.NaN);
        }
        ResumenNumerico resumen = Numeros.resumir(valores);
        if (valores.size() == 1 || Double.isNaN(resumen.desv())) {
            return new RangoConfianza(resumen.media(), resumen.media());
        }
        double tCritica = new TDistribution(valores.size() - 1)
                .inverseCumulativeProbability(0.975);
        double margen = tCritica * resumen.desv() / Math.sqrt(valores.size());
        return new RangoConfianza(resumen.media() - margen,
                resumen.media() + margen);
    }

    // paraProporcion(): p̂ ± Z·√(p̂(1-p̂)/n), recortado a [0,1].
    public static RangoConfianza paraProporcion(double proporcionEstimada,
            int totalCasos) {
        if (totalCasos <= 0 || Double.isNaN(proporcionEstimada)) {
            return new RangoConfianza(Double.NaN, Double.NaN);
        }
        double margen = Z_95 * Math.sqrt(
                proporcionEstimada * (1 - proporcionEstimada) / totalCasos);
        return new RangoConfianza(
                Math.max(0, proporcionEstimada - margen),
                Math.min(1, proporcionEstimada + margen));
    }

    // paraDiferencia(): IC de (mediaA - mediaB) con varianza agrupada.
    // Si el 0 queda fuera del rango, los grupos SÍ difieren (§7.1 guía).
    public static RangoConfianza paraDiferencia(List<Double> grupoA,
            List<Double> grupoB) {
        if (grupoA.size() < 2 || grupoB.size() < 2) {
            return new RangoConfianza(Double.NaN, Double.NaN);
        }
        ResumenNumerico resumenA = Numeros.resumir(grupoA);
        ResumenNumerico resumenB = Numeros.resumir(grupoB);
        double varianzaAgrupada = ((grupoA.size() - 1) * resumenA.desv() * resumenA.desv()
                + (grupoB.size() - 1) * resumenB.desv() * resumenB.desv())
                / (grupoA.size() + grupoB.size() - 2);
        double tCritica = new TDistribution(grupoA.size() + grupoB.size() - 2)
                .inverseCumulativeProbability(0.975);
        double margen = tCritica * Math.sqrt(varianzaAgrupada)
                * Math.sqrt(1.0 / grupoA.size() + 1.0 / grupoB.size());
        double diferencia = resumenA.media() - resumenB.media();
        return new RangoConfianza(diferencia - margen, diferencia + margen);
    }
}
