package bo.umss.mercado.servidor.servicio;

import bo.umss.mercado.nucleo.stats.ResultadoChiCuadrada;

// FichaCruce: UN cruce de dos preguntas con su veredicto en frase simple
// (para no obligar a nadie a releer el número p).
public record FichaCruce(
        String etiqueta,
        String columnaA,
        String columnaB,
        ResultadoChiCuadrada resultado,
        String fraseSimple) {
}
