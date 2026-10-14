package bo.umss.mercado.nucleo.stats;

// IntervaloHistograma: UNA barra del histograma.
// inicio/fin = límites del intervalo, conteo = cuántos datos cayeron dentro.
public record IntervaloHistograma(double inicio, double fin, int conteo) {
}
