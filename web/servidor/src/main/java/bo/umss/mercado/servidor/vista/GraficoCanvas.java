package bo.umss.mercado.servidor.vista;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * GraficoCanvas: utilitario para dibujar gráficos estadísticos con canvas puro
 * (sin librerías externas como Chart.js).
 *
 * Cada método devuelve un Componente de Vaadin listo para insertar en una vista.
 * Internamente genera un <canvas> y ejecuta JavaScript mediante page.executeJs()
 * para dibujar el gráfico con los datos recibidos.
 *
 * Colores usados (paleta simple y legible):
 *   - Azul principal:  #0b3d62
 *   - Azul secundario: #0984e3
 *   - Verde:           #27ae60
 *   - Naranja:         #e17055
 *   - Morado:          #6c5ce7
 *   - Gris:            #b2bec3
 */
public class GraficoCanvas {

    private static final String COLOR_PRINCIPAL = "#0b3d62";
    private static final String COLOR_SECUNDARIO = "#0984e3";
    private static final String COLOR_VERDE = "#27ae60";
    private static final String COLOR_NARANJA = "#e17055";
    private static final String COLOR_MORADO = "#6c5ce7";
    private static final String COLOR_GRIS = "#b2bec3";

    // ------------------------------------------------------------------------
    // barrasVerticales(): dibuja un gráfico de barras verticales.
    //   id        : identificador único del canvas en el DOM
    //   datos     : valores numéricos (uno por categoría)
    //   etiquetas : nombres de las categorías
    //   color     : color de las barras
    //   titulo    : título que se muestra arriba del gráfico
    // ------------------------------------------------------------------------
    public static Component barrasVerticales(String id, int[] datos, String[] etiquetas,
            String color, String titulo) {
        Div contenedor = new Div();
        contenedor.setId(id);
        contenedor.getStyle().set("width", "100%").set("max-width", "700px")
                .set("height", "350px").set("margin", "10px 0");

        Span lblTitulo = new Span(titulo);
        lblTitulo.getStyle().set("font-weight", "bold").set("font-size", "16px")
                .set("margin-bottom", "8px").set("display", "block");

        Div canvasWrap = new Div();
        canvasWrap.setId(id + "-canvas");
        canvasWrap.getStyle().set("width", "100%").set("height", "300px");

        contenedor.add(lblTitulo, canvasWrap);

        // Construir arrays JS para pasar a executeJs
        String jsDatos = arrayToJs(datos);
        String jsEtiquetas = arrayStringsToJs(etiquetas);

        String script = """
                (function(){
                  var c = document.getElementById('%s');
                  if(!c) return;
                  var g = c.getContext('2d');
                  var w = c.parentElement.clientWidth || 700;
                  var h = 300;
                  c.width = w; c.height = h;
                  g.fillStyle = '#ffffff'; g.fillRect(0,0,w,h);
                  var datos = %s;
                  var etiquetas = %s;
                  var color = '%s';
                  var mx = Math.max.apply(null, datos.concat([1]));
                  var n = datos.length;
                  var bw = (w - 80) / (n * 1.5);
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
                  g.fillText('%s', 10, 30);
                })();
                """.formatted(id + "-canvas", jsDatos, jsEtiquetas, color, titulo);

        contenedor.addAttachListener(e -> {
            com.vaadin.flow.component.UI ui = e.getUI();
            if (ui != null) {
                ui.getPage().executeJs(script);
            }
        });

        return contenedor;
    }

    // ------------------------------------------------------------------------
    // torta(): dibuja un gráfico circular (pie chart).
    //   datos   : valores numéricos (uno por categoría)
    //   etiquetas: nombres de las categorías
    //   colores : colores de cada segmento (si hay menos que datos, se repiten)
    //   titulo  : título del gráfico
    // ------------------------------------------------------------------------
    public static Component torta(String id, int[] datos, String[] etiquetas,
            String[] colores, String titulo) {
        Div contenedor = new Div();
        contenedor.setId(id);
        contenedor.getStyle().set("width", "100%").set("max-width", "400px")
                .set("height", "320px").set("margin", "10px 0");

        Span lblTitulo = new Span(titulo);
        lblTitulo.getStyle().set("font-weight", "bold").set("font-size", "16px")
                .set("margin-bottom", "8px").set("display", "block");

        Div canvasWrap = new Div();
        canvasWrap.setId(id + "-canvas");
        canvasWrap.getStyle().set("width", "100%").set("height", "280px");

        contenedor.add(lblTitulo, canvasWrap);

        String jsDatos = arrayToJs(datos);
        String jsEtiquetas = arrayStringsToJs(etiquetas);
        String jsColores = arrayStringsToJs(colores);

        String script = """
                (function(){
                  var c = document.getElementById('%s');
                  if(!c) return;
                  var g = c.getContext('2d');
                  var w = c.parentElement.clientWidth || 400;
                  var h = 280;
                  c.width = w; c.height = h;
                  g.fillStyle = '#ffffff'; g.fillRect(0,0,w,h);
                  var datos = %s;
                  var etiquetas = %s;
                  var colores = %s;
                  var total = datos.reduce(function(a,b){return a+b;},0) || 1;
                  var cx = w/2, cy = h/2 - 10, R = Math.min(w,h)/2 - 40;
                  var ang = -Math.PI/2;
                  for(var i=0;i<datos.length;i++){
                    var a2 = ang + 2*Math.PI*(datos[i]/total);
                    g.beginPath(); g.moveTo(cx,cy); g.arc(cx,cy,R,ang,a2); g.closePath();
                    g.fillStyle = colores[i % colores.length]; g.fill();
                    g.strokeStyle = '#ffffff'; g.lineWidth=2; g.stroke();
                    ang = a2;
                  }
                  g.fillStyle = '#1c2733'; g.font = '16px Arial';
                  g.fillText('%s', 10, 30);
                  // Leyenda
                  var ly = 50;
                  for(var i=0;i<etiquetas.length;i++){
                    g.fillStyle = colores[i % colores.length];
                    g.fillRect(10, ly, 14, 14);
                    g.fillStyle = '#1c2733'; g.font = '14px Arial';
                    g.fillText(etiquetas[i] + ' ' + Math.round(100*datos[i]/total) + '%', 30, ly + 12);
                    ly += 22;
                  }
                })();
                """.formatted(id + "-canvas", jsDatos, jsEtiquetas, jsColores, titulo);

        contenedor.addAttachListener(e -> {
            com.vaadin.flow.component.UI ui = e.getUI();
            if (ui != null) {
                ui.getPage().executeJs(script);
            }
        });

        return contenedor;
    }

    // ------------------------------------------------------------------------
    // mapaCalor(): dibuja una matriz de calor (heatmap) para tablas de contingencia.
    //   obs   : matriz de conteos [filas][columnas]
    //   filas : nombres de las filas
    //   cols  : nombres de las columnas
    //   titulo: título del gráfico
    // ------------------------------------------------------------------------
    public static Component mapaCalor(String id, int[][] obs, String[] filas,
            String[] cols, String titulo) {
        Div contenedor = new Div();
        contenedor.setId(id);
        contenedor.getStyle().set("width", "100%").set("max-width", "700px")
                .set("height", "340px").set("margin", "10px 0");

        Span lblTitulo = new Span(titulo);
        lblTitulo.getStyle().set("font-weight", "bold").set("font-size", "16px")
                .set("margin-bottom", "8px").set("display", "block");

        Div canvasWrap = new Div();
        canvasWrap.setId(id + "-canvas");
        canvasWrap.getStyle().set("width", "100%").set("height", "300px");

        contenedor.add(lblTitulo, canvasWrap);

        String jsObs = matrizToJs(obs);
        String jsFilas = arrayStringsToJs(filas);
        String jsCols = arrayStringsToJs(cols);

        String script = """
                (function(){
                  var c = document.getElementById('%s');
                  if(!c) return;
                  var g = c.getContext('2d');
                  var w = c.parentElement.clientWidth || 700;
                  var h = 300;
                  c.width = w; c.height = h;
                  g.fillStyle = '#ffffff'; g.fillRect(0,0,w,h);
                  var obs = %s;
                  var filas = %s;
                  var cols = %s;
                  var f = filas.length, col = cols.length;
                  var mx = 1; obs.forEach(function(r){r.forEach(function(v){mx=Math.max(mx,v);});});
                  var cw = (w - 150)/Math.max(1,col);
                  var rh = (h - 60)/Math.max(1,f);
                  // Dibujar celdas
                  for(var i=0;i<f;i++){
                    for(var j=0;j<col;j++){
                      var t = obs[i][j]/mx;
                      var r = Math.round(11 + t*200);
                      var gcol = Math.round(61 + t*100);
                      var b = Math.round(98 + t*60);
                      g.fillStyle = 'rgb('+r+','+gcol+','+b+')';
                      g.fillRect(140 + j*cw, 10 + i*rh, cw-4, rh-6);
                      g.fillStyle = '#1c2733'; g.font = '12px Arial';
                      g.fillText(String(obs[i][j]), 140 + j*cw + 6, 10 + i*rh + rh/2 + 5);
                    }
                  }
                  // Etiquetas filas
                  g.fillStyle = '#1c2733'; g.font = '14px Arial';
                  for(var i=0;i<f;i++){
                    g.fillText(filas[i], 5, 30 + i*rh + rh/2);
                  }
                  // Etiquetas columnas
                  for(var j=0;j<col;j++){
                    g.save(); g.translate(140 + j*cw + cw/2, h - 10);
                    g.rotate(-Math.PI/4); g.fillText(cols[j], 5, 5); g.restore();
                  }
                  g.fillStyle = '#1c2733'; g.font = '16px Arial';
                  g.fillText('%s', 10, 30);
                })();
                """.formatted(id + "-canvas", jsObs, jsFilas, jsCols, titulo);

        contenedor.addAttachListener(e -> {
            com.vaadin.flow.component.UI ui = e.getUI();
            if (ui != null) {
                ui.getPage().executeJs(script);
            }
        });

        return contenedor;
    }

    // ------------------------------------------------------------------------
    // radar(): dibuja un diagrama de radar para valores de 1 a 4.
    //   valores : puntajes (una por categoría)
    //   etiquetas: nombres de las categorías
    //   titulo  : título del gráfico
    // ------------------------------------------------------------------------
    public static Component radar(String id, double[] valores, String[] etiquetas,
            String titulo) {
        Div contenedor = new Div();
        contenedor.setId(id);
        contenedor.getStyle().set("width", "100%").set("max-width", "400px")
                .set("height", "340px").set("margin", "10px 0");

        Span lblTitulo = new Span(titulo);
        lblTitulo.getStyle().set("font-weight", "bold").set("font-size", "16px")
                .set("margin-bottom", "8px").set("display", "block");

        Div canvasWrap = new Div();
        canvasWrap.setId(id + "-canvas");
        canvasWrap.getStyle().set("width", "100%").set("height", "300px");

        contenedor.add(lblTitulo, canvasWrap);

        String jsValores = arrayDoublesToJs(valores);
        String jsEtiquetas = arrayStringsToJs(etiquetas);

        String script = """
                (function(){
                  var c = document.getElementById('%s');
                  if(!c) return;
                  var g = c.getContext('2d');
                  var w = c.parentElement.clientWidth || 400;
                  var h = 300;
                  c.width = w; c.height = h;
                  g.fillStyle = '#ffffff'; g.fillRect(0,0,w,h);
                  var valores = %s;
                  var etiquetas = %s;
                  var n = valores.length;
                  var cx = w/2, cy = h/2 - 20, R = Math.min(w,h)/2 - 50;
                  // Dibujar anillos (4 niveles)
                  for(var ring=1; ring<=4; ring++){
                    g.beginPath(); g.strokeStyle = '#dfe6ee'; g.lineWidth = 1;
                    for(var i=0;i<=n;i++){
                      var an = -Math.PI/2 + i*2*Math.PI/n;
                      var x = cx + R*(ring/4)*Math.cos(an);
                      var y = cy + R*(ring/4)*Math.sin(an);
                      if(i===0) g.moveTo(x,y); else g.lineTo(x,y);
                    } g.stroke();
                  }
                  // Dibujar polígono de datos
                  g.beginPath(); g.strokeStyle = '#0b3d62'; g.lineWidth = 3;
                  g.fillStyle = 'rgba(9,132,227,0.25)';
                  for(var i=0;i<n;i++){
                    var an = -Math.PI/2 + i*2*Math.PI/n;
                    var v = valores[i];
                    var r = R*(v/4);
                    var x = cx + r*Math.cos(an);
                    var y = cy + r*Math.sin(an);
                    if(i===0) g.moveTo(x,y); else g.lineTo(x,y);
                  } g.closePath(); g.stroke(); g.fill();
                  // Etiquetas
                  g.fillStyle = '#1c2733'; g.font = '14px Arial';
                  for(var i=0;i<n;i++){
                    var an = -Math.PI/2 + i*2*Math.PI/n;
                    var x = cx + R*1.15*Math.cos(an);
                    var y = cy + R*1.15*Math.sin(an);
                    g.fillText(etiquetas[i], x - 30, y + 5);
                  }
                  g.fillStyle = '#1c2733'; g.font = '16px Arial';
                  g.fillText('%s', 10, 30);
                })();
                """.formatted(id + "-canvas", jsValores, jsEtiquetas, titulo);

        contenedor.addAttachListener(e -> {
            com.vaadin.flow.component.UI ui = e.getUI();
            if (ui != null) {
                ui.getPage().executeJs(script);
            }
        });

        return contenedor;
    }

    // ------------------------------------------------------------------------
    // Helpers para convertir arrays Java a arrays JavaScript
    // ------------------------------------------------------------------------
    private static String arrayToJs(int[] datos) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < datos.length; i++) {
            if (i > 0) s.append(",");
            s.append(datos[i]);
        }
        return s.append("]").toString();
    }

    private static String arrayDoublesToJs(double[] datos) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < datos.length; i++) {
            if (i > 0) s.append(",");
            s.append(String.format("%.2f", datos[i]));
        }
        return s.append("]").toString();
    }

    private static String arrayStringsToJs(String[] datos) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < datos.length; i++) {
            if (i > 0) s.append(",");
            s.append('"').append(datos[i].replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
        }
        return s.append("]").toString();
    }

    private static String matrizToJs(int[][] m) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < m.length; i++) {
            if (i > 0) s.append(",");
            s.append("[");
            for (int j = 0; j < m[i].length; j++) {
                if (j > 0) s.append(",");
                s.append(m[i][j]);
            }
            s.append("]");
        }
        return s.append("]").toString();
    }

    // Método de conveniencia para añadir un componente envuelto en un VerticalLayout
    public static Component enTarjeta(Component contenido, String tituloTarjeta) {
        VerticalLayout tarjeta = new VerticalLayout(contenido);
        tarjeta.getStyle().set("background", "#ffffff")
                .set("border-radius", "10px")
                .set("padding", "16px")
                .set("box-shadow", "0 1px 4px rgba(0,0,0,.12)")
                .set("width", "100%");
        if (tituloTarjeta != null && !tituloTarjeta.isEmpty()) {
            Span t = new Span(tituloTarjeta);
            t.getStyle().set("font-weight", "bold").set("font-size", "15px")
                    .set("margin-bottom", "10px").set("display", "block");
            tarjeta.addComponentAtIndex(0, t);
        }
        return tarjeta;
    }
}
