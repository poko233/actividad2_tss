import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Analizador de mercado de titulados Ing. Sistemas UMSS.
 * ----------------------------------------------------------------------------
 * QUÉ ES: pipeline de análisis estadístico descriptivo/exploratorio + consola
 * de reportes + dashboard HTML, según la "Guía Metodológica y Técnica Integral
 * (Enfoque ARCUSUR)". Cubre las HU1–HU5 de la guía:
 *   HU1 antigüedad/empleabilidad -> moduloUnivariado + moduloSegmentacion
 *   HU2 formación continua      -> frecuencias + cruces área×institución
 *   HU3 financiamiento          -> cruce financiamiento×nivel + χ² + IC beca
 *   HU4 brechas competencias    -> moduloBrechas (ver NOTA HU4 dentro)
 *   HU5 reportes/dashboard      -> exportarCsvs + generarDashboard
 * USO:
 *   Recolectar datos : java -cp bin Registro titulados.csv empleadores.csv
 *   Analizar         : java -cp bin Analizador titulados.csv empleadores.csv salida
 * RESTRICCIONES (fidelidad a la especificación):
 *   - Sin generadores congruenciales, sin rechazo/composición/transformada.
 *   - Única aleatoriedad: Math.random() en experimentación Monte Carlo y en
 *     el análisis de sensibilidad (§5 validación: quitar 10% y re-medir).
 *   - Fuente de información: exclusivamente los CSV de encuestas.
 */
public class Analizador {

    static Path salida;
    static CsvUtil.Tabla TIT, EMP;
    static StringBuilder dash = new StringBuilder();

    // Columnas obligatorias (fidelidad seccion 1.1 / 1.2)
    static final String[] OBLIG_TIT = {"id_titulado", "edad", "genero", "interes_posgrado",
            "anios_vida_profesional", "situacion_laboral_actual"};
    static final String[] OBLIG_EMP = {"id_empleador", "nombre_empresa", "tipo_organizacion",
            "tamano_organizacion", "sector_rubro"};

    static final String[] LIKERT_EMP = {"likert_confianza_formadora", "likert_consistencia_titulo",
            "likert_consulta_regular", "likert_conoce_perfil", "likert_coherencia_perfil",
            "likert_formacion_desempeno", "likert_considera_opinion", "likert_competencias_laborales",
            "likert_valores_actitudes", "likert_participacion_procesos"};

    // ------------------------------------------------------------------------
    // main(): orquesta el pipeline completo. Flujo:
    //   1) Carga y Data Wrangling de ambos CSV (módulo 4.1).
    //   2) Calcula los 7 módulos de análisis (4.2–4.8) en memoria.
    //   3) Escribe reportes .txt + informe ARCUSUR + exportables CSV.
    //   4) Genera dashboard.html y muestra el resumen ejecutivo en consola.
    // ------------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        // args opcionales con valores por defecto (carpeta actual / salida).
        String fTit = args.length > 0 ? args[0] : "titulados.csv";
        String fEmp = args.length > 1 ? args[1] : "empleadores.csv";
        String dOut = args.length > 2 ? args[2] : "salida";
        salida = Paths.get(dOut);
        Files.createDirectories(salida);

        System.out.println("=== ANALISIS DE MERCADO TITULADOS ING. SISTEMAS UMSS ===");
        TIT = cargar(fTit, OBLIG_TIT, "titulados");
        EMP = cargar(fEmp, OBLIG_EMP, "empleadores");
        limpiar(TIT);
        limpiar(EMP);

        // Cada módulo devuelve su reporte como String (luego se escribe a disco).
        String univ = moduloUnivariado();
        String biv = moduloBivariado();
        String ics = moduloIntervalos();
        String bre = moduloBrechas();
        String seg = moduloSegmentacion();
        String exp = moduloExperimentacion();
        String emp = moduloEmpleadores();
        String inf = moduloInforme(); // documento final estructura ARCUSUR §2

        escribir("reporte_univariado.txt", univ);
        escribir("reporte_bivariado.txt", biv);
        escribir("reporte_intervalos.txt", ics);
        escribir("reporte_brechas.txt", bre);
        escribir("reporte_segmentacion.txt", seg);
        escribir("reporte_experimentacion.txt", exp);
        escribir("reporte_empleadores.txt", emp);
        escribir("informe_mercado.txt", inf);

        exportarCsvs();
        generarDashboard();

        dashboardConsola();
        System.out.println("\nArchivos generados en: " + salida.toAbsolutePath());
        System.out.println("Abra dashboard.html en su navegador.");
    }

    // ------------------------------------------------------------------------
    // CARGA Y LIMPIEZA (módulo 4.1 + fase Data Wrangling del marco §2).
    // cargar(): lee el CSV o sigue con tabla vacía si falta el archivo
    //   (así el análisis de titulados no se bloquea sin empleadores).
    //   Valida columnas obligatorias y AVISA las faltantes (no falla).
    // limpiar(): normaliza cada celda — trim, nulos ("null","n/a","-") -> "",
    //   numéricos inválidos -> "" (para no romper parseos), SI/NO unificado.
    //   Sin esta fase, "Si" y "SI" serían categorías distintas en los cruces.
    // ------------------------------------------------------------------------
    static CsvUtil.Tabla cargar(String f, String[] oblig, String nombre) throws IOException {
        Path p = Paths.get(f);
        if (!Files.exists(p)) {
            System.out.println("[AVISO] No existe " + f + " (" + nombre + "). Se continua con 0 registros.");
            CsvUtil.Tabla t = new CsvUtil.Tabla();
            t.headers.addAll(Arrays.asList(oblig));
            return t;
        }
        CsvUtil.Tabla t = CsvUtil.leer(p);
        List<String> faltan = new ArrayList<>();
        for (String o : oblig) if (!t.headers.contains(o)) faltan.add(o);
        if (!faltan.isEmpty())
            System.out.println("[AVISO] " + f + " columnas faltantes: " + faltan + " (se asumen vacias)");
        System.out.println("[OK] " + f + ": " + t.filas.size() + " registros, " + t.headers.size() + " columnas.");
        return t;
    }

    /** Data wrangling: trim, "" para nulos, normaliza SI/NO, numéricos inválidos->"". */
    static void limpiar(CsvUtil.Tabla t) {
        Set<String> numericas = new HashSet<>(Arrays.asList("edad", "anios_vida_profesional", "anios_desempleado"));
        for (Map<String, String> m : t.filas) {
            for (String h : t.headers) {
                String v = m.getOrDefault(h, "");
                if (v == null) v = "";
                v = v.trim();
                if (v.equalsIgnoreCase("null") || v.equalsIgnoreCase("n/a") || v.equals("-") || v.equalsIgnoreCase("na"))
                    v = "";
                if (numericas.contains(h) && !v.isEmpty()) {
                    try { Double.parseDouble(v.replace(",", ".")); }
                    catch (NumberFormatException e) { v = ""; }
                }
                if ((h.equals("interes_posgrado") || h.equals("realizo_formacion_complementaria")
                        || h.equals("red_contactos") || h.equals("primer_empleo_actual")
                        || h.equals("contrato_ultimos_5_anios")) && !v.isEmpty()) {
                    String n = CsvUtil.normalizar(v);
                    if (n.startsWith("SI") || n.startsWith("SÍ") || n.equals("YES") || n.equals("1")) v = "SI";
                    else if (n.startsWith("NO") || n.equals("0")) v = "NO";
                }
                m.put(h, v);
            }
        }
    }

    // Helpers de acceso tipado a columnas: col() crudo, num() filtra no
    // numéricos (coma decimal incluida), likertCol() traduce etiquetas a
    // 1..4 y descarta NO_SABE/vacíos (NaN) para no sesgar medias.
    static List<String> col(CsvUtil.Tabla t, String c) {
        List<String> out = new ArrayList<>();
        for (Map<String, String> m : t.filas) out.add(m.getOrDefault(c, ""));
        return out;
    }

    static List<Double> num(CsvUtil.Tabla t, String c) {
        List<Double> out = new ArrayList<>();
        for (Map<String, String> m : t.filas) {
            String v = m.getOrDefault(c, "").replace(",", ".");
            if (v.isEmpty()) continue;
            try { out.add(Double.parseDouble(v)); } catch (NumberFormatException ignored) {}
        }
        return out;
    }

    static List<Double> likertCol(CsvUtil.Tabla t, String c) {
        List<Double> out = new ArrayList<>();
        for (Map<String, String> m : t.filas) {
            double v = StatsUtil.likert4(m.getOrDefault(c, ""));
            if (!Double.isNaN(v)) out.add(v);
        }
        return out;
    }

    static void escribir(String nombre, String contenido) throws IOException {
        Files.writeString(salida.resolve(nombre), contenido, StandardCharsets.UTF_8);
        System.out.println("  -> " + nombre);
    }

    static String encabezado(String titulo) {
        return "=".repeat(70) + "\n" + titulo
                + "\nFecha: " + LocalDate.now() + " | N_titulados=" + TIT.filas.size()
                + " N_empleadores=" + EMP.filas.size() + "\n" + "=".repeat(70) + "\n";
    }

    // ------------------------------------------------------------------------
    // moduloUnivariado (§4.2 + HU1/HU2): una variable a la vez.
    //   Numéricas (edad, antigüedad, desempleo): media/mediana/desv/min/
    //     cuartiles/max + histograma textual de 10 intervalos.
    //   Likert: media/mediana/desv + IC95 (t) — evidencia para ARCUSUR.
    //   Categóricas: frecuencia absoluta+relativa, moda y barra textual.
    //   Múltiples (separador |): conteo por opción sobre quienes responden.
    // ------------------------------------------------------------------------
    static String moduloUnivariado() {
        StringBuilder sb = new StringBuilder(encabezado("REPORTE UNIVARIADO"));
        sb.append("\n--- VARIABLES NUMERICAS (titulados) ---\n");
        for (String c : new String[]{"edad", "anios_vida_profesional", "anios_desempleado"}) {
            List<Double> xs = num(TIT, c);
            sb.append("\n[").append(c).append("] n=").append(xs.size()).append("\n");
            if (xs.isEmpty()) { sb.append("  Sin datos.\n"); continue; }
            double[] q = StatsUtil.cuartiles(xs);
            sb.append(String.format(Locale.US,
                    "  media=%s mediana=%s desv=%s min=%s Q1=%s Q2=%s Q3=%s max=%s\n",
                    StatsUtil.f2(StatsUtil.media(xs)), StatsUtil.f2(StatsUtil.mediana(xs)),
                    StatsUtil.f2(StatsUtil.desvEstandar(xs)), StatsUtil.f2(StatsUtil.min(xs)),
                    StatsUtil.f2(q[0]), StatsUtil.f2(q[1]), StatsUtil.f2(q[2]), StatsUtil.f2(StatsUtil.max(xs))));
            List<double[]> h = StatsUtil.histograma(xs, 10);
            sb.append("  Histograma (10 intervalos):\n");
            int mx = 1; for (double[] b : h) mx = Math.max(mx, (int) b[2]);
            for (double[] b : h) {
                int nb = (int) Math.round(b[2] / (double) mx * 40);
                sb.append(String.format(Locale.US, "  [%6.1f - %6.1f) %4d %s\n", b[0], b[1], (int) b[2], "#".repeat(nb)));
            }
        }
        sb.append("\n--- VARIABLES LIKERT (titulados, escala 1-4; negocio 1-3/1-4) ---\n");
        for (String c : new String[]{"opinion_posgrado_fcyt", "satisfaccion_formacion",
                "concordancia_formacion_requerimientos", "satisfaccion_negocio", "importancia_formacion_negocio"}) {
            List<Double> xs = likertCol(TIT, c);
            if (xs.isEmpty()) { sb.append("\n[").append(c).append("] Sin datos.\n"); continue; }
            double[] ic = StatsUtil.icMedia(xs);
            sb.append(String.format(Locale.US, "\n[%s] n=%d media=%s mediana=%s desv=%s IC95%%=[%s,%s]\n",
                    c, xs.size(), StatsUtil.f2(StatsUtil.media(xs)), StatsUtil.f2(StatsUtil.mediana(xs)),
                    StatsUtil.f2(StatsUtil.desvEstandar(xs)), StatsUtil.f2(ic[0]), StatsUtil.f2(ic[1])));
        }
        sb.append("\n--- VARIABLES CATEGORICAS (titulados) ---\n");
        for (String c : new String[]{"genero", "realizo_formacion_complementaria", "nivel_posgrado",
                "institucion_formacion", "financiamiento_formacion", "interes_posgrado",
                "area_interes_posgrado", "institucion_futura_posgrado", "situacion_laboral_actual",
                "tiempo_primer_empleo", "num_empleos_desde_titulacion", "red_contactos"}) {
            if (!TIT.headers.contains(c)) continue;
            List<String> v = col(TIT, c);
            Map<String, Integer> f = StatsUtil.frecuencias(v);
            int n = v.size();
            sb.append("\n[").append(c).append("] moda=").append(StatsUtil.moda(f)).append("\n");
            List<Map.Entry<String, Integer>> e = new ArrayList<>(f.entrySet());
            e.sort((a, b) -> b.getValue() - a.getValue());
            for (Map.Entry<String, Integer> en : e) {
                double p = n == 0 ? 0 : 100.0 * en.getValue() / n;
                sb.append(String.format(Locale.US, "  %-28s %4d %5.1f%% %s\n",
                        en.getKey().isEmpty() ? "(VACIO)" : en.getKey(), en.getValue(), p,
                        StatsUtil.barra(p, 30)));
            }
        }
        // Multi-respuesta: aspectos y asignaturas (separador |)
        sb.append("\n--- PREGUNTAS MULTIPLES (conteo por opcion, separador |) ---\n");
        for (String c : new String[]{"aspectos_utiles", "aspectos_mejorar", "asignaturas_ventajosas",
                "asignaturas_inutiles", "medio_convocatoria", "cargos_desempenados"}) {
            if (!TIT.headers.contains(c) && !EMP.headers.contains(c)) continue;
            List<String> base = TIT.headers.contains(c) ? col(TIT, c) : col(EMP, c);
            Map<String, Integer> f = new LinkedHashMap<>();
            int nResp = 0;
            for (String v : base) {
                if (v == null || v.isEmpty()) continue;
                nResp++;
                for (String op : v.split("\\|")) {
                    op = op.trim(); if (op.isEmpty()) continue;
                    f.put(op, f.getOrDefault(op, 0) + 1);
                }
            }
            sb.append("\n[").append(c).append("] respondieron=").append(nResp).append("\n");
            List<Map.Entry<String, Integer>> e = new ArrayList<>(f.entrySet());
            e.sort((a, b) -> b.getValue() - a.getValue());
            for (Map.Entry<String, Integer> en : e)
                sb.append(String.format(Locale.US, "  %-30s %4d (%5.1f%% de quienes responden)\n",
                        en.getKey(), en.getValue(), nResp == 0 ? 0 : 100.0 * en.getValue() / nResp));
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------------
    // moduloBivariado (§4.3 + HU1/HU2/HU3): cruza pares de variables clave,
    // imprime la matriz observada con totales y aplica χ² de independencia.
    // Incluye el cruce HU3 "financiamiento × nivel de posgrado" exigido por
    // la guía (¿la fuente de pago depende del nivel elegido?).
    // Chequeo de validación §5: el total de la tabla debe cuadrar con n.
    // ------------------------------------------------------------------------
    static String moduloBivariado() {
        StringBuilder sb = new StringBuilder(encabezado("REPORTE BIVARIADO (contingencia + chi-cuadrada)"));
        // Cada fila: {columna A, columna B, etiqueta}. "segmento_antiguedad"
        // es una columna DERIVADA (ver columnaExtendida), no viene del CSV.
        String[][] pares = {
                {"area_interes_posgrado", "financiamiento_formacion", "HU2: area x financiamiento"},
                {"nivel_posgrado", "institucion_formacion", "HU2/HU3: nivel x institucion"},
                {"financiamiento_formacion", "nivel_posgrado", "HU3: financiamiento x nivel posgrado"},
                {"situacion_laboral_actual", "satisfaccion_formacion", "HU1: situacion x satisfaccion"},
                {"segmento_antiguedad", "interes_posgrado", "HU1: antiguedad x intencion posgrado"},
        };
        for (String[] p : pares) {
            List<String> a = columnaExtendida(p[0]), b = columnaExtendida(p[1]);
            // filtrar filas donde ambas vacías
            List<String> fa = new ArrayList<>(), fb = new ArrayList<>();
            for (int i = 0; i < a.size(); i++) {
                if (a.get(i).isEmpty() && b.get(i).isEmpty()) continue;
                fa.add(a.get(i)); fb.add(b.get(i));
            }
            sb.append("\n### ").append(p[2]).append(" [").append(p[0]).append(" x ").append(p[1]).append("] n=").append(fa.size()).append("\n");
            if (fa.isEmpty()) { sb.append("Sin datos.\n"); continue; }
            StatsUtil.TablaContingencia t = StatsUtil.contingencia(fa, fb);
            sb.append(String.format("%-24s", ""));
            for (String c : t.columnas) sb.append(String.format("%-12s", acortar(c, 11)));
            sb.append("TOTAL\n");
            int[] totC = new int[t.columnas.size()]; int tot = 0;
            for (int i = 0; i < t.filas.size(); i++) {
                sb.append(String.format("%-24s", acortar(t.filas.get(i), 23)));
                int s = 0;
                for (int j = 0; j < t.columnas.size(); j++) {
                    sb.append(String.format("%-12d", t.obs[i][j])); s += t.obs[i][j]; totC[j] += t.obs[i][j];
                }
                tot += s; sb.append(s).append("\n");
            }
            sb.append(String.format("%-24s", "TOTAL"));
            for (int c : totC) sb.append(String.format("%-12d", c));
            sb.append(tot).append("\n");
            sb.append(String.format(Locale.US, "chi2=%.3f gl=%d p=%.4f => %s\n",
                    t.chi2, t.gl, t.pValor,
                    Double.isNaN(t.pValor) ? "no calculable" : (t.pValor < 0.05 ? "DEPENDENCIA significativa (rechaza independencia)" : "INDEPENDIENTES (no hay dependencia significativa)")));
            // consistencia interna
            sb.append("Consistencia: total tabla=" + tot + (tot == fa.size() ? " OK" : " DESCUADRE") + "\n");
        }
        return sb.toString();
    }

    // segmentoDe(): regla de negocio HU1 — madurez profesional por
    // antigüedad: Junior 0-3 (demanda especialización inicial),
    // Intermedio 4-7, Consolidado 8+ (mandos). Punto de corte de la guía.
    static String segmentoDe(double anios) {
        if (Double.isNaN(anios)) return "";
        if (anios <= 3) return "JUNIOR_0_3";
        if (anios <= 7) return "INTERMEDIO_4_7";
        return "CONSOLIDADO_8_MAS";
    }

    // columnaExtendida(): resuelve una columna por nombre, incluyendo la
    // DERIVADA "segmento_antiguedad" (no existe en el CSV: se calcula de
    // anios_vida_profesional vía segmentoDe). También busca en empleadores
    // para cruces mixtos. Vacío "" = no aplicable a ese registro.
    static List<String> columnaExtendida(String c) {
        if (c.equals("segmento_antiguedad")) {
            List<String> out = new ArrayList<>();
            for (Map<String, String> m : TIT.filas) {
                try { out.add(segmentoDe(Double.parseDouble(m.getOrDefault("anios_vida_profesional", "").replace(",", ".")))); }
                catch (Exception e) { out.add(""); }
            }
            return out;
        }
        if (TIT.headers.contains(c)) return col(TIT, c);
        if (EMP.headers.contains(c)) return col(EMP, c);
        return new ArrayList<>();
    }

    static String acortar(String s, int n) {
        if (s == null) return "";
        return s.length() <= n ? s : s.substring(0, n - 1) + "…";
    }

    // ------------------------------------------------------------------------
    // moduloIntervalos (§4.4): cuantifica la INCERTIDUMBRE de los indicadores.
    //   Medias (t Student, muestras chicas) y proporciones (Z: inserción,
    //   intención posgrado, requiere beca). Intervalo estrecho = estimación
    //   precisa; si el IC de "requiere beca" supera el 50%, urge convenio banca.
    // ------------------------------------------------------------------------
    static String moduloIntervalos() {
        StringBuilder sb = new StringBuilder(encabezado("REPORTE INTERVALOS DE CONFIANZA 95%"));
        sb.append("\n--- IC para MEDIAS (t Student: x̄ ± t·s/√n) ---\n");
        for (String c : new String[]{"edad", "anios_vida_profesional", "anios_desempleado"}) {
            List<Double> xs = num(TIT, c);
            if (xs.isEmpty()) { sb.append(c).append(": sin datos\n"); continue; }
            double[] ic = StatsUtil.icMedia(xs);
            sb.append(String.format(Locale.US, "%-24s n=%d media=%s IC95=[%s , %s]\n",
                    c, xs.size(), StatsUtil.f2(StatsUtil.media(xs)), StatsUtil.f2(ic[0]), StatsUtil.f2(ic[1])));
        }
        for (String c : new String[]{"opinion_posgrado_fcyt", "satisfaccion_formacion", "concordancia_formacion_requerimientos"}) {
            List<Double> xs = likertCol(TIT, c);
            if (xs.isEmpty()) { sb.append(c).append(": sin datos\n"); continue; }
            double[] ic = StatsUtil.icMedia(xs);
            sb.append(String.format(Locale.US, "%-24s n=%d media=%s IC95=[%s , %s]\n",
                    c, xs.size(), StatsUtil.f2(StatsUtil.media(xs)), StatsUtil.f2(ic[0]), StatsUtil.f2(ic[1])));
        }
        sb.append("\n--- IC para PROPORCIONES (Z: p̂ ± 1.96·√(p̂(1-p̂)/n)) ---\n");
        int n = TIT.filas.size();
        double pIns = prop(TIT, "situacion_laboral_actual", v -> !CsvUtil.normalizar(v).contains("NO_TRABAJA") && !v.isEmpty());
        double pPos = prop(TIT, "interes_posgrado", v -> v.equals("SI"));
        double pBeca = prop(TIT, "financiamiento_formacion", v -> CsvUtil.normalizar(v).contains("BECA"));
        double[][] ps = {{pIns, n}, {pPos, n}, {pBeca, n}};
        String[] nombres = {"tasa_insercion (trabaja o emprende)", "intencion_posgrado (SI)", "requiere_beca (BECA_TOTAL/PARCIAL)"};
        for (int i = 0; i < ps.length; i++) {
            double[] ic = StatsUtil.icProporcion(ps[i][0], (int) ps[i][1]);
            sb.append(String.format(Locale.US, "%-42s p̂=%5.1f%% IC95=[%5.1f%% , %5.1f%%] n=%d\n",
                    nombres[i], ps[i][0] * 100, ic[0] * 100, ic[1] * 100, (int) ps[i][1]));
        }
        sb.append("\nNota HU1: el tiempo_primer_empleo es categorico; su IC se reporta como proporcion por categoria en univariado.\n");
        return sb.toString();
    }

    interface Pred { boolean test(String v); }

    static double prop(CsvUtil.Tabla t, String c, Pred p) {
        if (t.filas.isEmpty() || !t.headers.contains(c)) return Double.NaN;
        int k = 0, d = 0;
        for (Map<String, String> m : t.filas) {
            String v = m.getOrDefault(c, "");
            if (v.isEmpty()) continue;
            d++; if (p.test(v)) k++;
        }
        return d == 0 ? Double.NaN : (double) k / d;
    }

    // ------------------------------------------------------------------------
    // moduloBrechas (§4.5 + HU4): ranking de brechas de competencias.
    // NOTA HU4 HONESTA: la encuesta oficial de titulados NO trae Likert 1-5 de
    // hard/soft (solo satisfacción global), así que por defecto se calcula:
    //   (a) brecha institucional empleadores = techo(4) - media de cada ítem;
    //   (b) brecha empleador-titulado (desempeño vs satisfacción);
    //   (c) minería de texto libre (competencias más mencionadas).
    //   (d) SI el CSV trae columnas extra "importancia_X" + "suficiencia_X"
    //       (extensión HU4: Likert 1-5 por competencia), se calcula la brecha
    //       real importancia-suficiencia con ranking top-5 + IC. Detección
    //       automática: no rompe compatibilidad con la encuesta oficial.
    // ------------------------------------------------------------------------
    static String moduloBrechas() {
        StringBuilder sb = new StringBuilder(encabezado("REPORTE BRECHAS DE COMPETENCIAS"));
        sb.append("NOTA: la encuesta de titulados NO incluye Likert hard/soft.\n");
        sb.append("Se calcula: (a) brecha institucional empleadores = techo(4) - media item;\n");
        sb.append("(b) brecha empleador-titulado = likert_desempeno(empleador) vs satisfaccion_formacion(titulado);\n");
        sb.append("(c) ranking de competencias demandadas (texto libre empleadores).\n\n");
        // (a)
        List<double[]> rank = new ArrayList<>();
        List<String> nombres = new ArrayList<>();
        for (String c : LIKERT_EMP) {
            if (!EMP.headers.contains(c)) continue;
            List<Double> xs = likertCol(EMP, c);
            if (xs.isEmpty()) continue;
            double media = StatsUtil.media(xs);
            rank.add(new double[]{media, 4.0 - media, xs.size()});
            nombres.add(c);
        }
        Integer[] idx = new Integer[rank.size()];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        Arrays.sort(idx, (a, b) -> Double.compare(rank.get(b)[1], rank.get(a)[1]));
        sb.append("--- (a) Ranking brechas institucionales (techo 4 - media) ---\n");
        int pos = 1;
        for (int i : idx) {
            double[] r = rank.get(i);
            double[] ic = StatsUtil.icMedia(likertCol(EMP, nombres.get(i)));
            sb.append(String.format(Locale.US, "%d. %-28s media=%s brecha=%s n=%d IC95=[%s,%s] %s\n",
                    pos++, nombres.get(i), StatsUtil.f2(r[0]), StatsUtil.f2(r[1]), (int) r[2],
                    StatsUtil.f2(ic[0]), StatsUtil.f2(ic[1]), pos <= 6 ? "<< CRITICA" : ""));
        }
        // (b)
        sb.append("\n--- (b) Brecha empleador-titulado ---\n");
        List<Double> des = EMP.headers.contains("likert_formacion_desempeno") ? likertCol(EMP, "likert_formacion_desempeno") : new ArrayList<>();
        List<Double> sat = TIT.headers.contains("satisfaccion_formacion") ? likertCol(TIT, "satisfaccion_formacion") : new ArrayList<>();
        if (!des.isEmpty() && !sat.isEmpty()) {
            double brecha = StatsUtil.media(des) - StatsUtil.media(sat);
            sb.append(String.format(Locale.US,
                    "Media empleador (formacion->desempeno)=%s vs media titulado (satisfaccion)=%s => brecha=%s\n",
                    StatsUtil.f2(StatsUtil.media(des)), StatsUtil.f2(StatsUtil.media(sat)), StatsUtil.f2(brecha)));
            sb.append(brecha > 0 ? "Empleadores valoran mejor el desempeno de lo que el titulado valora su formacion.\n"
                    : "Titulados mas satisfechos que la valoracion del empleador.\n");
        } else sb.append("Sin datos suficientes.\n");
        // (c) tokens de habilidades
        sb.append("\n--- (c) Competencias mas demandadas (texto libre empleadores) ---\n");
        Map<String, Integer> tok = new LinkedHashMap<>();
        for (Map<String, String> m : EMP.filas) {
            String v = m.getOrDefault("habilidades_competencias_requeridas", "")
                    + " " + m.getOrDefault("areas_conocimiento_emergentes", "")
                    + " " + m.getOrDefault("herramientas_tecnologicas", "");
            for (String w : v.toLowerCase().replaceAll("[^a-záéíóúñ0-9+#. ]", " ").split("\\s+")) {
                w = w.trim();
                if (w.length() < 3 || STOP.contains(w)) continue;
                tok.put(w, tok.getOrDefault(w, 0) + 1);
            }
        }
        List<Map.Entry<String, Integer>> e = new ArrayList<>(tok.entrySet());
        e.sort((a, b) -> b.getValue() - a.getValue());
        for (int i = 0; i < Math.min(15, e.size()); i++)
            sb.append(String.format("  %-18s %d menciones\n", e.get(i).getKey(), e.get(i).getValue()));
        if (e.isEmpty()) sb.append("  (sin texto libre para analizar)\n");
        // (d) Extensión HU4: pares importancia_X / suficiencia_X (Likert 1-5).
        sb.append("\n--- (d) Brechas hard/soft HU4 (solo si el CSV trae importancia_X/suficiencia_X) ---\n");
        boolean hubo = false;
        for (String h : TIT.headers) {
            if (!h.startsWith("importancia_")) continue;
            String comp = h.substring("importancia_".length());
            String hs = "suficiencia_" + comp;
            if (!TIT.headers.contains(hs)) continue;
            hubo = true;
            List<Double> brechas = new ArrayList<>();
            for (Map<String, String> m : TIT.filas) {
                try {
                    double im = Double.parseDouble(m.getOrDefault(h, "").replace(",", "."));
                    double su = Double.parseDouble(m.getOrDefault(hs, "").replace(",", "."));
                    brechas.add(im - su); // brecha = importancia - suficiencia
                } catch (Exception ignored) {}
            }
            if (brechas.isEmpty()) { sb.append("  " + comp + ": sin datos\n"); continue; }
            double[] ic = StatsUtil.icMedia(brechas);
            sb.append(String.format(Locale.US, "  %-24s brecha media=%s IC95=[%s,%s] n=%d%s\n",
                    comp, StatsUtil.f2(StatsUtil.media(brechas)), StatsUtil.f2(ic[0]), StatsUtil.f2(ic[1]),
                    brechas.size(), StatsUtil.media(brechas) >= 1.5 ? " << CRITICA" : ""));
        }
        if (!hubo) sb.append("  (no detectadas; la encuesta oficial no las incluye — ver NOTA HU4 arriba)\n");
        return sb.toString();
    }

    static final Set<String> STOP = new HashSet<>(Arrays.asList(
            "los", "las", "una", "uno", "con", "para", "por", "que", "del", "ela", "y", "de", "la",
            "el", "en", "the", "and", "conocimiento", "area", "areas", "requiere", "requieren"));

    // ------------------------------------------------------------------------
    // moduloSegmentacion (§4.6 + HU1): parte la muestra por madurez profesional
    // (Junior 0-3 / Intermedio 4-7 / Consolidado 8+ años) y compara por grupo:
    // satisfacción media, % intención de posgrado y tasa de desempleo
    // (anios_desempleado / anios_vida_profesional). Cierra con el IC 95% para
    // la DIFERENCIA de satisfacción Junior vs Consolidado (§7.1, varianzas
    // agrupadas): si el 0 queda fuera, la brecha entre segmentos es real.
    // ------------------------------------------------------------------------
    static String moduloSegmentacion() {
        StringBuilder sb = new StringBuilder(encabezado("REPORTE SEGMENTACION (antiguedad)"));
        Map<String, List<Map<String, String>>> seg = new LinkedHashMap<>();
        seg.put("JUNIOR_0_3", new ArrayList<>()); seg.put("INTERMEDIO_4_7", new ArrayList<>()); seg.put("CONSOLIDADO_8_MAS", new ArrayList<>());
        for (Map<String, String> m : TIT.filas) {
            try {
                double a = Double.parseDouble(m.getOrDefault("anios_vida_profesional", "").replace(",", "."));
                seg.get(segmentoDe(a)).add(m);
            } catch (Exception ignored) {}
        }
        sb.append(String.format("%-18s %6s %12s %12s %14s\n", "SEGMENTO", "n", "sat_formac", "int_posg_%", "tasa_desemp."));
        for (Map.Entry<String, List<Map<String, String>>> en : seg.entrySet()) {
            List<Map<String, String>> g = en.getValue();
            List<Double> sat = new ArrayList<>(), des = new ArrayList<>();
            int kPos = 0, dPos = 0;
            for (Map<String, String> m : g) {
                double s = StatsUtil.likert4(m.getOrDefault("satisfaccion_formacion", ""));
                if (!Double.isNaN(s)) sat.add(s);
                try {
                    double a = Double.parseDouble(m.getOrDefault("anios_vida_profesional", "0").replace(",", "."));
                    double d = Double.parseDouble(m.getOrDefault("anios_desempleado", "0").replace(",", "."));
                    if (a > 0) des.add(d / a);
                } catch (Exception ignored) {}
                String ip = m.getOrDefault("interes_posgrado", "");
                if (!ip.isEmpty()) { dPos++; if (ip.equals("SI")) kPos++; }
            }
            sb.append(String.format(Locale.US, "%-18s %6d %12s %12s %14s\n", en.getKey(), g.size(),
                    sat.isEmpty() ? "NA" : StatsUtil.f2(StatsUtil.media(sat)),
                    dPos == 0 ? "NA" : StatsUtil.f1(100.0 * kPos / dPos),
                    des.isEmpty() ? "NA" : StatsUtil.f2(StatsUtil.media(des))));
        }
        // IC diferencia de medias Junior vs Consolidado (guía §7.1).
        List<Double> satJ = satDeSegmento(seg.get("JUNIOR_0_3")), satC = satDeSegmento(seg.get("CONSOLIDADO_8_MAS"));
        double[] icd = StatsUtil.icDifMedias(satJ, satC);
        sb.append(String.format(Locale.US,
                "\nIC95 diferencia satisfaccion (Junior-Consolidado): dif=%s IC=[%s , %s] n1=%d n2=%d => %s\n",
                StatsUtil.f2(icd[0]), StatsUtil.f2(icd[1]), StatsUtil.f2(icd[2]), satJ.size(), satC.size(),
                (Double.isNaN(icd[1]) ? "no calculable"
                        : (icd[1] <= 0 && 0 <= icd[2] ? "sin diferencia significativa (el 0 esta dentro)"
                        : "DIFERENCIA significativa entre segmentos"))));
        return sb.toString();
    }

    // satDeSegmento(): extrae los puntajes Likert de satisfacción de un grupo
    // ya segmentado (helper del IC de diferencia de medias).
    static List<Double> satDeSegmento(List<Map<String, String>> g) {
        List<Double> out = new ArrayList<>();
        if (g == null) return out;
        for (Map<String, String> m : g) {
            double s = StatsUtil.likert4(m.getOrDefault("satisfaccion_formacion", ""));
            if (!Double.isNaN(s)) out.add(s);
        }
        return out;
    }

    // ------------------------------------------------------------------------
    // moduloExperimentacion (§4.7 + §6 + §7.2): proyección de demanda Monte
    // Carlo LIGERO. Idea: NO se inventan datos; se parte de la distribución
    // empírica (p = afines al área / N) y se simula "¿cuántos vendrían si se
    // abre el programa?" con 1000 repeticiones de sorteo Math.random().
    // Modelo de sorteo por titulado: afín (interés SI + área) -> p=0.8
    // (entusiasmo), resto -> p·0.2 (arrastre). Reporta media/desv/IC95 de la
    // demanda + n de corrida requerido n=Z²p(1-p)/E² (§7.2) + sensibilidad.
    // ------------------------------------------------------------------------
    // Escenario: resultado de simular UN programa (ej. "maestría en IA").
    // probBase = proporción empírica afín; media/desv/li/ls = demanda
    // proyectada y su IC95; muestra = las 1000 demandas (para el gráfico [7]).
    static class Escenario {
        String nombre, area; double probBase; double media, desv; double li, ls; List<Integer> muestra;
    }

    static List<Escenario> escenarios = new ArrayList<>();
    static List<Integer> ultimaMuestraHist;

    static String moduloExperimentacion() {
        StringBuilder sb = new StringBuilder(encabezado("REPORTE EXPERIMENTACION (Monte Carlo ligero, Math.random)"));
        sb.append("Metodo: distribucion empirica observada. Sorteo con Math.random(), 1000 repeticiones.\n");
        sb.append("n_corrida = Z^2·p̂(1-p̂)/E^2 con Z=1.96, E=0.05.\n\n");
        // p base: interesados por area / total con interes SI
        Map<String, Integer> fArea = StatsUtil.frecuencias(col(TIT, "area_interes_posgrado"));
        int nInt = 0;
        for (Map<String, String> m : TIT.filas) if (m.getOrDefault("interes_posgrado", "").equals("SI")) nInt++;
        int N = Math.max(TIT.filas.size(), 1);
        String[][] esc = {
                {"Abrir maestria en IA", "IA"},
                {"Abrir especialidad en ciberseguridad", "REDES_SEGURIDAD"},
                {"Abrir maestria en Ing. Software", "ING_SOFTWARE"},
                {"Abrir maestria en Base de Datos", "BASE_DATOS"},
                {"Abrir maestria en Robotica", "ROBOTICA"},
        };
        for (String[] e : esc) {
            int k = 0;
            for (Map<String, String> m : TIT.filas)
                if (m.getOrDefault("interes_posgrado", "").equals("SI")
                        && CsvUtil.normalizar(m.getOrDefault("area_interes_posgrado", "")).contains(CsvUtil.normalizar(e[1]))) k++;
            double p = N == 0 ? 0 : (double) k / N;
            double nReq = (1.96 * 1.96 * Math.max(p * (1 - p), 0.25 * 0.25)) / (0.05 * 0.05);
            // Monte Carlo: 1000 repeticiones, cada una sortea N titulados con prob p
            int REP = 1000;
            List<Double> demandas = new ArrayList<>();
            List<Integer> primera = null;
            for (int r = 0; r < REP; r++) {
                int d = 0;
                // sorteo por titulado: conserva heterogeneidad — cada titulado con interes SI y area compatible aporta prob 0.8, resto prob base*0.2
                for (Map<String, String> m : TIT.filas) {
                    boolean afin = m.getOrDefault("interes_posgrado", "").equals("SI")
                            && CsvUtil.normalizar(m.getOrDefault("area_interes_posgrado", "")).contains(CsvUtil.normalizar(e[1]));
                    double pi = afin ? 0.8 : p * 0.2;
                    if (Math.random() < pi) d++;
                }
                demandas.add((double) d);
                if (r == 0) { primera = new ArrayList<>(); }
            }
            double media = StatsUtil.media(demandas), desv = StatsUtil.desvEstandar(demandas);
            double[] ic = StatsUtil.icMedia(demandas);
            Escenario es = new Escenario();
            es.nombre = e[0]; es.area = e[1]; es.probBase = p; es.media = media; es.desv = desv; es.li = ic[0]; es.ls = ic[1];
            es.muestra = new ArrayList<>();
            for (double d : demandas) es.muestra.add((int) Math.round(d));
            escenarios.add(es);
            if (escenarios.size() == 1) {
                ultimaMuestraHist = es.muestra;
            }
            sb.append(String.format(Locale.US,
                    "[%s] area=%s p_empirica=%.3f n_requerido(5%%)=%.0f\n  Demanda simulada (1000 reps): media=%s desv=%s IC95=[%s , %s]\n",
                    e[0], e[1], p, nReq, StatsUtil.f2(media), StatsUtil.f2(desv), StatsUtil.f2(ic[0]), StatsUtil.f2(ic[1])));
            List<double[]> h = StatsUtil.histograma(demandas, 10);
            for (double[] b : h)
                sb.append(String.format(Locale.US, "    [%5.1f-%5.1f]: %d\n", b[0], b[1], (int) b[2]));
        }
        // ---- Análisis de sensibilidad (§5 validación): ¿qué tan estables son
        // los indicadores si se pierde el 10% de la muestra? Se repite 200
        // veces: submuestra aleatoria del 90% (Math.random, sin reemplazo) y
        // se re-miden tasa de inserción, intención de posgrado y antigüedad
        // media. Se reporta el rango observado: rango estrecho = robusto.
        sb.append("\n--- Sensibilidad (quitar 10% aleatorio, 200 repeticiones) ---\n");
        double baseIns = prop(TIT, "situacion_laboral_actual", v -> !CsvUtil.normalizar(v).contains("NO_TRABAJA") && !v.isEmpty());
        double basePos = prop(TIT, "interes_posgrado", v -> v.equals("SI"));
        double baseAnt = num(TIT, "anios_vida_profesional").isEmpty() ? Double.NaN : StatsUtil.media(num(TIT, "anios_vida_profesional"));
        double minI = 2, maxI = -1, minP = 2, maxP = -1, minA = 1e9, maxA = -1e9;
        for (int b = 0; b < 200; b++) {
            // Submuestra del 90%: se baraja con sorteos y se toma el 90%.
            List<Map<String, String>> cop = new ArrayList<>(TIT.filas);
            for (int i = cop.size() - 1; i > 0; i--) {
                int j = (int) (Math.random() * (i + 1));
                Map<String, String> tmp = cop.get(i); cop.set(i, cop.get(j)); cop.set(j, tmp);
            }
            int keep = (int) Math.round(cop.size() * 0.9);
            List<Map<String, String>> sub = cop.subList(0, Math.max(1, keep));
            CsvUtil.Tabla ts = new CsvUtil.Tabla(); ts.headers = TIT.headers; ts.filas = sub;
            double pi = prop(ts, "situacion_laboral_actual", v -> !CsvUtil.normalizar(v).contains("NO_TRABAJA") && !v.isEmpty());
            double pp = prop(ts, "interes_posgrado", v -> v.equals("SI"));
            List<Double> aa = new ArrayList<>();
            for (Map<String, String> m : sub) {
                try { aa.add(Double.parseDouble(m.getOrDefault("anios_vida_profesional", "").replace(",", "."))); }
                catch (Exception ignored) {}
            }
            if (!Double.isNaN(pi)) { minI = Math.min(minI, pi); maxI = Math.max(maxI, pi); }
            if (!Double.isNaN(pp)) { minP = Math.min(minP, pp); maxP = Math.max(maxP, pp); }
            if (!aa.isEmpty()) { double ma = StatsUtil.media(aa); minA = Math.min(minA, ma); maxA = Math.max(maxA, ma); }
        }
        sb.append(String.format(Locale.US,
                "  insercion: base=%.1f%% rango90%%=[%.1f%%, %.1f%%] %s\n  intencion posgrado: base=%.1f%% rango90%%=[%.1f%%, %.1f%%] %s\n  antiguedad media: base=%s rango90%%=[%s, %s] %s\n",
                baseIns * 100, minI * 100, maxI * 100, etiquetaSens((maxI - minI) * 100, 10),
                basePos * 100, minP * 100, maxP * 100, etiquetaSens((maxP - minP) * 100, 10),
                StatsUtil.f2(baseAnt), StatsUtil.f2(minA), StatsUtil.f2(maxA),
                etiquetaSens(maxA - minA, 1.5)));
        return sb.toString();
    }

    // etiquetaSens(): regla de lectura del análisis de sensibilidad: si el
    // rango observado cabe en la tolerancia, el indicador es ROBUSTO.
    static String etiquetaSens(double rango, double tol) {
        if (Double.isNaN(rango)) return "(sin datos)";
        return rango <= tol ? "=> ROBUSTO" : "=> SENSIBLE (ampliar muestra)";
    }

    // ------------------------------------------------------------------------
    // moduloEmpleadores (§4.8 + cap.4 de la guía): lado DEMANDA del mercado.
    //   Descriptivo (tipo/tamaño/sector/demanda de formación), cruces
    //   sector×nivel y tamaño×posibilidad futura (χ²), los 10 Likert
    //   institucionales en % de acuerdo (= evidencia ARCUSUR de confianza y
    //   coherencia del perfil) y chequeo de cobertura de campo (§5 guía).
    // ------------------------------------------------------------------------
    static String moduloEmpleadores() {
        StringBuilder sb = new StringBuilder(encabezado("REPORTE EMPLEADORES"));
        sb.append("\n--- Descriptivo categorico ---\n");
        for (String c : new String[]{"tipo_organizacion", "tamano_organizacion", "sector_rubro",
                "contrato_ultimos_5_anios", "posibilidad_futura_contratacion", "nivel_formacion_demanda"}) {
            if (!EMP.headers.contains(c)) continue;
            Map<String, Integer> f = StatsUtil.frecuencias(col(EMP, c));
            sb.append("\n[").append(c).append("] moda=").append(StatsUtil.moda(f)).append("\n");
            int n = EMP.filas.size();
            List<Map.Entry<String, Integer>> e = new ArrayList<>(f.entrySet());
            e.sort((a, b) -> b.getValue() - a.getValue());
            for (Map.Entry<String, Integer> en : e)
                sb.append(String.format(Locale.US, "  %-28s %4d %5.1f%% %s\n", acortar(en.getKey(), 27),
                        en.getValue(), n == 0 ? 0 : 100.0 * en.getValue() / n, StatsUtil.barra(n == 0 ? 0 : 100.0 * en.getValue() / n, 25)));
        }
        sb.append("\n--- Cruces ---\n");
        String[][] pares = {{"sector_rubro", "nivel_formacion_demanda"}, {"tamano_organizacion", "posibilidad_futura_contratacion"}};
        for (String[] p : pares) {
            if (!EMP.headers.contains(p[0]) || !EMP.headers.contains(p[1])) continue;
            StatsUtil.TablaContingencia t = StatsUtil.contingencia(col(EMP, p[0]), col(EMP, p[1]));
            sb.append(String.format(Locale.US, "\n[%s x %s] chi2=%.3f gl=%d p=%.4f => %s\n",
                    p[0], p[1], t.chi2, t.gl, t.pValor,
                    (!Double.isNaN(t.pValor) && t.pValor < 0.05) ? "DEPENDENCIA" : "INDEPENDIENTES"));
        }
        sb.append("\n--- 10 Likert institucionales (% acuerdo = 3 o 4) ---\n");
        for (String c : LIKERT_EMP) {
            if (!EMP.headers.contains(c)) continue;
            List<Double> xs = likertCol(EMP, c);
            if (xs.isEmpty()) { sb.append(c).append(": sin datos\n"); continue; }
            sb.append(String.format(Locale.US, "%-28s media=%s acuerdo=%s%% n=%d %s\n", c,
                    StatsUtil.f2(StatsUtil.media(xs)), StatsUtil.f1(StatsUtil.pctAcuerdo(xs, 3)),
                    xs.size(), StatsUtil.barra(StatsUtil.pctAcuerdo(xs, 3), 25)));
        }
        // ---- Cobertura de campo (§5 guía): mínimo 3 empresas de software/TI,
        // al menos una pública y una privada. Si no se cumple, se advierte
        // para respaldar la toma de datos en campo.
        sb.append("\n--- Cobertura de campo (guia §5) ---\n");
        Set<String> empresas = new LinkedHashSet<>();
        boolean hayPub = false, hayPriv = false;
        for (Map<String, String> m : EMP.filas) {
            String nom = m.getOrDefault("nombre_empresa", "").trim();
            if (!nom.isEmpty()) empresas.add(nom);
            String t = CsvUtil.normalizar(m.getOrDefault("tipo_organizacion", ""));
            if (t.contains("PUBLICO")) hayPub = true;
            if (t.contains("PRIVADO")) hayPriv = true;
        }
        sb.append("Empresas distintas: " + empresas.size() + (empresas.size() >= 3 ? " OK (>=3)" : " INSUFICIENTE (minimo 3)") + "\n");
        sb.append("Sector publico: " + (hayPub ? "SI OK" : "NO FALTA") + " | Sector privado: " + (hayPriv ? "SI OK" : "NO FALTA") + "\n");
        if (empresas.size() < 3 || !hayPub || !hayPriv)
            sb.append("ACCION: use Registro para encuestar mas empresas antes del informe ARCUSUR.\n");
        return sb.toString();
    }

    // ------------------------------------------------------------------------
    // exportarCsvs (§4.9 + HU5): artefactos REUTILIZABLES para Excel/otros.
    //   matriz_contingencia.csv (área×financiamiento, cruza con HU2/HU3),
    //   histogramas.csv (datos crudos del gráfico [1]),
    //   datos_limpios_*.csv (trazabilidad: lo que entró vs lo analizado).
    // generarDashboard (§4.10 + HU5): escribe dashboard.html AUTOCONTENIDO
    //   (CSS+JS+datos JSON embebidos, canvas puro, cero dependencias): KPIs,
    //   9 gráficos (histograma, barras, torta, heatmap, radar, tabla χ²,
    //   simulación, ranking, Likert) + recomendaciones con reglas if-then.
    // ------------------------------------------------------------------------
    static void exportarCsvs() throws IOException {
        // matriz_contingencia.csv (area x financiamiento)
        StatsUtil.TablaContingencia t = StatsUtil.contingencia(
                columnaExtendida("area_interes_posgrado"), columnaExtendida("financiamiento_formacion"));
        List<String> h = new ArrayList<>(); h.add("area\\financiamiento"); h.addAll(t.columnas); h.add("TOTAL");
        List<List<String>> filas = new ArrayList<>();
        for (int i = 0; i < t.filas.size(); i++) {
            List<String> f = new ArrayList<>(); f.add(t.filas.get(i));
            int s = 0; for (int j = 0; j < t.columnas.size(); j++) { f.add(String.valueOf(t.obs[i][j])); s += t.obs[i][j]; }
            f.add(String.valueOf(s)); filas.add(f);
        }
        CsvUtil.escribir(salida.resolve("matriz_contingencia.csv"), h, filas);
        // histogramas.csv (anios_vida_profesional)
        List<double[]> hist = StatsUtil.histograma(num(TIT, "anios_vida_profesional"), 10);
        List<List<String>> fh = new ArrayList<>();
        for (double[] b : hist) fh.add(Arrays.asList(StatsUtil.f2(b[0]), StatsUtil.f2(b[1]), String.valueOf((int) b[2])));
        CsvUtil.escribir(salida.resolve("histogramas.csv"),
                Arrays.asList("lim_inf", "lim_sup", "frecuencia"), fh);
        // datos_limpios
        CsvUtil.escribir(salida.resolve("datos_limpios_titulados.csv"), TIT.headers,
                aFilas(TIT));
        CsvUtil.escribir(salida.resolve("datos_limpios_empleadores.csv"), EMP.headers,
                aFilas(EMP));
        System.out.println("  -> matriz_contingencia.csv, histogramas.csv, datos_limpios_*.csv");
    }

    static List<List<String>> aFilas(CsvUtil.Tabla t) {
        List<List<String>> out = new ArrayList<>();
        for (Map<String, String> m : t.filas) {
            List<String> f = new ArrayList<>();
            for (String c : t.headers) f.add(m.getOrDefault(c, ""));
            out.add(f);
        }
        return out;
    }

    // dashboardConsola (§4.10): resumen ejecutivo en consola (KPIs + top
    // hallazgos). Es lo primero que lee la Dirección de Carrera.
    // recomendaciones(): reglas if-then sobre los datos (panel [10] del
    // dashboard e insumo del cap.6 del informe). Cada regla cruza un umbral
    // con su acción (ej. recursos propios >50% -> convenio banca).
    static void dashboardConsola() {
        int n = TIT.filas.size(), m = EMP.filas.size();
        double pIns = prop(TIT, "situacion_laboral_actual", v -> !CsvUtil.normalizar(v).contains("NO_TRABAJA") && !v.isEmpty());
        double pPos = prop(TIT, "interes_posgrado", v -> v.equals("SI"));
        System.out.println("\n========== DASHBOARD (resumen ejecutivo) ==========");
        System.out.println("Titulados N=" + n + " | Empleadores M=" + m);
        System.out.println(String.format(Locale.US, "Tasa insercion: %s%% | Intencion posgrado: %s%% | Antiguedad media: %s anios",
                Double.isNaN(pIns) ? "NA" : StatsUtil.f1(pIns * 100),
                Double.isNaN(pPos) ? "NA" : StatsUtil.f1(pPos * 100),
                num(TIT, "anios_vida_profesional").isEmpty() ? "NA" : StatsUtil.f2(StatsUtil.media(num(TIT, "anios_vida_profesional")))));
        System.out.println("Top hallazgos y recomendaciones: ver panel #10 del dashboard.html");
        for (String r : recomendaciones()) System.out.println("  - " + r);
    }

    static List<String> recomendaciones() {
        List<String> rec = new ArrayList<>();
        Map<String, Integer> fArea = StatsUtil.frecuencias(col(TIT, "area_interes_posgrado"));
        int n = Math.max(1, TIT.filas.size());
        double pIA = fArea.getOrDefault("IA", 0) * 100.0 / n;
        if (pIA > 30) rec.add("Alta demanda en IA (" + StatsUtil.f1(pIA) + "%) -> abrir maestria modular en IA.");
        double pProp = prop(TIT, "financiamiento_formacion", v -> CsvUtil.normalizar(v).contains("PROPIO"));
        if (!Double.isNaN(pProp) && pProp > 0.5) rec.add("Mas del 50% usa recursos propios -> convenio con banca / becas parciales.");
        List<Double> cons = EMP.headers.contains("likert_consulta_regular") ? likertCol(EMP, "likert_consulta_regular") : new ArrayList<>();
        if (!cons.isEmpty() && StatsUtil.pctAcuerdo(cons, 3) < 50) rec.add("Consulta al empleador baja -> activar observatorio permanente.");
        List<Double> sat = likertCol(TIT, "satisfaccion_formacion");
        if (!sat.isEmpty() && StatsUtil.media(sat) < 3.0) rec.add("Satisfaccion < 3.0 -> revisar malla curricular.");
        Map<String, Integer> fT = StatsUtil.frecuencias(col(TIT, "tiempo_primer_empleo"));
        int mas6 = fT.getOrDefault("8_12_MESES", 0) + fT.getOrDefault("MAS_12_MESES", 0);
        if (mas6 * 1.0 / n > 0.25) rec.add("Insercion lenta (>25% tarda 8+ meses) -> fortalecer bolsa de trabajo.");
        if (rec.isEmpty()) rec.add("Muestra estable: mantener oferta actual y monitorear anualmente.");
        return rec;
    }

    // ---- HTML autocontenido (CSS + canvas puro + JSON embebido) ----
    // generarDashboard(): serializa los resultados a JSON (helpers aJson*),
    // los incrusta en <script> junto al dibujado canvas y escribe el .html.
    // Abrir el archivo en cualquier navegador = dashboard sin servidor.
    static void generarDashboard() throws IOException {
        Map<String, Integer> fArea = StatsUtil.frecuencias(col(TIT, "area_interes_posgrado"));
        Map<String, Integer> fFin = StatsUtil.frecuencias(col(TIT, "financiamiento_formacion"));
        List<double[]> hist = StatsUtil.histograma(num(TIT, "anios_vida_profesional"), 10);
        StatsUtil.TablaContingencia heat = StatsUtil.contingencia(
                columnaExtendida("area_interes_posgrado"), columnaExtendida("institucion_futura_posgrado"));
        // radar: medias 10 likert empleadores
        List<String> radarLab = new ArrayList<>(); List<Double> radarVal = new ArrayList<>();
        for (String c : LIKERT_EMP) {
            if (!EMP.headers.contains(c)) continue;
            List<Double> xs = likertCol(EMP, c);
            if (xs.isEmpty()) continue;
            radarLab.add(c.replace("likert_", ""));
            radarVal.add(StatsUtil.media(xs));
        }
        Map<String, Integer> fVent = contarOpciones("asignaturas_ventajosas");
        Map<String, Integer> fInut = contarOpciones("asignaturas_inutiles");
        List<Double> satMed = num(TIT, "anios_vida_profesional");

        String json = "{"
                + "\"n\":" + TIT.filas.size() + ",\"m\":" + EMP.filas.size() + ","
                + "\"hist\":" + aJsonHist(hist) + ","
                + "\"areas\":" + aJsonFrec(fArea) + ","
                + "\"fin\":" + aJsonFrec(fFin) + ","
                + "\"heat\":{\"filas\":" + aJsonStr(heat.filas) + ",\"cols\":" + aJsonStr(heat.columnas)
                + ",\"obs\":" + aJsonMat(heat.obs) + ",\"chi2\":" + round3(heat.chi2) + ",\"gl\":" + heat.gl
                + ",\"p\":" + (Double.isNaN(heat.pValor) ? "null" : round3(heat.pValor)) + "},"
                + "\"radarLab\":" + aJsonStr(radarLab) + ",\"radarVal\":" + aJsonNum(radarVal) + ","
                + "\"sim\":" + aJsonNumInt(escenarios.isEmpty() ? new ArrayList<>() : escenarios.get(0).muestra) + ","
                + "\"simMedia\":" + (escenarios.isEmpty() ? "0" : StatsUtil.f2(escenarios.get(0).media)) + ","
                + "\"vent\":" + aJsonFrec(fVent) + ",\"inut\":" + aJsonFrec(fInut) + ","
                + "\"likert\":" + aJsonLikert()
                + "}";

        String recs = "";
        for (String r : recomendaciones()) recs += "<li>" + escHtml(r) + "</li>\n";

        String html = "<!DOCTYPE html><html lang='es'><head><meta charset='utf-8'>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<title>Dashboard — Mercado Titulados Ing. Sistemas UMSS</title>"
                + "<style>body{font-family:Arial,sans-serif;background:#f2f4f8;margin:0;color:#1c2733}"
                + "header{background:#0b3d62;color:#fff;padding:18px 24px}"
                + ".wrap{max-width:1100px;margin:auto;padding:16px}"
                + ".kpis{display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:12px;margin:14px 0}"
                + ".kpi{background:#fff;border-radius:10px;padding:14px;box-shadow:0 1px 4px rgba(0,0,0,.12)}"
                + ".card{background:#fff;border-radius:10px;padding:16px;margin:12px 0;box-shadow:0 1px 4px rgba(0,0,0,.12)}"
                + "nav{display:flex;flex-wrap:wrap;gap:6px;margin:12px 0}"
                + "nav a{background:#0b3d62;color:#fff;text-decoration:none;font-size:13px;padding:6px 10px;border-radius:20px}"
                + "nav a:hover{background:#0984e3}"
                + "#tip{position:fixed;display:none;background:#1c2733;color:#fff;font-size:13px;padding:5px 9px;border-radius:6px;pointer-events:none;z-index:99}"
                + "canvas{width:100%;height:260px;border:1px solid #e1e6ec;border-radius:8px;background:#fff}"
                + "table{border-collapse:collapse;width:100%;font-size:13px}td,th{border:1px solid #cbd5e1;padding:5px 8px;text-align:center}"
                + "th{background:#0b3d62;color:#fff}h2{font-size:18px;margin:4px 0 10px}</style></head><body>"
                + "<header><h1>Dashboard — Análisis de Mercado Titulados Ing. Sistemas UMSS</h1>"
                + "<div>Fuente: titulados.csv (N=" + TIT.filas.size() + ") + empleadores.csv (M=" + EMP.filas.size()
                + ") | Fecha: " + LocalDate.now() + "</div></header><div class='wrap'>"
                + "<nav><a href='#c1'>[1] Antigüedad</a><a href='#c2'>[2] Áreas</a><a href='#c3'>[3] Financiamiento</a>"
                + "<a href='#c4'>[4] Calor</a><a href='#c5'>[5] Radar</a><a href='#t6'>[6] χ²</a>"
                + "<a href='#c7'>[7] Simulación</a><a href='#c8'>[8] Asignaturas</a><a href='#c9'>[9] Empleadores</a></nav>"
                + "<div id='tip'></div>"
                + "<div class='kpis'>" + kpisHtml() + "</div>"
                + "<div class='card'><h2>[1] Histograma — Años de vida profesional</h2><canvas id='c1'></canvas></div>"
                + "<div class='card'><h2>[2] Barras — Área de interés posgrado</h2><canvas id='c2'></canvas></div>"
                + "<div class='card'><h2>[3] Torta — Financiamiento</h2><canvas id='c3'></canvas></div>"
                + "<div class='card'><h2>[4] Mapa de calor — Área × Institución futura</h2><canvas id='c4' style='height:300px'></canvas></div>"
                + "<div class='card'><h2>[5] Radar — Likert institucional empleadores (1-4)</h2><canvas id='c5'></canvas></div>"
                + "<div class='card'><h2>[6] Contingencia Área × Financiamiento + χ²</h2><div id='t6'></div></div>"
                + "<div class='card'><h2>[7] Proyección Monte Carlo — " + (escenarios.isEmpty() ? "" : escHtml(escenarios.get(0).nombre))
                + " (media " + (escenarios.isEmpty() ? "" : StatsUtil.f2(escenarios.get(0).media)) + ")</h2><canvas id='c7'></canvas></div>"
                + "<div class='card'><h2>[8] Asignaturas ventajosas vs inútiles</h2><canvas id='c8'></canvas></div>"
                + "<div class='card'><h2>[9] Empleadores — % acuerdo por ítem Likert</h2><canvas id='c9'></canvas></div>"
                + "<div class='card'><h2>[10] Recomendaciones automáticas</h2><ul>" + recs + "</ul></div>"
                + "</div><script>var D=" + json + ";\n" + JS + "</script></body></html>";

        Files.writeString(salida.resolve("dashboard.html"), html, StandardCharsets.UTF_8);
        System.out.println("  -> dashboard.html");
    }

    // ------------------------------------------------------------------------
    // moduloInforme(): PRODUCTO FINAL de la guía §2 y §4 — "Documento de
    // Análisis de Mercado" con la tabla de contenidos oficial ARCUSUR
    // (7 capítulos). No es texto fijo: cada cifra se calcula de los CSV
    // (N, medias, IC, χ², rankings), así el informe siempre refleja los datos
    // reales cargados. Cierra con referencias APA 7.ª ed. (sin URLs).
    // ------------------------------------------------------------------------
    static String moduloInforme() {
        StringBuilder sb = new StringBuilder();
        sb.append("ANALISIS DE MERCADO — PERFIL, FORMACION Y EMPLEABILIDAD\n");
        sb.append("Titulados de Ingenieria en Sistemas, UMSS (insumo Plan de Desarrollo\n");
        sb.append("Estrategico y acreditacion ARCUSUR)\n");
        sb.append("Fecha: ").append(LocalDate.now())
                .append(" | N titulados=").append(TIT.filas.size())
                .append(" | M empleadores=").append(EMP.filas.size()).append("\n\n");

        List<Double> ant = num(TIT, "anios_vida_profesional");
        double mAnt = ant.isEmpty() ? Double.NaN : StatsUtil.media(ant);
        double sAnt = StatsUtil.desvEstandar(ant);
        double pIns = prop(TIT, "situacion_laboral_actual", v -> !CsvUtil.normalizar(v).contains("NO_TRABAJA") && !v.isEmpty());
        double pPos = prop(TIT, "interes_posgrado", v -> v.equals("SI"));
        double[] icIns = StatsUtil.icProporcion(pIns, TIT.filas.size());
        Map<String, Integer> fArea = StatsUtil.frecuencias(col(TIT, "area_interes_posgrado"));
        List<Map.Entry<String, Integer>> areas = new ArrayList<>(fArea.entrySet());
        areas.sort((a, b) -> b.getValue() - a.getValue());
        Map<String, Integer> fSec = EMP.headers.contains("sector_rubro")
                ? StatsUtil.frecuencias(col(EMP, "sector_rubro")) : new LinkedHashMap<>();

        sb.append("1. INTRODUCCION Y JUSTIFICACION\n");
        sb.append("La Carrera de Ingenieria en Sistemas (UMSS) desarrolla su autoevaluacion\n");
        sb.append("para el Plan de Desarrollo Estrategico y su postulacion ante ARCUSUR.\n");
        sb.append("Objetivo general: cuantificar tendencias de formacion continua, patrones\n");
        sb.append("demograficos y de preferencia academica de los titulados. Objetivos\n");
        sb.append("especificos: HU1 antiguedad/empleabilidad, HU2 demanda de posgrado,\n");
        sb.append("HU3 financiamiento, HU4 brechas de competencias, HU5 reportes y\n");
        sb.append("dashboard. Justificacion: evidencia empirica de pertinencia curricular\n");
        sb.append("y de gestion de alianzas, criterio exigible por pares ARCUSUR.\n\n");

        sb.append("2. MARCO METODOLOGICO\n");
        sb.append("Diseno cuantitativo exploratorio sobre encuestas estructuradas (titulados\n");
        sb.append("N=").append(TIT.filas.size()).append(", empleadores M=").append(EMP.filas.size()).append(").\n");
        sb.append("Pipeline: Data Wrangling (validacion + limpieza) -> estadistica univariada\n");
        sb.append("(media, mediana, desv. estandar, frecuencias, histogramas) -> bivariada\n");
        sb.append("con pruebas chi-cuadrada de independencia -> IC 95% (t Student para\n");
        sb.append("medias, Z para proporciones, varianzas agrupadas para diferencias) ->\n");
        sb.append("experimentacion Monte Carlo ligera (solo Math.random) -> dashboard.\n");
        sb.append("Sin generadores congruenciales: la fuente son los datos reales.\n\n");

        sb.append("3. DEMANDA EDUCATIVA Y PERFIL DE LOS TITULADOS\n");
        sb.append(String.format(Locale.US,
                "Antiguedad media %.2f anios (desv. %.2f). Tasa de insercion %.1f%% IC95 [%.1f, %.1f].\n",
                mAnt, sAnt, pIns * 100, icIns[0] * 100, icIns[1] * 100));
        sb.append(String.format(Locale.US, "Intencion de posgrado %.1f%%. Areas top: ", pPos * 100));
        int mostradas = 0; // (VACIO) = sin interés declarado: no es un área y se excluye del top
        for (int i = 0; i < areas.size() && mostradas < 3; i++) {
            if (areas.get(i).getKey().equals("(VACIO)")) continue;
            sb.append(areas.get(i).getKey()).append(" (").append(areas.get(i).getValue()).append(") ");
            mostradas++;
        }
        sb.append("\nSegmentacion Junior/Intermedio/Consolidado y detalle en reporte_segmentacion.txt.\n\n");

        sb.append("4. OFERTA LABORAL Y REQUERIMIENTOS DEL SECTOR\n");
        List<Map.Entry<String, Integer>> secs = new ArrayList<>(fSec.entrySet());
        secs.sort((a, b) -> b.getValue() - a.getValue());
        sb.append("Sectores top empleadores: ");
        for (int i = 0; i < Math.min(3, secs.size()); i++)
            sb.append(secs.get(i).getKey()).append(" (").append(secs.get(i).getValue()).append(") ");
        sb.append("\nNivel de formacion mas demandado y Likert institucional en reporte_empleadores.txt;\n");
        sb.append("competencias emergentes del texto libre en reporte_brechas.txt.\n\n");

        sb.append("5. DIAGNOSTICO Y SINTESIS\n");
        StatsUtil.TablaContingencia taf = StatsUtil.contingencia(
                columnaExtendida("area_interes_posgrado"), columnaExtendida("financiamiento_formacion"));
        sb.append(String.format(Locale.US,
                "Cruce area x financiamiento: chi2=%.2f gl=%d p=%.4f (%s). ",
                taf.chi2, taf.gl, taf.pValor, (taf.pValor < 0.05 ? "dependencia" : "independencia")));
        StatsUtil.TablaContingencia tfn = StatsUtil.contingencia(
                columnaExtendida("financiamiento_formacion"), columnaExtendida("nivel_posgrado"));
        sb.append(String.format(Locale.US,
                "Cruce financiamiento x nivel: chi2=%.2f gl=%d p=%.4f (%s).\n",
                tfn.chi2, tfn.gl, tfn.pValor, (tfn.pValor < 0.05 ? "dependencia" : "independencia")));
        sb.append("Hallazgos y brechas criticas: ver reportes bivariado/brechas; estabilidad\n");
        sb.append("validada por sensibilidad del 90% (reporte_experimentacion.txt).\n\n");

        sb.append("6. CONCLUSIONES Y PROPUESTAS ESTRATEGICAS\n");
        List<String> recs = recomendaciones();
        for (int i = 0; i < recs.size(); i++) sb.append((i + 1) + ". " + recs.get(i) + "\n");
        sb.append("Lineas permanentes: posgrados modulares, diversificacion de becas,\n");
        sb.append("vinculacion B2B y observatorio permanente de egresados.\n\n");

        sb.append("7. REFERENCIAS BIBLIOGRAFICAS (APA 7)\n");
        sb.append("Coss Bu, R. (1993). Simulacion: Un enfoque practico. Limusa.\n");
        sb.append("Hernandez Sampieri, R., Fernandez Collado, C., y Baptista Lucio, P. (2014).\n");
        sb.append("    Metodologia de la investigacion (6.a ed.). McGraw-Hill.\n");
        sb.append("Walpole, R. E., Myers, R. H., Myers, S. L., y Ye, K. (2012).\n");
        sb.append("    Probabilidad y estadistica para ingenieria y ciencias (9.a ed.). Pearson.\n");
        sb.append("Red de Agencias Nacionales de Acreditacion. (2008). Sistema de\n");
        sb.append("    acreditacion regional de carreras universitarias (ARCUSUR).\n");
        sb.append("Carrera de Ingenieria en Sistemas, UMSS. Encuestas a titulados y\n");
        sb.append("    empleadores: instrumento de autoevaluacion (documento interno).\n");
        return sb.toString();
    }

    static String kpisHtml() {
        List<Double> ant = num(TIT, "anios_vida_profesional");
        double pIns = prop(TIT, "situacion_laboral_actual", v -> !CsvUtil.normalizar(v).contains("NO_TRABAJA") && !v.isEmpty());
        double pPos = prop(TIT, "interes_posgrado", v -> v.equals("SI"));
        double pBeca = prop(TIT, "financiamiento_formacion", v -> CsvUtil.normalizar(v).contains("BECA"));
        return "<div class='kpi'><b>Antigüedad media</b><div style='font-size:22px'>"
                + (ant.isEmpty() ? "NA" : StatsUtil.f2(StatsUtil.media(ant)) + " ± " + StatsUtil.f2(StatsUtil.desvEstandar(ant)))
                + "</div></div><div class='kpi'><b>Tasa inserción</b><div style='font-size:22px'>"
                + (Double.isNaN(pIns) ? "NA" : StatsUtil.f1(pIns * 100) + "%")
                + "</div></div><div class='kpi'><b>Intención posgrado</b><div style='font-size:22px'>"
                + (Double.isNaN(pPos) ? "NA" : StatsUtil.f1(pPos * 100) + "%")
                + "</div></div><div class='kpi'><b>Requiere beca</b><div style='font-size:22px'>"
                + (Double.isNaN(pBeca) ? "NA" : StatsUtil.f1(pBeca * 100) + "%") + "</div></div>";
    }

    static Map<String, Integer> contarOpciones(String colTIT) {
        Map<String, Integer> f = new LinkedHashMap<>();
        if (!TIT.headers.contains(colTIT)) return f;
        for (String v : col(TIT, colTIT)) {
            if (v == null || v.isEmpty()) continue;
            for (String op : v.split("\\|")) { op = op.trim(); if (!op.isEmpty()) f.put(op, f.getOrDefault(op, 0) + 1); }
        }
        return f;
    }

    // ---- JSON helpers ----
    static String aJsonHist(List<double[]> h) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < h.size(); i++) {
            if (i > 0) s.append(",");
            s.append("[").append(StatsUtil.f2(h.get(i)[0])).append(",").append(StatsUtil.f2(h.get(i)[1]))
                    .append(",").append((int) h.get(i)[2]).append("]");
        }
        return s.append("]").toString();
    }

    static String aJsonFrec(Map<String, Integer> f) {
        StringBuilder s = new StringBuilder("[");
        boolean p = false;
        for (Map.Entry<String, Integer> e : f.entrySet()) {
            if (p) s.append(","); p = true;
            s.append("{\"k\":").append(q(e.getKey())).append(",\"v\":").append(e.getValue()).append("}");
        }
        return s.append("]").toString();
    }

    static String aJsonStr(List<String> l) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < l.size(); i++) { if (i > 0) s.append(","); s.append(q(l.get(i))); }
        return s.append("]").toString();
    }

    static String aJsonNum(List<Double> l) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < l.size(); i++) { if (i > 0) s.append(","); s.append(StatsUtil.f2(l.get(i))); }
        return s.append("]").toString();
    }

    static String aJsonNumInt(List<Integer> l) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < l.size(); i++) { if (i > 0) s.append(","); s.append(l.get(i)); }
        return s.append("]").toString();
    }

    static String aJsonMat(int[][] m) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < m.length; i++) {
            if (i > 0) s.append(",");
            s.append("[");
            for (int j = 0; j < m[i].length; j++) { if (j > 0) s.append(","); s.append(m[i][j]); }
            s.append("]");
        }
        return s.append("]").toString();
    }

    static String aJsonLikert() {
        StringBuilder s = new StringBuilder("[");
        boolean p = false;
        for (String c : LIKERT_EMP) {
            if (!EMP.headers.contains(c)) continue;
            List<Double> xs = likertCol(EMP, c);
            if (xs.isEmpty()) continue;
            if (p) s.append(","); p = true;
            s.append("{\"k\":").append(q(c.replace("likert_", ""))).append(",\"v\":")
                    .append(StatsUtil.f1(StatsUtil.pctAcuerdo(xs, 3))).append("}");
        }
        return s.append("]").toString();
    }

    static String q(String s) {
        if (s == null) s = "";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    static String round3(double x) {
        if (Double.isNaN(x) || Double.isInfinite(x)) return "0";
        return String.format(Locale.US, "%.3f", x);
    }

    static String escHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** JS con canvas puro (sin librerías): 9 gráficos + tooltips hover (HU5 "interactivo").
     *  Cómo funciona bars(): dibuja a doble resolución (retina) y guarda la
     *  geometría de cada barra en c._bars; un listener mousemove traduce el
     *  cursor a coordenadas del canvas, detecta la barra bajo el puntero y
     *  muestra el div #tip con "etiqueta: valor". Sin dependencias externas. */
    static final String JS =
        "var TIP=document.getElementById('tip');\n"
        + "function ctx(id){var c=document.getElementById(id);return[c,c.getContext('2d')];}\n"
        + "function bars(id,data,labels,color){var a=ctx(id),c=a[0],g=a[1];var W=c.width=c.offsetWidth*2,H=c.height=520;"
        + "g.fillStyle='#fff';g.fillRect(0,0,W,H);var mx=Math.max.apply(null,data.concat([1]));"
        + "var n=data.length,bw=W/(n*1.6);c._bars=[];"
        + "for(var i=0;i<n;i++){var h=(H-90)*(data[i]/mx);var x=20+i*1.6*bw;"
        + "g.fillStyle=color;g.fillRect(x,H-60-h,bw,h);g.fillStyle='#1c2733';g.font='20px Arial';"
        + "g.fillText(String(data[i]),x,H-40);var lb=(labels[i]||'').substring(0,12);g.fillText(lb,x,H-12);"
        + "c._bars.push({x:x,y:H-60-h,w:bw,h:h,label:labels[i]||'',val:data[i]});}"
        + "c.onmousemove=function(ev){var r=c.getBoundingClientRect();var sx=c.width/r.width,sy=c.height/r.height;"
        + "var px=(ev.clientX-r.left)*sx,py=(ev.clientY-r.top)*sy;var f=null;"
        + "for(var k=0;k<c._bars.length;k++){var b=c._bars[k];if(px>=b.x&&px<=b.x+b.w&&py>=b.y&&py<=b.y+b.h){f=b;break;}}"
        + "if(f){TIP.style.display='block';TIP.style.left=(ev.clientX+12)+'px';TIP.style.top=(ev.clientY+12)+'px';"
        + "TIP.textContent=f.label+': '+f.val;}else{TIP.style.display='none';};};"
        + "c.onmouseleave=function(){TIP.style.display='none';};}\n"
        + "bars('c1',D.hist.map(function(r){return r[2];}),D.hist.map(function(r){return r[0]+'-'+r[1];}),'#0b3d62');\n"
        + "bars('c2',D.areas.map(function(r){return r.v;}),D.areas.map(function(r){return r.k;}),'#0984e3');\n"
        + "(function(){var a=ctx('c3'),c=a[0],g=a[1];var W=c.width=c.offsetWidth*2,H=c.height=520;"
        + "var tot=D.fin.reduce(function(s,r){return s+r.v;},0)||1;var ang=-Math.PI/2;var cx=W/2,cy=H/2,R=Math.min(W,H)/2-30;"
        + "var cols=['#0b3d62','#0984e3','#00cec9','#e17055','#6c5ce7','#fdcb6e'];"
        + "D.fin.forEach(function(r,i){var a2=ang+Math.PI*2*r.v/tot;g.beginPath();g.moveTo(cx,cy);g.arc(cx,cy,R,ang,a2);g.closePath();"
        + "g.fillStyle=cols[i%cols.length];g.fill();ang=a2;});g.fillStyle='#1c2733';g.font='20px Arial';var y=30;"
        + "D.fin.forEach(function(r,i){g.fillStyle=cols[i%cols.length];g.fillRect(10,y-14,22,16);g.fillStyle='#1c2733';"
        + "g.fillText(r.k+' '+(100*r.v/tot).toFixed(1)+'%',42,y);y+=28;});})();\n"
        + "(function(){var a=ctx('c4'),c=a[0],g=a[1];var W=c.width=c.offsetWidth*2,H=c.height=600;"
        + "var F=D.heat.filas,C=D.heat.cols,O=D.heat.obs;var mx=1;O.forEach(function(r){r.forEach(function(v){mx=Math.max(mx,v);});});"
        + "var cw=(W-260)/Math.max(1,C.length),rh=(H-60)/Math.max(1,F.length);g.font='18px Arial';"
        + "for(var j=0;j<C.length;j++){g.fillStyle='#1c2733';g.fillText(C[j].substring(0,10),260+j*cw,H-8);}"
        + "for(var i=0;i<F.length;i++){g.fillStyle='#1c2733';g.fillText(F[i].substring(0,22),4,30+i*rh+rh/2);"
        + "for(var k=0;k<C.length;k++){var t=O[i][k]/mx;g.fillStyle='rgb('+Math.round(11+t*200)+','+Math.round(61+t*100)+','+Math.round(98+t*60)+')';"
        + "g.fillRect(260+k*cw,20+i*rh,cw-3,rh-6);g.fillStyle='#fff';g.fillText(String(O[i][k]),260+k*cw+8,30+i*rh+rh/2);}}})();\n"
        + "(function(){var a=ctx('c5'),c=a[0],g=a[1];var W=c.width=c.offsetWidth*2,H=c.height=520;var cx=W/2,cy=H/2+10,R=Math.min(W,H)/2-60;"
        + "var n=D.radarLab.length||1;function pt(i,v){var an=-Math.PI/2+i*2*Math.PI/n;return[cx+R*v/4*Math.cos(an),cy+R*v/4*Math.sin(an)];}"
        + "for(var ring=1;ring<=4;ring++){g.beginPath();for(var i=0;i<=n;i++){var p=pt(i%n,ring);if(i==0)g.moveTo(p[0],p[1]);else g.lineTo(p[0],p[1]);}g.strokeStyle='#dfe6ee';g.stroke();}"
        + "g.beginPath();for(var k=0;k<n;k++){var q=pt(k,D.radarVal[k]);if(k==0)g.moveTo(q[0],q[1]);else g.lineTo(q[0],q[1]);}"
        + "g.closePath();g.strokeStyle='#0b3d62';g.lineWidth=4;g.stroke();g.fillStyle='rgba(9,132,227,.25)';g.fill();"
        + "g.fillStyle='#1c2733';g.font='20px Arial';for(var m=0;m<n;m++){var l=pt(m,4.4);g.fillText(D.radarLab[m].substring(0,12),l[0]-30,l[1]);}})();\n"
        + "(function(){var h='<table><tr><th>Área \\\\ Financ.</th>'+D.heat.cols.map(function(c){return '<th>'+c+'</th>';}).join('')+'</tr>';"
        + "D.heat.filas.forEach(function(f,i){h+='<tr><td><b>'+f+'</b></td>'+D.heat.obs[i].map(function(v){return '<td>'+v+'</td>';}).join('')+'</tr>';});"
        + "h+='</table><p>χ²='+D.heat.chi2+' gl='+D.heat.gl+' p='+(D.heat.p==null?'NA':D.heat.p)+'</p>';document.getElementById('t6').innerHTML=h;})();\n"
        + "(function(){var m={};D.sim.forEach(function(v){m[v]=(m[v]||0)+1;});var ks=Object.keys(m).map(Number).sort(function(a,b){return a-b;});"
        + "bars('c7',ks.map(function(k){return m[k];}),ks.map(String),'#27ae60');})();\n"
        + "(function(){var L=D.vent.concat(D.inut);var mx=1;L.forEach(function(r){mx=Math.max(mx,r.v);});"
        + "bars('c8',L.map(function(r){return r.v;}),L.map(function(r){return r.k;}),'#e17055');})();\n"
        + "bars('c9',D.likert.map(function(r){return r.v;}),D.likert.map(function(r){return r.k;}),'#6c5ce7');\n";
}
