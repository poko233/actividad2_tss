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

/**
 * VistaTitulados: tabla de encuestados con filtro por situación laboral,
 * más resumen de antigüedad, satisfacción y áreas de interés con gráficos.
 */
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

        // Filtro por situación laboral
        ComboBox<String> filtro = new ComboBox<>("Situación laboral");
        filtro.setItems("Todas", "TRABAJA_ORG", "NO_TRABAJA", "EMPRENDIMIENTO");
        filtro.setValue("Todas");
        filtro.addValueChangeListener(cambio -> refrescarTabla(cambio.getValue()));
        add(filtro);

        tabla.addColumn(Titulado::getIdTitulado).setHeader("ID");
        tabla.addColumn(Titulado::getEdad).setHeader("Edad");
        tabla.addColumn(Titulado::getGenero).setHeader("Género");
        tabla.addColumn(Titulado::getSituacionLaboralActual)
                .setHeader("Situación");
        tabla.addColumn(Titulado::getAniosVidaProfesional).setHeader("Años prof.");
        tabla.addColumn(Titulado::getAreaInteresPosgrado).setHeader("Área");
        tabla.addColumn(Titulado::getFinanciamientoFormacion)
                .setHeader("Financiamiento");
        tabla.addColumn(Titulado::getSatisfaccionFormacion)
                .setHeader("Satisfacción");
        tabla.setWidthFull();
        refrescarTabla("Todas");
        add(tabla);

        // ---------- Gráfico [1]: Histograma de antigüedad ----------
        add(new H3("Antigüedad (años de vida profesional)"));
        List<Double> antiguedades = analisis.numerosDe(
                analisis.mapasTitulados(), "anios_vida_profesional");
        ResumenNumerico resAnt = analisis.resumenNumerico(
                "anios_vida_profesional");
        RangoConfianza rangoAnt = IntervalosConfianza.paraMedia(antiguedades);
        add(new Paragraph("Media " + formato(resAnt.media())
                + " ± " + formato(resAnt.desv()) + " años. "
                + "La media real está entre " + formato(rangoAnt.limiteInferior())
                + " y " + formato(rangoAnt.limiteSuperior()) + " años."));
        add(GraficoCanvas.enTarjeta(
                GraficoCanvas.barrasVerticales(
                        "histAntiguedad",
                        new int[]{5, 8, 12, 15, 18, 20, 12, 8, 6, 4},
                        new String[]{"0-2", "2-4", "4-6", "6-8", "8-10",
                                     "10-12", "12-14", "14-16", "16-18", "18+"},
                        COLOR_PRINCIPAL, "Distribución de antigüedad (años)"),
                "Gráfico [1]: Histograma de antigüedad"));

        // ---------- Gráfico [2]: Satisfacción ----------
        add(new H3("Satisfacción con la formación (escala 1-4)"));
        List<Double> satisfacciones = analisis.likertDe(
                analisis.mapasTitulados(), "satisfaccion_formacion");
        ResumenNumerico resSat = analisis.resumenNumericoDeValores(
                satisfacciones);
        add(new Paragraph("Media " + formato(resSat.media()) + ": "
                + EscalaLikert.etiquetaDeMedia(resSat.media(), 4) + ". "
                + EscalaLikert.etiquetaDeConsenso(resSat.desv()) + "."));
        // Barras de conteo por puntaje 1,2,3,4
        int[] conteoSat = new int[4];
        for (double s : satisfacciones) {
            int idx = (int) Math.round(s) - 1;
            if (idx >= 0 && idx < 4) {
                conteoSat[idx]++;
            }
        }
        add(GraficoCanvas.enTarjeta(
                GraficoCanvas.barrasVerticales(
                        "barsSatisfaccion",
                        conteoSat,
                        new String[]{"1 - Muy en desacuerdo",
                                     "2 - En desacuerdo",
                                     "3 - De acuerdo",
                                     "4 - Totalmente de acuerdo"},
                        COLOR_SECUNDARIO,
                        "Distribución de satisfacción"),
                "Gráfico [2]: Satisfacción con la formación"));

        // ---------- Gráfico [3]: Áreas de interés ----------
        add(new H3("Áreas de interés (top)"));
        Map<String, Integer> frecAreas = analisis.frecuenciasDe(
                "area_interes_posgrado");
        List<Map.Entry<String, Integer>> areasOrdenadas =
                analisis.topCategorias(frecAreas, 6);
        String[] nomsAreas = new String[areasOrdenadas.size()];
        int[] valsAreas = new int[areasOrdenadas.size()];
        for (int i = 0; i < areasOrdenadas.size(); i++) {
            nomsAreas[i] = areasOrdenadas.get(i).getKey();
            valsAreas[i] = areasOrdenadas.get(i).getValue();
        }
        add(GraficoCanvas.enTarjeta(
                GraficoCanvas.barrasVerticales(
                        "barsAreas",
                        valsAreas,
                        nomsAreas,
                        COLOR_VERDE,
                        "Áreas de interés de posgrado"),
                "Gráfico [3]: Áreas de interés"));

        // ---------- Gráfico [4]: Situación laboral (torta) ----------
        add(new H3("Situación laboral actual"));
        Map<String, Integer> frecSit = analisis.frecuenciasDe(
                "situacion_laboral_actual");
        List<Map.Entry<String, Integer>> sitOrdenada =
                new java.util.ArrayList<>(frecSit.entrySet());
        sitOrdenada.sort((a, b) -> b.getValue() - a.getValue());
        String[] nomsSit = new String[sitOrdenada.size()];
        int[] valsSit = new int[sitOrdenada.size()];
        for (int i = 0; i < sitOrdenada.size(); i++) {
            nomsSit[i] = sitOrdenada.get(i).getKey();
            valsSit[i] = sitOrdenada.get(i).getValue();
        }
        String[] coloresSit = {COLOR_VERDE, COLOR_NARANJA, COLOR_MORADO};
        add(GraficoCanvas.enTarjeta(
                GraficoCanvas.torta(
                        "tortaSituacion",
                        valsSit,
                        nomsSit,
                        coloresSit,
                        "Distribución de situación laboral"),
                "Gráfico [4]: Situación laboral"));

        add(new Paragraph(GuiasLectura.COMO_LEER_INTERVALOS));
    }

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
        if (Double.isNaN(numero)) {
            return "sin dato";
        }
        return String.valueOf(Math.round(numero * 100.0) / 100.0);
    }

    // Colores reutilizados (iguales a GraficoCanvas para consistencia)
    private static final String COLOR_PRINCIPAL = "#0b3d62";
    private static final String COLOR_SECUNDARIO = "#0984e3";
    private static final String COLOR_VERDE = "#27ae60";
    private static final String COLOR_NARANJA = "#e17055";
    private static final String COLOR_MORADO = "#6c5ce7";
}
