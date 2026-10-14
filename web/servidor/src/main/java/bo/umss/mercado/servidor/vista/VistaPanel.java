package bo.umss.mercado.servidor.vista;

import bo.umss.mercado.nucleo.stats.RangoConfianza;
import bo.umss.mercado.servidor.datos.RepositorioEmpleadores;
import bo.umss.mercado.servidor.datos.RepositorioTitulados;
import bo.umss.mercado.servidor.servicio.ServicioAnalisis;
import bo.umss.mercado.servidor.servicio.ServicioRecomendaciones;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

// VistaPanel: portada con los 4 indicadores clave (KPIs), el tamaño de
// cada muestra y las recomendaciones automáticas. Todo lo calcula
// ServicioAnalisis; aquí solo se muestra en tarjetas.
@Route(value = "", layout = MainLayout.class)
@PageTitle("Panel | Mercado UMSS")
public class VistaPanel extends VerticalLayout {

    public VistaPanel(ServicioAnalisis analisis,
            ServicioRecomendaciones recomendaciones,
            RepositorioTitulados repoTitulados,
            RepositorioEmpleadores repoEmpleadores) {
        H1 titulo = new H1("Panel de mercado");
        Paragraph muestra = new Paragraph("Titulados N=" + repoTitulados.count()
                + " · Empleadores M=" + repoEmpleadores.count()
                + " · Base: mercado.db (SQLite)");
        add(titulo, muestra);

        // Las 4 tarjetas KPI.
        double insercion = analisis.tasaInsercion();
        double intencion = analisis.intencionPosgrado();
        double beca = analisis.proporcionRequiereBeca();
        double antiguedad = analisis.resumenNumerico(
                "anios_vida_profesional").media();
        RangoConfianza rangoInsercion =
                analisis.rangoDe(insercion, (int) repoTitulados.count());
        HorizontalLayout tarjetas = new HorizontalLayout(
                tarjeta("Antigüedad media",
                        textoONoDato(antiguedad, " años")),
                tarjeta("Tasa de inserción",
                        textoONoDato(insercion * 100, "%")
                                + "  (entre " + Math.round(
                                        rangoInsercion.limiteInferior() * 100)
                                + " y " + Math.round(
                                        rangoInsercion.limiteSuperior() * 100)
                                + " de cada 100)"),
                tarjeta("Quieren posgrado",
                        textoONoDato(intencion * 100, "%")),
                tarjeta("Necesitarían beca",
                        textoONoDato(beca * 100, "%")));
        tarjetas.setWidthFull();
        add(tarjetas);

        // Recomendaciones automáticas (cap. 6 del informe).
        add(new H3("Recomendaciones automáticas"));
        for (String recomendacion : recomendaciones.generar()) {
            add(new Paragraph("• " + recomendacion));
        }

        // Atajos a las demás pantallas.
        add(new H3("Ir a"));
        HorizontalLayout atajos = new HorizontalLayout(
                botonIr("Ver titulados", VistaTitulados.class),
                botonIr("Ver cruces χ²", VistaCruces.class),
                botonIr("Correr simulación", VistaSimulacion.class),
                botonIr("Registrar encuesta", VistaRegistro.class));
        add(atajos);
    }

    // tarjeta(): cajita blanca con título grande y valor.
    private Div tarjeta(String titulo, String valor) {
        Div tarjeta = new Div();
        tarjeta.getStyle().set("background", "white")
                .set("border-radius", "10px")
                .set("padding", "14px")
                .set("box-shadow", "0 1px 4px rgba(0,0,0,.15)")
                .set("min-width", "200px");
        Div tituloDiv = new Div();
        tituloDiv.setText(titulo);
        Div valorDiv = new Div();
        valorDiv.setText(valor);
        valorDiv.getStyle().set("font-size", "1.4em").set("font-weight", "bold");
        tarjeta.add(tituloDiv, valorDiv);
        return tarjeta;
    }

    private String textoONoDato(double numero, String unidad) {
        if (Double.isNaN(numero)) {
            return "sin dato";
        }
        return String.format("%.1f%s", numero, unidad);
    }

    private Button botonIr(String texto, Class<?> vista) {
        Button boton = new Button(texto);
        boton.addClickListener(clic -> UI.getCurrent().navigate((Class) vista));
        return boton;
    }
}
