package bo.umss.mercado.servidor.vista;

import bo.umss.mercado.nucleo.limpieza.LimpiezaDatos;
import bo.umss.mercado.servidor.datos.RepositorioEmpleadores;
import bo.umss.mercado.servidor.datos.RepositorioTitulados;
import bo.umss.mercado.servidor.servicio.ServicioAnalisis;
import bo.umss.mercado.servidor.servicio.ServicioRecomendaciones;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Panel | Mercado UMSS")
public class VistaPanel extends VerticalLayout {

        // Paleta (colores sólidos, sin degradados)
        private static final String AZUL = "#0b3d62";
        private static final String AZUL2 = "#0984e3";
        private static final String VERDE = "#27ae60";
        private static final String NARANJA = "#e17055";
        private static final String MORADO = "#6c5ce7";
        private static final String GRIS = "#7f8c8d";

        // Fondos muy suaves (sólidos, no degradados)
        private static final String BG_AZUL = "#e8f2fc";
        private static final String BG_VERDE = "#e8f5e9";
        private static final String BG_NARANJA = "#fdf1ee";
        private static final String BG_MORADO = "#f0ebfc";

        // SVG paths (Material Design Icons 24x24)
        private static final String SVG_USERS = "M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z";
        private static final String SVG_BUILDING = "M12 7V3H2v18h20V7H12zM6 19H4v-2h2v2zm0-4H4v-2h2v2zm0-4H4V9h2v2zm0-4H4V5h2v2zm4 12H8v-2h2v2zm0-4H8v-2h2v2zm0-4H8V9h2v2zm0-4H8V5h2v2zm10 12h-8v-2h2v-2h-2v-2h2v-2h-2V9h8v10zm-2-8h-2v2h2v-2zm0 4h-2v2h2v-2z";
        private static final String SVG_CHART = "M5 9.2h3V19H5zM10.6 5h2.8v14h-2.8zm5.6 8H19v6h-2.8z";
        private static final String SVG_LIGHTBULB = "M9 21c0 .55.45 1 1 1h4c.55 0 1-.45 1-1v-1H9v1zm3-19C8.14 2 5 5.14 5 9c0 2.38 1.19 4.47 3 5.74V17c0 .55.45 1 1 1h6c.55 0 1-.45 1-1v-2.26c1.81-1.27 3-3.36 3-5.74 0-3.86-3.14-7-7-7z";
        private static final String SVG_TARGET = "M12 8c-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4zm8.94 3c-.46-4.17-3.77-7.48-7.94-7.94V1h-2v2.06C6.83 3.52 3.52 6.83 3.06 11H1v2h2.06c.46 4.17 3.77 7.48 7.94 7.94V23h2v-2.06c4.17-.46 7.48-3.77 7.94-7.94H23v-2h-2.06zM12 19c-3.87 0-7-3.13-7-7s3.13-7 7-7 7 3.13 7 7-3.13 7-7 7z";
        private static final String SVG_ROCKET = "M12 2.5s-4 3.5-4 9.5c0 2.5 1 5 1 5H5l-1 3 4-1c1.5 1.5 4 2.5 4 2.5s2.5-1 4-2.5l4 1-1-3h-4s1-2.5 1-5c0-6-4-9.5-4-9.5zm0 12c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2z";
        private static final String SVG_PIN = "M16 12V4h1V2H7v2h1v8l-2 2v2h5.2v6h1.6v-6H18v-2l-2-2z";
        private static final String SVG_GRADUATION = "M5 13.18v4L12 21l7-3.82v-4L12 17l-7-3.82zM12 3 1 9l11 6 9-4.91V17h2V9L12 3z";
        private static final String SVG_MONEY = "M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z";
        private static final String SVG_CLOCK = "M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z";
        private static final String SVG_CHECK = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z";
        private static final String SVG_TRENDING = "M16 6l2.29 2.29-4.88 4.88-4-4L2 16.59 3.41 18l6-6 4 4 6.3-6.29L22 12V6z";
        private static final String SVG_ROBOT = "M20 9V7c0-1.1-.9-2-2-2h-3c0-1.66-1.34-3-3-3S9 3.34 9 5H6c-1.1 0-2 .9-2 2v2c-1.66 0-3 1.34-3 3s1.34 3 3 3v4c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2v-4c1.66 0 3-1.34 3-3s-1.34-3-3-3zM7.5 11.5c0-.83.67-1.5 1.5-1.5s1.5.67 1.5 1.5S9.83 13 9 13s-1.5-.67-1.5-1.5zM16 17H8v-2h8v2zm-1-4c-.83 0-1.5-.67-1.5-1.5S14.17 10 15 10s1.5.67 1.5 1.5S15.83 13 15 13z";
        private static final String SVG_STAR = "M12 17.27 18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z";
        private static final String SVG_BRIEFCASE = "M20 6h-4V4c0-1.11-.89-2-2-2h-4c-1.11 0-2 .89-2 2v2H4c-1.11 0-1.99.89-1.99 2L2 19c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V8c0-1.11-.89-2-2-2zm-6 0h-4V4h4v2z";
        private static final String SVG_BILL = "M19.5 3.5L18 2l-1.5 1.5L15 2l-1.5 1.5L12 2l-1.5 1.5L9 2 7.5 3.5 6 2 4.5 3.5 3 2v20l1.5-1.5L6 22l1.5-1.5L9 22l1.5-1.5L12 22l1.5-1.5L15 22l1.5-1.5L18 22l1.5-1.5L21 22V2l-1.5 1.5zM19 19.09H5V4.91h14v14.18zM6 15h12v2H6zm0-4h12v2H6zm0-4h12v2H6z";
        private static final String SVG_EDIT = "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z";
        private static final String SVG_DOWNLOAD = "M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z";
        private static final String SVG_UPLOAD = "M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM14 13v4h-4v-4H7l5-5 5 5h-3z";

        public VistaPanel(ServicioAnalisis analisis,
                        ServicioRecomendaciones recomendaciones,
                        RepositorioTitulados repoTitulados,
                        RepositorioEmpleadores repoEmpleadores) {

                inyectarCss();
                setPadding(true);
                setSpacing(true);
                getStyle().set("background", "#f8fafc").set("min-height", "100vh");

                add(construirHeader(repoTitulados.count(), repoEmpleadores.count()));

                add(construirTituloSeccion(SVG_USERS, "Titulados (N = " + repoTitulados.count() + ")",
                                "Indicadores clave de los titulados de Ingeniería de Sistemas"));
                add(construirFilaKpisTitulados(analisis));

                add(construirTituloSeccion(SVG_BUILDING, "Empleadores (M = " + repoEmpleadores.count() + ")",
                                "Indicadores clave de las organizaciones empleadoras"));
                add(construirFilaKpisEmpleadores(analisis));

                add(construirTituloSeccion(SVG_CHART, "Visualización rápida",
                                "Comparación visual de los principales indicadores"));
                add(construirGraficosResumen(analisis));

                add(construirTituloSeccion(SVG_LIGHTBULB, "Hallazgos clave",
                                "Insights derivados de los datos recolectados"));
                add(construirHallazgos(analisis));

                add(construirTituloSeccion(SVG_TARGET, "Recomendaciones automáticas",
                                "Líneas de acción sugeridas por las reglas if-then del sistema"));
                add(construirRecomendaciones(recomendaciones));

                add(construirTituloSeccion(SVG_ROCKET, "Ir a",
                                "Accesos directos a las pantallas de análisis detallado"));
                add(construirAtajos());

                // Animaciones de entrada (fade-in escalonado)
                getElement().executeJs(
                                "setTimeout(() => {"
                                                + "  document.querySelectorAll('.panel-kpi').forEach((el, i) => {"
                                                + "    el.style.animationDelay = (i * 100) + 'ms';"
                                                + "    el.classList.add('panel-animate-in');"
                                                + "  });"
                                                + "  document.querySelectorAll('.panel-grafico').forEach((el, i) => {"
                                                + "    el.style.animationDelay = (300 + i * 120) + 'ms';"
                                                + "    el.classList.add('panel-animate-in');"
                                                + "  });"
                                                + "  document.querySelectorAll('.panel-hallazgo').forEach((el, i) => {"
                                                + "    el.style.animationDelay = (200 + i * 150) + 'ms';"
                                                + "    el.classList.add('panel-animate-in');"
                                                + "  });"
                                                + "}, 100);");

                // Progress bars
                getElement().executeJs(
                                "setTimeout(() => {"
                                                + "  document.querySelectorAll('.panel-progress-bar[data-target]').forEach(bar => {"
                                                + "    const target = bar.getAttribute('data-target');"
                                                + "    setTimeout(() => { bar.style.width = target + '%'; }, 200);"
                                                + "  });"
                                                + "}, 300);");
        }

        // ================================================================
        // HEADER (color sólido + círculo decorativo sutil)
        // ================================================================
        private Div construirHeader(long n, long m) {
                Div header = new Div();
                header.getStyle()
                                .set("background", AZUL)
                                .set("padding", "32px 40px")
                                .set("border-radius", "18px")
                                .set("color", "white")
                                .set("margin-bottom", "16px")
                                .set("box-shadow", "0 8px 24px rgba(11,61,98,.2)")
                                .set("position", "relative")
                                .set("overflow", "hidden");

                Div titulo = new Div();
                titulo.setText("Panel de Mercado Laboral");
                titulo.getStyle()
                                .set("font-size", "30px")
                                .set("font-weight", "700")
                                .set("letter-spacing", "-0.3px")
                                .set("margin-bottom", "6px")
                                .set("position", "relative")
                                .set("z-index", "1");

                Div subtitulo = new Div();
                subtitulo.setText("Ingeniería de Sistemas · UMSS · Análisis de titulados y empleadores");
                subtitulo.getStyle()
                                .set("font-size", "14px")
                                .set("opacity", "0.85")
                                .set("margin-bottom", "20px")
                                .set("position", "relative")
                                .set("z-index", "1");

                HorizontalLayout stats = new HorizontalLayout();
                stats.getStyle().set("gap", "12px").set("flex-wrap", "wrap")
                                .set("position", "relative").set("z-index", "1");
                stats.add(chipHeader(SVG_USERS, n + " titulados"),
                                chipHeader(SVG_BUILDING, m + " empleadores"),
                                chipHeader(SVG_CLOCK, "Abril - Octubre 2026"),
                                chipHeader(SVG_CHECK, "Base consolidada"));

                // Círculos decorativos sutiles (color sólido con opacidad, no gradiente)
                Div circ1 = new Div();
                circ1.getStyle()
                                .set("position", "absolute")
                                .set("top", "-60px")
                                .set("right", "-60px")
                                .set("width", "220px")
                                .set("height", "220px")
                                .set("background", "rgba(255,255,255,0.04)")
                                .set("border-radius", "50%")
                                .set("z-index", "0");

                Div circ2 = new Div();
                circ2.getStyle()
                                .set("position", "absolute")
                                .set("bottom", "-80px")
                                .set("right", "100px")
                                .set("width", "180px")
                                .set("height", "180px")
                                .set("background", "rgba(255,255,255,0.03)")
                                .set("border-radius", "50%")
                                .set("z-index", "0");

                header.add(circ1, circ2, titulo, subtitulo, stats);
                return header;
        }

        private Div chipHeader(String svg, String texto) {
                Div chip = new Div();
                chip.getStyle()
                                .set("background", "rgba(255,255,255,0.12)")
                                .set("padding", "8px 16px")
                                .set("border-radius", "24px")
                                .set("font-size", "13px")
                                .set("font-weight", "600")
                                .set("border", "1px solid rgba(255,255,255,0.2)")
                                .set("transition", "all 0.25s ease");
                chip.getElement().executeJs(
                                "this.innerHTML = $0",
                                "<span style='display:inline-flex;align-items:center;gap:8px;'>"
                                                + svgIcon(svg, "#ffffff", 14)
                                                + "<span>" + escapar(texto) + "</span></span>");
                chip.getElement().executeJs(
                                "this.addEventListener('mouseenter', () => {"
                                                + "  this.style.background = 'rgba(255,255,255,0.22)';"
                                                + "  this.style.transform = 'translateY(-2px)';"
                                                + "});"
                                                + "this.addEventListener('mouseleave', () => {"
                                                + "  this.style.background = 'rgba(255,255,255,0.12)';"
                                                + "  this.style.transform = 'translateY(0)';"
                                                + "});");
                return chip;
        }

        // ================================================================
        // Título de sección con ícono SVG + color sólido
        // ================================================================
        private Div construirTituloSeccion(String svgPath, String titulo, String subtitulo) {
                Div wrapper = new Div();
                wrapper.getStyle().set("margin-top", "20px").set("margin-bottom", "12px");

                Div row = new Div();
                row.getStyle().set("display", "flex").set("align-items", "center")
                                .set("gap", "14px").set("margin-bottom", "4px");

                Div icono = new Div();
                icono.getStyle()
                                .set("width", "46px").set("height", "46px")
                                .set("background", AZUL2)
                                .set("border-radius", "10px")
                                .set("display", "flex").set("align-items", "center")
                                .set("justify-content", "center")
                                .set("box-shadow", "0 3px 10px rgba(9,132,227,.25)")
                                .set("flex-shrink", "0");
                icono.getElement().executeJs("this.innerHTML = $0", svgIcon(svgPath, "#ffffff", 24));

                Div textos = new Div();
                Div t = new Div();
                t.setText(titulo);
                t.getStyle().set("font-size", "20px").set("font-weight", "700").set("color", AZUL);
                Div s = new Div();
                s.setText(subtitulo);
                s.getStyle().set("font-size", "13px").set("color", "#666").set("margin-top", "2px");
                textos.add(t, s);

                row.add(icono, textos);

                Div linea = new Div();
                linea.getStyle()
                                .set("height", "2px")
                                .set("background", "#e0e8f0")
                                .set("border-radius", "1px")
                                .set("margin-top", "12px");

                wrapper.add(row, linea);
                return wrapper;
        }

        // ================================================================
        // KPIs Titulados
        // ================================================================
        private HorizontalLayout construirFilaKpisTitulados(ServicioAnalisis analisis) {
                List<Map<String, String>> m = analisis.mapasTitulados();

                double insercion = analisis.tasaInsercion();
                double interes = analisis.intencionPosgrado();
                double recursosPropios = analisis.proporcionDonde(m, "financiamiento_formacion",
                                v -> LimpiezaDatos.normalizar(v).contains("PROPIO"));
                double antiguedad = analisis.resumenNumerico("anios_vida_profesional").media();

                HorizontalLayout fila = new HorizontalLayout(
                                tarjetaKpi(SVG_PIN, "Tasa de inserción",
                                                pct(insercion), "Trabajan o emprenden",
                                                insercion * 100, AZUL2, BG_AZUL),
                                tarjetaKpi(SVG_GRADUATION, "Interesados en posgrado",
                                                pct(interes), "Demanda potencial",
                                                interes * 100, VERDE, BG_VERDE),
                                tarjetaKpi(SVG_MONEY, "Financiaron con recursos propios",
                                                pct(recursosPropios), "Dependencia económica",
                                                recursosPropios * 100, NARANJA, BG_NARANJA),
                                tarjetaKpi(SVG_CLOCK, "Antigüedad media",
                                                Double.isNaN(antiguedad) ? "n/d"
                                                                : String.format(Locale.US, "%.2f años", antiguedad),
                                                "Años de vida profesional", -1, MORADO, BG_MORADO));
                fila.setWidthFull();
                fila.getStyle().set("gap", "16px").set("flex-wrap", "wrap");
                return fila;
        }

        // ================================================================
        // KPIs Empleadores
        // ================================================================
        private HorizontalLayout construirFilaKpisEmpleadores(ServicioAnalisis analisis) {
                List<Map<String, String>> maps = analisis.mapasEmpleadores();

                double contrato5a = analisis.proporcionDonde(maps, "contrato_ultimos_5_anios",
                                v -> v.equals("SI"));
                double altaProyeccion = analisis.proporcionDonde(maps, "posibilidad_futura_contratacion",
                                v -> v.equals("ALTA_PROBABILIDAD"));
                double demandaIA = analisis.proporcionDemandaIA();
                double dimA = analisis.mediasPorDimension("A").stream()
                                .mapToDouble(d -> d).average().orElse(Double.NaN);

                HorizontalLayout fila = new HorizontalLayout(
                                tarjetaKpi(SVG_CHECK, "Contratación (últimos 5 años)",
                                                pct(contrato5a), "Sí contrataron",
                                                contrato5a * 100, VERDE, BG_VERDE),
                                tarjetaKpi(SVG_TRENDING, "Alta probabilidad de contratar",
                                                pct(altaProyeccion), "Proyección declarada",
                                                altaProyeccion * 100, AZUL2, BG_AZUL),
                                tarjetaKpi(SVG_ROBOT, "Demanda de IA",
                                                pct(demandaIA), "Mencionan IA como tendencia",
                                                demandaIA * 100, MORADO, BG_MORADO),
                                tarjetaKpi(SVG_STAR, "Calidad e Imagen (Dim A)",
                                                Double.isNaN(dimA) ? "n/d"
                                                                : String.format(Locale.US, "%.2f/3", dimA),
                                                "Percepción institucional", dimA / 3 * 100, NARANJA, BG_NARANJA));
                fila.setWidthFull();
                fila.getStyle().set("gap", "16px").set("flex-wrap", "wrap");
                return fila;
        }

        // ================================================================
        // Tarjeta KPI (fondo sólido suave, hover lift, progress bar)
        // ================================================================
        private Div tarjetaKpi(String svg, String etiqueta, String valor, String subtitulo,
                        double porcentajeParaBarra, String color, String colorFondo) {
                Div tarjeta = new Div();
                tarjeta.addClassName("panel-kpi");
                tarjeta.getStyle()
                                .set("padding", "20px 22px")
                                .set("border-radius", "12px")
                                .set("min-width", "240px")
                                .set("flex", "1")
                                .set("position", "relative")
                                .set("overflow", "hidden")
                                .set("cursor", "pointer")
                                .set("background", colorFondo)
                                .set("border-left", "4px solid " + color)
                                .set("box-shadow", "0 1px 3px rgba(0,0,0,.05)");

                // Header con ícono SVG + etiqueta
                Div header = new Div();
                header.getStyle().set("display", "flex").set("align-items", "center")
                                .set("gap", "10px").set("margin-bottom", "10px");

                Div iconoDiv = new Div();
                iconoDiv.getStyle()
                                .set("width", "40px").set("height", "40px")
                                .set("border-radius", "10px")
                                .set("background", color)
                                .set("display", "flex").set("align-items", "center")
                                .set("justify-content", "center")
                                .set("flex-shrink", "0");
                iconoDiv.getElement().executeJs("this.innerHTML = $0", svgIcon(svg, "#ffffff", 20));

                Div labelDiv = new Div();
                labelDiv.setText(etiqueta);
                labelDiv.getStyle().set("font-size", "12px").set("font-weight", "600")
                                .set("color", "#555").set("text-transform", "uppercase")
                                .set("letter-spacing", "0.4px");
                header.add(iconoDiv, labelDiv);

                Div valorDiv = new Div();
                valorDiv.setText(valor);
                valorDiv.getStyle()
                                .set("font-size", "26px")
                                .set("font-weight", "700")
                                .set("color", color)
                                .set("margin-bottom", "6px")
                                .set("line-height", "1.1");

                Div subDiv = new Div();
                subDiv.setText(subtitulo);
                subDiv.getStyle().set("font-size", "12px").set("color", "#888");

                tarjeta.add(header, valorDiv, subDiv);

                // Progress bar (color sólido)
                if (porcentajeParaBarra >= 0 && porcentajeParaBarra <= 100) {
                        Div progressWrapper = new Div();
                        progressWrapper.getStyle()
                                        .set("height", "5px")
                                        .set("background", "rgba(0,0,0,.06)")
                                        .set("border-radius", "3px")
                                        .set("overflow", "hidden")
                                        .set("margin-top", "12px");

                        Div bar = new Div();
                        bar.addClassName("panel-progress-bar");
                        bar.getElement().setAttribute("data-target",
                                        String.valueOf((int) Math.min(100, porcentajeParaBarra)));
                        bar.getStyle()
                                        .set("height", "100%")
                                        .set("width", "0%")
                                        .set("border-radius", "3px")
                                        .set("background", color)
                                        .set("transition", "width 1.4s cubic-bezier(0.4, 0, 0.2, 1)");

                        progressWrapper.add(bar);
                        tarjeta.add(progressWrapper);
                }

                // Hover lift
                tarjeta.getElement().executeJs(
                                "this.addEventListener('mouseenter', () => {"
                                                + "  this.style.transform = 'translateY(-5px)';"
                                                + "  this.style.boxShadow = '0 10px 24px rgba(0,0,0,.1)';"
                                                + "});"
                                                + "this.addEventListener('mouseleave', () => {"
                                                + "  this.style.transform = 'translateY(0)';"
                                                + "  this.style.boxShadow = '0 1px 3px rgba(0,0,0,.05)';"
                                                + "});");
                return tarjeta;
        }

        // ================================================================
        // Gráficos resumen (2x2)
        // ================================================================
        private VerticalLayout construirGraficosResumen(ServicioAnalisis analisis) {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.setSpacing(true);

                HorizontalLayout fila1 = new HorizontalLayout();
                fila1.setWidthFull();
                fila1.getStyle().set("gap", "16px").set("flex-wrap", "wrap");

                Map<String, Integer> frecSit = analisis.frecuenciasDe("situacion_laboral_actual");
                Component tortaSit = GraficoCanvas.enTarjeta(
                                GraficoCanvas.torta("panel_torta_sit",
                                                valoresDe(frecSit),
                                                etiquetasDe(frecSit, Map.of(
                                                                "TRABAJA_ORG", "Trabaja en org.",
                                                                "NO_TRABAJA", "No trabaja",
                                                                "EMPRENDIMIENTO", "Emprendimiento")),
                                                new String[] { VERDE, NARANJA, MORADO },
                                                "Situación laboral de los titulados"),
                                "Situación laboral (N=" + analisis.mapasTitulados().size() + ")");
                tortaSit.addClassName("panel-grafico");

                Map<String, Integer> frecAreas = analisis.frecuenciasDe("area_interes_posgrado");
                List<Map.Entry<String, Integer>> topAreas = analisis.topCategorias(frecAreas, 6);
                int[] vAreas = new int[topAreas.size()];
                String[] lAreas = new String[topAreas.size()];
                for (int i = 0; i < topAreas.size(); i++) {
                        lAreas[i] = etiquetaArea(topAreas.get(i).getKey());
                        vAreas[i] = topAreas.get(i).getValue();
                }
                Component barrasAreas = GraficoCanvas.enTarjeta(
                                GraficoCanvas.barrasVerticales("panel_bars_areas",
                                                vAreas, lAreas, VERDE, "Áreas de interés para posgrado (top 6)"),
                                "Áreas de interés (n=" + sumar(vAreas) + ")");
                barrasAreas.addClassName("panel-grafico");

                fila1.add(tortaSit, barrasAreas);
                v.add(fila1);

                HorizontalLayout fila2 = new HorizontalLayout();
                fila2.setWidthFull();
                fila2.getStyle().set("gap", "16px").set("flex-wrap", "wrap");

                Map<String, Integer> frecTipo = analisis.frecuenciasTipoOrg();
                Component tortaTipo = GraficoCanvas.enTarjeta(
                                GraficoCanvas.torta("panel_torta_tipo",
                                                valoresDe(frecTipo),
                                                etiquetasDe(frecTipo, Map.of(
                                                                "PUBLICO", "Público",
                                                                "PRIVADO", "Privado")),
                                                new String[] { AZUL, NARANJA },
                                                "Tipo de organización empleadora"),
                                "Tipo de organización (M=" + analisis.mapasEmpleadores().size() + ")");
                tortaTipo.addClassName("panel-grafico");

                Map<String, Integer> frecNivel = analisis.frecuenciasNivelAcademico();
                Component barrasNivel = GraficoCanvas.enTarjeta(
                                GraficoCanvas.barrasVerticales("panel_bars_nivel",
                                                valoresDe(frecNivel),
                                                etiquetasDe(frecNivel, Map.of(
                                                                "LICENCIATURA", "Licenciatura",
                                                                "DIPLOMADO", "Diplomado",
                                                                "ESPECIALIDAD", "Especialidad",
                                                                "MAESTRIA", "Maestría")),
                                                MORADO, "Nivel académico demandado"),
                                "Nivel académico demandado (n=" + sumar(valoresDe(frecNivel)) + ")");
                barrasNivel.addClassName("panel-grafico");

                fila2.add(tortaTipo, barrasNivel);
                v.add(fila2);
                return v;
        }

        // ================================================================
        // Hallazgos
        // ================================================================
        private VerticalLayout construirHallazgos(ServicioAnalisis analisis) {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.setSpacing(true);

                List<Map<String, String>> mt = analisis.mapasTitulados();
                List<Map<String, String>> me = analisis.mapasEmpleadores();

                double insercion = analisis.tasaInsercion();
                double yaTrabajaba = analisis.proporcionDonde(mt, "tiempo_primer_empleo",
                                vv -> "YA_TRABAJABA".equals(vv));
                double recursosPropios = analisis.proporcionDonde(mt, "financiamiento_formacion",
                                vv -> LimpiezaDatos.normalizar(vv).contains("PROPIO"));
                double beca = analisis.proporcionRequiereBeca();
                double altaProy = analisis.proporcionDonde(me, "posibilidad_futura_contratacion",
                                vv -> "ALTA_PROBABILIDAD".equals(vv));
                double demandaIA = analisis.proporcionDemandaIA();

                v.add(hallazgo(SVG_BRIEFCASE, "Empleabilidad de los titulados",
                                "La tasa de inserción es del " + pct(insercion)
                                                + ", lo que refleja una alta empleabilidad del egresado. "
                                                + "Además, el " + pct(yaTrabajaba)
                                                + " ya contaba con un empleo antes de titularse: existe una vinculación "
                                                + "temprana con el mercado tecnológico.",
                                VERDE));

                v.add(hallazgo(SVG_BILL, "Barrera financiera en formación continua",
                                "El " + pct(recursosPropios) + " financió su formación de posgrado con recursos "
                                                + "propios; solo el " + pct(beca) + " obtuvo algún tipo de beca. "
                                                + "Se trata de una barrera económica significativa que limita el acceso a "
                                                + "programas de actualización profesional.",
                                NARANJA));

                v.add(hallazgo(SVG_TRENDING, "Predisposición del sector empleador",
                                "El " + pct(altaProy) + " de las organizaciones encuestadas declara una alta "
                                                + "probabilidad de contratar Ingenieros de Sistemas en los próximos años. "
                                                + "Esto confirma que el sector productivo mantiene una demanda sostenida de "
                                                + "perfiles tecnológicos.",
                                AZUL2));

                v.add(hallazgo(SVG_ROBOT, "La IA como tendencia dominante",
                                "La demanda explícita de IA alcanza al " + pct(demandaIA)
                                                + " de los empleadores, lo que la posiciona como la tendencia emergente más "
                                                + "relevante para el diseño de la oferta académica de posgrado.",
                                MORADO));

                return v;
        }

        private Div hallazgo(String svg, String titulo, String texto, String color) {
                Div caja = new Div();
                caja.addClassName("panel-hallazgo");
                caja.getStyle()
                                .set("background", "#ffffff")
                                .set("border-left", "4px solid " + color)
                                .set("border-radius", "10px")
                                .set("padding", "16px 20px")
                                .set("margin", "6px 0")
                                .set("width", "100%")
                                .set("box-shadow", "0 1px 3px rgba(0,0,0,.05)")
                                .set("transition", "all 0.3s ease")
                                .set("cursor", "pointer");

                HorizontalLayout row = new HorizontalLayout();
                row.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.START);
                row.getStyle().set("gap", "14px").set("width", "100%");

                Div iconoDiv = new Div();
                iconoDiv.getStyle()
                                .set("width", "42px").set("height", "42px")
                                .set("border-radius", "10px")
                                .set("background", color)
                                .set("display", "flex").set("align-items", "center")
                                .set("justify-content", "center")
                                .set("flex-shrink", "0");
                iconoDiv.getElement().executeJs("this.innerHTML = $0", svgIcon(svg, "#ffffff", 20));

                Div textos = new Div();
                textos.getStyle().set("flex", "1");
                Div h = new Div();
                h.setText(titulo);
                h.getStyle().set("font-weight", "700").set("color", AZUL)
                                .set("font-size", "15px").set("margin-bottom", "6px");
                Div p = new Div();
                p.setText(texto);
                p.getStyle().set("color", "#2c3e50").set("font-size", "13px")
                                .set("line-height", "1.6");
                textos.add(h, p);

                row.add(iconoDiv, textos);
                caja.add(row);

                caja.getElement().executeJs(
                                "this.addEventListener('mouseenter', () => {"
                                                + "  this.style.boxShadow = '0 6px 18px rgba(0,0,0,.08)';"
                                                + "  this.style.transform = 'translateY(-2px)';"
                                                + "});"
                                                + "this.addEventListener('mouseleave', () => {"
                                                + "  this.style.boxShadow = '0 1px 3px rgba(0,0,0,.05)';"
                                                + "  this.style.transform = 'translateY(0)';"
                                                + "});");
                return caja;
        }

        // ================================================================
        // Recomendaciones
        // ================================================================
        private VerticalLayout construirRecomendaciones(ServicioRecomendaciones recomendaciones) {
                VerticalLayout v = new VerticalLayout();
                v.setPadding(false);
                v.setSpacing(true);
                for (String r : recomendaciones.generar()) {
                        Div caja = new Div();
                        caja.addClassName("panel-hallazgo");
                        caja.getStyle()
                                        .set("background", "#ffffff")
                                        .set("border-radius", "10px")
                                        .set("padding", "14px 20px")
                                        .set("border-left", "3px solid " + AZUL2)
                                        .set("font-size", "13px")
                                        .set("color", "#2c3e50")
                                        .set("box-shadow", "0 1px 3px rgba(0,0,0,.05)")
                                        .set("transition", "all 0.3s ease");
                        caja.setText(r);
                        caja.getElement().executeJs(
                                        "this.addEventListener('mouseenter', () => {"
                                                        + "  this.style.transform = 'translateX(4px)';"
                                                        + "  this.style.boxShadow = '0 4px 12px rgba(0,0,0,.08)';"
                                                        + "});"
                                                        + "this.addEventListener('mouseleave', () => {"
                                                        + "  this.style.transform = 'translateX(0)';"
                                                        + "  this.style.boxShadow = '0 1px 3px rgba(0,0,0,.05)';"
                                                        + "});");
                        v.add(caja);
                }
                return v;
        }

        // ================================================================
        // Atajos con botones (colores sólidos + hover lift)
        // ================================================================
        private HorizontalLayout construirAtajos() {
                HorizontalLayout atajos = new HorizontalLayout(
                                botonIr(SVG_USERS, "Ver Titulados", VistaTitulados.class, AZUL2),
                                botonIr(SVG_BUILDING, "Ver Empleadores", VistaEmpleadores.class, VERDE),
                                botonIr(SVG_CHART, "Simulación Monte Carlo", VistaSimulacion.class, MORADO),
                                botonIr(SVG_EDIT, "Registrar encuesta", VistaRegistro.class, NARANJA),
                                botonIr(SVG_UPLOAD, "Importar CSV", VistaImportar.class, GRIS));
                atajos.setWidthFull();
                atajos.getStyle().set("gap", "12px").set("flex-wrap", "wrap");
                return atajos;
        }

        private Button botonIr(String svg, String texto, Class<? extends Component> vista, String color) {
                Button boton = new Button();
                boton.getStyle()
                                .set("background", color)
                                .set("color", "white")
                                .set("border", "none")
                                .set("padding", "10px 20px")
                                .set("border-radius", "10px")
                                .set("font-weight", "600")
                                .set("font-size", "13px")
                                .set("cursor", "pointer")
                                .set("box-shadow", "0 3px 10px " + color + "40")
                                .set("transition", "all 0.3s ease");
                boton.getElement().executeJs(
                                "this.innerHTML = $0",
                                "<span style='display:inline-flex;align-items:center;gap:8px;'>"
                                                + svgIcon(svg, "#ffffff", 16)
                                                + "<span>" + escapar(texto) + "</span></span>");
                boton.getElement().executeJs(
                                "this.addEventListener('mouseenter', () => {"
                                                + "  this.style.transform = 'translateY(-2px)';"
                                                + "  this.style.boxShadow = '0 8px 20px " + color + "60';"
                                                + "});"
                                                + "this.addEventListener('mouseleave', () => {"
                                                + "  this.style.transform = 'translateY(0)';"
                                                + "  this.style.boxShadow = '0 3px 10px " + color + "40';"
                                                + "});");
                boton.addClickListener(e -> UI.getCurrent().navigate(vista));
                return boton;
        }

        // ================================================================
        // Utilidades
        // ================================================================
        private static String svgIcon(String path, String color, int size) {
                return "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' "
                                + "width='" + size + "' height='" + size + "' fill='" + color + "'>"
                                + "<path d='" + path + "'/></svg>";
        }

        private String pct(double proporcion) {
                if (Double.isNaN(proporcion))
                        return "n/d";
                return String.format(Locale.US, "%.1f%%", proporcion * 100);
        }

        private static int[] valoresDe(Map<String, Integer> frec) {
                int[] v = new int[frec.size()];
                int i = 0;
                for (int x : frec.values())
                        v[i++] = x;
                return v;
        }

        private static String[] etiquetasDe(Map<String, Integer> frec, Map<String, String> mapa) {
                List<String> out = new ArrayList<>();
                for (String key : frec.keySet())
                        out.add(mapa.getOrDefault(key, key));
                return out.toArray(new String[0]);
        }

        private static int sumar(int[] valores) {
                int s = 0;
                for (int v : valores)
                        s += v;
                return s;
        }

        private static String etiquetaArea(String key) {
                if (key == null)
                        return "—";
                return switch (key) {
                        case "IA" -> "IA";
                        case "ING_SOFTWARE" -> "Ing. Software";
                        case "REDES_SEGURIDAD" -> "Redes y Seg.";
                        case "BASE_DATOS" -> "Base de Datos";
                        case "ROBOTICA" -> "Robótica";
                        case "OTRAS" -> "Otras";
                        default -> key;
                };
        }

        private static String escapar(String s) {
                if (s == null)
                        return "";
                return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                                .replace("\"", "&quot;");
        }

        // CSS de animaciones (sin degradados)
        private void inyectarCss() {
                String css = """
                                @keyframes panelFadeInUp {
                                    from { opacity: 0; transform: translateY(30px); }
                                    to { opacity: 1; transform: translateY(0); }
                                }
                                .panel-animate-in {
                                    animation: panelFadeInUp 0.7s cubic-bezier(0.4, 0, 0.2, 1) both;
                                }
                                .panel-kpi {
                                    transition: transform 0.3s ease, box-shadow 0.3s ease;
                                }
                                .panel-grafico {
                                    transition: transform 0.3s ease, box-shadow 0.3s ease;
                                }
                                .panel-grafico:hover {
                                    transform: translateY(-3px);
                                    box-shadow: 0 8px 24px rgba(0,0,0,.08) !important;
                                }
                                """;
                getElement().executeJs(
                                "const id = 'panel-anim';"
                                                + "const prev = document.getElementById(id);"
                                                + "if (prev) prev.remove();"
                                                + "const s = document.createElement('style');"
                                                + "s.id = id;"
                                                + "s.textContent = $0;"
                                                + "document.head.appendChild(s);",
                                css);
        }
}