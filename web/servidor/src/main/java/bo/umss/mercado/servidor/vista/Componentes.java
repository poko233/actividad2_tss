package bo.umss.mercado.servidor.vista;

import bo.umss.mercado.nucleo.stats.RangoConfianza;
import bo.umss.mercado.nucleo.stats.ResumenNumerico;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;
import java.util.Map;

public final class Componentes {

    private Componentes() {}

    public static VerticalLayout seccion(String titulo, String interpretacion,
            Component... contenido) {
        VerticalLayout bloque = new VerticalLayout();
        bloque.setPadding(false);
        bloque.setSpacing(true);
        H3 h = new H3(titulo);
        h.getStyle().set("margin-bottom", "4px");
        bloque.add(h);
        for (Component c : contenido) bloque.add(c);
        if (interpretacion != null && !interpretacion.isEmpty()) {
            Paragraph p = new Paragraph(interpretacion);
            p.getStyle().set("font-style", "italic")
                    .set("color", "#555")
                    .set("font-size", "13px");
            bloque.add(p);
        }
        return bloque;
    }

    public static Div tarjetaKpi(String etiqueta, String valor, String subtitulo) {
        Div tarjeta = new Div();
        tarjeta.getStyle()
                .set("background", "white")
                .set("border-radius", "10px")
                .set("padding", "14px 18px")
                .set("box-shadow", "0 1px 4px rgba(0,0,0,.12)")
                .set("min-width", "180px")
                .set("flex", "1");
        Div t = new Div();
        t.setText(etiqueta);
        t.getStyle().set("font-size", "12px").set("color", "#666")
                .set("margin-bottom", "6px");
        Div v = new Div();
        v.setText(valor);
        v.getStyle().set("font-size", "22px").set("font-weight", "bold")
                .set("color", "#0b3d62");
        Div s = new Div();
        s.setText(subtitulo);
        s.getStyle().set("font-size", "11px").set("color", "#888")
                .set("margin-top", "4px");
        tarjeta.add(t, v, s);
        return tarjeta;
    }

    public static Div tarjetaKpiConIC(String etiqueta, double proporcion,
            RangoConfianza ic, String subtitulo) {
        String valorPrincipal = redondear(proporcion * 100, 1) + "%";
        String rangoIC = "IC 95%: ["
                + redondear(ic.limiteInferior() * 100, 1) + "%, "
                + redondear(ic.limiteSuperior() * 100, 1) + "%]";
        return tarjetaKpi(etiqueta, valorPrincipal, rangoIC + " · " + subtitulo);
    }

    public static HorizontalLayout filaKpis(Component... tarjetas) {
        HorizontalLayout fila = new HorizontalLayout(tarjetas);
        fila.setWidthFull();
        fila.getStyle().set("gap", "12px").set("flex-wrap", "wrap");
        return fila;
    }

    public static Grid<Map.Entry<String, Integer>> tablaFrecuencias(
            Map<String, Integer> frecuencias, String encabezadoCategoria) {
        Grid<Map.Entry<String, Integer>> grid = new Grid<>();
        grid.addColumn(Map.Entry::getKey).setHeader(encabezadoCategoria).setAutoWidth(true);
        grid.addColumn(Map.Entry::getValue).setHeader("Frecuencia (n)").setAutoWidth(true);

        int total = frecuencias.values().stream().mapToInt(Integer::intValue).sum();
        grid.addColumn(e -> redondear(100.0 * e.getValue() / Math.max(1, total), 1) + "%")
                .setHeader("Porcentaje (%)").setAutoWidth(true);

        grid.setItems(frecuencias.entrySet());
        grid.setAllRowsVisible(true);
        grid.setWidthFull();
        return grid;
    }

    public static Grid<String[]> tablaResumenNumerico(ResumenNumerico r, String unidad) {
        Grid<String[]> grid = new Grid<>();
        grid.addColumn(row -> row[0]).setHeader("Estadístico").setAutoWidth(true);
        grid.addColumn(row -> row[1]).setHeader("Valor").setAutoWidth(true);
        grid.setItems(List.of(
                new String[]{"n", String.valueOf(r.totalDatos())},
                new String[]{"Media", redondear(r.media(), 2) + " " + unidad},
                new String[]{"Mediana", redondear(r.mediana(), 2) + " " + unidad},
                new String[]{"Desviación estándar", redondear(r.desv(), 2) + " " + unidad},
                new String[]{"Mínimo", redondear(r.minimo(), 2) + " " + unidad},
                new String[]{"Máximo", redondear(r.maximo(), 2) + " " + unidad},
                new String[]{"Q1", redondear(r.q1(), 2) + " " + unidad},
                new String[]{"Q3", redondear(r.q3(), 2) + " " + unidad},
                new String[]{"Coef. Variación", cvTexto(r)}
        ));
        grid.setAllRowsVisible(true);
        grid.setWidthFull();
        return grid;
    }

    private static String cvTexto(ResumenNumerico r) {
        if (Double.isNaN(r.media()) || Double.isNaN(r.desv()) || r.media() == 0) return "n/d";
        double cv = 100.0 * r.desv() / r.media();
        String interpretacion = cv > 30 ? "(alta heterogeneidad)" : "(muestra homogénea)";
        return redondear(cv, 1) + "% " + interpretacion;
    }

    public static String redondear(double v, int decimales) {
        if (Double.isNaN(v)) return "n/d";
        double factor = Math.pow(10, decimales);
        return String.valueOf(Math.round(v * factor) / factor);
    }
}