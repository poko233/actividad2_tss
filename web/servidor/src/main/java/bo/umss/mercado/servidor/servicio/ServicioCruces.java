package bo.umss.mercado.servidor.servicio;

import bo.umss.mercado.nucleo.guia.GuiasLectura;
import bo.umss.mercado.nucleo.stats.HolmCorreccion;
import bo.umss.mercado.nucleo.stats.PruebaChiCuadrada;
import bo.umss.mercado.nucleo.stats.ResultadoChiCuadrada;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ServicioCruces {

    private final ServicioAnalisis analisis;

    public ServicioCruces(ServicioAnalisis analisis) {
        this.analisis = analisis;
    }

    private List<String> columnaDerivada(List<Map<String, String>> mapas, String columna) {
        List<String> salida = new ArrayList<>();
        for (Map<String, String> f : mapas) {
            String derivado = analisis.valorDerivado(f, columna);
            salida.add(derivado != null ? derivado : f.getOrDefault(columna, ""));
        }
        return salida;
    }

    private FichaCruce armarCruce(String etiqueta, String colA, String colB,
            List<Map<String, String>> mapas) {
        ResultadoChiCuadrada r = PruebaChiCuadrada.cruzar(
                columnaDerivada(mapas, colA), columnaDerivada(mapas, colB));
        String frase = GuiasLectura.fraseDependencia(colA, colB, r.valorP());
        return new FichaCruce(etiqueta, colA, colB, r, frase);
    }

    public List<FichaCruce> crucesTitulados() {
        List<Map<String, String>> m = analisis.mapasTitulados();
        List<FichaCruce> c = new ArrayList<>();
        c.add(armarCruce("Edad x Situacion Laboral", "edad_rango", "situacion_laboral_actual", m));
        c.add(armarCruce("Genero x Situacion Laboral", "genero", "situacion_laboral_actual", m));
        c.add(armarCruce("Cohorte x Situacion Laboral", "segmento_antiguedad", "situacion_laboral_actual", m));
        c.add(armarCruce("Genero x Tiempo al Primer Empleo", "genero", "tiempo_primer_grupo", m));
        c.add(armarCruce("Cohorte x Numero de Empleos", "segmento_antiguedad", "num_empleos_grupo", m));
        c.add(armarCruce("Edad x Remuneracion", "edad_grupo", "remuneracion_grupo", m));
        c.add(armarCruce("Sector x Remuneracion", "sector", "remuneracion_grupo", m));
        c.add(armarCruce("Genero x Remuneracion", "genero", "remuneracion_grupo", m));
        c.add(armarCruce("Formacion Continua x Remuneracion", "formacion_grupo", "remuneracion_grupo", m));
        c.add(armarCruce("Formacion Continua x Interes Posgrado", "realizo_formacion_complementaria",
                "interes_posgrado", m));
        c.add(armarCruce("Nivel de Posgrado x Lugar", "nivel_grupo", "lugar_grupo", m));
        c.add(armarCruce("Percepcion FCyT x Organizacion Preferida", "percepcion_grupo", "organizacion_grupo", m));
        c.add(armarCruce("Cohorte x Area de Interes Posgrado", "segmento_antiguedad", "area_interes_grupo", m));
        c.add(armarCruce("Satisfaccion Global x Concordancia", "satisfaccion_grupo", "concordancia_grupo", m));
        c.add(armarCruce("Pertinencia del Trabajo x Satisfaccion", "pertinencia_grupo", "satisfaccion_grupo", m));
        return c;
    }

    public List<FichaCruce> crucesEmpleadores() {
        List<Map<String, String>> m = analisis.mapasEmpleadores();
        List<FichaCruce> c = new ArrayList<>();
        c.add(armarCruce("Tamano x Proyeccion", "tamano_agrupado", "posibilidad_futura_contratacion", m));
        c.add(armarCruce("Tamano x Medio", "tamano_agrupado", "medio_convocatoria", m));
        c.add(armarCruce("Tipo x Nivel Academico", "tipo_organizacion", "nivel_formacion_demanda", m));
        c.add(armarCruce("Rubro x Nivel Academico", "rubro_agrupado", "nivel_formacion_demanda", m));
        c.add(armarCruce("Demanda IA x Tamano", "demanda_ia", "tamano_agrupado", m));
        c.add(armarCruce("Demanda IA x Sector", "demanda_ia", "rubro_agrupado", m));
        c.add(armarCruce("L8 (Competencias) x L6 (Desempeño)",
                "likert_competencias_laborales_grupo", "likert_formacion_desempeno_grupo", m));
        c.add(armarCruce("L11 (Vínculos) x L17 (Recurre UMSS)",
                "likert_vinculos_organizaciones_grupo", "likert_recurre_umss_grupo", m));
        c.add(armarCruce("L3 (Consulta) x L7 (Incorpora)",
                "likert_consulta_regular_grupo", "likert_considera_opinion_grupo", m));
        return c;
    }

    public record FilaMatrizBivariada(
            String etiqueta, double chi, int gl, double pOriginal,
            double pHolm, double vCramer, String intensidad,
            boolean significativa) {
    }

    private List<FilaMatrizBivariada> matrizDesdeCruces(List<FichaCruce> cruces) {
        List<Double> ps = new ArrayList<>();
        for (FichaCruce c : cruces)
            ps.add(c.resultado().valorP());
        List<HolmCorreccion.ItemHolm> ajustados = HolmCorreccion.ajustar(ps);

        List<FilaMatrizBivariada> out = new ArrayList<>();
        for (int i = 0; i < cruces.size(); i++) {
            FichaCruce c = cruces.get(i);
            ResultadoChiCuadrada r = c.resultado();
            HolmCorreccion.ItemHolm h = ajustados.get(i);
            out.add(new FilaMatrizBivariada(
                    c.etiqueta(), r.chiCuadrada(), r.gradosLibertad(),
                    r.valorP(), h.valorPAjustado(), r.vCramer(),
                    r.intensidadVCramer(), h.significativo()));
        }
        return out;
    }

    public List<FilaMatrizBivariada> matrizResumenTitulados() {
        return matrizDesdeCruces(crucesTitulados());
    }

    public List<FilaMatrizBivariada> matrizResumenEmpleadores() {
        return matrizDesdeCruces(crucesEmpleadores());
    }

    // ================================================================
    // IC Wilson por fila (fórmula correcta, no Wald)
    // ================================================================
    public List<double[]> wilsonPorFila(ResultadoChiCuadrada r) {
        List<double[]> out = new ArrayList<>();
        if (r.observadas() == null)
            return out;
        double z = 1.96;
        double z2 = z * z;
        for (int i = 0; i < r.observadas().length; i++) {
            long total = 0;
            for (long v : r.observadas()[i])
                total += v;
            for (int j = 0; j < r.observadas()[i].length; j++) {
                long obs = r.observadas()[i][j];
                double[] ic = { Double.NaN, Double.NaN };
                if (total > 0) {
                    double p = (double) obs / total;
                    double n = (double) total;
                    double centro = (p + z2 / (2 * n)) / (1 + z2 / n);
                    double margen = (z / (1 + z2 / n))
                            * Math.sqrt(p * (1 - p) / n + z2 / (4 * n * n));
                    ic[0] = Math.max(0, centro - margen);
                    ic[1] = Math.min(1, centro + margen);
                }
                out.add(ic);
            }
        }
        return out;
    }

    // ================================================================
    // Interpretación dinámica basada en los datos reales del cruce
    // ================================================================
    public String interpretarCruceDinamico(FichaCruce cruce) {
        ResultadoChiCuadrada r = cruce.resultado();
        if (r == null || r.observadas() == null || r.observadas().length == 0) {
            return "No hay datos suficientes para interpretar este cruce.";
        }

        int filas = r.filas().size();
        int cols = r.columnas().size();

        long totalGeneral = 0;
        long[] totalPorCol = new long[cols];
        long[] totalPorFila = new long[filas];
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < cols; j++) {
                totalPorFila[i] += r.observadas()[i][j];
                totalPorCol[j] += r.observadas()[i][j];
                totalGeneral += r.observadas()[i][j];
            }
        }

        StringBuilder sb = new StringBuilder();

        if (!r.significativa()) {
            sb.append("No se encontró evidencia estadística de asociación entre ")
                    .append(humanizar(cruce.columnaA())).append(" y ")
                    .append(humanizar(cruce.columnaB()))
                    .append(" (χ²=").append(redondear(r.chiCuadrada(), 2))
                    .append(", gl=").append(r.gradosLibertad())
                    .append(", p=").append(redondear(r.valorP(), 4)).append("). ");
            sb.append("Los intervalos de confianza al 95% se solapan, lo que sugiere ")
                    .append("que ambas variables son independientes. ");
            sb.append("No se recomienda implementar políticas diferenciadas en función ")
                    .append("de esta variable.");
            return sb.toString();
        }

        String intensidad = r.intensidadVCramer();
        sb.append("Existe una asociación estadísticamente significativa ");
        if (intensidad.equals("grande") || intensidad.equals("mediano")) {
            sb.append("y de intensidad ").append(intensidad);
        }
        sb.append(" entre ").append(humanizar(cruce.columnaA())).append(" y ")
                .append(humanizar(cruce.columnaB())).append(" ")
                .append("(χ²=").append(redondear(r.chiCuadrada(), 2))
                .append(", gl=").append(r.gradosLibertad())
                .append(", p=").append(redondear(r.valorP(), 4))
                .append(", V Cramér=").append(redondear(r.vCramer(), 3)).append("). ");

        int mejorFila = -1, mejorCol = -1;
        double mejorDesvio = 0;
        int peorFila = -1, peorCol = -1;
        double peorDesvio = 0;

        for (int i = 0; i < filas; i++) {
            if (totalPorFila[i] < 10)
                continue;
            for (int j = 0; j < cols; j++) {
                double esperada = (double) totalPorFila[i] * totalPorCol[j] / totalGeneral;
                double desvio = r.observadas()[i][j] - esperada;
                if (desvio > mejorDesvio) {
                    mejorDesvio = desvio;
                    mejorFila = i;
                    mejorCol = j;
                }
                if (desvio < peorDesvio) {
                    peorDesvio = desvio;
                    peorFila = i;
                    peorCol = j;
                }
            }
        }

        if (mejorFila >= 0) {
            double prop = (double) r.observadas()[mejorFila][mejorCol] / totalPorFila[mejorFila];
            double propGlobal = (double) totalPorCol[mejorCol] / totalGeneral;
            sb.append("El patrón más fuerte se observa en \"").append(r.filas().get(mejorFila))
                    .append("\" con \"").append(r.columnas().get(mejorCol)).append("\": ")
                    .append(redondear(prop * 100, 1)).append("% (n=")
                    .append(totalPorFila[mejorFila]).append(") frente al ")
                    .append(redondear(propGlobal * 100, 1)).append("% global. ");
        }

        if (peorFila >= 0 && peorFila != mejorFila) {
            double prop = (double) r.observadas()[peorFila][peorCol] / totalPorFila[peorFila];
            sb.append("En contraste, \"").append(r.filas().get(peorFila))
                    .append("\" muestra la menor proporción de \"")
                    .append(r.columnas().get(peorCol)).append("\" (")
                    .append(redondear(prop * 100, 1)).append("%). ");
        }

        if (intensidad.equals("grande")) {
            sb.append("La asociación es fuerte, por lo que se recomienda diseñar políticas ")
                    .append("focalizadas hacia los segmentos identificados.");
        } else if (intensidad.equals("mediano")) {
            sb.append("La asociación es moderada, por lo que conviene monitorear el fenómeno ")
                    .append("y considerar acciones específicas para los segmentos identificados.");
        } else {
            sb.append("Aunque la asociación es significativa, la baja intensidad sugiere ")
                    .append("revisar los datos con más detalle antes de implementar cambios estructurales.");
        }

        return sb.toString();
    }

    private static String humanizar(String col) {
        return col.replace("_", " ");
    }

    private static String redondear(double v, int dec) {
        if (Double.isNaN(v))
            return "n/d";
        double f = Math.pow(10, dec);
        return String.valueOf(Math.round(v * f) / f);
    }
}
