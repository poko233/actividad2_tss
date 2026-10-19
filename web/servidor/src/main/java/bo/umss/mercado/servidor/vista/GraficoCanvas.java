package bo.umss.mercado.servidor.vista;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * GraficoCanvas: graficos estadisticos dibujados con SVG generado en Java.
 *
 * POR QUE SVG Y NO CANVAS:
 * el canvas necesita JavaScript que se ejecute DESPUES de que el navegador
 * ya dibujo el elemento, y ademas necesita un elemento <canvas> real (con
 * un <div> el navegador responde "c.getContext is not a function"). Con SVG
 * el grafico viaja dentro del HTML que el servidor manda, asi que SIEMPRE
 * se ve, sin depender de JavaScript ni de sincronismos.
 *
 * QUE GRAFICOS HACE:
 *   - barrasVerticales(): histogramas y frecuencias.
 *   - torta():           proporciones (pie chart con segmentos circulares).
 *   - mapaCalor():       tablas de contingencia coloreadas por intensidad.
 *   - radar():           los 10 items Likert de la encuesta a empleadores.
 */
public class GraficoCanvas {

    // Paleta simple y legible para todos los graficos.
    public static final String COLOR_PRINCIPAL = "#0b3d62";
    public static final String COLOR_SECUNDARIO = "#0984e3";
    public static final String COLOR_VERDE = "#27ae60";
    public static final String COLOR_NARANJA = "#e17055";
    public static final String COLOR_MORADO = "#6c5ce7";
    public static final String COLOR_GRIS = "#b2bec3";

    // Paleta para tortas (se repiten si hay mas categorias que colores).
    public static final String[] COLORES_TORTA = {
            COLOR_PRINCIPAL, COLOR_SECUNDARIO, COLOR_VERDE,
            COLOR_NARANJA, COLOR_MORADO, COLOR_GRIS
    };

    // ------------------------------------------------------------------------
    // barrasVerticales(): barras verticales (histogramas, frecuencias).
    //   id        : identificador del grafico (accesibilidad)
    //   datos     : valores numericos (uno por categoria)
    //   etiquetas : nombres de las categorias
    //   color     : color de las barras
    //   titulo    : titulo del grafico
    // ------------------------------------------------------------------------
    public static Component barrasVerticales(String id, int[] datos, String[] etiquetas,
            String color, String titulo) {
        final int ancho = 700;
        final int alto = 320;
        final int margenIzquierdo = 50;
        final int margenDerecho = 15;
        final int margenSuperior = 40;
        final int margenInferior = 80;
        final int anchoUtil = ancho - margenIzquierdo - margenDerecho;
        final int altoUtil = alto - margenSuperior - margenInferior;

        double maximo = 1;
        for (int dato : datos) {
            maximo = Math.max(maximo, dato);
        }
        int cantidad = datos.length;
        double paso = (double) anchoUtil / Math.max(1, cantidad);
        double anchoBarra = paso * 0.7;

        StringBuilder svg = iniciarSvg(ancho, alto, titulo);

        // Ejes
        svg.append(linea(numero(margenIzquierdo), numero(margenSuperior),
                numero(margenIzquierdo), numero(margenSuperior + altoUtil), "#cbd5e1"));
        svg.append(linea(numero(margenIzquierdo), numero(margenSuperior + altoUtil),
                numero(ancho - margenDerecho), numero(margenSuperior + altoUtil), "#cbd5e1"));

        for (int i = 0; i < cantidad; i++) {
            double alturaBarra = (datos[i] / maximo) * altoUtil;
            double x = margenIzquierdo + i * paso + (paso - anchoBarra) / 2;
            double y = margenSuperior + altoUtil - alturaBarra;
            String etiqueta = i < etiquetas.length ? etiquetas[i] : ("#" + i);

            // La barra, con title para el tooltip al pasar el mouse.
            svg.append("<rect x='").append(numero(x))
                    .append("' y='").append(numero(y))
                    .append("' width='").append(numero(anchoBarra))
                    .append("' height='").append(numero(alturaBarra))
                    .append("' fill='").append(color).append("'>")
                    .append("<title>").append(escapar(etiqueta)).append(": ")
                    .append(datos[i]).append("</title></rect>");

            // El valor encima de la barra.
            svg.append(texto(numero(x + anchoBarra / 2), numero(y - 6),
                    String.valueOf(datos[i]), 12, "#1c2733", "middle"));

            // La etiqueta de la categoria (rotada para que no se encimene).
            svg.append("<text transform='translate(").append(numero(x + anchoBarra / 2))
                    .append(",").append(margenSuperior + altoUtil + 12)
                    .append(") rotate(-35)' font-family='Arial' font-size='11' ")
                    .append("fill='#1c2733' text-anchor='end'>")
                    .append(escapar(etiqueta)).append("</text>");
        }
        svg.append("</svg>");
        return contenedorConSvg(svg.toString(), titulo);
    }

    // ------------------------------------------------------------------------
    // torta(): grafico circular de proporciones.
    //   datos    : valores numericos
    //   etiquetas: nombres de las categorias
    //   colores  : color de cada segmento (se repiten si no alcanzan)
    //   titulo   : titulo del grafico
    // ------------------------------------------------------------------------
    public static Component torta(String id, int[] datos, String[] etiquetas,
            String[] colores, String titulo) {
        final int ancho = 460;
        final int alto = 320;
        final double centroX = 135;
        final double centroY = 160;
        final double radio = 95;
        final double grosor = 46;
        final double circunferencia = 2 * Math.PI * radio;

        double total = 0;
        for (int dato : datos) {
            total = total + dato;
        }
        if (total == 0) {
            total = 1;
        }

        StringBuilder svg = iniciarSvg(ancho, alto, titulo);

        // Cada segmento es un circulo con dasharray (el truco estandar para
        // hacer arcos en SVG sin calcular coordenadas de curva).
        double acumulado = 0;
        for (int i = 0; i < datos.length; i++) {
            double largoSegmento = (datos[i] / total) * circunferencia;
            String colorSegmento = colores[i % colores.length];
            String etiqueta = i < etiquetas.length ? etiquetas[i] : ("#" + i);
            double porcentaje = Math.round(1000.0 * datos[i] / total) / 10.0;

            svg.append("<circle cx='").append(numero(centroX))
                    .append("' cy='").append(numero(centroY))
                    .append("' r='").append(numero(radio))
                    .append("' fill='none' stroke='").append(colorSegmento)
                    .append("' stroke-width='").append(numero(grosor))
                    .append("' stroke-dasharray='").append(numero(largoSegmento))
                    .append(" ").append(numero(circunferencia - largoSegmento))
                    .append("' stroke-dashoffset='").append(numero(-acumulado))
                    .append("' transform='rotate(-90 ")
                    .append(numero(centroX)).append(" ").append(numero(centroY)).append(")'>")
                    .append("<title>").append(escapar(etiqueta)).append(": ")
                    .append(datos[i]).append(" (").append(numero(porcentaje))
                    .append(" pct)</title></circle>");
            acumulado = acumulado + largoSegmento;
        }

        // Leyenda a la derecha.
        double y = 45;
        for (int i = 0; i < datos.length; i++) {
            String etiqueta = i < etiquetas.length ? etiquetas[i] : ("#" + i);
            double porcentaje = Math.round(1000.0 * datos[i] / total) / 10.0;
            svg.append("<rect x='265' y='").append(numero(y - 11))
                    .append("' width='13' height='13' fill='")
                    .append(colores[i % colores.length]).append("'/>");
            svg.append(texto("278", numero(y), etiqueta + "  " + numero(porcentaje) + "%",
                    12, "#1c2733", "start"));
            y = y + 22;
        }
        svg.append("</svg>");
        return contenedorConSvg(svg.toString(), titulo);
    }

    // ------------------------------------------------------------------------
    // mapaCalor(): matriz de calor de una tabla de contingencia.
    //   obs  : matriz de conteos [filas][columnas]
    //   filas: nombres de las filas
    //   cols : nombres de las columnas
    // ------------------------------------------------------------------------
    public static Component mapaCalor(String id, int[][] obs, String[] filas,
            String[] cols, String titulo) {
        final int ancho = 780;
        final int alto = 330;
        final int margenIzquierdo = 165;
        final int margenSuperior = 35;

        int maximo = 1;
        for (int[] fila : obs) {
            for (int valor : fila) {
                maximo = Math.max(maximo, valor);
            }
        }
        int cantidadFilas = Math.max(1, filas.length);
        int cantidadColumnas = Math.max(1, cols.length);
        int anchoUtil = ancho - margenIzquierdo - 20;
        int altoUtil = alto - margenSuperior - 60;
        double anchoCelda = (double) anchoUtil / cantidadColumnas;
        double altoCelda = (double) altoUtil / cantidadFilas;

        StringBuilder svg = iniciarSvg(ancho, alto, titulo);

        for (int f = 0; f < obs.length; f++) {
            for (int c = 0; c < obs[f].length; c++) {
                double intensidad = obs[f][c] / (double) maximo;
                int rojo = (int) Math.round(11 + intensidad * 200);
                int verde = (int) Math.round(61 + intensidad * 100);
                int azul = (int) Math.round(98 + intensidad * 60);
                String colorCelda = "rgb(" + rojo + "," + verde + "," + azul + ")";
                String colorTexto = intensidad > 0.5 ? "#ffffff" : "#1c2733";
                String nombreFila = f < filas.length ? filas[f] : ("#" + f);
                String nombreColumna = c < cols.length ? cols[c] : ("#" + c);

                svg.append("<rect x='").append(numero(margenIzquierdo + c * anchoCelda))
                        .append("' y='").append(numero(margenSuperior + f * altoCelda))
                        .append("' width='").append(numero(anchoCelda - 3))
                        .append("' height='").append(numero(altoCelda - 4))
                        .append("' fill='").append(colorCelda).append("'>")
                        .append("<title>").append(escapar(nombreFila)).append(" / ")
                        .append(escapar(nombreColumna)).append(": ")
                        .append(obs[f][c]).append("</title></rect>");
                svg.append(texto(
                        numero(margenIzquierdo + c * anchoCelda + anchoCelda / 2 - 1),
                        numero(margenSuperior + f * altoCelda + altoCelda / 2 + 4),
                        String.valueOf(obs[f][c]), 12, colorTexto, "middle"));
            }
        }

        // Etiquetas de las filas (a la izquierda).
        for (int f = 0; f < filas.length; f++) {
            svg.append(texto("6", numero(margenSuperior + f * altoCelda + altoCelda / 2 + 4),
                    filas[f], 12, "#1c2733", "start"));
        }

        // Etiquetas de las columnas (abajo, rotadas).
        for (int c = 0; c < cols.length; c++) {
            svg.append("<text transform='translate(")
                    .append(numero(margenIzquierdo + c * anchoCelda + anchoCelda / 2))
                    .append(",").append(margenSuperior + altoUtil + 12)
                    .append(") rotate(-40)' font-family='Arial' font-size='11' ")
                    .append("fill='#1c2733' text-anchor='end'>")
                    .append(escapar(cols[c])).append("</text>");
        }
        svg.append("</svg>");
        return contenedorConSvg(svg.toString(), titulo);
    }

    // ------------------------------------------------------------------------
    // radar(): diagrama de radar para puntajes de 1 a 4 (los 10 items Likert
    // de la encuesta a empleadores, pedido por la HU5).
    //   valores  : puntajes (uno por categoria)
    //   etiquetas: nombres de las categorias
    // ------------------------------------------------------------------------
    public static Component radar(String id, double[] valores, String[] etiquetas,
            String titulo) {
        final int ancho = 480;
        final int alto = 360;
        final double centroX = 235;
        final double centroY = 175;
        final double radio = 110;

        StringBuilder svg = iniciarSvg(ancho, alto, titulo);
        int cantidad = valores.length;
        if (cantidad < 3) {
            svg.append(texto("20", "60", "Se necesitan al menos 3 categorias", 13,
                    "#888888", "start"));
            svg.append("</svg>");
            return contenedorConSvg(svg.toString(), titulo);
        }

        // Anillos (uno por nivel de la escala 1-4).
        for (int nivel = 1; nivel <= 4; nivel++) {
            StringBuilder puntos = new StringBuilder();
            for (int i = 0; i < cantidad; i++) {
                double angulo = -Math.PI / 2 + i * 2 * Math.PI / cantidad;
                double x = centroX + radio * (nivel / 4.0) * Math.cos(angulo);
                double y = centroY + radio * (nivel / 4.0) * Math.sin(angulo);
                puntos.append(i == 0 ? "M" : "L").append(numero(x)).append(",")
                        .append(numero(y)).append(" ");
            }
            puntos.append("Z");
            svg.append("<path d='").append(puntos)
                    .append("' fill='none' stroke='#dfe6ee' stroke-width='1'/>");
        }

        // Ejes hacia cada categoria.
        for (int i = 0; i < cantidad; i++) {
            double angulo = -Math.PI / 2 + i * 2 * Math.PI / cantidad;
            double x = centroX + radio * Math.cos(angulo);
            double y = centroY + radio * Math.sin(angulo);
            svg.append(linea(numero(centroX), numero(centroY), numero(x), numero(y), "#e8edf3"));
        }

        // Poligono con los datos.
        StringBuilder puntos = new StringBuilder();
        for (int i = 0; i < cantidad; i++) {
            double angulo = -Math.PI / 2 + i * 2 * Math.PI / cantidad;
            double distancia = radio * (valores[i] / 4.0);
            double x = centroX + distancia * Math.cos(angulo);
            double y = centroY + distancia * Math.sin(angulo);
            puntos.append(i == 0 ? "M" : "L").append(numero(x)).append(",")
                    .append(numero(y)).append(" ");
        }
        puntos.append("Z");
        svg.append("<path d='").append(puntos)
                .append("' fill='rgba(9,132,227,0.25)' stroke='#0b3d62' ")
                .append("stroke-width='3'/>");

        // Marcadores y etiquetas de cada eje.
        for (int i = 0; i < cantidad; i++) {
            double angulo = -Math.PI / 2 + i * 2 * Math.PI / cantidad;
            double distancia = radio * (valores[i] / 4.0);
            double x = centroX + distancia * Math.cos(angulo);
            double y = centroY + distancia * Math.sin(angulo);
            String etiqueta = i < etiquetas.length ? etiquetas[i] : ("#" + i);
            svg.append("<circle cx='").append(numero(x)).append("' cy='")
                    .append(numero(y)).append("' r='4' fill='#0b3d62'>")
                    .append("<title>").append(escapar(etiqueta)).append(": ")
                    .append(numero(Math.round(valores[i] * 100.0) / 100.0))
                    .append(" de 4</title></circle>");
            double xEtiqueta = centroX + radio * 1.22 * Math.cos(angulo);
            double yEtiqueta = centroY + radio * 1.22 * Math.sin(angulo);
            svg.append(texto(numero(xEtiqueta - 30), numero(yEtiqueta + 4),
                    etiqueta, 11, "#1c2733", "middle"));
        }
        svg.append("</svg>");
        return contenedorConSvg(svg.toString(), titulo);
    }

    // ------------------------------------------------------------------------
    // enTarjeta(): envuelve un grafico en una tarjeta blanca con titulo.
    // ------------------------------------------------------------------------
    public static Component enTarjeta(Component contenido, String tituloTarjeta) {
        VerticalLayout tarjeta = new VerticalLayout(contenido);
        tarjeta.getStyle().set("background", "#ffffff")
                .set("border-radius", "10px")
                .set("padding", "16px")
                .set("box-shadow", "0 1px 4px rgba(0,0,0,.12)")
                .set("width", "100%");
        if (tituloTarjeta != null && !tituloTarjeta.isEmpty()) {
            Span titulo = new Span(tituloTarjeta);
            titulo.getStyle().set("font-weight", "bold").set("font-size", "15px")
                    .set("margin-bottom", "10px").set("display", "block");
            tarjeta.addComponentAtIndex(0, titulo);
        }
        return tarjeta;
    }

    // ------------------------------------------------------------------------
    // Helpers privados (construccion del SVG).
    // ------------------------------------------------------------------------

    // iniciarSvg(): devuelve el StringBuilder con la cabecera del SVG
    // (viewBox responsivo + titulo). Se sigue con .append(...) y al final
    // se pasa a contenedorConSvg(svg.toString(), titulo).
    private static StringBuilder iniciarSvg(int ancho, int alto, String titulo) {
        return new StringBuilder()
                .append("<svg viewBox='0 0 ").append(ancho).append(" ").append(alto)
                .append("' preserveAspectRatio='xMidYMid meet' ")
                .append("style='width:100%;height:auto;background:#ffffff' ")
                .append("role='img' aria-label='").append(escapar(titulo)).append("'>")
                .append("<text x='12' y='24' font-family='Arial' font-size='15' ")
                .append("fill='#1c2733'>").append(escapar(titulo)).append("</text>");
    }

    private static String linea(String x1, String y1, String x2, String y2, String color) {
        return "<line x1='" + x1 + "' y1='" + y1 + "' x2='" + x2
                + "' y2='" + y2 + "' stroke='" + color + "' stroke-width='1'/>";
    }

    private static String texto(String x, String y, String contenido, int tamano,
            String color, String ancla) {
        return "<text x='" + x + "' y='" + y
                + "' font-family='Arial' font-size='" + tamano + "' fill='" + color
                + "' text-anchor='" + ancla + "'>" + escapar(contenido) + "</text>";
    }

    // contenedorConSvg(): inserta el SVG como HTML dentro de un Div.
    //
    // POR QUE HTML INLINE Y NO <img> CON DATA URI:
    //   el Data URI travela en el atributo src de un <img>. En produccion
    //   (Render) esa imagen llega rota y el navegador muestra el icono de
    //   "imagen no disponible" con el texto alternativo.
    //   Con innerHTML el SVG es parte del DOM (como cualquier texto), asi
    //   que el navegador lo dibuja siempre. Ademas los <title> internos
    //   siguen funcionando como tooltips al pasar el mouse.
    // El executeJs se ejecuta cuando el Div ya esta en el navegador, y
    // "this" es el propio Div, asi que no hay problemas de sincronismo.
    private static Component contenedorConSvg(String svg, String textoAlternativo) {
        Div contenedor = new Div();
        contenedor.setId("grafico-" + textoAlternativo.hashCode());
        contenedor.getStyle().set("width", "100%")
                .set("max-width", "820px")
                .set("margin", "10px 0")
                .set("overflow-x", "auto");
        contenedor.getElement().executeJs("this.innerHTML = $0", svg);
        return contenedor;
    }

    // numero(): double -> texto con maximo 2 decimales (sin String.format,
    // para no volver a tropezar con el simbolo %).
    private static String numero(double valor) {
        return String.valueOf(Math.round(valor * 100.0) / 100.0);
    }

    // escapar(): texto -> texto seguro dentro de XML/SVG.
    private static String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
