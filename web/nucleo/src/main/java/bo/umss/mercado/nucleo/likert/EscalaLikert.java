package bo.umss.mercado.nucleo.likert;

import bo.umss.mercado.nucleo.limpieza.LimpiezaDatos;

// EscalaLikert: traduce opiniones en TEXTO a puntajes 1..4 para promediar.
// Por qué: "De acuerdo" no se puede promediar, pero el 3 sí.
// La encuesta de titulados usa 4 opciones (1-4); la de negocio de
// satisfacción usa solo 3 (1-3); la del empleador usa 1, 3 y 4
// (no tiene 2) y su "NO_SABE" se excluye por no aportar.
// El orden de las preguntas importa: lo específico va primero
// ("TOTALMENTE_..." antes que el "DESACUERDO" suelto).
public class EscalaLikert {

    // traducir(): etiqueta -> puntaje. Lo desconocido o vacío -> NaN
    // (así NO contamina los promedios: simplemente se salta).
    public static double traducir(String etiqueta) {
        String clave = LimpiezaDatos.normalizar(etiqueta);
        if (clave.contains("TOTALMENTE_DESACUERDO")) {
            return 1;
        }
        if (clave.equals("DESACUERDO") || clave.contains("PARCIALMENTE_DESACUERDO")) {
            return 2;
        }
        if (clave.equals("ACUERDO") || clave.contains("PARCIALMENTE_ACUERDO")
                || clave.contains("ALGO")) {
            return 3;
        }
        if (clave.contains("TOTALMENTE_ACUERDO")) {
            return 4;
        }
        if (clave.contains("NO_IMPORTANTE")) {
            return 1;
        }
        if (clave.contains("POCO_IMPORTANTE")) {
            return 2;
        }
        if (clave.contains("IMPORTANTE") && !clave.contains("MUY")) {
            return 3;
        }
        if (clave.contains("MUY_IMPORTANTE")) {
            return 4;
        }
        if (clave.contains("INSATISFECHO") && !clave.contains("ALGO")) {
            return 1;
        }
        if (clave.contains("ALGO_SATISFECHO")) {
            return 2;
        }
        if (clave.equals("SATISFECHO")) {
            return 3;
        }
        return Double.NaN;
    }

    // escalaMaximaDe(): techo de cada pregunta (casi todas llegan a 4;
    // la satisfacción con el negocio solo tiene 3 opciones).
    public static int escalaMaximaDe(String nombreColumna) {
        if (nombreColumna.equals("satisfaccion_negocio")) {
            return 3;
        }
        return 4;
    }

    // etiquetaDeMedia(): el promedio, dicho en palabras.
    public static String etiquetaDeMedia(double mediaObservada, int maximoEscala) {
        if (Double.isNaN(mediaObservada)) {
            return "sin dato";
        }
        if (mediaObservada < 1.5) {
            return "rechazo fuerte (zona del 1)";
        }
        if (mediaObservada < 2.5) {
            return "desacuerdo (zona del 2)";
        }
        if (mediaObservada < 3.0) {
            return "tibio, entre desacuerdo y acuerdo";
        }
        if (mediaObservada < 3.5) {
            return "de acuerdo / satisfecho (zona del 3)";
        }
        return "acuerdo total (zona del " + maximoEscala + ")";
    }

    // etiquetaDeConsenso(): ¿piensan parecido? (por la desviación).
    public static String etiquetaDeConsenso(double desviacionEstandar) {
        if (Double.isNaN(desviacionEstandar)) {
            return "sin dato";
        }
        if (desviacionEstandar < 0.5) {
            return "todos piensan casi igual";
        }
        if (desviacionEstandar <= 1.0) {
            return "opiniones moderadamente dispersas";
        }
        return "opiniones muy divididas";
    }

    // etiquetaDePrecision(): ¿qué tan creíble es el IC? (por su ancho).
    // OJO: 0.4 puntos Likert es mucho, pero 0.4 años es poquísimo;
    // por eso hay umbrales separados para escala Likert y para años.
    public static String etiquetaDePrecision(double limiteInferior,
            double limiteSuperior, boolean esEscalaLikert) {
        if (Double.isNaN(limiteInferior) || Double.isNaN(limiteSuperior)) {
            return "sin dato";
        }
        double ancho = limiteSuperior - limiteInferior;
        if (esEscalaLikert) {
            if (ancho < 0.5) {
                return "estimación muy precisa";
            }
            if (ancho < 1.0) {
                return "estimación buena";
            }
            if (ancho < 2.0) {
                return "estimación regular (muestra chica o dividida)";
            }
            return "no concluyente (muestra muy chica)";
        }
        if (ancho < 1.0) {
            return "estimación muy precisa";
        }
        if (ancho < 4.0) {
            return "estimación buena";
        }
        if (ancho < 8.0) {
            return "estimación regular (muestra chica o dividida)";
        }
        return "no concluyente (muestra muy chica)";
    }

    // GUIA_LIKERT: leyenda que se muestra arriba de cada reporte/pantalla
    // para que nadie tenga que adivinar qué significa cada número.
    public static final String GUIA_LIKERT =
            "Las opiniones vienen como TEXTO y se traducen a NÚMEROS:\n"
            + "  1 = TOTALMENTE EN DESACUERDO / INSATISFECHO / NO IMPORTANTE\n"
            + "  2 = EN DESACUERDO / ALGO SATISFECHO / POCO IMPORTANTE\n"
            + "  3 = DE ACUERDO / SATISFECHO / IMPORTANTE\n"
            + "  4 = TOTALMENTE DE ACUERDO / MUY IMPORTANTE\n"
            + "OJO: 'satisfaccion_negocio' solo llega hasta 3. "
            + "'NO_SABE' y vacíos NO se cuentan.\n"
            + "  n = cuántos respondieron. media 1-1.5 rechazo | 1.5-2.5 "
            + "desacuerdo | 2.5-3.0 tibio | 3.0-3.5 de acuerdo | 3.5-4 total.\n"
            + "  desv <0.5 todos igual | 0.5-1.0 moderada | >1.0 divididos.\n"
            + "  IC95% = rango del valor REAL. Ancho <0.5 preciso | 0.5-1.0 "
            + "bueno | 1.0-2.0 regular | >2.0 malo.\n";
}
