package bo.umss.mercado.servidor.vista;

import bo.umss.mercado.nucleo.guia.GuiasLectura;
import bo.umss.mercado.servidor.servicio.FichaCruce;
import bo.umss.mercado.servidor.servicio.ServicioCruces;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.List;

// VistaCruces: cada cruce en su sección — matriz de conteos pintada
// como mapa de calor (más oscuro = más casos) + veredicto χ² en frase.
// El color lo pone un renderer por celda: sin librerías de gráficos.
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

    // mapaDeCalor(): Grid donde cada fila es [etiqueta, c1, c2, ...].
    // La primera columna es texto; las demás se pintan según el conteo.
    private Grid<List<String>> mapaDeCalor(FichaCruce cruce) {
        int maximo = 1;
        for (int[] fila : cruce.resultado().observados()) {
            for (int valor : fila) {
                maximo = Math.max(maximo, valor);
            }
        }
        final int tope = maximo;
        List<List<String>> filas = new ArrayList<>();
        for (int f = 0; f < cruce.resultado().filas().size(); f++) {
            List<String> fila = new ArrayList<>();
            fila.add(cruce.resultado().filas().get(f));
            for (int c = 0; c < cruce.resultado().columnas().size(); c++) {
                fila.add(String.valueOf(cruce.resultado().observados()[f][c]));
            }
            filas.add(fila);
        }
        Grid<List<String>> tabla = new Grid<>();
        tabla.addColumn(fila -> fila.get(0)).setHeader("");
        for (int c = 0; c < cruce.resultado().columnas().size(); c++) {
            final int indice = c + 1;
            String tituloColumna = cruce.resultado().columnas().get(c);
            // Celda coloreada: intensidad = conteo / máximo.
            tabla.addColumn(new ComponentRenderer<>(Div::new,
                    (celda, fila) -> {
                        int conteo = Integer.parseInt(fila.get(indice));
                        celda.setText(fila.get(indice));
                        int azul = 98 + (int) (157.0 * conteo / tope);
                        celda.getStyle().set("background",
                                "rgb(11,61," + azul + ")");
                        celda.getStyle().set("color", "white");
                        celda.getStyle().set("text-align", "center");
                        celda.setWidthFull();
                    })).setHeader(tituloColumna);
        }
        tabla.setItems(filas);
        tabla.setWidthFull();
        return tabla;
    }
}
