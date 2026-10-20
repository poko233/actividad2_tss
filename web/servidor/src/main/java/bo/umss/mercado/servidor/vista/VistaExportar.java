package bo.umss.mercado.servidor.vista;

import bo.umss.mercado.servidor.servicio.ServicioExportacion;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;

/**
 * VistaExportar: botones para descargar los CSV (HU5). Cada archivo se
 * genera en el momento de la descarga con los datos de la base, asi que
 * lo que se descarga es exactamente lo que se ve en las pantallas.
 */
@Route(value = "exportar", layout = MainLayout.class)
@PageTitle("Exportar | Mercado UMSS")
public class VistaExportar extends VerticalLayout {

    public VistaExportar(ServicioExportacion exportacion) {
        add(new H1("Exportar datos (CSV)"));
        add(new Paragraph("Archivos tabulates listos para Excel. Se generan "
                + "con los datos reales de la base de datos en el momento de "
                + "la descarga."));

        add(new H3("Indicadores y distribuciones"));
        HorizontalLayout primeraFila = new HorizontalLayout();
        for (String nombre : List.of("indicadores_globales",
                "distribucion_areas_interes", "distribucion_financiamiento",
                "distribucion_situacion_laboral", "distribucion_genero",
                "distribucion_institucion_posgrado")) {
            primeraFila.add(botonDescarga(nombre));
        }
        add(primeraFila);

        add(new H3("Distribuciones del sector productive (empleadores)"));
        HorizontalLayout filaEmpleadores = new HorizontalLayout();
        for (String nombre : List.of("distribucion_sector_empleadores",
                "distribucion_tipo_organizacion", "distribucion_tamano_organizacion",
                "distribucion_posibilidad_contratacion")) {
            filaEmpleadores.add(botonDescarga(nombre));
        }
        add(filaEmpleadores);

        add(new H3("Matrices de frecuencia cruzada (con χ² y p-valor)"));
        HorizontalLayout segundaFila = new HorizontalLayout();
        for (String nombre : List.of(
                "matriz_contingencia_area_x_financiamiento",
                "matriz_contingencia_financiamiento_x_nivel",
                "matriz_contingencia_nivel_x_institucion")) {
            segundaFila.add(botonDescarga(nombre));
        }
        add(segundaFila);

        add(new H3("Brechas, segmentos y competencias"));
        HorizontalLayout terceraFila = new HorizontalLayout();
        for (String nombre : List.of("likert_empleadores", "segmentos_antiguedad",
                "histograma_antiguedad", "competencias_mas_mencionadas")) {
            terceraFila.add(botonDescarga(nombre));
        }
        add(terceraFila);
    }

    // botonDescarga(): enlace que baja el CSV (el atributo "download" hace
    // que el navegador lo guarde en vez de abrirlo).
    private Anchor botonDescarga(String nombre) {
        Anchor enlace = new Anchor("/exportar/" + nombre + ".csv", nombre + ".csv");
        enlace.getElement().setAttribute("download", true);
        enlace.getStyle().set("padding", "8px 14px")
                .set("background", "#0b3d62")
                .set("color", "white")
                .set("border-radius", "8px")
                .set("text-decoration", "none")
                .set("font-size", "13px")
                .set("margin", "4px")
                .set("display", "inline-block");
        return enlace;
    }
}
