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
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;
import java.util.Map;

// VistaEmpleadores: tabla de empresas + las 10 barras de acuerdo
// institucional + competencias más mencionadas en texto libre.
// acuerdo% = % que marcó 3 (parcialmente) o 4 (totalmente).
@Route(value = "empleadores", layout = MainLayout.class)
@PageTitle("Empleadores | Mercado UMSS")
public class VistaEmpleadores extends VerticalLayout {

    // Los 10 ítems Likert con nombre corto para la pantalla.
    private static final List<String[]> ITEMS_LIKERT = List.of(
            new String[]{"likert_confianza_formadora", "Confianza formadora"},
            new String[]{"likert_consistencia_titulo", "Consistencia título"},
            new String[]{"likert_consulta_regular", "Consulta regular"},
            new String[]{"likert_conoce_perfil", "Conoce perfil"},
            new String[]{"likert_coherencia_perfil", "Coherencia perfil"},
            new String[]{"likert_formacion_desempeno", "Formación→desempeño"},
            new String[]{"likert_considera_opinion", "Considera opinión"},
            new String[]{"likert_competencias_laborales", "Competencias"},
            new String[]{"likert_valores_actitudes", "Valores"},
            new String[]{"likert_participacion_procesos", "Participación"});

    public VistaEmpleadores(RepositorioEmpleadores repositorio,
            ServicioAnalisis analisis) {
        add(new H1("Empleadores encuestados"));
        add(new Details("Cómo leer esta pantalla",
                new Paragraph(GuiasLectura.COMO_LEER_EMPLEADORES)));

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

        add(new H3("Acuerdo institucional por ítem"));
        List<Map<String, String>> mapas = analisis.mapasEmpleadores();
        for (String[] item : ITEMS_LIKERT) {
            List<Double> puntajes = analisis.likertDe(mapas, item[0]);
            long deAcuerdo = puntajes.stream().filter(p -> p >= 3).count();
            double porcentaje =
                    puntajes.isEmpty() ? 0 : 100.0 * deAcuerdo / puntajes.size();
            add(barraDeAcuerdo(item[1], porcentaje));
        }

        add(new H3("Competencias más mencionadas (texto libre)"));
        for (var competencia : analisis.competenciasMencionadas(10)) {
            add(new Paragraph(competencia.getKey() + ": "
                    + competencia.getValue() + " menciones"));
        }

        // Cobertura de campo (§5 guía): 3 empresas, pública + privada.
        long distintas = mapas.stream()
                .map(f -> f.getOrDefault("nombre_empresa", "").trim())
                .filter(n -> !n.isEmpty()).distinct().count();
        boolean hayPublica = mapas.stream().anyMatch(f -> f
                .getOrDefault("tipo_organizacion", "").contains("PUBLICO"));
        boolean hayPrivada = mapas.stream().anyMatch(f -> f
                .getOrDefault("tipo_organizacion", "").contains("PRIVADO"));
        add(new Paragraph("Cobertura: " + distintas + " empresas "
                + (distintas >= 3 ? "OK" : "INSUFICIENTE (mínimo 3)")
                + " · Pública: " + (hayPublica ? "sí" : "FALTA")
                + " · Privada: " + (hayPrivada ? "sí" : "FALTA")));
    }

    // barraDeAcuerdo(): renglón con nombre + barra 0-100% + cifra.
    private HorizontalLayout barraDeAcuerdo(String nombre, double porcentaje) {
        Div etiqueta = new Div();
        etiqueta.setText(nombre);
        etiqueta.setWidth("220px");
        ProgressBar barra = new ProgressBar();
        barra.setValue(porcentaje / 100.0);
        barra.setWidth("300px");
        Div cifra = new Div();
        cifra.setText(String.format("%.1f%%", porcentaje));
        HorizontalLayout fila = new HorizontalLayout(etiqueta, barra, cifra);
        fila.setAlignItems(Alignment.CENTER);
        return fila;
    }
}
