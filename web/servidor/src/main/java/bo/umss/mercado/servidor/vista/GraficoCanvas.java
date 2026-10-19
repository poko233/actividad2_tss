package bo.umss.mercado.servidor.vista;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * GraficoCanvas: utilitario para dibujar gráficos estadísticos con canvas puro
 * (sin librerías externas como Chart.js).
 *
 * POR QUÉ SE USA ".replace()" Y NO "String.formatted()":
 * el código JavaScript contiene el operador módulo (%) —por ejemplo
 * "colores[i % colores.length]"—. Si ese texto pasara por String.format(),
 * Java lo interpretaría como un especificador de formato y lanzaría
 * FormatFlagsConversionMismatchException. Por eso aquí los valores se
 * inyectan con marcadores @@NOMBRE@@ y luego se reemplazan con
 * String.replace(), que no interpreta nada.
 *
 * Cada método devuelve un Componente de Vaadin listo para insertar en una vista:
 * crea un <div> con el <canvas> y ejecuta el JavaScript al adjuntarse.
 */
public class GraficoCanvas {

    // Paleta simple y legible para todos los gráficos.
    public static final String COLOR_PRINCIPAL = "#0b3d62";
    public static final String COLOR_SECUNDARIO = "#0984e3";
    public static final String COLOR_VERDE = "#27ae60";
    public static final String COLOR_NARANJA = "#e17055";
    public static final String COLOR_MORADO = "#6c5ce7";
    public static final String COLOR_GRIS = "#b2bec3";

    // Paleta para tortas (se repiten si hay más categorías que colores).
    public static final String[] COLORES_TORTA = {
            COLOR_PRINCIPAL, COLOR_SECUNDARIO, COLOR_VERDE,
            COLOR_NARANJA, COLOR_MORADO, COLOR_GRIS
    };

    // ------------------------------------------------------------------------
    // barrasVerticales(): gráfico de barras verticales (histogramas, frecuencias).
    //   id        : identificador único del canvas en el DOM
    //   datos     : valores numéricos (uno por categoría)
    //   etiquetas : nombres de las categorías
    //   color     : color de las barras
    //   titulo    : título que se dibuja dentro del gráfico
    // ------------------------------------------------------------------------
    public static Component barrasVerticales(String id, int[] datos, String[] etiquetas,
            String color, String titulo) {
        Div contenedor = new Div();
        contenedor.setId(id);
        contenedor.getStyle().set("width", "100%").set("max-width", "760px")
                .set("margin", "10px 0");

        Span lblTitulo = new Span(titulo);
        lblTitulo.getStyle().set("font-weight", "bold").set("font-size", "16px")
                .set("margin-bottom", "8px").set("display", "block");

        Div canvasWrap = new Div();
        canvasWrap.setId(id + "-canvas");
        canvasWrap.getStyle().set("width", "100%").set("height", "300px");

        contenedor.add(lblTitulo, canvasWrap);

        // Script con marcadores @@@@ (Java NO interpreta % dentro de él).
        String script = """
                (function(){
                  var c = document.getElementById('@@ID@@');
                  if(!c) return;
                  var g = c.getContext('2d');
                  var w = c.parentElement.clientWidth || 700;
                  var h = 300;
                  c.width = w; c.height = h;
                  g.fillStyle = '#ffffff'; g.fillRect(0,0,w,h);
                  var datos = @@DATOS@@;
                  var etiquetas = @@ETIQUETAS@@;
                  var color = '@@COLOR@@';
                  var titulo = '@@TITULO@@';
                  var mx = Math.max.apply(null, datos.concat([1]));
                  var n = datos.length;
                  var bw = (w - 90) / (n * 1.5);
                  for(var i=0;i<n;i++){
                    var bh = (h - 60) * (datos[i]/mx);
                    var x = 50 + i * 1.5 * bw;
                    var y = h - 40 - bh;
                    g.fillStyle = color; g.fillRect(x, y, bw - 4, bh);
                    g.fillStyle = '#1c2733'; g.font = '14px Arial';
                    g.fillText(String(datos[i]), x, y - 6);
                    g.save(); g.translate(x + bw/2, h - 25); g.rotate(-Math.PI/6);
                    g.fillText(etiquetas[i], 5, 5); g.restore();
                  }
                  g.fillStyle = '#1c2733'; g.font = '16px Arial';
                  g.fillText(titulo, 10, 30);
                })();
                """;

        script = script
                .replace("@@ID@@", id + "-canvas")
                .replace("@@DATOS@@", arrayToJs(datos))
                .replace("@@ETIQUETAS@@", arrayStringsToJs(etiquetas))
                .replace("@@COLOR@@", color)
                .replace("@@TITULO@@", titulo);

        return adjuntarScript(contenedor, script);
    }

    // ------------------------------------------------------------------------
    // torta(): gráfico circular (pie chart) con leyenda.
    //   datos    : valores numéricos
    //   etiquetas: nombres de las categorías
    //   colores  : color de cada segmento (se repiten si no alcanzan)
    //   titulo   : título del gráfico
    // ------------------------------------------------------------------------
    public static Component torta(String id, int[] datos, String[] etiquetas,
            String[] colores, String titulo) {
        Div contenedor = new Div();
        contenedor.setId(id);
        contenedor.getStyle().set("width", "100%").set("max-width", "460px")
                .set("margin", "10px 0");

        Span lblTitulo = new Span(titulo);
        lblTitulo.getStyle().set("font-weight", "bold").set("font-size", "16px")
                .set("margin-bottom", "8px").set("display", "block");

        Div canvasWrap = new Div();
        canvasWrap.setId(id + "-canvas");
        canvasWrap.getStyle().set("width", "100%").set("height", "300px");

        contenedor.add(lblTitulo, canvasWrap);

        String script = """
                (function(){
                  var c = document.getElementById('@@ID@@');
                  if(!c) return;
                  var g = c.getContext('2d');
                  var w = c.parentElement.clientWidth || 400;
                  var h = 300;
                  c.width = w; c.height = h;
                  g.fillStyle = '#ffffff'; g.fillRect(0,0,w,h);
                  var datos = @@DATOS@@;
                  var etiquetas = @@ETIQUETAS@@;
                  var colores = @@COLORES@@;
                  var titulo = '@@TITULO@@';
                  var total = 0;
                  for(var i=0;i<datos.length;i++){ total = total + datos[i]; }
                  if(total === 0){ total = 1; }
                  var cx = w/2, cy = h/2 - 10, R = Math.min(w,h)/2 - 45;
                  var ang = -Math.PI/2;
                  for(var i=0;i<datos.length;i++){
                    var a2 = ang + 2*Math.PI*(datos[i]/total);
                    g.beginPath(); g.moveTo(cx,cy); g.arc(cx,cy,R,ang,a2); g.closePath();
                    g.fillStyle = colores[i % colores.length]; g.fill();
                    g.strokeStyle = '#ffffff'; g.lineWidth=2; g.stroke();
                    ang = a2;
                  }
                  g.fillStyle = '#1c2733'; g.font = '16px Arial';
                  g.fillText(titulo, 10, 30);
                  var ly = 50;
                  for(var i=0;i<etiquetas.length;i++){
                    g.fillStyle = colores[i % colores.length];
                    g.fillRect(10, ly, 14, 14);
                    g.fillStyle = '#1c2733'; g.font = '14px Arial';
                    g.fillText(etiquetas[i] + ' ' + Math.round(100*datos[i]/total) + ' pct', 30, ly + 12);
                    ly += 22;
                  }
                })();
                """;

        script = script
                .replace("@@ID@@", id + "-canvas")
                .replace("@@DATOS@@", arrayToJs(datos))
                .replace("@@ETIQUETAS@@", arrayStringsToJs(etiquetas))
                .replace("@@COLORES@@", arrayStringsToJs(colores))
                .replace("@@TITULO@@", titulo);

        return adjuntarScript(contenedor, script);
    }

    // ------------------------------------------------------------------------
    // mapaCalor(): matriz de calor (heatmap) de una tabla de contingencia.
    //   obs  : matriz de conteos [filas][columnas]
    //   filas: nombres de las filas
    //   cols : nombres de las columnas
    // ------------------------------------------------------------------------
    public static Component mapaCalor(String id, int[][] obs, String[] filas,
            String[] cols, String titulo) {
        Div contenedor = new Div();
        contenedor.setId(id);
        contenedor.getStyle().set("width", "100%").set("max-width", "820px")
                .set("margin", "10px 0");

        Span lblTitulo = new Span(titulo);
        lblTitulo.getStyle().set("font-weight", "bold").set("font-size", "16px")
                .set("margin-bottom", "8px").set("display", "block");

        Div canvasWrap = new Div();
        canvasWrap.setId(id + "-canvas");
        canvasWrap.getStyle().set("width", "100%").set("height", "320px");

        contenedor.add(lblTitulo, canvasWrap);

        String script = """
                (function(){
                  var c = document.getElementById('@@ID@@');
                  if(!c) return;
                  var g = c.getContext('2d');
                  var w = c.parentElement.clientWidth || 700;
                  var h = 320;
                  c.width = w; c.height = h;
                  g.fillStyle = '#ffffff'; g.fillRect(0,0,w,h);
                  var obs = @@OBS@@;
                  var filas = @@FILAS@@;
                  var cols = @@COLS@@;
                  var titulo = '@@TITULO@@';
                  var f = filas.length, nc = cols.length;
                  var mx = 1;
                  obs.forEach(function(r){ r.forEach(function(v){ if(v > mx) mx = v; }); });
                  var cw = (w - 170)/Math.max(1,nc);
                  var rh = (h - 70)/Math.max(1,f);
                  for(var i=0;i<f;i++){
                    for(var j=0;j<nc;j++){
                      var t = obs[i][j]/mx;
                      var r = Math.round(11 + t*200);
                      var gg = Math.round(61 + t*100);
                      var b = Math.round(98 + t*60);
                      g.fillStyle = 'rgb(' + r + ',' + gg + ',' + b + ')';
                      g.fillRect(160 + j*cw, 10 + i*rh, cw-4, rh-6);
                      g.fillStyle = (t > 0.5) ? '#ffffff' : '#1c2733';
                      g.font = '12px Arial';
                      g.fillText(String(obs[i][j]), 160 + j*cw + 6, 10 + i*rh + rh/2 + 5);
                    }
                  }
                  g.fillStyle = '#1c2733'; g.font = '14px Arial';
                  for(var i=0;i<f;i++){ g.fillText(filas[i], 5, 30 + i*rh + rh/2); }
                  for(var j=0;j<nc;j++){
                    g.save(); g.translate(160 + j*cw + cw/2, h - 12);
                    g.rotate(-Math.PI/4); g.fillText(cols[j], 5, 5); g.restore();
                  }
                  g.fillStyle = '#1c2733'; g.font = '16px Arial';
                  g.fillText(titulo, 10, 30);
                })();
                """;

        script = script
                .replace("@@ID@@", id + "-canvas")
                .replace("@@OBS@@", matrizToJs(obs))
                .replace("@@FILAS@@", arrayStringsToJs(filas))
                .replace("@@COLS@@", arrayStringsToJs(cols))
                .replace("@@TITULO@@", titulo);

        return adjuntarScript(contenedor, script);
    }

    // ------------------------------------------------------------------------
    // radar(): diagrama de radar para puntajes de 1 a 4 (los 10 ítems Likert
    // de la encuesta a empleadores, pedido por la HU5).
    //   valores  : puntajes (uno por categoría)
    //   etiquetas: nombres de las categorías
    // ------------------------------------------------------------------------
    public static Component radar(String id, double[] valores, String[] etiquetas,
            String titulo) {
        Div contenedor = new Div();
        contenedor.setId(id);
        contenedor.getStyle().set("width", "100%").set("max-width", "480px")
                .set("margin", "10px 0");

        Span lblTitulo = new Span(titulo);
        lblTitulo.getStyle().set("font-weight", "bold").set("font-size", "16px")
                .set("margin-bottom", "8px").set("display", "block");

        Div canvasWrap = new Div();
        canvasWrap.setId(id + "-canvas");
        canvasWrap.getStyle().set("width", "100%").set("height", "320px");

        contenedor.add(lblTitulo, canvasWrap);

        String script = """
                (function(){
                  var c = document.getElementById('@@ID@@');
                  if(!c) return;
                  var g = c.getContext('2d');
                  var w = c.parentElement.clientWidth || 400;
                  var h = 320;
                  c.width = w; c.height = h;
                  g.fillStyle = '#ffffff'; g.fillRect(0,0,w,h);
                  var valores = @@VALORES@@;
                  var etiquetas = @@ETIQUETAS@@;
                  var titulo = '@@TITULO@@';
                  var n = valores.length;
                  if(n < 3){ return; }
                  var cx = w/2, cy = h/2 - 15, R = Math.min(w,h)/2 - 55;
                  for(var ring=1; ring<=4; ring++){
                    g.beginPath(); g.strokeStyle = '#dfe6ee'; g.lineWidth = 1;
                    for(var i=0;i<=n;i++){
                      var an = -Math.PI/2 + i*2*Math.PI/n;
                      var x = cx + R*(ring/4)*Math.cos(an);
                      var y = cy + R*(ring/4)*Math.sin(an);
                      if(i===0){ g.moveTo(x,y); } else { g.lineTo(x,y); }
                    } g.stroke();
                  }
                  g.beginPath(); g.strokeStyle = '#0b3d62'; g.lineWidth = 3;
                  g.fillStyle = 'rgba(9,132,227,0.25)';
                  for(var i=0;i<n;i++){
                    var an = -Math.PI/2 + i*2*Math.PI/n;
                    var r = R*(valores[i]/4);
                    var x = cx + r*Math.cos(an);
                    var y = cy + r*Math.sin(an);
                    if(i===0){ g.moveTo(x,y); } else { g.lineTo(x,y); }
                  } g.closePath(); g.stroke(); g.fill();
                  g.fillStyle = '#1c2733'; g.font = '13px Arial';
                  for(var i=0;i<n;i++){
                    var an = -Math.PI/2 + i*2*Math.PI/n;
                    var x = cx + R*1.18*Math.cos(an);
                    var y = cy + R*1.18*Math.sin(an);
                    g.fillText(etiquetas[i], x - 30, y + 5);
                  }
                  g.fillStyle = '#1c2733'; g.font = '16px Arial';
                  g.fillText(titulo, 10, 30);
                })();
                """;

        script = script
                .replace("@@ID@@", id + "-canvas")
                .replace("@@VALORES@@", arrayDoublesToJs(valores))
                .replace("@@ETIQUETAS@@", arrayStringsToJs(etiquetas))
                .replace("@@TITULO@@", titulo);

        return adjuntarScript(contenedor, script);
    }

    // ------------------------------------------------------------------------
    // adjuntarScript(): conecta el componente al JavaScript cuando se dibuja.
    // ------------------------------------------------------------------------
    private static Component adjuntarScript(Div contenedor, String script) {
        contenedor.addAttachListener(evento -> {
            com.vaadin.flow.component.UI interfaz = evento.getUI();
            if (interfaz != null) {
                interfaz.getPage().executeJs(script);
            }
        });
        return contenedor;
    }

    // ------------------------------------------------------------------------
    // Helpers: convierten arrays Java a texto JavaScript.
    // ------------------------------------------------------------------------
    private static String arrayToJs(int[] datos) {
        StringBuilder texto = new StringBuilder("[");
        for (int i = 0; i < datos.length; i++) {
            if (i > 0) {
                texto.append(",");
            }
            texto.append(datos[i]);
        }
        return texto.append("]").toString();
    }

    private static String arrayDoublesToJs(double[] datos) {
        StringBuilder texto = new StringBuilder("[");
        for (int i = 0; i < datos.length; i++) {
            if (i > 0) {
                texto.append(",");
            }
            // Redondeo a 2 decimales sin usar String.format (evita líos con %).
            double valor = Math.round(datos[i] * 100.0) / 100.0;
            texto.append(valor);
        }
        return texto.append("]").toString();
    }

    private static String arrayStringsToJs(String[] datos) {
        StringBuilder texto = new StringBuilder("[");
        for (int i = 0; i < datos.length; i++) {
            if (i > 0) {
                texto.append(",");
            }
            texto.append('"');
            texto.append(datos[i].replace("\\", "\\\\").replace("\"", "\\\""));
            texto.append('"');
        }
        return texto.append("]").toString();
    }

    private static String matrizToJs(int[][] matriz) {
        StringBuilder texto = new StringBuilder("[");
        for (int i = 0; i < matriz.length; i++) {
            if (i > 0) {
                texto.append(",");
            }
            texto.append("[");
            for (int j = 0; j < matriz[i].length; j++) {
                if (j > 0) {
                    texto.append(",");
                }
                texto.append(matriz[i][j]);
            }
            texto.append("]");
        }
        return texto.append("]").toString();
    }

    // ------------------------------------------------------------------------
    // enTarjeta(): envuelve un gráfico en una tarjeta blanca con título.
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
}
