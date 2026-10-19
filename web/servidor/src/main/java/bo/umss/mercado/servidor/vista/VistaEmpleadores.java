package bo.umss.mercado.servidor.vista;

import bo.umss.mercado.nucleo.guia.GuiasLectura;
import bo.umss.mercado.nucleo.likert.EscalaLikert;
import bo.umss.mercado.servidor.datos.Empleador;
import bo.umss.mercado.servidor.datos.RepositorioEmpleadores;
import bo.umss.mercado.servidor.servicio.ServicioAnalisis;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * VistaEmpleadores: tabla de empresas + barras de acuerdo institucional
 * por ítem Likert + torta de tipo de organización + competencias más
 * mencionadas en texto libre.
 */
@Route(value = "empleadores", layout = MainLayout.class)
@PageTitle("Empleadores | Mercado UMSS")
public class VistaEmpleadores extends VerticalLayout {

    private static final String[] ITEMS_LIKERT = {
            "likert_confianza_formadora", "Confianza formadora",
            "likert_consistencia_titulo", "Consistencia título",
            "likert_consulta_regular", "Consulta regular",
            "likert_conoce_perfil", "Conoce perfil",
            "likert_coherencia_perfil", "Coherencia perfil",
            "likert_formacion_desempeno", "Formación→desempeño",
            "likert_considera_opinion", "Considera opinión",
            "likert_competencias_laborales", "Competencias",
            "likert_valores_actitudes", "Valores",
            "likert_participacion_procesos", "Participación"
    };

    public VistaEmpleadores(RepositorioEmpleadores repositorio,
            ServicioAnalisis analisis) {
        add(new H1("Empleadores encuestados"));

        Grid<Empleador> tabla = new Grid<>(Empleador.class, false);
        tabla.addColumn(Empleador::getNombreEmpresa).setHeader("Empresa");
        tabla.addColumn(Empleador::getTipoOrganizacion).setHeader("Tipo");
        tabla.addColumn(Empleador::getTamanoOrganizacion).setHeader("Tamaño");
        tabla.addColumn(Empleador::getSectorRubro).setHeader("Sector");
        tabla.addColumn(Empleador::getNivelFormacionDemanda)
                .setHeader("Nivel que demanda");
        tabla.addColumn(Empleador::getPosibilidadFuturaContratacion)
                .setHeader("Posibilidad futura");
        tabla.setItems(repositorio.findAll());
        tabla.setWidthFull();
        add(tabla);

        // ---------- Gráfico [1]: Barras de acuerdo Likert ----------
        add(new H3("Acuerdo institucional por ítem Likert"));
        List<Map<String, String>> mapas = analisis.mapasEmpleadores();
        String[] nomsLikert = new String[ITEMS_LIKERT.length / 2];
        int[] valsAcuerdo = new int[ITEMS_LIKERT.length / 2];
        for (int i = 0; i < ITEMS_LIKERT.length; i += 2) {
            String clave = ITEMS_LIKERT[i];
            String etiqueta = ITEMS_LIKERT[i + 1];
            List<Double> puntajes = analisis.likertDe(mapas, clave);
            long deAcuerdo = puntajes.stream().filter(p -> p >= 3).count();
            double porcentaje =
                    puntajes.isEmpty() ? 0 : 100.0 * deAcuerdo / puntajes.size();
            nomsLikert[i / 2] = etiqueta;
            valsAcuerdo[i / 2] = (int) Math.round(porcentaje);
        }
        add(GraficoCanvas.enTarjeta(
                GraficoCanvas.barrasVerticales(
                        "barsLikert",
                        valsAcuerdo,
                        nomsLikert,
                        COLOR_VERDE,
                        "% de empleadores que están de acuerdo (3 o 4) por ítem"),
                "Gráfico [1]: Likert empleadores"));

        // ---------- Gráfico [2]: Torta de tipo de organización ----------
        add(new H3("Tipo de organización"));
        Map<String, Integer> frecTipo = bo.umss.mercado.nucleo.stats.Frecuencias
                .contar(analisis.columnaDe(mapas, "tipo_organizacion"));
        List<Map.Entry<String, Integer>> tiposOrdenados =
                new java.util.ArrayList<>(frecTipo.entrySet());
        tiposOrdenados.sort((a, b) -> b.getValue() - a.getValue());
        String[] nomsTipo = new String[tiposOrdenados.size()];
        int[] valsTipo = new int[tiposOrdenados.size()];
        for (int i = 0; i < tiposOrdenados.size(); i++) {
            nomsTipo[i] = tiposOrdenados.get(i).getKey();
            valsTipo[i] = tiposOrdenados.get(i).getValue();
        }
        String[] coloresTipo = {COLOR_PRINCIPAL, COLOR_SECUNDARIO,
                COLOR_NARANJA, COLOR_MORADO};
        add(GraficoCanvas.enTarjeta(
                GraficoCanvas.torta(
                        "tortaTipoOrg",
                        valsTipo,
                        nomsTipo,
                        coloresTipo,
                        "Distribución por tipo de organización"),
                "Gráfico [2]: Tipo de organización"));

        add(new H3("Competencias más mencionadas (texto libre)"));
        for (Map.Entry<String, Integer> competencia :
                analisis.competenciasMencionadas(10)) {
            add(new Paragraph(competencia.getKey() + ": "
                    + competencia.getValue() + " menciones"));
        }

        // Cobertura de campo (§5 guía)
        long distintas = mapas.stream()
                .map(f -> f.getOrDefault("nombre_empresa", "").trim())
                .filter(n -> !n.isEmpty()).distinct().count();
        boolean hayPub = mapas.stream().anyMatch(f -> f
                .getOrDefault("tipo_organizacion", "").contains("PUBLICO"));
        boolean hayPriv = mapas.stream().anyMatch(f -> f
                .getOrDefault("tipo_organizacion", "").contains("PRIVADO"));
        add(new Paragraph("Cobertura: " + distintas + " empresas "
                + (distintas >= 3 ? "OK" : "INSUFICIENTE (mínimo 3)")
                + " · Pública: " + (hayPub ? "sí" : "FALTA")
                + " · Privada: " + (hayPriv ? "sí" : "FALTA")));

        add(new Paragraph(GuiasLectura.COMO_LEER_EMPLEADORES));
    }

    // Colores reutilizados
    private static final String COLOR_PRINCIPAL = "#0b3d62";
    private static final String COLOR_SECUNDARIO = "#0984e3";
    private static final String COLOR_VERDE = "#27ae60";
    private static final String COLOR_NARANJA = "#e17055";
    private static final String COLOR_MORADO = "#6c5ce7";
}
