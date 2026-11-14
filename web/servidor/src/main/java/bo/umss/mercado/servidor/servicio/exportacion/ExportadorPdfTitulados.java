package bo.umss.mercado.servidor.servicio.exportacion;

import bo.umss.mercado.nucleo.stats.ResumenNumerico;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ExportadorPdfTitulados {

        private final ServicioAnalisis analisis;
        private final ServicioCruces cruces;
        private final ServicioInterpretacion interpretacion;

        public ExportadorPdfTitulados(ServicioAnalisis analisis,
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
                        throw new IllegalStateException(
                                        "No se pudo generar el PDF de titulados: " + e.getMessage(), e);
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

                doc.add(EstilosPdf.centrado("Titulados (N = " + analisis.mapasTitulados().size() + ")",
                                EstilosPdf.fuenteSubtituloPortada()));
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
                EstilosPdf.banner(doc, "Resumen ejecutivo", "Indicadores y hallazgos principales de los titulados");

                List<Map<String, String>> m = analisis.mapasTitulados();
                int n = m.size();

                doc.add(EstilosPdf.tituloSeccion("Indicadores clave con IC 95% (Wilson)"));
                doc.add(EstilosPdf.nota(
                                "Cada proporción incluye su intervalo de confianza al 95% calculado con la fórmula de Wilson."));

                PdfPTable tabla = EstilosPdf.tabla(5);
                tabla.addCell(EstilosPdf.celdaHeader("Indicador"));
                tabla.addCell(EstilosPdf.celdaHeader("Proporción (%)"));
                tabla.addCell(EstilosPdf.celdaHeader("IC inferior (%)"));
                tabla.addCell(EstilosPdf.celdaHeader("IC superior (%)"));
                tabla.addCell(EstilosPdf.celdaHeader("n"));
                tabla.setHeaderRows(1);

                double insercion = analisis.tasaInsercion();
                double interes = analisis.intencionPosgrado();
                double beca = analisis.proporcionRequiereBeca();
                double pertinencia = analisis.proporcionDonde(m, "pertinencia_trabajo",
                                v -> bo.umss.mercado.nucleo.limpieza.LimpiezaDatos.normalizar(v).contains("ACUERDO"));
                double ofertaAfin = analisis.proporcionDonde(m, "institucion_futura_posgrado",
                                v -> v.equals("DIR_POSGRADO_FCyT") || v.equals("EUPG"));

                agregarFilaKpi(tabla, "Trabajan en organización", insercion, n);
                agregarFilaKpi(tabla, "Interesados en posgrado", interes, n);
                agregarFilaKpi(tabla, "Financiaron con recursos propios", beca, n);
                agregarFilaKpi(tabla, "Consideran formación pertinente", pertinencia, n);
                agregarFilaKpi(tabla, "Oferta FCyT es afín", ofertaAfin, n);
                doc.add(tabla);

                doc.add(EstilosPdf.subtitulo("Hallazgos clave"));
                double yaTrabajaba = analisis.proporcionDonde(m, "tiempo_primer_empleo", v -> "YA_TRABAJABA".equals(v));

                doc.add(EstilosPdf.parrafo(
                                "Empleabilidad: la tasa de inserción es del " + pct(insercion) + ". Además, el "
                                                + pct(yaTrabajaba) + " ya contaba con un empleo antes de titularse."));
                doc.add(EstilosPdf.parrafo(
                                "Barrera económica: el " + pct(beca) + " financió su formación con recursos propios."));
                doc.add(EstilosPdf.parrafo("Demanda de posgrado: el " + pct(interes)
                                + " declara interés en realizar estudios de posgrado."));
                doc.add(EstilosPdf.parrafo("Pertinencia formativa: el " + pct(pertinencia)
                                + " considera que su formación es pertinente a sus funciones actuales."));
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
                EstilosPdf.banner(doc, "3.1 Análisis univariado",
                                "Distribuciones y estadísticos descriptivos de los titulados");

                doc.add(EstilosPdf.subtitulo("Situación laboral"));
                agregarTablaFrecuencias(doc, analisis.frecuenciasDe("situacion_laboral_actual"));
                agregarGrafico(doc, GraficoCanvas.svgTorta("pdf_sit",
                                valoresDe(analisis.frecuenciasDe("situacion_laboral_actual")),
                                etiquetasDe(analisis.frecuenciasDe("situacion_laboral_actual"),
                                                Map.of("TRABAJA_ORG", "Trabaja en org.", "NO_TRABAJA", "No trabaja",
                                                                "EMPRENDIMIENTO", "Emprendimiento")),
                                new String[] { GraficoCanvas.COLOR_VERDE, GraficoCanvas.COLOR_NARANJA,
                                                GraficoCanvas.COLOR_MORADO },
                                "Situación laboral"), 460, 320, "Situación laboral");

                doc.add(EstilosPdf.subtitulo("Distribución por género"));
                agregarTablaFrecuencias(doc, analisis.frecuenciasDe("genero"));
                agregarGrafico(doc, GraficoCanvas.svgTorta("pdf_genero", valoresDe(analisis.frecuenciasDe("genero")),
                                etiquetasDe(analisis.frecuenciasDe("genero"),
                                                Map.of("HOMBRE", "Hombre", "MUJER", "Mujer", "OTRO", "Otro")),
                                new String[] { GraficoCanvas.COLOR_PRINCIPAL, GraficoCanvas.COLOR_NARANJA,
                                                GraficoCanvas.COLOR_MORADO },
                                "Distribución por género"), 460, 320, "Distribución por género");

                doc.add(EstilosPdf.subtitulo("Rango de edad"));
                Map<String, Integer> edades = calcularRangosEdad();
                agregarTablaFrecuencias(doc, edades);
                agregarGraficoBarrasOrdenadas(doc, edades, GraficoCanvas.COLOR_PRINCIPAL, "Rango de edad");

                doc.add(EstilosPdf.subtitulo("Nivel de posgrado alcanzado"));
                agregarTablaFrecuencias(doc, analisis.frecuenciasDe("nivel_posgrado"));
                agregarGraficoBarrasOrdenadas(doc, analisis.frecuenciasDe("nivel_posgrado"),
                                GraficoCanvas.COLOR_SECUNDARIO, "Nivel de posgrado");

                doc.add(EstilosPdf.subtitulo("Institución donde cursó la formación"));
                agregarTablaFrecuencias(doc, analisis.frecuenciasDe("institucion_formacion"));
                agregarGraficoBarrasOrdenadas(doc, analisis.frecuenciasDe("institucion_formacion"),
                                GraficoCanvas.COLOR_VERDE, "Institución");

                doc.add(EstilosPdf.subtitulo("Formación complementaria"));
                agregarTablaFrecuencias(doc, analisis.frecuenciasDe("realizo_formacion_complementaria"));
                agregarGrafico(doc,
                                GraficoCanvas.svgTorta("pdf_form",
                                                valoresDe(analisis.frecuenciasDe("realizo_formacion_complementaria")),
                                                etiquetasDe(analisis.frecuenciasDe("realizo_formacion_complementaria"),
                                                                Map.of("SI", "Sí", "NO", "No")),
                                                new String[] { GraficoCanvas.COLOR_VERDE, GraficoCanvas.COLOR_GRIS },
                                                "¿Realizó formación complementaria?"),
                                460, 320, "Formación continua");

                doc.add(EstilosPdf.subtitulo("Fuente de financiamiento"));
                agregarTablaFrecuencias(doc, analisis.frecuenciasDe("financiamiento_formacion"));
                agregarGraficoBarrasOrdenadas(doc, analisis.frecuenciasDe("financiamiento_formacion"),
                                GraficoCanvas.COLOR_NARANJA, "Financiamiento");

                doc.add(EstilosPdf.subtitulo("Áreas de interés para posgrado"));
                agregarTablaFrecuencias(doc, analisis.frecuenciasDe("area_interes_posgrado"));
                agregarGraficoBarrasOrdenadas(doc, analisis.frecuenciasDe("area_interes_posgrado"),
                                GraficoCanvas.COLOR_MORADO, "Áreas de interés");

                doc.add(EstilosPdf.subtitulo("Tiempo al primer empleo"));
                agregarTablaFrecuencias(doc, analisis.frecuenciasDe("tiempo_primer_empleo"));
                agregarGraficoBarrasOrdenadas(doc, analisis.frecuenciasDe("tiempo_primer_empleo"),
                                GraficoCanvas.COLOR_NARANJA, "Tiempo al primer empleo");

                doc.add(EstilosPdf.subtitulo("Rango de remuneración"));
                agregarTablaFrecuencias(doc, analisis.frecuenciasDe("remuneracion_rango"));
                agregarGraficoBarrasOrdenadas(doc, analisis.frecuenciasDe("remuneracion_rango"),
                                GraficoCanvas.COLOR_MORADO, "Remuneración");

                doc.add(EstilosPdf.subtitulo("Años de vida profesional"));
                agregarTablaResumenNumerico(doc, analisis.resumenNumerico("anios_vida_profesional"));
                agregarGrafico(doc, GraficoCanvas.svgBarrasVerticales("pdf_antig", calcularBucketsAntiguedad(),
                                new String[] { "0-2", "2-4", "4-6", "6-8", "8-10", "10-12", "12-14", "14-16", "16-18",
                                                "18+" },
                                GraficoCanvas.COLOR_PRINCIPAL, "Antigüedad (histograma)"), 800, 420,
                                "Antigüedad (histograma)");

                doc.add(EstilosPdf.subtitulo("Satisfacción con la formación"));
                agregarTablaFrecuencias(doc, analisis.frecuenciasDe("satisfaccion_formacion"));
                agregarGrafico(doc,
                                GraficoCanvas.svgBarrasVerticales("pdf_sat", calcularConteoSatisfaccion(),
                                                new String[] { "1 - Muy en desac.", "2 - En desac.", "3 - De acuerdo",
                                                                "4 - Totalmente de acuerdo" },
                                                GraficoCanvas.COLOR_SECUNDARIO, "Satisfacción con la formación"),
                                800, 420, "Satisfacción con la formación");
        }

        private void agregarBivariado(Document doc) throws Exception {
                EstilosPdf.banner(doc, "3.2 Análisis bivariado",
                                "Pruebas de independencia χ² (V de Cramér, sin corrección de Holm)");

                List<FichaCruce> lista = cruces.crucesTitulados();
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

                int significativas = 0;
                for (int i = 0; i < lista.size(); i++) {
                        FichaCruce c = lista.get(i);
                        var r = c.resultado();
                        if (r == null)
                                continue;
                        boolean sig = !Double.isNaN(r.valorP()) && r.valorP() < 0.05;
                        if (sig)
                                significativas++;
                        tabla.addCell(EstilosPdf.celdaNumero(String.valueOf(i + 1)));
                        tabla.addCell(EstilosPdf.celdaTexto(c.etiqueta()));
                        tabla.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(r.chiCuadrada(), 3)));
                        tabla.addCell(EstilosPdf.celdaNumero(String.valueOf(r.gradosLibertad())));
                        tabla.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(r.valorP(), 4)));
                        tabla.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(r.vCramer(), 3)));
                        tabla.addCell(EstilosPdf.celdaTexto(r.intensidadVCramer()));
                        tabla.addCell(EstilosPdf.celdaTexto(sig ? "SÍ" : "NO"));
                }
                doc.add(tabla);

                doc.add(EstilosPdf.parrafo("De los " + lista.size() + " cruces analizados, " + significativas
                                + " resultaron estadísticamente significativos (p < 0.05)."));

                if (!lista.isEmpty() && lista.get(0).resultado() != null
                                && lista.get(0).resultado().observadas() != null) {
                        doc.add(EstilosPdf.subtitulo("Mapa de calor: cruce más significativo"));
                        var r = lista.get(0).resultado();
                        int[][] obs = new int[r.observadas().length][];
                        for (int i = 0; i < r.observadas().length; i++) {
                                obs[i] = new int[r.observadas()[i].length];
                                for (int j = 0; j < r.observadas()[i].length; j++)
                                        obs[i][j] = (int) r.observadas()[i][j];
                        }
                        agregarGrafico(doc,
                                        GraficoCanvas.svgMapaCalor("pdf_heat", obs, r.filas().toArray(new String[0]),
                                                        r.columnas().toArray(new String[0]), "Frecuencias observadas"),
                                        1000, 500, "Heatmap del cruce 1");
                }
        }

        private void agregarMultivariado(Document doc) throws Exception {
                EstilosPdf.banner(doc, "3.3 Análisis multivariable", "Modelamiento de media condicional (Ecuación 10)");

                agregarModelo(doc, "Área de interés × Financiamiento × Años de vida profesional",
                                analisis.tablaMultivariableTitulados("area_interes_posgrado",
                                                "financiamiento_formacion", "anios_vida_profesional"),
                                "años", "Área de interés", "Antigüedad (años)");
                doc.newPage();
                agregarModelo(doc, "Sector × Cargo × Remuneración promedio",
                                analisis.tablaMultivariableTitulados("sector", "cargo_grupo", "remuneracion_rango"),
                                "Bs.", "Sector", "Remuneración (Bs.)");
                doc.newPage();
                agregarModelo(doc, "Satisfacción × Concordancia × Años de vida profesional",
                                analisis.tablaMultivariableTitulados("satisfaccion_grupo", "concordancia_grupo",
                                                "anios_vida_profesional"),
                                "años", "Satisfacción", "Antigüedad (años)");
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

                for (var celda : tabla.celdas()) {
                        if (celda.n() == 0)
                                continue;
                        t.addCell(EstilosPdf.celdaTexto(celda.valorX()));
                        t.addCell(EstilosPdf.celdaTexto(celda.valorY()));
                        t.addCell(EstilosPdf.celdaNumero(String.valueOf(celda.n())));
                        t.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(celda.mediaZ(), 2)));
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
                                                + analisis.mapasTitulados().size() + " titulados y "
                                                + analisis.mapasEmpleadores().size() + " empleadores."));
                doc.add(EstilosPdf.parrafo(
                                "Los estadísticos descriptivos, los intervalos de confianza (Wilson), las pruebas de independencia (χ², V de Cramér) y el modelamiento de media condicional (Ecuación 10) siguen la metodología descrita en el Capítulo 2 del informe."));
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

        private void agregarTablaResumenNumerico(Document doc, ResumenNumerico rn) throws Exception {
                PdfPTable tabla = EstilosPdf.tabla(8);
                tabla.addCell(EstilosPdf.celdaHeader("n"));
                tabla.addCell(EstilosPdf.celdaHeader("Media"));
                tabla.addCell(EstilosPdf.celdaHeader("Mediana"));
                tabla.addCell(EstilosPdf.celdaHeader("Desv."));
                tabla.addCell(EstilosPdf.celdaHeader("Mín."));
                tabla.addCell(EstilosPdf.celdaHeader("Máx."));
                tabla.addCell(EstilosPdf.celdaHeader("Q1"));
                tabla.addCell(EstilosPdf.celdaHeader("Q3"));
                tabla.setHeaderRows(1);
                tabla.addCell(EstilosPdf.celdaNumero(String.valueOf(rn.totalDatos())));
                tabla.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(rn.media(), 2)));
                tabla.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(rn.mediana(), 2)));
                tabla.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(rn.desv(), 2)));
                tabla.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(rn.minimo(), 2)));
                tabla.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(rn.maximo(), 2)));
                tabla.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(rn.q1(), 2)));
                tabla.addCell(EstilosPdf.celdaNumero(EstilosPdf.fmt(rn.q3(), 2)));
                doc.add(tabla);
        }

        private Map<String, Integer> calcularRangosEdad() {
                Map<String, Integer> edades = new LinkedHashMap<>();
                for (String v : analisis.columnaDe(analisis.mapasTitulados(), "edad")) {
                        try {
                                double e = Double.parseDouble(v);
                                String k = e <= 22 ? "19-22"
                                                : e <= 26 ? "23-26" : e <= 30 ? "27-30" : e <= 34 ? "31-34" : "35+";
                                edades.merge(k, 1, Integer::sum);
                        } catch (NumberFormatException ignored) {
                        }
                }
                return edades;
        }

        private int[] calcularBucketsAntiguedad() {
                List<Double> antig = analisis.numerosDe(analisis.mapasTitulados(), "anios_vida_profesional");
                int[] buckets = new int[10];
                for (double a : antig)
                        buckets[Math.min(9, (int) (a / 2))]++;
                return buckets;
        }

        private int[] calcularConteoSatisfaccion() {
                List<Double> sat = analisis.likertDe(analisis.mapasTitulados(), "satisfaccion_formacion");
                int[] c = new int[4];
                for (double s : sat) {
                        int idx = (int) Math.round(s) - 1;
                        if (idx >= 0 && idx < 4)
                                c[idx]++;
                }
                return c;
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

        private static String pct(double proporcion) {
                if (Double.isNaN(proporcion))
                        return "n/d";
                return String.format(Locale.US, "%.1f%%", proporcion * 100);
        }
}
