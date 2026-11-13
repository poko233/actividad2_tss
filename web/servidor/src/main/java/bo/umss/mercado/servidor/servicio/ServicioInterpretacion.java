package bo.umss.mercado.servidor.servicio;

import bo.umss.mercado.servidor.servicio.ServicioAnalisis.CeldaMultivariable;
import bo.umss.mercado.servidor.servicio.ServicioAnalisis.TablaMultivariable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ServicioInterpretacion {

    public String interpretar(TablaMultivariable tabla) {
        StringBuilder sb = new StringBuilder();

        if (tabla == null || tabla.celdas().isEmpty()) {
            return "• No hay datos suficientes para generar interpretación.";
        }

        int totalGlobal = 0;
        double sumaPonderada = 0;
        for (var c : tabla.celdas()) {
            if (c.n() > 0 && !Double.isNaN(c.mediaZ())) {
                totalGlobal += c.n();
                sumaPonderada += c.mediaZ() * c.n();
            }
        }
        if (totalGlobal == 0) {
            return "• Ninguna celda tiene datos válidos (n > 0 y Z calculable).";
        }
        double mediaGlobalZ = sumaPonderada / totalGlobal;

        String contexto = detectarContexto(tabla.etiquetaX(), tabla.etiquetaY());
        switch (contexto) {
            case "AREA_FINANCIAMIENTO" ->
                    sb.append(reglasAreaFinanciamiento(tabla, mediaGlobalZ, totalGlobal));
            case "SECTOR_CARGO" ->
                    sb.append(reglasSectorCargo(tabla, mediaGlobalZ, totalGlobal));
            case "SATISFACCION_CONCORDANCIA" ->
                    sb.append(reglasSatisfaccionConcordancia(tabla, mediaGlobalZ, totalGlobal));
            default -> { }
        }

        sb.append(reglasGenericas(tabla, mediaGlobalZ, totalGlobal));

        return sb.toString();
    }

    private String detectarContexto(String x, String y) {
        String xl = x.toLowerCase();
        String yl = y.toLowerCase();
        if ((xl.contains("area") || xl.contains("área")) && yl.contains("financ"))
            return "AREA_FINANCIAMIENTO";
        if (xl.contains("sector") && yl.contains("cargo"))
            return "SECTOR_CARGO";
        if (xl.contains("satisf") && yl.contains("concord"))
            return "SATISFACCION_CONCORDANCIA";
        return "GENERICO";
    }

    private String reglasAreaFinanciamiento(TablaMultivariable tabla,
            double mediaGlobalZ, int totalGlobal) {
        StringBuilder sb = new StringBuilder();

        Map<String, List<CeldaMultivariable>> porArea = agruparPorX(tabla);

        for (var entry : porArea.entrySet()) {
            String area = entry.getKey();
            List<CeldaMultivariable> celdas = entry.getValue();
            if (celdas.isEmpty()) continue;

            CeldaMultivariable top = celdas.stream()
                    .max(Comparator.comparingInt(CeldaMultivariable::n))
                    .orElse(null);
            if (top == null) continue;

            int totalArea = celdas.stream().mapToInt(CeldaMultivariable::n).sum();
            if (totalArea == 0) continue;

            double pct = top.n() * 100.0 / totalArea;
            double sumZ = celdas.stream()
                    .filter(c -> !Double.isNaN(c.mediaZ()))
                    .mapToDouble(c -> c.mediaZ() * c.n())
                    .sum();
            int nConZ = celdas.stream()
                    .filter(c -> !Double.isNaN(c.mediaZ()))
                    .mapToInt(CeldaMultivariable::n)
                    .sum();
            double promZ = nConZ > 0 ? sumZ / nConZ : Double.NaN;

            String concentracion = pct >= 80 ? "fuerte concentración"
                    : pct >= 50 ? "predominio"
                            : "distribución mixta";

            String comparacion = "";
            if (!Double.isNaN(promZ)) {
                if (promZ > mediaGlobalZ * 1.15)
                    comparacion = ", por encima del promedio general";
                else if (promZ < mediaGlobalZ * 0.85)
                    comparacion = ", por debajo del promedio general";
                else
                    comparacion = ", en línea con el promedio general";
            }

            sb.append("• ").append(area).append(": ")
                    .append(totalArea).append(" casos; ")
                    .append(concentracion).append(" en \"")
                    .append(top.valorY()).append("\" (")
                    .append(String.format(Locale.US, "%.1f", pct)).append("%); ");
            if (!Double.isNaN(promZ)) {
                sb.append("antigüedad promedio ")
                        .append(String.format(Locale.US, "%.2f", promZ))
                        .append(" años").append(comparacion);
            }
            sb.append(".\n");
        }

        if (porArea.size() >= 2) {
            sb.append("• Lectura: el financiamiento preferido varía por área de interés, "
                    + "lo que sugiere diseñar ofertas de posgrado con modalidades de pago "
                    + "diferenciadas según el perfil del titulado.\n");
        }

        return sb.toString();
    }

    private String reglasSectorCargo(TablaMultivariable tabla,
            double mediaGlobalZ, int totalGlobal) {
        StringBuilder sb = new StringBuilder();

        Map<String, List<CeldaMultivariable>> porSector = agruparPorX(tabla);

        for (var entry : porSector.entrySet()) {
            String sector = entry.getKey();
            List<CeldaMultivariable> celdas = new ArrayList<>(entry.getValue());
            celdas.sort((a, b) -> Double.compare(b.mediaZ(), a.mediaZ()));
            if (celdas.isEmpty()) continue;

            sb.append("• Sector ").append(sector).append(":\n");
            for (var c : celdas) {
                sb.append("  - ").append(c.valorY()).append(": ")
                        .append(c.n()).append(" casos, remuneración promedio Bs. ")
                        .append(String.format(Locale.US, "%.2f", c.mediaZ())).append(".\n");
            }
        }

        Map<String, Map<String, Double>> porCargo = new LinkedHashMap<>();
        for (var c : tabla.celdas()) {
            if (c.n() == 0) continue;
            porCargo.computeIfAbsent(c.valorY(), k -> new LinkedHashMap<>())
                    .put(c.valorX(), c.mediaZ());
        }
        for (var entry : porCargo.entrySet()) {
            if (entry.getValue().size() >= 2) {
                sb.append("• Cargo \"").append(entry.getKey())
                        .append("\" — comparación entre sectores: ");
                List<String> partes = new ArrayList<>();
                for (var e : entry.getValue().entrySet()) {
                    partes.add(e.getKey() + " = Bs. "
                            + String.format(Locale.US, "%.2f", e.getValue()));
                }
                sb.append(String.join(", ", partes)).append(".\n");
            }
        }

        return sb.toString();
    }

    private String reglasSatisfaccionConcordancia(TablaMultivariable tabla,
            double mediaGlobalZ, int totalGlobal) {
        StringBuilder sb = new StringBuilder();

        CeldaMultivariable mayorGrupo = null;
        for (var c : tabla.celdas()) {
            if (c.n() == 0) continue;
            if (mayorGrupo == null || c.n() > mayorGrupo.n()) mayorGrupo = c;
        }
        if (mayorGrupo != null) {
            double pct = mayorGrupo.n() * 100.0 / totalGlobal;
            sb.append("• El grupo más numeroso es Satisfacción ")
                    .append(mayorGrupo.valorX()).append(" + Concordancia ")
                    .append(mayorGrupo.valorY()).append(": ")
                    .append(mayorGrupo.n()).append(" casos (")
                    .append(String.format(Locale.US, "%.1f", pct))
                    .append("% del total), antigüedad promedio ")
                    .append(String.format(Locale.US, "%.2f", mayorGrupo.mediaZ()))
                    .append(" años.\n");
        }

        CeldaMultivariable altaAlta = buscarCelda(tabla, "Alta", "Alta");
        CeldaMultivariable bajaBaja = buscarCelda(tabla, "Baja", "Baja");
        if (altaAlta != null && bajaBaja != null) {
            String direccion = altaAlta.mediaZ() > bajaBaja.mediaZ()
                    ? "mayor" : "menor";
            sb.append("• Titulados con Satisfacción Alta + Concordancia Alta tienen ")
                    .append(direccion).append(" antigüedad promedio (")
                    .append(String.format(Locale.US, "%.2f", altaAlta.mediaZ()))
                    .append(" vs ")
                    .append(String.format(Locale.US, "%.2f", bajaBaja.mediaZ()))
                    .append(" años). ");
            if (altaAlta.mediaZ() > bajaBaja.mediaZ()) {
                sb.append("La satisfacción se consolida con la trayectoria, "
                        + "siempre que el titulado perciba pertinencia en su trabajo.\n");
            } else {
                sb.append("La insatisfacción no desaparece con la antigüedad, "
                        + "lo que sugiere obsolescencia de contenidos.\n");
            }
        }

        return sb.toString();
    }

    private String reglasGenericas(TablaMultivariable tabla,
            double mediaGlobalZ, int totalGlobal) {
        StringBuilder sb = new StringBuilder();

        CeldaMultivariable max = null;
        CeldaMultivariable min = null;
        List<CeldaMultivariable> gruposPequenios = new ArrayList<>();

        for (var c : tabla.celdas()) {
            if (c.n() == 0 || Double.isNaN(c.mediaZ())) continue;
            if (max == null || c.mediaZ() > max.mediaZ()) max = c;
            if (min == null || c.mediaZ() < min.mediaZ()) min = c;
            if (c.n() < 10) gruposPequenios.add(c);
        }

        String zLegible = tabla.etiquetaZ().replace("_", " ");

        sb.append("• Media global ponderada de ").append(zLegible)
                .append(": ").append(String.format(Locale.US, "%.2f", mediaGlobalZ))
                .append(" sobre ").append(totalGlobal).append(" casos válidos.\n");

        if (max != null) {
            sb.append("• Máximo: ").append(max.valorX()).append(" + ")
                    .append(max.valorY()).append(" = ")
                    .append(String.format(Locale.US, "%.2f", max.mediaZ()))
                    .append(" (n = ").append(max.n()).append(").\n");
        }
        if (min != null && min != max) {
            sb.append("• Mínimo: ").append(min.valorX()).append(" + ")
                    .append(min.valorY()).append(" = ")
                    .append(String.format(Locale.US, "%.2f", min.mediaZ()))
                    .append(" (n = ").append(min.n()).append(").\n");
        }
        if (max != null && min != null && max != min) {
            double rango = max.mediaZ() - min.mediaZ();
            sb.append("• Amplitud entre extremos: ")
                    .append(String.format(Locale.US, "%.2f", rango))
                    .append(" unidades de ").append(zLegible).append(".\n");
        }

        if (!gruposPequenios.isEmpty()) {
            sb.append("• Advertencia: ")
                    .append(gruposPequenios.size())
                    .append(" celda(s) tienen n < 10, por lo que su media puede ser inestable.");
            sb.append(" Interpretar con cautela: ");
            List<String> nombres = new ArrayList<>();
            for (var g : gruposPequenios) {
                nombres.add(g.valorX() + "/" + g.valorY() + " (n=" + g.n() + ")");
            }
            sb.append(String.join(", ", nombres)).append(".\n");
        }

        return sb.toString();
    }

    private Map<String, List<CeldaMultivariable>> agruparPorX(TablaMultivariable tabla) {
        Map<String, List<CeldaMultivariable>> grupos = new LinkedHashMap<>();
        for (var c : tabla.celdas()) {
            if (c.n() == 0) continue;
            grupos.computeIfAbsent(c.valorX(), k -> new ArrayList<>()).add(c);
        }
        return grupos;
    }

    private CeldaMultivariable buscarCelda(TablaMultivariable tabla, String x, String y) {
        for (var c : tabla.celdas()) {
            if (x.equalsIgnoreCase(c.valorX()) && y.equalsIgnoreCase(c.valorY())
                    && c.n() > 0) {
                return c;
            }
        }
        return null;
    }
}