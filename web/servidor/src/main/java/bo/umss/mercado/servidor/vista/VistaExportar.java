package bo.umss.mercado.servidor.vista;

import bo.umss.mercado.servidor.servicio.ServicioExportacion;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * VistaExportar: descarga de archivos.
 * Solo dos modos:
 * 1) Libros Excel multi-hoja (.xlsx) — auditoría completa.
 * 2) Informes PDF ejecutivos (.pdf) — presentación y lectura rápida.
 * Diseño con el estándar VistaPanel: header sólido, tarjetas con borde
 * superior de color, SVG inline, hover lift, animaciones fade-in.
 */
@Route(value = "exportar", layout = MainLayout.class)
@PageTitle("Exportar | Mercado UMSS")
public class VistaExportar extends VerticalLayout {

        private static final String AZUL = "#0b3d62";
        private static final String AZUL2 = "#0984e3";
        private static final String VERDE = "#27ae60";
        private static final String BG_AZUL = "#e8f2fc";
        private static final String BG_VERDE = "#e8f5e9";

        private static final String SVG_USERS = "M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z";
        private static final String SVG_BUILDING = "M12 7V3H2v18h20V7H12zM6 19H4v-2h2v2zm0-4H4v-2h2v2zm0-4H4V9h2v2zm0-4H4V5h2v2zm4 12H8v-2h2v2zm0-4H8v-2h2v2zm0-4H8V9h2v2zm0-4H8V5h2v2zm10 12h-8v-2h2v-2h-2v-2h2v-2h-2V9h8v10zm-2-8h-2v2h2v-2zm0 4h-2v2h2v-2z";
        private static final String SVG_DOWNLOAD = "M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z";
        private static final String SVG_DOC = "M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z";

        public VistaExportar(ServicioExportacion exportacion) {
                inyectarCss();
                setPadding(true);
                setSpacing(true);
                getStyle().set("background", "#f8fafc").set("min-height", "100vh");

                add(construirHeader());
                add(construirTituloSeccion(SVG_DOWNLOAD, "Reportes Multi-formato",
                                "Descarga los resultados consolidados listos para auditar o presentar"));
                add(construirFilaLibros());

                getElement().executeJs(
                                "setTimeout(() => {"
                                                + "  document.querySelectorAll('.export-card').forEach((el, i) => {"
                                                + "    el.style.animationDelay = (i * 80) + 'ms';"
                                                + "    el.classList.add('export-animate-in');"
                                                + "  });"
                                                + "}, 100);");
        }

        // ================================================================
        // Header
        // ================================================================
        private Div construirHeader() {
                Div header = new Div();
                header.getStyle()
                                .set("background", AZUL)
                                .set("padding", "28px 36px")
                                .set("border-radius", "16px")
                                .set("color", "white")
                                .set("margin-bottom", "8px")
                                .set("box-shadow", "0 6px 20px rgba(11,61,98,.2)")
                                .set("position", "relative")
                                .set("overflow", "hidden");

                Div titulo = new Div();
                titulo.setText("Exportar datos");
                titulo.getStyle()
                                .set("font-size", "26px")
                                .set("font-weight", "700")
                                .set("letter-spacing", "-0.3px")
                                .set("margin-bottom", "4px")
                                .set("position", "relative").set("z-index", "1");

                Div sub = new Div();
                sub.setText("Descarga los análisis en formatos listos para auditar o presentar");
                sub.getStyle()
                                .set("font-size", "13px")
                                .set("opacity", "0.85")
                                .set("margin-bottom", "16px")
                                .set("position", "relative").set("z-index", "1");

                HorizontalLayout chips = new HorizontalLayout();
                chips.getStyle().set("gap", "10px").set("flex-wrap", "wrap")
                                .set("position", "relative").set("z-index", "1");
                chips.add(chipHeader(SVG_DOC, "Excel .xlsx multi-hoja"));
                chips.add(chipHeader(SVG_DOWNLOAD, "Informes PDF ejecutivos"));

                Div circ = new Div();
                circ.getStyle()
                                .set("position", "absolute")
                                .set("top", "-60px").set("right", "-60px")
                                .set("width", "220px").set("height", "220px")
                                .set("background", "rgba(255,255,255,0.04)")
                                .set("border-radius", "50%")
                                .set("z-index", "0");

                header.add(circ, titulo, sub, chips);
                return header;
        }

        private Div chipHeader(String svg, String texto) {
                Div chip = new Div();
                chip.getStyle()
                                .set("background", "rgba(255,255,255,0.12)")
                                .set("padding", "7px 14px")
                                .set("border-radius", "20px")
                                .set("font-size", "12px")
                                .set("font-weight", "600")
                                .set("border", "1px solid rgba(255,255,255,0.2)")
                                .set("transition", "all 0.25s ease");
                chip.getElement().executeJs(
                                "this.innerHTML = $0",
                                "<span style='display:inline-flex;align-items:center;gap:6px;'>"
                                                + svgIcon(svg, "#ffffff", 13)
                                                + "<span>" + esc(texto) + "</span></span>");
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
        // Título de sección
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
        // Libros Excel y PDF (2 tarjetas grandes)
        // ================================================================
        private HorizontalLayout construirFilaLibros() {
                HorizontalLayout fila = new HorizontalLayout(
                                tarjetaLibro("titulados.xlsx", "titulados.pdf", "Titulados",
                                                "Libro Excel con 5 hojas + Informe PDF ejecutivo",
                                                "Datos crudos, univariado, bivariado, multivariado "
                                                                + "y el informe ejecutivo en PDF con portada, "
                                                                + "resumen y trazabilidad.",
                                                SVG_USERS, AZUL2, BG_AZUL),
                                tarjetaLibro("empleadores.xlsx", "empleadores.pdf", "Empleadores",
                                                "Libro Excel con 6 hojas + Informe PDF ejecutivo",
                                                "Datos crudos, univariado, Likert descriptivo, "
                                                                + "bivariado, multivariado y el informe ejecutivo "
                                                                + "en PDF con portada, resumen y trazabilidad.",
                                                SVG_BUILDING, VERDE, BG_VERDE));
                fila.setWidthFull();
                fila.getStyle().set("gap", "20px").set("flex-wrap", "wrap");
                return fila;
        }

        private Div tarjetaLibro(String archivoExcel, String archivoPdf,
                        String titulo, String subtitulo, String descripcion,
                        String svg, String color, String bgColor) {
                Div card = new Div();
                card.addClassName("export-card");
                card.getStyle()
                                .set("background", "#ffffff")
                                .set("border-radius", "14px")
                                .set("padding", "24px")
                                .set("flex", "1")
                                .set("min-width", "320px")
                                .set("box-shadow", "0 1px 3px rgba(0,0,0,.05)")
                                .set("border-top", "4px solid " + color)
                                .set("position", "relative")
                                .set("overflow", "hidden")
                                .set("transition", "all 0.3s ease");

                Div header = new Div();
                header.getStyle().set("display", "flex").set("align-items", "center")
                                .set("gap", "14px").set("margin-bottom", "14px");

                Div icono = new Div();
                icono.getStyle()
                                .set("width", "50px").set("height", "50px")
                                .set("border-radius", "12px")
                                .set("background", color)
                                .set("display", "flex").set("align-items", "center")
                                .set("justify-content", "center")
                                .set("flex-shrink", "0")
                                .set("box-shadow", "0 3px 10px " + color + "40");
                icono.getElement().executeJs("this.innerHTML = $0", svgIcon(svg, "#ffffff", 26));

                Div textos = new Div();
                Div t = new Div();
                t.setText(titulo);
                t.getStyle().set("font-size", "18px").set("font-weight", "700").set("color", AZUL);
                Div s = new Div();
                s.setText(subtitulo);
                s.getStyle().set("font-size", "12px").set("color", "#888").set("margin-top", "2px");
                textos.add(t, s);

                header.add(icono, textos);

                Div desc = new Div();
                desc.setText(descripcion);
                desc.getStyle()
                                .set("font-size", "13px")
                                .set("color", "#555")
                                .set("line-height", "1.55")
                                .set("padding", "12px 14px")
                                .set("background", bgColor)
                                .set("border-radius", "8px")
                                .set("margin-bottom", "18px")
                                .set("border-left", "3px solid " + color);

                HorizontalLayout botones = new HorizontalLayout();
                botones.getStyle().set("gap", "10px").set("flex-wrap", "wrap");

                Anchor botonExcel = crearBotonDescarga(
                                "/exportar/" + archivoExcel,
                                "Excel · " + archivoExcel,
                                svgIcon(SVG_DOWNLOAD, "#ffffff", 16),
                                color, true);
                botones.add(botonExcel);

                if (archivoPdf != null) {
                        Anchor botonPdf = crearBotonDescarga(
                                        "/exportar/" + archivoPdf,
                                        "PDF · " + archivoPdf,
                                        svgIcon(SVG_DOC, "#ffffff", 16),
                                        color, false);
                        botones.add(botonPdf);
                }

                card.add(header, desc, botones);

                card.getElement().executeJs(
                                "this.addEventListener('mouseenter', () => {"
                                                + "  this.style.transform = 'translateY(-4px)';"
                                                + "  this.style.boxShadow = '0 10px 24px rgba(0,0,0,.1)';"
                                                + "});"
                                                + "this.addEventListener('mouseleave', () => {"
                                                + "  this.style.transform = 'translateY(0)';"
                                                + "  this.style.boxShadow = '0 1px 3px rgba(0,0,0,.05)';"
                                                + "});");
                return card;
        }

        private Anchor crearBotonDescarga(String href, String texto, String svgHtml,
                        String color, boolean forzarDescarga) {
                Anchor boton = new Anchor(href, "");
                if (forzarDescarga) {
                        boton.getElement().setAttribute("download", true);
                } else {
                        boton.setTarget("_blank");
                }
                boton.getStyle()
                                .set("display", "inline-flex")
                                .set("align-items", "center")
                                .set("gap", "8px")
                                .set("background", color)
                                .set("color", "white")
                                .set("padding", "11px 22px")
                                .set("border-radius", "10px")
                                .set("font-weight", "600")
                                .set("font-size", "13px")
                                .set("text-decoration", "none")
                                .set("cursor", "pointer")
                                .set("box-shadow", "0 3px 10px " + color + "40")
                                .set("transition", "all 0.3s ease");
                boton.getElement().executeJs(
                                "this.innerHTML = $0",
                                "<span style='display:inline-flex;align-items:center;gap:8px;'>"
                                                + svgHtml
                                                + "<span>" + esc(texto) + "</span></span>");
                boton.getElement().executeJs(
                                "this.addEventListener('mouseenter', () => {"
                                                + "  this.style.transform = 'translateY(-2px)';"
                                                + "  this.style.boxShadow = '0 8px 20px " + color + "60';"
                                                + "});"
                                                + "this.addEventListener('mouseleave', () => {"
                                                + "  this.style.transform = 'translateY(0)';"
                                                + "  this.style.boxShadow = '0 3px 10px " + color + "40';"
                                                + "});");
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

        private static String esc(String s) {
                if (s == null)
                        return "";
                return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                                .replace("\"", "&quot;");
        }

        private void inyectarCss() {
                String css = """
                                @keyframes exportFadeInUp {
                                    from { opacity: 0; transform: translateY(20px); }
                                    to { opacity: 1; transform: translateY(0); }
                                }
                                .export-animate-in {
                                    animation: exportFadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) both;
                                }
                                """;
                getElement().executeJs(
                                "const id='exportar-anim';"
                                                + "const prev=document.getElementById(id);"
                                                + "if(prev)prev.remove();"
                                                + "const s=document.createElement('style');"
                                                + "s.id=id;"
                                                + "s.textContent=$0;"
                                                + "document.head.appendChild(s);",
                                css);
        }
}