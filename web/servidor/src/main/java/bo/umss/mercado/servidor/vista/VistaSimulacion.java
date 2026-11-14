package bo.umss.mercado.servidor.vista;

import bo.umss.mercado.servidor.datos.Simulacion;
import bo.umss.mercado.servidor.servicio.ConfiguracionSimulacion;
import bo.umss.mercado.servidor.servicio.ResultadoSimulacion;
import bo.umss.mercado.servidor.servicio.ServicioSimulacion;
import bo.umss.mercado.servidor.servicio.exportacion.ExportadorPdfSimulacion;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Route(value = "simulacion", layout = MainLayout.class)
@PageTitle("Simulación | Mercado UMSS")
public class VistaSimulacion extends VerticalLayout {

        private static final String AZUL = "#0b3d62";
        private static final String VERDE = "#27ae60";
        private static final String NARANJA = "#e17055";
        private static final String MORADO = "#6c5ce7";

        private final ServicioSimulacion simulacion;
        private final ExportadorPdfSimulacion pdfExporter;

        private ComboBox<String> cmbVariable = new ComboBox<>("Variable a analizar");
        private ComboBox<String> cmbCategoria = new ComboBox<>("Categoría objetivo");
        private NumberField numProbInteresados = new NumberField("Probabilidad Interesados Directos (%)");
        private NumberField numProbResto = new NumberField("Probabilidad del Resto (%)");
        private NumberField numIteraciones = new NumberField("Iteraciones");
        private NumberField numSemilla = new NumberField("Semilla (Auditoría)");

        private VerticalLayout zonaCajaCristal = new VerticalLayout();
        private VerticalLayout zonaResultados = new VerticalLayout();
        private Grid<Simulacion> tablaHistorial = new Grid<>(Simulacion.class, false);

        private ResultadoSimulacion ultimoResultado;
        private ConfiguracionSimulacion ultimaConfiguracion;
        private boolean usarEmpleadores = false;
        private String nombreEscenarioActual = "Personalizada";

        public VistaSimulacion(ServicioSimulacion simulacion, ExportadorPdfSimulacion pdfExporter) {
                this.simulacion = simulacion;
                this.pdfExporter = pdfExporter;
                inyectarCss();
                setPadding(true);
                setSpacing(true);
                getStyle().set("background", "#f8fafc").set("min-height", "100vh");

                add(construirHeader());
                add(construirEscenariosPredefinidos());

                HorizontalLayout panelTrabajo = new HorizontalLayout();
                panelTrabajo.setWidthFull();
                panelTrabajo.getStyle().set("gap", "24px").set("align-items", "flex-start").set("flex-wrap", "wrap");

                panelTrabajo.add(construirPanelConfiguracion());
                panelTrabajo.add(construirPanelCajaCristal());

                add(panelTrabajo);
                add(zonaResultados);
                add(zonaComparacion);

                add(new H3("Historial de Simulaciones (Auditoría)"));
                add(construirHistorial());

                configurarEventos();
        }

        private VerticalLayout zonaComparacion = new VerticalLayout();
        private Grid<String[]> tablaComparacion = new Grid<>();

        private void configurarEventos() {
                cmbVariable.setItems(
                                "area_interes_posgrado",
                                "interes_posgrado",
                                "realizo_formacion_complementaria",
                                "posibilidad_futura_contratacion",
                                "financiamiento_formacion",
                                "situacion_laboral_actual",
                                "institucion_futura_posgrado",
                                "departamento");

                cmbVariable.addValueChangeListener(e -> {
                        String var = e.getValue();
                        cmbCategoria.clear();
                        if (var == null)
                                return;

                        if (var.equals("area_interes_posgrado")) {
                                cmbCategoria.setItems("IA", "ING_SOFTWARE", "REDES_SEGURIDAD", "BASE_DATOS", "ROBOTICA",
                                                "OTRAS");
                                usarEmpleadores = false;
                        } else if (var.equals("interes_posgrado")) {
                                cmbCategoria.setItems("SI", "NO");
                                usarEmpleadores = false;
                        } else if (var.equals("realizo_formacion_complementaria")) {
                                cmbCategoria.setItems("SI", "NO");
                                usarEmpleadores = false;
                        } else if (var.equals("posibilidad_futura_contratacion")) {
                                cmbCategoria.setItems("ALTA_PROBABILIDAD", "DEPENDE_CRECIMIENTO", "NO_PREVISTA",
                                                "NO_REQUIERE");
                                usarEmpleadores = true;
                        } else if (var.equals("financiamiento_formacion")) {
                                cmbCategoria.setItems("RECURSOS_PROPIOS", "BECA_TOTAL", "BECA_PARCIAL");
                                usarEmpleadores = false;
                        } else if (var.equals("situacion_laboral_actual")) {
                                cmbCategoria.setItems("TRABAJA_ORG", "NO_TRABAJA", "EMPRENDIMIENTO");
                                usarEmpleadores = false;
                        } else if (var.equals("institucion_futura_posgrado")) {
                                cmbCategoria.setItems("UNIV_INTERNACIONALES", "UNIV_PRIVADAS_NAC", "UNIV_PUBLICAS_NAC",
                                                "UMSS");
                                usarEmpleadores = false;
                        } else if (var.equals("departamento")) {
                                cmbCategoria.setItems("EXTRANJERO", "SANTA_CRUZ", "LA_PAZ", "COCHABAMBA", "ORURO");
                                usarEmpleadores = false;
                        }
                });

                cmbCategoria.addValueChangeListener(e -> {
                        String var = cmbVariable.getValue();
                        String cat = e.getValue();
                        if (var == null || cat == null)
                                return;

                        double p = simulacion.calcularProporcion(var, cat, usarEmpleadores);
                        double probInt = simulacion.obtenerProbabilidadInteresados(var, cat);

                        numProbInteresados.setValue(probInt * 100);
                        numProbResto.setValue(0.2 * p * 100);
                });
        }

        private Div construirHeader() {
                Div header = new Div();
                header.getStyle()
                                .set("background", AZUL)
                                .set("padding", "28px 36px")
                                .set("border-radius", "16px")
                                .set("color", "white")
                                .set("margin-bottom", "16px")
                                .set("box-shadow", "0 6px 20px rgba(11,61,98,.2)");

                Div titulo = new Div();
                titulo.setText("Simulación Monte Carlo");
                titulo.getStyle().set("font-size", "26px").set("font-weight", "700");

                Div sub = new Div();
                sub.setText("Proyección de demanda estocástica con análisis de sensibilidad");
                sub.getStyle().set("font-size", "13px").set("opacity", "0.85");

                header.add(titulo, sub);
                return header;
        }

        private HorizontalLayout construirEscenariosPredefinidos() {
                HorizontalLayout fila1 = new HorizontalLayout();
                fila1.getStyle().set("gap", "12px").set("flex-wrap", "wrap").set("margin-bottom", "8px");

                Button btnDemanda = new Button("Demanda Posgrado");
                Button btnContratacion = new Button("Contratación Empleadores");
                Button btnBecas = new Button("Impacto de Becas");
                Button btnVirtual = new Button("Modalidad Virtual");

                btnDemanda.getStyle().set("background", MORADO).set("color", "white")
                                .set("font-weight", "600").set("border-radius", "8px")
                                .set("cursor", "pointer").set("transition", "all 0.3s");
                btnContratacion.getStyle().set("background", VERDE).set("color", "white")
                                .set("font-weight", "600").set("border-radius", "8px")
                                .set("cursor", "pointer").set("transition", "all 0.3s");
                btnBecas.getStyle().set("background", NARANJA).set("color", "white")
                                .set("font-weight", "600").set("border-radius", "8px")
                                .set("cursor", "pointer").set("transition", "all 0.3s");
                btnVirtual.getStyle().set("background", AZUL).set("color", "white")
                                .set("font-weight", "600").set("border-radius", "8px")
                                .set("cursor", "pointer").set("transition", "all 0.3s");

                btnDemanda.addClickListener(e -> cargarEscenario(simulacion.configEscenarioDemanda()));
                btnContratacion.addClickListener(e -> cargarEscenario(simulacion.configEscenarioContratacion()));
                btnBecas.addClickListener(e -> cargarEscenario(simulacion.configEscenarioBecas()));
                btnVirtual.addClickListener(e -> cargarEscenario(simulacion.configEscenarioVirtual()));

                fila1.add(btnDemanda, btnContratacion, btnBecas, btnVirtual);

                HorizontalLayout fila2 = new HorizontalLayout();
                fila2.getStyle().set("gap", "12px").set("flex-wrap", "wrap").set("margin-bottom", "16px");

                Button btnFidelizacion = new Button("Fidelización vs Competencia");
                Button btnExpansion = new Button("Expansión Departamental");

                btnFidelizacion.getStyle().set("background", "#2980b9").set("color", "white")
                                .set("font-weight", "600").set("border-radius", "8px")
                                .set("cursor", "pointer").set("transition", "all 0.3s");
                btnExpansion.getStyle().set("background", "#8e44ad").set("color", "white")
                                .set("font-weight", "600").set("border-radius", "8px")
                                .set("cursor", "pointer").set("transition", "all 0.3s");

                btnFidelizacion.addClickListener(e -> cargarEscenario(simulacion.configEscenarioFidelizacion()));
                btnExpansion.addClickListener(e -> cargarEscenario(simulacion.configEscenarioExpansion()));

                fila2.add(btnFidelizacion, btnExpansion);

                VerticalLayout contenedor = new VerticalLayout(fila1, fila2);
                contenedor.setPadding(false);
                contenedor.setSpacing(false);
                HorizontalLayout wrapper = new HorizontalLayout(contenedor);
                wrapper.setWidthFull();
                return wrapper;
        }

        private void cargarEscenario(ConfiguracionSimulacion config) {
                cmbVariable.setValue(config.nombreVariable());
                cmbCategoria.setValue(config.categoriaObjetivo());
                numProbInteresados.setValue(config.probabilidadInteresados() * 100);
                numProbResto.setValue(config.probabilidadResto() * 100);
                numIteraciones.setValue((double) config.iteraciones());
                numSemilla.setValue((double) config.semilla());
                usarEmpleadores = config.usarEmpleadores();
                nombreEscenarioActual = config.nombreEscenario();
                ejecutarDesdeUI(usarEmpleadores);
        }

        private VerticalLayout construirPanelConfiguracion() {
                VerticalLayout panel = new VerticalLayout();
                panel.setPadding(true);
                panel.setSpacing(true);
                panel.getStyle().set("background", "white").set("border-radius", "12px")
                                .set("box-shadow", "0 2px 8px rgba(0,0,0,.05)").set("width", "100%")
                                .set("max-width", "450px");

                panel.add(new H3("Configurador del Motor"));

                cmbVariable.setWidthFull();
                cmbCategoria.setWidthFull();

                numProbInteresados.setWidthFull();
                numProbInteresados.setValue(80.0);
                numProbInteresados.setMin(0);
                numProbInteresados.setMax(100);
                numProbInteresados.setStep(1);
                numProbInteresados.setSuffixComponent(new Span("%"));

                numProbResto.setWidthFull();
                numProbResto.setValue(8.78);
                numProbResto.setMin(0);
                numProbResto.setMax(100);
                numProbResto.setStep(0.1);
                numProbResto.setSuffixComponent(new Span("%"));

                Paragraph nota = new Paragraph(
                                "Sugerencia metodológica: Para el Escenario Base, use 80% para interesados directos y (0.2 * Proporción Empírica) para el resto.");
                nota.getStyle().set("font-size", "11px").set("color", "#666").set("font-style", "italic");

                numIteraciones.setValue(1000.0);
                numIteraciones.setWidthFull();

                numSemilla.setValue(12345.0);
                numSemilla.setWidthFull();

                Button ejecutar = new Button("Ejecutar Simulación Personalizada");
                ejecutar.getStyle().set("background", AZUL).set("color", "white")
                                .set("width", "100%").set("font-weight", "bold").set("margin-top", "16px")
                                .set("cursor", "pointer");
                ejecutar.addClickListener(e -> ejecutarDesdeUI(usarEmpleadores));

                Button btnComparar = new Button("Comparar los 6 Escenarios");
                btnComparar.getStyle().set("background", NARANJA).set("color", "white")
                                .set("width", "100%").set("font-weight", "bold").set("margin-top", "8px")
                                .set("cursor", "pointer");
                btnComparar.addClickListener(e -> compararEscenarios());

                panel.add(cmbVariable, cmbCategoria, numProbInteresados, numProbResto, nota, numIteraciones, numSemilla,
                                ejecutar, btnComparar);
                return panel;
        }

        private VerticalLayout construirPanelCajaCristal() {
                zonaCajaCristal.setPadding(true);
                zonaCajaCristal.setSpacing(true);
                zonaCajaCristal.getStyle().set("background", "white").set("border-radius", "12px")
                                .set("box-shadow", "0 2px 8px rgba(0,0,0,.05)").set("flex-grow", "1")
                                .set("min-width", "400px");

                zonaCajaCristal.add(new H3("Paso a Paso"));
                Div placeholder = new Div();
                placeholder.setText("Configura los parámetros y presiona 'Ejecutar' para ver el proceso.");
                placeholder.getStyle().set("color", "#666").set("font-style", "italic");
                zonaCajaCristal.add(placeholder);

                return zonaCajaCristal;
        }

        private void ejecutarDesdeUI(boolean usarEmpleadores) {
                try {
                        if (cmbVariable.isEmpty() || cmbCategoria.isEmpty()) {
                                Notification.show("Selecciona variable y categoría.");
                                return;
                        }

                        ConfiguracionSimulacion config = new ConfiguracionSimulacion(
                                        nombreEscenarioActual, cmbVariable.getValue(), cmbCategoria.getValue(),
                                        numProbInteresados.getValue() / 100.0, numProbResto.getValue() / 100.0,
                                        numIteraciones.getValue().intValue(), numSemilla.getValue().longValue(),
                                        usarEmpleadores);

                        ultimoResultado = simulacion.ejecutarSimulacion(config);

                        actualizarCajaCristal(config, ultimoResultado);
                        actualizarResultados(config, ultimoResultado);
                        refrescarHistorial();
                        zonaComparacion.removeAll();
                        Notification.show("Simulación ejecutada y guardada en el historial.");

                } catch (Exception ex) {
                        Notification.show("Error: " + ex.getMessage());
                }
        }

        private void compararEscenarios() {
                zonaComparacion.removeAll();
                zonaResultados.removeAll();
                zonaCajaCristal.removeAll();

                List<ConfiguracionSimulacion> escenarios = List.of(
                                simulacion.configEscenarioDemanda(),
                                simulacion.configEscenarioContratacion(),
                                simulacion.configEscenarioBecas(),
                                simulacion.configEscenarioVirtual(),
                                simulacion.configEscenarioFidelizacion(),
                                simulacion.configEscenarioExpansion());

                List<String[]> data = new ArrayList<>();
                for (ConfiguracionSimulacion c : escenarios) {
                        ResultadoSimulacion r = simulacion.ejecutarSimulacion(c);
                        String veredicto = r.media() >= 30 ? "Alta Demanda"
                                        : r.media() >= 10 ? "Demanda Moderada" : "Baja Demanda";
                        data.add(new String[] {
                                        c.nombreEscenario(),
                                        c.nombreVariable() + " = " + c.categoriaObjetivo(),
                                        String.format(Locale.US, "%.1f%%", c.probabilidadInteresados() * 100),
                                        String.format(Locale.US, "%.1f", r.media()),
                                        String.format(Locale.US, "[%.1f, %.1f]", r.icInferior(), r.icSuperior()),
                                        veredicto
                        });
                }

                Grid<String[]> grid = new Grid<>();
                grid.addColumn(r -> r[0]).setHeader("Escenario").setAutoWidth(true);
                grid.addColumn(r -> r[1]).setHeader("Variable / Categoría").setAutoWidth(true);
                grid.addColumn(r -> r[2]).setHeader("Prob. Base").setAutoWidth(true);
                grid.addColumn(r -> r[3]).setHeader("Media Proyectada").setAutoWidth(true);
                grid.addColumn(r -> r[4]).setHeader("IC 95% (Predicción)").setAutoWidth(true);
                grid.addColumn(r -> r[5]).setHeader("Veredicto").setAutoWidth(true);
                grid.setItems(data);
                grid.setAllRowsVisible(true);
                grid.setWidthFull();

                zonaComparacion.add(new H3("Tabla Comparativa de los 6 Escenarios"));
                zonaComparacion.add(grid);
                refrescarHistorial();
                Notification.show("Comparación de 6 escenarios completada.");
        }

        private void actualizarCajaCristal(ConfiguracionSimulacion config, ResultadoSimulacion res) {
                zonaCajaCristal.removeAll();
                zonaCajaCristal.add(new H3("Paso a Paso"));

                zonaCajaCristal.add(crearTarjetaPaso(1, "Proporción Empírica (p)",
                                String.format(Locale.US,
                                                "Se identificaron %d interesados en '%s' de un total de %d encuestados.\nFórmula: %d / %d = %.2f%%",
                                                res.cantidadInteresados(), config.categoriaObjetivo(),
                                                res.poblacionTotal(),
                                                res.cantidadInteresados(), res.poblacionTotal(),
                                                res.proporcionEmpirica() * 100),
                                AZUL));

                zonaCajaCristal.add(crearTarjetaPaso(2, "Probabilidades Definidas",
                                String.format(Locale.US,
                                                "- Interesados directos: %.1f%%\n- Resto de la población: %.2f%%",
                                                config.probabilidadInteresados() * 100,
                                                config.probabilidadResto() * 100),
                                NARANJA));

                StringBuilder sbIter = new StringBuilder();
                for (int i = 0; i < res.primerasIteraciones().size(); i++) {
                        sbIter.append("Iteración ").append(i + 1).append(": ").append(res.primerasIteraciones().get(i))
                                        .append(" casos.\n");
                }
                zonaCajaCristal.add(crearTarjetaPaso(3, "Ejecución de las Iteraciones",
                                String.format(Locale.US, "El motor ejecutó %d vueltas. Primeros resultados:\n%s",
                                                config.iteraciones(), sbIter.toString()),
                                VERDE));

                zonaCajaCristal.add(crearTarjetaPaso(4, "Consolidación de Resultados",
                                "Se calculan los estadísticos con los " + config.iteraciones()
                                                + " resultados y se genera el gráfico.",
                                MORADO));
        }

        private Div crearTarjetaPaso(int numero, String titulo, String contenido, String color) {
                Div card = new Div();
                card.getStyle().set("background", "white").set("border-radius", "8px")
                                .set("padding", "16px").set("border-left", "4px solid " + color)
                                .set("box-shadow", "0 2px 6px rgba(0,0,0,.05)").set("margin-bottom", "12px");

                Div header = new Div();
                header.setText("Paso " + numero + ": " + titulo);
                header.getStyle().set("font-weight", "700").set("color", color).set("margin-bottom", "8px");

                Div body = new Div();
                body.setText(contenido);
                body.getStyle().set("white-space", "pre-wrap").set("font-size", "13px").set("color", "#444");

                card.add(header, body);
                return card;
        }

        private void actualizarResultados(ConfiguracionSimulacion config, ResultadoSimulacion res) {
                zonaResultados.removeAll();

                HorizontalLayout kpis = Componentes.filaKpis(
                                Componentes.tarjetaKpi("Media Proyectada",
                                                String.format(Locale.US, "%.1f", res.media()),
                                                "alumnos/puestos"),
                                Componentes.tarjetaKpi("Desviación Estándar",
                                                String.format(Locale.US, "%.2f", res.desviacionEstandar()),
                                                "variabilidad"),
                                Componentes.tarjetaKpi("IC 95% (Predicción)",
                                                String.format(Locale.US, "[%.1f, %.1f]", res.icInferior(),
                                                                res.icSuperior()),
                                                "rango probable"));
                zonaResultados.add(kpis);

                HorizontalLayout graficos = new HorizontalLayout();
                graficos.setWidthFull();
                graficos.getStyle().set("gap", "16px").set("flex-wrap", "wrap");

                Map<Integer, Integer> hist = res.histograma();
                int[] vals = hist.values().stream().mapToInt(Integer::intValue).toArray();
                String[] etiq = hist.keySet().stream().map(String::valueOf).toArray(String[]::new);
                Component gHist = GraficoCanvas.enTarjeta(
                                GraficoCanvas.barrasVerticales("histSim", vals, etiq, VERDE,
                                                "Distribución de Resultados"),
                                "Histograma de Frecuencias (Iteraciones)");

                Component gTornado = GraficoCanvas.enTarjeta(
                                GraficoCanvas.barrasVerticales("tornadoSim",
                                                new int[] { (int) Math.round(res.mediaPesimista()),
                                                                (int) Math.round(res.mediaBase()),
                                                                (int) Math.round(res.mediaOptimista()) },
                                                new String[] { "Pesimista (-30%)", "Base", "Optimista (+15%)" },
                                                NARANJA, "Análisis de Sensibilidad (Tornado)"),
                                "Tornado de Escenarios");

                graficos.add(gHist, gTornado);
                zonaResultados.add(graficos);

                Div interpretacion = new Div();
                interpretacion.getElement().setProperty("innerHTML", String.format(Locale.US,
                                "<b>Interpretación Automática:</b><br/>El análisis revela que, con un 95%% de confianza (Rango de Predicción), la demanda esperada se sitúa entre <b>%.1f</b> y <b>%.1f</b>, con una media de <b>%.1f</b>. "
                                                +
                                                "Incluso en el escenario pesimista, la demanda se mantiene en <b>%.1f</b>.",
                                res.icInferior(), res.icSuperior(), res.media(), res.mediaPesimista()));
                interpretacion.getStyle().set("background", "#fdf1ee").set("padding", "16px")
                                .set("border-left", "4px solid " + NARANJA).set("border-radius", "6px")
                                .set("margin-top", "16px");
                zonaResultados.add(interpretacion);

                StreamResource resource = new StreamResource(
                                "Simulacion_" + config.categoriaObjetivo() + "_" + LocalDate.now() + ".pdf",
                                () -> new java.io.ByteArrayInputStream(pdfExporter.generarPdf(config, res)));

                Anchor linkPdf = new Anchor(resource, "Exportar Reporte a PDF");
                linkPdf.getElement().setAttribute("download", true);
                linkPdf.getStyle().set("margin-top", "16px").set("display", "inline-block")
                                .set("background", AZUL).set("color", "white").set("padding", "10px 20px")
                                .set("border-radius", "8px").set("text-decoration", "none").set("font-weight", "bold");
                zonaResultados.add(linkPdf);
        }

        private Grid<Simulacion> construirHistorial() {
                tablaHistorial = new Grid<>(Simulacion.class, false);
                tablaHistorial.addColumn(Simulacion::getFechaHora).setHeader("Fecha").setAutoWidth(true);
                tablaHistorial.addColumn(Simulacion::getEscenario).setHeader("Escenario").setAutoWidth(true);
                tablaHistorial.addColumn(s -> String.format(Locale.US, "%.2f", s.getDemandaMedia())).setHeader("Media")
                                .setAutoWidth(true);
                tablaHistorial.addColumn(Simulacion::getSemilla).setHeader("Semilla").setAutoWidth(true);
                tablaHistorial.setWidthFull();
                refrescarHistorial();
                return tablaHistorial;
        }

        private void refrescarHistorial() {
                tablaHistorial.setItems(simulacion.historial());
        }

        private void inyectarCss() {
                String css = """
                                @keyframes fadeInUp { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
                                div { animation: fadeInUp 0.4s ease both; }
                                """;
                getElement().executeJs(
                                "const s=document.createElement('style'); s.textContent=$0; document.head.appendChild(s);",
                                css);
        }
}