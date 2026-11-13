package bo.umss.mercado.servidor.vista;

import bo.umss.mercado.nucleo.guia.GuiasLectura;
import bo.umss.mercado.nucleo.limpieza.LimpiezaDatos;
import bo.umss.mercado.nucleo.stats.IntervalosConfianza;
import bo.umss.mercado.nucleo.stats.ResumenNumerico;
import bo.umss.mercado.servidor.datos.RepositorioTitulados;
import bo.umss.mercado.servidor.datos.Titulado;
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
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Route(value = "titulados", layout = MainLayout.class)
@PageTitle("Titulados | Mercado UMSS")
public class VistaTitulados extends VerticalLayout {

        // ---------- Definición de campos (fuente única de verdad) ----------
        private record Campo(String clave, String grupo, String etiqueta,
                        Function<Titulado, String> getter) {
        }

        private static final List<Campo> CAMPOS = List.of(
                        new Campo("id", "Demografía", "ID", Titulado::getIdTitulado),
                        new Campo("edad", "Demografía", "Edad", Titulado::getEdad),
                        new Campo("genero", "Demografía", "Género", Titulado::getGenero),
                        new Campo("formacion", "Formación", "Formación compl.",
                                        Titulado::getRealizoFormacionComplementaria),
                        new Campo("nivel", "Formación", "Nivel", Titulado::getNivelPosgrado),
                        new Campo("institucion", "Formación", "Institución", Titulado::getInstitucionFormacion),
                        new Campo("financiamiento", "Formación", "Financiamiento",
                                        Titulado::getFinanciamientoFormacion),
                        new Campo("programa", "Formación", "Programa", Titulado::getNombrePrograma),
                        new Campo("interes", "Formación", "Interés posgrado", Titulado::getInteresPosgrado),
                        new Campo("area", "Formación", "Área interés", Titulado::getAreaInteresPosgrado),
                        new Campo("institucion_futura", "Formación", "Institución futura",
                                        Titulado::getInstitucionFuturaPosgrado),
                        new Campo("opinion", "Formación", "Opinión FCyT", Titulado::getOpinionPosgradoFcyt),
                        new Campo("anios_prof", "Trayectoria", "Años prof.", Titulado::getAniosVidaProfesional),
                        new Campo("anios_desemp", "Trayectoria", "Años desemp.", Titulado::getAniosDesempleado),
                        new Campo("situacion", "Trayectoria", "Situación", Titulado::getSituacionLaboralActual),
                        new Campo("tiempo_1er", "Trayectoria", "Tiempo 1er empleo", Titulado::getTiempoPrimerEmpleo),
                        new Campo("num_empleos", "Trayectoria", "N° empleos", Titulado::getNumEmpleosDesdeTitulacion),
                        new Campo("primer_empleo", "Trayectoria", "¿1er empleo?", Titulado::getPrimerEmpleoActual),
                        new Campo("empresa", "Trabajo", "Empresa", Titulado::getNombreEmpresa),
                        new Campo("rubro", "Trabajo", "Rubro", Titulado::getRubro),
                        new Campo("otro_rubro", "Trabajo", "Otro rubro", Titulado::getOtroRubro),
                        new Campo("sector", "Trabajo", "Sector", Titulado::getSector),
                        new Campo("departamento", "Trabajo", "Departamento", Titulado::getDepartamento),
                        new Campo("area_org", "Trabajo", "Área", Titulado::getAreaOrganizacion),
                        new Campo("otra_area", "Trabajo", "Otra área", Titulado::getOtraArea),
                        new Campo("cargo", "Trabajo", "Cargo", Titulado::getCargo),
                        new Campo("otro_cargo", "Trabajo", "Otro cargo", Titulado::getOtroCargo),
                        new Campo("pertinencia", "Trabajo", "Pertinencia", Titulado::getPertinenciaTrabajo),
                        new Campo("antiguedad", "Trabajo", "Antigüedad puesto", Titulado::getAntiguedadPuesto),
                        new Campo("medio_obt", "Trabajo", "Medio obtención", Titulado::getMedioObtencionEmpleo),
                        new Campo("remuneracion", "Trabajo", "Remuneración", Titulado::getRemuneracionRango),
                        new Campo("origen_empr", "Emprendimiento", "Origen empr.", Titulado::getOrigenEmprendimiento),
                        new Campo("entregable", "Emprendimiento", "Entregable", Titulado::getTipoEntregableNegocio),
                        new Campo("fin_neg", "Emprendimiento", "Fin. negocio", Titulado::getFinanciamientoNegocio),
                        new Campo("sat_neg", "Emprendimiento", "Satisf. negocio", Titulado::getSatisfaccionNegocio),
                        new Campo("imp_neg", "Emprendimiento", "Importancia formación",
                                        Titulado::getImportanciaFormacionNegocio),
                        new Campo("razon", "Desempleo", "Razón no trabaja", Titulado::getRazonNoTrabaja),
                        new Campo("trab_antes", "Desempleo", "¿Trabajó antes?", Titulado::getHaTenidoTrabajoAntes),
                        new Campo("sat_form", "Percepción", "Satisf. formación", Titulado::getSatisfaccionFormacion),
                        new Campo("concord", "Percepción", "Concordancia",
                                        Titulado::getConcordanciaFormacionRequerimientos),
                        new Campo("aspectos_utiles", "Percepción", "Aspectos útiles", Titulado::getAspectosUtiles),
                        new Campo("aspectos_mejorar", "Percepción", "Aspectos a mejorar", Titulado::getAspectosMejorar),
                        new Campo("asig_vent", "Percepción", "Asignaturas ventajosas",
                                        Titulado::getAsignaturasVentajosas),
                        new Campo("asig_inut", "Percepción", "Asignaturas inútiles", Titulado::getAsignaturasInutiles),
                        new Campo("asig_vent_otra", "Percepción", "Otra asignatura ventajosa",
                                        Titulado::getAsignaturaVentajosaOtra),
                        new Campo("asig_inut_otra", "Percepción", "Otra asignatura inútil",
                                        Titulado::getAsignaturaInutilOtra),
                        new Campo("red", "Contacto", "Red contactos", Titulado::getRedContactos),
                        new Campo("email", "Contacto", "Email", Titulado::getEmailContacto),
                        new Campo("telefono", "Contacto", "Teléfono", Titulado::getTelefonoContacto),
                        new Campo("linkedin", "Contacto", "LinkedIn", Titulado::getLinkedinContacto));

        private static final Map<String, String[]> COLORES_GRUPO = new LinkedHashMap<>();
        static {
                COLORES_GRUPO.put("Demografía", new String[] { "#e0f7fa", "#006064" });
                COLORES_GRUPO.put("Formación", new String[] { "#f3e5f5", "#4a148c" });
                COLORES_GRUPO.put("Trayectoria", new String[] { "#e8f5e9", "#1b5e20" });
                COLORES_GRUPO.put("Trabajo", new String[] { "#fff3e0", "#e65100" });
                COLORES_GRUPO.put("Emprendimiento", new String[] { "#fce4ec", "#880e4f" });
                COLORES_GRUPO.put("Desempleo", new String[] { "#ffebee", "#b71c1c" });
                COLORES_GRUPO.put("Percepción", new String[] { "#e3f2fd", "#0d47a1" });
                COLORES_GRUPO.put("Contacto", new String[] { "#f5f5f5", "#424242" });
        }

        private static final String COLOR_PRINCIPAL = "#0b3d62";
        private static final String COLOR_SECUNDARIO = "#0984e3";
        private static final String COLOR_VERDE = "#27ae60";
        private static final String COLOR_NARANJA = "#e17055";
        private static final String COLOR_MORADO = "#6c5ce7";
        private static final String COLOR_GRIS = "#b2bec3";

        private final RepositorioTitulados repositorio;
        private final ServicioAnalisis analisis;
        private final ServicioCruces cruces;
        private final ServicioInterpretacion interpretacion;

        // NO final: se recrea en construirTablaCompleta
        private Grid<Titulado> tabla = new Grid<>(Titulado.class, false);
        private final Span conteoMostrado = new Span();
        private String filtroSubmuestra = "Todas";
        private final Map<String, Grid.Column<Titulado>> mapaColumnas = new LinkedHashMap<>();

        public VistaTitulados(RepositorioTitulados repositorio,
                        ServicioAnalisis analisis,
                        ServicioCruces cruces,
                        ServicioInterpretacion interpretacion) {
                this.repositorio = repositorio;
                this.analisis = analisis;
                this.cruces = cruces;
                this.interpretacion = interpretacion;

                inyectarCss();

                add(new H1("Titulados encuestados (N = " + repositorio.count() + ")"));

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

        // ---------- CSS: SIEMPRE reemplaza el <style> anterior ----------
        private void inyectarCss() {
                StringBuilder css = new StringBuilder();
                for (var entry : COLORES_GRUPO.entrySet()) {
                        String clase = "grupo-" + slug(entry.getKey());
                        css.append("vaadin-grid::part(").append(clase).append(") {");
                        css.append("  background-color: ").append(entry.getValue()[0]).append(";");
                        css.append("}");
                }
                getElement().executeJs(
                                "const prev = document.getElementById('css-vista-titulados');\n"
                                                + "if (prev) prev.remove();\n"
                                                + "const s = document.createElement('style');\n"
                                                + "s.id = 'css-vista-titulados';\n"
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

        // ---------- Tab 1 ----------
        private VerticalLayout construirTabUnivariado() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.setSpacing(true);
                v.add(construirFiltros());
                v.add(construirTablaCompleta());
                v.add(construirTabla17());
                v.add(construirTabla1());
                v.add(construirTabla2());
                v.add(construirTabla3());
                v.add(construirTabla10());
                v.add(construirTabla11());
                v.add(construirTabla12());
                v.add(construirTabla13());
                v.add(construirTabla14());
                v.add(construirGraficos());
                v.add(new Paragraph(GuiasLectura.COMO_LEER_INTERVALOS));
                return v;
        }

        // ---------- Filtros ----------
        private HorizontalLayout construirFiltros() {
                ComboBox<String> submuestra = new ComboBox<>("Submuestra");
                submuestra.setItems("Todas",
                                "Trabajan en organización",
                                "No trabajan",
                                "Emprendimiento",
                                "Interesados en posgrado",
                                "Con formación continua",
                                "Sin formación continua",
                                "Con remuneración registrada",
                                "Hombres",
                                "Mujeres",
                                "Área IA",
                                "Área Redes y Seguridad");
                submuestra.setValue("Todas");
                submuestra.setWidth("280px");
                submuestra.addValueChangeListener(e -> {
                        filtroSubmuestra = e.getValue();
                        aplicarFiltros();
                        autoScrollYDestacarColumna();
                });

                conteoMostrado.getStyle().set("color", "#666").set("font-size", "13px")
                                .set("align-self", "flex-end").set("padding-bottom", "8px");

                HorizontalLayout fila = new HorizontalLayout(submuestra, conteoMostrado);
                fila.setWidthFull();
                fila.getStyle().set("gap", "16px").set("align-items", "flex-end");
                return fila;
        }

        private void aplicarFiltros() {
                List<Titulado> base = repositorio.findAll();
                List<Titulado> filtrados = base.stream()
                                .filter(t -> switch (filtroSubmuestra) {
                                        case "Trabajan en organización" ->
                                                "TRABAJA_ORG".equals(t.getSituacionLaboralActual());
                                        case "No trabajan" -> "NO_TRABAJA".equals(t.getSituacionLaboralActual());
                                        case "Emprendimiento" -> "EMPRENDIMIENTO".equals(t.getSituacionLaboralActual());
                                        case "Interesados en posgrado" -> "SI".equals(t.getInteresPosgrado());
                                        case "Con formación continua" ->
                                                "SI".equals(t.getRealizoFormacionComplementaria());
                                        case "Sin formación continua" ->
                                                "NO".equals(t.getRealizoFormacionComplementaria());
                                        case "Con remuneración registrada" ->
                                                t.getRemuneracionRango() != null && !t.getRemuneracionRango().isEmpty();
                                        case "Hombres" -> "HOMBRE".equals(LimpiezaDatos.normalizar(t.getGenero()));
                                        case "Mujeres" -> "MUJER".equals(LimpiezaDatos.normalizar(t.getGenero()));
                                        case "Área IA" -> "IA".equals(t.getAreaInteresPosgrado());
                                        case "Área Redes y Seguridad" ->
                                                "REDES_SEGURIDAD".equals(t.getAreaInteresPosgrado());
                                        default -> true;
                                })
                                .toList();
                tabla.setItems(filtrados);
                conteoMostrado.setText(filtrados.size() + " de " + base.size() + " titulados mostrados");
        }

        private String columnaRelevanteParaFiltro() {
                return switch (filtroSubmuestra) {
                        case "Trabajan en organización", "No trabajan", "Emprendimiento" -> "situacion";
                        case "Interesados en posgrado" -> "interes";
                        case "Con formación continua", "Sin formación continua" -> "formacion";
                        case "Con remuneración registrada" -> "remuneracion";
                        case "Hombres", "Mujeres" -> "genero";
                        case "Área IA", "Área Redes y Seguridad" -> "area";
                        default -> null;
                };
        }

        // ---------- Scroll + flash con Web Animations API ----------
        private void autoScrollYDestacarColumna() {
                String clave = columnaRelevanteParaFiltro();
                if (clave == null)
                        return;
                Grid.Column<Titulado> col = mapaColumnas.get(clave);
                if (col == null)
                        return;
                int idx = tabla.getColumns().indexOf(col);
                if (idx < 0)
                        return;

                tabla.getElement().executeJs(
                                "const grid = this;\n"
                                                + "const colIdx = $0;\n"
                                                + "let intentos = 0;\n"
                                                + "function paso() {\n"
                                                // 1) El thead debe tener la fila de headers individuales (la última).
                                                + "  const shadow = grid.shadowRoot;\n"
                                                + "  const thead = shadow && shadow.querySelector('thead');\n"
                                                + "  if (!thead) { if (++intentos < 40) setTimeout(paso, 100); return; }\n"
                                                + "  const rows = thead.querySelectorAll('tr');\n"
                                                + "  if (rows.length < 2) { if (++intentos < 40) setTimeout(paso, 100); return; }\n"
                                                + "  const ths = rows[rows.length - 1].querySelectorAll('th');\n"
                                                + "  const targetTh = ths[colIdx];\n"
                                                // 2) Scroll horizontal suave
                                                + "  if (targetTh) {\n"
                                                + "    targetTh.scrollIntoView({behavior:'smooth', block:'nearest', inline:'center'});\n"
                                                + "  }\n"
                                                // 3) Flash con WAAPI a los <td> visibles (espera 500ms para que el
                                                // scroll avance)
                                                + "  setTimeout(() => {\n"
                                                + "    const tbody = shadow.querySelector('tbody');\n"
                                                + "    if (!tbody) return;\n"
                                                + "    const filas = tbody.querySelectorAll('tr');\n"
                                                + "    if (filas.length === 0) { if (++intentos < 40) setTimeout(paso, 100); return; }\n"
                                                + "    filas.forEach(row => {\n"
                                                + "      const cell = row.children[colIdx];\n"
                                                + "      if (!cell || typeof cell.animate !== 'function') return;\n"
                                                + "      cell.animate([\n"
                                                + "        { backgroundColor:'#ffe066',\n"
                                                + "          boxShadow:'inset 0 0 0 3px #ffc107',\n"
                                                + "          offset: 0 },\n"
                                                + "        { backgroundColor:'#ffe066',\n"
                                                + "          boxShadow:'inset 0 0 0 3px #ffc107',\n"
                                                + "          offset: 0.6 },\n"
                                                + "        { offset: 1 }\n"
                                                + "      ], { duration: 2500, easing: 'ease-out', fill: 'none' });\n"
                                                + "    });\n"
                                                + "  }, 500);\n"
                                                + "}\n"
                                                + "paso();",
                                idx);
        }

        // ---------- Tabla completa ----------
        private VerticalLayout construirTablaCompleta() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.add(new H3("Tabla completa de titulados"));

                // Recrear el Grid para evitar duplicar columnas al volver al tab
                tabla = new Grid<>(Titulado.class, false);
                mapaColumnas.clear();

                Map<String, List<Campo>> porGrupo = new LinkedHashMap<>();
                for (Campo c : CAMPOS) {
                        porGrupo.computeIfAbsent(c.grupo(), k -> new ArrayList<>()).add(c);
                }

                Map<String, List<Grid.Column<Titulado>>> colsPorGrupo = new LinkedHashMap<>();
                for (var entry : porGrupo.entrySet()) {
                        String grupo = entry.getKey();
                        String parteGrupo = "grupo-" + slug(grupo);
                        List<Grid.Column<Titulado>> cols = new ArrayList<>();
                        for (Campo c : entry.getValue()) {
                                Grid.Column<Titulado> col = tabla.addColumn(t -> {
                                        String vc = c.getter().apply(t);
                                        return vc == null ? "" : vc;
                                })
                                                .setHeader(c.etiqueta())
                                                .setAutoWidth(true)
                                                .setResizable(true);
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
                span.getStyle()
                                .set("background-color", bgColor)
                                .set("color", txtColor)
                                .set("font-weight", "bold")
                                .set("text-align", "center")
                                .set("padding", "6px")
                                .set("width", "100%")
                                .set("display", "block");
                cell.setComponent(span);
        }

        // ---------- Modal con Accordion ----------
        private void abrirModalDetalle(Titulado t) {
                Dialog modal = new Dialog();
                modal.setHeaderTitle("Detalle del titulado: " + t.getIdTitulado());
                modal.setWidth("900px");
                modal.setHeight("90vh");
                modal.getElement().getStyle().set("border-radius", "14px");

                VerticalLayout cont = new VerticalLayout();
                cont.setPadding(false);
                cont.setSpacing(true);

                Span resumen = new Span(CAMPOS.size() + " variables · "
                                + (t.getGenero() == null ? "" : t.getGenero()) + " · "
                                + (t.getEdad() == null ? "" : t.getEdad()) + " años");
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
                        String grupo = entry.getKey();
                        String[] colores = COLORES_GRUPO.get(grupo);

                        Grid<Campo> g = new Grid<>();
                        g.addColumn(Campo::etiqueta).setHeader("Variable")
                                        .setWidth("240px").setFlexGrow(0);
                        g.addColumn(new ComponentRenderer<>(c -> {
                                String vv = c.getter().apply(t);
                                Span s = new Span((vv == null || vv.isEmpty()) ? "—" : vv);
                                s.getStyle()
                                                .set("white-space", "normal")
                                                .set("overflow-wrap", "anywhere")
                                                .set("word-break", "break-word")
                                                .set("display", "block");
                                return s;
                        })).setHeader("Valor").setFlexGrow(1);

                        g.setItems(entry.getValue());
                        g.setAllRowsVisible(true);
                        g.setHeight(null);

                        Span badge = new Span(grupo + "  (" + entry.getValue().size() + ")");
                        badge.getStyle()
                                        .set("background-color", colores[0])
                                        .set("color", colores[1])
                                        .set("padding", "6px 12px")
                                        .set("border-radius", "6px")
                                        .set("font-weight", "bold")
                                        .set("font-size", "13px");

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

        // ---------- Tabla 17 ----------
        private VerticalLayout construirTabla17() {
                List<Map<String, String>> m = analisis.mapasTitulados();
                int n = m.size();
                double insercion = analisis.tasaInsercion();
                double interes = analisis.intencionPosgrado();
                double beca = analisis.proporcionRequiereBeca();
                double pertinencia = analisis.proporcionDonde(m, "pertinencia_trabajo",
                                v -> LimpiezaDatos.normalizar(v).contains("ACUERDO"));
                double ofertaAfin = analisis.proporcionDonde(m, "institucion_futura_posgrado",
                                v -> v.equals("DIR_POSGRADO_FCyT") || v.equals("EUPG"));

                HorizontalLayout fila = Componentes.filaKpis(
                                Componentes.tarjetaKpiConIC("Trabajan en organización", insercion,
                                                IntervalosConfianza.paraProporcion(insercion, n),
                                                "Trabajan o emprenden"),
                                Componentes.tarjetaKpiConIC("Interesados en posgrado", interes,
                                                IntervalosConfianza.paraProporcion(interes, n), "Demanda potencial"),
                                Componentes.tarjetaKpiConIC("Financiaron con recursos propios", beca,
                                                IntervalosConfianza.paraProporcion(beca, n), "Requieren beca"),
                                Componentes.tarjetaKpiConIC("Consideran formación pertinente", pertinencia,
                                                IntervalosConfianza.paraProporcion(pertinencia, n), "Pertinencia"),
                                Componentes.tarjetaKpiConIC("Oferta FCyT es afín", ofertaAfin,
                                                IntervalosConfianza.paraProporcion(ofertaAfin, n),
                                                "Institución = UMSS"));

                return Componentes.seccion("Indicadores clave con IC 95% (Wilson)",
                                "Cada tarjeta muestra la proporción real y su intervalo de confianza al 95%.", fila);
        }

        // ---------- Otras tablas ----------
        private VerticalLayout construirTabla1() {
                Map<String, Integer> genero = analisis.frecuenciasDe("genero");
                Map<String, Integer> situacion = analisis.frecuenciasDe("situacion_laboral_actual");
                Map<String, Integer> edadRango = new LinkedHashMap<>();
                for (String v : analisis.columnaDe(analisis.mapasTitulados(), "edad")) {
                        try {
                                double e = Double.parseDouble(v);
                                String k = e <= 22 ? "19-22"
                                                : e <= 26 ? "23-26"
                                                                : e <= 30 ? "27-30"
                                                                                : e <= 34 ? "31-34" : "35+";
                                edadRango.merge(k, 1, Integer::sum);
                        } catch (NumberFormatException ignored) {
                        }
                }
                return Componentes.seccion("Tabla Caracterización demográfica",
                                "Distribución de los titulados por edad, género y situación laboral.",
                                new H3("Tabla Edad por rango"),
                                Componentes.tablaFrecuencias(edadRango, "Rango de edad"),
                                new H3("Tabla Género"), Componentes.tablaFrecuencias(genero, "Género"),
                                new H3("Tabla Situación laboral"),
                                Componentes.tablaFrecuencias(situacion, "Situación"));
        }

        private VerticalLayout construirTabla2() {
                ResumenNumerico r = analisis.resumenNumerico("anios_vida_profesional");
                return Componentes.seccion("Tabla Años de vida profesional",
                                "El coeficiente de variación > 30% indica alta heterogeneidad.",
                                Componentes.tablaResumenNumerico(r, "años"));
        }

        private VerticalLayout construirTabla3() {
                Map<String, Integer> f = analisis.frecuenciasDe("tiempo_primer_empleo");
                return Componentes.seccion("Tabla Tiempo al primer empleo",
                                "Frecuencia y porcentaje del tiempo que tardaron en conseguir su primer empleo.",
                                Componentes.tablaFrecuencias(f, "Tiempo"));
        }

        private VerticalLayout construirTabla10() {
                List<Map<String, String>> m = analisis.mapasTitulados();
                int n = m.size();
                double form = analisis.proporcionDonde(m, "realizo_formacion_complementaria", v -> v.equals("SI"));
                double posg = analisis.proporcionDonde(m, "interes_posgrado", v -> v.equals("SI"));
                double propios = analisis.proporcionDonde(m, "financiamiento_formacion",
                                v -> LimpiezaDatos.normalizar(v).contains("PROPIO"));
                HorizontalLayout fila = Componentes.filaKpis(
                                Componentes.tarjetaKpiConIC("Formación continua", form,
                                                IntervalosConfianza.paraProporcion(form, n), "Realizó o realiza"),
                                Componentes.tarjetaKpiConIC("Interesados en posgrado", posg,
                                                IntervalosConfianza.paraProporcion(posg, n), "Demanda"),
                                Componentes.tarjetaKpiConIC("Financiaron con recursos propios", propios,
                                                IntervalosConfianza.paraProporcion(propios, n),
                                                "Dependencia económica"));
                return Componentes.seccion("Indicadores de formación continua",
                                "Tres tasas fundamentales de la formación complementaria.", fila);
        }

        private VerticalLayout construirTabla11() {
                Map<String, Integer> nivel = analisis.frecuenciasDe("nivel_posgrado");
                Map<String, Integer> lugar = analisis.frecuenciasDe("institucion_formacion");
                return Componentes.seccion("Nivel y lugar de formación",
                                "Nivel alcanzado y dónde cursó la formación complementaria.",
                                new H3("Tabla Nivel alcanzado"), Componentes.tablaFrecuencias(nivel, "Nivel"),
                                new H3("Tabla Lugar donde cursó"), Componentes.tablaFrecuencias(lugar, "Institución"));
        }

        private VerticalLayout construirTabla12() {
                Map<String, Integer> fin = analisis.frecuenciasDe("financiamiento_formacion");
                return Componentes.seccion("Tabla Fuente de financiamiento",
                                "Distribución de quién pagó la formación complementaria.",
                                Componentes.tablaFrecuencias(fin, "Fuente"));
        }

        private VerticalLayout construirTabla13() {
                Map<String, Integer> areas = analisis.frecuenciasDe("area_interes_posgrado");
                return Componentes.seccion("Tabla Áreas de interés para posgrado",
                                "Preferencia de áreas declaradas por los titulados.",
                                Componentes.tablaFrecuencias(areas, "Área"));
        }

        private VerticalLayout construirTabla14() {
                Map<String, Integer> sat = analisis.frecuenciasDe("satisfaccion_formacion");
                Map<String, Integer> conc = analisis.frecuenciasDe("concordancia_formacion_requerimientos");
                return Componentes.seccion("Satisfacción y concordancia",
                                "Percepción de los titulados sobre la formación recibida.",
                                new H3("Tabla Satisfacción global"), Componentes.tablaFrecuencias(sat, "Nivel"),
                                new H3("Tabla Concordancia formación-requerimientos"),
                                Componentes.tablaFrecuencias(conc, "Nivel"));
        }

        // ---------- Gráficos ----------
        private VerticalLayout construirGraficos() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);

                List<Double> antiguedades = analisis.numerosDe(analisis.mapasTitulados(), "anios_vida_profesional");
                int[] buckets = new int[10];
                for (double a : antiguedades)
                        buckets[Math.min(9, (int) (a / 2))]++;
                v.add(GraficoCanvas.enTarjeta(
                                GraficoCanvas.barrasVerticales("histAntiguedad", buckets,
                                                new String[] { "0-2", "2-4", "4-6", "6-8", "8-10", "10-12", "12-14",
                                                                "14-16", "16-18", "18+" },
                                                COLOR_PRINCIPAL, "Distribución de antigüedad (años)"),
                                "Histograma de antigüedad"));

                List<Double> satisfacciones = analisis.likertDe(analisis.mapasTitulados(), "satisfaccion_formacion");
                int[] conteoSat = new int[4];
                for (double s : satisfacciones) {
                        int idx = (int) Math.round(s) - 1;
                        if (idx >= 0 && idx < 4)
                                conteoSat[idx]++;
                }
                v.add(GraficoCanvas.enTarjeta(
                                GraficoCanvas.barrasVerticales("barsSatisfaccion", conteoSat,
                                                new String[] { "1 - Muy en desacuerdo", "2 - En desacuerdo",
                                                                "3 - De acuerdo", "4 - Totalmente de acuerdo" },
                                                COLOR_SECUNDARIO, "Distribución de satisfacción"),
                                "Satisfacción con la formación"));

                v.add(graficoBarras("barsAreas", analisis.frecuenciasDe("area_interes_posgrado"),
                                COLOR_VERDE, "Áreas de interés de posgrado", "Áreas de interés"));
                v.add(graficoTorta("tortaSituacion", analisis.frecuenciasDe("situacion_laboral_actual"),
                                new String[] { COLOR_VERDE, COLOR_NARANJA, COLOR_MORADO },
                                "Distribución de situación laboral", "Situación laboral"));
                v.add(graficoTorta("tortaGenero", analisis.frecuenciasDe("genero"),
                                new String[] { COLOR_PRINCIPAL, COLOR_NARANJA, COLOR_MORADO },
                                "Distribución por género", "Género"));

                Map<String, Integer> edadRango = new LinkedHashMap<>();
                for (String v2 : analisis.columnaDe(analisis.mapasTitulados(), "edad")) {
                        try {
                                double e = Double.parseDouble(v2);
                                String k = e <= 22 ? "19-22"
                                                : e <= 26 ? "23-26"
                                                                : e <= 30 ? "27-30"
                                                                                : e <= 34 ? "31-34" : "35+";
                                edadRango.merge(k, 1, Integer::sum);
                        } catch (NumberFormatException ignored) {
                        }
                }
                v.add(graficoBarras("barsEdad", edadRango, COLOR_PRINCIPAL,
                                "Distribución por rango de edad", "Rango de edad"));

                v.add(graficoTorta("tortaFormacion", analisis.frecuenciasDe("realizo_formacion_complementaria"),
                                new String[] { COLOR_VERDE, COLOR_GRIS },
                                "¿Realizó formación complementaria?", "Formación continua"));
                v.add(graficoBarras("barsTiempo", analisis.frecuenciasDe("tiempo_primer_empleo"),
                                COLOR_NARANJA, "Tiempo al primer empleo", "Tiempo al primer empleo"));
                v.add(graficoBarras("barsRemuneracion", analisis.frecuenciasDe("remuneracion_rango"),
                                COLOR_MORADO, "Distribución por rango de remuneración", "Remuneración"));
                v.add(graficoBarras("barsNivel", analisis.frecuenciasDe("nivel_posgrado"),
                                COLOR_SECUNDARIO, "Nivel de posgrado alcanzado", "Nivel de posgrado"));
                v.add(graficoBarras("barsInstitucion", analisis.frecuenciasDe("institucion_formacion"),
                                COLOR_VERDE, "Institución donde cursó formación", "Institución"));
                return v;
        }

        private Component graficoBarras(String id, Map<String, Integer> frec,
                        String color, String titulo, String tarjeta) {
                if (frec.isEmpty())
                        return new Paragraph("Sin datos para: " + titulo);
                List<Map.Entry<String, Integer>> orden = new ArrayList<>(frec.entrySet());
                orden.sort((a, b) -> b.getValue() - a.getValue());
                String[] etiquetas = new String[orden.size()];
                int[] valores = new int[orden.size()];
                for (int i = 0; i < orden.size(); i++) {
                        etiquetas[i] = orden.get(i).getKey();
                        valores[i] = orden.get(i).getValue();
                }
                return GraficoCanvas.enTarjeta(
                                GraficoCanvas.barrasVerticales(id, valores, etiquetas, color, titulo), tarjeta);
        }

        private Component graficoTorta(String id, Map<String, Integer> frec,
                        String[] colores, String titulo, String tarjeta) {
                if (frec.isEmpty())
                        return new Paragraph("Sin datos para: " + titulo);
                List<Map.Entry<String, Integer>> orden = new ArrayList<>(frec.entrySet());
                orden.sort((a, b) -> b.getValue() - a.getValue());
                String[] etiquetas = new String[orden.size()];
                int[] valores = new int[orden.size()];
                for (int i = 0; i < orden.size(); i++) {
                        etiquetas[i] = orden.get(i).getKey();
                        valores[i] = orden.get(i).getValue();
                }
                return GraficoCanvas.enTarjeta(
                                GraficoCanvas.torta(id, valores, etiquetas, colores, titulo), tarjeta);
        }

        // ---------- Tabs placeholder (Paso 3) ----------
        private VerticalLayout construirTabBivariado() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.setSpacing(true);

                v.add(new H2("Análisis Bivariado — Pruebas de Independencia (χ²)"));
                v.add(new Paragraph(
                                "Para determinar si existe una relación estadísticamente significativa entre "
                                                + "las variables, se aplicó la prueba de independencia Chi-cuadrado (χ²). "
                                                + "Para medir la intensidad de la asociación se calculó la V de Cramér. "
                                                + "Los intervalos de confianza al 95% se construyeron con la Fórmula de Wilson."));

                List<FichaCruce> lista = cruces.crucesTitulados();
                Map<String, List<FichaCruce>> porSeccion = agruparCrucesPorSeccion(lista);

                int contador = 0;
                for (var entry : porSeccion.entrySet()) {
                        v.add(new H3(entry.getKey()));
                        for (FichaCruce c : entry.getValue()) {
                                contador++;
                                v.add(renderCruceConTexto(c, contador));
                        }
                }

                v.add(construirMatrizResumenBivariada());
                return v;
        }

        private VerticalLayout construirTabMultivariado() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.setSpacing(true);

                v.add(new H2("Análisis Exploratorio Multivariable (Modelamiento Analítico)"));
                v.add(new Paragraph(
                                "Se aplica la Ecuación (10) de Media Condicional del Marco Metodológico. "
                                                + "A continuación se presentan tres modelamientos independientes que cruzan "
                                                + "dos variables categóricas (X, Y) con una variable numérica (Z)."));

                v.add(construirMultivariable1());
                v.add(construirMultivariable2());
                v.add(construirMultivariable3());
                return v;
        }

        private VerticalLayout construirMultivariable1() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.setSpacing(true);

                v.add(new H3("Área de interés, Fuente de financiamiento y Años de vida profesional"));
                v.add(new Paragraph("Tabulación Cruzada Multivariable:"));

                ServicioAnalisis.TablaMultivariable tabla = analisis.tablaMultivariableTitulados(
                                "area_interes_posgrado", "financiamiento_formacion", "anios_vida_profesional");
                v.add(renderTablaMultivariable(tabla,
                                "Área de Interés (X)", "Financiamiento (Y)", "Antigüedad Promedio (Z)", " años"));

                v.add(new H4("Hallazgos y Patrones por Área de Interés"));
                v.add(construirHallazgos(interpretacion.interpretar(tabla)));

                v.add(GraficoCanvas.enTarjeta(
                                GraficoCanvas.dispersionCategorizada(
                                                "disp_mv1",
                                                tabla.valoresX(),
                                                tabla.valoresY(),
                                                puntosParaGrafico(tabla),
                                                new String[] { COLOR_PRINCIPAL, COLOR_SECUNDARIO, COLOR_VERDE,
                                                                COLOR_NARANJA, COLOR_MORADO, COLOR_GRIS },
                                                "Área de interés", "Antigüedad (años)",
                                                "Dispersión: Área × Financiamiento × Antigüedad"),
                                "Gráfico de dispersión categorizado (tamaño = n)"));

                // Gráfico de barras apiladas (X × Y por frecuencia)
                String[] etiquetasX1 = tabla.valoresX().toArray(new String[0]);
                String[] etiquetasY1 = tabla.valoresY().toArray(new String[0]);
                int[][] frecStack1 = new int[etiquetasX1.length][etiquetasY1.length];
                for (var c : tabla.celdas()) {
                        int xi = tabla.valoresX().indexOf(c.valorX());
                        int yi = tabla.valoresY().indexOf(c.valorY());
                        if (xi >= 0 && yi >= 0)
                                frecStack1[xi][yi] = c.n();
                }
                v.add(GraficoCanvas.enTarjeta(
                                GraficoCanvas.barrasApiladas(
                                                "stack_mv1",
                                                etiquetasX1,
                                                etiquetasY1,
                                                frecStack1,
                                                new String[] { COLOR_VERDE, COLOR_NARANJA, COLOR_MORADO,
                                                                COLOR_PRINCIPAL, COLOR_SECUNDARIO },
                                                "Barras apiladas: Área × Financiamiento"),
                                "Gráfico de barras apiladas (frecuencia por financiamiento)"));

                return v;
        }

        private VerticalLayout construirMultivariable2() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.setSpacing(true);

                v.add(new H3("Sector Laboral, Cargo y Remuneración Promedio"));
                v.add(new Paragraph("Tabulación Cruzada Multivariable:"));

                ServicioAnalisis.TablaMultivariable tabla = analisis.tablaMultivariableTitulados(
                                "sector", "cargo_grupo", "remuneracion_rango");
                v.add(renderTablaMultivariable(tabla,
                                "Sector (X)", "Cargo (Y)", "Remuneración Promedio (Z)", " Bs."));

                v.add(new H4("Hallazgos y Patrones por Sector Laboral"));
                v.add(construirHallazgos(interpretacion.interpretar(tabla)));

                v.add(GraficoCanvas.enTarjeta(
                                GraficoCanvas.dispersionCategorizada(
                                                "disp_mv2",
                                                tabla.valoresX(),
                                                tabla.valoresY(),
                                                puntosParaGrafico(tabla),
                                                new String[] { COLOR_PRINCIPAL, COLOR_SECUNDARIO, COLOR_VERDE,
                                                                COLOR_NARANJA, COLOR_MORADO, COLOR_GRIS },
                                                "Sector laboral", "Remuneración promedio (Bs.)",
                                                "Dispersión: Sector × Cargo × Remuneración"),
                                "Gráfico de dispersión categorizado (tamaño = n)"));

                // Gráfico de barras apiladas (X × Y por frecuencia)
                String[] etiquetasX2 = tabla.valoresX().toArray(new String[0]);
                String[] etiquetasY2 = tabla.valoresY().toArray(new String[0]);
                int[][] frecStack2 = new int[etiquetasX2.length][etiquetasY2.length];
                for (var c : tabla.celdas()) {
                        int xi = tabla.valoresX().indexOf(c.valorX());
                        int yi = tabla.valoresY().indexOf(c.valorY());
                        if (xi >= 0 && yi >= 0)
                                frecStack2[xi][yi] = c.n();
                }
                v.add(GraficoCanvas.enTarjeta(
                                GraficoCanvas.barrasApiladas(
                                                "stack_mv2",
                                                etiquetasX2,
                                                etiquetasY2,
                                                frecStack2,
                                                new String[] { COLOR_PRINCIPAL, COLOR_SECUNDARIO, COLOR_VERDE,
                                                                COLOR_NARANJA, COLOR_MORADO },
                                                "Barras apiladas: Sector × Cargo"),
                                "Gráfico de barras apiladas (frecuencia por cargo)"));

                return v;
        }

        private VerticalLayout construirMultivariable3() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.setSpacing(true);

                v.add(new H3("Satisfacción Formativa, Concordancia y Antigüedad"));
                v.add(new Paragraph("Tabulación Cruzada Multivariable:"));
                Paragraph notaEscala = new Paragraph(
                                "Escala Likert agrupada: 'Alta' = De acuerdo / Totalmente de acuerdo; "
                                                + "'Baja' = En desacuerdo / Totalmente en desacuerdo.");
                notaEscala.getStyle().set("font-style", "italic")
                                .set("font-size", "12px").set("color", "#666");
                v.add(notaEscala);

                ServicioAnalisis.TablaMultivariable tabla = analisis.tablaMultivariableTitulados(
                                "satisfaccion_grupo", "concordancia_grupo", "anios_vida_profesional");
                v.add(renderTablaMultivariable(tabla,
                                "Satisfacción (X)", "Concordancia (Y)", "Antigüedad Promedio (Z)", " años"));

                v.add(new H4("Hallazgos y Patrones por Nivel de Satisfacción"));
                v.add(construirHallazgos(interpretacion.interpretar(tabla)));

                v.add(GraficoCanvas.enTarjeta(
                                GraficoCanvas.dispersionCategorizada(
                                                "disp_mv3",
                                                tabla.valoresX(),
                                                tabla.valoresY(),
                                                puntosParaGrafico(tabla),
                                                new String[] { COLOR_PRINCIPAL, COLOR_VERDE, COLOR_NARANJA,
                                                                COLOR_MORADO },
                                                "Satisfacción", "Antigüedad (años)",
                                                "Dispersión: Satisfacción × Concordancia × Antigüedad"),
                                "Gráfico de dispersión categorizado (tamaño = n)"));

                // Gráfico de barras apiladas (X × Y por frecuencia)
                String[] etiquetasX3 = tabla.valoresX().toArray(new String[0]);
                String[] etiquetasY3 = tabla.valoresY().toArray(new String[0]);
                int[][] frecStack3 = new int[etiquetasX3.length][etiquetasY3.length];
                for (var c : tabla.celdas()) {
                        int xi = tabla.valoresX().indexOf(c.valorX());
                        int yi = tabla.valoresY().indexOf(c.valorY());
                        if (xi >= 0 && yi >= 0)
                                frecStack3[xi][yi] = c.n();
                }
                v.add(GraficoCanvas.enTarjeta(
                                GraficoCanvas.barrasApiladas(
                                                "stack_mv3",
                                                etiquetasX3,
                                                etiquetasY3,
                                                frecStack3,
                                                new String[] { COLOR_PRINCIPAL, COLOR_VERDE, COLOR_NARANJA,
                                                                COLOR_MORADO },
                                                "Barras apiladas: Satisfacción × Concordancia"),
                                "Gráfico de barras apiladas (frecuencia por concordancia)"));

                return v;
        }

        // ================================================================
        // Helpers del Paso 3 — Bivariado + Multivariado
        // ================================================================

        private record TextoCruce(String titulo, String descripcion, String interpretacion) {
        }

        private static final Map<String, TextoCruce> TEXTOS_CRUCES = new LinkedHashMap<>();
        static {
                TEXTOS_CRUCES.put("Edad x Situacion Laboral", new TextoCruce(
                                "Relación entre Edad y Situación Laboral",
                                "Determinar si la etapa generacional o la edad cronológica condiciona la "
                                                + "probabilidad de encontrar empleo en el mercado tecnológico.",
                                "Existe una asociación estadísticamente significativa entre la edad y la situación "
                                                + "laboral. Los intervalos de confianza revelan que el grupo de 19 a 26 años presenta "
                                                + "la mayor tasa de desempleo. Se recomienda implementar programas de inserción "
                                                + "temprana (pasantías, bolsa de trabajo, convenios con empresas) dirigidos a este segmento."));
                TEXTOS_CRUCES.put("Genero x Situacion Laboral", new TextoCruce(
                                "Relación entre Género y Situación Laboral",
                                "Determinar si el género condiciona la probabilidad de estar empleado, desempleado "
                                                + "o emprender en el sector tecnológico.",
                                "No se encontró evidencia estadística de que el género influya en la situación laboral. "
                                                + "Los intervalos de confianza se solapan considerablemente. Se recomienda que las "
                                                + "políticas de inserción laboral sean generales, sin sesgos de género."));
                TEXTOS_CRUCES.put("Cohorte x Situacion Laboral", new TextoCruce(
                                "Relación entre Cohorte de Experiencia y Situación Laboral",
                                "Evaluar si los años de trayectoria (Junior, Intermedio, Consolidado) inciden "
                                                + "en la estabilidad ocupacional.",
                                "Aunque los junior tienen una tasa de empleo ligeramente menor, los intervalos de "
                                                + "confianza se solapan con los otros grupos. La diferencia no es estadísticamente "
                                                + "significativa. Se recomienda monitorear a los junior, sin requerir una política "
                                                + "diferenciada por años de experiencia."));
                TEXTOS_CRUCES.put("Genero x Tiempo al Primer Empleo", new TextoCruce(
                                "Relación entre Género y Tiempo al Primer Empleo",
                                "Determinar si las mujeres tardan más tiempo en conseguir su primer empleo "
                                                + "relacionado con la carrera.",
                                "No hay evidencia de que el género afecte el tiempo de inserción. Hombres y mujeres "
                                                + "consiguen empleo en plazos similares. Se recomienda mantener programas de "
                                                + "inserción generales."));
                TEXTOS_CRUCES.put("Cohorte x Numero de Empleos", new TextoCruce(
                                "Relación entre Cohorte y Número de Empleos",
                                "Evaluar si los profesionales con más años de experiencia han tenido una mayor "
                                                + "rotación laboral (número de empleos desempeñados).",
                                "Existe una asociación muy fuerte. Los junior se concentran en 1 empleo, mientras "
                                                + "que los consolidados presentan alta rotación (3+ empleos). Se recomienda fomentar "
                                                + "la estabilidad en puestos de mayor responsabilidad para los consolidados y "
                                                + "programas de retención para los junior."));
                TEXTOS_CRUCES.put("Edad x Remuneracion", new TextoCruce(
                                "Relación entre Edad y Remuneración",
                                "Evaluar si los titulados de mayor edad perciben salarios más altos.",
                                "Existe una asociación significativa. Los profesionales de 35 años o más tienen una "
                                                + "probabilidad mucho mayor de ganar más de Bs. 7.000 que los menores de 34. "
                                                + "Se recomienda enfocar los esfuerzos de formación continua en los jóvenes para "
                                                + "acelerar su progresión salarial."));
                TEXTOS_CRUCES.put("Sector x Remuneracion", new TextoCruce(
                                "Relación entre Sector y Remuneración",
                                "Comparar los salarios entre el sector público y privado.",
                                "El sector privado ofrece salarios significativamente más altos. Se recomienda "
                                                + "orientar las pasantías y convenios hacia el sector privado, especialmente "
                                                + "software factories y banca."));
                TEXTOS_CRUCES.put("Genero x Remuneracion", new TextoCruce(
                                "Relación entre Género y Remuneración",
                                "Evaluar si existe una brecha salarial de género.",
                                "No se encontró evidencia de brecha salarial de género. La distribución es casi "
                                                + "idéntica. Se recomienda no implementar políticas diferenciadas por género en "
                                                + "este aspecto."));
                TEXTOS_CRUCES.put("Formacion Continua x Remuneracion", new TextoCruce(
                                "Relación entre Formación Continua y Remuneración",
                                "Determinar si tener un posgrado (maestría/especialidad) mejora el salario.",
                                "No se encontró evidencia de que tener un posgrado se traduzca en un mayor salario. "
                                                + "Se recomienda investigar por qué el posgrado no está siendo valorado salarialmente "
                                                + "en el mercado local."));
                TEXTOS_CRUCES.put("Formacion Continua x Interes Posgrado", new TextoCruce(
                                "Relación entre Formación Continua e Interés en Posgrado",
                                "Determinar si quienes ya han realizado un programa de formación complementaria "
                                                + "tienen mayor interés en seguir estudiando.",
                                "Quienes ya realizaron formación continua muestran una predisposición "
                                                + "significativamente mayor a seguir estudiando que quienes no lo han hecho. "
                                                + "Se recomienda crear 'rutas de continuidad' (ej. convalidación de diplomados "
                                                + "hacia maestrías) para captar a este segmento que ya demostró interés."));
                TEXTOS_CRUCES.put("Nivel de Posgrado x Lugar", new TextoCruce(
                                "Relación entre Nivel de Posgrado y Lugar donde Cursó",
                                "Evaluar si la UMSS (FCyT/EUPG) capta más estudiantes de diplomado que de maestría.",
                                "La UMSS capta mayoritariamente estudiantes de diplomado, pero solo una fracción "
                                                + "minoritaria de maestría/especialidad. Se recomienda a la Dirección de Posgrado "
                                                + "de la FCyT fortalecer su oferta de maestrías propias, ya que el mercado de "
                                                + "posgrados se lo llevan otras universidades."));
                TEXTOS_CRUCES.put("Percepcion FCyT x Organizacion Preferida", new TextoCruce(
                                "Relación entre Percepción de la FCyT y Organización Preferida",
                                "Determinar si quienes tienen una buena percepción de la oferta de posgrado "
                                                + "de la FCyT prefieren quedarse en la UMSS.",
                                "La percepción está asociada a la elección. Si un titulado cree que la FCyT tiene "
                                                + "buenos programas, la probabilidad de que se quede es notablemente mayor. "
                                                + "Se recomienda difundir agresivamente la oferta de la FCyT en las áreas de "
                                                + "mayor demanda (IA, Software)."));
                TEXTOS_CRUCES.put("Cohorte x Area de Interes Posgrado", new TextoCruce(
                                "Relación entre Cohorte y Área de Interés para Posgrado",
                                "Evaluar si los más jóvenes prefieren IA y los consolidados prefieren otras "
                                                + "áreas como Gestión o Redes.",
                                "No se encontró evidencia suficiente para afirmar que el área de interés depende "
                                                + "de la cohorte, aunque la tendencia es clara: los junior prefieren IA mientras "
                                                + "los consolidados prefieren Redes y Seguridad. Se recomienda diseñar una oferta "
                                                + "modular que cubra ambas áreas."));
                TEXTOS_CRUCES.put("Satisfaccion Global x Concordancia", new TextoCruce(
                                "Relación entre Satisfacción Global y Concordancia Formación-Requerimientos",
                                "Evaluar si quienes están satisfechos con la carrera sienten que su trabajo es "
                                                + "pertinente a lo que estudiaron.",
                                "Existe una asociación muy fuerte. Si un titulado siente que su trabajo es "
                                                + "pertinente a su formación, su satisfacción con la carrera es alta; si no, cae "
                                                + "drásticamente. Se recomienda mejorar las prácticas profesionales para que los "
                                                + "titulados encuentren trabajos pertinentes, lo que elevará su satisfacción."));
                TEXTOS_CRUCES.put("Pertinencia del Trabajo x Satisfaccion", new TextoCruce(
                                "Relación entre Pertinencia del Trabajo y Satisfacción con la Carrera",
                                "Evaluar si trabajar en algo relacionado con la carrera mejora la percepción "
                                                + "de la formación recibida.",
                                "La pertinencia del trabajo está fuertemente asociada a la satisfacción con la "
                                                + "carrera. Quienes trabajan en su área están mucho más satisfechos. Se recomienda "
                                                + "fortalecer los vínculos con empresas para que más egresados trabajen en su área."));
        }

        private static final Map<String, String[]> SECCIONES_BIVARIADO = new LinkedHashMap<>();
        static {
                SECCIONES_BIVARIADO.put("Demografía y Empleabilidad",
                                new String[] { "Edad x Situacion Laboral", "Genero x Situacion Laboral",
                                                "Cohorte x Situacion Laboral" });
                SECCIONES_BIVARIADO.put("Trayectoria y Movilidad",
                                new String[] { "Genero x Tiempo al Primer Empleo", "Cohorte x Numero de Empleos" });
                SECCIONES_BIVARIADO.put("Condiciones Laborales y Remuneración",
                                new String[] { "Edad x Remuneracion", "Sector x Remuneracion",
                                                "Genero x Remuneracion", "Formacion Continua x Remuneracion" });
                SECCIONES_BIVARIADO.put("Formación Continua y Posgrado",
                                new String[] { "Formacion Continua x Interes Posgrado", "Nivel de Posgrado x Lugar",
                                                "Percepcion FCyT x Organizacion Preferida",
                                                "Cohorte x Area de Interes Posgrado" });
                SECCIONES_BIVARIADO.put("Percepción Formativa y Currículo",
                                new String[] { "Satisfaccion Global x Concordancia",
                                                "Pertinencia del Trabajo x Satisfaccion" });
        }

        private Map<String, List<FichaCruce>> agruparCrucesPorSeccion(List<FichaCruce> cruces) {
                Map<String, FichaCruce> porEtiqueta = new LinkedHashMap<>();
                for (FichaCruce c : cruces) {
                        porEtiqueta.put(c.etiqueta(), c);
                }
                Map<String, List<FichaCruce>> out = new LinkedHashMap<>();
                for (var entry : SECCIONES_BIVARIADO.entrySet()) {
                        List<FichaCruce> lista = new ArrayList<>();
                        for (String etiqueta : entry.getValue()) {
                                FichaCruce c = porEtiqueta.get(etiqueta);
                                if (c != null)
                                        lista.add(c);
                        }
                        out.put(entry.getKey(), lista);
                }
                return out;
        }

        private VerticalLayout renderCruceConTexto(FichaCruce c, int numero) {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.setSpacing(true);

                TextoCruce t = TEXTOS_CRUCES.get(c.etiqueta());
                if (t != null) {
                        v.add(new H4(numero + ". " + t.titulo()));
                        Paragraph desc = new Paragraph(t.descripcion());
                        desc.getStyle().set("font-size", "13px").set("color", "#555");
                        v.add(desc);
                }

                v.add(PanelCruce.render(c, cruces));

                if (t != null && !t.interpretacion().isEmpty()) {
                        Paragraph p = new Paragraph("Interpretación y recomendación: " + t.interpretacion());
                        p.getStyle().set("font-style", "italic").set("color", "#2c3e50")
                                        .set("background", "#f7f9fb").set("padding", "10px 14px")
                                        .set("border-left", "4px solid #0984e3").set("border-radius", "4px");
                        v.add(p);
                }
                return v;
        }

        private VerticalLayout construirMatrizResumenBivariada() {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.setSpacing(true);

                v.add(new H3("Matriz Resumen Bivariada con ajuste de Holm-Bonferroni"));
                v.add(new Paragraph(
                                "Los p-valores ajustados por Holm controlan el riesgo de falsos positivos "
                                                + "por comparaciones múltiples. Los cruces con p ajustado < 0.05 se consideran "
                                                + "significativos tras el control."));

                List<ServicioCruces.FilaMatrizBivariada> filas = cruces.matrizResumenTitulados();

                Grid<String[]> grid = new Grid<>();
                grid.addColumn(row -> row[0]).setHeader("N°").setAutoWidth(true);
                grid.addColumn(row -> row[1]).setHeader("Cruce").setAutoWidth(true);
                grid.addColumn(row -> row[2]).setHeader("χ²").setAutoWidth(true);
                grid.addColumn(row -> row[3]).setHeader("gl").setAutoWidth(true);
                grid.addColumn(row -> row[4]).setHeader("p original").setAutoWidth(true);
                grid.addColumn(row -> row[5]).setHeader("p (Holm)").setAutoWidth(true);
                grid.addColumn(row -> row[6]).setHeader("V de Cramér").setAutoWidth(true);
                grid.addColumn(row -> row[7]).setHeader("Interpretación").setAutoWidth(true);

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
                                                        ? "Asociación significativa (" + f.intensidad() + ")"
                                                        : "No significativo"
                        });
                }
                grid.setItems(data);
                grid.setAllRowsVisible(true);
                grid.setWidthFull();
                grid.getElement().getStyle().set("overflow-x", "auto");
                v.add(grid);

                long significativas = filas.stream()
                                .filter(ServicioCruces.FilaMatrizBivariada::significativa).count();
                v.add(new Paragraph(
                                "De los " + filas.size() + " cruces, " + significativas
                                                + " mantienen su significancia estadística tras aplicar la corrección de Holm-Bonferroni."));
                return v;
        }

        private Grid<String[]> renderTablaMultivariable(
                        ServicioAnalisis.TablaMultivariable tabla,
                        String headerX, String headerY, String headerZ, String unidadZ) {
                Grid<String[]> grid = new Grid<>();
                grid.addColumn(row -> row[0]).setHeader(headerX).setAutoWidth(true);
                grid.addColumn(row -> row[1]).setHeader(headerY).setAutoWidth(true);
                grid.addColumn(row -> row[2]).setHeader("Frecuencia (n)").setAutoWidth(true);
                grid.addColumn(row -> row[3]).setHeader(headerZ).setAutoWidth(true);

                List<String[]> filas = new ArrayList<>();
                for (ServicioAnalisis.CeldaMultivariable celda : tabla.celdas()) {
                        if (celda.n() == 0)
                                continue;
                        String z = Double.isNaN(celda.mediaZ())
                                        ? "—"
                                        : Componentes.redondear(celda.mediaZ(), 2) + unidadZ;
                        filas.add(new String[] {
                                        celda.valorX(),
                                        celda.valorY(),
                                        String.valueOf(celda.n()),
                                        z
                        });
                }
                grid.setItems(filas);
                grid.setAllRowsVisible(true);
                grid.setWidthFull();
                return grid;
        }

        private Component construirHallazgos(String texto) {
                UnorderedList ul = new UnorderedList();
                for (String linea : texto.split("\\n")) {
                        if (linea.trim().isEmpty())
                                continue;
                        String limpia = linea.trim().replaceFirst("^[•\\-]\\s*", "");
                        ul.add(new ListItem(limpia));
                }
                ul.getStyle()
                                .set("font-size", "13px")
                                .set("color", "#2c3e50")
                                .set("background", "#f7f9fb")
                                .set("padding", "10px 14px 10px 28px")
                                .set("border-left", "4px solid #0984e3")
                                .set("border-radius", "4px")
                                .set("margin", "6px 0");
                return ul;
        }

        private List<GraficoCanvas.PuntoDispersion> puntosParaGrafico(
                        ServicioAnalisis.TablaMultivariable tabla) {
                List<GraficoCanvas.PuntoDispersion> puntos = new ArrayList<>();
                for (var c : tabla.celdas()) {
                        if (c.n() == 0)
                                continue;
                        puntos.add(new GraficoCanvas.PuntoDispersion(
                                        c.valorX(), c.valorY(), c.mediaZ(), c.n()));
                }
                return puntos;
        }
}