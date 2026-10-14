package bo.umss.mercado.servidor.servicio;

// FichaSegmento: UNA fila de la tabla Junior/Intermedio/Consolidado.
// tasaDesempleo = fracción de su vida profesional sin empleo
// (0.67 significa "2 de cada 3 años desempleado").
public record FichaSegmento(
        String nombre,
        int total,
        double satisfaccionMedia,
        double intencionPosgradoPct,
        double tasaDesempleo) {
}
