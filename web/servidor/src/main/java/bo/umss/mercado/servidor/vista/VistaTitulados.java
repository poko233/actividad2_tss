package bo.umss.mercado.servidor.vista;

import bo.umss.mercado.nucleo.guia.GuiasLectura;
import bo.umss.mercado.nucleo.likert.EscalaLikert;
import bo.umss.mercado.nucleo.stats.Frecuencias;
import bo.umss.mercado.nucleo.stats.IntervalosConfianza;
import bo.umss.mercado.nucleo.stats.RangoConfianza;
import bo.umss.mercado.nucleo.stats.ResumenNumerico;
import bo.umss.mercado.servidor.datos.RepositorioTitulados;
import bo.umss.mercado.servidor.datos.Titulado;
import bo.umss.mercado.servidor.servicio.ServicioAnalisis;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;
import java.util.Map;

// VistaTitulados: tabla de encuestados + resumen de antigüedad y
// satisfacción + ranking de áreas. El filtro recorta la tabla;
// los resúmenes siempre usan TODA la muestra.
@Route(value = "titulados", layout = MainLayout.class)
@PageTitle("Titulados | Mercado UMSS")
public class VistaTitulados extends VerticalLayout {

    private final RepositorioTitulados repositorio;
    private final ServicioAnalisis analisis;
    private final Grid<Titulado> tabla = new Grid<>(Titulado.class, false);

    public VistaTitulados(RepositorioTitulados repositorio,
            ServicioAnalisis analisis) {
        this.repositorio = repositorio;
        this.analisis = analisis;

        add(new H1("Titulados encuestados"));
        add(new Details("Cómo leer esta pantalla",
                new Paragraph(EscalaLikert.GUIA_LIKERT)));

        // Filtro por situación laboral (todas / trabaja / no trabaja / emprende).
        ComboBox<String> filtro = new ComboBox<>("Situación laboral");
        filtro.setItems("Todas", "TRABAJA_ORG", "NO_TRABAJA", "EMPRENDIMIENTO");
        filtro.setValue("Todas");
        filtro.addValueChangeListener(cambio -> refrescarTabla(cambio.getValue()));
        add(filtro);

        // Columnas principales (la tabla tiene 35; aquí las 8 clave).
        tabla.addColumn(Titulado::getIdTitulado).setHeader("ID");
        tabla.addColumn(Titulado::getEdad).setHeader("Edad");
        tabla.addColumn(Titulado::getGenero).setHeader("Género");
        tabla.addColumn(Titulado::getSituacionLaboralActual)
                .setHeader("Situación");
        tabla.addColumn(Titulado::getAniosVidaProfesional)
                .setHeader("Años prof.");
        tabla.addColumn(Titulado::getAreaInteresPosgrado).setHeader("Área");
        tabla.addColumn(Titulado::getFinanciamientoFormacion)
                .setHeader("Financiamiento");
        tabla.addColumn(Titulado::getSatisfaccionFormacion)
                .setHeader("Satisfacción");
        tabla.setWidthFull();
        refrescarTabla("Todas");
        add(tabla);

        add(new H3("Antigüedad (años de vida profesional)"));
        ResumenNumerico antiguedad =
                analisis.resumenNumerico("anios_vida_profesional");
        RangoConfianza rango =
                IntervalosConfianza.paraMedia(analisis.numerosDe(
                        analisis.mapasTitulados(), "anios_vida_profesional"));
        add(new Paragraph("Media " + formato(antiguedad.media())
                + " ± " + formato(antiguedad.desv()) + " años. "
                + "La media real está entre " + formato(rango.limiteInferior())
                + " y " + formato(rango.limiteSuperior()) + " años."));

        add(new H3("Satisfacción con la formación (escala 1-4)"));
        List<Double> satisfacciones =
                analisis.likertDe(analisis.mapasTitulados(),
                        "satisfaccion_formacion");
        ResumenNumerico resumenSat =
                analisis.resumenNumericoDeValores(satisfacciones);
        add(new Paragraph("Media " + formato(resumenSat.media()) + ": "
                + EscalaLikert.etiquetaDeMedia(resumenSat.media(), 4) + ". "
                + EscalaLikert.etiquetaDeConsenso(resumenSat.desv()) + "."));

        add(new H3("Áreas de interés (top)"));
        for (var area : analisis.topCategorias(
                analisis.frecuenciasDe("area_interes_posgrado"), 5)) {
            add(new Paragraph(area.getKey() + ": " + area.getValue()
                    + " titulados"));
        }
        add(new Paragraph(GuiasLectura.COMO_LEER_INTERVALOS));
    }

    // refrescarTabla(): vuelve a pedir al repositorio según el filtro.
    private void refrescarTabla(String situacion) {
        List<Titulado> todos = repositorio.findAll();
        if (situacion == null || situacion.equals("Todas")) {
            tabla.setItems(todos);
            return;
        }
        tabla.setItems(todos.stream()
                .filter(t -> situacion.equals(t.getSituacionLaboralActual()))
                .toList());
    }

    private String formato(double numero) {
        return Double.isNaN(numero) ? "sin dato"
                : String.format("%.2f", numero);
    }
}
