package bo.umss.mercado.nucleo.likert;

import bo.umss.mercado.nucleo.limpieza.LimpiezaDatos;
import java.util.Set;

// EscalaLikert: texto -> puntaje. Soporta DOS escalas:
//   - Titulados (4 puntos): 1..4
//   - Empleadores (3 puntos): 1..3, con "No sabe" -> NaN
// El despachador traducir(etiqueta, columna) elige la escala segun la
// columna, asi los llamadores no se equivocan.
public class EscalaLikert {

    // Columnas de empleadores con escala 1-3 (18 items de la matriz).
    private static final Set<String> COLUMNAS_3P = Set.of(
            "likert_confianza_formadora", "likert_consistencia_titulo",
            "likert_consulta_regular", "likert_conoce_perfil",
            "likert_coherencia_perfil", "likert_formacion_desempeno",
            "likert_considera_opinion", "likert_competencias_laborales",
            "likert_valores_actitudes", "likert_participacion_procesos",
            "likert_vinculos_organizaciones", "likert_participacion_vinculacion",
            "likert_difusion_oportunidades", "likert_vinculacion_profesionales",
            "likert_consultado_desempeno", "likert_consultado_pertinencia",
            "likert_recurre_umss", "likert_consultado_autoevaluacion");

    // Traduccion ESCALA 4 (titulados).
    public static double traducir(String etiqueta) {
        String clave = LimpiezaDatos.normalizar(etiqueta);
        if (clave.contains("NO_SABE")) return Double.NaN;
        if (clave.contains("TOTALMENTE_EN_DESACUERDO")
                || clave.contains("TOTALMENTE_DESACUERDO")) return 1;
        if (clave.equals("DESACUERDO") || clave.contains("PARCIALMENTE_DESACUERDO")) return 2;
        if (clave.equals("ACUERDO") || clave.contains("PARCIALMENTE_ACUERDO")) return 3;
        if (clave.contains("TOTALMENTE_ACUERDO")) return 4;
        if (clave.contains("NO_IMPORTANTE")) return 1;
        if (clave.contains("POCO_IMPORTANTE")) return 2;
        if (clave.contains("IMPORTANTE") && !clave.contains("MUY")) return 3;
        if (clave.contains("MUY_IMPORTANTE")) return 4;
        if (clave.contains("INSATISFECHO") && !clave.contains("ALGO")) return 1;
        if (clave.contains("ALGO_SATISFECHO")) return 2;
        if (clave.equals("SATISFECHO")) return 3;
        return Double.NaN;
    }

    // Traduccion ESCALA 3 (empleadores). "No sabe" -> NaN.
    public static double traducir3(String etiqueta) {
        String clave = LimpiezaDatos.normalizar(etiqueta);
        if (clave.isEmpty() || clave.contains("NO_SABE")) return Double.NaN;
        if (clave.contains("TOTALMENTE_DESACUERDO")
                || clave.contains("TOTALMENTE_EN_DESACUERDO")) return 1;
        if (clave.contains("PARCIALMENTE_ACUERDO")
                || clave.contains("PARCIALMENTE_DE_ACUERDO")) return 2;
        if (clave.contains("TOTALMENTE_ACUERDO")
                || clave.contains("TOTALMENTE_DE_ACUERDO")) return 3;
        return Double.NaN;
    }

    // Despachador: elige 3P si la columna es de empleador, sino 4P.
    public static double traducir(String etiqueta, String columna) {
        return COLUMNAS_3P.contains(columna) ? traducir3(etiqueta) : traducir(etiqueta);
    }

    public static int escalaMaximaDe(String columna) {
        if (COLUMNAS_3P.contains(columna)) return 3;
        if (columna.equals("satisfaccion_negocio")) return 3;
        return 4;
    }

    public static String etiquetaDeMedia(double media, int maximo) {
        if (Double.isNaN(media)) return "sin dato";
        if (maximo == 3) {
            if (media < 1.5) return "rechazo (zona del 1)";
            if (media < 2.5) return "parcial (zona del 2)";
            return "acuerdo total (zona del 3)";
        }
        if (media < 1.5) return "rechazo fuerte (zona del 1)";
        if (media < 2.5) return "desacuerdo (zona del 2)";
        if (media < 3.0) return "tibio, entre desacuerdo y acuerdo";
        if (media < 3.5) return "de acuerdo (zona del 3)";
        return "acuerdo total (zona del 4)";
    }

    public static String etiquetaDeConsenso(double sd) {
        if (Double.isNaN(sd)) return "sin dato";
        if (sd < 0.4) return "consenso alto";
        if (sd < 0.8) return "consenso moderado";
        return "opiniones divididas";
    }

    public static String etiquetaDePrecision(double li, double ls, boolean esLikert) {
        if (Double.isNaN(li) || Double.isNaN(ls)) return "sin dato";
        double ancho = ls - li;
        if (esLikert) {
            if (ancho < 0.4) return "estimacion muy precisa";
            if (ancho < 0.8) return "estimacion buena";
            if (ancho < 1.5) return "estimacion regular";
            return "no concluyente";
        }
        if (ancho < 1.0) return "estimacion muy precisa";
        if (ancho < 4.0) return "estimacion buena";
        if (ancho < 8.0) return "estimacion regular";
        return "no concluyente";
    }

    public static final String GUIA_LIKERT =
            "Titulados (escala 4): 1 = Totalmente desacuerdo, 2 = Desacuerdo, "
            + "3 = De acuerdo, 4 = Totalmente de acuerdo.\n"
            + "Empleadores (escala 3): 1 = Totalmente desacuerdo, "
            + "2 = Parcialmente de acuerdo, 3 = Totalmente de acuerdo. "
            + "'No sabe' y vacio se excluyen.\n"
            + "n = respondieron. desv <0.4 consenso alto | 0.4-0.8 moderado | >0.8 dividido.";
}
