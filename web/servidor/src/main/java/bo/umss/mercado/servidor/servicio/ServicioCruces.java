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
        c.add(armarCruce("Formacion Continua x Interes Posgrado", "realizo_formacion_complementaria", "interes_posgrado", m));
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
        c.add(armarCruce("4.2.1 Tamano x Proyeccion", "tamano_agrupado", "posibilidad_futura_contratacion", m));
        c.add(armarCruce("4.2.1 Tamano x Medio", "tamano_agrupado", "medio_convocatoria", m));
        c.add(armarCruce("4.2.1 Tipo x Nivel Academico", "tipo_organizacion", "nivel_formacion_demanda", m));
        c.add(armarCruce("4.2.1 Rubro x Nivel Academico", "rubro_agrupado", "nivel_formacion_demanda", m));
        c.add(armarCruce("4.2.2 Demanda IA x Tamano", "demanda_ia", "tamano_agrupado", m));
        c.add(armarCruce("4.2.2 Demanda IA x Sector", "demanda_ia", "rubro_agrupado", m));
        c.add(armarCruce("4.2.3 L8 (competencias) x L6 (desempeno)",
                "likert_competencias_laborales", "likert_formacion_desempeno", m));
        c.add(armarCruce("4.2.3 L11 (vinculos) x L17 (recurre UMSS)",
                "likert_vinculos_organizaciones", "likert_recurre_umss", m));
        c.add(armarCruce("4.2.3 L3 (consulta) x L7 (incorpora)",
                "likert_consulta_regular", "likert_considera_opinion", m));
        return c;
    }

    public record FilaMatrizBivariada(
            String etiqueta, double chi, int gl, double pOriginal,
            double pHolm, double vCramer, String intensidad,
            boolean significativa) {}

    private List<FilaMatrizBivariada> matrizDesdeCruces(List<FichaCruce> cruces) {
        List<Double> ps = new ArrayList<>();
        for (FichaCruce c : cruces) ps.add(c.resultado().valorP());
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

    public List<double[]> wilsonPorFila(ResultadoChiCuadrada r) {
        List<double[]> out = new ArrayList<>();
        if (r.observadas() == null) return out;
        for (int i = 0; i < r.observadas().length; i++) {
            long total = 0;
            for (long v : r.observadas()[i]) total += v;
            for (int j = 0; j < r.observadas()[i].length; j++) {
                long obs = r.observadas()[i][j];
                double p = total == 0 ? Double.NaN : (double) obs / total;
                double[] ic = {Double.NaN, Double.NaN};
                if (!Double.isNaN(p) && total > 0) {
                    var w = bo.umss.mercado.nucleo.stats.IntervalosConfianza
                            .paraProporcion(p, (int) total);
                    ic[0] = w.limiteInferior();
                    ic[1] = w.limiteSuperior();
                }
                out.add(ic);
            }
        }
        return out;
    }
}