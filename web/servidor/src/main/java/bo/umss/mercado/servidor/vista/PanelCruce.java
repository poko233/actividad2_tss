package bo.umss.mercado.servidor.vista;

import bo.umss.mercado.nucleo.stats.ResultadoChiCuadrada;
import bo.umss.mercado.servidor.servicio.FichaCruce;
import bo.umss.mercado.servidor.servicio.ServicioCruces;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.ArrayList;
import java.util.List;

public final class PanelCruce {

    private PanelCruce() {}

    public static VerticalLayout render(FichaCruce cruce, ServicioCruces servicioCruces) {
        VerticalLayout bloque = new VerticalLayout();
        bloque.setPadding(false);
        bloque.setSpacing(true);

        ResultadoChiCuadrada r = cruce.resultado();

        H4 titulo = new H4(cruce.etiqueta());
        titulo.getStyle().set("margin-bottom", "2px");
        bloque.add(titulo);

        UnorderedList hip = new UnorderedList(
                new ListItem("H₀: " + cruce.columnaA() + " y " + cruce.columnaB()
                        + " son independientes."),
                new ListItem("H₁: existe asociación entre las variables."));
        hip.getStyle().set("font-size", "12px").set("color", "#666")
                .set("margin", "0").set("padding-left", "22px");
        bloque.add(hip);

        if (r.observadas() != null && r.observadas().length > 0) {
            int[][] obs = new int[r.observadas().length][];
            for (int i = 0; i < r.observadas().length; i++) {
                obs[i] = new int[r.observadas()[i].length];
                for (int j = 0; j < r.observadas()[i].length; j++) {
                    obs[i][j] = (int) r.observadas()[i][j];
                }
            }
            String[] filas = r.filas().toArray(new String[0]);
            String[] cols = r.columnas().toArray(new String[0]);
            Component heatmap = GraficoCanvas.enTarjeta(
                    GraficoCanvas.mapaCalor("heat_" + Math.abs(cruce.etiqueta().hashCode()),
                            obs, filas, cols, "Frecuencias observadas"),
                    "Heatmap: " + cruce.etiqueta());
            bloque.add(heatmap);
        }

        bloque.add(new H4("Frecuencias observadas"));
        bloque.add(tablaObservadas(r));

        bloque.add(new H4("Frecuencias esperadas"));
        bloque.add(tablaEsperadas(r));

        Paragraph stats = new Paragraph(
                "χ² = " + Componentes.redondear(r.chiCuadrada(), 3)
                + "   ·   gl = " + r.gradosLibertad()
                + "   ·   p = " + Componentes.redondear(r.valorP(), 4)
                + "   ·   V de Cramér = " + Componentes.redondear(r.vCramer(), 3)
                + " (" + r.intensidadVCramer() + ")");
        stats.getStyle().set("font-weight", "bold").set("margin-top", "8px");
        bloque.add(stats);

        Paragraph veredicto = new Paragraph("→ " + cruce.fraseSimple());
        veredicto.getStyle().set("font-weight", "bold").set("margin-top", "6px")
                .set("color", "#2c3e50");
        bloque.add(veredicto);

        if (servicioCruces != null) {
            List<double[]> ics = servicioCruces.wilsonPorFila(r);
            if (!ics.isEmpty()) {
                bloque.add(new H4("Intervalos de confianza por fila (Wilson 95%)"));
                bloque.add(tablaWilson(r, ics));
            }
        }

        Paragraph sep = new Paragraph("──────────────");
        sep.getStyle().set("color", "#ccc").set("text-align", "center").set("margin", "12px 0");
        bloque.add(sep);

        return bloque;
    }

    private static Grid<String[]> tablaObservadas(ResultadoChiCuadrada r) {
        Grid<String[]> grid = new Grid<>();
        grid.addColumn(row -> row[0]).setHeader("Fila \\ Columna").setAutoWidth(true);
        int c = r.columnas().size();
        for (int j = 0; j < c; j++) {
            final int col = j;
            grid.addColumn(row -> row[col + 1]).setHeader(r.columnas().get(j)).setAutoWidth(true);
        }
        grid.addColumn(row -> row[c + 1]).setHeader("Total").setAutoWidth(true);

        List<String[]> filas = new ArrayList<>();
        for (int i = 0; i < r.filas().size(); i++) {
            String[] fila = new String[c + 2];
            fila[0] = r.filas().get(i);
            long total = 0;
            for (int j = 0; j < c; j++) {
                fila[j + 1] = String.valueOf(r.observadas()[i][j]);
                total += r.observadas()[i][j];
            }
            fila[c + 1] = String.valueOf(total);
            filas.add(fila);
        }
        String[] totalFila = new String[c + 2];
        totalFila[0] = "Total";
        long granTotal = 0;
        for (int j = 0; j < c; j++) {
            long suma = 0;
            for (int i = 0; i < r.filas().size(); i++) suma += r.observadas()[i][j];
            totalFila[j + 1] = String.valueOf(suma);
            granTotal += suma;
        }
        totalFila[c + 1] = String.valueOf(granTotal);
        filas.add(totalFila);

        grid.setItems(filas);
        grid.setAllRowsVisible(true);
        grid.setWidthFull();
        return grid;
    }

    private static Grid<String[]> tablaEsperadas(ResultadoChiCuadrada r) {
        Grid<String[]> grid = new Grid<>();
        grid.addColumn(row -> row[0]).setHeader("Fila \\ Columna").setAutoWidth(true);
        int c = r.columnas().size();
        for (int j = 0; j < c; j++) {
            final int col = j;
            grid.addColumn(row -> row[col + 1]).setHeader(r.columnas().get(j)).setAutoWidth(true);
        }
        List<String[]> filas = new ArrayList<>();
        for (int i = 0; i < r.filas().size(); i++) {
            String[] fila = new String[c + 1];
            fila[0] = r.filas().get(i);
            for (int j = 0; j < c; j++) {
                fila[j + 1] = Componentes.redondear(r.esperadas()[i][j], 2);
            }
            filas.add(fila);
        }
        grid.setItems(filas);
        grid.setAllRowsVisible(true);
        grid.setWidthFull();
        return grid;
    }

    private static Grid<String[]> tablaWilson(ResultadoChiCuadrada r, List<double[]> ics) {
        Grid<String[]> grid = new Grid<>();
        grid.addColumn(row -> row[0]).setHeader("Fila").setAutoWidth(true);
        grid.addColumn(row -> row[1]).setHeader("n fila").setAutoWidth(true);
        grid.addColumn(row -> row[2]).setHeader("Proporción col. 1").setAutoWidth(true);
        grid.addColumn(row -> row[3]).setHeader("IC 95% Wilson").setAutoWidth(true);

        List<String[]> filas = new ArrayList<>();
        int c = r.columnas().size();
        for (int i = 0; i < r.filas().size(); i++) {
            long total = 0;
            for (int j = 0; j < c; j++) total += r.observadas()[i][j];
            if (total == 0) continue;
            long obsCol1 = r.observadas()[i][0];
            double p = (double) obsCol1 / total;
            double[] ic = ics.get(i * c);
            filas.add(new String[]{
                    r.filas().get(i),
                    String.valueOf(total),
                    Componentes.redondear(p * 100, 1) + "%",
                    "[" + Componentes.redondear(ic[0] * 100, 1) + "%, "
                            + Componentes.redondear(ic[1] * 100, 1) + "%]"
            });
        }
        grid.setItems(filas);
        grid.setAllRowsVisible(true);
        grid.setWidthFull();
        return grid;
    }
}