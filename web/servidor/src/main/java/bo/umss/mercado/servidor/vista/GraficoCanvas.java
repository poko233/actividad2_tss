package bo.umss.mercado.servidor.vista;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;

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
 * - barrasVerticales(): histogramas y frecuencias.
 * - torta(): proporciones (pie chart con segmentos circulares).
 * - mapaCalor(): tablas de contingencia coloreadas por intensidad.
 * - radar(): los 10 items Likert de la encuesta a empleadores.
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
        // id : identificador del grafico (accesibilidad)
        // datos : valores numericos (uno por categoria)
        // etiquetas : nombres de las categorias
        // color : color de las barras
        // titulo : titulo del grafico
        // ------------------------------------------------------------------------
        public static Component barrasVerticales(String id, int[] datos, String[] etiquetas,
                        String color, String titulo) {
                final int ancho = 800;
                final int alto = 420;
                final int margenIzquierdo = 60;
                final int margenDerecho = 15;
                final int margenSuperior = 40;
                final int margenInferior = 140;
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

                        svg.append("<rect x='").append(numero(x))
                                        .append("' y='").append(numero(y))
                                        .append("' width='").append(numero(anchoBarra))
                                        .append("' height='").append(numero(alturaBarra))
                                        .append("' fill='").append(color).append("'>")
                                        .append("<title>").append(escapar(etiqueta)).append(": ")
                                        .append(datos[i]).append("</title></rect>");

                        svg.append(texto(numero(x + anchoBarra / 2), numero(y - 6),
                                        String.valueOf(datos[i]), 12, "#1c2733", "middle"));

                        svg.append("<text transform='translate(").append(numero(x + anchoBarra / 2))
                                        .append(",").append(margenSuperior + altoUtil + 16)
                                        .append(") rotate(-35)' font-family='Arial' font-size='11' ")
                                        .append("fill='#1c2733' text-anchor='end'>")
                                        .append(escapar(etiqueta)).append("</text>");
                }
                svg.append("</svg>");
                return contenedorConSvg(svg.toString(), titulo);
        }

        // ------------------------------------------------------------------------
        // torta(): grafico circular de proporciones.
        // datos : valores numericos
        // etiquetas: nombres de las categorias
        // colores : color de cada segmento (se repiten si no alcanzan)
        // titulo : titulo del grafico
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
        // obs : matriz de conteos [filas][columnas]
        // filas: nombres de las filas
        // cols : nombres de las columnas
        // ------------------------------------------------------------------------
        public static Component mapaCalor(String id, int[][] obs, String[] filas,
                        String[] cols, String titulo) {
                final int ancho = 1000;
                final int alto = 500;
                final int margenIzquierdo = 200;
                final int margenSuperior = 40;
                final int margenInferior = 170;

                int maximo = 1;
                for (int[] fila : obs) {
                        for (int valor : fila) {
                                maximo = Math.max(maximo, valor);
                        }
                }
                int cantidadFilas = Math.max(1, filas.length);
                int cantidadColumnas = Math.max(1, cols.length);
                int anchoUtil = ancho - margenIzquierdo - 30;
                int altoUtil = alto - margenSuperior - margenInferior;
                double anchoCelda = (double) anchoUtil / cantidadColumnas;
                double altoCelda = (double) altoUtil / cantidadFilas;

                int[][] paleta = {
                                { 247, 251, 255 },
                                { 222, 235, 247 },
                                { 198, 219, 239 },
                                { 158, 202, 225 },
                                { 107, 174, 214 },
                                { 66, 146, 198 },
                                { 33, 113, 181 },
                                { 8, 81, 156 },
                                { 8, 48, 107 }
                };

                StringBuilder svg = iniciarSvg(ancho, alto, titulo);

                for (int f = 0; f < obs.length; f++) {
                        for (int c = 0; c < obs[f].length; c++) {
                                double intensidad = maximo == 0 ? 0 : (obs[f][c] / (double) maximo);
                                int idx = Math.min(paleta.length - 1,
                                                (int) Math.round(intensidad * (paleta.length - 1)));
                                int rojo = paleta[idx][0];
                                int verde = paleta[idx][1];
                                int azul = paleta[idx][2];
                                String colorCelda = "rgb(" + rojo + "," + verde + "," + azul + ")";
                                String colorTexto = idx >= 5 ? "#ffffff" : "#1c2733";

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
                                                numero(margenIzquierdo + c * anchoCelda + anchoCelda / 2),
                                                numero(margenSuperior + f * altoCelda + altoCelda / 2 + 6),
                                                String.valueOf(obs[f][c]), 15, colorTexto, "middle"));
                        }
                }

                for (int f = 0; f < filas.length; f++) {
                        svg.append("<text x='").append(margenIzquierdo - 12)
                                        .append("' y='")
                                        .append(numero(margenSuperior + f * altoCelda + altoCelda / 2 + 5))
                                        .append("' font-family='Arial' font-size='13' fill='#1c2733' ")
                                        .append("text-anchor='end'>").append(escapar(filas[f])).append("</text>");
                }

                for (int c = 0; c < cols.length; c++) {
                        svg.append("<text transform='translate(")
                                        .append(numero(margenIzquierdo + c * anchoCelda + anchoCelda / 2))
                                        .append(",").append(margenSuperior + altoUtil + 20)
                                        .append(") rotate(-35)' font-family='Arial' font-size='12' ")
                                        .append("fill='#1c2733' text-anchor='end'>")
                                        .append(escapar(cols[c])).append("</text>");
                }
                svg.append("</svg>");
                return contenedorConSvg(svg.toString(), titulo);
        }

        // ------------------------------------------------------------------------
        // radar(): diagrama de radar para puntajes de 1 a 4 (los 10 items Likert
        // de la encuesta a empleadores, pedido por la HU5).
        // valores : puntajes (uno por categoria)
        // etiquetas: nombres de las categorias
        // ------------------------------------------------------------------------
        public static Component radar(String id, double[] valores, String[] etiquetas, String titulo) {
                return radarConEscala(id, valores, etiquetas, 4, titulo);
        }

        public static Component radarConEscala(String id, double[] valores, String[] etiquetas,
                        int escalaMaxima, String titulo) {
                final int ancho = 480;
                final int max = Math.max(2, escalaMaxima);
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
                for (int nivel = 1; nivel <= max; nivel++) {
                        StringBuilder puntos = new StringBuilder();
                        for (int i = 0; i < cantidad; i++) {
                                double angulo = -Math.PI / 2 + i * 2 * Math.PI / cantidad;
                                double x = centroX + radio * (nivel / (double) max) * Math.cos(angulo);
                                double y = centroY + radio * (nivel / (double) max) * Math.sin(angulo);
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
                        double distancia = radio * (valores[i] / (double) max);
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
                        double distancia = radio * (valores[i] / (double) max);
                        double x = centroX + distancia * Math.cos(angulo);
                        double y = centroY + distancia * Math.sin(angulo);
                        String etiqueta = i < etiquetas.length ? etiquetas[i] : ("#" + i);
                        svg.append("<circle cx='").append(numero(x)).append("' cy='")
                                        .append(numero(y)).append("' r='4' fill='#0b3d62'>")
                                        .append("<title>").append(escapar(etiqueta)).append(": ")
                                        .append(numero(Math.round(valores[i] * 100.0) / 100.0))
                                        .append(" de ").append(max).append("</title></circle>");
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
        public record PuntoDispersion(String categoriaX, String categoriaY,
                        double valorY, int tamano) {
        }

        public static Component dispersionCategorizada(
                        String id,
                        List<String> xCategorias,
                        List<String> yCategorias,
                        List<PuntoDispersion> puntos,
                        String[] coloresY,
                        String tituloX, String tituloY, String titulo) {

                final int ancho = 900;
                final int alto = 520;
                final int margenIzq = 90;
                final int margenDer = 240;
                final int margenSup = 50;
                final int margenInf = 160;

                final int anchoUtil = ancho - margenIzq - margenDer;
                final int altoUtil = alto - margenSup - margenInf;

                double minZraw = Double.MAX_VALUE, maxZraw = -Double.MAX_VALUE;
                int maxN = 1;
                for (var p : puntos) {
                        minZraw = Math.min(minZraw, p.valorY());
                        maxZraw = Math.max(maxZraw, p.valorY());
                        maxN = Math.max(maxN, p.tamano());
                }
                if (maxZraw == minZraw)
                        maxZraw = minZraw + 1;

                double minZ, maxZ;
                if (minZraw >= 0) {
                        minZ = 0;
                        maxZ = maxZraw + (maxZraw - minZraw) * 0.1;
                } else {
                        double rangoOrig = maxZraw - minZraw;
                        minZ = minZraw - rangoOrig * 0.1;
                        maxZ = maxZraw + rangoOrig * 0.1;
                }
                if (maxZ == minZ)
                        maxZ = minZ + 1;
                double rangoZ = maxZ - minZ;

                int nX = Math.max(1, xCategorias.size());
                double pasoX = (double) anchoUtil / nX;

                StringBuilder svg = iniciarSvg(ancho, alto, titulo);

                svg.append(linea(numero(margenIzq), numero(margenSup),
                                numero(margenIzq), numero(margenSup + altoUtil), "#cbd5e1"));
                svg.append(linea(numero(margenIzq), numero(margenSup + altoUtil),
                                numero(ancho - margenDer), numero(margenSup + altoUtil), "#cbd5e1"));

                for (int k = 0; k <= 4; k++) {
                        double valorZ = minZ + rangoZ * (k / 4.0);
                        double y = margenSup + altoUtil - ((valorZ - minZ) / rangoZ) * altoUtil;
                        svg.append(linea(numero(margenIzq), numero(y),
                                        numero(ancho - margenDer), numero(y), "#eef2f6"));
                        svg.append(texto(String.valueOf(margenIzq - 8), numero(y + 4),
                                        String.format(java.util.Locale.US, "%.1f", valorZ),
                                        11, "#666", "end"));
                }

                for (int i = 0; i < nX; i++) {
                        double xCentro = margenIzq + pasoX * i + pasoX / 2;
                        svg.append("<text transform='translate(").append(numero(xCentro))
                                        .append(",").append(margenSup + altoUtil + 20)
                                        .append(") rotate(-35)' font-family='Arial' font-size='11' ")
                                        .append("fill='#1c2733' text-anchor='end'>")
                                        .append(escapar(xCategorias.get(i))).append("</text>");
                }

                svg.append("<text x='20' y='").append(margenSup + altoUtil / 2)
                                .append("' transform='rotate(-90 20 ").append(margenSup + altoUtil / 2)
                                .append(")' font-family='Arial' font-size='12' fill='#1c2733' ")
                                .append("text-anchor='middle'>").append(escapar(tituloY)).append("</text>");

                for (var p : puntos) {
                        int idxX = xCategorias.indexOf(p.categoriaX());
                        int idxY = yCategorias.indexOf(p.categoriaY());
                        if (idxX < 0 || idxY < 0)
                                continue;

                        double x = margenIzq + pasoX * idxX + pasoX / 2;
                        double y = margenSup + altoUtil - ((p.valorY() - minZ) / rangoZ) * altoUtil;
                        double radio = 4 + (p.tamano() / (double) maxN) * 16;
                        String color = coloresY[idxY % coloresY.length];

                        svg.append("<circle cx='").append(numero(x))
                                        .append("' cy='").append(numero(y))
                                        .append("' r='").append(numero(radio))
                                        .append("' fill='").append(color).append("' fill-opacity='0.6'")
                                        .append(" stroke='").append(color).append("' stroke-width='2'>")
                                        .append("<title>").append(escapar(p.categoriaX())).append(" / ")
                                        .append(escapar(p.categoriaY())).append(": ")
                                        .append(numero(Math.round(p.valorY() * 100.0) / 100.0))
                                        .append(" (n=").append(p.tamano()).append(")</title></circle>");
                }

                double ly = 60;
                for (int i = 0; i < yCategorias.size(); i++) {
                        svg.append("<circle cx='").append(ancho - margenDer + 25)
                                        .append("' cy='").append(numero(ly - 4))
                                        .append("' r='7' fill='").append(coloresY[i % coloresY.length]).append("'/>");
                        svg.append(texto(String.valueOf(ancho - margenDer + 42), numero(ly),
                                        yCategorias.get(i), 12, "#1c2733", "start"));
                        ly += 24;
                }

                svg.append("</svg>");
                return contenedorConSvg(svg.toString(), titulo);
        }

        public static Component barrasApiladas(
                        String id,
                        String[] categoriasX,
                        String[] categoriasY,
                        int[][] frecuencias,
                        String[] coloresY,
                        String titulo) {

                final int ancho = 900;
                final int alto = 500;
                final int margenIzq = 70;
                final int margenDer = 240;
                final int margenSup = 55;
                final int margenInf = 160;

                final int anchoUtil = ancho - margenIzq - margenDer;
                final int altoUtil = alto - margenSup - margenInf;

                int[] totalesX = new int[categoriasX.length];
                int maxTotal = 1;
                for (int i = 0; i < categoriasX.length; i++) {
                        for (int j = 0; j < categoriasY.length; j++) {
                                totalesX[i] += frecuencias[i][j];
                        }
                        maxTotal = Math.max(maxTotal, totalesX[i]);
                }

                int n = Math.max(1, categoriasX.length);
                double paso = (double) anchoUtil / n;
                double anchoBarra = paso * 0.7;

                StringBuilder svg = iniciarSvg(ancho, alto, titulo);

                // Ejes
                svg.append(linea(numero(margenIzq), numero(margenSup),
                                numero(margenIzq), numero(margenSup + altoUtil), "#cbd5e1"));
                svg.append(linea(numero(margenIzq), numero(margenSup + altoUtil),
                                numero(ancho - margenDer), numero(margenSup + altoUtil), "#cbd5e1"));

                // Gridlines Y + escala
                for (int k = 0; k <= 4; k++) {
                        double valor = maxTotal * k / 4.0;
                        double y = margenSup + altoUtil - (valor / maxTotal) * altoUtil;
                        svg.append(linea(numero(margenIzq), numero(y),
                                        numero(ancho - margenDer), numero(y), "#eef2f6"));
                        svg.append(texto(String.valueOf(margenIzq - 8), numero(y + 4),
                                        String.valueOf(Math.round(valor)), 11, "#666", "end"));
                }

                // Título eje Y
                svg.append("<text x='22' y='").append(margenSup + altoUtil / 2)
                                .append("' transform='rotate(-90 22 ").append(margenSup + altoUtil / 2)
                                .append(")' font-family='Arial' font-size='12' fill='#1c2733' ")
                                .append("text-anchor='middle'>Frecuencia (n)</text>");

                // Barras apiladas
                for (int i = 0; i < n; i++) {
                        // ¿Cuántos segmentos no-cero tiene esta barra?
                        int segmentosNoCero = 0;
                        for (int j = 0; j < categoriasY.length; j++) {
                                if (frecuencias[i][j] > 0)
                                        segmentosNoCero++;
                        }
                        boolean esSegmentoUnico = (segmentosNoCero == 1);

                        double x = margenIzq + i * paso + (paso - anchoBarra) / 2;
                        double yActual = margenSup + altoUtil;

                        for (int j = 0; j < categoriasY.length; j++) {
                                int valor = frecuencias[i][j];
                                if (valor == 0)
                                        continue;
                                double alturaSeg = (valor / (double) maxTotal) * altoUtil;
                                yActual -= alturaSeg;
                                String color = coloresY[j % coloresY.length];

                                svg.append("<rect x='").append(numero(x))
                                                .append("' y='").append(numero(yActual))
                                                .append("' width='").append(numero(anchoBarra))
                                                .append("' height='").append(numero(alturaSeg))
                                                .append("' fill='").append(color).append("'>")
                                                .append("<title>").append(escapar(categoriasX[i]))
                                                .append(" / ").append(escapar(categoriasY[j]))
                                                .append(": ").append(valor).append("</title></rect>");

                                // Solo dibuja la etiqueta del segmento si hay 2+ segmentos.
                                // Si hay uno solo, el total ya dice el valor.
                                if (!esSegmentoUnico) {
                                        String valorTexto = String.valueOf(valor);
                                        if (alturaSeg >= 18) {
                                                // Blanco dentro del segmento, centrado
                                                svg.append(texto(numero(x + anchoBarra / 2),
                                                                numero(yActual + alturaSeg / 2 + 4),
                                                                valorTexto, 12, "#ffffff", "middle"));
                                        } else {
                                                // Segmento pequeño: número afuera con línea conectora
                                                double yCentro = yActual + alturaSeg / 2;
                                                double xLineaIni = x + anchoBarra;
                                                double xLineaFin = xLineaIni + 6;
                                                double xTexto = xLineaFin + 4;
                                                svg.append(linea(numero(xLineaIni), numero(yCentro),
                                                                numero(xLineaFin), numero(yCentro), "#666"));
                                                svg.append(texto(numero(xTexto), numero(yCentro + 3),
                                                                valorTexto, 10, "#1c2733", "start"));
                                        }
                                }
                        }

                        // Total encima de la barra
                        if (totalesX[i] > 0) {
                                double yTotal = margenSup + altoUtil
                                                - (totalesX[i] / (double) maxTotal) * altoUtil;
                                svg.append(texto(numero(x + anchoBarra / 2), numero(yTotal - 6),
                                                String.valueOf(totalesX[i]), 12, "#1c2733", "middle"));
                        }

                        // Etiqueta X
                        svg.append("<text transform='translate(").append(numero(x + anchoBarra / 2))
                                        .append(",").append(margenSup + altoUtil + 18)
                                        .append(") rotate(-35)' font-family='Arial' font-size='11' ")
                                        .append("fill='#1c2733' text-anchor='end'>")
                                        .append(escapar(categoriasX[i])).append("</text>");
                }

                // Leyenda Y (derecha)
                double ly = margenSup + 20;
                for (int j = 0; j < categoriasY.length; j++) {
                        svg.append("<rect x='").append(ancho - margenDer + 20)
                                        .append("' y='").append(numero(ly - 11))
                                        .append("' width='14' height='14' fill='")
                                        .append(coloresY[j % coloresY.length]).append("'/>");
                        svg.append(texto(String.valueOf(ancho - margenDer + 40), numero(ly),
                                        categoriasY[j], 12, "#1c2733", "start"));
                        ly += 24;
                }

                svg.append("</svg>");
                return contenedorConSvg(svg.toString(), titulo);
        }

        public static Component barrasAgrupadas(
                        String id,
                        String[] categoriasX,
                        String[] categoriasY,
                        int[][] frecuencias,
                        String[] coloresY,
                        String titulo) {

                final int ancho = 900;
                final int alto = 480;
                final int margenIzq = 70;
                final int margenDer = 240;
                final int margenSup = 60;
                final int margenInf = 120;

                final int anchoUtil = ancho - margenIzq - margenDer;
                final int altoUtil = alto - margenSup - margenInf;

                int[] totalesX = new int[categoriasX.length];
                int maxValor = 1;
                for (int i = 0; i < categoriasX.length; i++) {
                        for (int j = 0; j < categoriasY.length; j++) {
                                totalesX[i] += frecuencias[i][j];
                                maxValor = Math.max(maxValor, frecuencias[i][j]);
                        }
                }

                int n = Math.max(1, categoriasX.length);
                int yCount = Math.max(1, categoriasY.length);
                double paso = (double) anchoUtil / n;
                double anchoGrupo = paso * 0.85;
                double gapInterno = 3;
                double anchoBarra = Math.max(4, (anchoGrupo - gapInterno * (yCount - 1)) / yCount);

                StringBuilder svg = iniciarSvg(ancho, alto, titulo);

                svg.append(linea(numero(margenIzq), numero(margenSup),
                                numero(margenIzq), numero(margenSup + altoUtil), "#cbd5e1"));
                svg.append(linea(numero(margenIzq), numero(margenSup + altoUtil),
                                numero(ancho - margenDer), numero(margenSup + altoUtil), "#cbd5e1"));

                for (int k = 0; k <= 4; k++) {
                        double valor = maxValor * k / 4.0;
                        double y = margenSup + altoUtil - (valor / maxValor) * altoUtil;
                        svg.append(linea(numero(margenIzq), numero(y),
                                        numero(ancho - margenDer), numero(y), "#eef2f6"));
                        svg.append(texto(String.valueOf(margenIzq - 8), numero(y + 4),
                                        String.valueOf(Math.round(valor)), 11, "#666", "end"));
                }

                svg.append("<text x='22' y='").append(margenSup + altoUtil / 2)
                                .append("' transform='rotate(-90 22 ").append(margenSup + altoUtil / 2)
                                .append(")' font-family='Arial' font-size='12' fill='#1c2733' ")
                                .append("text-anchor='middle'>Frecuencia (n)</text>");

                for (int i = 0; i < n; i++) {
                        double xBase = margenIzq + i * paso + (paso - anchoGrupo) / 2;

                        for (int j = 0; j < yCount; j++) {
                                int valor = frecuencias[i][j];
                                if (valor == 0)
                                        continue;
                                double alturaBarra = (valor / (double) maxValor) * altoUtil;
                                double x = xBase + j * (anchoBarra + gapInterno);
                                double y = margenSup + altoUtil - alturaBarra;
                                String color = coloresY[j % coloresY.length];

                                svg.append("<rect x='").append(numero(x))
                                                .append("' y='").append(numero(y))
                                                .append("' width='").append(numero(anchoBarra))
                                                .append("' height='").append(numero(alturaBarra))
                                                .append("' fill='").append(color).append("'>")
                                                .append("<title>").append(escapar(categoriasX[i]))
                                                .append(" / ").append(escapar(categoriasY[j]))
                                                .append(": ").append(valor).append("</title></rect>");
                        }

                        if (totalesX[i] > 0) {
                                double yTotal = margenSup + altoUtil
                                                - (totalesX[i] / (double) maxValor) * altoUtil;
                                svg.append(texto(numero(xBase + anchoGrupo / 2), numero(yTotal - 8),
                                                "n=" + totalesX[i], 12, "#1c2733", "middle"));
                        }

                        svg.append("<text transform='translate(").append(numero(xBase + anchoGrupo / 2))
                                        .append(",").append(margenSup + altoUtil + 18)
                                        .append(") rotate(-30)' font-family='Arial' font-size='11' ")
                                        .append("fill='#1c2733' text-anchor='end'>")
                                        .append(escapar(categoriasX[i])).append("</text>");
                }

                double ly = margenSup + 20;
                for (int j = 0; j < yCount; j++) {
                        svg.append("<rect x='").append(ancho - margenDer + 20)
                                        .append("' y='").append(numero(ly - 11))
                                        .append("' width='14' height='14' fill='")
                                        .append(coloresY[j % coloresY.length]).append("'/>");
                        svg.append(texto(String.valueOf(ancho - margenDer + 40), numero(ly),
                                        categoriasY[j], 12, "#1c2733", "start"));
                        ly += 24;
                }

                svg.append("</svg>");
                return contenedorConSvg(svg.toString(), titulo);
        }

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
        // el Data URI travela en el atributo src de un <img>. En produccion
        // (Render) esa imagen llega rota y el navegador muestra el icono de
        // "imagen no disponible" con el texto alternativo.
        // Con innerHTML el SVG es parte del DOM (como cualquier texto), asi
        // que el navegador lo dibuja siempre. Ademas los <title> internos
        // siguen funcionando como tooltips al pasar el mouse.
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
