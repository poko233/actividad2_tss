package bo.umss.mercado.servidor.servicio.exportacion;

import bo.umss.mercado.nucleo.stats.IntervalosConfianza;
import bo.umss.mercado.nucleo.stats.RangoConfianza;
import bo.umss.mercado.servidor.servicio.FichaCruce;
import bo.umss.mercado.servidor.servicio.ServicioAnalisis;
import bo.umss.mercado.servidor.servicio.ServicioCruces;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class ExportadorExcelEmpleadores {

        private final ServicioAnalisis analisis;
        private final ServicioCruces cruces;

        public ExportadorExcelEmpleadores(ServicioAnalisis analisis, ServicioCruces cruces) {
                this.analisis = analisis;
                this.cruces = cruces;
        }

        public byte[] exportar() {
                try (XSSFWorkbook wb = new XSSFWorkbook()) {
                        escribirDatosCrudos(wb);
                        escribirUnivariado(wb);
                        escribirLikertDescriptivo(wb);
                        escribirBivariadoObservadas(wb);
                        escribirBivariadoEstadisticos(wb);
                        escribirMultivariado(wb);
                        ByteArrayOutputStream salida = new ByteArrayOutputStream();
                        wb.write(salida);
                        return salida.toByteArray();
                } catch (IOException e) {
                        throw new IllegalStateException(
                                        "No se pudo generar el Excel de empleadores: " + e.getMessage(), e);
                }
        }

        private void escribirDatosCrudos(XSSFWorkbook wb) {
                List<Map<String, String>> filas = analisis.mapasEmpleadores();
                Sheet hoja = wb.createSheet("Datos crudos");

                int r = 0;
                r = EstilosExcel.escribirTitulo(wb, hoja, r,
                                "Base de datos de empleadores (M = " + filas.size() + ")");

                if (filas.isEmpty()) {
                        EstilosExcel.escribirFila(wb, hoja, r, "Sin registros en la base");
                        return;
                }

                String[] columnas = filas.get(0).keySet().toArray(new String[0]);
                r = EstilosExcel.escribirEncabezados(wb, hoja, r, columnas);

                for (Map<String, String> fila : filas) {
                        String[] valores = new String[columnas.length];
                        for (int i = 0; i < columnas.length; i++) {
                                valores[i] = fila.getOrDefault(columnas[i], "");
                        }
                        r = EstilosExcel.escribirFila(wb, hoja, r, valores);
                }
                EstilosExcel.autoSize(hoja);
        }

        private void escribirUnivariado(XSSFWorkbook wb) {
                Sheet hoja = wb.createSheet("Univariado");
                int r = 0;
                r = EstilosExcel.escribirTitulo(wb, hoja, r,
                                "Análisis univariado de los empleadores");

                List<Map<String, String>> m = analisis.mapasEmpleadores();
                int n = m.size();

                r = escribirSubtitulo(wb, hoja, r, "Indicadores clave con IC 95% (Wilson)");
                r = EstilosExcel.escribirEncabezados(wb, hoja, r,
                                "Indicador", "Proporción (%)", "IC 95% inferior (%)", "IC 95% superior (%)", "n");
                r = filaKpi(wb, hoja, r, "Contrató en los últimos 5 años",
                                analisis.proporcionDonde(m, "contrato_ultimos_5_anios", v -> v.equals("SI")), n);
                r = filaKpi(wb, hoja, r, "Alta probabilidad de contratar",
                                analisis.proporcionDonde(m, "posibilidad_futura_contratacion",
                                                v -> v.equals("ALTA_PROBABILIDAD")),
                                n);
                r = filaKpi(wb, hoja, r, "Demanda de IA",
                                analisis.proporcionDemandaIA(), n);
                r = EstilosExcel.filaVacia(r);

                r = escribirSubtitulo(wb, hoja, r, "Medias de las dimensiones Likert (escala 1-3)");
                double mediaA = promedio(analisis.mediasPorDimension("A"));
                double mediaB = promedio(analisis.mediasPorDimension("B"));
                double media4 = analisis.fichaItem4().media();
                r = EstilosExcel.escribirEncabezados(wb, hoja, r,
                                "Dimensión", "Ítems", "Media (1-3)", "n");
                r = EstilosExcel.escribirFilaNumerica(wb, hoja, r,
                                "A — Calidad e Imagen", ServicioAnalisis.DIM_A_ITEMS.size(),
                                Double.isNaN(mediaA) ? "" : redondear(mediaA),
                                analisis.mediasPorDimension("A").size());
                r = EstilosExcel.escribirFilaNumerica(wb, hoja, r,
                                "B — Vinculación y Retroalimentación", ServicioAnalisis.DIM_B_ITEMS.size(),
                                Double.isNaN(mediaB) ? "" : redondear(mediaB),
                                analisis.mediasPorDimension("B").size());
                r = EstilosExcel.escribirFilaNumerica(wb, hoja, r,
                                "Ítem 4 — Conoce el perfil de egreso", 1,
                                Double.isNaN(media4) ? "" : redondear(media4),
                                analisis.fichaItem4().n());
                r = EstilosExcel.filaVacia(r);

                r = escribirDistribucion(wb, hoja, r, "Tipo de organización",
                                analisis.frecuenciasTipoOrg());
                r = escribirDistribucion(wb, hoja, r, "Tamaño de la organización",
                                analisis.frecuenciasTamano());
                r = escribirDistribucion(wb, hoja, r, "Tamaño agrupado",
                                analisis.frecuenciasTamanoAgrupado());
                r = escribirDistribucion(wb, hoja, r, "Sector / Rubro",
                                analisis.frecuenciasSector());
                r = escribirDistribucion(wb, hoja, r, "Rubro agrupado",
                                analisis.frecuenciasRubroAgrupado());
                r = escribirDistribucion(wb, hoja, r, "Nivel académico demandado",
                                analisis.frecuenciasNivelAcademico());
                r = escribirDistribucion(wb, hoja, r, "¿Contrató en los últimos 5 años?",
                                analisis.frecuenciasContrato5a());
                r = escribirDistribucion(wb, hoja, r, "Proyección de contratación",
                                analisis.frecuenciasProyeccion());
                r = escribirDistribucion(wb, hoja, r, "Demanda de IA",
                                analisis.frecuenciasDemandaIA());

                r = escribirDistribucion(wb, hoja, r, "Medio de convocatoria (multi-selección)",
                                analisis.distribucionMedio());
                r = escribirDistribucion(wb, hoja, r, "Cargos desempeñados (multi-selección)",
                                analisis.distribucionCargos());
                r = escribirDistribucion(wb, hoja, r, "Tendencias emergentes (multi-selección)",
                                analisis.distribucionTendencias());
                r = escribirDistribucion(wb, hoja, r, "Hard skills (multi-selección)",
                                analisis.distribucionHardSkills());
                r = escribirDistribucion(wb, hoja, r, "Soft skills (multi-selección)",
                                analisis.distribucionSoftSkills());

                EstilosExcel.autoSize(hoja);
        }

        private int escribirDistribucion(XSSFWorkbook wb, Sheet hoja, int r,
                        String titulo, Map<String, Integer> frec) {
                r = escribirSubtitulo(wb, hoja, r, titulo);
                int total = frec.values().stream().mapToInt(Integer::intValue).sum();
                r = EstilosExcel.escribirEncabezados(wb, hoja, r,
                                "Categoría", "Frecuencia (n)", "Porcentaje (%)");
                for (Map.Entry<String, Integer> e : frec.entrySet()) {
                        double pct = total == 0 ? 0 : 100.0 * e.getValue() / total;
                        r = EstilosExcel.escribirFilaNumerica(wb, hoja, r,
                                        e.getKey(), e.getValue(), redondear(pct));
                }
                r = EstilosExcel.filaVacia(r);
                return r;
        }

        private int filaKpi(XSSFWorkbook wb, Sheet hoja, int r,
                        String etiqueta, double proporcion, int n) {
                RangoConfianza ic = IntervalosConfianza.paraProporcion(proporcion, n);
                return EstilosExcel.escribirFilaNumerica(wb, hoja, r,
                                etiqueta,
                                redondear(proporcion * 100),
                                redondear(ic.limiteInferior() * 100),
                                redondear(ic.limiteSuperior() * 100),
                                n);
        }

        private void escribirLikertDescriptivo(XSSFWorkbook wb) {
                Sheet hoja = wb.createSheet("Likert descriptivo");
                int r = 0;
                r = EstilosExcel.escribirTitulo(wb, hoja, r,
                                "Evaluación institucional — 18 ítems Likert (escala 1-3)");
                r = EstilosExcel.escribirFila(wb, hoja, r,
                                "Escala: 1 = Totalmente en desacuerdo, 2 = Parcialmente de acuerdo, "
                                                + "3 = Totalmente de acuerdo. 'No sabe' y vacío se excluyen.");
                r = EstilosExcel.filaVacia(r);

                r = EstilosExcel.escribirEncabezados(wb, hoja, r,
                                "Ítem", "Etiqueta", "n", "Media (1-3)", "Mediana", "Desv.", "% Acuerdo");
                List<Map<String, String>> mapas = analisis.mapasEmpleadores();
                for (String item : ServicioAnalisis.ITEMS_LIKERT) {
                        ServicioAnalisis.ResumenItem res = analisis.resumenItem(item);
                        List<Double> valores = analisis.likertDe(mapas, item);
                        double mediana = medianaDe(valores);
                        r = EstilosExcel.escribirFilaNumerica(wb, hoja, r,
                                        item,
                                        res.etiqueta(),
                                        res.n(),
                                        Double.isNaN(res.media()) ? "" : redondear(res.media()),
                                        Double.isNaN(mediana) ? "" : redondear(mediana),
                                        Double.isNaN(res.desviacion()) ? "" : redondear(res.desviacion()),
                                        Double.isNaN(res.pctAcuerdo()) ? "" : redondear(res.pctAcuerdo()));
                }
                r = EstilosExcel.filaVacia(r);

                r = escribirSubtitulo(wb, hoja, r, "Agrupación por dimensiones");
                double mediaA = promedio(analisis.mediasPorDimension("A"));
                double mediaB = promedio(analisis.mediasPorDimension("B"));
                double media4 = analisis.fichaItem4().media();

                r = EstilosExcel.escribirEncabezados(wb, hoja, r,
                                "Dimensión", "Ítems que la componen", "n (encuestados)", "Media (1-3)");
                r = EstilosExcel.escribirFilaNumerica(wb, hoja, r,
                                "A — Calidad e Imagen",
                                String.join(", ", ServicioAnalisis.DIM_A_ITEMS),
                                analisis.mediasPorDimension("A").size(),
                                Double.isNaN(mediaA) ? "" : redondear(mediaA));
                r = EstilosExcel.escribirFilaNumerica(wb, hoja, r,
                                "B — Vinculación y Retroalimentación",
                                String.join(", ", ServicioAnalisis.DIM_B_ITEMS),
                                analisis.mediasPorDimension("B").size(),
                                Double.isNaN(mediaB) ? "" : redondear(mediaB));
                r = EstilosExcel.escribirFilaNumerica(wb, hoja, r,
                                "Ítem 4 — Conoce el perfil de egreso",
                                ServicioAnalisis.ITEM_INDEPENDIENTE,
                                analisis.fichaItem4().n(),
                                Double.isNaN(media4) ? "" : redondear(media4));

                hoja.setColumnWidth(0, 40 * 256);
                hoja.setColumnWidth(1, 55 * 256);
                EstilosExcel.autoSize(hoja);
        }

        private void escribirBivariadoObservadas(XSSFWorkbook wb) {
                Sheet hoja = wb.createSheet("Bivariado - Observadas");
                int r = 0;
                r = EstilosExcel.escribirTitulo(wb, hoja, r,
                                "Matrices de contingencia — frecuencias observadas (empleadores)");
                r = EstilosExcel.escribirFila(wb, hoja, r,
                                "Cada tabla muestra las frecuencias observadas del cruce.");
                r = EstilosExcel.filaVacia(r);

                List<FichaCruce> lista = cruces.crucesEmpleadores();
                int numero = 0;
                for (FichaCruce c : lista) {
                        numero++;
                        var res = c.resultado();
                        if (res == null || res.observadas() == null || res.observadas().length == 0) {
                                r = escribirSubtitulo(wb, hoja, r, numero + ". " + c.etiqueta() + " — sin datos");
                                r = EstilosExcel.filaVacia(r);
                                continue;
                        }
                        r = escribirSubtitulo(wb, hoja, r, numero + ". " + c.etiqueta());
                        r = escribirMatrizObservada(wb, hoja, r, res);
                        r = EstilosExcel.filaVacia(r);
                }
                EstilosExcel.autoSize(hoja);
        }

        private int escribirMatrizObservada(XSSFWorkbook wb, Sheet hoja, int r,
                        bo.umss.mercado.nucleo.stats.ResultadoChiCuadrada res) {
                String[] enc = new String[res.columnas().size() + 2];
                enc[0] = "Fila \\ Columna";
                for (int j = 0; j < res.columnas().size(); j++) {
                        enc[j + 1] = res.columnas().get(j);
                }
                enc[enc.length - 1] = "Total";
                r = EstilosExcel.escribirEncabezados(wb, hoja, r, enc);

                for (int i = 0; i < res.filas().size(); i++) {
                        Object[] fila = new Object[res.columnas().size() + 2];
                        fila[0] = res.filas().get(i);
                        long totalFila = 0;
                        for (int j = 0; j < res.columnas().size(); j++) {
                                fila[j + 1] = res.observadas()[i][j];
                                totalFila += res.observadas()[i][j];
                        }
                        fila[fila.length - 1] = totalFila;
                        r = EstilosExcel.escribirFilaNumerica(wb, hoja, r, fila);
                }

                Object[] totales = new Object[res.columnas().size() + 2];
                totales[0] = "TOTAL";
                long granTotal = 0;
                for (int j = 0; j < res.columnas().size(); j++) {
                        long suma = 0;
                        for (int i = 0; i < res.filas().size(); i++) {
                                suma += res.observadas()[i][j];
                        }
                        totales[j + 1] = suma;
                        granTotal += suma;
                }
                totales[totales.length - 1] = granTotal;
                r = EstilosExcel.escribirFilaNumerica(wb, hoja, r, totales);
                return r;
        }

        private void escribirBivariadoEstadisticos(XSSFWorkbook wb) {
                Sheet hoja = wb.createSheet("Bivariado - Estadísticos");
                int r = 0;
                r = EstilosExcel.escribirTitulo(wb, hoja, r,
                                "Estadísticos de los cruces χ² de empleadores");
                r = EstilosExcel.escribirFila(wb, hoja, r,
                                "Solo se reportan los estadísticos originales. "
                                                + "No se incluye corrección de Holm-Bonferroni.");
                r = EstilosExcel.filaVacia(r);

                r = EstilosExcel.escribirEncabezados(wb, hoja, r,
                                "N°", "Cruce", "χ²", "gl", "p-valor", "V de Cramér",
                                "Intensidad", "Significativa (p<0.05)");

                List<FichaCruce> lista = cruces.crucesEmpleadores();
                for (int i = 0; i < lista.size(); i++) {
                        FichaCruce c = lista.get(i);
                        var res = c.resultado();
                        if (res == null) {
                                continue;
                        }
                        r = EstilosExcel.escribirFilaNumerica(wb, hoja, r,
                                        i + 1,
                                        c.etiqueta(),
                                        redondear(res.chiCuadrada()),
                                        res.gradosLibertad(),
                                        redondear(res.valorP()),
                                        redondear(res.vCramer()),
                                        res.intensidadVCramer(),
                                        res.significativa() ? "SI" : "NO");
                }
                EstilosExcel.autoSize(hoja);
        }

        private void escribirMultivariado(XSSFWorkbook wb) {
                Sheet hoja = wb.createSheet("Multivariado");
                int r = 0;
                r = EstilosExcel.escribirTitulo(wb, hoja, r,
                                "Modelamiento multivariable (media condicional, Ecuación 10)");
                r = EstilosExcel.filaVacia(r);

                r = escribirTablaMultivariable(wb, hoja, r,
                                "Tamaño × Tipo × Media Likert global",
                                analisis.tablaMultivariableEmpleadores(
                                                "tamano_agrupado", "tipo_organizacion"),
                                "1-3");

                r = escribirTablaMultivariable(wb, hoja, r,
                                "Rubro × Nivel académico × Proyección",
                                analisis.tablaMultivariable(
                                                analisis.mapasEmpleadores(),
                                                "rubro_agrupado", "nivel_academico_grupo", "proyeccion_num"),
                                "1-3");

                r = escribirTablaMultivariable(wb, hoja, r,
                                "Tamaño × Tipo × Cantidad de cargos",
                                analisis.tablaMultivariable(
                                                analisis.mapasEmpleadores(),
                                                "tamano_agrupado", "tipo_organizacion", "cantidad_cargos"),
                                "cargos");

                EstilosExcel.autoSize(hoja);
        }

        private int escribirTablaMultivariable(XSSFWorkbook wb, Sheet hoja, int r,
                        String titulo, ServicioAnalisis.TablaMultivariable tabla, String unidad) {
                r = escribirSubtitulo(wb, hoja, r, titulo);
                r = EstilosExcel.escribirEncabezados(wb, hoja, r,
                                "X: " + tabla.etiquetaX(),
                                "Y: " + tabla.etiquetaY(),
                                "n",
                                "Media Z (" + tabla.etiquetaZ() + ") en " + unidad);
                for (var celda : tabla.celdas()) {
                        if (celda.n() == 0) {
                                continue;
                        }
                        double media = celda.mediaZ();
                        r = EstilosExcel.escribirFilaNumerica(wb, hoja, r,
                                        celda.valorX(),
                                        celda.valorY(),
                                        celda.n(),
                                        Double.isNaN(media) ? "" : redondear(media));
                }
                r = EstilosExcel.filaVacia(r);
                return r;
        }

        private int escribirSubtitulo(XSSFWorkbook wb, Sheet hoja, int r, String texto) {
                Row row = hoja.getRow(r);
                if (row == null) {
                        row = hoja.createRow(r);
                }
                var c = row.createCell(0);
                c.setCellValue(texto);
                c.setCellStyle(EstilosExcel.headerSecundario(wb));
                return r + 1;
        }

        private static double promedio(List<Double> valores) {
                if (valores == null || valores.isEmpty()) {
                        return Double.NaN;
                }
                double suma = 0;
                for (double v : valores) {
                        suma += v;
                }
                return suma / valores.size();
        }

        private static double medianaDe(List<Double> valores) {
                if (valores == null || valores.isEmpty()) {
                        return Double.NaN;
                }
                List<Double> copia = new ArrayList<>(valores);
                copia.sort(Double::compare);
                int n = copia.size();
                if (n % 2 == 1) {
                        return copia.get(n / 2);
                }
                return (copia.get(n / 2 - 1) + copia.get(n / 2)) / 2.0;
        }

        private static double redondear(double valor) {
                if (Double.isNaN(valor)) {
                        return Double.NaN;
                }
                return Math.round(valor * 10000.0) / 10000.0;
        }
}