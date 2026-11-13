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

    // ---- helper: resuelve columnas derivadas ----
    private List<String> columnaDerivada(List<Map<String, String>> mapas, String columna) {
        if (columna.equals("segmento_antiguedad")) {
            List<String> s = new ArrayList<>();
            for (Map<String, String> f : mapas) {
                try {
                    double a = Double.parseDouble(f.getOrDefault("anios_vida_profesional", "").replace(",", "."));
                    s.add(ServicioAnalisis.segmentoDe(a));
                } catch (Exception e) { s.add(""); }
            }
            return s;
        }
        return analisis.columnaDe(mapas, columna);
    }

    private FichaCruce armarCruce(String etiqueta, String colA, String colB,
            List<Map<String, String>> mapas) {
        ResultadoChiCuadrada r = PruebaChiCuadrada.cruzar(
                columnaDerivada(mapas, colA), columnaDerivada(mapas, colB));
        String frase = GuiasLectura.fraseDependencia(colA, colB, r.valorP());
        return new FichaCruce(etiqueta, colA, colB, r, frase);
    }

    // ================================================================
    // CRUCES TITULADOS (HU1/HU2/HU3)
    // ================================================================
    public List<FichaCruce> crucesTitulados() {
        List<Map<String, String>> m = analisis.mapasTitulados();
        List<FichaCruce> c = new ArrayList<>();
        c.add(armarCruce("HU2: area x financiamiento", "area_interes_posgrado", "financiamiento_formacion", m));
        c.add(armarCruce("HU2/HU3: nivel x institucion", "nivel_posgrado", "institucion_formacion", m));
        c.add(armarCruce("HU3: financiamiento x nivel", "financiamiento_formacion", "nivel_posgrado", m));
        c.add(armarCruce("HU1: situacion x satisfaccion", "situacion_laboral_actual", "satisfaccion_formacion", m));
        c.add(armarCruce("HU1: antiguedad x intencion", "segmento_antiguedad", "interes_posgrado", m));
        return c;
    }

    // ================================================================
    // CRUCES EMPLEADORES (Chapter 4)
    // ================================================================
    public List<FichaCruce> crucesEmpleadores() {
        List<Map<String, String>> m = analisis.mapasEmpleadores();
        List<FichaCruce> c = new ArrayList<>();
        // 4.2.1 Perfil y Demanda
        c.add(armarCruce("4.2.1 Tamano x Proyeccion", "tamano_agrupado", "posibilidad_futura_contratacion", m));
        c.add(armarCruce("4.2.1 Tamano x Medio", "tamano_agrupado", "medio_convocatoria", m));
        c.add(armarCruce("4.2.1 Tipo x Nivel academico", "tipo_organizacion", "nivel_formacion_demanda", m));
        c.add(armarCruce("4.2.1 Rubro x Nivel academico", "rubro_agrupado", "nivel_formacion_demanda", m));
        // 4.2.2 Competencias (IA binaria)
        c.add(armarCruce("4.2.2 Demanda IA x Tamano", "demanda_ia", "tamano_agrupado", m));
        c.add(armarCruce("4.2.2 Demanda IA x Sector", "demanda_ia", "rubro_agrupado", m));
        // 4.2.3 Percepcion y Vinculacion (Likert x Likert)
        c.add(armarCruce("4.2.3 L8 (competencias) x L6 (desempeno)",
                "likert_competencias_laborales", "likert_formacion_desempeno", m));
        c.add(armarCruce("4.2.3 L11 (vinculos) x L17 (recurre UMSS)",
                "likert_vinculos_organizaciones", "likert_recurre_umss", m));
        c.add(armarCruce("4.2.3 L3 (consulta) x L7 (incorpora)",
                "likert_consulta_regular", "likert_considera_opinion", m));
        return c;
    }

    // ================================================================
    // Matriz resumen bivariada con correccion de Holm (4.2.4)
    // ================================================================
    public record FilaMatrizBivariada(
            String etiqueta, double chi, int gl, double pOriginal,
            double pHolm, double vCramer, String intensidad,
            boolean significativa, String metodo) {}

    public List<FilaMatrizBivariada> matrizResumenEmpleadores() {
        List<FichaCruce> cruces = crucesEmpleadores();
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
                    r.intensidadVCramer(), h.significativo(), r.metodo()));
        }
        return out;
    }

    // Wilson IC por celda, util para el reporte
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
                    var w = bo.umss.mercado.nucleo.stats.IntervalosConfianza.paraProporcion(p, (int) total);
                    ic[0] = w.limiteInferior();
                    ic[1] = w.limiteSuperior();
                }
                out.add(ic);
            }
        }
        return out;
    }
}
