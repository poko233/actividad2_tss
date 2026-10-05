import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

// ============================================================================
// Analizador — Pipeline de análisis de mercado, titulados Ing. Sistemas UMSS.
// ----------------------------------------------------------------------------
// QUÉ HACE (guía ARCUSUR, HU1–HU5):
//   HU1 Antigüedad y empleabilidad  -> describirVariablesNumericas +
//                                      clasificarPorAntiguedad + moduloSegmentacion
//   HU2 Formación continua          -> frecuencias de área + cruces área×institución
//   HU3 Financiamiento              -> cruce financiamiento×nivel + χ² + IC de beca
//   HU4 Brechas de competencias     -> moduloBrechas (ver NOTA HU4 dentro)
//   HU5 Reportes y dashboard        -> exportarArchivosCsv + generarDashboardHtml
// CÓMO SE USA:
//   Recolectar datos : java -cp bin Registro titulados.csv empleadores.csv
//   Analizar         : java -cp bin Analizador titulados.csv empleadores.csv salida
// REGLAS DEL PROYECTO (no negociables):
//   - Sin generadores congruenciales, sin rechazo/composición/transformada.
//   - Única aleatoriedad: Math.random() en Monte Carlo y en sensibilidad.
//   - La única fuente de información son los CSV de encuestas.
// CONVENCIÓN DE NOMBRES: todo en español, sin abreviaturas; cada método
// explica en su cabecera QUÉ calcula, de QUÉ HU viene y CÓMO leer el resultado.
// ============================================================================
public class Analizador {

    // ---- Estado global del análisis (se llena una vez en main) ----
    static Path carpetaSalida;              // carpeta donde se escriben reportes
    static CsvUtil.Tabla titulados;         // encuesta a titulados ya limpia
    static CsvUtil.Tabla empleadores;       // encuesta a empleadores ya limpia

    // ---- Columnas obligatorias: si faltan, se avisa pero no se falla ----
    static final String[] COLUMNAS_OBLIGATORIAS_TITULADOS = {"id_titulado", "edad", "genero",
            "interes_posgrado", "anios_vida_profesional", "situacion_laboral_actual"};
    static final String[] COLUMNAS_OBLIGATORIAS_EMPLEADORES = {"id_empleador", "nombre_empresa",
            "tipo_organizacion", "tamano_organizacion", "sector_rubro"};

    // ---- Los 10 ítems Likert institucionales de la encuesta a empleadores ----
    static final String[] ITEMS_LIKERT_EMPLEADOR = {"likert_confianza_formadora",
            "likert_consistencia_titulo", "likert_consulta_regular", "likert_conoce_perfil",
            "likert_coherencia_perfil", "likert_formacion_desempeno", "likert_considera_opinion",
            "likert_competencias_laborales", "likert_valores_actitudes", "likert_participacion_procesos"};

    // ---- Nombres de columna que se usan en varios módulos (un solo lugar) ----
    static final String COL_ANIOS_VIDA_PROFESIONAL = "anios_vida_profesional";
    static final String COL_ANIOS_DESEMPLEO = "anios_desempleado";
    static final String COL_INTERES_POSGRADO = "interes_posgrado";
    static final String COL_AREA_INTERES = "area_interes_posgrado";
    static final String COL_SITUACION_LABORAL = "situacion_laboral_actual";
    static final String COL_FINANCIAMIENTO = "financiamiento_formacion";
    static final String COL_NIVEL_POSGRADO = "nivel_posgrado";
    static final String COL_SATISFACCION_FORMACION = "satisfaccion_formacion";

    // ------------------------------------------------------------------------
    // main(): director del pipeline. Etapas:
    //   1) Carga + Data Wrangling de ambos CSV (módulo 4.1).
    //   2) Cálculo de los 8 módulos de análisis (4.2–4.8 + informe ARCUSUR).
    //   3) Escritura de reportes .txt, exportables .csv y dashboard.html.
    //   4) Resumen ejecutivo en consola para la Dirección de Carrera.
    // ------------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        // Argumentos opcionales con valores por defecto (carpeta actual).
        String rutaTitulados = args.length > 0 ? args[0] : "titulados.csv";
        String rutaEmpleadores = args.length > 1 ? args[1] : "empleadores.csv";
        String nombreCarpetaSalida = args.length > 2 ? args[2] : "salida";
        carpetaSalida = Paths.get(nombreCarpetaSalida);
        Files.createDirectories(carpetaSalida);

        System.out.println("=== ANALISIS DE MERCADO TITULADOS ING. SISTEMAS UMSS ===");
        titulados = cargarEncuesta(rutaTitulados, COLUMNAS_OBLIGATORIAS_TITULADOS, "titulados");
        empleadores = cargarEncuesta(rutaEmpleadores, COLUMNAS_OBLIGATORIAS_EMPLEADORES, "empleadores");
        limpiarDatos(titulados);
        limpiarDatos(empleadores);

        // Cada módulo devuelve su reporte como texto (luego se guarda a disco).
        String reporteUnivariado = moduloUnivariado();
        String reporteBivariado = moduloBivariado();
        String reporteIntervalos = moduloIntervalos();
        String reporteBrechas = moduloBrechas();
        String reporteSegmentacion = moduloSegmentacion();
        String reporteExperimentacion = moduloExperimentacion();
        String reporteEmpleadores = moduloEmpleadores();
        String informeArcusur = moduloInforme(); // documento final, estructura §2

        guardarReporte("reporte_univariado.txt", reporteUnivariado);
        guardarReporte("reporte_bivariado.txt", reporteBivariado);
        guardarReporte("reporte_intervalos.txt", reporteIntervalos);
        guardarReporte("reporte_brechas.txt", reporteBrechas);
        guardarReporte("reporte_segmentacion.txt", reporteSegmentacion);
        guardarReporte("reporte_experimentacion.txt", reporteExperimentacion);
        guardarReporte("reporte_empleadores.txt", reporteEmpleadores);
        guardarReporte("informe_mercado.txt", informeArcusur);

        exportarArchivosCsv();
        generarDashboardHtml();

        mostrarResumenEjecutivo();
        System.out.println("\nArchivos generados en: " + carpetaSalida.toAbsolutePath());
        System.out.println("Abra dashboard.html en su navegador.");
    }

    // ------------------------------------------------------------------------
    // CARGA Y LIMPIEZA (módulo 4.1 = fase Data Wrangling del marco §2).
    // cargarEncuesta(): lee el CSV; si el archivo no existe sigue con tabla
    //   vacía para no bloquear el otro análisis. Valida columnas obligatorias
    //   y AVISA las faltantes (no interrumpe).
    // limpiarDatos(): normaliza cada celda — recorta espacios, convierte
    //   nulos ("null","n/a","-") en "", invalida números mal escritos y
    //   unifica SI/NO. Sin esto, "Si" y "SI" contarían como categorías
    //   distintas y los cruces χ² saldrían fragmentados.
    // ------------------------------------------------------------------------
    static CsvUtil.Tabla cargarEncuesta(String rutaArchivo, String[] columnasObligatorias,
            String nombreEncuesta) throws IOException {
        Path ruta = Paths.get(rutaArchivo);
        if (!Files.exists(ruta)) {
            System.out.println("[AVISO] No existe " + rutaArchivo + " (" + nombreEncuesta
                    + "). Se continua con 0 registros.");
            CsvUtil.Tabla tablaVacia = new CsvUtil.Tabla();
            tablaVacia.headers.addAll(Arrays.asList(columnasObligatorias));
            return tablaVacia;
        }
        CsvUtil.Tabla tablaEncuesta = CsvUtil.leer(ruta);
        List<String> columnasFaltantes = new ArrayList<>();
        for (String columnaObligatoria : columnasObligatorias)
            if (!tablaEncuesta.headers.contains(columnaObligatoria))
                columnasFaltantes.add(columnaObligatoria);
        if (!columnasFaltantes.isEmpty())
            System.out.println("[AVISO] " + rutaArchivo + " columnas faltantes: " + columnasFaltantes
                    + " (se asumen vacias)");
        System.out.println("[OK] " + rutaArchivo + ": " + tablaEncuesta.filas.size()
                + " registros, " + tablaEncuesta.headers.size() + " columnas.");
        return tablaEncuesta;
    }

    /** Data wrangling: recorta, vacía nulos, unifica SI/NO, depura numéricos. */
    static void limpiarDatos(CsvUtil.Tabla tabla) {
        Set<String> columnasNumericas = new HashSet<>(
                Arrays.asList("edad", COL_ANIOS_VIDA_PROFESIONAL, COL_ANIOS_DESEMPLEO));
        Set<String> columnasSiNo = new HashSet<>(Arrays.asList(COL_INTERES_POSGRADO,
                "realizo_formacion_complementaria", "red_contactos", "primer_empleo_actual",
                "contrato_ultimos_5_anios"));
        for (Map<String, String> respuesta : tabla.filas) {
            for (String nombreColumna : tabla.headers) {
                String valorCelda = respuesta.getOrDefault(nombreColumna, "");
                if (valorCelda == null) valorCelda = "";
                valorCelda = valorCelda.trim();
                if (valorCelda.equalsIgnoreCase("null") || valorCelda.equalsIgnoreCase("n/a")
                        || valorCelda.equals("-") || valorCelda.equalsIgnoreCase("na"))
                    valorCelda = "";
                if (columnasNumericas.contains(nombreColumna) && !valorCelda.isEmpty()) {
                    try { Double.parseDouble(valorCelda.replace(",", ".")); }
                    catch (NumberFormatException errorParseo) { valorCelda = ""; }
                }
                if (columnasSiNo.contains(nombreColumna) && !valorCelda.isEmpty()) {
                    String valorNormalizado = CsvUtil.normalizar(valorCelda);
                    if (valorNormalizado.startsWith("SI") || valorNormalizado.startsWith("SÍ")
                            || valorNormalizado.equals("YES") || valorNormalizado.equals("1"))
                        valorCelda = "SI";
                    else if (valorNormalizado.startsWith("NO") || valorNormalizado.equals("0"))
                        valorCelda = "NO";
                }
                respuesta.put(nombreColumna, valorCelda);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Lecturas tipadas de columnas (atajos que evitan repetir parseos):
    //   leerColumnaTexto()    -> valores crudos tal cual quedaron limpios.
    //   leerColumnaNumerica() -> solo lo convertible a número (acepta coma
    //                            decimal); lo demás se salta en silencio.
    //   leerColumnaLikert()   -> etiquetas ("DE_ACUERDO") traducidas a 1..4;
    //                            "NO_SABE"/vacío se descartan (NaN) para no
    //                            sesgar las medias.
    // ------------------------------------------------------------------------
    static List<String> leerColumnaTexto(CsvUtil.Tabla tabla, String nombreColumna) {
        List<String> valoresTexto = new ArrayList<>();
        for (Map<String, String> respuesta : tabla.filas)
            valoresTexto.add(respuesta.getOrDefault(nombreColumna, ""));
        return valoresTexto;
    }

    static List<Double> leerColumnaNumerica(CsvUtil.Tabla tabla, String nombreColumna) {
        List<Double> valoresNumericos = new ArrayList<>();
        for (Map<String, String> respuesta : tabla.filas) {
            String textoNumero = respuesta.getOrDefault(nombreColumna, "").replace(",", ".");
            if (textoNumero.isEmpty()) continue;
            try { valoresNumericos.add(Double.parseDouble(textoNumero)); }
            catch (NumberFormatException valorNoNumerico) {}
        }
        return valoresNumericos;
    }

    static List<Double> leerColumnaLikert(CsvUtil.Tabla tabla, String nombreColumna) {
        List<Double> puntajesLikert = new ArrayList<>();
        for (Map<String, String> respuesta : tabla.filas) {
            double puntaje = StatsUtil.likert4(respuesta.getOrDefault(nombreColumna, ""));
            if (!Double.isNaN(puntaje)) puntajesLikert.add(puntaje);
        }
        return puntajesLikert;
    }

    // guardarReporte(): escribe un reporte de texto en la carpeta de salida.
    static void guardarReporte(String nombreArchivo, String contenidoReporte) throws IOException {
        Files.writeString(carpetaSalida.resolve(nombreArchivo), contenidoReporte, StandardCharsets.UTF_8);
        System.out.println("  -> " + nombreArchivo);
    }

    // encabezadoReporte(): cabecera común con fecha y tamaños de muestra.
    static String encabezadoReporte(String tituloReporte) {
        return "=".repeat(70) + "\n" + tituloReporte
                + "\nFecha: " + LocalDate.now() + " | N_titulados=" + titulados.filas.size()
                + " N_empleadores=" + empleadores.filas.size() + "\n" + "=".repeat(70) + "\n";
    }

    // ------------------------------------------------------------------------
    // GUÍAS DE LECTURA: cada reporte empieza con un recuadro "CÓMO LEER" en
    // lenguaje simple, más una línea "-> Lectura:" tras cada resultado.
    // Idea: cualquiera (sin estadística) entiende qué significa cada número.
    // ------------------------------------------------------------------------

    // guiaLecturaLikert(): leyenda texto->número + reglas para leer n, media,
    // mediana, desv e IC95 de un vistazo.
    static String guiaLecturaLikert() {
        return "------------------------------------------------------------\n"
                + "CÓMO LEER ESTE REPORTE (30 segundos)\n"
                + "------------------------------------------------------------\n"
                + "Las opiniones vienen como TEXTO y el programa las traduce a\n"
                + "NÚMEROS para poder promediarlas. La traducción es esta:\n"
                + "  1 = TOTALMENTE EN DESACUERDO / INSATISFECHO / NO IMPORTANTE\n"
                + "  2 = EN DESACUERDO / ALGO SATISFECHO / POCO IMPORTANTE\n"
                + "  3 = DE ACUERDO / SATISFECHO / IMPORTANTE\n"
                + "  4 = TOTALMENTE DE ACUERDO / MUY IMPORTANTE\n"
                + "OJO: 'satisfaccion_negocio' solo llega hasta 3 (su encuesta\n"
                + "no tiene 'muy satisfecho'). 'NO_SABE' y vacíos NO se cuentan.\n"
                + "  n      = cuántos respondieron (n chico = poco confiable).\n"
                + "  media  = promedio. 1-1.5 rechazo | 1.5-2.5 desacuerdo |\n"
                + "           2.5-3.0 tibio | 3.0-3.5 de acuerdo | 3.5-4 total.\n"
                + "  mediana= respuesta del medio (no la mueven los extremos).\n"
                + "  desv   = dispersión: <0.5 todos igual | 0.5-1.0 moderada |\n"
                + "           >1.0 divididos.\n"
                + "  IC95%  = rango del valor REAL (95% confianza). Ancho <0.5\n"
                + "           preciso | 0.5-1.0 bueno | 1.0-2.0 regular | >2.0 malo.\n"
                + "------------------------------------------------------------\n";
    }

    // maximoEscalaLikert(): techo de cada pregunta (casi todas 1-4;
    // satisfaccion_negocio solo tiene 3 opciones en la encuesta).
    static int maximoEscalaLikert(String nombreColumna) {
        if (nombreColumna.equals("satisfaccion_negocio")) return 3;
        return 4;
    }

    // etiquetaMediaLikert(): traduce el promedio a palabras ("tibio",
    // "de acuerdo"...) según en qué zona de la escala 1-max cayó.
    static String etiquetaMediaLikert(double mediaObservada, int maximoEscala) {
        if (Double.isNaN(mediaObservada)) return "sin dato";
        if (mediaObservada < 1.5) return "rechazo fuerte (zona del 1)";
        if (mediaObservada < 2.5) return "desacuerdo (zona del 2)";
        if (mediaObservada < 3.0) return "tibio, entre desacuerdo y acuerdo";
        if (mediaObservada < 3.5) return "de acuerdo / satisfecho (zona del 3)";
        return "acuerdo total (zona del " + maximoEscala + ")";
    }

    // etiquetaPrecision(): cuán creíble es la estimación según el ANCHO del IC.
    // Los umbrales dependen de la escala: 0.4 puntos Likert es mucho, pero
    // 0.4 años es poquísimo. Por eso se distingue esEscalaLikert (1-4) de
    // años (edad, antigüedad, desempleo).
    static String etiquetaPrecision(double limiteInferior, double limiteSuperior,
            boolean esEscalaLikert) {
        if (Double.isNaN(limiteInferior) || Double.isNaN(limiteSuperior))
            return "sin dato";
        double anchoIntervalo = limiteSuperior - limiteInferior;
        if (esEscalaLikert) {
            if (anchoIntervalo < 0.5) return "estimación muy precisa";
            if (anchoIntervalo < 1.0) return "estimación buena";
            if (anchoIntervalo < 2.0) return "estimación regular (muestra chica o dividida)";
            return "no concluyente (muestra muy chica)";
        }
        if (anchoIntervalo < 1.0) return "estimación muy precisa";
        if (anchoIntervalo < 4.0) return "estimación buena";
        if (anchoIntervalo < 8.0) return "estimación regular (muestra chica o dividida)";
        return "no concluyente (muestra muy chica)";
    }

    // etiquetaConsenso(): cuán de acuerdo están entre sí (por la desv).
    static String etiquetaConsenso(double desviacionEstandar) {
        if (Double.isNaN(desviacionEstandar)) return "sin dato";
        if (desviacionEstandar < 0.5) return "todos piensan casi igual";
        if (desviacionEstandar <= 1.0) return "opiniones moderadamente dispersas";
        return "opiniones muy divididas";
    }

    // guiaLecturaChiCuadrada(): cómo leer p y chi2 sin saber estadística.
    static String guiaLecturaChiCuadrada() {
        return "------------------------------------------------------------\n"
                + "CÓMO LEER: cada tabla cruza 2 preguntas (ej. área VS beca).\n"
                + "chi2 mide qué tan lejos está lo visto de 'no hay relación'.\n"
                + "p = probabilidad de ver esto por puro azar:\n"
                + "  p < 0.05  -> SÍ hay relación (una condiciona a la otra).\n"
                + "  p >= 0.05 -> NO hay evidencia de relación.\n"
                + "gl = tamaño de la tabla. 'Consistencia OK' = totales cuadran.\n"
                + "------------------------------------------------------------\n";
    }

    // fraseDependencia(): veredicto en una frase con los nombres de las
    // variables involucradas (evita releer el número p).
    static String fraseDependencia(String nombreColumnaA, String nombreColumnaB,
            double pValor) {
        if (Double.isNaN(pValor)) return "En palabras simples: no se pudo calcular.";
        if (pValor < 0.05)
            return "En palabras simples: '" + nombreColumnaA + "' SÍ está relacionada con '"
                    + nombreColumnaB + "'.";
        return "En palabras simples: '" + nombreColumnaA + "' NO muestra relación con '"
                + nombreColumnaB + "' en esta muestra.";
    }

    // guiaLecturaIntervalos(): el IC como frase ("entre X y Y") + anchos.
    static String guiaLecturaIntervalos() {
        return "------------------------------------------------------------\n"
                + "CÓMO LEER: el IC95% es el rango donde está el valor REAL con\n"
                + "95% de confianza. Ej: inserción 91.8% IC [84.9, 98.7] =\n"
                + "'la inserción real está entre 85% y 99%'.\n"
                + "Ancho en puntos %: <5 preciso | 5-15 bueno | 15-30 regular.\n"
                + "Ancho en puntos Likert: <0.5 preciso | 0.5-1.0 bueno | >1.0 regular.\n"
                + "------------------------------------------------------------\n";
    }

    // guiaLecturaBrechas(): brecha = lo que FALTA para llegar al ideal.
    static String guiaLecturaBrechas() {
        return "------------------------------------------------------------\n"
                + "CÓMO LEER: brecha = lo que FALTA para llegar al ideal.\n"
                + " (a) techo 4 - media: 0 = perfecto; cerca de 3 = grave.\n"
                + "     'CRITICA' = está entre las 5 peores.\n"
                + " (b) positivo = el empleador ve mejor desempeño del que el\n"
                + "     titulado siente con su formación.\n"
                + " (c) menciones = veces que aparece la palabra en texto libre.\n"
                + " (d) importancia - suficiencia: 1.5 o más = brecha crítica.\n"
                + "------------------------------------------------------------\n";
    }

    // guiaLecturaSegmentos(): quién es quién en Junior/Intermedio/Consolidado.
    static String guiaLecturaSegmentos() {
        return "------------------------------------------------------------\n"
                + "CÓMO LEER: Junior 0-3 años (recién egresados) | Intermedio\n"
                + "4-7 | Consolidado 8+ (trayectoria larga).\n"
                + "sat_formac = satisfacción media en escala 1-4.\n"
                + "int_posg_% = % del segmento que quiere hacer posgrado.\n"
                + "tasa_desemp. = fracción de su vida profesional sin empleo\n"
                + "(0.67 = 2 de cada 3 años desempleado).\n"
                + "Al final: si el IC de la diferencia incluye al 0, los\n"
                + "segmentos NO difieren entre sí.\n"
                + "------------------------------------------------------------\n";
    }

    // guiaLecturaMonteCarlo(): qué es (y qué no es) la simulación de demanda.
    static String guiaLecturaMonteCarlo() {
        return "------------------------------------------------------------\n"
                + "CÓMO LEER: no se inventan datos. p_empirica = % real afín.\n"
                + "Se simula 1000 veces '¿cuántos vendrían?' sorteando al azar.\n"
                + "media = demanda esperada; IC95 = rango probable de inscritos.\n"
                + "n_requerido = encuestas necesarias para error de 5% o menos.\n"
                + "Sensibilidad: si al quitar 10% todo se mueve poco => ROBUSTO.\n"
                + "------------------------------------------------------------\n";
    }

    // guiaLecturaEmpleadores(): acuerdo% y escala propia del empleador.
    static String guiaLecturaEmpleadores() {
        return "------------------------------------------------------------\n"
                + "CÓMO LEER: acuerdo% = % que marcó 3 (parcialmente) o 4.\n"
                + "Escala del empleador: 1 = totalmente en desacuerdo,\n"
                + "3 = parcialmente de acuerdo, 4 = totalmente de acuerdo\n"
                + "(el 2 no existe en su encuesta). NO_SABE se excluye.\n"
                + "Cobertura: mínimo 3 empresas, una pública y una privada.\n"
                + "------------------------------------------------------------\n";
    }

    // ------------------------------------------------------------------------
    // moduloUnivariado (§4.2 + HU1/HU2): describe UNA variable a la vez.
    //   Numéricas (edad, antigüedad, desempleo): media/mediana/desv/mín/
    //     cuartiles/máx + histograma textual de 10 intervalos.
    //   Likert: media/mediana/desv + IC95 (t) — evidencia para ARCUSUR.
    //   Categóricas: frecuencia absoluta + relativa, moda y barra textual.
    //   Múltiples (separador |): conteo por opción sobre quienes responden.
    // ------------------------------------------------------------------------
    static String moduloUnivariado() {
        StringBuilder reporte = new StringBuilder(encabezadoReporte("REPORTE UNIVARIADO"));
        reporte.append(guiaLecturaLikert());
        describirVariablesNumericas(reporte,
                new String[]{"edad", COL_ANIOS_VIDA_PROFESIONAL, COL_ANIOS_DESEMPLEO});
        describirVariablesLikert(reporte, new String[]{"opinion_posgrado_fcyt",
                COL_SATISFACCION_FORMACION, "concordancia_formacion_requerimientos",
                "satisfaccion_negocio", "importancia_formacion_negocio"});
        describirVariablesCategoricas(reporte, new String[]{"genero",
                "realizo_formacion_complementaria", COL_NIVEL_POSGRADO, "institucion_formacion",
                COL_FINANCIAMIENTO, COL_INTERES_POSGRADO, COL_AREA_INTERES,
                "institucion_futura_posgrado", COL_SITUACION_LABORAL, "tiempo_primer_empleo",
                "num_empleos_desde_titulacion", "red_contactos"});
        describirPreguntasMultiples(reporte, new String[]{"aspectos_utiles", "aspectos_mejorar",
                "asignaturas_ventajosas", "asignaturas_inutiles", "medio_convocatoria",
                "cargos_desempenados"});
        return reporte.toString();
    }

    // describirVariablesNumericas(): resumen de 8 estadísticos + histograma
    // con barras de '#' proporcionales al intervalo más lleno.
    static void describirVariablesNumericas(StringBuilder reporte, String[] nombresColumnas) {
        reporte.append("\n--- VARIABLES NUMERICAS (titulados) ---\n");
        for (String nombreColumna : nombresColumnas) {
            List<Double> valoresObservados = leerColumnaNumerica(titulados, nombreColumna);
            reporte.append("\n[").append(nombreColumna).append("] n=").append(valoresObservados.size()).append("\n");
            if (valoresObservados.isEmpty()) { reporte.append("  Sin datos.\n"); continue; }
            double[] cuartiles = StatsUtil.cuartiles(valoresObservados);
            reporte.append(String.format(Locale.US,
                    "  media=%s mediana=%s desv=%s min=%s Q1=%s Q2=%s Q3=%s max=%s\n",
                    StatsUtil.f2(StatsUtil.media(valoresObservados)),
                    StatsUtil.f2(StatsUtil.mediana(valoresObservados)),
                    StatsUtil.f2(StatsUtil.desvEstandar(valoresObservados)),
                    StatsUtil.f2(StatsUtil.min(valoresObservados)),
                    StatsUtil.f2(cuartiles[0]), StatsUtil.f2(cuartiles[1]), StatsUtil.f2(cuartiles[2]),
                    StatsUtil.f2(StatsUtil.max(valoresObservados))));
            List<double[]> intervalosHistograma = StatsUtil.histograma(valoresObservados, 10);
            reporte.append("  Histograma (10 intervalos):\n");
            int conteoMaximo = 1;
            for (double[] intervalo : intervalosHistograma)
                conteoMaximo = Math.max(conteoMaximo, (int) intervalo[2]);
            for (double[] intervalo : intervalosHistograma) {
                int anchoBarra = (int) Math.round(intervalo[2] / (double) conteoMaximo * 40);
                reporte.append(String.format(Locale.US, "  [%6.1f - %6.1f) %4d %s\n",
                        intervalo[0], intervalo[1], (int) intervalo[2], "#".repeat(anchoBarra)));
            }
        }
    }

    // describirVariablesLikert(): puntaje medio + IC95 por pregunta Likert.
    static void describirVariablesLikert(StringBuilder reporte, String[] nombresColumnas) {
        reporte.append("\n--- VARIABLES LIKERT (titulados, escala 1-4; negocio 1-3/1-4) ---\n");
        for (String nombreColumna : nombresColumnas) {
            List<Double> valoresObservados = leerColumnaLikert(titulados, nombreColumna);
            if (valoresObservados.isEmpty()) {
                reporte.append("\n[").append(nombreColumna).append("] Sin datos.\n");
                continue;
            }
            double[] intervaloConfianza = StatsUtil.icMedia(valoresObservados);
            double mediaObservada = StatsUtil.media(valoresObservados);
            reporte.append(String.format(Locale.US, "\n[%s] n=%d media=%s mediana=%s desv=%s IC95%%=[%s,%s]\n",
                    nombreColumna, valoresObservados.size(),
                    StatsUtil.f2(mediaObservada),
                    StatsUtil.f2(StatsUtil.mediana(valoresObservados)),
                    StatsUtil.f2(StatsUtil.desvEstandar(valoresObservados)),
                    StatsUtil.f2(intervaloConfianza[0]), StatsUtil.f2(intervaloConfianza[1])));
            int maximoEscala = maximoEscalaLikert(nombreColumna);
            reporte.append(String.format(Locale.US,
                    "  -> Lectura: %s (%.2f en escala 1-%d); %s; %s.\n",
                    etiquetaMediaLikert(mediaObservada, maximoEscala), mediaObservada,
                    maximoEscala,
                    etiquetaConsenso(StatsUtil.desvEstandar(valoresObservados)),
                    etiquetaPrecision(intervaloConfianza[0], intervaloConfianza[1], true)));
        }
    }

    // describirVariablesCategoricas(): tabla de frecuencias ordenada de mayor
    // a menor con moda (categoría típica), % y barra textual.
    static void describirVariablesCategoricas(StringBuilder reporte, String[] nombresColumnas) {
        reporte.append("\n--- VARIABLES CATEGORICAS (titulados) ---\n");
        for (String nombreColumna : nombresColumnas) {
            if (!titulados.headers.contains(nombreColumna)) continue;
            List<String> respuestas = leerColumnaTexto(titulados, nombreColumna);
            Map<String, Integer> frecuenciaPorCategoria = StatsUtil.frecuencias(respuestas);
            int totalRespuestas = respuestas.size();
            reporte.append("\n[").append(nombreColumna).append("] moda=")
                    .append(StatsUtil.moda(frecuenciaPorCategoria)).append("\n");
            List<Map.Entry<String, Integer>> categoriasOrdenadas =
                    new ArrayList<>(frecuenciaPorCategoria.entrySet());
            categoriasOrdenadas.sort((categoriaA, categoriaB) ->
                    categoriaB.getValue() - categoriaA.getValue());
            for (Map.Entry<String, Integer> categoriaConteo : categoriasOrdenadas) {
                double porcentajeCategoria = totalRespuestas == 0 ? 0
                        : 100.0 * categoriaConteo.getValue() / totalRespuestas;
                reporte.append(String.format(Locale.US, "  %-28s %4d %5.1f%% %s\n",
                        categoriaConteo.getKey().isEmpty() ? "(VACIO)" : categoriaConteo.getKey(),
                        categoriaConteo.getValue(), porcentajeCategoria,
                        StatsUtil.barra(porcentajeCategoria, 30)));
            }
        }
    }

    // describirPreguntasMultiples(): las celdas con "A|B|C" se parten por "|"
    // y cada opción se cuenta por separado (% sobre quienes respondieron,
    // no sobre la muestra total, porque una persona marca varias).
    static void describirPreguntasMultiples(StringBuilder reporte, String[] nombresColumnas) {
        reporte.append("\n--- PREGUNTAS MULTIPLES (conteo por opcion, separador |) ---\n");
        for (String nombreColumna : nombresColumnas) {
            if (!titulados.headers.contains(nombreColumna)
                    && !empleadores.headers.contains(nombreColumna)) continue;
            List<String> respuestasMultiples = titulados.headers.contains(nombreColumna)
                    ? leerColumnaTexto(titulados, nombreColumna)
                    : leerColumnaTexto(empleadores, nombreColumna);
            Map<String, Integer> conteoPorOpcion = new LinkedHashMap<>();
            int personasQueRespondieron = 0;
            for (String respuestaMultiple : respuestasMultiples) {
                if (respuestaMultiple == null || respuestaMultiple.isEmpty()) continue;
                personasQueRespondieron++;
                for (String opcionElegida : respuestaMultiple.split("\\|")) {
                    opcionElegida = opcionElegida.trim();
                    if (opcionElegida.isEmpty()) continue;
                    conteoPorOpcion.put(opcionElegida,
                            conteoPorOpcion.getOrDefault(opcionElegida, 0) + 1);
                }
            }
            reporte.append("\n[").append(nombreColumna).append("] respondieron=")
                    .append(personasQueRespondieron).append("\n");
            List<Map.Entry<String, Integer>> opcionesOrdenadas =
                    new ArrayList<>(conteoPorOpcion.entrySet());
            opcionesOrdenadas.sort((opcionA, opcionB) -> opcionB.getValue() - opcionA.getValue());
            for (Map.Entry<String, Integer> opcionConteo : opcionesOrdenadas)
                reporte.append(String.format(Locale.US, "  %-30s %4d (%5.1f%% de quienes responden)\n",
                        opcionConteo.getKey(), opcionConteo.getValue(),
                        personasQueRespondieron == 0 ? 0
                                : 100.0 * opcionConteo.getValue() / personasQueRespondieron));
        }
    }

    // ------------------------------------------------------------------------
    // moduloBivariado (§4.3 + HU1/HU2/HU3): cruza pares de variables clave,
    // imprime la matriz observada con totales y aplica χ² de independencia.
    // Incluye el cruce HU3 "financiamiento × nivel de posgrado" exigido por
    // la guía (¿la fuente de pago depende del nivel elegido?).
    // Chequeo de validación §5: el total de la tabla debe cuadrar con n.
    // ------------------------------------------------------------------------
    static String moduloBivariado() {
        StringBuilder reporte = new StringBuilder(
                encabezadoReporte("REPORTE BIVARIADO (contingencia + chi-cuadrada)"));
        reporte.append(guiaLecturaChiCuadrada());
        // Cada cruce: {columna A, columna B, etiqueta}. "segmento_antiguedad"
        // es una columna DERIVADA (ver resolverColumna), no viene del CSV.
        String[][] crucesAAnalizar = {
                {COL_AREA_INTERES, COL_FINANCIAMIENTO, "HU2: area x financiamiento"},
                {COL_NIVEL_POSGRADO, "institucion_formacion", "HU2/HU3: nivel x institucion"},
                {COL_FINANCIAMIENTO, COL_NIVEL_POSGRADO, "HU3: financiamiento x nivel posgrado"},
                {COL_SITUACION_LABORAL, COL_SATISFACCION_FORMACION, "HU1: situacion x satisfaccion"},
                {"segmento_antiguedad", COL_INTERES_POSGRADO, "HU1: antiguedad x intencion posgrado"},
        };
        for (String[] cruceActual : crucesAAnalizar) {
            anexarTablaContingencia(reporte, cruceActual[2], cruceActual[0], cruceActual[1],
                    resolverColumna(cruceActual[0]), resolverColumna(cruceActual[1]));
        }
        return reporte.toString();
    }

    // anexarTablaContingencia(): imprime UN cruce completo — matriz observada
    // con totales marginales, estadístico χ², grados de libertad, p-valor y su
    // lectura (p<0.05 = dependencia). Se descartan los registros donde AMBAS
    // celdas están vacías (no aportan información al cruce).
    static void anexarTablaContingencia(StringBuilder reporte, String etiquetaCruce,
            String nombreColumnaA, String nombreColumnaB,
            List<String> valoresColumnaA, List<String> valoresColumnaB) {
        List<String> respuestasFiltradasA = new ArrayList<>();
        List<String> respuestasFiltradasB = new ArrayList<>();
        for (int indiceRegistro = 0; indiceRegistro < valoresColumnaA.size(); indiceRegistro++) {
            if (valoresColumnaA.get(indiceRegistro).isEmpty()
                    && valoresColumnaB.get(indiceRegistro).isEmpty()) continue;
            respuestasFiltradasA.add(valoresColumnaA.get(indiceRegistro));
            respuestasFiltradasB.add(valoresColumnaB.get(indiceRegistro));
        }
        reporte.append("\n### ").append(etiquetaCruce).append(" [").append(nombreColumnaA)
                .append(" x ").append(nombreColumnaB).append("] n=")
                .append(respuestasFiltradasA.size()).append("\n");
        if (respuestasFiltradasA.isEmpty()) { reporte.append("Sin datos.\n"); return; }
        StatsUtil.TablaContingencia pruebaChiCuadrada =
                StatsUtil.contingencia(respuestasFiltradasA, respuestasFiltradasB);
        reporte.append(String.format("%-24s", ""));
        for (String nombreCategoriaColumna : pruebaChiCuadrada.columnas)
            reporte.append(String.format("%-12s", abreviarTexto(nombreCategoriaColumna, 11)));
        reporte.append("TOTAL\n");
        int[] totalesPorColumna = new int[pruebaChiCuadrada.columnas.size()];
        int totalGeneral = 0;
        for (int indiceFila = 0; indiceFila < pruebaChiCuadrada.filas.size(); indiceFila++) {
            reporte.append(String.format("%-24s",
                    abreviarTexto(pruebaChiCuadrada.filas.get(indiceFila), 23)));
            int totalFila = 0;
            for (int indiceColumna = 0;
                    indiceColumna < pruebaChiCuadrada.columnas.size(); indiceColumna++) {
                reporte.append(String.format("%-12d",
                        pruebaChiCuadrada.obs[indiceFila][indiceColumna]));
                totalFila += pruebaChiCuadrada.obs[indiceFila][indiceColumna];
                totalesPorColumna[indiceColumna] += pruebaChiCuadrada.obs[indiceFila][indiceColumna];
            }
            totalGeneral += totalFila;
            reporte.append(totalFila).append("\n");
        }
        reporte.append(String.format("%-24s", "TOTAL"));
        for (int totalColumna : totalesPorColumna)
            reporte.append(String.format("%-12d", totalColumna));
        reporte.append(totalGeneral).append("\n");
        reporte.append(String.format(Locale.US, "chi2=%.3f gl=%d p=%.4f => %s\n",
                pruebaChiCuadrada.chi2, pruebaChiCuadrada.gl, pruebaChiCuadrada.pValor,
                Double.isNaN(pruebaChiCuadrada.pValor) ? "no calculable"
                        : (pruebaChiCuadrada.pValor < 0.05
                                ? "DEPENDENCIA significativa (rechaza independencia)"
                                : "INDEPENDIENTES (no hay dependencia significativa)")));
        // consistencia interna
        reporte.append("Consistencia: total tabla=" + totalGeneral
                + (totalGeneral == respuestasFiltradasA.size() ? " OK" : " DESCUADRE") + "\n");
        reporte.append(fraseDependencia(nombreColumnaA, nombreColumnaB,
                pruebaChiCuadrada.pValor) + "\n");
    }

    // clasificarPorAntiguedad(): regla de negocio HU1 — madurez profesional:
    // Junior 0-3 años (demanda especialización inicial), Intermedio 4-7,
    // Consolidado 8+ (mandos). Puntos de corte fijados por la guía.
    static String clasificarPorAntiguedad(double aniosVidaProfesional) {
        if (Double.isNaN(aniosVidaProfesional)) return "";
        if (aniosVidaProfesional <= 3) return "JUNIOR_0_3";
        if (aniosVidaProfesional <= 7) return "INTERMEDIO_4_7";
        return "CONSOLIDADO_8_MAS";
    }

    // resolverColumna(): obtiene una columna por nombre, incluyendo la
    // DERIVADA "segmento_antiguedad" (no existe en el CSV: se calcula de
    // anios_vida_profesional). También busca en empleadores para cruces
    // mixtos. Vacío "" = pregunta no aplicable a ese registro.
    static List<String> resolverColumna(String nombreColumna) {
        if (nombreColumna.equals("segmento_antiguedad")) {
            List<String> segmentosCalculados = new ArrayList<>();
            for (Map<String, String> respuesta : titulados.filas) {
                try {
                    segmentosCalculados.add(clasificarPorAntiguedad(Double.parseDouble(
                            respuesta.getOrDefault(COL_ANIOS_VIDA_PROFESIONAL, "")
                                    .replace(",", "."))));
                } catch (Exception errorParseo) { segmentosCalculados.add(""); }
            }
            return segmentosCalculados;
        }
        if (titulados.headers.contains(nombreColumna)) return leerColumnaTexto(titulados, nombreColumna);
        if (empleadores.headers.contains(nombreColumna)) return leerColumnaTexto(empleadores, nombreColumna);
        return new ArrayList<>();
    }

    // abreviarTexto(): recorta etiquetas largas para que las tablas quepan
    // en 80 columnas, marcando el corte con "…".
    static String abreviarTexto(String textoOriginal, int longitudMaxima) {
        if (textoOriginal == null) return "";
        return textoOriginal.length() <= longitudMaxima ? textoOriginal
                : textoOriginal.substring(0, longitudMaxima - 1) + "…";
    }

    // ------------------------------------------------------------------------
    // moduloIntervalos (§4.4): cuantifica la INCERTIDUMBRE de los indicadores.
    //   Medias (t Student, ideal con muestras chicas) y proporciones (Z:
    //   inserción, intención de posgrado, requiere beca). Intervalo estrecho
    //   = estimación precisa; si el IC de "requiere beca" supera el 50%,
    //   urge el convenio con la banca (cap.6 del informe).
    // ------------------------------------------------------------------------
    static String moduloIntervalos() {
        StringBuilder reporte = new StringBuilder(
                encabezadoReporte("REPORTE INTERVALOS DE CONFIANZA 95%"));
        reporte.append(guiaLecturaIntervalos());
        reporte.append("\n--- IC para MEDIAS (t Student: x̄ ± t·s/√n) ---\n");
        for (String nombreColumna : new String[]{"edad", COL_ANIOS_VIDA_PROFESIONAL,
                COL_ANIOS_DESEMPLEO}) {
            List<Double> valoresObservados = leerColumnaNumerica(titulados, nombreColumna);
            if (valoresObservados.isEmpty()) {
                reporte.append(nombreColumna).append(": sin datos\n");
                continue;
            }
            double[] intervaloConfianza = StatsUtil.icMedia(valoresObservados);
            reporte.append(String.format(Locale.US, "%-24s n=%d media=%s IC95=[%s , %s]\n",
                    nombreColumna, valoresObservados.size(),
                    StatsUtil.f2(StatsUtil.media(valoresObservados)),
                    StatsUtil.f2(intervaloConfianza[0]), StatsUtil.f2(intervaloConfianza[1])));
            reporte.append(String.format(Locale.US,
                    "  -> La media real está entre %s y %s años; %s.\n",
                    StatsUtil.f2(intervaloConfianza[0]), StatsUtil.f2(intervaloConfianza[1]),
                    etiquetaPrecision(intervaloConfianza[0], intervaloConfianza[1], false)));
        }
        for (String nombreColumna : new String[]{"opinion_posgrado_fcyt",
                COL_SATISFACCION_FORMACION, "concordancia_formacion_requerimientos"}) {
            List<Double> valoresObservados = leerColumnaLikert(titulados, nombreColumna);
            if (valoresObservados.isEmpty()) {
                reporte.append(nombreColumna).append(": sin datos\n");
                continue;
            }
            double[] intervaloConfianza = StatsUtil.icMedia(valoresObservados);
            reporte.append(String.format(Locale.US, "%-24s n=%d media=%s IC95=[%s , %s]\n",
                    nombreColumna, valoresObservados.size(),
                    StatsUtil.f2(StatsUtil.media(valoresObservados)),
                    StatsUtil.f2(intervaloConfianza[0]), StatsUtil.f2(intervaloConfianza[1])));
            reporte.append(String.format(Locale.US,
                    "  -> La media real está entre %s y %s puntos (escala 1-%d); %s.\n",
                    StatsUtil.f2(intervaloConfianza[0]), StatsUtil.f2(intervaloConfianza[1]),
                    maximoEscalaLikert(nombreColumna),
                    etiquetaPrecision(intervaloConfianza[0], intervaloConfianza[1], true)));
        }
        reporte.append("\n--- IC para PROPORCIONES (Z: p̂ ± 1.96·√(p̂(1-p̂)/n)) ---\n");
        int totalTitulados = titulados.filas.size();
        double tasaInsercion = calcularProporcion(titulados, COL_SITUACION_LABORAL,
                valorCelda -> !CsvUtil.normalizar(valorCelda).contains("NO_TRABAJA")
                        && !valorCelda.isEmpty());
        double intencionPosgrado = calcularProporcion(titulados, COL_INTERES_POSGRADO,
                valorCelda -> valorCelda.equals("SI"));
        double proporcionRequiereBeca = calcularProporcion(titulados, COL_FINANCIAMIENTO,
                valorCelda -> CsvUtil.normalizar(valorCelda).contains("BECA"));
        double[][] proporcionesAReportar = {
                {tasaInsercion, totalTitulados},
                {intencionPosgrado, totalTitulados},
                {proporcionRequiereBeca, totalTitulados}};
        String[] nombresIndicadores = {"tasa_insercion (trabaja o emprende)",
                "intencion_posgrado (SI)", "requiere_beca (BECA_TOTAL/PARCIAL)"};
        for (int indiceIndicador = 0;
                indiceIndicador < proporcionesAReportar.length; indiceIndicador++) {
            double[] intervaloConfianza = StatsUtil.icProporcion(
                    proporcionesAReportar[indiceIndicador][0],
                    (int) proporcionesAReportar[indiceIndicador][1]);
            reporte.append(String.format(Locale.US, "%-42s p̂=%5.1f%% IC95=[%5.1f%% , %5.1f%%] n=%d\n",
                    nombresIndicadores[indiceIndicador],
                    proporcionesAReportar[indiceIndicador][0] * 100,
                    intervaloConfianza[0] * 100, intervaloConfianza[1] * 100,
                    (int) proporcionesAReportar[indiceIndicador][1]));
        }
        reporte.append("\nNota HU1: el tiempo_primer_empleo es categorico; "
                + "su IC se reporta como proporcion por categoria en univariado.\n");
        // Frases en lenguaje simple: "de cada 100, entre X y Y...".
        double[] intervaloInsercion =
                StatsUtil.icProporcion(tasaInsercion, totalTitulados);
        double[] intervaloIntencion =
                StatsUtil.icProporcion(intencionPosgrado, totalTitulados);
        double[] intervaloBeca =
                StatsUtil.icProporcion(proporcionRequiereBeca, totalTitulados);
        reporte.append(String.format(Locale.US,
                "  -> De cada 100 titulados, entre %.0f y %.0f trabajan o emprenden.\n",
                intervaloInsercion[0] * 100, intervaloInsercion[1] * 100));
        reporte.append(String.format(Locale.US,
                "  -> De cada 100 titulados, entre %.0f y %.0f quieren hacer posgrado.\n",
                intervaloIntencion[0] * 100, intervaloIntencion[1] * 100));
        reporte.append(String.format(Locale.US,
                "  -> De cada 100 titulados, entre %.0f y %.0f necesitarían beca.\n",
                intervaloBeca[0] * 100, intervaloBeca[1] * 100));
        return reporte.toString();
    }

    // CondicionRespuesta: predicado "¿esta celda cuenta para el indicador?"
    // (ej. situacion distinta de NO_TRABAJA = titulado insertado).
    interface CondicionRespuesta { boolean cumple(String valorCelda); }

    // calcularProporcion(): casos favorables / casos con respuesta. Las celdas
    // vacías NO entran al denominador (no-respuesta ≠ respuesta negativa).
    static double calcularProporcion(CsvUtil.Tabla tabla, String nombreColumna,
            CondicionRespuesta condicion) {
        if (tabla.filas.isEmpty() || !tabla.headers.contains(nombreColumna)) return Double.NaN;
        int casosFavorables = 0, casosConRespuesta = 0;
        for (Map<String, String> respuesta : tabla.filas) {
            String valorCelda = respuesta.getOrDefault(nombreColumna, "");
            if (valorCelda.isEmpty()) continue;
            casosConRespuesta++;
            if (condicion.cumple(valorCelda)) casosFavorables++;
        }
        return casosConRespuesta == 0 ? Double.NaN
                : (double) casosFavorables / casosConRespuesta;
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
    //       real importancia-suficiencia con ranking + IC. Detección
    //       automática: no rompe compatibilidad con la encuesta oficial.
    // ------------------------------------------------------------------------
    static String moduloBrechas() {
        StringBuilder reporte = new StringBuilder(
                encabezadoReporte("REPORTE BRECHAS DE COMPETENCIAS"));
        reporte.append(guiaLecturaBrechas());
        reporte.append("NOTA: la encuesta de titulados NO incluye Likert hard/soft.\n");
        reporte.append("Se calcula: (a) brecha institucional empleadores = techo(4) - media item;\n");
        reporte.append("(b) brecha empleador-titulado = likert_desempeno(empleador) vs "
                + "satisfaccion_formacion(titulado);\n");
        reporte.append("(c) ranking de competencias demandadas (texto libre empleadores).\n\n");
        anexarBrechasInstitucionales(reporte);
        anexarBrechaEmpleadorTitulado(reporte);
        anexarCompetenciasMasMencionadas(reporte);
        anexarBrechasHardSoftHU4(reporte);
        return reporte.toString();
    }

    // (a) Por cada uno de los 10 Likert: brecha = 4 - media. Se ordena de
    // mayor a menor brecha y se marca "<< CRITICA" a las 5 primeras.
    static void anexarBrechasInstitucionales(StringBuilder reporte) {
        List<double[]> brechasInstitucionales = new ArrayList<>(); // {media, brecha, n}
        List<String> nombresItems = new ArrayList<>();
        for (String nombreItem : ITEMS_LIKERT_EMPLEADOR) {
            if (!empleadores.headers.contains(nombreItem)) continue;
            List<Double> puntajesLikert = leerColumnaLikert(empleadores, nombreItem);
            if (puntajesLikert.isEmpty()) continue;
            double puntajeMedio = StatsUtil.media(puntajesLikert);
            brechasInstitucionales.add(new double[]{puntajeMedio, 4.0 - puntajeMedio,
                    puntajesLikert.size()});
            nombresItems.add(nombreItem);
        }
        Integer[] indicesOrdenados = new Integer[brechasInstitucionales.size()];
        for (int indiceItem = 0; indiceItem < indicesOrdenados.length; indiceItem++)
            indicesOrdenados[indiceItem] = indiceItem;
        Arrays.sort(indicesOrdenados, (indiceA, indiceB) -> Double.compare(
                brechasInstitucionales.get(indiceB)[1], brechasInstitucionales.get(indiceA)[1]));
        reporte.append("--- (a) Ranking brechas institucionales (techo 4 - media) ---\n");
        int posicionRanking = 1;
        for (int indiceItem : indicesOrdenados) {
            double[] datosBrecha = brechasInstitucionales.get(indiceItem);
            double[] intervaloConfianza =
                    StatsUtil.icMedia(leerColumnaLikert(empleadores, nombresItems.get(indiceItem)));
            reporte.append(String.format(Locale.US, "%d. %-28s media=%s brecha=%s n=%d IC95=[%s,%s] %s\n",
                    posicionRanking++, nombresItems.get(indiceItem),
                    StatsUtil.f2(datosBrecha[0]), StatsUtil.f2(datosBrecha[1]),
                    (int) datosBrecha[2],
                    StatsUtil.f2(intervaloConfianza[0]), StatsUtil.f2(intervaloConfianza[1]),
                    posicionRanking <= 6 ? "<< CRITICA" : ""));
        }
        if (indicesOrdenados.length > 0) {
            double[] peorBrecha = brechasInstitucionales.get(indicesOrdenados[0]);
            reporte.append(String.format(Locale.US,
                    "-> La mayor brecha es '%s' (%.2f puntos bajo el ideal de 4).\n",
                    nombresItems.get(indicesOrdenados[0]), peorBrecha[1]));
        }
    }

    // (b) ¿Coincide lo que el empleador ve con lo que el titulado siente?
    // brecha = media_desempeño(empleador) - media_satisfacción(titulado).
    static void anexarBrechaEmpleadorTitulado(StringBuilder reporte) {
        reporte.append("\n--- (b) Brecha empleador-titulado ---\n");
        List<Double> desempenoSegunEmpleador = empleadores.headers.contains(
                "likert_formacion_desempeno")
                        ? leerColumnaLikert(empleadores, "likert_formacion_desempeno")
                        : new ArrayList<>();
        List<Double> satisfaccionDelTitulado = titulados.headers.contains(
                COL_SATISFACCION_FORMACION)
                        ? leerColumnaLikert(titulados, COL_SATISFACCION_FORMACION)
                        : new ArrayList<>();
        if (!desempenoSegunEmpleador.isEmpty() && !satisfaccionDelTitulado.isEmpty()) {
            double brechaEmpleadorTitulado = StatsUtil.media(desempenoSegunEmpleador)
                    - StatsUtil.media(satisfaccionDelTitulado);
            reporte.append(String.format(Locale.US,
                    "Media empleador (formacion->desempeno)=%s vs media titulado "
                            + "(satisfaccion)=%s => brecha=%s\n",
                    StatsUtil.f2(StatsUtil.media(desempenoSegunEmpleador)),
                    StatsUtil.f2(StatsUtil.media(satisfaccionDelTitulado)),
                    StatsUtil.f2(brechaEmpleadorTitulado)));
            reporte.append(brechaEmpleadorTitulado > 0
                    ? "Empleadores valoran mejor el desempeno de lo que el titulado valora su formacion.\n"
                    : "Titulados mas satisfechos que la valoracion del empleador.\n");
        } else {
            reporte.append("Sin datos suficientes.\n");
        }
    }

    // Palabras vacías del español/inglés que no aportan al ranking de
    // competencias (artículos, preposiciones, relleno de la pregunta).
    static final Set<String> PALABRAS_VACIAS = new HashSet<>(Arrays.asList(
            "los", "las", "una", "uno", "con", "para", "por", "que", "del", "ela", "y", "de",
            "la", "el", "en", "the", "and", "conocimiento", "area", "areas", "requiere",
            "requieren"));

    // (c) Minería simple de los 3 textos libres del empleador: se tokeniza a
    // minúsculas, se filtra ruido y se cuentan menciones (top 15).
    static void anexarCompetenciasMasMencionadas(StringBuilder reporte) {
        reporte.append("\n--- (c) Competencias mas demandadas (texto libre empleadores) ---\n");
        Map<String, Integer> conteoPalabrasClave = new LinkedHashMap<>();
        for (Map<String, String> respuestaEmpleador : empleadores.filas) {
            String textoLibreConcatenado =
                    respuestaEmpleador.getOrDefault("habilidades_competencias_requeridas", "")
                            + " " + respuestaEmpleador.getOrDefault(
                                    "areas_conocimiento_emergentes", "")
                            + " " + respuestaEmpleador.getOrDefault(
                                    "herramientas_tecnologicas", "");
            for (String palabraCandidata : textoLibreConcatenado.toLowerCase()
                    .replaceAll("[^a-záéíóúñ0-9+#. ]", " ").split("\\s+")) {
                palabraCandidata = palabraCandidata.trim();
                if (palabraCandidata.length() < 3
                        || PALABRAS_VACIAS.contains(palabraCandidata)) continue;
                conteoPalabrasClave.put(palabraCandidata,
                        conteoPalabrasClave.getOrDefault(palabraCandidata, 0) + 1);
            }
        }
        List<Map.Entry<String, Integer>> palabrasOrdenadas =
                new ArrayList<>(conteoPalabrasClave.entrySet());
        palabrasOrdenadas.sort((palabraA, palabraB) ->
                palabraB.getValue() - palabraA.getValue());
        for (int indicePalabra = 0;
                indicePalabra < Math.min(15, palabrasOrdenadas.size()); indicePalabra++)
            reporte.append(String.format("  %-18s %d menciones\n",
                    palabrasOrdenadas.get(indicePalabra).getKey(),
                    palabrasOrdenadas.get(indicePalabra).getValue()));
        if (palabrasOrdenadas.isEmpty())
            reporte.append("  (sin texto libre para analizar)\n");
    }

    // (d) Extensión HU4: por cada par importancia_X/suficiencia_X del CSV,
    // brecha = importancia - suficiencia, con IC95 y marca "<< CRITICA"
    // si la brecha media ≥ 1.5 puntos de la escala.
    static void anexarBrechasHardSoftHU4(StringBuilder reporte) {
        reporte.append("\n--- (d) Brechas hard/soft HU4 "
                + "(solo si el CSV trae importancia_X/suficiencia_X) ---\n");
        boolean seDetectaronPares = false;
        for (String nombreColumnaImportancia : titulados.headers) {
            if (!nombreColumnaImportancia.startsWith("importancia_")) continue;
            String nombreCompetencia =
                    nombreColumnaImportancia.substring("importancia_".length());
            String nombreColumnaSuficiencia = "suficiencia_" + nombreCompetencia;
            if (!titulados.headers.contains(nombreColumnaSuficiencia)) continue;
            seDetectaronPares = true;
            List<Double> brechasPorCompetencia = new ArrayList<>();
            for (Map<String, String> respuestaTitulado : titulados.filas) {
                try {
                    double puntajeImportancia = Double.parseDouble(respuestaTitulado
                            .getOrDefault(nombreColumnaImportancia, "").replace(",", "."));
                    double puntajeSuficiencia = Double.parseDouble(respuestaTitulado
                            .getOrDefault(nombreColumnaSuficiencia, "").replace(",", "."));
                    brechasPorCompetencia.add(puntajeImportancia - puntajeSuficiencia);
                } catch (Exception valorNoNumerico) {}
            }
            if (brechasPorCompetencia.isEmpty()) {
                reporte.append("  " + nombreCompetencia + ": sin datos\n");
                continue;
            }
            double[] intervaloConfianza = StatsUtil.icMedia(brechasPorCompetencia);
            reporte.append(String.format(Locale.US,
                    "  %-24s brecha media=%s IC95=[%s,%s] n=%d%s\n",
                    nombreCompetencia, StatsUtil.f2(StatsUtil.media(brechasPorCompetencia)),
                    StatsUtil.f2(intervaloConfianza[0]), StatsUtil.f2(intervaloConfianza[1]),
                    brechasPorCompetencia.size(),
                    StatsUtil.media(brechasPorCompetencia) >= 1.5 ? " << CRITICA" : ""));
        }
        if (!seDetectaronPares)
            reporte.append("  (no detectadas; la encuesta oficial no las incluye "
                    + "— ver NOTA HU4 arriba)\n");
    }

    // ------------------------------------------------------------------------
    // moduloSegmentacion (§4.6 + HU1): parte la muestra por madurez profesional
    // (Junior 0-3 / Intermedio 4-7 / Consolidado 8+ años) y compara por grupo:
    // satisfacción media, % intención de posgrado y tasa de desempleo
    // (anios_desempleado / anios_vida_profesional). Cierra con el IC 95% para
    // la DIFERENCIA de satisfacción Junior vs Consolidado (§7.1, varianzas
    // agrupadas): si el 0 queda fuera, la brecha entre segmentos es real.
    // ------------------------------------------------------------------------
    static String moduloSegmentacion() {
        StringBuilder reporte = new StringBuilder(
                encabezadoReporte("REPORTE SEGMENTACION (antiguedad)"));
        reporte.append(guiaLecturaSegmentos());
        Map<String, List<Map<String, String>>> tituladosPorSegmento = new LinkedHashMap<>();
        tituladosPorSegmento.put("JUNIOR_0_3", new ArrayList<>());
        tituladosPorSegmento.put("INTERMEDIO_4_7", new ArrayList<>());
        tituladosPorSegmento.put("CONSOLIDADO_8_MAS", new ArrayList<>());
        for (Map<String, String> respuestaTitulado : titulados.filas) {
            try {
                double aniosVidaProfesional = Double.parseDouble(respuestaTitulado
                        .getOrDefault(COL_ANIOS_VIDA_PROFESIONAL, "").replace(",", "."));
                tituladosPorSegmento.get(clasificarPorAntiguedad(aniosVidaProfesional))
                        .add(respuestaTitulado);
            } catch (Exception antiguedadNoNumerica) {}
        }
        reporte.append(String.format("%-18s %6s %12s %12s %14s\n",
                "SEGMENTO", "n", "sat_formac", "int_posg_%", "tasa_desemp."));
        for (Map.Entry<String, List<Map<String, String>>> segmentoActual
                : tituladosPorSegmento.entrySet()) {
            List<Map<String, String>> respuestasDelSegmento = segmentoActual.getValue();
            List<Double> puntajesSatisfaccion = new ArrayList<>();
            List<Double> tasasDesempleo = new ArrayList<>();
            int interesadosEnPosgrado = 0, respuestasConIntencionDeclarada = 0;
            for (Map<String, String> respuestaTitulado : respuestasDelSegmento) {
                double puntajeSatisfaccion = StatsUtil.likert4(respuestaTitulado
                        .getOrDefault(COL_SATISFACCION_FORMACION, ""));
                if (!Double.isNaN(puntajeSatisfaccion))
                    puntajesSatisfaccion.add(puntajeSatisfaccion);
                try {
                    double aniosVidaProfesional = Double.parseDouble(respuestaTitulado
                            .getOrDefault(COL_ANIOS_VIDA_PROFESIONAL, "0").replace(",", "."));
                    double aniosDesempleado = Double.parseDouble(respuestaTitulado
                            .getOrDefault(COL_ANIOS_DESEMPLEO, "0").replace(",", "."));
                    if (aniosVidaProfesional > 0)
                        tasasDesempleo.add(aniosDesempleado / aniosVidaProfesional);
                } catch (Exception antiguedadNoNumerica) {}
                String intencionPosgrado =
                        respuestaTitulado.getOrDefault(COL_INTERES_POSGRADO, "");
                if (!intencionPosgrado.isEmpty()) {
                    respuestasConIntencionDeclarada++;
                    if (intencionPosgrado.equals("SI")) interesadosEnPosgrado++;
                }
            }
            reporte.append(String.format(Locale.US, "%-18s %6d %12s %12s %14s\n",
                    segmentoActual.getKey(), respuestasDelSegmento.size(),
                    puntajesSatisfaccion.isEmpty() ? "NA"
                            : StatsUtil.f2(StatsUtil.media(puntajesSatisfaccion)),
                    respuestasConIntencionDeclarada == 0 ? "NA" : StatsUtil.f1(
                            100.0 * interesadosEnPosgrado / respuestasConIntencionDeclarada),
                    tasasDesempleo.isEmpty() ? "NA"
                            : StatsUtil.f2(StatsUtil.media(tasasDesempleo))));
        }
        // IC diferencia de medias Junior vs Consolidado (guía §7.1).
        List<Double> satisfaccionJuniors =
                extraerSatisfaccionDelSegmento(tituladosPorSegmento.get("JUNIOR_0_3"));
        List<Double> satisfaccionConsolidados =
                extraerSatisfaccionDelSegmento(tituladosPorSegmento.get("CONSOLIDADO_8_MAS"));
        double[] intervaloDiferencia =
                StatsUtil.icDifMedias(satisfaccionJuniors, satisfaccionConsolidados);
        reporte.append(String.format(Locale.US,
                "\nIC95 diferencia satisfaccion (Junior-Consolidado): dif=%s IC=[%s , %s] "
                        + "n1=%d n2=%d => %s\n",
                StatsUtil.f2(intervaloDiferencia[0]), StatsUtil.f2(intervaloDiferencia[1]),
                StatsUtil.f2(intervaloDiferencia[2]),
                satisfaccionJuniors.size(), satisfaccionConsolidados.size(),
                (Double.isNaN(intervaloDiferencia[1]) ? "no calculable"
                        : (intervaloDiferencia[1] <= 0 && 0 <= intervaloDiferencia[2]
                                ? "sin diferencia significativa (el 0 esta dentro)"
                                : "DIFERENCIA significativa entre segmentos"))));
        boolean hayDiferenciaReal = !Double.isNaN(intervaloDiferencia[1])
                && !(intervaloDiferencia[1] <= 0 && 0 <= intervaloDiferencia[2]);
        reporte.append(hayDiferenciaReal
                ? "En palabras simples: juniors y consolidados están DISTINTO de satisfechos.\n"
                : "En palabras simples: juniors y consolidados están IGUAL de satisfechos.\n");
        return reporte.toString();
    }

    // extraerSatisfaccionDelSegmento(): puntajes Likert de satisfacción de un
    // grupo ya segmentado (insumo del IC de diferencia de medias).
    static List<Double> extraerSatisfaccionDelSegmento(
            List<Map<String, String>> respuestasDelSegmento) {
        List<Double> puntajesSatisfaccion = new ArrayList<>();
        if (respuestasDelSegmento == null) return puntajesSatisfaccion;
        for (Map<String, String> respuestaTitulado : respuestasDelSegmento) {
            double puntajeSatisfaccion = StatsUtil.likert4(
                    respuestaTitulado.getOrDefault(COL_SATISFACCION_FORMACION, ""));
            if (!Double.isNaN(puntajeSatisfaccion))
                puntajesSatisfaccion.add(puntajeSatisfaccion);
        }
        return puntajesSatisfaccion;
    }

    // ------------------------------------------------------------------------
    // EscenarioProyectado: resultado de simular UN programa (ej. "maestría en
    // IA"). proporcionEmpiricaAfin = % real afín al área; demandaMediaProyectada
    // ± desviación + IC95 = pronóstico; demandasSimuladas = las 1000 demandas
    // (materia prima del gráfico [7] del dashboard).
    // ------------------------------------------------------------------------
    static class EscenarioProyectado {
        String tituloEscenario;
        String areaTematica;
        double proporcionEmpiricaAfin;
        double demandaMediaProyectada;
        double desviacionDemanda;
        double limiteInferiorIC;
        double limiteSuperiorIC;
        List<Integer> demandasSimuladas;

        // tamanoCorridaRequerido (§7.2): cuántas encuestas harían falta para
        // estimar la proporción con error ≤5%: n = Z²·p(1-p)/E². Se usa el
        // peor caso p=0.5 si la p observada da un n menor (cota conservadora).
        double tamanoCorridaRequerido() {
            final double valorZ95 = 1.96, errorMaximo = 0.05;
            double varianzaProporcion =
                    Math.max(proporcionEmpiricaAfin * (1 - proporcionEmpiricaAfin),
                            0.25 * 0.25);
            return (valorZ95 * valorZ95 * varianzaProporcion)
                    / (errorMaximo * errorMaximo);
        }

        // demandasComoDecimales(): el histograma trabaja con doubles; aquí se
        // convierten las 1000 demandas enteras sin perder información.
        List<Double> demandasComoDecimales() {
            List<Double> demandasDecimales = new ArrayList<>();
            for (int demandaSimulada : demandasSimuladas)
                demandasDecimales.add((double) demandaSimulada);
            return demandasDecimales;
        }
    }

    static List<EscenarioProyectado> escenariosProyectados = new ArrayList<>();

    // ------------------------------------------------------------------------
    // moduloExperimentacion (§4.7 + §6 + §7.2): proyección de demanda Monte
    // Carlo LIGERO. Idea: NO se inventan datos; se parte de la distribución
    // empírica (p = afines al área / N) y se simula "¿cuántos vendrían si se
    // abre el programa?" con 1000 repeticiones de sorteo Math.random().
    // Modelo de sorteo por titulado: afín (interés SI + área) -> p=0.8
    // (entusiasmo), resto -> p·0.2 (arrastre). Reporta media/desv/IC95 de la
    // demanda + n de corrida requerido n=Z²p(1-p)/E² (§7.2) + sensibilidad.
    // ------------------------------------------------------------------------
    static String moduloExperimentacion() {
        StringBuilder reporte = new StringBuilder(encabezadoReporte(
                "REPORTE EXPERIMENTACION (Monte Carlo ligero, Math.random)"));
        reporte.append(guiaLecturaMonteCarlo());
        reporte.append("Metodo: distribucion empirica observada. "
                + "Sorteo con Math.random(), 1000 repeticiones.\n");
        reporte.append("n_corrida = Z^2·p̂(1-p̂)/E^2 con Z=1.96, E=0.05.\n\n");
        int totalTitulados = Math.max(titulados.filas.size(), 1);
        String[][] escenariosAEvaluar = {
                {"Abrir maestria en IA", "IA"},
                {"Abrir especialidad en ciberseguridad", "REDES_SEGURIDAD"},
                {"Abrir maestria en Ing. Software", "ING_SOFTWARE"},
                {"Abrir maestria en Base de Datos", "BASE_DATOS"},
                {"Abrir maestria en Robotica", "ROBOTICA"},
        };
        for (String[] escenarioActual : escenariosAEvaluar) {
            EscenarioProyectado resultadoEscenario = simularDemandaDeEscenario(
                    escenarioActual[0], escenarioActual[1], totalTitulados);
            escenariosProyectados.add(resultadoEscenario);
            reporte.append(String.format(Locale.US,
                    "[%s] area=%s p_empirica=%.3f n_requerido(5%%)=%.0f\n"
                            + "  Demanda simulada (1000 reps): media=%s desv=%s IC95=[%s , %s]\n",
                    resultadoEscenario.tituloEscenario, resultadoEscenario.areaTematica,
                    resultadoEscenario.proporcionEmpiricaAfin,
                    resultadoEscenario.tamanoCorridaRequerido(),
                    StatsUtil.f2(resultadoEscenario.demandaMediaProyectada),
                    StatsUtil.f2(resultadoEscenario.desviacionDemanda),
                    StatsUtil.f2(resultadoEscenario.limiteInferiorIC),
                    StatsUtil.f2(resultadoEscenario.limiteSuperiorIC)));
            List<double[]> intervalosDemanda = StatsUtil.histograma(
                    resultadoEscenario.demandasComoDecimales(), 10);
            for (double[] intervalo : intervalosDemanda)
                reporte.append(String.format(Locale.US, "    [%5.1f-%5.1f]: %d\n",
                        intervalo[0], intervalo[1], (int) intervalo[2]));
            reporte.append(String.format(Locale.US,
                    "  -> En palabras simples: si abres este programa, espera unos %s "
                            + "inscritos (entre %s y %s).\n",
                    StatsUtil.f1(resultadoEscenario.demandaMediaProyectada),
                    StatsUtil.f1(resultadoEscenario.limiteInferiorIC),
                    StatsUtil.f1(resultadoEscenario.limiteSuperiorIC)));
        }
        anexarAnalisisSensibilidad(reporte);
        return reporte.toString();
    }

    // simularDemandaDeEscenario(): 1000 veces sortea a cada titulado con
    // Math.random(): el afín convierte con p=0.8 y el resto con p·0.2.
    // Cada repetición produce UNA demanda; el conjunto da media/desv/IC95.
    static EscenarioProyectado simularDemandaDeEscenario(String tituloEscenario,
            String areaTematica, int totalTitulados) {
        int tituladosAfines = 0;
        for (Map<String, String> respuestaTitulado : titulados.filas)
            if (esTituladoAfinAlArea(respuestaTitulado, areaTematica)) tituladosAfines++;
        double proporcionEmpirica = (double) tituladosAfines / totalTitulados;
        final int totalRepeticiones = 1000;
        List<Double> demandasPorRepeticion = new ArrayList<>();
        for (int numeroRepeticion = 0;
                numeroRepeticion < totalRepeticiones; numeroRepeticion++) {
            int demandaSimulada = 0;
            for (Map<String, String> respuestaTitulado : titulados.filas) {
                // Heterogeneidad conservada: el afín pesa 0.8, el resto 0.2·p.
                double probabilidadConversion = esTituladoAfinAlArea(
                        respuestaTitulado, areaTematica) ? 0.8 : proporcionEmpirica * 0.2;
                if (Math.random() < probabilidadConversion) demandaSimulada++;
            }
            demandasPorRepeticion.add((double) demandaSimulada);
        }
        double[] intervaloDemanda = StatsUtil.icMedia(demandasPorRepeticion);
        EscenarioProyectado resultadoEscenario = new EscenarioProyectado();
        resultadoEscenario.tituloEscenario = tituloEscenario;
        resultadoEscenario.areaTematica = areaTematica;
        resultadoEscenario.proporcionEmpiricaAfin = proporcionEmpirica;
        resultadoEscenario.demandaMediaProyectada = StatsUtil.media(demandasPorRepeticion);
        resultadoEscenario.desviacionDemanda = StatsUtil.desvEstandar(demandasPorRepeticion);
        resultadoEscenario.limiteInferiorIC = intervaloDemanda[0];
        resultadoEscenario.limiteSuperiorIC = intervaloDemanda[1];
        resultadoEscenario.demandasSimuladas = new ArrayList<>();
        for (double demandaSimulada : demandasPorRepeticion)
            resultadoEscenario.demandasSimuladas.add((int) Math.round(demandaSimulada));
        return resultadoEscenario;
    }

    // esTituladoAfinAlArea(): ¿declaró interés SI y esa área temática?
    // (comparación normalizada para tolerar tildes/mayúsculas).
    static boolean esTituladoAfinAlArea(Map<String, String> respuestaTitulado,
            String areaTematica) {
        return respuestaTitulado.getOrDefault(COL_INTERES_POSGRADO, "").equals("SI")
                && CsvUtil.normalizar(respuestaTitulado.getOrDefault(COL_AREA_INTERES, ""))
                        .contains(CsvUtil.normalizar(areaTematica));
    }

    // anexarAnalisisSensibilidad (§5 validación): ¿qué tan estables son los
    // indicadores si se pierde el 10% de la muestra? 200 veces: submuestra
    // aleatoria del 90% (Math.random, sin reemplazo) y se re-miden inserción,
    // intención de posgrado y antigüedad media. Rango estrecho = robusto.
    static void anexarAnalisisSensibilidad(StringBuilder reporte) {
        reporte.append("\n--- Sensibilidad (quitar 10% aleatorio, 200 repeticiones) ---\n");
        double insercionBase = calcularProporcion(titulados, COL_SITUACION_LABORAL,
                valorCelda -> !CsvUtil.normalizar(valorCelda).contains("NO_TRABAJA")
                        && !valorCelda.isEmpty());
        double intencionBase = calcularProporcion(titulados, COL_INTERES_POSGRADO,
                valorCelda -> valorCelda.equals("SI"));
        List<Double> antiguedadesObservadas =
                leerColumnaNumerica(titulados, COL_ANIOS_VIDA_PROFESIONAL);
        double antiguedadBase = antiguedadesObservadas.isEmpty() ? Double.NaN
                : StatsUtil.media(antiguedadesObservadas);
        double insercionMinima = 2, insercionMaxima = -1;
        double intencionMinima = 2, intencionMaxima = -1;
        double antiguedadMinima = 1e9, antiguedadMaxima = -1e9;
        for (int numeroSubmuestra = 0; numeroSubmuestra < 200; numeroSubmuestra++) {
            List<Map<String, String>> submuestraNoventaPorciento =
                    extraerSubmuestraAleatoriaNoventaPorciento();
            CsvUtil.Tabla tablaSubmuestra = new CsvUtil.Tabla();
            tablaSubmuestra.headers = titulados.headers;
            tablaSubmuestra.filas = submuestraNoventaPorciento;
            double insercionSubmuestra = calcularProporcion(tablaSubmuestra,
                    COL_SITUACION_LABORAL, valorCelda ->
                            !CsvUtil.normalizar(valorCelda).contains("NO_TRABAJA")
                                    && !valorCelda.isEmpty());
            double intencionSubmuestra = calcularProporcion(tablaSubmuestra,
                    COL_INTERES_POSGRADO, valorCelda -> valorCelda.equals("SI"));
            List<Double> antiguedadesSubmuestra = new ArrayList<>();
            for (Map<String, String> respuestaSubmuestra : submuestraNoventaPorciento) {
                try {
                    antiguedadesSubmuestra.add(Double.parseDouble(respuestaSubmuestra
                            .getOrDefault(COL_ANIOS_VIDA_PROFESIONAL, "")
                            .replace(",", ".")));
                } catch (Exception antiguedadNoNumerica) {}
            }
            if (!Double.isNaN(insercionSubmuestra)) {
                insercionMinima = Math.min(insercionMinima, insercionSubmuestra);
                insercionMaxima = Math.max(insercionMaxima, insercionSubmuestra);
            }
            if (!Double.isNaN(intencionSubmuestra)) {
                intencionMinima = Math.min(intencionMinima, intencionSubmuestra);
                intencionMaxima = Math.max(intencionMaxima, intencionSubmuestra);
            }
            if (!antiguedadesSubmuestra.isEmpty()) {
                double antiguedadMediaSubmuestra = StatsUtil.media(antiguedadesSubmuestra);
                antiguedadMinima = Math.min(antiguedadMinima, antiguedadMediaSubmuestra);
                antiguedadMaxima = Math.max(antiguedadMaxima, antiguedadMediaSubmuestra);
            }
        }
        reporte.append(String.format(Locale.US,
                "  insercion: base=%.1f%% rango90%%=[%.1f%%, %.1f%%] %s\n"
                        + "  intencion posgrado: base=%.1f%% rango90%%=[%.1f%%, %.1f%%] %s\n"
                        + "  antiguedad media: base=%s rango90%%=[%s, %s] %s\n",
                insercionBase * 100, insercionMinima * 100, insercionMaxima * 100,
                evaluarRobustez((insercionMaxima - insercionMinima) * 100, 10),
                intencionBase * 100, intencionMinima * 100, intencionMaxima * 100,
                evaluarRobustez((intencionMaxima - intencionMinima) * 100, 10),
                StatsUtil.f2(antiguedadBase),
                StatsUtil.f2(antiguedadMinima), StatsUtil.f2(antiguedadMaxima),
                evaluarRobustez(antiguedadMaxima - antiguedadMinima, 1.5)));
    }

    // extraerSubmuestraAleatoriaNoventaPorciento(): baraja Fisher-Yates con
    // Math.random() y conserva el 90% (sin reemplazo = nadie se repite).
    static List<Map<String, String>> extraerSubmuestraAleatoriaNoventaPorciento() {
        List<Map<String, String>> copiaMezclable = new ArrayList<>(titulados.filas);
        for (int indiceMezcla = copiaMezclable.size() - 1; indiceMezcla > 0; indiceMezcla--) {
            int indiceAleatorio = (int) (Math.random() * (indiceMezcla + 1));
            Map<String, String> respuestaTemporal = copiaMezclable.get(indiceMezcla);
            copiaMezclable.set(indiceMezcla, copiaMezclable.get(indiceAleatorio));
            copiaMezclable.set(indiceAleatorio, respuestaTemporal);
        }
        int cantidadConservada =
                (int) Math.round(copiaMezclable.size() * 0.9);
        return copiaMezclable.subList(0, Math.max(1, cantidadConservada));
    }

    // evaluarRobustez(): regla de lectura de sensibilidad — si el rango
    // observado cabe en la tolerancia, el indicador es ROBUSTO.
    static String evaluarRobustez(double rangoObservado, double toleranciaAceptada) {
        if (Double.isNaN(rangoObservado)) return "(sin datos)";
        return rangoObservado <= toleranciaAceptada ? "=> ROBUSTO"
                : "=> SENSIBLE (ampliar muestra)";
    }

    // ------------------------------------------------------------------------
    // moduloEmpleadores (§4.8 + cap.4 de la guía): lado DEMANDA del mercado.
    //   Descriptivo (tipo/tamaño/sector/demanda de formación), cruces
    //   sector×nivel y tamaño×posibilidad futura (χ²), los 10 Likert
    //   institucionales en % de acuerdo (= evidencia ARCUSUR de confianza y
    //   coherencia del perfil) y chequeo de cobertura de campo (§5 guía).
    // ------------------------------------------------------------------------
    static String moduloEmpleadores() {
        StringBuilder reporte = new StringBuilder(encabezadoReporte("REPORTE EMPLEADORES"));
        reporte.append(guiaLecturaEmpleadores());
        reporte.append("\n--- Descriptivo categorico ---\n");
        for (String nombreColumna : new String[]{"tipo_organizacion", "tamano_organizacion",
                "sector_rubro", "contrato_ultimos_5_anios", "posibilidad_futura_contratacion",
                "nivel_formacion_demanda"}) {
            if (!empleadores.headers.contains(nombreColumna)) continue;
            Map<String, Integer> frecuenciaPorCategoria =
                    StatsUtil.frecuencias(leerColumnaTexto(empleadores, nombreColumna));
            reporte.append("\n[").append(nombreColumna).append("] moda=")
                    .append(StatsUtil.moda(frecuenciaPorCategoria)).append("\n");
            int totalEmpleadores = empleadores.filas.size();
            List<Map.Entry<String, Integer>> categoriasOrdenadas =
                    new ArrayList<>(frecuenciaPorCategoria.entrySet());
            categoriasOrdenadas.sort((categoriaA, categoriaB) ->
                    categoriaB.getValue() - categoriaA.getValue());
            for (Map.Entry<String, Integer> categoriaConteo : categoriasOrdenadas) {
                double porcentajeCategoria = totalEmpleadores == 0 ? 0
                        : 100.0 * categoriaConteo.getValue() / totalEmpleadores;
                reporte.append(String.format(Locale.US, "  %-28s %4d %5.1f%% %s\n",
                        abreviarTexto(categoriaConteo.getKey(), 27),
                        categoriaConteo.getValue(), porcentajeCategoria,
                        StatsUtil.barra(porcentajeCategoria, 25)));
            }
        }
        reporte.append("\n--- Cruces ---\n");
        anexarResumenCruce(reporte, "sector_rubro", "nivel_formacion_demanda");
        anexarResumenCruce(reporte, "tamano_organizacion", "posibilidad_futura_contratacion");
        reporte.append("\n--- 10 Likert institucionales (% acuerdo = 3 o 4) ---\n");
        for (String nombreColumna : ITEMS_LIKERT_EMPLEADOR) {
            if (!empleadores.headers.contains(nombreColumna)) continue;
            List<Double> puntajesLikert = leerColumnaLikert(empleadores, nombreColumna);
            if (puntajesLikert.isEmpty()) {
                reporte.append(nombreColumna).append(": sin datos\n");
                continue;
            }
            reporte.append(String.format(Locale.US, "%-28s media=%s acuerdo=%s%% n=%d %s\n",
                    nombreColumna, StatsUtil.f2(StatsUtil.media(puntajesLikert)),
                    StatsUtil.f1(StatsUtil.pctAcuerdo(puntajesLikert, 3)),
                    puntajesLikert.size(),
                    StatsUtil.barra(StatsUtil.pctAcuerdo(puntajesLikert, 3), 25)));
        }
        anexarCoberturaDeCampo(reporte);
        return reporte.toString();
    }

    // anexarResumenCruce(): versión de UNA LÍNEA del χ² (para empleadores):
    // solo χ², gl, p y veredicto, sin imprimir la matriz completa.
    static void anexarResumenCruce(StringBuilder reporte, String nombreColumnaA,
            String nombreColumnaB) {
        if (!empleadores.headers.contains(nombreColumnaA)
                || !empleadores.headers.contains(nombreColumnaB)) return;
        StatsUtil.TablaContingencia pruebaChiCuadrada = StatsUtil.contingencia(
                leerColumnaTexto(empleadores, nombreColumnaA),
                leerColumnaTexto(empleadores, nombreColumnaB));
        reporte.append(String.format(Locale.US, "\n[%s x %s] chi2=%.3f gl=%d p=%.4f => %s\n",
                nombreColumnaA, nombreColumnaB, pruebaChiCuadrada.chi2,
                pruebaChiCuadrada.gl, pruebaChiCuadrada.pValor,
                (!Double.isNaN(pruebaChiCuadrada.pValor)
                        && pruebaChiCuadrada.pValor < 0.05) ? "DEPENDENCIA"
                        : "INDEPENDIENTES"));
    }

    // anexarCoberturaDeCampo (§5 guía): mínimo 3 empresas de software/TI, al
    // menos una pública y una privada. Si no se cumple, se ordena encuestar
    // más empresas con la herramienta Registro antes del informe ARCUSUR.
    static void anexarCoberturaDeCampo(StringBuilder reporte) {
        reporte.append("\n--- Cobertura de campo (guia §5) ---\n");
        Set<String> nombresEmpresasDistintas = new LinkedHashSet<>();
        boolean hayEmpresaPublica = false, hayEmpresaPrivada = false;
        for (Map<String, String> respuestaEmpleador : empleadores.filas) {
            String nombreEmpresa =
                    respuestaEmpleador.getOrDefault("nombre_empresa", "").trim();
            if (!nombreEmpresa.isEmpty()) nombresEmpresasDistintas.add(nombreEmpresa);
            String tipoOrganizacionNormalizado = CsvUtil.normalizar(
                    respuestaEmpleador.getOrDefault("tipo_organizacion", ""));
            if (tipoOrganizacionNormalizado.contains("PUBLICO")) hayEmpresaPublica = true;
            if (tipoOrganizacionNormalizado.contains("PRIVADO")) hayEmpresaPrivada = true;
        }
        reporte.append("Empresas distintas: " + nombresEmpresasDistintas.size()
                + (nombresEmpresasDistintas.size() >= 3 ? " OK (>=3)"
                        : " INSUFICIENTE (minimo 3)") + "\n");
        reporte.append("Sector publico: " + (hayEmpresaPublica ? "SI OK" : "NO FALTA")
                + " | Sector privado: " + (hayEmpresaPrivada ? "SI OK" : "NO FALTA") + "\n");
        if (nombresEmpresasDistintas.size() < 3 || !hayEmpresaPublica || !hayEmpresaPrivada)
            reporte.append("ACCION: use Registro para encuestar mas empresas "
                    + "antes del informe ARCUSUR.\n");
    }

    // ------------------------------------------------------------------------
    // exportarArchivosCsv (§4.9 + HU5): artefactos REUTILIZABLES en Excel.
    //   matriz_contingencia.csv (área×financiamiento, cruza con HU2/HU3),
    //   histogramas.csv (datos crudos del gráfico [1] del dashboard),
    //   datos_limpios_*.csv (trazabilidad: lo que entró vs lo analizado).
    // ------------------------------------------------------------------------
    static void exportarArchivosCsv() throws IOException {
        // matriz_contingencia.csv (area x financiamiento)
        StatsUtil.TablaContingencia tablaAreaVsFinanciamiento = StatsUtil.contingencia(
                resolverColumna(COL_AREA_INTERES), resolverColumna(COL_FINANCIAMIENTO));
        List<String> encabezadosMatriz = new ArrayList<>();
        encabezadosMatriz.add("area\\financiamiento");
        encabezadosMatriz.addAll(tablaAreaVsFinanciamiento.columnas);
        encabezadosMatriz.add("TOTAL");
        List<List<String>> filasMatriz = new ArrayList<>();
        for (int indiceFila = 0;
                indiceFila < tablaAreaVsFinanciamiento.filas.size(); indiceFila++) {
            List<String> filaMatriz = new ArrayList<>();
            filaMatriz.add(tablaAreaVsFinanciamiento.filas.get(indiceFila));
            int totalFila = 0;
            for (int indiceColumna = 0;
                    indiceColumna < tablaAreaVsFinanciamiento.columnas.size();
                    indiceColumna++) {
                filaMatriz.add(String.valueOf(
                        tablaAreaVsFinanciamiento.obs[indiceFila][indiceColumna]));
                totalFila += tablaAreaVsFinanciamiento.obs[indiceFila][indiceColumna];
            }
            filaMatriz.add(String.valueOf(totalFila));
            filasMatriz.add(filaMatriz);
        }
        CsvUtil.escribir(carpetaSalida.resolve("matriz_contingencia.csv"),
                encabezadosMatriz, filasMatriz);
        // histogramas.csv (anios_vida_profesional)
        List<double[]> intervalosAntiguedad = StatsUtil.histograma(
                leerColumnaNumerica(titulados, COL_ANIOS_VIDA_PROFESIONAL), 10);
        List<List<String>> filasHistograma = new ArrayList<>();
        for (double[] intervalo : intervalosAntiguedad)
            filasHistograma.add(Arrays.asList(StatsUtil.f2(intervalo[0]),
                    StatsUtil.f2(intervalo[1]), String.valueOf((int) intervalo[2])));
        CsvUtil.escribir(carpetaSalida.resolve("histogramas.csv"),
                Arrays.asList("lim_inf", "lim_sup", "frecuencia"), filasHistograma);
        // datos_limpios (foto exacta de lo que se analizó)
        CsvUtil.escribir(carpetaSalida.resolve("datos_limpios_titulados.csv"),
                titulados.headers, convertirTablaAFilas(titulados));
        CsvUtil.escribir(carpetaSalida.resolve("datos_limpios_empleadores.csv"),
                empleadores.headers, convertirTablaAFilas(empleadores));
        System.out.println("  -> matriz_contingencia.csv, histogramas.csv, datos_limpios_*.csv");
    }

    // convertirTablaAFilas(): de Map<columna,valor> a lista ordenada según el
    // encabezado (el formato que CsvUtil.escribir espera).
    static List<List<String>> convertirTablaAFilas(CsvUtil.Tabla tabla) {
        List<List<String>> filasTexto = new ArrayList<>();
        for (Map<String, String> respuesta : tabla.filas) {
            List<String> filaTexto = new ArrayList<>();
            for (String nombreColumna : tabla.headers)
                filaTexto.add(respuesta.getOrDefault(nombreColumna, ""));
            filasTexto.add(filaTexto);
        }
        return filasTexto;
    }

    // mostrarResumenEjecutivo (§4.10): KPIs en consola — lo primero que lee
    // la Dirección de Carrera antes de abrir el dashboard.
    static void mostrarResumenEjecutivo() {
        int totalTitulados = titulados.filas.size(), totalEmpleadores = empleadores.filas.size();
        double tasaInsercion = calcularProporcion(titulados, COL_SITUACION_LABORAL,
                valorCelda -> !CsvUtil.normalizar(valorCelda).contains("NO_TRABAJA")
                        && !valorCelda.isEmpty());
        double intencionPosgrado = calcularProporcion(titulados, COL_INTERES_POSGRADO,
                valorCelda -> valorCelda.equals("SI"));
        List<Double> antiguedadesObservadas =
                leerColumnaNumerica(titulados, COL_ANIOS_VIDA_PROFESIONAL);
        System.out.println("\n========== DASHBOARD (resumen ejecutivo) ==========");
        System.out.println("Titulados N=" + totalTitulados + " | Empleadores M=" + totalEmpleadores);
        System.out.println(String.format(Locale.US,
                "Tasa insercion: %s%% | Intencion posgrado: %s%% | Antiguedad media: %s anios",
                Double.isNaN(tasaInsercion) ? "NA" : StatsUtil.f1(tasaInsercion * 100),
                Double.isNaN(intencionPosgrado) ? "NA" : StatsUtil.f1(intencionPosgrado * 100),
                antiguedadesObservadas.isEmpty() ? "NA"
                        : StatsUtil.f2(StatsUtil.media(antiguedadesObservadas))));
        System.out.println("Top hallazgos y recomendaciones: ver panel #10 del dashboard.html");
        for (String recomendacion : generarRecomendacionesAutomaticas())
            System.out.println("  - " + recomendacion);
    }

    // generarRecomendacionesAutomaticas(): reglas if-then sobre los datos
    // (panel [10] del dashboard e insumo del cap.6 del informe). Cada regla
    // cruza un umbral con su acción (ej. recursos propios >50% -> banca).
    static List<String> generarRecomendacionesAutomaticas() {
        List<String> recomendaciones = new ArrayList<>();
        Map<String, Integer> frecuenciaPorArea =
                StatsUtil.frecuencias(leerColumnaTexto(titulados, COL_AREA_INTERES));
        int totalTitulados = Math.max(1, titulados.filas.size());
        double porcentajeInteresIA =
                frecuenciaPorArea.getOrDefault("IA", 0) * 100.0 / totalTitulados;
        if (porcentajeInteresIA > 30)
            recomendaciones.add("Alta demanda en IA (" + StatsUtil.f1(porcentajeInteresIA)
                    + "%) -> abrir maestria modular en IA.");
        double proporcionRecursosPropios = calcularProporcion(titulados, COL_FINANCIAMIENTO,
                valorCelda -> CsvUtil.normalizar(valorCelda).contains("PROPIO"));
        if (!Double.isNaN(proporcionRecursosPropios) && proporcionRecursosPropios > 0.5)
            recomendaciones.add("Mas del 50% usa recursos propios -> "
                    + "convenio con banca / becas parciales.");
        List<Double> consultaAlEmpleador = empleadores.headers.contains(
                "likert_consulta_regular")
                        ? leerColumnaLikert(empleadores, "likert_consulta_regular")
                        : new ArrayList<>();
        if (!consultaAlEmpleador.isEmpty()
                && StatsUtil.pctAcuerdo(consultaAlEmpleador, 3) < 50)
            recomendaciones.add("Consulta al empleador baja -> activar observatorio permanente.");
        List<Double> satisfaccionFormacion =
                leerColumnaLikert(titulados, COL_SATISFACCION_FORMACION);
        if (!satisfaccionFormacion.isEmpty() && StatsUtil.media(satisfaccionFormacion) < 3.0)
            recomendaciones.add("Satisfaccion < 3.0 -> revisar malla curricular.");
        Map<String, Integer> frecuenciaTiempoPrimerEmpleo = StatsUtil.frecuencias(
                leerColumnaTexto(titulados, "tiempo_primer_empleo"));
        int tardanOchoMesesOMas = frecuenciaTiempoPrimerEmpleo.getOrDefault("8_12_MESES", 0)
                + frecuenciaTiempoPrimerEmpleo.getOrDefault("MAS_12_MESES", 0);
        if (tardanOchoMesesOMas * 1.0 / totalTitulados > 0.25)
            recomendaciones.add("Insercion lenta (>25% tarda 8+ meses) -> "
                    + "fortalecer bolsa de trabajo.");
        if (recomendaciones.isEmpty())
            recomendaciones.add(
                    "Muestra estable: mantener oferta actual y monitorear anualmente.");
        return recomendaciones;
    }

    // ------------------------------------------------------------------------
    // generarDashboardHtml (§4.10 + HU5): escribe dashboard.html AUTOCONTENIDO
    // (CSS + JS + datos JSON embebidos, dibujo con canvas puro, cero
    // dependencias). Contenido: 4 KPIs, 9 gráficos (histograma, barras, torta,
    // heatmap, radar, tabla χ², simulación, ranking, Likert) + panel [10] de
    // recomendaciones. Abrirlo en cualquier navegador = dashboard sin servidor.
    // ------------------------------------------------------------------------
    static void generarDashboardHtml() throws IOException {
        Map<String, Integer> frecuenciaPorArea =
                StatsUtil.frecuencias(leerColumnaTexto(titulados, COL_AREA_INTERES));
        Map<String, Integer> frecuenciaFinanciamiento =
                StatsUtil.frecuencias(leerColumnaTexto(titulados, COL_FINANCIAMIENTO));
        List<double[]> intervalosAntiguedad = StatsUtil.histograma(
                leerColumnaNumerica(titulados, COL_ANIOS_VIDA_PROFESIONAL), 10);
        StatsUtil.TablaContingencia mapaCalorAreaInstitucion = StatsUtil.contingencia(
                resolverColumna(COL_AREA_INTERES),
                resolverColumna("institucion_futura_posgrado"));
        // radar: puntaje medio de cada uno de los 10 Likert del empleador
        List<String> etiquetasRadar = new ArrayList<>();
        List<Double> valoresRadar = new ArrayList<>();
        for (String nombreItemLikert : ITEMS_LIKERT_EMPLEADOR) {
            if (!empleadores.headers.contains(nombreItemLikert)) continue;
            List<Double> puntajesLikert = leerColumnaLikert(empleadores, nombreItemLikert);
            if (puntajesLikert.isEmpty()) continue;
            etiquetasRadar.add(nombreItemLikert.replace("likert_", ""));
            valoresRadar.add(StatsUtil.media(puntajesLikert));
        }
        Map<String, Integer> asignaturasVentajosas =
                contarRespuestasMultiples("asignaturas_ventajosas");
        Map<String, Integer> asignaturasInutiles =
                contarRespuestasMultiples("asignaturas_inutiles");

        String datosJsonParaGraficos = "{"
                + "\"n\":" + titulados.filas.size() + ",\"m\":" + empleadores.filas.size() + ","
                + "\"hist\":" + jsonDeHistograma(intervalosAntiguedad) + ","
                + "\"areas\":" + jsonDeFrecuencias(frecuenciaPorArea) + ","
                + "\"fin\":" + jsonDeFrecuencias(frecuenciaFinanciamiento) + ","
                + "\"heat\":{\"filas\":" + jsonDeTextos(mapaCalorAreaInstitucion.filas)
                + ",\"cols\":" + jsonDeTextos(mapaCalorAreaInstitucion.columnas)
                + ",\"obs\":" + jsonDeMatriz(mapaCalorAreaInstitucion.obs)
                + ",\"chi2\":" + conTresDecimales(mapaCalorAreaInstitucion.chi2)
                + ",\"gl\":" + mapaCalorAreaInstitucion.gl
                + ",\"p\":" + (Double.isNaN(mapaCalorAreaInstitucion.pValor) ? "null"
                        : conTresDecimales(mapaCalorAreaInstitucion.pValor)) + "},"
                + "\"radarLab\":" + jsonDeTextos(etiquetasRadar)
                + ",\"radarVal\":" + jsonDeNumeros(valoresRadar) + ","
                + "\"sim\":" + jsonDeEnteros(escenariosProyectados.isEmpty() ? new ArrayList<>()
                        : escenariosProyectados.get(0).demandasSimuladas) + ","
                + "\"simMedia\":" + (escenariosProyectados.isEmpty() ? "0"
                        : StatsUtil.f2(escenariosProyectados.get(0).demandaMediaProyectada))
                + ","
                + "\"vent\":" + jsonDeFrecuencias(asignaturasVentajosas)
                + ",\"inut\":" + jsonDeFrecuencias(asignaturasInutiles) + ","
                + "\"likert\":" + jsonDeAcuerdoLikert()
                + "}";

        String listaRecomendacionesHtml = "";
        for (String recomendacion : generarRecomendacionesAutomaticas())
            listaRecomendacionesHtml += "<li>" + escaparHtml(recomendacion) + "</li>\n";

        String tituloPrimerEscenario = escenariosProyectados.isEmpty() ? ""
                : escaparHtml(escenariosProyectados.get(0).tituloEscenario);
        String mediaPrimerEscenario = escenariosProyectados.isEmpty() ? ""
                : StatsUtil.f2(escenariosProyectados.get(0).demandaMediaProyectada);

        String paginaDashboard = "<!DOCTYPE html><html lang='es'><head><meta charset='utf-8'>"
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
                + "<div>Fuente: titulados.csv (N=" + titulados.filas.size()
                + ") + empleadores.csv (M=" + empleadores.filas.size()
                + ") | Fecha: " + LocalDate.now() + "</div></header><div class='wrap'>"
                + "<nav><a href='#c1'>[1] Antigüedad</a><a href='#c2'>[2] Áreas</a><a href='#c3'>[3] Financiamiento</a>"
                + "<a href='#c4'>[4] Calor</a><a href='#c5'>[5] Radar</a><a href='#t6'>[6] χ²</a>"
                + "<a href='#c7'>[7] Simulación</a><a href='#c8'>[8] Asignaturas</a><a href='#c9'>[9] Empleadores</a></nav>"
                + "<div id='tip'></div>"
                + "<div class='kpis'>" + tarjetasKpiHtml() + "</div>"
                + "<div class='card'><h2>[1] Histograma — Años de vida profesional</h2><canvas id='c1'></canvas></div>"
                + "<div class='card'><h2>[2] Barras — Área de interés posgrado</h2><canvas id='c2'></canvas></div>"
                + "<div class='card'><h2>[3] Torta — Financiamiento</h2><canvas id='c3'></canvas></div>"
                + "<div class='card'><h2>[4] Mapa de calor — Área × Institución futura</h2><canvas id='c4' style='height:300px'></canvas></div>"
                + "<div class='card'><h2>[5] Radar — Likert institucional empleadores (1-4)</h2><canvas id='c5'></canvas></div>"
                + "<div class='card'><h2>[6] Contingencia Área × Financiamiento + χ²</h2><div id='t6'></div></div>"
                + "<div class='card'><h2>[7] Proyección Monte Carlo — " + tituloPrimerEscenario
                + " (media " + mediaPrimerEscenario + ")</h2><canvas id='c7'></canvas></div>"
                + "<div class='card'><h2>[8] Asignaturas ventajosas vs inútiles</h2><canvas id='c8'></canvas></div>"
                + "<div class='card'><h2>[9] Empleadores — % acuerdo por ítem Likert</h2><canvas id='c9'></canvas></div>"
                + "<div class='card'><h2>[10] Recomendaciones automáticas</h2><ul>"
                + listaRecomendacionesHtml + "</ul></div>"
                + "</div><script>var D=" + datosJsonParaGraficos + ";\n" + SCRIPT_GRAFICOS_JS
                + "</script></body></html>";

        Files.writeString(carpetaSalida.resolve("dashboard.html"), paginaDashboard,
                StandardCharsets.UTF_8);
        System.out.println("  -> dashboard.html");
    }

    // tarjetasKpiHtml(): las 4 tarjetas superiores del dashboard (antigüedad,
    // inserción, intención de posgrado, necesita beca).
    static String tarjetasKpiHtml() {
        List<Double> antiguedades =
                leerColumnaNumerica(titulados, COL_ANIOS_VIDA_PROFESIONAL);
        double tasaInsercion = calcularProporcion(titulados, COL_SITUACION_LABORAL,
                valorCelda -> !CsvUtil.normalizar(valorCelda).contains("NO_TRABAJA")
                        && !valorCelda.isEmpty());
        double intencionPosgrado = calcularProporcion(titulados, COL_INTERES_POSGRADO,
                valorCelda -> valorCelda.equals("SI"));
        double proporcionBeca = calcularProporcion(titulados, COL_FINANCIAMIENTO,
                valorCelda -> CsvUtil.normalizar(valorCelda).contains("BECA"));
        return "<div class='kpi'><b>Antigüedad media</b><div style='font-size:22px'>"
                + (antiguedades.isEmpty() ? "NA"
                        : StatsUtil.f2(StatsUtil.media(antiguedades)) + " ± "
                                + StatsUtil.f2(StatsUtil.desvEstandar(antiguedades)))
                + "</div></div><div class='kpi'><b>Tasa inserción</b><div style='font-size:22px'>"
                + (Double.isNaN(tasaInsercion) ? "NA" : StatsUtil.f1(tasaInsercion * 100) + "%")
                + "</div></div><div class='kpi'><b>Intención posgrado</b><div style='font-size:22px'>"
                + (Double.isNaN(intencionPosgrado) ? "NA"
                        : StatsUtil.f1(intencionPosgrado * 100) + "%")
                + "</div></div><div class='kpi'><b>Requiere beca</b><div style='font-size:22px'>"
                + (Double.isNaN(proporcionBeca) ? "NA"
                        : StatsUtil.f1(proporcionBeca * 100) + "%")
                + "</div></div>";
    }

    // contarRespuestasMultiples(): reutiliza la lógica "partir por |" para el
    // gráfico [8] (asignaturas ventajosas vs inútiles).
    static Map<String, Integer> contarRespuestasMultiples(String nombreColumna) {
        Map<String, Integer> conteoPorOpcion = new LinkedHashMap<>();
        if (!titulados.headers.contains(nombreColumna)) return conteoPorOpcion;
        for (String respuestaMultiple : leerColumnaTexto(titulados, nombreColumna)) {
            if (respuestaMultiple == null || respuestaMultiple.isEmpty()) continue;
            for (String opcionIndividual : respuestaMultiple.split("\\|")) {
                opcionIndividual = opcionIndividual.trim();
                if (!opcionIndividual.isEmpty())
                    conteoPorOpcion.put(opcionIndividual,
                            conteoPorOpcion.getOrDefault(opcionIndividual, 0) + 1);
            }
        }
        return conteoPorOpcion;
    }

    // ------------------------------------------------------------------------
    // moduloInforme(): PRODUCTO FINAL de la guía §2 y §4 — "Documento de
    // Análisis de Mercado" con la tabla de contenidos oficial ARCUSUR
    // (7 capítulos). No es texto fijo: cada cifra se calcula de los CSV
    // (N, medias, IC, χ², rankings), así el informe siempre refleja los datos
    // reales cargados. Cierra con referencias APA 7.ª ed. (sin URLs).
    // ------------------------------------------------------------------------
    static String moduloInforme() {
        StringBuilder reporte = new StringBuilder();
        reporte.append("ANALISIS DE MERCADO — PERFIL, FORMACION Y EMPLEABILIDAD\n");
        reporte.append("Titulados de Ingenieria en Sistemas, UMSS (insumo Plan de Desarrollo\n");
        reporte.append("Estrategico y acreditacion ARCUSUR)\n");
        reporte.append("Fecha: ").append(LocalDate.now())
                .append(" | N titulados=").append(titulados.filas.size())
                .append(" | M empleadores=").append(empleadores.filas.size()).append("\n");
        reporte.append("Guía rápida: Likert 1-4 (1=total desacuerdo, 4=total acuerdo); "
                + "p<0.05 en chi2 = hay relación; IC95% = rango del valor real.\n\n");

        List<Double> antiguedades =
                leerColumnaNumerica(titulados, COL_ANIOS_VIDA_PROFESIONAL);
        double antiguedadMedia = antiguedades.isEmpty() ? Double.NaN
                : StatsUtil.media(antiguedades);
        double antiguedadDesviacion = StatsUtil.desvEstandar(antiguedades);
        double tasaInsercion = calcularProporcion(titulados, COL_SITUACION_LABORAL,
                valorCelda -> !CsvUtil.normalizar(valorCelda).contains("NO_TRABAJA")
                        && !valorCelda.isEmpty());
        double intencionPosgrado = calcularProporcion(titulados, COL_INTERES_POSGRADO,
                valorCelda -> valorCelda.equals("SI"));
        double[] intervaloInsercion =
                StatsUtil.icProporcion(tasaInsercion, titulados.filas.size());
        Map<String, Integer> frecuenciaPorArea = StatsUtil.frecuencias(
                leerColumnaTexto(titulados, COL_AREA_INTERES));
        List<Map.Entry<String, Integer>> areasOrdenadas =
                new ArrayList<>(frecuenciaPorArea.entrySet());
        areasOrdenadas.sort((areaA, areaB) -> areaB.getValue() - areaA.getValue());
        Map<String, Integer> frecuenciaPorSector =
                empleadores.headers.contains("sector_rubro")
                        ? StatsUtil.frecuencias(
                                leerColumnaTexto(empleadores, "sector_rubro"))
                        : new LinkedHashMap<>();

        reporte.append("1. INTRODUCCION Y JUSTIFICACION\n");
        reporte.append("La Carrera de Ingenieria en Sistemas (UMSS) desarrolla su autoevaluacion\n");
        reporte.append("para el Plan de Desarrollo Estrategico y su postulacion ante ARCUSUR.\n");
        reporte.append("Objetivo general: cuantificar tendencias de formacion continua, patrones\n");
        reporte.append("demograficos y de preferencia academica de los titulados. Objetivos\n");
        reporte.append("especificos: HU1 antiguedad/empleabilidad, HU2 demanda de posgrado,\n");
        reporte.append("HU3 financiamiento, HU4 brechas de competencias, HU5 reportes y\n");
        reporte.append("dashboard. Justificacion: evidencia empirica de pertinencia curricular\n");
        reporte.append("y de gestion de alianzas, criterio exigible por pares ARCUSUR.\n\n");

        reporte.append("2. MARCO METODOLOGICO\n");
        reporte.append("Diseno cuantitativo exploratorio sobre encuestas estructuradas (titulados\n");
        reporte.append("N=").append(titulados.filas.size()).append(", empleadores M=")
                .append(empleadores.filas.size()).append(").\n");
        reporte.append("Pipeline: Data Wrangling (validacion + limpieza) -> estadistica univariada\n");
        reporte.append("(media, mediana, desv. estandar, frecuencias, histogramas) -> bivariada\n");
        reporte.append("con pruebas chi-cuadrada de independencia -> IC 95% (t Student para\n");
        reporte.append("medias, Z para proporciones, varianzas agrupadas para diferencias) ->\n");
        reporte.append("experimentacion Monte Carlo ligera (solo Math.random) -> dashboard.\n");
        reporte.append("Sin generadores congruenciales: la fuente son los datos reales.\n\n");

        reporte.append("3. DEMANDA EDUCATIVA Y PERFIL DE LOS TITULADOS\n");
        reporte.append(String.format(Locale.US,
                "Antiguedad media %.2f anios (desv. %.2f). Tasa de insercion %.1f%% IC95 [%.1f, %.1f].\n",
                antiguedadMedia, antiguedadDesviacion, tasaInsercion * 100,
                intervaloInsercion[0] * 100, intervaloInsercion[1] * 100));
        reporte.append(String.format(Locale.US, "Intencion de posgrado %.1f%%. Areas top: ",
                intencionPosgrado * 100));
        anexarTopTresCategorias(reporte, areasOrdenadas);
        reporte.append("\nSegmentacion Junior/Intermedio/Consolidado y detalle en "
                + "reporte_segmentacion.txt.\n\n");

        reporte.append("4. OFERTA LABORAL Y REQUERIMIENTOS DEL SECTOR\n");
        List<Map.Entry<String, Integer>> sectoresOrdenados =
                new ArrayList<>(frecuenciaPorSector.entrySet());
        sectoresOrdenados.sort((sectorA, sectorB) ->
                sectorB.getValue() - sectorA.getValue());
        reporte.append("Sectores top empleadores: ");
        for (int indiceSector = 0;
                indiceSector < Math.min(3, sectoresOrdenados.size()); indiceSector++)
            reporte.append(sectoresOrdenados.get(indiceSector).getKey()).append(" (")
                    .append(sectoresOrdenados.get(indiceSector).getValue()).append(") ");
        reporte.append("\nNivel de formacion mas demandado y Likert institucional en "
                + "reporte_empleadores.txt;\n");
        reporte.append("competencias emergentes del texto libre en reporte_brechas.txt.\n\n");

        reporte.append("5. DIAGNOSTICO Y SINTESIS\n");
        StatsUtil.TablaContingencia cruceAreaFinanciamiento = StatsUtil.contingencia(
                resolverColumna(COL_AREA_INTERES), resolverColumna(COL_FINANCIAMIENTO));
        reporte.append(String.format(Locale.US,
                "Cruce area x financiamiento: chi2=%.2f gl=%d p=%.4f (%s). ",
                cruceAreaFinanciamiento.chi2, cruceAreaFinanciamiento.gl,
                cruceAreaFinanciamiento.pValor,
                (cruceAreaFinanciamiento.pValor < 0.05 ? "dependencia" : "independencia")));
        StatsUtil.TablaContingencia cruceFinanciamientoNivel = StatsUtil.contingencia(
                resolverColumna(COL_FINANCIAMIENTO), resolverColumna(COL_NIVEL_POSGRADO));
        reporte.append(String.format(Locale.US,
                "Cruce financiamiento x nivel: chi2=%.2f gl=%d p=%.4f (%s).\n",
                cruceFinanciamientoNivel.chi2, cruceFinanciamientoNivel.gl,
                cruceFinanciamientoNivel.pValor,
                (cruceFinanciamientoNivel.pValor < 0.05 ? "dependencia" : "independencia")));
        reporte.append("Hallazgos y brechas criticas: ver reportes bivariado/brechas; estabilidad\n");
        reporte.append("validada por sensibilidad del 90% (reporte_experimentacion.txt).\n\n");

        reporte.append("6. CONCLUSIONES Y PROPUESTAS ESTRATEGICAS\n");
        List<String> recomendaciones = generarRecomendacionesAutomaticas();
        for (int indiceRecomendacion = 0;
                indiceRecomendacion < recomendaciones.size(); indiceRecomendacion++)
            reporte.append((indiceRecomendacion + 1) + ". "
                    + recomendaciones.get(indiceRecomendacion) + "\n");
        reporte.append("Lineas permanentes: posgrados modulares, diversificacion de becas,\n");
        reporte.append("vinculacion B2B y observatorio permanente de egresados.\n\n");

        reporte.append("7. REFERENCIAS BIBLIOGRAFICAS (APA 7)\n");
        reporte.append("Coss Bu, R. (1993). Simulacion: Un enfoque practico. Limusa.\n");
        reporte.append("Hernandez Sampieri, R., Fernandez Collado, C., y Baptista Lucio, P. (2014).\n");
        reporte.append("    Metodologia de la investigacion (6.a ed.). McGraw-Hill.\n");
        reporte.append("Walpole, R. E., Myers, R. H., Myers, S. L., y Ye, K. (2012).\n");
        reporte.append("    Probabilidad y estadistica para ingenieria y ciencias (9.a ed.). Pearson.\n");
        reporte.append("Red de Agencias Nacionales de Acreditacion. (2008). Sistema de\n");
        reporte.append("    acreditacion regional de carreras universitarias (ARCUSUR).\n");
        reporte.append("Carrera de Ingenieria en Sistemas, UMSS. Encuestas a titulados y\n");
        reporte.append("    empleadores: instrumento de autoevaluacion (documento interno).\n");
        return reporte.toString();
    }

    // anexarTopTresCategorias(): escribe "NOMBRE (conteo)" de las 3 primeras,
    // saltando "(VACIO)" porque la no-respuesta no es una categoría real.
    static void anexarTopTresCategorias(StringBuilder reporte,
            List<Map.Entry<String, Integer>> categoriasOrdenadas) {
        int categoriasMostradas = 0; // (VACIO) = sin interés declarado: se excluye del top
        for (int indiceCategoria = 0;
                indiceCategoria < categoriasOrdenadas.size() && categoriasMostradas < 3;
                indiceCategoria++) {
            if (categoriasOrdenadas.get(indiceCategoria).getKey().equals("(VACIO)")) continue;
            reporte.append(categoriasOrdenadas.get(indiceCategoria).getKey()).append(" (")
                    .append(categoriasOrdenadas.get(indiceCategoria).getValue()).append(") ");
            categoriasMostradas++;
        }
    }

    // ---- Serialización a JSON para los gráficos del dashboard ----

    // jsonDeHistograma(): [[limInf, limSup, conteo], ...] para el gráfico [1].
    static String jsonDeHistograma(List<double[]> intervalosHistograma) {
        StringBuilder fragmentoJson = new StringBuilder("[");
        for (int indiceIntervalo = 0;
                indiceIntervalo < intervalosHistograma.size(); indiceIntervalo++) {
            if (indiceIntervalo > 0) fragmentoJson.append(",");
            fragmentoJson.append("[").append(StatsUtil.f2(
                    intervalosHistograma.get(indiceIntervalo)[0])).append(",")
                    .append(StatsUtil.f2(intervalosHistograma.get(indiceIntervalo)[1]))
                    .append(",").append((int) intervalosHistograma.get(indiceIntervalo)[2])
                    .append("]");
        }
        return fragmentoJson.append("]").toString();
    }

    // jsonDeFrecuencias(): [{"k":categoria,"v":conteo}] para barras y torta.
    static String jsonDeFrecuencias(Map<String, Integer> frecuenciaPorCategoria) {
        StringBuilder fragmentoJson = new StringBuilder("[");
        boolean esPrimerElemento = true;
        for (Map.Entry<String, Integer> categoriaConteo : frecuenciaPorCategoria.entrySet()) {
            if (!esPrimerElemento) fragmentoJson.append(",");
            esPrimerElemento = false;
            fragmentoJson.append("{\"k\":").append(entreComillasJson(categoriaConteo.getKey()))
                    .append(",\"v\":").append(categoriaConteo.getValue()).append("}");
        }
        return fragmentoJson.append("]").toString();
    }

    // jsonDeTextos(): ["a","b",...] para etiquetas del heatmap y del radar.
    static String jsonDeTextos(List<String> textosOriginales) {
        StringBuilder fragmentoJson = new StringBuilder("[");
        for (int indiceTexto = 0; indiceTexto < textosOriginales.size(); indiceTexto++) {
            if (indiceTexto > 0) fragmentoJson.append(",");
            fragmentoJson.append(entreComillasJson(textosOriginales.get(indiceTexto)));
        }
        return fragmentoJson.append("]").toString();
    }

    // jsonDeNumeros(): [3.24, ...] para los valores del radar.
    static String jsonDeNumeros(List<Double> numerosOriginales) {
        StringBuilder fragmentoJson = new StringBuilder("[");
        for (int indiceNumero = 0; indiceNumero < numerosOriginales.size(); indiceNumero++) {
            if (indiceNumero > 0) fragmentoJson.append(",");
            fragmentoJson.append(StatsUtil.f2(numerosOriginales.get(indiceNumero)));
        }
        return fragmentoJson.append("]").toString();
    }

    // jsonDeEnteros(): [12, 13, ...] para las 1000 demandas simuladas.
    static String jsonDeEnteros(List<Integer> enterosOriginales) {
        StringBuilder fragmentoJson = new StringBuilder("[");
        for (int indiceEntero = 0; indiceEntero < enterosOriginales.size(); indiceEntero++) {
            if (indiceEntero > 0) fragmentoJson.append(",");
            fragmentoJson.append(enterosOriginales.get(indiceEntero));
        }
        return fragmentoJson.append("]").toString();
    }

    // jsonDeMatriz(): [[1,2],[3,4]] con los conteos del mapa de calor.
    static String jsonDeMatriz(int[][] matrizConteos) {
        StringBuilder fragmentoJson = new StringBuilder("[");
        for (int indiceFila = 0; indiceFila < matrizConteos.length; indiceFila++) {
            if (indiceFila > 0) fragmentoJson.append(",");
            fragmentoJson.append("[");
            for (int indiceColumna = 0;
                    indiceColumna < matrizConteos[indiceFila].length; indiceColumna++) {
                if (indiceColumna > 0) fragmentoJson.append(",");
                fragmentoJson.append(matrizConteos[indiceFila][indiceColumna]);
            }
            fragmentoJson.append("]");
        }
        return fragmentoJson.append("]").toString();
    }

    // jsonDeAcuerdoLikert(): % de acuerdo de cada ítem para el gráfico [9].
    static String jsonDeAcuerdoLikert() {
        StringBuilder fragmentoJson = new StringBuilder("[");
        boolean esPrimerElemento = true;
        for (String nombreItemLikert : ITEMS_LIKERT_EMPLEADOR) {
            if (!empleadores.headers.contains(nombreItemLikert)) continue;
            List<Double> puntajesLikert = leerColumnaLikert(empleadores, nombreItemLikert);
            if (puntajesLikert.isEmpty()) continue;
            if (!esPrimerElemento) fragmentoJson.append(",");
            esPrimerElemento = false;
            fragmentoJson.append("{\"k\":")
                    .append(entreComillasJson(nombreItemLikert.replace("likert_", "")))
                    .append(",\"v\":")
                    .append(StatsUtil.f1(StatsUtil.pctAcuerdo(puntajesLikert, 3))).append("}");
        }
        return fragmentoJson.append("]").toString();
    }

    // entreComillasJson(): escapa un texto para incrustarlo en JSON
    // (barras y comillas internas).
    static String entreComillasJson(String textoOriginal) {
        if (textoOriginal == null) textoOriginal = "";
        return "\"" + textoOriginal.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    // conTresDecimales(): χ² y p-valor con 3 decimales ("0" si no calculable).
    static String conTresDecimales(double valorOriginal) {
        if (Double.isNaN(valorOriginal) || Double.isInfinite(valorOriginal)) return "0";
        return String.format(Locale.US, "%.3f", valorOriginal);
    }

    // escaparHtml(): evita que una recomendación con "<" rompa el dashboard.
    static String escaparHtml(String textoOriginal) {
        return textoOriginal.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    /** SCRIPT_GRAFICOS_JS: dibujado con canvas puro (sin librerías): 9 gráficos
     *  + tooltips hover (HU5 "interactivo"). Cómo funciona bars(): dibuja a
     *  doble resolución (retina) y guarda la geometría de cada barra en
     *  c._bars; un listener mousemove traduce el cursor a coordenadas del
     *  canvas, detecta la barra bajo el puntero y muestra el div #tip con
     *  "etiqueta: valor". Sin dependencias externas. */
    static final String SCRIPT_GRAFICOS_JS =
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
