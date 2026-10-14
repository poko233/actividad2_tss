package bo.umss.mercado.nucleo.stats;

// RangoConfianza: intervalo [limiteInferior, limiteSuperior] donde vive
// el valor REAL con 95% de confianza. Ancho chico = estimación precisa.
public record RangoConfianza(double limiteInferior, double limiteSuperior) {

    // ancho(): tamaño del intervalo (para etiquetar su precisión).
    public double ancho() {
        return limiteSuperior - limiteInferior;
    }
}
