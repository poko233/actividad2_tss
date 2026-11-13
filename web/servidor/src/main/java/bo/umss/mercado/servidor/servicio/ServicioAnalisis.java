package bo.umss.mercado.servidor.servicio;

import bo.umss.mercado.nucleo.likert.EscalaLikert;
import bo.umss.mercado.nucleo.limpieza.LimpiezaDatos;
import bo.umss.mercado.nucleo.stats.CronbachAlpha;
import bo.umss.mercado.nucleo.stats.Frecuencias;
import bo.umss.mercado.nucleo.stats.IntervalosConfianza;
import bo.umss.mercado.nucleo.stats.Numeros;
import bo.umss.mercado.nucleo.stats.RangoConfianza;
import bo.umss.mercado.nucleo.stats.ResumenNumerico;
import bo.umss.mercado.servidor.datos.RepositorioEmpleadores;
import bo.umss.mercado.servidor.datos.RepositorioTitulados;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ServicioAnalisis {

    private final RepositorioTitulados titulados;
    private final RepositorioEmpleadores empleadores;

    public ServicioAnalisis(RepositorioTitulados titulados,
            RepositorioEmpleadores empleadores) {
        this.titulados = titulados;
        this.empleadores = empleadores;
    }

    // ================================================================
    // Lecturas básicas
    // ================================================================
    public List<Map<String, String>> mapasTitulados() {
        List<Map<String, String>> m = new ArrayList<>();
        titulados.findAll().forEach(t -> m.add(t.comoMapa()));
        return m;
    }

    public List<Map<String, String>> mapasEmpleadores() {
        List<Map<String, String>> m = new ArrayList<>();
        empleadores.findAll().forEach(e -> m.add(e.comoMapa()));
        return m;
    }

    public List<String> columnaDe(List<Map<String, String>> mapas, String columna) {
        List<String> v = new ArrayList<>();
        for (Map<String, String> f : mapas) v.add(f.getOrDefault(columna, ""));
        return v;
    }

    public List<Double> numerosDe(List<Map<String, String>> mapas, String columna) {
        List<Double> v = new ArrayList<>();
        for (Map<String, String> f : mapas) {
            String t = f.getOrDefault(columna, "").replace(",", ".");
            if (t.isEmpty()) continue;
            try { v.add(Double.parseDouble(t)); } catch (NumberFormatException ignored) {}
        }
        return v;
    }

    public List<Double> likertDe(List<Map<String, String>> mapas, String columna) {
        List<Double> v = new ArrayList<>();
        for (Map<String, String> f : mapas) {
            double p = EscalaLikert.traducir(f.getOrDefault(columna, ""), columna);
            if (!Double.isNaN(p)) v.add(p);
        }
        return v;
    }

    // ================================================================
    // Indicadores titulados (HU1/HU2/HU3)
    // ================================================================
    public ResumenNumerico resumenNumerico(String columna) {
        return Numeros.resumir(numerosDe(mapasTitulados(), columna));
    }

    public ResumenNumerico resumenNumericoDeValores(List<Double> valores) {
        return Numeros.resumir(valores);
    }

    public Map<String, Integer> frecuenciasDe(String columna) {
        return Frecuencias.contar(columnaDe(mapasTitulados(), columna));
    }

    public double proporcionDonde(List<Map<String, String>> mapas, String columna,
            java.util.function.Predicate<String> cond) {
        int fav = 0, resp = 0;
        for (Map<String, String> f : mapas) {
            String v = f.getOrDefault(columna, "");
            if (v.isEmpty()) continue;
            resp++;
            if (cond.test(v)) fav++;
        }
        return resp == 0 ? Double.NaN : (double) fav / resp;
    }

    public double tasaInsercion() {
        return proporcionDonde(mapasTitulados(), "situacion_laboral_actual",
                v -> !LimpiezaDatos.normalizar(v).contains("NO_TRABAJA") && !v.isEmpty());
    }

    public double intencionPosgrado() {
        return proporcionDonde(mapasTitulados(), "interes_posgrado", v -> v.equals("SI"));
    }

    public double proporcionRequiereBeca() {
        return proporcionDonde(mapasTitulados(), "financiamiento_formacion",
                v -> LimpiezaDatos.normalizar(v).contains("BECA"));
    }

    public RangoConfianza rangoDe(double p, int n) {
        return IntervalosConfianza.paraProporcion(p, n);
    }

    public static String segmentoDe(double anios) {
        if (Double.isNaN(anios)) return "";
        if (anios <= 3) return "JUNIOR (0-3 años)";
        if (anios <= 7) return "INTERMEDIO (4-7 años)";
        return "CONSOLIDADO (8+ años)";
    }

    public List<FichaSegmento> fichasSegmentos() {
        Map<String, List<Map<String, String>>> grupos = new LinkedHashMap<>();
        grupos.put("JUNIOR (0-3 años)", new ArrayList<>());
        grupos.put("INTERMEDIO (4-7 años)", new ArrayList<>());
        grupos.put("CONSOLIDADO (8+ años)", new ArrayList<>());
        for (Map<String, String> f : mapasTitulados()) {
            try {
                double a = Double.parseDouble(f.getOrDefault("anios_vida_profesional", "").replace(",", "."));
                grupos.get(segmentoDe(a)).add(f);
            } catch (Exception ignored) {}
        }
        List<FichaSegmento> out = new ArrayList<>();
        for (var g : grupos.entrySet()) {
            List<Double> sat = likertDe(g.getValue(), "satisfaccion_formacion");
            double m = sat.isEmpty() ? Double.NaN : Numeros.promedio(sat);
            double inte = proporcionDonde(g.getValue(), "interes_posgrado", v -> v.equals("SI"));
            out.add(new FichaSegmento(g.getKey(), g.getValue().size(), m, inte * 100, Double.NaN));
        }
        return out;
    }

    public String fraseDiferenciaSegmentos() {
        List<Double> j = new ArrayList<>(), c = new ArrayList<>();
        for (Map<String, String> f : mapasTitulados()) {
            try {
                double a = Double.parseDouble(f.getOrDefault("anios_vida_profesional", "").replace(",", "."));
                double s = EscalaLikert.traducir(f.getOrDefault("satisfaccion_formacion", ""), "satisfaccion_formacion");
                if (Double.isNaN(s)) continue;
                if (a <= 3) j.add(s);
                else if (a >= 8) c.add(s);
            } catch (Exception ignored) {}
        }
        RangoConfianza r = IntervalosConfianza.paraDiferencia(j, c);
        if (Double.isNaN(r.limiteInferior())) return "Aún no hay datos para comparar segmentos.";
        boolean incluyeCero = r.limiteInferior() <= 0 && 0 <= r.limiteSuperior();
        return incluyeCero
                ? "Juniors y consolidados están IGUAL de satisfechos."
                : "Juniors y consolidados están DISTINTO de satisfechos.";
    }

    public List<Map.Entry<String, Integer>> topCategorias(
            Map<String, Integer> frec, int n) {
        List<Map.Entry<String, Integer>> ord = new ArrayList<>(frec.entrySet());
        ord.sort(Comparator.comparing(Map.Entry<String, Integer>::getValue).reversed());
        List<Map.Entry<String, Integer>> top = new ArrayList<>();
        for (var e : ord) {
            if (e.getKey().equals(Frecuencias.ETIQUETA_VACIO)) continue;
            top.add(e);
            if (top.size() >= n) break;
        }
        return top;
    }

    public List<Map.Entry<String, Integer>> competenciasMencionadas(int cuantas) {
        Map<String, Integer> c = new LinkedHashMap<>();
        Set<String> vac = Set.of("los","las","una","uno","con","para","por","que","del","y","de","la","el","en","the","and","conocimiento","area","areas","requiere","requieren");
        for (Map<String, String> f : mapasEmpleadores()) {
            String t = f.getOrDefault("habilidades_competencias_requeridas", "") + " "
                    + f.getOrDefault("areas_conocimiento_emergentes", "") + " "
                    + f.getOrDefault("herramientas_tecnologicas", "");
            for (String p : t.toLowerCase().replaceAll("[^a-záéíóúñ0-9+#. ]", " ").split("\\s+")) {
                p = p.trim();
                if (p.length() < 3 || vac.contains(p)) continue;
                c.put(p, c.getOrDefault(p, 0) + 1);
            }
        }
        return topCategorias(c, cuantas);
    }

    // ================================================================
    // Chapter 4 — EMPLEADORES
    // ================================================================

    // ---- 4.1.1 Caracterización (Tipo, Tamaño, Rubro) ----
    public Map<String, Integer> frecuenciasEmpleadores(String columna) {
        return Frecuencias.contar(columnaDe(mapasEmpleadores(), columna));
    }

    public Map<String, Integer> frecuenciasTipoOrg() {
        return frecuenciasEmpleadores("tipo_organizacion");
    }
    public Map<String, Integer> frecuenciasTamano() {
        return frecuenciasEmpleadores("tamano_organizacion");
    }
    public Map<String, Integer> frecuenciasTamanoAgrupado() {
        return frecuenciasEmpleadores("tamano_agrupado");
    }
    public Map<String, Integer> frecuenciasSector() {
        return frecuenciasEmpleadores("sector_rubro");
    }
    public Map<String, Integer> frecuenciasRubroAgrupado() {
        return frecuenciasEmpleadores("rubro_agrupado");
    }

    // ---- 4.1.2 Demanda y proyección ----
    public Map<String, Integer> frecuenciasContrato5a() {
        return frecuenciasEmpleadores("contrato_ultimos_5_anios");
    }
    public Map<String, Integer> frecuenciasProyeccion() {
        return frecuenciasEmpleadores("posibilidad_futura_contratacion");
    }

    // Distribución de menciones (para multi-selección con "|")
    public Map<String, Integer> distribucionMulti(String columna) {
        Map<String, Integer> c = new LinkedHashMap<>();
        for (Map<String, String> f : mapasEmpleadores()) {
            String v = f.getOrDefault(columna, "");
            if (v.isEmpty()) continue;
            for (String p : v.split("\\|")) {
                p = p.trim();
                if (!p.isEmpty()) c.put(p, c.getOrDefault(p, 0) + 1);
            }
        }
        return c;
    }

    public Map<String, Integer> distribucionCargos() {
        return distribucionMulti("cargos_desempenados");
    }

    public Map<String, Integer> distribucionMedio() {
        return distribucionMulti("medio_convocatoria");
    }

    // ---- 4.1.3 Nivel académico ----
    public Map<String, Integer> frecuenciasNivelAcademico() {
        return frecuenciasEmpleadores("nivel_formacion_demanda");
    }

    // ---- 4.1.4 Análisis de contenido (tendencias, hard, soft, IA) ----
    public Map<String, Integer> distribucionTendencias() {
        return distribucionMulti("tendencias_categorias");
    }
    public Map<String, Integer> distribucionHardSkills() {
        return distribucionMulti("hard_skills_categorias");
    }
    public Map<String, Integer> distribucionSoftSkills() {
        return distribucionMulti("soft_skills_categorias");
    }
    public Map<String, Integer> frecuenciasDemandaIA() {
        return frecuenciasEmpleadores("demanda_ia");
    }
    public double proporcionDemandaIA() {
        return proporcionDonde(mapasEmpleadores(), "demanda_ia", v -> v.equals("SI"));
    }

    // ---- 4.1.5 Likert: 18 ítems + 2 dimensiones + ítem 4 individual ----
    public static final List<String> ITEMS_LIKERT = List.of(
            "likert_confianza_formadora", "likert_consistencia_titulo",
            "likert_consulta_regular", "likert_conoce_perfil",
            "likert_coherencia_perfil", "likert_formacion_desempeno",
            "likert_considera_opinion", "likert_competencias_laborales",
            "likert_valores_actitudes", "likert_participacion_procesos",
            "likert_vinculos_organizaciones", "likert_participacion_vinculacion",
            "likert_difusion_oportunidades", "likert_vinculacion_profesionales",
            "likert_consultado_desempeno", "likert_consultado_pertinencia",
            "likert_recurre_umss", "likert_consultado_autoevaluacion");

    // Etiquetas legibles (para la vista y los CSV exportables)
    public static final Map<String, String> ETIQUETA_ITEM = Map.ofEntries(
            Map.entry("likert_confianza_formadora", "L1. Confianza como formadora"),
            Map.entry("likert_consistencia_titulo", "L2. Consistencia del título"),
            Map.entry("likert_consulta_regular", "L3. Consulta regular a empleadores"),
            Map.entry("likert_conoce_perfil", "L4. Conoce el perfil de egreso"),
            Map.entry("likert_coherencia_perfil", "L5. Coherencia del perfil"),
            Map.entry("likert_formacion_desempeno", "L6. Formación permite desempeño destacado"),
            Map.entry("likert_considera_opinion", "L7. Incorpora opinión en mejoras"),
            Map.entry("likert_competencias_laborales", "L8. Desempeño evidencia competencias"),
            Map.entry("likert_valores_actitudes", "L9. Valores y actitudes"),
            Map.entry("likert_participacion_procesos", "L10. Participación en evaluación"),
            Map.entry("likert_vinculos_organizaciones", "L11. Vínculos con organizaciones"),
            Map.entry("likert_participacion_vinculacion", "L12. Participación en vinculación"),
            Map.entry("likert_difusion_oportunidades", "L13. Difusión de oportunidades"),
            Map.entry("likert_vinculacion_profesionales", "L14. Vinculación entre profesionales"),
            Map.entry("likert_consultado_desempeno", "L15. Consultado sobre desempeño"),
            Map.entry("likert_consultado_pertinencia", "L16. Consultado sobre pertinencia"),
            Map.entry("likert_recurre_umss", "L17. Recurre a la UMSS"),
            Map.entry("likert_consultado_autoevaluacion", "L18. Consultado en autoevaluación"));

    // Dimensiones validadas empíricamente (Cap.2)
    public static final List<String> DIM_A_ITEMS = List.of(
            "likert_confianza_formadora", "likert_consistencia_titulo",
            "likert_coherencia_perfil", "likert_formacion_desempeno",
            "likert_competencias_laborales", "likert_valores_actitudes");
    public static final List<String> DIM_B_ITEMS = List.of(
            "likert_consulta_regular", "likert_considera_opinion",
            "likert_participacion_procesos", "likert_vinculos_organizaciones",
            "likert_participacion_vinculacion", "likert_difusion_oportunidades",
            "likert_vinculacion_profesionales", "likert_consultado_desempeno",
            "likert_consultado_pertinencia", "likert_recurre_umss",
            "likert_consultado_autoevaluacion");
    public static final String ITEM_INDEPENDIENTE = "likert_conoce_perfil";

    public record ResumenItem(String columna, String etiqueta, int n,
            double media, double desviacion, double pctAcuerdo) {}

    public record ResumenDimension(String nombre, List<String> items,
            double alpha, int n, String interpretacion) {}

    // Resumen de UN ítem Likert (media, desv, % de acuerdo).
    public ResumenItem resumenItem(String columna) {
        List<Double> vals = likertDe(mapasEmpleadores(), columna);
        if (vals.isEmpty()) return new ResumenItem(columna,
                ETIQUETA_ITEM.getOrDefault(columna, columna), 0,
                Double.NaN, Double.NaN, Double.NaN);
        double m = Numeros.promedio(vals);
        double sd = Numeros.resumir(vals).desv();
        int acuerdos = 0;
        for (double v : vals) if (v >= 2.5) acuerdos++;
        double pct = 100.0 * acuerdos / vals.size();
        return new ResumenItem(columna,
                ETIQUETA_ITEM.getOrDefault(columna, columna),
                vals.size(), m, sd, pct);
    }

    // Matriz de los 18 ítems.
    public List<ResumenItem> matriz18Items() {
        List<ResumenItem> out = new ArrayList<>();
        for (String c : ITEMS_LIKERT) out.add(resumenItem(c));
        return out;
    }

    // Ficha de una dimensión con su alfa + n de casos completos.
    public ResumenDimension fichaDimension(String nombre, List<String> items) {
        List<List<Double>> completas = new ArrayList<>();
        for (Map<String, String> f : mapasEmpleadores()) {
            List<Double> fila = new ArrayList<>();
            boolean ok = true;
            for (String c : items) {
                double v = EscalaLikert.traducir(f.getOrDefault(c, ""), c);
                if (Double.isNaN(v)) { ok = false; break; }
                fila.add(v);
            }
            if (ok) completas.add(fila);
        }
        double alpha = CronbachAlpha.calcular(completas, items.size());
        return new ResumenDimension(nombre, items, alpha, completas.size(),
                CronbachAlpha.descripcion(alpha));
    }

    public ResumenDimension fichaDimensionA() {
        return fichaDimension("A. Calidad e Imagen", DIM_A_ITEMS);
    }

    public ResumenDimension fichaDimensionB() {
        return fichaDimension("B. Vinculación y Retroalimentación", DIM_B_ITEMS);
    }

    public ResumenItem fichaItem4() {
        return resumenItem(ITEM_INDEPENDIENTE);
    }

    // Media de la DIMENSIÓN por encuestado (promedio de sus ítems no vacíos).
    public List<Double> mediasPorDimension(String dim) {
        List<String> items = dim.equals("A") ? DIM_A_ITEMS : DIM_B_ITEMS;
        List<Double> medias = new ArrayList<>();
        for (Map<String, String> f : mapasEmpleadores()) {
            double suma = 0; int cnt = 0;
            for (String c : items) {
                double v = EscalaLikert.traducir(f.getOrDefault(c, ""), c);
                if (!Double.isNaN(v)) { suma += v; cnt++; }
            }
            if (cnt > 0) medias.add(suma / cnt);
        }
        return medias;
    }
}
