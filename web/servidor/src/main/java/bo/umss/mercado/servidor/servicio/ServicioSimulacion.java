package bo.umss.mercado.servidor.servicio;

import bo.umss.mercado.nucleo.limpieza.LimpiezaDatos;
import bo.umss.mercado.nucleo.stats.IntervalosConfianza;
import bo.umss.mercado.nucleo.stats.Numeros;
import bo.umss.mercado.nucleo.stats.RangoConfianza;
import bo.umss.mercado.servidor.datos.RepositorioSimulaciones;
import bo.umss.mercado.servidor.datos.Simulacion;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.stereotype.Service;

// ServicioSimulacion: proyección de demanda Monte Carlo LIGERO.
// Idea: NO se inventan datos. Se parte del % real afín al área y se
// sortea 1000 veces "¿cuántos vendrían si se abre el programa?".
// Cada corrida se GUARDA en la base (historial auditable).
@Service
public class ServicioSimulacion {

    // Los 5 programas a evaluar: {título, área temática}.
    public static final List<String[]> ESCENARIOS = List.of(
            new String[]{"Abrir maestría en IA", "IA"},
            new String[]{"Abrir especialidad en ciberseguridad", "REDES_SEGURIDAD"},
            new String[]{"Abrir maestría en Ing. Software", "ING_SOFTWARE"},
            new String[]{"Abrir maestría en Base de Datos", "BASE_DATOS"},
            new String[]{"Abrir maestría en Robótica", "ROBOTICA"});

    private static final int REPETICIONES = 1000;

    private final ServicioAnalisis analisis;
    private final RepositorioSimulaciones historial;
    private final Random azar = new Random();

    public ServicioSimulacion(ServicioAnalisis analisis,
            RepositorioSimulaciones historial) {
        this.analisis = analisis;
        this.historial = historial;
    }

    // esAfin(): ¿declaró interés SI justo en esta área?
    private boolean esAfin(Map<String, String> fila, String area) {
        return fila.getOrDefault("interes_posgrado", "").equals("SI")
                && LimpiezaDatos.normalizar(
                        fila.getOrDefault("area_interes_posgrado", ""))
                        .contains(LimpiezaDatos.normalizar(area));
    }

    // simularTodo(): corre los 5 escenarios, guarda cada uno y devuelve
    // las fichas para la pantalla (con su frase simple).
    public List<FichaEscenario> simularTodo() {
        List<Map<String, String>> mapas = analisis.mapasTitulados();
        List<FichaEscenario> fichas = new ArrayList<>();
        for (String[] escenario : ESCENARIOS) {
            String titulo = escenario[0];
            String area = escenario[1];
            int afines = 0;
            for (Map<String, String> fila : mapas) {
                if (esAfin(fila, area)) {
                    afines++;
                }
            }
            double proporcion =
                    mapas.isEmpty() ? 0 : (double) afines / mapas.size();
            // 1000 sorteos: el afín convierte con 0.8, el resto con p*0.2.
            List<Double> demandas = new ArrayList<>();
            for (int repeticion = 0; repeticion < REPETICIONES; repeticion++) {
                int inscritos = 0;
                for (Map<String, String> fila : mapas) {
                    double probabilidad = esAfin(fila, area) ? 0.8
                            : proporcion * 0.2;
                    if (azar.nextDouble() < probabilidad) {
                        inscritos++;
                    }
                }
                demandas.add((double) inscritos);
            }
            double media = Numeros.promedio(demandas);
            RangoConfianza rango = IntervalosConfianza.paraMedia(demandas);
            String frase = "Si abres este programa, espera unos "
                    + Math.round(media) + " inscritos (entre "
                    + Math.round(rango.limiteInferior()) + " y "
                    + Math.round(rango.limiteSuperior()) + ").";
            fichas.add(new FichaEscenario(titulo, area, proporcion, media,
                    rango.limiteInferior(), rango.limiteSuperior(), frase));
            // Se registra en la base para el historial.
            Simulacion registro = new Simulacion();
            registro.setFechaHora(LocalDateTime.now());
            registro.setEscenario(titulo);
            registro.setArea(area);
            registro.setProporcionEmpirica(proporcion);
            registro.setDemandaMedia(media);
            registro.setLimiteInferior(rango.limiteInferior());
            registro.setLimiteSuperior(rango.limiteSuperior());
            registro.setRepeticiones(REPETICIONES);
            historial.save(registro);
        }
        return fichas;
    }

    // historial(): corridas guardadas, de la más nueva a la más vieja.
    public List<Simulacion> historial() {
        return historial.findAllByOrderByFechaHoraDesc();
    }
}
