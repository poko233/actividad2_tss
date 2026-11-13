package bo.umss.mercado.servidor.vista;

import bo.umss.mercado.nucleo.limpieza.LimpiezaDatos;
import bo.umss.mercado.servidor.datos.Empleador;
import bo.umss.mercado.servidor.datos.RepositorioEmpleadores;
import bo.umss.mercado.servidor.servicio.FichaCruce;
import bo.umss.mercado.servidor.servicio.ServicioAnalisis;
import bo.umss.mercado.servidor.servicio.ServicioCruces;
import bo.umss.mercado.servidor.servicio.ServicioInterpretacion;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

@Route(value = "empleadores", layout = MainLayout.class)
@PageTitle("Empleadores | Mercado UMSS")
public class VistaEmpleadores extends VerticalLayout {

        private record Campo(String clave, String grupo, String etiqueta,
                        Function<Empleador, String> getter) {
        }

        private static final List<Campo> CAMPOS = List.of(
                        new Campo("id", "Identificación", "ID", Empleador::getIdEmpleador),
                        new Campo("correo", "Identificación", "Correo", Empleador::getCorreo),
                        new Campo("nombre", "Identificación", "Empresa", Empleador::getNombreEmpresa),
                        new Campo("tipo", "Caracterización", "Tipo", Empleador::getTipoOrganizacion),
                        new Campo("tamano", "Caracterización", "Tamaño", Empleador::getTamanoOrganizacion),
                        new Campo("tamano_agr", "Caracterización", "Tamaño agrupado", Empleador::getTamanoAgrupado),
                        new Campo("sector", "Caracterización", "Sector", Empleador::getSectorRubro),
                        new Campo("rubro_agr", "Caracterización", "Rubro agrupado", Empleador::getRubroAgrupado),
                        new Campo("contrato", "Demanda", "Contrato 5 años", Empleador::getContratoUltimos5Anios),
                        new Campo("proyeccion", "Demanda", "Proyección", Empleador::getPosibilidadFuturaContratacion),
                        new Campo("nivel", "Demanda", "Nivel académico", Empleador::getNivelFormacionDemanda),
                        new Campo("medio", "Demanda", "Medio convocatoria", Empleador::getMedioConvocatoria),
                        new Campo("cargos", "Cargos y competencias", "Cargos", Empleador::getCargosDesempenados),
                        new Campo("areas", "Cargos y competencias", "Áreas emergentes",
                                        Empleador::getAreasConocimientoEmergentes),
                        new Campo("herr", "Cargos y competencias", "Herramientas",
                                        Empleador::getHerramientasTecnologicas),
                        new Campo("hab", "Cargos y competencias", "Habilidades",
                                        Empleador::getHabilidadesCompetenciasRequeridas),
                        new Campo("demanda_ia", "Análisis de texto", "Demanda IA", Empleador::getDemandaIa),
                        new Campo("tend", "Análisis de texto", "Tendencias", Empleador::getTendenciasCategorias),
                        new Campo("hard", "Análisis de texto", "Hard skills", Empleador::getHardSkillsCategorias),
                        new Campo("soft", "Análisis de texto", "Soft skills", Empleador::getSoftSkillsCategorias),
                        new Campo("l1", "Likert", "L1 Confianza", Empleador::getLikertConfianzaFormadora),
                        new Campo("l2", "Likert", "L2 Consistencia", Empleador::getLikertConsistenciaTitulo),
                        new Campo("l3", "Likert", "L3 Consulta", Empleador::getLikertConsultaRegular),
                        new Campo("l4", "Likert", "L4 Conoce perfil", Empleador::getLikertConocePerfil),
                        new Campo("l5", "Likert", "L5 Coherencia", Empleador::getLikertCoherenciaPerfil),
                        new Campo("l6", "Likert", "L6 Desempeño", Empleador::getLikertFormacionDesempeno),
                        new Campo("l7", "Likert", "L7 Incorpora opinión", Empleador::getLikertConsideraOpinion),
                        new Campo("l8", "Likert", "L8 Competencias", Empleador::getLikertCompetenciasLaborales),
                        new Campo("l9", "Likert", "L9 Valores", Empleador::getLikertValoresActitudes),
                        new Campo("l10", "Likert", "L10 Participación", Empleador::getLikertParticipacionProcesos),
                        new Campo("l11", "Likert", "L11 Vínculos", Empleador::getLikertVinculosOrganizaciones),
                        new Campo("l12", "Likert", "L12 Participa vinculación",
                                        Empleador::getLikertParticipacionVinculacion),
                        new Campo("l13", "Likert", "L13 Difusión", Empleador::getLikertDifusionOportunidades),
                        new Campo("l14", "Likert", "L14 Vinculación profesionales",
                                        Empleador::getLikertVinculacionProfesionales),
                        new Campo("l15", "Likert", "L15 Consultado desempeño", Empleador::getLikertConsultadoDesempeno),
                        new Campo("l16", "Likert", "L16 Consultado pertinencia",
                                        Empleador::getLikertConsultadoPertinencia),
                        new Campo("l17", "Likert", "L17 Recurre UMSS", Empleador::getLikertRecurreUmss),
                        new Campo("l18", "Likert", "L18 Consultado autoevaluación",
                                        Empleador::getLikertConsultadoAutoevaluacion));

        private static final Map<String, String[]> COLORES_GRUPO = new LinkedHashMap<>();
        static {
                COLORES_GRUPO.put("Identificación", new String[] { "#f5f5f5", "#424242" });
                COLORES_GRUPO.put("Caracterización", new String[] { "#e3f2fd", "#0d47a1" });
                COLORES_GRUPO.put("Demanda", new String[] { "#e8f5e9", "#1b5e20" });
                COLORES_GRUPO.put("Cargos y competencias", new String[] { "#fff3e0", "#e65100" });
                COLORES_GRUPO.put("Análisis de texto", new String[] { "#fce4ec", "#880e4f" });
                COLORES_GRUPO.put("Likert", new String[] { "#e0f7fa", "#006064" });
        }

        private static final String COLOR_PRINCIPAL = "#0b3d62";
        private static final String COLOR_SECUNDARIO = "#0984e3";
        private static final String COLOR_VERDE = "#27ae60";
        private static final String COLOR_NARANJA = "#e17055";
        private static final String COLOR_MORADO = "#6c5ce7";
        private static final String COLOR_GRIS = "#b2bec3";

        private final RepositorioEmpleadores repositorio;
        private final ServicioAnalisis analisis;
        private final ServicioCruces cruces;
        private final ServicioInterpretacion interpretacion;

        private Grid<Empleador> tabla = new Grid<>(Empleador.class, false);
        private final Span conteoMostrado = new Span();
        private String filtroSubmuestra = "Todas";
        private final Map<String, Grid.Column<Empleador>> mapaColumnas = new LinkedHashMap<>();

        public VistaEmpleadores(RepositorioEmpleadores repositorio,
                        ServicioAnalisis analisis,
                        ServicioCruces cruces,
                        ServicioInterpretacion interpretacion) {
                this.repositorio = repositorio;
                this.analisis = analisis;
                this.cruces = cruces;
                this.interpretacion = interpretacion;

                inyectarCss();

                add(new H1("Empleadores encuestados (M = " + repositorio.count() + ")"));

                Tabs tabs = new Tabs(
                                new Tab("Univariado"),
                                new Tab("Bivariado"),
                                new Tab("Multivariado"));
                VerticalLayout contenido = new VerticalLayout();
                contenido.setPadding(false);
                contenido.add(construirTabUnivariado());
                tabs.addSelectedChangeListener(e -> {
                        contenido.removeAll();
                        switch (e.getSelectedTab().getLabel()) {
                                case "Univariado" -> contenido.add(construirTabUnivariado());
                                case "Bivariado" -> contenido.add(construirTabBivariado());
                                case "Multivariado" -> contenido.add(construirTabMultivariado());
                        }
                });
                add(tabs, contenido);
        }

        private void inyectarCss() {
                StringBuilder css = new StringBuilder();
                for (var entry : COLORES_GRUPO.entrySet()) {
                        String clase = "grupo-" + slug(entry.getKey());
                        css.append("vaadin-grid::part(").append(clase).append(") {");
                        css.append("  background-color: ").append(entry.getValue()[0]).append(";");
                        css.append("}");
                }
                getElement().executeJs(
                                "const prev = document.getElementById('css-vista-empleadores');\n"
                                                + "if (prev) prev.remove();\n"
                                                + "const s = document.createElement('style');\n"
                                                + "s.id = 'css-vista-empleadores';\n"
                                                + "s.textContent = $0;\n"
                                                + "document.head.appendChild(s);",
                                css.toString());
        }

        private static String slug(String s) {
                return s.toLowerCase()
                                .replace("ó", "o").replace("í", "i").replace("á", "a")
                                .replace("é", "e").replace("ú", "u").replace("ñ", "n")
                                .replace(" ", "_");
        }

        private VerticalLayout construirTabUnivariado() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.setSpacing(true);
                v.add(construirFiltros());
                v.add(construirTablaCompleta());
                v.add(construir411());
                v.add(construir412());
                v.add(construir413());
                v.add(construir414());
                v.add(construir415());
                v.add(construirGraficos());
                return v;
        }

        private HorizontalLayout construirFiltros() {
                ComboBox<String> sub = new ComboBox<>("Submuestra");
                sub.setItems("Todas",
                                "Privadas", "Públicas",
                                "Micro", "Pequeña", "Mediana", "Grande",
                                "Desarrollo de Software", "Otros sectores",
                                "Demanda IA", "Con alta proyección",
                                "Con cargos demandados");
                sub.setValue("Todas");
                sub.setWidth("280px");
                sub.addValueChangeListener(e -> {
                        filtroSubmuestra = e.getValue();
                        aplicarFiltros();
                        autoScrollYDestacarColumna();
                });
                conteoMostrado.getStyle().set("color", "#666").set("font-size", "13px")
                                .set("align-self", "flex-end").set("padding-bottom", "8px");
                HorizontalLayout fila = new HorizontalLayout(sub, conteoMostrado);
                fila.setWidthFull();
                fila.getStyle().set("gap", "16px").set("align-items", "flex-end");
                return fila;
        }

        private void aplicarFiltros() {
                List<Empleador> base = repositorio.findAll();
                List<Empleador> filtrados = base.stream()
                                .filter(t -> switch (filtroSubmuestra) {
                                        case "Privadas" -> "PRIVADO".equals(t.getTipoOrganizacion());
                                        case "Públicas" -> "PUBLICO".equals(t.getTipoOrganizacion());
                                        case "Micro" -> "MICRO".equals(t.getTamanoOrganizacion());
                                        case "Pequeña" -> "PEQUENA".equals(t.getTamanoOrganizacion());
                                        case "Mediana" -> "MEDIANA".equals(t.getTamanoOrganizacion());
                                        case "Grande" -> "GRANDE".equals(t.getTamanoOrganizacion());
                                        case "Desarrollo de Software" ->
                                                "DESARROLLO_SOFTWARE".equals(t.getSectorRubro());
                                        case "Otros sectores" -> !"DESARROLLO_SOFTWARE".equals(t.getSectorRubro());
                                        case "Demanda IA" -> "SI".equals(t.getDemandaIa());
                                        case "Con alta proyección" ->
                                                "ALTA_PROBABILIDAD".equals(t.getPosibilidadFuturaContratacion());
                                        case "Con cargos demandados" ->
                                                t.getCargosDesempenados() != null
                                                                && !t.getCargosDesempenados().isEmpty();
                                        default -> true;
                                })
                                .toList();
                tabla.setItems(filtrados);
                conteoMostrado.setText(filtrados.size() + " de " + base.size() + " organizaciones");
        }

        private String columnaRelevanteParaFiltro() {
                return switch (filtroSubmuestra) {
                        case "Privadas", "Públicas" -> "tipo";
                        case "Micro", "Pequeña", "Mediana", "Grande" -> "tamano";
                        case "Desarrollo de Software", "Otros sectores" -> "sector";
                        case "Demanda IA" -> "demanda_ia";
                        case "Con alta proyección" -> "proyeccion";
                        case "Con cargos demandados" -> "cargos";
                        default -> null;
                };
        }

        private void autoScrollYDestacarColumna() {
                String clave = columnaRelevanteParaFiltro();
                if (clave == null)
                        return;
                Grid.Column<Empleador> col = mapaColumnas.get(clave);
                if (col == null)
                        return;
                int idx = tabla.getColumns().indexOf(col);
                if (idx < 0)
                        return;
                tabla.getElement().executeJs(
                                "const grid = this, colIdx = $0;\n"
                                                + "let intentos = 0;\n"
                                                + "function paso(){\n"
                                                + "  const sh = grid.shadowRoot;\n"
                                                + "  const th = sh && sh.querySelector('thead');\n"
                                                + "  if (!th) { if(++intentos<40) setTimeout(paso,100); return; }\n"
                                                + "  const rows = th.querySelectorAll('tr');\n"
                                                + "  if (rows.length < 2) { if(++intentos<40) setTimeout(paso,100); return; }\n"
                                                + "  const ths = rows[rows.length-1].querySelectorAll('th');\n"
                                                + "  if (ths[colIdx]) ths[colIdx].scrollIntoView({behavior:'smooth',block:'nearest',inline:'center'});\n"
                                                + "  setTimeout(() => {\n"
                                                + "    const tbody = sh.querySelector('tbody');\n"
                                                + "    if (!tbody) return;\n"
                                                + "    tbody.querySelectorAll('tr').forEach(r => {\n"
                                                + "      const c = r.children[colIdx];\n"
                                                + "      if (c && typeof c.animate === 'function') c.animate([\n"
                                                + "        {backgroundColor:'#ffe066',boxShadow:'inset 0 0 0 3px #ffc107',offset:0},\n"
                                                + "        {backgroundColor:'#ffe066',boxShadow:'inset 0 0 0 3px #ffc107',offset:0.6},\n"
                                                + "        {offset:1}], {duration:2500,easing:'ease-out',fill:'none'});\n"
                                                + "    });\n"
                                                + "  }, 500);\n"
                                                + "}\n" + "paso();",
                                idx);
        }

        private VerticalLayout construirTablaCompleta() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.add(new H3("Tabla completa de organizaciones"));

                tabla = new Grid<>(Empleador.class, false);
                mapaColumnas.clear();

                Map<String, List<Campo>> porGrupo = new LinkedHashMap<>();
                for (Campo c : CAMPOS) {
                        porGrupo.computeIfAbsent(c.grupo(), k -> new ArrayList<>()).add(c);
                }

                Map<String, List<Grid.Column<Empleador>>> colsPorGrupo = new LinkedHashMap<>();
                for (var entry : porGrupo.entrySet()) {
                        String grupo = entry.getKey();
                        String parteGrupo = "grupo-" + slug(grupo);
                        List<Grid.Column<Empleador>> cols = new ArrayList<>();
                        for (Campo c : entry.getValue()) {
                                Grid.Column<Empleador> col = tabla.addColumn(t -> {
                                        String vc = c.getter().apply(t);
                                        return vc == null ? "" : vc;
                                }).setHeader(c.etiqueta()).setAutoWidth(true).setResizable(true);
                                col.setPartNameGenerator(t -> parteGrupo);
                                mapaColumnas.put(c.clave(), col);
                                cols.add(col);
                        }
                        colsPorGrupo.put(grupo, cols);
                }

                HeaderRow top = tabla.prependHeaderRow();
                for (var entry : colsPorGrupo.entrySet()) {
                        String[] colores = COLORES_GRUPO.get(entry.getKey());
                        pintarGrupo(top.join(entry.getValue().toArray(new Grid.Column[0])),
                                        entry.getKey(), colores[0], colores[1]);
                }

                tabla.setWidthFull();
                tabla.setHeight("520px");
                tabla.getElement().getStyle().set("overflow-x", "auto");
                aplicarFiltros();
                tabla.addItemClickListener(ev -> abrirModalDetalle(ev.getItem()));

                VerticalLayout bloque = new VerticalLayout(tabla);
                bloque.setPadding(false);
                return bloque;
        }

        private void pintarGrupo(HeaderRow.HeaderCell cell, String texto,
                        String bgColor, String txtColor) {
                Span span = new Span(texto);
                span.getStyle().set("background-color", bgColor).set("color", txtColor)
                                .set("font-weight", "bold").set("text-align", "center")
                                .set("padding", "6px").set("width", "100%").set("display", "block");
                cell.setComponent(span);
        }

        private void abrirModalDetalle(Empleador t) {
                Dialog modal = new Dialog();
                modal.setHeaderTitle("Detalle de la organización: " + t.getNombreEmpresa());
                modal.setWidth("900px");
                modal.setHeight("90vh");

                VerticalLayout cont = new VerticalLayout();
                cont.setPadding(false);
                cont.setSpacing(true);

                Span resumen = new Span(t.getTipoOrganizacion() + " · " + t.getTamanoOrganizacion()
                                + " · " + t.getSectorRubro());
                resumen.getStyle().set("color", "#666").set("font-size", "13px");
                cont.add(resumen);

                Map<String, List<Campo>> porGrupo = new LinkedHashMap<>();
                for (Campo c : CAMPOS) {
                        porGrupo.computeIfAbsent(c.grupo(), k -> new ArrayList<>()).add(c);
                }

                Accordion acc = new Accordion();
                acc.setWidthFull();
                boolean first = true;
                for (var entry : porGrupo.entrySet()) {
                        String[] colores = COLORES_GRUPO.get(entry.getKey());
                        Grid<Campo> g = new Grid<>();
                        g.addColumn(Campo::etiqueta).setHeader("Variable").setWidth("240px").setFlexGrow(0);
                        g.addColumn(new ComponentRenderer<>(c -> {
                                String vv = c.getter().apply(t);
                                Span s = new Span((vv == null || vv.isEmpty()) ? "—" : vv);
                                s.getStyle().set("white-space", "normal")
                                                .set("overflow-wrap", "anywhere")
                                                .set("word-break", "break-word").set("display", "block");
                                return s;
                        })).setHeader("Valor").setFlexGrow(1);
                        g.setItems(entry.getValue());
                        g.setAllRowsVisible(true);
                        g.setHeight(null);

                        Span badge = new Span(entry.getKey() + "  (" + entry.getValue().size() + ")");
                        badge.getStyle().set("background-color", colores[0]).set("color", colores[1])
                                        .set("padding", "6px 12px").set("border-radius", "6px")
                                        .set("font-weight", "bold").set("font-size", "13px");
                        AccordionPanel panel = new AccordionPanel(badge, g);
                        acc.add(panel);
                        if (first) {
                                panel.setOpened(true);
                                first = false;
                        }
                }
                cont.add(acc);
                modal.add(cont);
                modal.open();
        }

        private VerticalLayout construir411() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.add(new H3("Caracterización de las Organizaciones"));
                HorizontalLayout fila = Componentes.filaKpis(
                                Componentes.tarjetaKpi("Total", String.valueOf(repositorio.count()), "organizaciones"),
                                Componentes.tarjetaKpi("Privadas", pct("tipo_organizacion", "PRIVADO"), "del total"),
                                Componentes.tarjetaKpi("Públicas", pct("tipo_organizacion", "PUBLICO"), "del total"),
                                Componentes.tarjetaKpi("Software", pct("sector_rubro", "DESARROLLO_SOFTWARE"),
                                                "del total"));
                v.add(fila);
                v.add(new H4("Tipo de organización"));
                v.add(Componentes.tablaFrecuencias(analisis.frecuenciasTipoOrg(), "Tipo"));
                v.add(new H4("Tamaño"));
                v.add(Componentes.tablaFrecuencias(analisis.frecuenciasTamano(), "Tamaño"));
                v.add(new H4("Sector / Rubro"));
                v.add(Componentes.tablaFrecuencias(analisis.frecuenciasSector(), "Sector"));
                return v;
        }

        private String pct(String col, String valor) {
                var f = analisis.frecuenciasEmpleadores(col);
                int total = f.values().stream().mapToInt(Integer::intValue).sum();
                int n = f.getOrDefault(valor, 0);
                if (total == 0)
                        return "0";
                return String.format(Locale.US, "%.1f%%", 100.0 * n / total);
        }

        private VerticalLayout construir412() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.add(new H3("Demanda y Proyección de Contratación"));
                v.add(new H4("¿Contrató los últimos 5 años?"));
                v.add(Componentes.tablaFrecuencias(analisis.frecuenciasContrato5a(), "Contrato"));
                v.add(new H4("Proyección de contratación"));
                v.add(Componentes.tablaFrecuencias(analisis.frecuenciasProyeccion(), "Proyección"));
                v.add(new H4("Medio de convocatoria (multi-selección)"));
                v.add(Componentes.tablaFrecuencias(analisis.distribucionMedio(), "Medio"));
                return v;
        }

        private VerticalLayout construir413() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.add(new H3("Nivel Académico y Cargos Requeridos"));
                v.add(new H4("Nivel académico demandado"));
                v.add(Componentes.tablaFrecuencias(analisis.frecuenciasNivelAcademico(), "Nivel"));
                v.add(new H4("Cargos desempeñados (multi-selección)"));
                v.add(Componentes.tablaFrecuencias(analisis.distribucionCargos(), "Cargo"));
                Paragraph nota = new Paragraph(
                                "Los porcentajes de cargos pueden sumar más de 100% por ser selección múltiple.");
                nota.getStyle().set("font-style", "italic").set("font-size", "12px").set("color", "#666");
                v.add(nota);
                return v;
        }

        private VerticalLayout construir414() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.add(new H3("Tendencias Tecnológicas y Competencias"));
                v.add(new H4("Demanda de IA"));
                v.add(Componentes.tablaFrecuencias(analisis.frecuenciasDemandaIA(), "Demanda IA"));
                v.add(new H4("Tendencias emergentes"));
                v.add(Componentes.tablaFrecuencias(analisis.distribucionTendencias(), "Tendencia"));
                v.add(new H4("Hard Skills"));
                v.add(Componentes.tablaFrecuencias(analisis.distribucionHardSkills(), "Hard Skill"));
                v.add(new H4("Soft Skills"));
                v.add(Componentes.tablaFrecuencias(analisis.distribucionSoftSkills(), "Soft Skill"));
                return v;
        }

        private VerticalLayout construir415() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.add(new H3("Evaluación Institucional (Matriz Likert)"));
                Paragraph nota = new Paragraph("18 ítems en escala 1-3. 'No sabe' y vacío se excluyen.");
                nota.getStyle().set("font-style", "italic").set("font-size", "12px").set("color", "#666");
                v.add(nota);

                ServicioAnalisis.ResumenDimension dimA = analisis.fichaDimensionA();
                ServicioAnalisis.ResumenDimension dimB = analisis.fichaDimensionB();
                ServicioAnalisis.ResumenItem item4 = analisis.fichaItem4();

                double mediaA = analisis.mediasPorDimension("A").stream().mapToDouble(d -> d).average()
                                .orElse(Double.NaN);
                double mediaB = analisis.mediasPorDimension("B").stream().mapToDouble(d -> d).average()
                                .orElse(Double.NaN);
                double media4 = item4.media();

                HorizontalLayout kpis = Componentes.filaKpis(
                                Componentes.tarjetaKpi("Dimensión A (Calidad e Imagen)",
                                                String.format(Locale.US, "%.2f/3", mediaA),
                                                "α=" + String.format(Locale.US, "%.2f", dimA.alpha()) + " · n="
                                                                + dimA.n()),
                                Componentes.tarjetaKpi("Dimensión B (Vinculación y Retroalimentación)",
                                                String.format(Locale.US, "%.2f/3", mediaB),
                                                "α=" + String.format(Locale.US, "%.2f", dimB.alpha()) + " · n="
                                                                + dimB.n()),
                                Componentes.tarjetaKpi("Ítem 4 (Conoce perfil)",
                                                String.format(Locale.US, "%.2f/3", media4),
                                                "Reporte individual"));
                v.add(kpis);

                v.add(GraficoCanvas.enTarjeta(
                                GraficoCanvas.radarConEscala("radarEmp",
                                                new double[] { mediaA, mediaB, media4 },
                                                new String[] { "Dim A", "Dim B", "Ítem 4" },
                                                3, "Radar institucional (escala 1-3)"),
                                "Gráfico de Radar: 2 dimensiones + ítem 4"));

                Grid<String[]> g = new Grid<>();
                g.addColumn(r -> r[0]).setHeader("Ítem").setAutoWidth(true);
                g.addColumn(r -> r[1]).setHeader("n").setAutoWidth(true);
                g.addColumn(r -> r[2]).setHeader("Media").setAutoWidth(true);
                g.addColumn(r -> r[3]).setHeader("Desv.").setAutoWidth(true);
                g.addColumn(r -> r[4]).setHeader("% Acuerdo").setAutoWidth(true);
                List<String[]> filas = new ArrayList<>();
                for (var item : analisis.matriz18Items()) {
                        filas.add(new String[] {
                                        item.etiqueta(),
                                        String.valueOf(item.n()),
                                        String.format(Locale.US, "%.2f", item.media()),
                                        String.format(Locale.US, "%.2f", item.desviacion()),
                                        String.format(Locale.US, "%.1f%%", item.pctAcuerdo())
                        });
                }
                g.setItems(filas);
                g.setAllRowsVisible(true);
                g.setWidthFull();
                v.add(new H4("Los 18 ítems individuales"));
                v.add(g);

                v.add(new H4("Confiabilidad (Alfa de Cronbach)"));
                v.add(new Paragraph("Dimensión A (Calidad e Imagen): ítems 1, 2, 5, 6, 8, 9 → α = "
                                + String.format(Locale.US, "%.2f", dimA.alpha())
                                + " (" + dimA.interpretacion() + "), n = " + dimA.n()));
                v.add(new Paragraph("Dimensión B (Vinculación y Retroalimentación): ítems 3, 7, 10-18 → α = "
                                + String.format(Locale.US, "%.2f", dimB.alpha())
                                + " (" + dimB.interpretacion() + "), n = " + dimB.n()));
                v.add(new Paragraph("Ítem 4 (Conoce el perfil de egreso): reporte individual, media = "
                                + String.format(Locale.US, "%.2f", media4) + "/3, n = " + item4.n()));
                return v;
        }

        private VerticalLayout construirGraficos() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.add(new H3("Visualizaciones"));

                v.add(graficoTorta("torta_tipo", analisis.frecuenciasTipoOrg(),
                                new String[] { COLOR_PRINCIPAL, COLOR_NARANJA },
                                "Distribución por tipo", "Tipo de organización"));

                v.add(graficoBarras("bars_tamano", analisis.frecuenciasTamano(),
                                COLOR_SECUNDARIO, "Distribución por tamaño", "Tamaño"));

                v.add(graficoBarras("bars_sector", analisis.frecuenciasSector(),
                                COLOR_VERDE, "Distribución por sector", "Sector"));

                v.add(graficoBarras("bars_nivel", analisis.frecuenciasNivelAcademico(),
                                COLOR_MORADO, "Nivel académico demandado", "Nivel académico"));

                v.add(graficoBarras("bars_tendencias", analisis.distribucionTendencias(),
                                COLOR_NARANJA, "Tendencias emergentes", "Tendencias"));

                v.add(graficoBarras("bars_hard", analisis.distribucionHardSkills(),
                                COLOR_PRINCIPAL, "Hard skills más mencionadas", "Hard skills"));

                v.add(graficoBarras("bars_soft", analisis.distribucionSoftSkills(),
                                COLOR_VERDE, "Soft skills más mencionadas", "Soft skills"));

                v.add(graficoBarras("bars_cargos", analisis.distribucionCargos(),
                                COLOR_SECUNDARIO, "Cargos demandados", "Cargos"));

                return v;
        }

        private Component graficoBarras(String id, Map<String, Integer> frec,
                        String color, String titulo, String tarjeta) {
                if (frec.isEmpty())
                        return new Paragraph("Sin datos para: " + titulo);
                List<Map.Entry<String, Integer>> ord = new ArrayList<>(frec.entrySet());
                ord.sort((a, b) -> b.getValue() - a.getValue());
                String[] etiquetas = new String[ord.size()];
                int[] valores = new int[ord.size()];
                for (int i = 0; i < ord.size(); i++) {
                        etiquetas[i] = ord.get(i).getKey();
                        valores[i] = ord.get(i).getValue();
                }
                return GraficoCanvas.enTarjeta(
                                GraficoCanvas.barrasVerticales(id, valores, etiquetas, color, titulo), tarjeta);
        }

        private Component graficoTorta(String id, Map<String, Integer> frec,
                        String[] colores, String titulo, String tarjeta) {
                if (frec.isEmpty())
                        return new Paragraph("Sin datos para: " + titulo);
                List<Map.Entry<String, Integer>> ord = new ArrayList<>(frec.entrySet());
                ord.sort((a, b) -> b.getValue() - a.getValue());
                String[] etiquetas = new String[ord.size()];
                int[] valores = new int[ord.size()];
                for (int i = 0; i < ord.size(); i++) {
                        etiquetas[i] = ord.get(i).getKey();
                        valores[i] = ord.get(i).getValue();
                }
                return GraficoCanvas.enTarjeta(
                                GraficoCanvas.torta(id, valores, etiquetas, colores, titulo), tarjeta);
        }

        private VerticalLayout construirTabBivariado() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.setSpacing(true);
                v.add(new H2("Análisis Bivariado — Pruebas de Independencia (χ²)"));
                v.add(new Paragraph("Chi-cuadrado de Pearson, V de Cramér, intervalos de confianza Wilson "
                                + "al 95% y corrección de Holm-Bonferroni para comparaciones múltiples."));

                List<FichaCruce> lista = cruces.crucesEmpleadores();
                int contador = 0;
                for (FichaCruce c : lista) {
                        contador++;
                        v.add(renderCruceConTexto(c, contador));
                }

                v.add(construirMatrizResumenBivariada());
                return v;
        }

        private VerticalLayout renderCruceConTexto(FichaCruce c, int numero) {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.setSpacing(true);
                v.add(new H4(numero + ". " + c.etiqueta()));
                v.add(PanelCruce.render(c, cruces));
                return v;
        }

        private VerticalLayout construirMatrizResumenBivariada() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.add(new H3("Matriz Resumen Bivariada con ajuste de Holm-Bonferroni"));
                List<ServicioCruces.FilaMatrizBivariada> filas = cruces.matrizResumenEmpleadores();

                Grid<String[]> g = new Grid<>();
                g.addColumn(r -> r[0]).setHeader("N°").setAutoWidth(true);
                g.addColumn(r -> r[1]).setHeader("Cruce").setAutoWidth(true);
                g.addColumn(r -> r[2]).setHeader("χ²").setAutoWidth(true);
                g.addColumn(r -> r[3]).setHeader("gl").setAutoWidth(true);
                g.addColumn(r -> r[4]).setHeader("p orig.").setAutoWidth(true);
                g.addColumn(r -> r[5]).setHeader("p (Holm)").setAutoWidth(true);
                g.addColumn(r -> r[6]).setHeader("V Cramér").setAutoWidth(true);
                g.addColumn(r -> r[7]).setHeader("Interpretación").setAutoWidth(true);

                List<String[]> data = new ArrayList<>();
                for (int i = 0; i < filas.size(); i++) {
                        var f = filas.get(i);
                        data.add(new String[] {
                                        String.valueOf(i + 1),
                                        f.etiqueta(),
                                        Componentes.redondear(f.chi(), 2),
                                        String.valueOf(f.gl()),
                                        Componentes.redondear(f.pOriginal(), 4),
                                        Componentes.redondear(f.pHolm(), 4),
                                        Componentes.redondear(f.vCramer(), 3),
                                        f.significativa()
                                                        ? "Significativa (" + f.intensidad() + ")"
                                                        : "No significativo"
                        });
                }
                g.setItems(data);
                g.setAllRowsVisible(true);
                g.setWidthFull();
                g.getElement().getStyle().set("overflow-x", "auto");
                v.add(g);

                long sig = filas.stream()
                                .filter(ServicioCruces.FilaMatrizBivariada::significativa).count();
                v.add(new Paragraph("De los " + filas.size() + " cruces, " + sig
                                + " mantienen significancia tras corrección de Holm-Bonferroni."));
                return v;
        }

        private VerticalLayout construirTabMultivariado() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.setSpacing(true);
                v.add(new H2("Análisis Exploratorio Multivariable"));
                v.add(new Paragraph("Ecuación (10) de Media Condicional. Tres modelamientos independientes."));

                v.add(construirMV1());
                v.add(construirMV2());
                v.add(construirMV3());
                return v;
        }

        private VerticalLayout construirMV1() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.add(new H3("Tamaño × Tipo × Media Likert Global"));
                v.add(new Paragraph("Modelamiento: Tamaño de la organización (X) × Tipo (Y) → "
                                + "media global de los 18 ítems Likert (Z)."));

                ServicioAnalisis.TablaMultivariable t = analisis.tablaMultivariableEmpleadores(
                                "tamano_agrupado", "tipo_organizacion");
                v.add(renderTablaMultivariable(t,
                                "Tamaño (X)", "Tipo (Y)", "Media Likert (Z)", "/3"));

                v.add(new H4("Hallazgos"));
                v.add(construirHallazgos(interpretacion.interpretar(t)));

                v.add(GraficoCanvas.enTarjeta(
                                GraficoCanvas.dispersionCategorizada("dispE1",
                                                t.valoresX(), t.valoresY(), puntos(t),
                                                new String[] { COLOR_PRINCIPAL, COLOR_NARANJA },
                                                "Tamaño", "Media Likert (1-3)",
                                                "Dispersión: Tamaño × Tipo × Media Likert"),
                                "Dispersión categorizada"));
                v.add(graficoApiladas("stackE1", t, "Media Likert (Z)",
                                new String[] { COLOR_PRINCIPAL, COLOR_NARANJA }));
                return v;
        }

        private VerticalLayout construirMV2() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.add(new H3("Rubro × Nivel Académico × Proyección"));
                v.add(new Paragraph("Modelamiento: Rubro agrupado (X) × Nivel académico agrupado (Y) → "
                                + "Proyección de contratación codificada 1-3 (Z)."));

                ServicioAnalisis.TablaMultivariable t2 = analisis.tablaMultivariable(
                                analisis.mapasEmpleadores(), "rubro_agrupado", "nivel_academico_grupo",
                                "proyeccion_num");

                v.add(renderTablaMultivariable(t2,
                                "Rubro (X)", "Nivel académico (Y)", "Proyección (Z)", " (1-3)"));

                v.add(new H4("Hallazgos"));
                v.add(construirHallazgos(interpretacion.interpretar(t2)));

                v.add(GraficoCanvas.enTarjeta(
                                GraficoCanvas.dispersionCategorizada("dispE2",
                                                t2.valoresX(), t2.valoresY(), puntos(t2),
                                                new String[] { COLOR_VERDE, COLOR_MORADO },
                                                "Rubro", "Proyección (1-3)",
                                                "Dispersión: Rubro × Nivel × Proyección"),
                                "Dispersión categorizada"));
                v.add(graficoApiladas("stackE2", t2, "Proyección (Z)",
                                new String[] { COLOR_VERDE, COLOR_MORADO }));
                return v;
        }

        private VerticalLayout construirMV3() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.add(new H3("Tamaño × Tipo × Cantidad de Cargos"));
                v.add(new Paragraph("Modelamiento: Tamaño (X) × Tipo (Y) → cantidad de cargos "
                                + "demandados por la organización (Z)."));

                ServicioAnalisis.TablaMultivariable t = analisis.tablaMultivariable(
                                analisis.mapasEmpleadores(), "tamano_agrupado", "tipo_organizacion", "cantidad_cargos");
                v.add(renderTablaMultivariable(t,
                                "Tamaño (X)", "Tipo (Y)", "Cantidad de cargos (Z)", " cargos"));

                v.add(new H4("Hallazgos"));
                v.add(construirHallazgos(interpretacion.interpretar(t)));

                v.add(GraficoCanvas.enTarjeta(
                                GraficoCanvas.dispersionCategorizada("dispE3",
                                                t.valoresX(), t.valoresY(), puntos(t),
                                                new String[] { COLOR_PRINCIPAL, COLOR_NARANJA },
                                                "Tamaño", "Cantidad de cargos",
                                                "Dispersión: Tamaño × Tipo × Cantidad de cargos"),
                                "Dispersión categorizada"));
                v.add(graficoApiladas("stackE3", t, "Cantidad de cargos (Z)",
                                new String[] { COLOR_PRINCIPAL, COLOR_NARANJA }));
                return v;
        }

        private Grid<String[]> renderTablaMultivariable(ServicioAnalisis.TablaMultivariable t,
                        String hx, String hy, String hz, String unidad) {
                Grid<String[]> g = new Grid<>();
                g.addColumn(r -> r[0]).setHeader(hx).setAutoWidth(true);
                g.addColumn(r -> r[1]).setHeader(hy).setAutoWidth(true);
                g.addColumn(r -> r[2]).setHeader("Frecuencia (n)").setAutoWidth(true);
                g.addColumn(r -> r[3]).setHeader(hz).setAutoWidth(true);
                List<String[]> filas = new ArrayList<>();
                for (var c : t.celdas()) {
                        if (c.n() == 0)
                                continue;
                        String z = Double.isNaN(c.mediaZ()) ? "—"
                                        : Componentes.redondear(c.mediaZ(), 2) + unidad;
                        filas.add(new String[] { c.valorX(), c.valorY(), String.valueOf(c.n()), z });
                }
                g.setItems(filas);
                g.setAllRowsVisible(true);
                g.setWidthFull();
                return g;
        }

        private Component construirHallazgos(String texto) {
                UnorderedList ul = new UnorderedList();
                for (String linea : texto.split("\\n")) {
                        if (linea.trim().isEmpty())
                                continue;
                        ul.add(new ListItem(linea.trim().replaceFirst("^[•\\-]\\s*", "")));
                }
                ul.getStyle().set("font-size", "13px").set("color", "#2c3e50")
                                .set("background", "#f7f9fb").set("padding", "10px 14px 10px 28px")
                                .set("border-left", "4px solid #0984e3").set("border-radius", "4px")
                                .set("margin", "6px 0");
                return ul;
        }

        private List<GraficoCanvas.PuntoDispersion> puntos(ServicioAnalisis.TablaMultivariable t) {
                List<GraficoCanvas.PuntoDispersion> p = new ArrayList<>();
                for (var c : t.celdas()) {
                        if (c.n() == 0)
                                continue;
                        p.add(new GraficoCanvas.PuntoDispersion(c.valorX(), c.valorY(), c.mediaZ(), c.n()));
                }
                return p;
        }

        private Component graficoApiladas(String id, ServicioAnalisis.TablaMultivariable t,
                        String titulo, String[] colores) {
                String[] ex = t.valoresX().toArray(new String[0]);
                String[] ey = t.valoresY().toArray(new String[0]);
                int[][] fr = new int[ex.length][ey.length];
                for (var c : t.celdas()) {
                        int xi = t.valoresX().indexOf(c.valorX());
                        int yi = t.valoresY().indexOf(c.valorY());
                        if (xi >= 0 && yi >= 0)
                                fr[xi][yi] = c.n();
                }
                return GraficoCanvas.enTarjeta(
                                GraficoCanvas.barrasApiladas(id, ex, ey, fr, colores,
                                                "Barras apiladas: " + titulo),
                                "Frecuencia por categoría");
        }
}
