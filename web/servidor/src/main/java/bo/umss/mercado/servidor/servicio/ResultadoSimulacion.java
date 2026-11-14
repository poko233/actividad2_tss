package bo.umss.mercado.servidor.servicio;

import java.util.List;
import java.util.Map;

public record ResultadoSimulacion(
    double proporcionEmpirica,
    int cantidadInteresados,
    int poblacionTotal,
    double media,
    double desviacionEstandar,
    double icInferior,
    double icSuperior,
    List<Integer> primerasIteraciones,
    Map<Integer, Integer> histograma,
    double mediaPesimista,
    double mediaBase,
    double mediaOptimista
) {
}