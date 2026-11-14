package bo.umss.mercado.servidor.servicio;

import bo.umss.mercado.nucleo.limpieza.LimpiezaDatos;
import bo.umss.mercado.servidor.datos.RepositorioSimulaciones;
import bo.umss.mercado.servidor.datos.Simulacion;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import org.springframework.stereotype.Service;

@Service
public class ServicioSimulacion {

    private final ServicioAnalisis analisis;
    private final RepositorioSimulaciones historial;

    public ServicioSimulacion(ServicioAnalisis analisis, RepositorioSimulaciones historial) {
        this.analisis = analisis;
        this.historial = historial;
    }

    public double obtenerProbabilidadInteresados(String variable, String categoria) {
        if (variable.equals("posibilidad_futura_contratacion")) {
            return switch (categoria) {
                case "ALTA_PROBABILIDAD" -> 0.80;
                case "DEPENDE_CRECIMIENTO" -> 0.40;
                default -> 0.0;
            };
        }
        if (variable.equals("financiamiento_formacion")) {
            return switch (categoria) {
                case "BECA_TOTAL" -> 0.95;
                case "BECA_PARCIAL" -> 0.88;
                default -> 0.80;
            };
        }
        if (variable.equals("situacion_laboral_actual") && categoria.equals("TRABAJA_ORG")) {
            return 0.95;
        }
        if (variable.equals("institucion_futura_posgrado") && categoria.equals("UNIV_INTERNACIONALES")) {
            return 0.80;
        }
        if (variable.equals("departamento") && (categoria.equals("EXTRANJERO") || categoria.equals("SANTA_CRUZ"))) {
            return 0.80;
        }
        return 0.80;
    }

    public double calcularProporcion(String variable, String categoria, boolean usarEmpleadores) {
        List<Map<String, String>> datos = usarEmpleadores ? analisis.mapasEmpleadores() : analisis.mapasTitulados();
        int total = datos.size();
        if (total == 0)
            return 0;
        long count = datos.stream().filter(f -> LimpiezaDatos.normalizar(f.getOrDefault(variable, ""))
                .contains(LimpiezaDatos.normalizar(categoria))).count();
        return (double) count / total;
    }

    public ConfiguracionSimulacion configEscenarioDemanda() {
        return new ConfiguracionSimulacion("Demanda Posgrado", "area_interes_posgrado", "IA", 0.80, 0.0878, 1000,
                12345L, false);
    }

    public ConfiguracionSimulacion configEscenarioContratacion() {
        return new ConfiguracionSimulacion("Contratación Empleadores", "posibilidad_futura_contratacion",
                "ALTA_PROBABILIDAD", 0.80, 0.20, 1000, 54321L, true);
    }

    public ConfiguracionSimulacion configEscenarioBecas() {
        return new ConfiguracionSimulacion("Impacto de Becas", "financiamiento_formacion", "BECA_TOTAL", 0.95, 0.10,
                1000, 99999L, false);
    }

    public ConfiguracionSimulacion configEscenarioVirtual() {
        return new ConfiguracionSimulacion("Modalidad Virtual", "situacion_laboral_actual", "TRABAJA_ORG", 0.95, 0.15,
                1000, 88888L, false);
    }

    public ConfiguracionSimulacion configEscenarioFidelizacion() {
        return new ConfiguracionSimulacion("Fidelización vs Competencia", "institucion_futura_posgrado",
                "UNIV_INTERNACIONALES", 0.80, 0.10, 1000, 77777L, false);
    }

    public ConfiguracionSimulacion configEscenarioExpansion() {
        return new ConfiguracionSimulacion("Expansión Departamental", "departamento", "EXTRANJERO", 0.80, 0.10, 1000,
                66666L, false);
    }

    public ResultadoSimulacion ejecutarSimulacion(ConfiguracionSimulacion config) {
        List<Map<String, String>> datos = config.usarEmpleadores() ? analisis.mapasEmpleadores()
                : analisis.mapasTitulados();
        int poblacionTotal = datos.size();
        if (poblacionTotal == 0)
            throw new IllegalStateException("No hay datos para simular.");

        int interesados = 0;
        for (Map<String, String> fila : datos) {
            if (LimpiezaDatos.normalizar(fila.getOrDefault(config.nombreVariable(), ""))
                    .contains(LimpiezaDatos.normalizar(config.categoriaObjetivo()))) {
                interesados++;
            }
        }
        double proporcion = (double) interesados / poblacionTotal;
        int resto = poblacionTotal - interesados;

        Random rnd = new Random(config.semilla());
        List<Integer> resultados = new ArrayList<>();
        Map<Integer, Integer> histograma = new TreeMap<>();
        List<Integer> primerasIteraciones = new ArrayList<>();

        for (int i = 0; i < config.iteraciones(); i++) {
            int exitos = 0;
            for (int j = 0; j < interesados; j++)
                if (rnd.nextDouble() < config.probabilidadInteresados())
                    exitos++;
            for (int j = 0; j < resto; j++)
                if (rnd.nextDouble() < config.probabilidadResto())
                    exitos++;

            resultados.add(exitos);
            histograma.put(exitos, histograma.getOrDefault(exitos, 0) + 1);
            if (i < 5)
                primerasIteraciones.add(exitos);
        }

        double suma = 0;
        for (int r : resultados)
            suma += r;
        double media = suma / resultados.size();

        double sumaCuadrados = 0;
        for (int r : resultados)
            sumaCuadrados += Math.pow(r - media, 2);
        double de = Math.sqrt(sumaCuadrados / (resultados.size() - 1));

        double icInferior = Math.max(0, media - (1.96 * de));
        double icSuperior = media + (1.96 * de);

        double probPesimista = Math.max(0.0, config.probabilidadInteresados() - 0.3);
        double probOptimista = Math.min(1.0, config.probabilidadInteresados() + 0.15);

        double mediaPesimista = ejecutarSimple(config.iteraciones(), config.semilla(), interesados, resto,
                probPesimista, config.probabilidadResto());
        double mediaOptimista = ejecutarSimple(config.iteraciones(), config.semilla(), interesados, resto,
                probOptimista, config.probabilidadResto());

        Simulacion registro = new Simulacion();
        registro.setEscenario(config.nombreEscenario() + " - " + config.categoriaObjetivo());
        registro.setArea(config.categoriaObjetivo());
        registro.setProporcionEmpirica(proporcion);
        registro.setDemandaMedia(media);
        registro.setLimiteInferior(icInferior);
        registro.setLimiteSuperior(icSuperior);
        registro.setRepeticiones(config.iteraciones());
        registro.setSemilla(config.semilla());
        registro.setProbInteresados(config.probabilidadInteresados());
        registro.setProbResto(config.probabilidadResto());
        historial.save(registro);

        return new ResultadoSimulacion(proporcion, interesados, poblacionTotal, media, de, icInferior, icSuperior,
                primerasIteraciones, histograma, mediaPesimista, media, mediaOptimista);
    }

    private double ejecutarSimple(int iteraciones, long semilla, int interesados, int resto, double pA, double pB) {
        Random rnd = new Random(semilla);
        double suma = 0;
        for (int i = 0; i < iteraciones; i++) {
            int exitos = 0;
            for (int j = 0; j < interesados; j++)
                if (rnd.nextDouble() < pA)
                    exitos++;
            for (int j = 0; j < resto; j++)
                if (rnd.nextDouble() < pB)
                    exitos++;
            suma += exitos;
        }
        return suma / iteraciones;
    }

    public List<Simulacion> historial() {
        return historial.findAllByOrderByFechaHoraDesc();
    }
}