package bo.umss.mercado.servidor.servicio;

public record ConfiguracionSimulacion(
    String nombreEscenario,
    String nombreVariable,
    String categoriaObjetivo,
    double probabilidadInteresados,
    double probabilidadResto,
    int iteraciones,
    long semilla,
    boolean usarEmpleadores
) {
}