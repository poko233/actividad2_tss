package bo.umss.mercado.nucleo.guia;

// GuiasLectura: textos fijos "CÓMO LEER" que acompañan cada pantalla y
// reporte. Todo en lenguaje simple, sin jerga estadística, para que
// la Dirección de Carrera entienda cada número al primer vistazo.
public class GuiasLectura {

    public static final String COMO_LEER_CHI2 =
            "Cada tabla cruza 2 preguntas (ej. área VS beca).\n"
            + "p = probabilidad de ver esto por puro azar:\n"
            + "  p < 0.05  -> SÍ hay relación (una condiciona a la otra).\n"
            + "  p >= 0.05 -> NO hay evidencia de relación.\n";

    public static final String COMO_LEER_INTERVALOS =
            "El IC95% es el rango donde está el valor REAL con 95% de confianza.\n"
            + "Ej: inserción 91.8% IC [84.9, 98.7] = 'la inserción real está "
            + "entre 85% y 99%'.\n";

    public static final String COMO_LEER_BRECHAS =
            "Brecha = lo que FALTA para llegar al ideal.\n"
            + " (a) techo 4 - media: 0 = perfecto; cerca de 3 = grave.\n"
            + " (b) positivo = el empleador ve mejor desempeño del que el "
            + "titulado siente.\n"
            + " (d) importancia - suficiencia de 1.5 o más = brecha crítica.\n";

    public static final String COMO_LEER_SEGMENTOS =
            "Junior 0-3 años (recién egresados) | Intermedio 4-7 | "
            + "Consolidado 8+ (trayectoria larga).\n"
            + "tasa_desemp. = fracción de su vida profesional sin empleo "
            + "(0.67 = 2 de cada 3 años desempleado).\n"
            + "Si el IC de la diferencia incluye al 0, NO difieren.\n";

    public static final String COMO_LEER_MONTECARLO =
            "No se inventan datos: p_empirica = % real afín al área.\n"
            + "Se simula 1000 veces '¿cuántos vendrían?' sorteando al azar.\n"
            + "media = demanda esperada; IC95 = rango probable de inscritos.\n";

    public static final String COMO_LEER_EMPLEADORES =
            "acuerdo% = % que marcó 3 (parcialmente) o 4 (totalmente).\n"
            + "Escala: 1 = totalmente en desacuerdo, 3 = parcialmente de "
            + "acuerdo, 4 = totalmente de acuerdo. NO_SABE se excluye.\n";

    // fraseDependencia(): el veredicto del chi² en UNA frase con los
    // nombres de las variables (nadie tiene que releer el número p).
    public static String fraseDependencia(String columnaA, String columnaB,
            double valorP) {
        if (Double.isNaN(valorP)) {
            return "No se pudo calcular la relación.";
        }
        if (valorP < 0.05) {
            return "'" + columnaA + "' SÍ está relacionada con '" + columnaB + "'.";
        }
        return "'" + columnaA + "' NO muestra relación con '" + columnaB + "'.";
    }
}
