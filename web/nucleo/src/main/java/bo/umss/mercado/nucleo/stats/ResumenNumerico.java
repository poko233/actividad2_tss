package bo.umss.mercado.nucleo.stats;

import java.util.List;

// ResumenNumerico: el resultado de describir UNA variable numérica.
// Es un "record": una cajita inmutable con nombre para cada valor,
// para no andar pasando 9 variables sueltas por ahí.
// Ejemplo: media() = promedio, mediana() = valor del medio,
// desv() = dispersión, q1()/q3() = cuartiles (25% y 75%).
public record ResumenNumerico(
        int totalDatos,
        double media,
        double mediana,
        double desv,
        double minimo,
        double maximo,
        double q1,
        double q2,
        double q3) {
}
