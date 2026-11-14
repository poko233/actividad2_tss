package bo.umss.mercado.servidor.servicio.exportacion;

import bo.umss.mercado.servidor.servicio.ConfiguracionSimulacion;
import bo.umss.mercado.servidor.servicio.ResultadoSimulacion;
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
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ExportadorPdfSimulacion {

    public byte[] generarPdf(ConfiguracionSimulacion config, ResultadoSimulacion res) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 45, 45, 50, 50);
        try {
            PdfWriter.getInstance(doc, salida);
            doc.open();

            String nombreEscenario = (config.nombreEscenario() != null && !config.nombreEscenario().isEmpty())
                    ? config.nombreEscenario()
                    : "Simulación Personalizada";

            EstilosPdf.banner(doc, "Reporte de Simulación Monte Carlo",
                    "Escenario: " + nombreEscenario);

            doc.add(EstilosPdf.tituloSeccion("1. Configuración del Motor"));
            PdfPTable tConfig = EstilosPdf.tabla(2);
            tConfig.addCell(EstilosPdf.celdaHeader("Parámetro"));
            tConfig.addCell(EstilosPdf.celdaHeader("Valor"));
            tConfig.setHeaderRows(1);

            tConfig.addCell(EstilosPdf.celdaTexto("Variable Analizada"));
            tConfig.addCell(EstilosPdf.celdaTexto(config.nombreVariable()));
            tConfig.addCell(EstilosPdf.celdaTexto("Categoría Objetivo"));
            tConfig.addCell(EstilosPdf.celdaTexto(config.categoriaObjetivo()));
            tConfig.addCell(EstilosPdf.celdaTexto("Prob. Interesados Directos"));
            tConfig.addCell(EstilosPdf
                    .celdaNumero(String.format(Locale.US, "%.1f %%", config.probabilidadInteresados() * 100)));
            tConfig.addCell(EstilosPdf.celdaTexto("Prob. del Resto"));
            tConfig.addCell(
                    EstilosPdf.celdaNumero(String.format(Locale.US, "%.2f %%", config.probabilidadResto() * 100)));
            tConfig.addCell(EstilosPdf.celdaTexto("Iteraciones"));
            tConfig.addCell(EstilosPdf.celdaNumero(String.valueOf(config.iteraciones())));
            tConfig.addCell(EstilosPdf.celdaTexto("Semilla (Auditoría)"));
            tConfig.addCell(EstilosPdf.celdaNumero(String.valueOf(config.semilla())));
            doc.add(tConfig);

            doc.add(EstilosPdf.tituloSeccion("2. Resultados Estadísticos"));
            PdfPTable tRes = EstilosPdf.tabla(2);
            tRes.addCell(EstilosPdf.celdaHeader("Estadístico"));
            tRes.addCell(EstilosPdf.celdaHeader("Valor Proyectado"));
            tRes.setHeaderRows(1);

            tRes.addCell(EstilosPdf.celdaTexto("Población Total Analizada"));
            tRes.addCell(EstilosPdf.celdaNumero(String.valueOf(res.poblacionTotal())));
            tRes.addCell(EstilosPdf.celdaTexto("Interesados Directos"));
            tRes.addCell(EstilosPdf.celdaNumero(res.cantidadInteresados() + " ("
                    + String.format(Locale.US, "%.2f %%", res.proporcionEmpirica() * 100) + ")"));
            tRes.addCell(EstilosPdf.celdaTexto("Media Proyectada"));
            tRes.addCell(EstilosPdf.celdaNumero(String.format(Locale.US, "%.2f", res.media())));
            tRes.addCell(EstilosPdf.celdaTexto("Desviación Estándar"));
            tRes.addCell(EstilosPdf.celdaNumero(String.format(Locale.US, "%.2f", res.desviacionEstandar())));
            tRes.addCell(EstilosPdf.celdaTexto("IC 95% (Rango de Predicción)"));
            tRes.addCell(EstilosPdf
                    .celdaNumero(String.format(Locale.US, "[%.2f, %.2f]", res.icInferior(), res.icSuperior())));
            doc.add(tRes);

            doc.add(EstilosPdf.tituloSeccion("3. Trazabilidad del Cálculo"));
            doc.add(EstilosPdf.parrafo(
                    String.format(Locale.US, "Paso 1: Se identificaron %d interesados de un total de %d (p = %.2f%%).",
                            res.cantidadInteresados(), res.poblacionTotal(), res.proporcionEmpirica() * 100)));
            doc.add(EstilosPdf.parrafo(String.format(Locale.US,
                    "Paso 2: Se aplicaron probabilidades del %.1f%% a los interesados y del %.2f%% al resto.",
                    config.probabilidadInteresados() * 100, config.probabilidadResto() * 100)));
            doc.add(EstilosPdf
                    .parrafo(String.format(Locale.US, "Paso 3: Se ejecutaron %d iteraciones con la semilla %d.",
                            config.iteraciones(), config.semilla())));

            StringBuilder sbIter = new StringBuilder("Ejemplo de los primeros resultados: ");
            for (int i = 0; i < Math.min(5, res.primerasIteraciones().size()); i++) {
                sbIter.append(res.primerasIteraciones().get(i)).append(", ");
            }
            sbIter.append("...");
            doc.add(EstilosPdf.parrafo(sbIter.toString()));

            doc.newPage();
            doc.add(EstilosPdf.tituloSeccion("4. Visualización de Resultados"));

            Map<Integer, Integer> hist = res.histograma();
            int[] vals = hist.values().stream().mapToInt(Integer::intValue).toArray();
            String[] etiq = hist.keySet().stream().map(String::valueOf).toArray(String[]::new);
            String svgHist = GraficoCanvas.svgBarrasVerticales("pdf_hist", vals, etiq, GraficoCanvas.COLOR_VERDE,
                    "Distribución de Frecuencias");
            agregarGrafico(doc, svgHist, 800, 420, "Histograma de Frecuencias");

            String svgTornado = GraficoCanvas.svgBarrasVerticales("pdf_tornado",
                    new int[] { (int) Math.round(res.mediaPesimista()), (int) Math.round(res.mediaBase()),
                            (int) Math.round(res.mediaOptimista()) },
                    new String[] { "Pesimista (-30%)", "Base", "Optimista (+15%)" },
                    GraficoCanvas.COLOR_NARANJA, "Análisis de Sensibilidad");
            agregarGrafico(doc, svgTornado, 800, 420, "Gráfico de Tornado (Sensibilidad)");

            doc.add(EstilosPdf.tituloSeccion("5. Conclusión Automática"));

            String conclusion;
            String nombreEsc = config.nombreEscenario().toLowerCase();

            if (nombreEsc.contains("fidelización")) {
                conclusion = String.format(Locale.US,
                        "El análisis revela que, con un 95%% de confianza, entre %.1f y %.1f titulados buscarían " +
                                "universidades internacionales. Incluso en el escenario pesimista, la fuga sería de %.1f casos. "
                                +
                                "Se recomienda implementar convenios internacionales para retener a estos titulados.",
                        res.icInferior(), res.icSuperior(), res.mediaPesimista());
            } else if (nombreEsc.contains("expansión") || nombreEsc.contains("expansion")) {
                conclusion = String.format(Locale.US,
                        "El análisis revela una demanda potencial de entre %.1f y %.1f titulados en otras regiones " +
                                "(media %.1f). Esto justifica la inversión en una plataforma virtual para captar alumnos de otros departamentos.",
                        res.icInferior(), res.icSuperior(), res.media());
            } else {
                conclusion = String.format(Locale.US,
                        "El análisis revela que, con un 95%% de confianza (Rango de Predicción), la demanda esperada " +
                                "se sitúa entre %.1f y %.1f, con una media de %.1f. Incluso en el escenario pesimista, la demanda "
                                +
                                "se mantiene en %.1f, lo que confirma la viabilidad del programa.",
                        res.icInferior(), res.icSuperior(), res.media(), res.mediaPesimista());
            }
            doc.add(EstilosPdf.parrafo(conclusion));

            doc.add(EstilosPdf.parrafo("Documento generado automáticamente el "
                    + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));

            doc.close();
            return salida.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Error al generar PDF de simulación: " + e.getMessage(), e);
        }
    }

    private void agregarGrafico(Document doc, String svg, int ancho, int alto, String titulo) {
        try {
            byte[] png = ConversorSvg.aPng(svg, ancho, alto);
            if (png != null) {
                Image img = Image.getInstance(png);
                float nuevoAncho = 450f;
                float nuevoAlto = nuevoAncho * alto / ancho;
                img.scaleAbsolute(nuevoAncho, nuevoAlto);
                img.setAlignment(Element.ALIGN_CENTER);
                doc.add(img);
                Paragraph fig = EstilosPdf.centrado("Figura: " + titulo, EstilosPdf.fuenteItalica());
                fig.setSpacingAfter(12);
                doc.add(fig);
            }
        } catch (Exception e) {
            doc.add(EstilosPdf.nota("[Gráfico no disponible: " + titulo + "]"));
        }
    }
}
