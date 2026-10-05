import java.util.*;

// ============================================================================
// StatsUtil — Biblioteca estadística del pipeline (Java estándar, sin deps).
// ----------------------------------------------------------------------------
// CAPAS:
//   A) DESCRIPTIVA : media, mediana, desv. estándar, min/max, cuartiles,
//                    frecuencias, moda, histogramas, barras de texto.
//   B) INFERENCIAL : IC 95% para medias (t Student), para proporciones (Z),
//                    IC para DIFERENCIA de medias (varianzas agrupadas, §7.1),
//                    tablas de contingencia + prueba χ² con p-valor exacto
//                    (gamma incompleta, sin tablas externas).
//   C) LIKERT      : mapeo de etiquetas ("DE_ACUERDO"->3) a 1..4 y % acuerdo.
// RESTRICCIÓN DEL PROYECTO: aquí NO hay aleatoriedad ni generadores
// congruenciales; el único sorteo del sistema vive en el módulo de
// experimentación de Analizador (Math.random para Monte Carlo ligero).
// ============================================================================
public class StatsUtil {

    // ==================== A) DESCRIPTIVA NUMÉRICA ====================

    // media(): promedio aritmético Σx/n. NaN si no hay datos (evita /0).
    public static double media(List<Double> xs) {
        if (xs.isEmpty()) return Double.NaN;
        double s = 0; for (double x : xs) s += x;
        return s / xs.size();
    }

    // mediana(): valor central con la muestra ORDENADA (copia para no mutar).
    // Si n es par, promedia los dos centrales. Robusta a valores atípicos.
    public static double mediana(List<Double> xs) {
        if (xs.isEmpty()) return Double.NaN;
        List<Double> c = new ArrayList<>(xs);
        Collections.sort(c);
        int n = c.size();
        if (n % 2 == 1) return c.get(n / 2);
        return (c.get(n / 2 - 1) + c.get(n / 2)) / 2.0;
    }

    // desvEstandar(): dispersión MUESTRAL (denominador n-1, estimador
    // insesgado de la varianza poblacional). NaN con <2 datos.
    public static double desvEstandar(List<Double> xs) {
        int n = xs.size();
        if (n < 2) return Double.NaN;
        double m = media(xs);
        double s = 0; for (double x : xs) s += (x - m) * (x - m);
        return Math.sqrt(s / (n - 1));
    }

    // min()/max(): extremos observados (definen el rango del histograma).
    public static double min(List<Double> xs) {
        if (xs.isEmpty()) return Double.NaN;
        double m = xs.get(0); for (double x : xs) m = Math.min(m, x);
        return m;
    }

    public static double max(List<Double> xs) {
        if (xs.isEmpty()) return Double.NaN;
        double m = xs.get(0); for (double x : xs) m = Math.max(m, x);
        return m;
    }

    // cuartiles(): Q1/Q2/Q3 por interpolación lineal de percentiles
    // (mismo método que Excel PERCENTIL.INC / numpy 'linear').
    public static double[] cuartiles(List<Double> xs) {
        if (xs.isEmpty()) return new double[]{Double.NaN, Double.NaN, Double.NaN};
        List<Double> c = new ArrayList<>(xs);
        Collections.sort(c);
        return new double[]{percentil(c, 25), percentil(c, 50), percentil(c, 75)};
    }

    // percentil(): posición fraccional pos=p/100·(n-1) e interpola entre los
    // dos vecinos. Privado porque exige la lista YA ordenada.
    private static double percentil(List<Double> ordenada, double p) {
        int n = ordenada.size();
        if (n == 1) return ordenada.get(0);
        double pos = p / 100.0 * (n - 1);
        int lo = (int) Math.floor(pos), hi = (int) Math.ceil(pos);
        if (lo == hi) return ordenada.get(lo); // cayó justo en un dato
        double d = pos - lo;                   // fracción hacia el vecino sup.
        return ordenada.get(lo) * (1 - d) + ordenada.get(hi) * d;
    }

    // ==================== A) DESCRIPTIVA CATEGÓRICA ====================

    // frecuencias(): conteo por categoría preservando orden de aparición
    // (LinkedHashMap). Los vacíos se agrupan como "(VACIO)" para visibilizar
    // la no-respuesta en vez de ocultarla.
    public static Map<String, Integer> frecuencias(List<String> vals) {
        Map<String, Integer> f = new LinkedHashMap<>();
        for (String v : vals) {
            String k = (v == null || v.isEmpty()) ? "(VACIO)" : v;
            f.put(k, f.getOrDefault(k, 0) + 1);
        }
        return f;
    }

    // moda(): categoría con mayor conteo (el "típico" de variables nominales
    // como género o área de interés, donde la media no tiene sentido).
    public static String moda(Map<String, Integer> frec) {
        String best = null; int n = -1;
        for (Map.Entry<String, Integer> e : frec.entrySet())
            if (e.getValue() > n) { n = e.getValue(); best = e.getKey(); }
        return best;
    }

    // histograma(): divide [min,max] en k intervalos iguales y cuenta cuántos
    // datos caen en cada uno. Devuelve filas [limInf, limSup, conteo] listas
    // para el reporte textual y el gráfico [1] del dashboard.
    // Detalles: si min==max hay un solo intervalo; el máximo global se fuerza
    // al último intervalo (idx>=k -> k-1) para no perderlo por redondeo.
    public static List<double[]> histograma(List<Double> xs, int k) {
        List<double[]> h = new ArrayList<>();
        if (xs.isEmpty() || k <= 0) return h;
        double mn = min(xs), mx = max(xs);
        if (mn == mx) { h.add(new double[]{mn, mx, xs.size()}); return h; }
        double w = (mx - mn) / k; // ancho de cada intervalo
        int[] c = new int[k];
        for (double x : xs) {
            int idx = (int) Math.floor((x - mn) / w);
            if (idx >= k) idx = k - 1;
            if (idx < 0) idx = 0;
            c[idx]++;
        }
        for (int i = 0; i < k; i++) h.add(new double[]{mn + i * w, mn + (i + 1) * w, c[i]});
        return h;
    }

    // ==================== B) INFERENCIA ====================

    // tCritico95(): valor t de Student bilateral α=0.05 según grados de
    // libertad. Tabla exacta gl=1..30; con gl>30 la t ya ≈ normal (1.96).
    // Se usa en IC de medias con muestras pequeñas (ej. n=20 empleadores).
    public static double tCritico95(int gl) {
        double[] tabla = {0,
            12.706, 4.303, 3.182, 2.776, 2.571, 2.447, 2.365, 2.306, 2.262, 2.228,
            2.201, 2.179, 2.160, 2.145, 2.131, 2.120, 2.110, 2.101, 2.093, 2.086,
            2.080, 2.074, 2.069, 2.064, 2.060, 2.056, 2.052, 2.048, 2.045, 2.042};
        if (gl >= 1 && gl <= 30) return tabla[gl];
        return 1.96; // gl grande -> normal
    }

    // icMedia(): IC 95% para la media poblacional, fórmula de la guía §4.4:
    //   x̄ ± t(α/2, n-1) · s / √n. Con n=1 no hay dispersión: [m, m].
    public static double[] icMedia(List<Double> xs) {
        int n = xs.size();
        if (n == 0) return new double[]{Double.NaN, Double.NaN};
        double m = media(xs), s = desvEstandar(xs);
        if (n == 1 || Double.isNaN(s)) return new double[]{m, m};
        double t = tCritico95(n - 1);
        double e = t * s / Math.sqrt(n); // margen de error
        return new double[]{m - e, m + e};
    }

    // icProporcion(): IC 95% para proporciones (tasa inserción, intención
    // posgrado), fórmula §4.4: p̂ ± Z · √(p̂(1-p̂)/n), Z=1.96. Se recorta a
    // [0,1] porque una proporción fuera de rango no tiene sentido.
    public static double[] icProporcion(double p, int n) {
        if (n <= 0) return new double[]{Double.NaN, Double.NaN};
        double e = 1.96 * Math.sqrt(p * (1 - p) / n);
        return new double[]{Math.max(0, p - e), Math.min(1, p + e)};
    }

    // icDifMedias(): IC 95% para la DIFERENCIA de medias (μ1-μ2) con
    // varianzas AGRUPADAS (pooled), método exigido en §7.1 de la guía:
    //   sp² = [(n1-1)s1² + (n2-1)s2²] / (n1+n2-2)
    //   (m1-m2) ± t(α/2, n1+n2-2) · sp·√(1/n1 + 1/n2)
    // Uso: comparar satisfacción junior vs consolidado (segmentación).
    // Devuelve {diferencia, li, ls}. Si el 0 queda fuera del intervalo, la
    // diferencia entre segmentos es estadísticamente significativa.
    public static double[] icDifMedias(List<Double> a, List<Double> b) {
        int n1 = a.size(), n2 = b.size();
        if (n1 < 2 || n2 < 2) return new double[]{Double.NaN, Double.NaN, Double.NaN};
        double m1 = media(a), m2 = media(b);
        double s1 = desvEstandar(a), s2 = desvEstandar(b);
        double sp2 = ((n1 - 1) * s1 * s1 + (n2 - 1) * s2 * s2) / (n1 + n2 - 2); // varianza pooled
        double t = tCritico95(n1 + n2 - 2);
        double e = t * Math.sqrt(sp2) * Math.sqrt(1.0 / n1 + 1.0 / n2);
        double dif = m1 - m2;
        return new double[]{dif, dif - e, dif + e};
    }

    // TablaContingencia: resultado de cruzar dos variables categóricas.
    // obs[][] = conteos observados, esp[][] = esperados bajo independencia,
    // chi2 = Σ(O-E)²/E, gl = (f-1)(c-1), pValor = P(χ²_gl ≥ chi2).
    public static class TablaContingencia {
        public List<String> filas;      // categorías variable A (orden)
        public List<String> columnas;   // categorías variable B (orden)
        public int[][] obs;             // [f][c]
        public double[][] esp;
        public double chi2;
        public int gl;
        public double pValor;
    }

    // contingencia(): cruza A×B y aplica la prueba χ² de independencia.
    // Orden de categorías = orden de aparición (LinkedHashSet) para reportes
    // estables; los vacíos se etiquetan "(VACIO)". Si p<0.05 se rechaza la
    // independencia (ej. HU3: ¿el financiamiento depende del área?).
    public static TablaContingencia contingencia(List<String> a, List<String> b) {
        LinkedHashSet<String> sa = new LinkedHashSet<>(), sb = new LinkedHashSet<>();
        List<String> ac = new ArrayList<>(), bc = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            String va = (a.get(i) == null || a.get(i).isEmpty()) ? "(VACIO)" : a.get(i);
            String vb = (b.get(i) == null || b.get(i).isEmpty()) ? "(VACIO)" : b.get(i);
            ac.add(va); bc.add(vb); sa.add(va); sb.add(vb);
        }
        TablaContingencia t = new TablaContingencia();
        t.filas = new ArrayList<>(sa);
        t.columnas = new ArrayList<>(sb);
        int f = t.filas.size(), c = t.columnas.size();
        t.obs = new int[f][c];
        // Índices categoría->posición para llenar la matriz en O(n)
        Map<String, Integer> fi = new HashMap<>(), ci = new HashMap<>();
        for (int i = 0; i < f; i++) fi.put(t.filas.get(i), i);
        for (int j = 0; j < c; j++) ci.put(t.columnas.get(j), j);
        for (int i = 0; i < ac.size(); i++) t.obs[fi.get(ac.get(i))][ci.get(bc.get(i))]++;
        int n = ac.size();
        int[] totF = new int[f], totC = new int[c];
        for (int i = 0; i < f; i++) for (int j = 0; j < c; j++) { totF[i] += t.obs[i][j]; totC[j] += t.obs[i][j]; }
        // Esperados bajo independencia: E = (totalFila·totalCol)/n, luego χ²
        t.esp = new double[f][c];
        t.chi2 = 0;
        for (int i = 0; i < f; i++) for (int j = 0; j < c; j++) {
            t.esp[i][j] = n == 0 ? 0 : (double) totF[i] * totC[j] / n;
            if (t.esp[i][j] > 0) t.chi2 += Math.pow(t.obs[i][j] - t.esp[i][j], 2) / t.esp[i][j];
        }
        t.gl = Math.max(0, (f - 1) * (c - 1));
        t.pValor = pValorChi2(t.chi2, t.gl);
        return t;
    }

    // pValorChi2(): cola derecha de la χ²: p = 1 - CDF(chi2).
    // La CDF de χ²_gl es la gamma incompleta P(gl/2, chi2/2) — así no se
    // necesitan tablas externas y vale para cualquier gl.
    public static double pValorChi2(double chi2, int gl) {
        if (gl <= 0) return Double.NaN;
        if (chi2 <= 0) return 1.0;
        double p = gammaP(gl / 2.0, chi2 / 2.0);
        return Math.max(0, Math.min(1, 1 - p));
    }

    // gammaP(): P(a,x), gamma incompleta inferior REGULARIZADA (Numerical
    // Recipes). Dos ramas: serie de potencias si x < a+1, fracción continua
    // (método de Lentz/Steed) en caso contrario. CORRECCIÓN HISTÓRICA: la
    // rama de fracción continua exigía FPMIN=1e-30 con c INICIANDO GRANDE
    // (1/FPMIN); con 1e-300 el primer cociente explotaba y todo p-valor con
    // x≥a+1 salía 1.0. Verificado: χ²=3.841/gl=1 -> p=0.05 exacto.
    private static double gammaP(double a, double x) {
        if (x < 0 || a <= 0) return Double.NaN;
        if (x == 0) return 0;
        if (x < a + 1) {
            // Serie: P = e^-x·x^a/Γ(a) · Σ x^n/(a(a+1)...(a+n))
            double ap = a, sum = 1.0 / a, del = sum;
            for (int n = 1; n <= 500; n++) {
                ap++; del *= x / ap; sum += del;
                if (Math.abs(del) < Math.abs(sum) * 1e-12) break; // convergió
            }
            return sum * Math.exp(-x + a * Math.log(x) - logGamma(a));
        } else {
            // Fracción continua -> calcula Q(a,x)=1-P y se devuelve 1-Q.
            final double FPMIN = 1e-30;
            double b = x + 1 - a, c = 1 / FPMIN, d = 1 / b, h = d;
            for (int i = 1; i <= 500; i++) {
                double an = -i * (i - a);
                b += 2;
                d = an * d + b;
                if (Math.abs(d) < FPMIN) d = FPMIN; // fusibles anti-división-por-cero
                c = b + an / c;
                if (Math.abs(c) < FPMIN) c = FPMIN;
                d = 1 / d;
                double del = d * c;
                h *= del;
                if (Math.abs(del - 1) < 1e-12) break; // convergió
            }
            return 1 - Math.exp(-x + a * Math.log(x) - logGamma(a)) * h;
        }
    }

    // logGamma(): logaritmo de Γ(x) por aproximación de Lanczos (la usa
    // gammaP para normalizar). Precisión ~1e-10, suficiente para p-valores.
    private static double logGamma(double x) {
        double[] c = {76.18009172947146, -86.50532032961677, 24.01409824083091,
                -1.231739572450155, 0.001208650973866179, -0.000005395239384953};
        double y = x, tmp = x + 5.5;
        tmp -= (x + 0.5) * Math.log(tmp);
        double ser = 1.000000000190015;
        for (double v : c) { y++; ser += v / y; }
        return -tmp + Math.log(2.5066282746310005 * ser / x);
    }

    // ==================== C) ESCALAS LIKERT ====================

    // likert4(): traduce ETIQUETAS de encuesta a puntaje 1..4.
    // Orden de los if importa: lo más específico primero ("TOTALMENTE_..."
    // antes que "DESACUERDO" suelto). Cubre: acuerdo 4 niveles, importancia
    // 4 niveles (negocio), satisfacción 3 niveles (negocio). "NO_SABE" o
    // vacío -> NaN (no contamina la media).
    public static double likert4(String v) {
        String n = CsvUtil.normalizar(v);
        if (n.contains("TOTALMENTE_DESACUERDO")) return 1;
        if (n.equals("DESACUERDO") || n.contains("PARCIALMENTE_DESACUERDO")) return 2;
        if (n.equals("ACUERDO") || n.contains("PARCIALMENTE_ACUERDO") || n.contains("ALGO")) return 3;
        if (n.contains("TOTALMENTE_ACUERDO")) return 4;
        if (n.contains("NO_IMPORTANTE")) return 1;
        if (n.contains("POCO_IMPORTANTE")) return 2;
        if (n.contains("IMPORTANTE") && !n.contains("MUY")) return 3;
        if (n.contains("MUY_IMPORTANTE")) return 4;
        if (n.contains("INSATISFECHO") && !n.contains("ALGO")) return 1;
        if (n.contains("ALGO_SATISFECHO")) return 2;
        if (n.equals("SATISFECHO")) return 3;
        return Double.NaN;
    }

    // pctAcuerdo(): % de respuestas ≥ umbral (3 en escala 1-4). Es el KPI que
    // alimenta las barras del panel [9] del dashboard.
    public static double pctAcuerdo(List<Double> xs, double umbral) {
        if (xs.isEmpty()) return Double.NaN;
        int k = 0; for (double x : xs) if (x >= umbral) k++;
        return 100.0 * k / xs.size();
    }

    // barra(): mini-gráfico de texto '####....' para los reportes .txt
    // (el dashboard HTML usa canvas; esto es para consola/archivo plano).
    public static String barra(double pct, int ancho) {
        int n = (int) Math.round(pct / 100.0 * ancho);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ancho; i++) sb.append(i < n ? '#' : '.');
        return sb.toString();
    }

    // f2()/f1(): formateo con punto decimal (Locale.US) y "NA" si no hay dato.
    // Locale.US es CLAVE: en Windows con coma decimal, String.format daría
    // "3,08" y rompería los CSV y el JSON del dashboard.
    public static String f2(double x) {
        if (Double.isNaN(x)) return "NA";
        return String.format(Locale.US, "%.2f", x);
    }

    public static String f1(double x) {
        if (Double.isNaN(x)) return "NA";
        return String.format(Locale.US, "%.1f", x);
    }
}
