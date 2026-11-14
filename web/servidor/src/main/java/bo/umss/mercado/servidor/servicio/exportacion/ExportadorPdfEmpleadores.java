package bo.umss.mercado.servidor.servicio.exportacion;

import bo.umss.mercado.servidor.servicio.FichaCruce;
import bo.umss.mercado.servidor.servicio.ServicioAnalisis;
import bo.umss.mercado.servidor.servicio.ServicioCruces;
import bo.umss.mercado.servidor.servicio.ServicioInterpretacion;
import bo.umss.mercado.servidor.vista.GraficoCanvas;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ExportadorPdfEmpleadores {

        private final ServicioAnalisis analisis;
        private final ServicioCruces cruces;
        private final ServicioInterpretacion interpretacion;

        public ExportadorPdfEmpleadores(ServicioAnalisis analisis,
                        ServicioCruces cruces,
                        ServicioInterpretacion interpretacion) {
                this.analisis = analisis;
                this.cruces = cruces;
                this.interpretacion = interpretacion;
        }

        public byte[] exportar() {
                ByteArrayOutputStream salida = new ByteArrayOutputStream();
                Document doc = new Document(PageSize.A4, 45, 45, 50, 50);
                try {
                        PdfWriter.getInstance(doc, salida);
                        doc.open();
                        agregarPortada(doc);
                        doc.newPage();
                        agregarResumenEjecutivo(doc);
                        doc.newPage();
                        agregarUnivariado(doc);
                        doc.newPage();
                        agregarBivariado(doc);
                        doc.newPage();
                        agregarMultivariado(doc);
                        doc.newPage();
                        agregarTrazabilidad(doc);
                        doc.close();
                        return salida.toByteArray();
                } catch (Exception e) {
                        throw new IllegalStateException("No se pudo generar el PDF de empleadores: " + e.getMessage(),
                                        e);
                }
        }

        private void agregarPortada(Document doc) throws Exception {
                Paragraph salto = new Paragraph(" ");
                salto.setSpacingBefore(180);
                doc.add(salto);
                doc.add(EstilosPdf.centrado("Análisis del Mercado Laboral", EstilosPdf.fuentePortada()));
                doc.add(EstilosPdf.centrado("Ingeniería de Sistemas · UMSS · Cochabamba, Bolivia",
                                EstilosPdf.fuenteSubtituloPortada()));

                Paragraph salto2 = new Paragraph(" ");
                salto2.setSpacingBefore(60);
                doc.add(salto2);
                doc.add(EstilosPdf.centrado("Autores", EstilosPdf.fuenteH2()));
                doc.add(EstilosPdf.centrado("Victor Manuel Terrazas Corrales", EstilosPdf.fuenteCuerpo()));
                doc.add(EstilosPdf.centrado("Nayra Pacheco Fuentes", EstilosPdf.fuenteCuerpo()));
                doc.add(EstilosPdf.centrado("Carlos Aurelio La Fuente Canaviri", EstilosPdf.fuenteCuerpo()));

                Paragraph salto3 = new Paragraph(" ");
                salto3.setSpacingBefore(50);
                doc.add(salto3);
                doc.add(EstilosPdf.centrado("Empleadores (M = " + analisis.mapasEmpleadores().size() + ")",
                                EstilosPdf.fuenteSubtituloPortada()));

                Paragraph salto4 = new Paragraph(" ");
                salto4.setSpacingBefore(40);
                doc.add(salto4);
                doc.add(EstilosPdf.centrado(
                                "Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                EstilosPdf.fuenteItalica()));
        }

        private void agregarResumenEjecutivo(Document doc) throws Exception {
                EstilosPdf.banner(doc, "Resumen ejecutivo", "Indicadores y hallazgos principales de los empleadores");

                List<Map<String, String>> m = analisis.mapasEmpleadores();
                int n = m.size();

                doc.add(EstilosPdf.tituloSeccion("Indicadores clave con IC 95% (Wilson)"));
                PdfPTable tabla = EstilosPdf.tabla(5);
                tabla.addCell(EstilosPdf.celdaHeader("Indicador"));
                tabla.addCell(EstilosPdf.celdaHeader("Proporción (%)"));
                tabla.addCell(EstilosPdf.celdaHeader("IC inferior (%)"));
                tabla.addCell(EstilosPdf.celdaHeader("IC superior (%)"));
                tabla.addCell(EstilosPdf.celdaHeader("n"));
                tabla.setHeaderRows(1);

                double contrato = analisis.proporcionDonde(m, "contrato_ultimos_5_anios", v -> v.equals("SI"));
                double altaProy = analisis.proporcionDonde(m, "posibilidad_futura_contratacion",
                                v -> v.equals("ALTA_PROBABILIDAD"));
                double ia = analisis.proporcionDemandaIA();

                agregarFilaKpi(tabla, "Contrató en los últimos 5 años", contrato, n);
                agregarFilaKpi(tabla, "Alta probabilidad de contratar", altaProy, n);
                agregarFilaKpi(tabla, "Demanda de IA", ia, n);
                doc.add(tabla);

                doc.add(EstilosPdf.subtitulo("Medias de dimensiones Likert (escala 1-3)"));
                double mediaA = promedio(analisis.mediasPorDimension("A"));
                double mediaB = promedio(analisis.mediasPorDimension("B"));
                double media4 = analisis.fichaItem4().media();
                doc.add(EstilosPdf.parrafo("Dimensión A — Calidad e Imagen: " + EstilosPdf.fmt(mediaA, 2) + "/3 (n="
                                + analisis.mediasPorDimension("A").size() + ")"));
                doc.add(EstilosPdf.parrafo("Dimensión B — Vinculación y Retroalimentación: " + EstilosPdf.fmt(mediaB, 2)
                                + "/3 (n=" + analisis.mediasPorDimension("B").size() + ")"));
                doc.add(EstilosPdf.parrafo("Ítem 4 — Conoce el perfil de egreso: " + EstilosPdf.fmt(media4, 2)
                                + "/3 (n=" + analisis.fichaItem4().n() + ")"));
        }

        private void agregarFilaKpi(PdfPTable t, String etiqueta, double proporcion, int n) {
                var ic = bo.umss.mercado.nucleo.stats.IntervalosConfianza.paraProporcion(proporcion, n);
                t.addCell(EstilosPdf.celdaTexto(etiqueta));
                t.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(proporcion * 100, 1)));
                t.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(ic.limiteInferior() * 100, 1)));
                t.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(ic.limiteSuperior() * 100, 1)));
                t.addCell(EstilosPdf.celdaNumero(String.valueOf(n)));
        }

        private void agregarUnivariado(Document doc) throws Exception {
                EstilosPdf.banner(doc, "4.1 Análisis univariado",
                                "Caracterización y evaluación de las organizaciones empleadoras");

                doc.add(EstilosPdf.subtitulo("Tipo de organización"));
                agregarTablaFrecuencias(doc, analisis.frecuenciasTipoOrg());
                agregarGrafico(doc, GraficoCanvas.svgTorta("pdf_tipo", valoresDe(analisis.frecuenciasTipoOrg()),
                                etiquetasDe(analisis.frecuenciasTipoOrg(),
                                                Map.of("PUBLICO", "Público", "PRIVADO", "Privado")),
                                new String[] { GraficoCanvas.COLOR_PRINCIPAL, GraficoCanvas.COLOR_NARANJA },
                                "Tipo de organización"), 460, 320, "Tipo de organización");

                doc.add(EstilosPdf.subtitulo("Tamaño de la organización"));
                agregarTablaFrecuencias(doc, analisis.frecuenciasTamano());
                agregarGraficoBarrasOrdenadas(doc, analisis.frecuenciasTamano(), GraficoCanvas.COLOR_SECUNDARIO,
                                "Tamaño");

                doc.add(EstilosPdf.subtitulo("Sector / Rubro"));
                agregarTablaFrecuencias(doc, analisis.frecuenciasSector());
                agregarGraficoBarrasOrdenadas(doc, analisis.frecuenciasSector(), GraficoCanvas.COLOR_VERDE, "Sector");

                doc.add(EstilosPdf.subtitulo("Nivel académico demandado"));
                agregarTablaFrecuencias(doc, analisis.frecuenciasNivelAcademico());
                agregarGraficoBarrasOrdenadas(doc, analisis.frecuenciasNivelAcademico(), GraficoCanvas.COLOR_MORADO,
                                "Nivel académico");

                doc.add(EstilosPdf.subtitulo("Cargos demandados"));
                agregarTablaFrecuencias(doc, analisis.distribucionCargos());
                agregarGraficoBarrasOrdenadas(doc, analisis.distribucionCargos(), GraficoCanvas.COLOR_SECUNDARIO,
                                "Cargos");

                doc.add(EstilosPdf.subtitulo("Tendencias emergentes"));
                agregarTablaFrecuencias(doc, analisis.distribucionTendencias());
                agregarGraficoBarrasOrdenadas(doc, analisis.distribucionTendencias(), GraficoCanvas.COLOR_NARANJA,
                                "Tendencias");

                doc.add(EstilosPdf.subtitulo("Hard skills más mencionadas"));
                agregarTablaFrecuencias(doc, analisis.distribucionHardSkills());
                agregarGraficoBarrasOrdenadas(doc, analisis.distribucionHardSkills(), GraficoCanvas.COLOR_PRINCIPAL,
                                "Hard skills");

                doc.add(EstilosPdf.subtitulo("Soft skills más mencionadas"));
                agregarTablaFrecuencias(doc, analisis.distribucionSoftSkills());
                agregarGraficoBarrasOrdenadas(doc, analisis.distribucionSoftSkills(), GraficoCanvas.COLOR_VERDE,
                                "Soft skills");

                doc.add(EstilosPdf.subtitulo("Evaluación institucional — Radar descriptivo"));
                double mediaA = promedio(analisis.mediasPorDimension("A"));
                double mediaB = promedio(analisis.mediasPorDimension("B"));
                double media4 = analisis.fichaItem4().media();
                agregarGrafico(doc,
                                GraficoCanvas.svgRadarConEscala("pdf_radar", new double[] { mediaA, mediaB, media4 },
                                                new String[] { "Dimensión A", "Dimensión B", "Ítem 4" }, 3,
                                                "Radar descriptivo (escala 1-3)"),
                                480, 360, "Radar de dimensiones");

                doc.add(EstilosPdf.subtitulo("Análisis por ítem (18 ítems Likert)"));
                PdfPTable t = EstilosPdf.tabla(5);
                t.addCell(EstilosPdf.celdaHeader("Ítem"));
                t.addCell(EstilosPdf.celdaHeader("n"));
                t.addCell(EstilosPdf.celdaHeader("Media"));
                t.addCell(EstilosPdf.celdaHeader("Desv."));
                t.addCell(EstilosPdf.celdaHeader("% Acuerdo"));
                t.setHeaderRows(1);
                for (var item : analisis.matriz18Items()) {
                        t.addCell(EstilosPdf.celdaTexto(item.etiqueta()));
                        t.addCell(EstilosPdf.celdaNumero(String.valueOf(item.n())));
                        t.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(item.media(), 2)));
                        t.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(item.desviacion(), 2)));
                        t.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(item.pctAcuerdo(), 1)));
                }
                doc.add(t);
        }

        private void agregarBivariado(Document doc) throws Exception {
                EstilosPdf.banner(doc, "4.2 Análisis bivariado",
                                "Pruebas de independencia χ² de los cruces de empleadores");

                List<FichaCruce> lista = cruces.crucesEmpleadores();
                PdfPTable tabla = EstilosPdf.tabla(8);
                tabla.addCell(EstilosPdf.celdaHeader("N°"));
                tabla.addCell(EstilosPdf.celdaHeader("Cruce"));
                tabla.addCell(EstilosPdf.celdaHeader("χ²"));
                tabla.addCell(EstilosPdf.celdaHeader("gl"));
                tabla.addCell(EstilosPdf.celdaHeader("p-valor"));
                tabla.addCell(EstilosPdf.celdaHeader("V Cramér"));
                tabla.addCell(EstilosPdf.celdaHeader("Intensidad"));
                tabla.addCell(EstilosPdf.celdaHeader("p < 0.05"));
                tabla.setHeaderRows(1);

                int sig = 0;
                for (int i = 0; i < lista.size(); i++) {
                        FichaCruce c = lista.get(i);
                        var r = c.resultado();
                        if (r == null)
                                continue;
                        boolean s = !Double.isNaN(r.valorP()) && r.valorP() < 0.05;
                        if (s)
                                sig++;
                        tabla.addCell(EstilosPdf.celdaNumero(String.valueOf(i + 1)));
                        tabla.addCell(EstilosPdf.celdaTexto(c.etiqueta()));
                        tabla.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(r.chiCuadrada(), 3)));
                        tabla.addCell(EstilosPdf.celdaNumero(String.valueOf(r.gradosLibertad())));
                        tabla.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(r.valorP(), 4)));
                        tabla.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(r.vCramer(), 3)));
                        tabla.addCell(EstilosPdf.celdaTexto(r.intensidadVCramer()));
                        tabla.addCell(EstilosPdf.celdaTexto(s ? "SÍ" : "NO"));
                }
                doc.add(tabla);
                doc.add(EstilosPdf.parrafo("De los " + lista.size() + " cruces analizados, " + sig
                                + " resultaron estadísticamente significativos (p < 0.05)."));
        }

        private void agregarMultivariado(Document doc) throws Exception {
                EstilosPdf.banner(doc, "4.3 Análisis multivariable", "Modelamiento de media condicional (Ecuación 10)");

                agregarModelo(doc, "Tamaño × Tipo × Media Likert global",
                                analisis.tablaMultivariableEmpleadores("tamano_agrupado", "tipo_organizacion"), "1-3",
                                "Tamaño", "Media Likert");
                doc.newPage();
                agregarModelo(doc, "Rubro × Nivel académico × Proyección",
                                analisis.tablaMultivariable(analisis.mapasEmpleadores(), "rubro_agrupado",
                                                "nivel_academico_grupo", "proyeccion_num"),
                                "1-3", "Rubro", "Proyección");
                doc.newPage();
                agregarModelo(doc, "Tamaño × Tipo × Cantidad de cargos",
                                analisis.tablaMultivariable(analisis.mapasEmpleadores(), "tamano_agrupado",
                                                "tipo_organizacion", "cantidad_cargos"),
                                "cargos", "Tamaño", "Cantidad de cargos");
        }

        private void agregarModelo(Document doc, String titulo, ServicioAnalisis.TablaMultivariable tabla,
                        String unidad, String tituloX, String tituloY) throws Exception {
                doc.add(EstilosPdf.tituloSeccion(titulo));

                PdfPTable t = EstilosPdf.tabla(4);
                t.addCell(EstilosPdf.celdaHeader("X: " + tabla.etiquetaX()));
                t.addCell(EstilosPdf.celdaHeader("Y: " + tabla.etiquetaY()));
                t.addCell(EstilosPdf.celdaHeader("n"));
                t.addCell(EstilosPdf.celdaHeader("Media Z (" + unidad + ")"));
                t.setHeaderRows(1);
                for (var c : tabla.celdas()) {
                        if (c.n() == 0)
                                continue;
                        t.addCell(EstilosPdf.celdaTexto(c.valorX()));
                        t.addCell(EstilosPdf.celdaTexto(c.valorY()));
                        t.addCell(EstilosPdf.celdaNumero(String.valueOf(c.n())));
                        t.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(c.mediaZ(), 2)));
                }
                doc.add(t);

                List<GraficoCanvas.PuntoDispersion> puntos = new ArrayList<>();
                for (var c : tabla.celdas()) {
                        if (c.n() == 0)
                                continue;
                        puntos.add(new GraficoCanvas.PuntoDispersion(c.valorX(), c.valorY(), c.mediaZ(), c.n()));
                }
                agregarGrafico(doc, GraficoCanvas.svgDispersionCategorizada("pdf_disp_" + tituloX.hashCode(),
                                tabla.valoresX(), tabla.valoresY(), puntos,
                                new String[] { GraficoCanvas.COLOR_PRINCIPAL, GraficoCanvas.COLOR_SECUNDARIO,
                                                GraficoCanvas.COLOR_VERDE, GraficoCanvas.COLOR_NARANJA,
                                                GraficoCanvas.COLOR_MORADO, GraficoCanvas.COLOR_GRIS },
                                tituloX, tituloY, "Dispersión: " + titulo), 900, 520, "Dispersión");

                String[] ex = tabla.valoresX().toArray(new String[0]);
                String[] ey = tabla.valoresY().toArray(new String[0]);
                int[][] fr = new int[ex.length][ey.length];
                for (var c : tabla.celdas()) {
                        int xi = tabla.valoresX().indexOf(c.valorX());
                        int yi = tabla.valoresY().indexOf(c.valorY());
                        if (xi >= 0 && yi >= 0)
                                fr[xi][yi] = c.n();
                }
                agregarGrafico(doc, GraficoCanvas.svgBarrasApiladas("pdf_stack_" + tituloX.hashCode(), ex, ey, fr,
                                new String[] { GraficoCanvas.COLOR_VERDE, GraficoCanvas.COLOR_NARANJA,
                                                GraficoCanvas.COLOR_MORADO, GraficoCanvas.COLOR_PRINCIPAL,
                                                GraficoCanvas.COLOR_SECUNDARIO },
                                "Barras apiladas: " + titulo), 900, 500, "Barras apiladas");

                doc.add(EstilosPdf.subtitulo("Hallazgos"));
                for (String linea : interpretacion.interpretar(tabla).split("\\n")) {
                        if (linea.trim().isEmpty())
                                continue;
                        doc.add(EstilosPdf.parrafo(linea.trim().replaceFirst("^[•\\-]\\s*", "")));
                }
        }

        private void agregarTrazabilidad(Document doc) throws Exception {
                EstilosPdf.banner(doc, "Trazabilidad del modelo", "Cómo se generaron los resultados de este informe");
                doc.add(EstilosPdf.parrafo(
                                "Este informe fue generado automáticamente por el sistema de análisis de mercado laboral el "
                                                + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                                + " a partir de la base consolidada de "
                                                + analisis.mapasEmpleadores().size() + " empleadores."));
                doc.add(EstilosPdf.parrafo(
                                "Los estadísticos descriptivos, las pruebas de independencia (χ², V de Cramér) y el modelamiento de media condicional (Ecuación 10) siguen la metodología descrita en el Capítulo 2 del informe."));
        }

        private void agregarGrafico(Document doc, String svg, int ancho, int alto, String titulo) {
                try {
                        byte[] png = ConversorSvg.aPng(svg, ancho, alto);
                        if (png == null) {
                                doc.add(EstilosPdf.nota("[Gráfico no disponible: " + titulo + "]"));
                                return;
                        }
                        Image img = Image.getInstance(png);
                        float nuevoAncho = 420f;
                        float nuevoAlto = nuevoAncho * alto / ancho;
                        img.scaleAbsolute(nuevoAncho, nuevoAlto);
                        img.setAlignment(Element.ALIGN_CENTER);
                        doc.add(img);
                        Paragraph fig = EstilosPdf.centrado("Figura: " + titulo, EstilosPdf.fuenteItalica());
                        fig.setSpacingAfter(12);
                        doc.add(fig);
                } catch (Exception e) {
                        doc.add(EstilosPdf.nota("[Gráfico no disponible: " + titulo + " — " + e.getMessage() + "]"));
                }
        }

        private void agregarGraficoBarrasOrdenadas(Document doc, Map<String, Integer> frec, String color,
                        String titulo) {
                if (frec.isEmpty())
                        return;
                List<Map.Entry<String, Integer>> orden = new ArrayList<>(frec.entrySet());
                orden.sort((a, b) -> b.getValue() - a.getValue());
                int[] v = new int[orden.size()];
                String[] e = new String[orden.size()];
                for (int i = 0; i < orden.size(); i++) {
                        v[i] = orden.get(i).getValue();
                        e[i] = orden.get(i).getKey();
                }
                agregarGrafico(doc,
                                GraficoCanvas.svgBarrasVerticales("pdf_bar_" + titulo.hashCode(), v, e, color, titulo),
                                800, 420, titulo);
        }

        private void agregarTablaFrecuencias(Document doc, Map<String, Integer> frec) throws Exception {
                PdfPTable t = EstilosPdf.tabla(3);
                t.addCell(EstilosPdf.celdaHeader("Categoría"));
                t.addCell(EstilosPdf.celdaHeader("Frecuencia (n)"));
                t.addCell(EstilosPdf.celdaHeader("Porcentaje (%)"));
                t.setHeaderRows(1);
                int total = frec.values().stream().mapToInt(Integer::intValue).sum();
                for (Map.Entry<String, Integer> e : frec.entrySet()) {
                        double pct = total == 0 ? 0 : 100.0 * e.getValue() / total;
                        t.addCell(EstilosPdf.celdaTexto(e.getKey()));
                        t.addCell(EstilosPdf.celdaNumero(String.valueOf(e.getValue())));
                        t.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(pct, 1)));
                }
                doc.add(t);
        }

        private static int[] valoresDe(Map<String, Integer> frec) {
                int[] v = new int[frec.size()];
                int i = 0;
                for (int x : frec.values())
                        v[i++] = x;
                return v;
        }

        private static String[] etiquetasDe(Map<String, Integer> frec, Map<String, String> mapa) {
                String[] out = new String[frec.size()];
                int i = 0;
                for (String key : frec.keySet())
                        out[i++] = mapa.getOrDefault(key, key);
                return out;
        }

        private static double promedio(List<Double> valores) {
                if (valores == null || valores.isEmpty())
                        return Double.NaN;
                double suma = 0;
                for (double v : valores)
                        suma += v;
                return suma / valores.size();
        }
}
