package bo.umss.mercado.servidor.servicio;

import bo.umss.mercado.nucleo.likert.EscalaLikert;
import bo.umss.mercado.nucleo.limpieza.LimpiezaDatos;
import bo.umss.mercado.nucleo.stats.Frecuencias;
import bo.umss.mercado.nucleo.stats.IntervalosConfianza;
import bo.umss.mercado.nucleo.stats.Numeros;
import bo.umss.mercado.nucleo.stats.RangoConfianza;
import bo.umss.mercado.nucleo.stats.ResumenNumerico;
import bo.umss.mercado.servidor.datos.RepositorioEmpleadores;
import bo.umss.mercado.servidor.datos.RepositorioTitulados;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

// ServicioAnalisis: TODA la estadística descriptiva en un solo lugar.
// Lee filas de la base, las convierte a mapas y delega los cálculos
// al núcleo. Las pantallas solo piden y muestran: aquí vive el cerebro.
@Service
public class ServicioAnalisis {

    private final RepositorioTitulados titulados;
    private final RepositorioEmpleadores empleadores;

    public ServicioAnalisis(RepositorioTitulados titulados,
            RepositorioEmpleadores empleadores) {
        this.titulados = titulados;
        this.empleadores = empleadores;
    }

    // ---- Lecturas básicas (de la base a listas simples) ----

    public List<Map<String, String>> mapasTitulados() {
        List<Map<String, String>> mapas = new ArrayList<>();
        titulados.findAll().forEach(t -> mapas.add(t.comoMapa()));
        return mapas;
    }

    public List<Map<String, String>> mapasEmpleadores() {
        List<Map<String, String>> mapas = new ArrayList<>();
        empleadores.findAll().forEach(e -> mapas.add(e.comoMapa()));
        return mapas;
    }

    // columnaDe(): todos los valores de UNA columna (textos tal cual).
    public List<String> columnaDe(List<Map<String, String>> mapas, String columna) {
        List<String> valores = new ArrayList<>();
        for (Map<String, String> fila : mapas) {
            valores.add(fila.getOrDefault(columna, ""));
        }
        return valores;
    }

    // numerosDe(): la columna convertida a números (lo no numérico se salta).
    public List<Double> numerosDe(List<Map<String, String>> mapas, String columna) {
        List<Double> valores = new ArrayList<>();
        for (Map<String, String> fila : mapas) {
            String texto = fila.getOrDefault(columna, "").replace(",", ".");
            if (texto.isEmpty()) {
                continue;
            }
            try {
                valores.add(Double.parseDouble(texto));
            } catch (NumberFormatException noEsNumero) {
                // se salta en silencio, igual que en la limpieza
            }
        }
        return valores;
    }

    // likertDe(): la columna traducida a puntajes 1..4 (NO_SABE se salta).
    public List<Double> likertDe(List<Map<String, String>> mapas, String columna) {
        List<Double> puntajes = new ArrayList<>();
        for (Map<String, String> fila : mapas) {
            double puntaje = EscalaLikert.traducir(fila.getOrDefault(columna, ""));
            if (!Double.isNaN(puntaje)) {
                puntajes.add(puntaje);
            }
        }
        return puntajes;
    }

    // ---- Indicadores listos para las pantallas (HU1/HU2/HU3) ----

    public ResumenNumerico resumenNumerico(String columna) {
        return Numeros.resumir(numerosDe(mapasTitulados(), columna));
    }

    // resumenNumericoDeValores(): mismo resumen pero de una lista ya
    // calculada (ej. puntajes Likert filtrados por la vista).
    public ResumenNumerico resumenNumericoDeValores(List<Double> valores) {
        return Numeros.resumir(valores);
    }

    public Map<String, Integer> frecuenciasDe(String columna) {
        return Frecuencias.contar(columnaDe(mapasTitulados(), columna));
    }

    // proporcionDonde(): % de filas que cumplen una condición.
    // Las vacías NO entran al denominador (no-respuesta ≠ negativo).
    public double proporcionDonde(List<Map<String, String>> mapas, String columna,
            java.util.function.Predicate<String> condicion) {
        int favorables = 0;
        int conRespuesta = 0;
        for (Map<String, String> fila : mapas) {
            String valor = fila.getOrDefault(columna, "");
            if (valor.isEmpty()) {
                continue;
            }
            conRespuesta++;
            if (condicion.test(valor)) {
                favorables++;
            }
        }
        return conRespuesta == 0 ? Double.NaN : (double) favorables / conRespuesta;
    }

    // Tasa de inserción: trabaja o emprende (HU1).
    public double tasaInsercion() {
        return proporcionDonde(mapasTitulados(), "situacion_laboral_actual",
                v -> !LimpiezaDatos.normalizar(v).contains("NO_TRABAJA") && !v.isEmpty());
    }

    // Intención de posgrado: marcó "SI" (HU2).
    public double intencionPosgrado() {
        return proporcionDonde(mapasTitulados(), "interes_posgrado",
                v -> v.equals("SI"));
    }

    // Requiere beca: fuente con "BECA" total o parcial (HU3).
    public double proporcionRequiereBeca() {
        return proporcionDonde(mapasTitulados(), "financiamiento_formacion",
                v -> LimpiezaDatos.normalizar(v).contains("BECA"));
    }

    public RangoConfianza rangoDe(double proporcion, int total) {
        return IntervalosConfianza.paraProporcion(proporcion, total);
    }

    // ---- Segmentación por antigüedad (HU1) ----

    // segmentoDe(): Junior 0-3 (piden especialización inicial),
    // Intermedio 4-7, Consolidado 8+ (mandos). Cortes de la guía.
    public static String segmentoDe(double aniosVidaProfesional) {
        if (Double.isNaN(aniosVidaProfesional)) {
            return "";
        }
        if (aniosVidaProfesional <= 3) {
            return "JUNIOR (0-3 años)";
        }
        if (aniosVidaProfesional <= 7) {
            return "INTERMEDIO (4-7 años)";
        }
        return "CONSOLIDADO (8+ años)";
    }

    // fichasSegmentos(): una ficha por grupo para dibujar la tabla.
    public List<FichaSegmento> fichasSegmentos() {
        Map<String, List<Map<String, String>>> grupos = new LinkedHashMap<>();
        grupos.put("JUNIOR (0-3 años)", new ArrayList<>());
        grupos.put("INTERMEDIO (4-7 años)", new ArrayList<>());
        grupos.put("CONSOLIDADO (8+ años)", new ArrayList<>());
        for (Map<String, String> fila : mapasTitulados()) {
            try {
                double anios = Double.parseDouble(
                        fila.getOrDefault("anios_vida_profesional", "")
                                .replace(",", "."));
                grupos.get(segmentoDe(anios)).add(fila);
            } catch (Exception sinAntiguedad) {
                // sin años no se puede segmentar: se salta
            }
        }
        List<FichaSegmento> fichas = new ArrayList<>();
        for (var grupo : grupos.entrySet()) {
            List<Double> satisfacciones = likertDe(grupo.getValue(),
                    "satisfaccion_formacion");
            double satMedia = satisfacciones.isEmpty() ? Double.NaN
                    : Numeros.promedio(satisfacciones);
            double intencion = proporcionDonde(grupo.getValue(), "interes_posgrado",
                    v -> v.equals("SI"));
            List<Double> tasas = new ArrayList<>();
            for (Map<String, String> fila : grupo.getValue()) {
                try {
                    double anios = Double.parseDouble(fila
                            .getOrDefault("anios_vida_profesional", "0")
                            .replace(",", "."));
                    double desempleo = Double.parseDouble(fila
                            .getOrDefault("anios_desempleado", "0")
                            .replace(",", "."));
                    if (anios > 0) {
                        tasas.add(desempleo / anios);
                    }
                } catch (Exception sinDato) {
                    // se salta
                }
            }
            double tasaMedia = tasas.isEmpty() ? Double.NaN : Numeros.promedio(tasas);
            fichas.add(new FichaSegmento(grupo.getKey(), grupo.getValue().size(),
                    satMedia, intencion * 100, tasaMedia));
        }
        return fichas;
    }

    // fraseDiferenciaSegmentos(): ¿juniors y consolidados difieren?
    // Si el IC incluye al 0, están IGUAL de satisfechos (§7.1 guía).
    public String fraseDiferenciaSegmentos() {
        List<Double> juniors = new ArrayList<>();
        List<Double> consolidados = new ArrayList<>();
        for (Map<String, String> fila : mapasTitulados()) {
            try {
                double anios = Double.parseDouble(fila
                        .getOrDefault("anios_vida_profesional", "").replace(",", "."));
                double sat = EscalaLikert.traducir(
                        fila.getOrDefault("satisfaccion_formacion", ""));
                if (Double.isNaN(sat)) {
                    continue;
                }
                if (anios <= 3) {
                    juniors.add(sat);
                } else if (anios >= 8) {
                    consolidados.add(sat);
                }
            } catch (Exception sinDato) {
                // se salta
            }
        }
        RangoConfianza rango = IntervalosConfianza.paraDiferencia(juniors, consolidados);
        if (Double.isNaN(rango.limiteInferior())) {
            return "Aún no hay datos para comparar segmentos.";
        }
        boolean incluyeCero = rango.limiteInferior() <= 0 && 0 <= rango.limiteSuperior();
        return incluyeCero
                ? "Juniors y consolidados están IGUAL de satisfechos."
                : "Juniors y consolidados están DISTINTO de satisfechos.";
    }

    // ---- Apoyos para panel y empleadores ----

    // topCategorias(): las N más repetidas, sin contar "(VACIO)".
    public List<Map.Entry<String, Integer>> topCategorias(
            Map<String, Integer> frecuencias, int cuantas) {
        List<Map.Entry<String, Integer>> ordenadas = new ArrayList<>(frecuencias.entrySet());
        ordenadas.sort(Comparator.comparing(Map.Entry<String, Integer>::getValue)
                .reversed());
        List<Map.Entry<String, Integer>> top = new ArrayList<>();
        for (var entrada : ordenadas) {
            if (entrada.getKey().equals(Frecuencias.ETIQUETA_VACIO)) {
                continue;
            }
            top.add(entrada);
            if (top.size() >= cuantas) {
                break;
            }
        }
        return top;
    }

    // competenciasMencionadas(): minería simple del texto libre del
    // empleador: cuenta palabras (top N) saltando las vacías.
    public List<Map.Entry<String, Integer>> competenciasMencionadas(int cuantas) {
        Map<String, Integer> conteo = new LinkedHashMap<>();
        Set<String> vacias = Set.of("los", "las", "una", "uno", "con", "para",
                "por", "que", "del", "y", "de", "la", "el", "en", "the", "and",
                "conocimiento", "area", "areas", "requiere", "requieren");
        for (Map<String, String> fila : mapasEmpleadores()) {
            String texto = fila.getOrDefault(
                    "habilidades_competencias_requeridas", "") + " "
                    + fila.getOrDefault("areas_conocimiento_emergentes", "") + " "
                    + fila.getOrDefault("herramientas_tecnologicas", "");
            for (String palabra : texto.toLowerCase()
                    .replaceAll("[^a-záéíóúñ0-9+#. ]", " ").split("\\s+")) {
                palabra = palabra.trim();
                if (palabra.length() < 3 || vacias.contains(palabra)) {
                    continue;
                }
                conteo.put(palabra, conteo.getOrDefault(palabra, 0) + 1);
            }
        }
        return topCategorias(conteo, cuantas);
    }
}
