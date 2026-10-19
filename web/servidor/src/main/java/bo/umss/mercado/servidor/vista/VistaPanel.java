package bo.umss.mercado.servidor.vista;

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

/**
 * VistaPanel: portada principal con los 4 indicadores clave (KPIs),
 * tamaño de muestra y recomendaciones automáticas.
 * Cada KPI va dentro de una tarjeta con su valor numérico.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Panel | Mercado UMSS")
public class VistaPanel extends VerticalLayout {

    public VistaPanel(ServicioAnalisis analisis,
            ServicioRecomendaciones recomendaciones,
            RepositorioTitulados repoTitulados,
            RepositorioEmpleadores repoEmpleadores) {
        H1 titulo = new H1("Panel de mercado");
        Div muestra = new Div();
        muestra.setText("Titulados N=" + repoTitulados.count()
                + "  ·  Empleadores M=" + repoEmpleadores.count()
                + "  ·  Base: mercado.db (SQLite)");
        muestra.getStyle().set("margin-bottom", "10px").set("color", "#666666");
        add(titulo, muestra);

        // ---------- 4 tarjetas KPI ----------
        double insercion = analisis.tasaInsercion();
        double intencion = analisis.intencionPosgrado();
        double beca = analisis.proporcionRequiereBeca();
        double antiguedad = analisis.resumenNumerico(
                "anios_vida_profesional").media();

        HorizontalLayout tarjetas = new HorizontalLayout(
            tarjetaKpi("Antigüedad media",
                    textoONoDato(antiguedad, " años"),
                    "Años transcurridos desde la titulación"),
            tarjetaKpi("Tasa de inserción",
                    textoONoDato(insercion * 100, "%"),
                    "Porcentaje de titulados que trabajan o emprenden"),
            tarjetaKpi("Quieren posgrado",
                    textoONoDato(intencion * 100, "%"),
                    "Porcentaje que declaró interés en continuar estudios"),
            tarjetaKpi("Requieren beca",
                    textoONoDato(beca * 100, "%"),
                    "Porcentaje que necesitaría financiamiento para posgrado")
        );
        tarjetas.setWidthFull();
        tarjetas.getStyle().set("gap", "12px").set("flex-wrap", "wrap");
        add(tarjetas);

        add(new H3("Recomendaciones automáticas"));
        for (String recomendacion : recomendaciones.generar()) {
            add(new Paragraph("• " + recomendacion));
        }

        add(new H3("Ir a"));
        HorizontalLayout atajos = new HorizontalLayout(
                botonIr("Ver titulados", VistaTitulados.class),
                botonIr("Ver cruces χ²", VistaCruces.class),
                botonIr("Correr simulación", VistaSimulacion.class),
                botonIr("Registrar encuesta", VistaRegistro.class));
        atajos.setWidthFull();
        atajos.getStyle().set("gap", "8px").set("flex-wrap", "wrap");
        add(atajos);
    }

    // tarjetaKpi(): cajita blanca con título, valor y subtítulo explicativo.
    private Div tarjetaKpi(String titulo, String valor, String subtitulo) {
        Div tarjeta = new Div();
        tarjeta.getStyle().set("background", "white")
                .set("border-radius", "10px")
                .set("padding", "14px")
                .set("box-shadow", "0 1px 4px rgba(0,0,0,.15)")
                .set("min-width", "180px")
                .set("flex", "1");
        Div t = new Div();
        t.setText(titulo);
        t.getStyle().set("font-size", "13px").set("color", "#666666")
                .set("margin-bottom", "6px");
        Div v = new Div();
        v.setText(valor);
        v.getStyle().set("font-size", "22px").set("font-weight", "bold")
                .set("color", "#0b3d62");
        Div s = new Div();
        s.setText(subtitulo);
        s.getStyle().set("font-size", "12px").set("color", "#888888")
                .set("margin-top", "4px");
        tarjeta.add(t, v, s);
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
