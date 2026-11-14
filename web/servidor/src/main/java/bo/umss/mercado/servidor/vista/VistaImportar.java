package bo.umss.mercado.servidor.vista;

import bo.umss.mercado.servidor.servicio.ServicioCarga;
import bo.umss.mercado.servidor.servicio.ServicioImportacionCruda;
import bo.umss.mercado.servidor.servicio.ServicioImportacionCruda.Accion;
import bo.umss.mercado.servidor.servicio.ServicioImportacionCruda.Resultado;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.io.InputStream;
import java.util.function.Consumer;

@Route(value = "importar", layout = MainLayout.class)
@PageTitle("Importar | Mercado UMSS")
public class VistaImportar extends VerticalLayout {

        // Paleta (colores sólidos, sin degradados)
        private static final String AZUL = "#0b3d62";
        private static final String AZUL2 = "#0984e3";
        private static final String VERDE = "#27ae60";
        private static final String NARANJA = "#e17055";
        private static final String MORADO = "#6c5ce7";
        private static final String GRIS = "#7f8c8d";
        private static final String ROJO = "#c0392b";

        private static final String BG_AZUL = "#e8f2fc";
        private static final String BG_VERDE = "#e8f5e9";
        private static final String BG_NARANJA = "#fdf1ee";
        private static final String BG_MORADO = "#f0ebfc";

        // SVG paths (Material Design Icons 24x24)
        private static final String SVG_USERS = "M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z";
        private static final String SVG_BUILDING = "M12 7V3H2v18h20V7H12zM6 19H4v-2h2v2zm0-4H4v-2h2v2zm0-4H4V9h2v2zm0-4H4V5h2v2zm4 12H8v-2h2v2zm0-4H8v-2h2v2zm0-4H8V9h2v2zm0-4H8V5h2v2zm10 12h-8v-2h2v-2h-2v-2h2v-2h-2V9h8v10zm-2-8h-2v2h2v-2zm0 4h-2v2h2v-2z";
        private static final String SVG_UPLOAD = "M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM14 13v4h-4v-4H7l5-5 5 5h-3z";
        private static final String SVG_INFO = "M11 7h2v2h-2zm0 4h2v6h-2zm1-9C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z";
        private static final String SVG_DOC = "M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z";
        private static final String SVG_COPY = "M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z";
        private static final String SVG_REFRESH = "M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z";
        private static final String SVG_CLOSE = "M19 6.41 17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z";
        private static final String SVG_ALERT = "M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z";
        private static final String SVG_CHECK = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z";

        private final ServicioImportacionCruda importacionCruda;

        public VistaImportar(ServicioCarga carga,
                        ServicioImportacionCruda importacionCruda) {
                this.importacionCruda = importacionCruda;

                inyectarCss();
                setPadding(true);
                setSpacing(true);
                getStyle().set("background", "#f8fafc").set("min-height", "100vh");

                add(construirHeader());
                add(construirTituloSeccion(SVG_UPLOAD, "Subir archivo CSV",
                                "Selecciona el tipo de encuesta que deseas importar"));
                add(construirFilaTarjetas());
                add(construirTituloSeccion(SVG_INFO, "Formatos soportados",
                                "La app detecta automáticamente el formato del archivo"));
                add(construirInfoBox());

                // Animación de entrada escalonada
                getElement().executeJs(
                                "setTimeout(() => {"
                                                + "  document.querySelectorAll('.importar-card').forEach((el, i) => {"
                                                + "    el.style.animationDelay = (i * 120) + 'ms';"
                                                + "    el.classList.add('importar-animate-in');"
                                                + "  });"
                                                + "}, 100);");
        }

        // ================================================================
        // Header (color sólido + círculo decorativo sutil)
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
                titulo.setText("Importar encuestas");
                titulo.getStyle()
                                .set("font-size", "26px")
                                .set("font-weight", "700")
                                .set("letter-spacing", "-0.3px")
                                .set("margin-bottom", "4px")
                                .set("position", "relative").set("z-index", "1");

                Div sub = new Div();
                sub.setText("Carga masiva de datos desde archivos CSV (MS Forms o normalizados)");
                sub.getStyle()
                                .set("font-size", "13px")
                                .set("opacity", "0.85")
                                .set("margin-bottom", "16px")
                                .set("position", "relative").set("z-index", "1");

                HorizontalLayout chips = new HorizontalLayout();
                chips.getStyle().set("gap", "10px").set("flex-wrap", "wrap")
                                .set("position", "relative").set("z-index", "1");
                chips.add(chipHeader(SVG_DOC, "CSV crudo o normalizado"));
                chips.add(chipHeader(SVG_CHECK, "Detección automática"));
                chips.add(chipHeader(SVG_COPY, "Duplicar / Reemplazar"));

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
        // Título de sección con icono SVG + línea separadora
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
        // Fila de tarjetas de upload
        // ================================================================
        private HorizontalLayout construirFilaTarjetas() {
                Upload upTit = subidaPara("Subir titulados", new String[] { ".csv" }, AZUL2,
                                contenido -> mostrarModal(contenido, true));
                Upload upEmp = subidaPara("Subir empleadores", new String[] { ".csv" }, VERDE,
                                contenido -> mostrarModal(contenido, false));

                Div cardTit = construirTarjetaUpload(
                                SVG_USERS, "Titulados",
                                "Encuesta de egresados",
                                "Formato crudo (57 columnas) o normalizado (35 columnas). "
                                                + "Los IDs se generan de forma estable para evitar duplicados.",
                                AZUL2, BG_AZUL, upTit);

                Div cardEmp = construirTarjetaUpload(
                                SVG_BUILDING, "Empleadores",
                                "Encuesta de organizaciones",
                                "Formato crudo (32 columnas) o normalizado (24 columnas). "
                                                + "Incluye las 18 variables Likert y las 6 columnas derivadas.",
                                VERDE, BG_VERDE, upEmp);

                HorizontalLayout fila = new HorizontalLayout(cardTit, cardEmp);
                fila.setWidthFull();
                fila.getStyle().set("gap", "20px").set("flex-wrap", "wrap");
                return fila;
        }

        private Div construirTarjetaUpload(String svg, String titulo, String subtitulo,
                        String descripcion, String color, String bgColor, Upload subida) {
                Div card = new Div();
                card.addClassName("importar-card");
                card.getStyle()
                                .set("background", "#ffffff")
                                .set("border-radius", "14px")
                                .set("padding", "24px")
                                .set("flex", "1")
                                .set("min-width", "340px")
                                .set("box-shadow", "0 1px 3px rgba(0,0,0,.05)")
                                .set("border-top", "4px solid " + color)
                                .set("position", "relative")
                                .set("overflow", "hidden")
                                .set("transition", "all 0.3s ease");

                // Header de la tarjeta (icono + título + subtítulo)
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
                t.getStyle().set("font-size", "16px").set("font-weight", "700").set("color", AZUL);
                Div s = new Div();
                s.setText(subtitulo);
                s.getStyle().set("font-size", "12px").set("color", "#888").set("margin-top", "2px");
                textos.add(t, s);

                header.add(icono, textos);

                // Descripción con fondo suave
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

                card.add(header, desc, subida);

                // Hover lift
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

        // ================================================================
        // Info box de formatos (alerta suave)
        // ================================================================
        private Div construirInfoBox() {
                Div box = new Div();
                box.getStyle()
                                .set("background", "#ffffff")
                                .set("border-left", "4px solid " + NARANJA)
                                .set("border-radius", "10px")
                                .set("padding", "16px 20px")
                                .set("box-shadow", "0 1px 3px rgba(0,0,0,.05)")
                                .set("display", "flex")
                                .set("gap", "14px")
                                .set("align-items", "flex-start");

                Div icono = new Div();
                icono.getStyle()
                                .set("width", "42px").set("height", "42px")
                                .set("border-radius", "10px")
                                .set("background", NARANJA)
                                .set("display", "flex").set("align-items", "center")
                                .set("justify-content", "center")
                                .set("flex-shrink", "0");
                icono.getElement().executeJs("this.innerHTML = $0", svgIcon(SVG_ALERT, "#ffffff", 20));

                Div textos = new Div();
                textos.getStyle().set("flex", "1");
                Div h = new Div();
                h.setText("Diferencia entre formato crudo y normalizado");
                h.getStyle().set("font-weight", "700").set("color", AZUL)
                                .set("font-size", "14px").set("margin-bottom", "6px");

                Div p = new Div();
                p.getElement().setProperty("innerHTML",
                                "<span style='color:#2c3e50;font-size:13px;line-height:1.65;'>"
                                                + "<b>Crudo:</b> el CSV que exporta MS Forms directamente "
                                                + "(57 columnas para titulados, 32 para empleadores). "
                                                + "La app lo limpia, unifica SI/NO y genera IDs estables.<br><br>"
                                                + "<b>Normalizado:</b> el CSV de 35/24 columnas que usa la app internamente. "
                                                + "Si subes este formato, se trata como duplicar (salta IDs repetidos)."
                                                + "</span>");

                textos.add(h, p);
                box.add(icono, textos);
                return box;
        }

        // ================================================================
        // Upload con botón styled (memory buffer + succeeded listener)
        // ================================================================
        private Upload subidaPara(String textoBoton, String[] extensiones, String color,
                        Consumer<InputStream> alRecibir) {
                MemoryBuffer memoria = new MemoryBuffer();
                Upload subida = new Upload(memoria);
                subida.setAcceptedFileTypes(extensiones);
                subida.addClassName("importar-upload");

                Button boton = new Button();
                boton.getStyle()
                                .set("background", color)
                                .set("color", "white")
                                .set("border", "none")
                                .set("padding", "11px 22px")
                                .set("border-radius", "10px")
                                .set("font-weight", "600")
                                .set("font-size", "13px")
                                .set("cursor", "pointer")
                                .set("box-shadow", "0 3px 10px " + color + "40")
                                .set("transition", "all 0.3s ease");
                boton.getElement().executeJs(
                                "this.innerHTML = $0",
                                "<span style='display:inline-flex;align-items:center;gap:8px;'>"
                                                + svgIcon(SVG_UPLOAD, "#ffffff", 16)
                                                + "<span>" + esc(textoBoton) + "</span></span>");
                boton.getElement().executeJs(
                                "this.addEventListener('mouseenter', () => {"
                                                + "  this.style.transform = 'translateY(-2px)';"
                                                + "  this.style.boxShadow = '0 8px 20px " + color + "60';"
                                                + "});"
                                                + "this.addEventListener('mouseleave', () => {"
                                                + "  this.style.transform = 'translateY(0)';"
                                                + "  this.style.boxShadow = '0 3px 10px " + color + "40';"
                                                + "});");

                subida.setUploadButton(boton);
                subida.addSucceededListener(exito -> alRecibir.accept(
                                memoria.getInputStream()));
                return subida;
        }

        // ================================================================
        // Modal de decisión (Duplicar / Reemplazar / Cancelar)
        // ================================================================
        private void mostrarModal(InputStream contenido, boolean esTitulado) {
                Dialog modal = new Dialog();
                modal.setWidth("540px");
                modal.setCloseOnEsc(true);

                modal.setHeaderTitle(esTitulado
                                ? "Se detectó un CSV de TITULADOS"
                                : "Se detectó un CSV de EMPLEADORES");

                modal.getHeader().getElement().getStyle()
                                .set("background", "#f5f7fa")
                                .set("padding", "16px 22px")
                                .set("border-bottom", "1px solid #e0e8f0")
                                .set("font-weight", "700");

                Div pregunta = new Div();
                pregunta.setText("¿Qué deseas hacer con los datos del archivo?");
                pregunta.getStyle()
                                .set("font-size", "14px")
                                .set("color", "#444")
                                .set("margin-bottom", "16px");

                Div cardDup = opcionModal(SVG_COPY, "Duplicar (añadir)",
                                "Añade solo los registros nuevos. Salta automáticamente los IDs que ya existen en la base.",
                                VERDE, () -> {
                                        modal.close();
                                        procesar(contenido, esTitulado, Accion.DUPLICAR);
                                });

                Div cardRep = opcionModal(SVG_REFRESH, "Reemplazar (borrar y cargar)",
                                "Elimina TODOS los registros actuales de esta tabla y carga únicamente los del CSV.",
                                ROJO, () -> {
                                        modal.close();
                                        procesar(contenido, esTitulado, Accion.REEMPLAZAR);
                                });

                Button cancelar = new Button();
                cancelar.getStyle()
                                .set("background", "transparent")
                                .set("color", GRIS)
                                .set("border", "1px solid #d0d7de")
                                .set("padding", "10px 22px")
                                .set("border-radius", "10px")
                                .set("font-weight", "600")
                                .set("font-size", "13px")
                                .set("cursor", "pointer")
                                .set("transition", "all 0.25s ease")
                                .set("margin-top", "8px");
                cancelar.getElement().executeJs(
                                "this.innerHTML = $0",
                                "<span style='display:inline-flex;align-items:center;gap:8px;'>"
                                                + svgIcon(SVG_CLOSE, GRIS, 16)
                                                + "<span>Cancelar</span></span>");
                cancelar.getElement().executeJs(
                                "this.addEventListener('mouseenter', () => {"
                                                + "  this.style.background = '#f5f7fa';"
                                                + "  this.style.color = '#555';"
                                                + "});"
                                                + "this.addEventListener('mouseleave', () => {"
                                                + "  this.style.background = 'transparent';"
                                                + "  this.style.color = '" + GRIS + "';"
                                                + "});");
                cancelar.addClickListener(e -> modal.close());

                HorizontalLayout pie = new HorizontalLayout(cancelar);
                pie.setWidthFull();
                pie.setJustifyContentMode(
                                com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.END);

                VerticalLayout cuerpo = new VerticalLayout(pregunta, cardDup, cardRep, pie);
                cuerpo.setPadding(false);
                cuerpo.setSpacing(true);
                cuerpo.getStyle().set("padding", "20px 22px");
                modal.add(cuerpo);
                modal.open();
        }

        private Div opcionModal(String svg, String titulo, String descripcion,
                        String color, Runnable accion) {
                Div card = new Div();
                card.getStyle()
                                .set("display", "flex")
                                .set("align-items", "center")
                                .set("gap", "14px")
                                .set("padding", "16px 18px")
                                .set("border-radius", "10px")
                                .set("background", "#ffffff")
                                .set("border-left", "4px solid " + color)
                                .set("cursor", "pointer")
                                .set("transition", "all 0.25s ease")
                                .set("box-shadow", "0 1px 3px rgba(0,0,0,.05)")
                                .set("width", "100%");

                Div icono = new Div();
                icono.getStyle()
                                .set("width", "42px").set("height", "42px")
                                .set("border-radius", "10px")
                                .set("background", color)
                                .set("display", "flex").set("align-items", "center")
                                .set("justify-content", "center")
                                .set("flex-shrink", "0");
                icono.getElement().executeJs("this.innerHTML = $0", svgIcon(svg, "#ffffff", 20));

                Div textos = new Div();
                textos.getStyle().set("flex", "1");
                Div t = new Div();
                t.setText(titulo);
                t.getStyle().set("font-size", "14px").set("font-weight", "700")
                                .set("color", AZUL).set("margin-bottom", "3px");
                Div d = new Div();
                d.setText(descripcion);
                d.getStyle().set("font-size", "12px").set("color", "#666")
                                .set("line-height", "1.45");
                textos.add(t, d);

                card.add(icono, textos);

                card.getElement().executeJs(
                                "this.addEventListener('mouseenter', () => {"
                                                + "  this.style.transform = 'translateY(-2px)';"
                                                + "  this.style.boxShadow = '0 6px 18px rgba(0,0,0,.08)';"
                                                + "});"
                                                + "this.addEventListener('mouseleave', () => {"
                                                + "  this.style.transform = 'translateY(0)';"
                                                + "  this.style.boxShadow = '0 1px 3px rgba(0,0,0,.05)';"
                                                + "});");
                card.addClickListener(e -> accion.run());
                return card;
        }

        // ================================================================
        // Procesar importación (sin cambios funcionales)
        // ================================================================
        private void procesar(InputStream contenido, boolean esTitulado, Accion accion) {
                try {
                        Resultado r = esTitulado
                                        ? importacionCruda.importarTitulados(contenido, accion)
                                        : importacionCruda.importarEmpleadores(contenido, accion);
                        String mensaje = r.leidas() + " filas leídas: "
                                        + r.nuevos() + " guardadas, " + r.omitidos()
                                        + " omitidas por id repetido (" + accion + ").";
                        Notification.show(mensaje, 8000, Notification.Position.MIDDLE);
                } catch (Exception error) {
                        Notification.show("No se pudo leer: " + error.getMessage(),
                                        8000, Notification.Position.MIDDLE);
                }
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
                if (s == null) {
                        return "";
                }
                return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                                .replace("\"", "&quot;");
        }

        // CSS global (animación + estilos del wrapper de Upload)
        private void inyectarCss() {
                String css = """
                                @keyframes importarFadeInUp {
                                    from { opacity: 0; transform: translateY(20px); }
                                    to { opacity: 1; transform: translateY(0); }
                                }
                                .importar-animate-in {
                                    animation: importarFadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) both;
                                }
                                .importar-upload {
                                    border: 2px dashed #cfd8e3 !important;
                                    border-radius: 10px !important;
                                    padding: 14px !important;
                                    background: #fafbfc !important;
                                    transition: all 0.25s ease !important;
                                }
                                .importar-upload:hover {
                                    border-color: #0984e3 !important;
                                    background: #f0f7ff !important;
                                }
                                .importar-upload::part(drop-label) {
                                    display: none !important;
                                }
                                """;
                getElement().executeJs(
                                "const id='importar-anim';"
                                                + "const prev=document.getElementById(id);"
                                                + "if(prev)prev.remove();"
                                                + "const s=document.createElement('style');"
                                                + "s.id=id;"
                                                + "s.textContent=$0;"
                                                + "document.head.appendChild(s);",
                                css);
        }
}