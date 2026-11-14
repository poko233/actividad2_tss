package bo.umss.mercado.servidor.servicio.exportacion;

import bo.umss.mercado.nucleo.stats.IntervalosConfianza;
import bo.umss.mercado.nucleo.stats.RangoConfianza;
import bo.umss.mercado.nucleo.stats.ResumenNumerico;
import bo.umss.mercado.servidor.servicio.FichaCruce;
import bo.umss.mercado.servidor.servicio.ServicioAnalisis;
import bo.umss.mercado.servidor.servicio.ServicioCruces;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class ExportadorExcelTitulados {

        private final ServicioAnalisis analisis;
        private final ServicioCruces cruces;

        public ExportadorExcelTitulados(ServicioAnalisis analisis, ServicioCruces cruces) {
                this.analisis = analisis;
                this.cruces = cruces;
        }

        public byte[] exportar() {
                try (XSSFWorkbook wb = new XSSFWorkbook()) {
                        escribirDatosCrudos(wb);
                        escribirUnivariado(wb);
                        escribirBivariadoObservadas(wb);
                        escribirBivariadoEstadisticos(wb);
                        escribirMultivariado(wb);
                        ByteArrayOutputStream salida = new ByteArrayOutputStream();
                        wb.write(salida);
                        return salida.toByteArray();
                } catch (IOException e) {
                        throw new IllegalStateException(
                                        "No se pudo generar el Excel de titulados: " + e.getMessage(), e);
                }
        }

        private void escribirDatosCrudos(XSSFWorkbook wb) {
                List<Map<String, String>> filas = analisis.mapasTitulados();
                Sheet hoja = wb.createSheet("Datos crudos");

                int r = 0;
                r = EstilosExcel.escribirTitulo(wb, hoja, r,
                                "Base de datos de titulados (N = " + filas.size() + ")");

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
                                "Análisis univariado de los titulados");

                r = escribirSubtitulo(wb, hoja, r, "Indicadores clave con IC 95% (Wilson)");
                List<Map<String, String>> m = analisis.mapasTitulados();
                int n = m.size();

                r = EstilosExcel.escribirEncabezados(wb, hoja, r,
                                "Indicador", "Proporción (%)", "IC 95% inferior (%)", "IC 95% superior (%)", "n");
                r = filaKpi(wb, hoja, r, "Trabajan en organización", analisis.tasaInsercion(), n);
                r = filaKpi(wb, hoja, r, "Interesados en posgrado", analisis.intencionPosgrado(), n);
                r = filaKpi(wb, hoja, r, "Financiaron con recursos propios",
                                analisis.proporcionRequiereBeca(), n);
                r = filaKpi(wb, hoja, r, "Consideran formación pertinente",
                                analisis.proporcionDonde(m, "pertinencia_trabajo",
                                                v -> bo.umss.mercado.nucleo.limpieza.LimpiezaDatos
                                                                .normalizar(v).contains("ACUERDO")),
                                n);
                r = EstilosExcel.filaVacia(r);

                r = escribirDistribucion(wb, hoja, r, "Género", "genero");
                r = escribirDistribucion(wb, hoja, r, "Situación laboral", "situacion_laboral_actual");
                r = escribirDistribucion(wb, hoja, r, "Tiempo al primer empleo", "tiempo_primer_empleo");
                r = escribirDistribucion(wb, hoja, r, "Nivel de posgrado", "nivel_posgrado");
                r = escribirDistribucion(wb, hoja, r, "Fuente de financiamiento", "financiamiento_formacion");
                r = escribirDistribucion(wb, hoja, r, "Área de interés de posgrado", "area_interes_posgrado");
                r = escribirDistribucion(wb, hoja, r, "Remuneración", "remuneracion_rango");

                r = escribirSubtitulo(wb, hoja, r, "Resumen numérico: años de vida profesional");
                ResumenNumerico rn = analisis.resumenNumerico("anios_vida_profesional");
                r = EstilosExcel.escribirEncabezados(wb, hoja, r,
                                "n", "Media", "Mediana", "Desv.", "Mínimo", "Máximo", "Q1", "Q3", "CV %");
                double cv = (rn.media() == 0 || Double.isNaN(rn.media()))
                                ? Double.NaN
                                : 100.0 * rn.desv() / rn.media();
                r = EstilosExcel.escribirFilaNumerica(wb, hoja, r,
                                rn.totalDatos(), rn.media(), rn.mediana(), rn.desv(),
                                rn.minimo(), rn.maximo(), rn.q1(), rn.q3(), cv);

                EstilosExcel.autoSize(hoja);
        }

        private int escribirDistribucion(XSSFWorkbook wb, Sheet hoja, int r,
                        String titulo, String columna) {
                r = escribirSubtitulo(wb, hoja, r, titulo);
                Map<String, Integer> frec = analisis.frecuenciasDe(columna);
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

        private void escribirBivariadoObservadas(XSSFWorkbook wb) {
                Sheet hoja = wb.createSheet("Bivariado - Observadas");
                int r = 0;
                r = EstilosExcel.escribirTitulo(wb, hoja, r,
                                "Matrices de contingencia — frecuencias observadas");
                r = EstilosExcel.escribirFila(wb, hoja, r,
                                "Cada tabla muestra las frecuencias observadas del cruce.");
                r = EstilosExcel.filaVacia(r);

                List<FichaCruce> lista = cruces.crucesTitulados();
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
                                "Estadísticos de los cruces χ² de titulados");
                r = EstilosExcel.escribirFila(wb, hoja, r,
                                "Solo se reportan los estadísticos originales. ");
                r = EstilosExcel.filaVacia(r);

                r = EstilosExcel.escribirEncabezados(wb, hoja, r,
                                "N°", "Cruce", "χ²", "gl", "p-valor", "V de Cramér",
                                "Intensidad", "Significativa (p<0.05)");

                List<FichaCruce> lista = cruces.crucesTitulados();
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
                                "Área de interés × Financiamiento × Años de vida profesional",
                                analisis.tablaMultivariableTitulados(
                                                "area_interes_posgrado", "financiamiento_formacion",
                                                "anios_vida_profesional"),
                                "años");

                r = escribirTablaMultivariable(wb, hoja, r,
                                "Sector × Cargo × Remuneración",
                                analisis.tablaMultivariableTitulados(
                                                "sector", "cargo_grupo", "remuneracion_rango"),
                                "Bs.");

                r = escribirTablaMultivariable(wb, hoja, r,
                                "Satisfacción × Concordancia × Años de vida profesional",
                                analisis.tablaMultivariableTitulados(
                                                "satisfaccion_grupo", "concordancia_grupo",
                                                "anios_vida_profesional"),
                                "años");

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

        private static double redondear(double valor) {
                if (Double.isNaN(valor)) {
                        return Double.NaN;
                }
                return Math.round(valor * 10000.0) / 10000.0;
        }
}