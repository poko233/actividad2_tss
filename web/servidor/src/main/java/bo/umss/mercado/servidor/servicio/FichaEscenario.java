package bo.umss.mercado.servidor.servicio;

// FichaEscenario: el pronóstico de UN programa (ej. "maestría en IA").
// demandaMedia = inscritos esperados; limites = rango probable (IC95).
public record FichaEscenario(
        String titulo,
        String area,
        double proporcionEmpirica,
        double demandaMedia,
        double limiteInferior,
        double limiteSuperior,
        String fraseSimple) {
}
