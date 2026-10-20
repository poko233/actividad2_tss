package bo.umss.mercado.servidor.servicio;

import bo.umss.mercado.nucleo.csv.LectorCsv;
import bo.umss.mercado.nucleo.likert.EscalaLikert;
import bo.umss.mercado.nucleo.stats.Frecuencias;
import bo.umss.mercado.nucleo.stats.IntervalosConfianza;
import bo.umss.mercado.nucleo.stats.Numeros;
import bo.umss.mercado.nucleo.stats.PruebaChiCuadrada;
import bo.umss.mercado.nucleo.stats.ResultadoChiCuadrada;
import bo.umss.mercado.servidor.datos.Empleador;
import bo.umss.mercado.servidor.datos.RepositorioEmpleadores;
import bo.umss.mercado.servidor.datos.RepositorioTitulados;
import bo.umss.mercado.servidor.datos.Titulado;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * ServicioExportacion: genera los archivos CSV que el comite de evaluacion
 * se descarga desde la pantalla "Exportar" (HU5: "exportar matrices de
 * frecuencia cruzada ... en formato tabular limpio (CSV/Excel)").
 *
 * Cada metodo devuelve el contenido CSV completo como texto, listo para
 * enviarse como descarga. Los datos salen de la misma base de datos que
 * alimentan las pantallas, asi que el CSV y la web siempre coinciden.
 */
@Service
public class ServicioExportacion {

    private final RepositorioTitulados repoTitulados;
    private final RepositorioEmpleadores repoEmpleadores;
    private final ServicioAnalisis analisis;

    public ServicioExportacion(RepositorioTitulados repoTitulados,
            RepositorioEmpleadores repoEmpleadores,
            ServicioAnalisis analisis) {
        this.repoTitulados = repoTitulados;
        this.repoEmpleadores = repoEmpleadores;
        this.analisis = analisis;
    }

    // Los 10 items Likert de la encuesta a empleadores.
    private static final String[] ITEMS_LIKERT = {
            "likert_confianza_formadora", "likert_consistencia_titulo",
            "likert_consulta_regular", "likert_conoce_perfil",
            "likert_coherencia_perfil", "likert_formacion_desempeno",
            "likert_considera_opinion", "likert_competencias_laborales",
            "likert_valores_actitudes", "likert_participacion_procesos"};

    /** Nombres de los CSV disponibles (tambien se usan en la lista blanca). */
    public List<String> nombresDisponibles() {
        return List.of("indicadores_globales", "distribucion_areas_interes",
                "distribucion_financiamiento", "distribucion_situacion_laboral",
                "distribucion_genero", "distribucion_institucion_posgrado",
                "distribucion_sector_empleadores", "distribucion_tipo_organizacion",
                "distribucion_tamano_organizacion",
                "distribucion_posibilidad_contratacion",
                "matriz_contingencia_area_x_financiamiento",
                "matriz_contingencia_financiamiento_x_nivel",
                "matriz_contingencia_nivel_x_institucion",
                "likert_empleadores", "segmentos_antiguedad",
                "histograma_antiguedad", "competencias_mas_mencionadas");
    }

    /**
     * generar(): produce el CSV pedido por su nombre. Devuelve null si el
     * nombre no existe (así el controlador responde 404 y no se expone nada
     * fuera de la lista).
     */
    public String generar(String nombre) {
        switch (nombre) {
            case "indicadores_globales": return indicadoresGlobales();
            case "distribucion_areas_interes": return distribucionTitulados("area_interes_posgrado");
            case "distribucion_financiamiento": return distribucionTitulados("financiamiento_formacion");
            case "distribucion_situacion_laboral": return distribucionTitulados("situacion_laboral_actual");
            case "distribucion_genero": return distribucionTitulados("genero");
            case "distribucion_institucion_posgrado": return distribucionTitulados("institucion_futura_posgrado");
            case "distribucion_sector_empleadores": return distribucionEmpleadores("sector_rubro");
            case "distribucion_tipo_organizacion": return distribucionEmpleadores("tipo_organizacion");
            case "distribucion_tamano_organizacion": return distribucionEmpleadores("tamano_organizacion");
            case "distribucion_posibilidad_contratacion": return distribucionEmpleadores("posibilidad_futura_contratacion");
            case "matriz_contingencia_area_x_financiamiento":
                return matriz("area_interes_posgrado", "financiamiento_formacion");
            case "matriz_contingencia_financiamiento_x_nivel":
                return matriz("financiamiento_formacion", "nivel_posgrado");
            case "matriz_contingencia_nivel_x_institucion":
                return matriz("nivel_posgrado", "institucion_formacion");
            case "likert_empleadores": return likertEmpleadores();
            case "segmentos_antiguedad": return segmentos();
            case "histograma_antiguedad": return histogramaAntiguedad();
            case "competencias_mas_mencionadas": return competencias();
            default: return null;
        }
    }

    // ---- Indicadores globales (resumen para el comite) ----
    private String indicadoresGlobales() {
        List<Map<String, String>> titulados = analisis.mapasTitulados();
        List<Map<String, String>> empleadores = analisis.mapasEmpleadores();
        int totalTitulados = titulados.size();
        List<Double> antiguedades = analisis.numerosDe(titulados, "anios_vida_profesional");
        double mediaAntiguedad = antiguedades.isEmpty() ? 0
                : Numeros.promedio(antiguedades);
        int conFormacion = 0;
        for (Map<String, String> t : titulados) {
            if ("SI".equals(t.get("realizo_formacion_complementaria"))) {
                conFormacion++;
            }
        }
        int publicas = 0;
        int privadas = 0;
        java.util.Set<String> empresas = new java.util.LinkedHashSet<>();
        for (Map<String, String> e : empleadores) {
            String nombre = e.getOrDefault("nombre_empresa", "").trim();
            if (!nombre.isEmpty()) {
                empresas.add(nombre);
            }
            String tipo = e.getOrDefault("tipo_organizacion", "");
            if (tipo.contains("PUBLICO")) {
                publicas++;
            }
            if (tipo.contains("PRIVADO")) {
                privadas++;
            }
        }
        List<List<String>> filas = new ArrayList<>();
        filas.add(List.of("titulados_encuestados", String.valueOf(totalTitulados)));
        filas.add(List.of("empleadores_encuestados", String.valueOf(empleadores.size())));
        filas.add(List.of("antiguedad_media_anios", String.valueOf(redondear(mediaAntiguedad, 2))));
        filas.add(List.of("tasa_insercion_pct", String.valueOf(redondear(analisis.tasaInsercion() * 100, 1))));
        filas.add(List.of("intencion_posgrado_pct", String.valueOf(redondear(analisis.intencionPosgrado() * 100, 1))));
        filas.add(List.of("requiere_beca_pct", String.valueOf(redondear(analisis.proporcionRequiereBeca() * 100, 1))));
        filas.add(List.of("formacion_complementaria_pct", String.valueOf(redondear(100.0 * conFormacion / Math.max(1, totalTitulados), 1))));
        filas.add(List.of("empresas_distintas", String.valueOf(empresas.size())));
        filas.add(List.of("empresas_publicas", String.valueOf(publicas)));
        filas.add(List.of("empresas_privadas", String.valueOf(privadas)));
        return csv(List.of("indicador", "valor"), filas);
    }

    // ---- Distribucion de frecuencia de una columna de TITULADOS ----
    private String distribucionTitulados(String columna) {
        return distribucion(analisis.columnaDe(analisis.mapasTitulados(), columna));
    }

    // ---- Distribucion de frecuencia de una columna de EMPLEADORES ----
    private String distribucionEmpleadores(String columna) {
        return distribucion(analisis.columnaDe(analisis.mapasEmpleadores(), columna));
    }

    // ---- Distribucion de frecuencia de una columna categorica ----
    private String distribucion(List<String> valores) {
        Map<String, Integer> conteo = Frecuencias.contar(valores);
        int total = conteo.values().stream().mapToInt(Integer::intValue).sum();
        List<List<String>> filas = new ArrayList<>();
        for (Map.Entry<String, Integer> entrada : conteo.entrySet()) {
            filas.add(List.of(entrada.getKey(), String.valueOf(entrada.getValue()),
                    String.valueOf(redondear(100.0 * entrada.getValue() / Math.max(1, total), 1))));
        }
        return csv(List.of("categoria", "frecuencia_absoluta", "frecuencia_relativa_pct"), filas);
    }

    // ---- Matriz de contingencia con chi-cuadrada (HU3 y HU5) ----
    private String matriz(String columnaA, String columnaB) {
        List<Map<String, String>> filas = analisis.mapasTitulados();
        ResultadoChiCuadrada resultado = PruebaChiCuadrada.cruzar(
                analisis.columnaDe(filas, columnaA),
                analisis.columnaDe(filas, columnaB));
        List<String> encabezados = new ArrayList<>();
        encabezados.add(columnaA + " \\ " + columnaB);
        encabezados.addAll(resultado.columnas());
        encabezados.add("TOTAL");
        List<List<String>> cuerpo = new ArrayList<>();
        for (int f = 0; f < resultado.filas().size(); f++) {
            List<String> fila = new ArrayList<>();
            fila.add(resultado.filas().get(f));
            int totalFila = 0;
            for (int c = 0; c < resultado.columnas().size(); c++) {
                int valor = resultado.observados()[f][c];
                fila.add(String.valueOf(valor));
                totalFila += valor;
            }
            fila.add(String.valueOf(totalFila));
            cuerpo.add(fila);
        }
        List<String> filaTotal = new ArrayList<>();
        filaTotal.add("TOTAL");
        int totalGeneral = 0;
        for (int c = 0; c < resultado.columnas().size(); c++) {
            int suma = 0;
            for (int f = 0; f < resultado.filas().size(); f++) {
                suma += resultado.observados()[f][c];
            }
            filaTotal.add(String.valueOf(suma));
            totalGeneral += suma;
        }
        filaTotal.add(String.valueOf(totalGeneral));
        cuerpo.add(filaTotal);
        // Al pie, el estadistico y su lectura (lo que el evaluador necesita).
        cuerpo.add(List.of("chi2", String.valueOf(redondear(resultado.chi2(), 4))));
        cuerpo.add(List.of("grados_de_libertad", String.valueOf(resultado.gradosLibertad())));
        cuerpo.add(List.of("valor_p", String.valueOf(redondear(resultado.valorP(), 4))));
        cuerpo.add(List.of("veredicto", resultado.hayDependencia()
                ? "SI hay relacion (p<0.05)" : "NO hay relacion (p>=0.05)"));
        return csv(encabezados, cuerpo);
    }

    // ---- Likert de empleadores (HU4) ----
    private String likertEmpleadores() {
        List<Map<String, String>> empleadores = analisis.mapasEmpleadores();
        List<List<String>> filas = new ArrayList<>();
        for (String item : ITEMS_LIKERT) {
            List<Double> puntajes = analisis.likertDe(empleadores, item);
            if (puntajes.isEmpty()) {
                continue;
            }
            long deAcuerdo = puntajes.stream().filter(p -> p >= 3).count();
            filas.add(List.of(item, String.valueOf(puntajes.size()),
                    String.valueOf(redondear(Numeros.promedio(puntajes), 2)),
                    String.valueOf(redondear(100.0 * deAcuerdo / puntajes.size(), 1))));
        }
        return csv(List.of("item", "n", "media_escala_1_4", "porcentaje_de_acuerdo"), filas);
    }

    // ---- Segmentacion por antiguedad (HU1) ----
    private String segmentos() {
        List<List<String>> filas = new ArrayList<>();
        for (FichaSegmento ficha : analisis.fichasSegmentos()) {
            filas.add(List.of(ficha.nombre(), String.valueOf(ficha.total()),
                    String.valueOf(redondear(ficha.satisfaccionMedia(), 2)),
                    String.valueOf(redondear(ficha.intencionPosgradoPct(), 1)),
                    String.valueOf(redondear(ficha.tasaDesempleo(), 2))));
        }
        return csv(List.of("segmento", "n", "satisfaccion_media_1_4",
                "intencion_posgrado_pct", "tasa_desempleo"), filas);
    }

    // ---- Histograma de antiguedad (HU1) ----
    private String histogramaAntiguedad() {
        List<Double> antiguedades = analisis.numerosDe(analisis.mapasTitulados(),
                "anios_vida_profesional");
        int[] limites = {0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 100};
        List<List<String>> filas = new ArrayList<>();
        for (int i = 0; i < limites.length - 1; i++) {
            int conteo = 0;
            for (double valor : antiguedades) {
                if (valor >= limites[i] && valor < limites[i + 1]) {
                    conteo++;
                }
            }
            filas.add(List.of("[" + limites[i] + ", " + limites[i + 1] + ")",
                    String.valueOf(conteo),
                    String.valueOf(redondear(100.0 * conteo / Math.max(1, antiguedades.size()), 1))));
        }
        return csv(List.of("intervalo_anios", "frecuencia", "porcentaje"), filas);
    }

    // ---- Competencias mas mencionadas en el texto libre de los empleadores ----
    private String competencias() {
        List<List<String>> filas = new ArrayList<>();
        for (Map.Entry<String, Integer> entrada : analisis.competenciasMencionadas(30)) {
            filas.add(List.of(entrada.getKey(), String.valueOf(entrada.getValue())));
        }
        return csv(List.of("termino", "menciones"), filas);
    }

    // ---- Utilidades ----

    /** csv(): arma el texto CSV completo con encabezados y filas. */
    private String csv(List<String> encabezados, List<List<String>> filas) {
        ByteArrayOutputStream memoria = new ByteArrayOutputStream();
        try {
            LectorCsv.escribir(memoria, encabezados, filas);
        } catch (Exception errorEscritura) {
            throw new IllegalStateException("No se pudo generar el CSV", errorEscritura);
        }
        return memoria.toString(java.nio.charset.StandardCharsets.UTF_8);
    }

    /** redondear(): double -> texto con los decimales pedidos. */
    private String redondear(double valor, int decimales) {
        if (Double.isNaN(valor)) {
            return "NA";
        }
        double factor = Math.pow(10, decimales);
        return String.valueOf(Math.round(valor * factor) / factor);
    }
}
