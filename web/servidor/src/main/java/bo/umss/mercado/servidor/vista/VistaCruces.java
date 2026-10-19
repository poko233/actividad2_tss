package bo.umss.mercado.servidor.vista;

import bo.umss.mercado.nucleo.guia.GuiasLectura;
import bo.umss.mercado.servidor.servicio.FichaCruce;
import bo.umss.mercado.servidor.servicio.ServicioCruces;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.List;

/**
 * VistaCruces: cada cruce en su sección — matriz de conteos pintada como
 * mapa de calor (más oscuro = más casos) + veredicto χ² en frase.
 */
@Route(value = "cruces", layout = MainLayout.class)
@PageTitle("Cruces χ² | Mercado UMSS")
public class VistaCruces extends VerticalLayout {

    public VistaCruces(ServicioCruces cruces) {
        add(new H1("Cruces de variables (χ²)"));
        add(new Details("Cómo leer estas tablas",
                new Paragraph(GuiasLectura.COMO_LEER_CHI2)));

        add(new H3("Titulados"));
        for (FichaCruce cruce : cruces.crucesTitulados()) {
            add(seccionDeCruce(cruce));
        }
        add(new H3("Empleadores"));
        for (FichaCruce cruce : cruces.crucesEmpleadores()) {
            add(seccionDeCruce(cruce));
        }
    }

    // seccionDeCruce(): título + heatmap + línea χ² + frase simple.
    private VerticalLayout seccionDeCruce(FichaCruce cruce) {
        VerticalLayout seccion = new VerticalLayout();
        seccion.add(new H3(cruce.etiqueta()));
        seccion.add(mapaDeCalor(cruce));
        seccion.add(new Paragraph(String.format(
                "χ²=%.2f   gl=%d   p=%.4f  →  %s",
                cruce.resultado().chi2(), cruce.resultado().gradosLibertad(),
                cruce.resultado().valorP(), cruce.fraseSimple())));
        return seccion;
    }

    // mapaDeCalor(): usa GraficoCanvas.mapaCalor() para dibujar la matriz.
    private Component mapaDeCalor(FichaCruce cruce) {
        int[][] obs = cruce.resultado().observados();
        String[] filas = cruce.resultado().filas()
                .toArray(new String[0]);
        String[] cols = cruce.resultado().columnas()
                .toArray(new String[0]);
        return GraficoCanvas.enTarjeta(
                GraficoCanvas.mapaCalor(
                        "heatmap_" + cruce.etiqueta().replace(" ", "_"),
                        obs, filas, cols,
                        cruce.etiqueta()),
                "Mapa de calor: " + cruce.etiqueta());
    }
}
