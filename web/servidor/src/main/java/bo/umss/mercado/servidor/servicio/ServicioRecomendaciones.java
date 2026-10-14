package bo.umss.mercado.servidor.servicio;

import bo.umss.mercado.nucleo.likert.EscalaLikert;
import bo.umss.mercado.nucleo.limpieza.LimpiezaDatos;
import bo.umss.mercado.nucleo.stats.Frecuencias;
import bo.umss.mercado.nucleo.stats.Numeros;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

// ServicioRecomendaciones: reglas if-then sobre los datos (cap.6 del
// informe). Cada regla cruza un umbral con su acción concreta.
// Si ninguna se dispara, la muestra está estable.
@Service
public class ServicioRecomendaciones {

    private final ServicioAnalisis analisis;

    public ServicioRecomendaciones(ServicioAnalisis analisis) {
        this.analisis = analisis;
    }

    public List<String> generar() {
        List<String> recomendaciones = new ArrayList<>();
        List<Map<String, String>> mapas = analisis.mapasTitulados();
        int total = Math.max(1, mapas.size());

        long interesadosIA = mapas.stream()
                .filter(f -> "IA".equals(f.getOrDefault(
                        "area_interes_posgrado", "")))
                .count();
        if (interesadosIA * 100.0 / total > 30) {
            recomendaciones.add("Alta demanda en IA -> abrir maestría modular en IA.");
        }

        double recursosPropios = analisis.proporcionDonde(mapas,
                "financiamiento_formacion",
                v -> LimpiezaDatos.normalizar(v).contains("PROPIO"));
        if (!Double.isNaN(recursosPropios) && recursosPropios > 0.5) {
            recomendaciones.add("Más del 50% usa recursos propios -> "
                    + "convenio con banca / becas parciales.");
        }

        List<Double> consulta = analisis.likertDe(analisis.mapasEmpleadores(),
                "likert_consulta_regular");
        long deAcuerdo = consulta.stream().filter(p -> p >= 3).count();
        if (!consulta.isEmpty() && deAcuerdo * 100.0 / consulta.size() < 50) {
            recomendaciones.add(
                    "Consulta al empleador baja -> activar observatorio permanente.");
        }

        List<Double> satisfaccion =
                analisis.likertDe(mapas, "satisfaccion_formacion");
        if (!satisfaccion.isEmpty() && Numeros.promedio(satisfaccion) < 3.0) {
            recomendaciones.add("Satisfacción menor a 3.0 -> revisar malla curricular.");
        }

        Map<String, Integer> tiempos = Frecuencias.contar(
                analisis.columnaDe(mapas, "tiempo_primer_empleo"));
        int lentos = tiempos.getOrDefault("8_12_MESES", 0)
                + tiempos.getOrDefault("MAS_12_MESES", 0);
        if (lentos * 1.0 / total > 0.25) {
            recomendaciones.add("Inserción lenta (más del 25% tarda 8+ meses) -> "
                    + "fortalecer bolsa de trabajo.");
        }

        if (recomendaciones.isEmpty()) {
            recomendaciones.add(
                    "Muestra estable: mantener oferta actual y monitorear anualmente.");
        }
        return recomendaciones;
    }

    // Nota de lectura para pantallas (usa el núcleo Likert).
    public String etiquetaMedia(double media, int maximo) {
        return EscalaLikert.etiquetaDeMedia(media, maximo);
    }
}
