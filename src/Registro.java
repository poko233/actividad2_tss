import java.nio.file.*;
import java.util.*;

// ============================================================================
// Registro — HERRAMIENTA DE RECOLECCIÓN DE DATOS (producto final §4 guía).
// ----------------------------------------------------------------------------
// QUÉ ES: formulario por consola que captura encuestas LIMPIAS desde el
// origen, aplicando las "implementaciones técnicas sugeridas" de HU1–HU4:
//   HU1: año/antigüedad como NÚMERO validado (no texto libre).
//   HU1–HU3: menús cerrados numerados (dropdowns) en vez de texto libre,
//            así las categorías salen normalizadas y los cruces χ² no se
//            fragmentan ("IA" vs "ia" vs "I.A." dejarían de cuadrar).
//   HU4: Likert presentado como escala 1-4/1-5 cerrada.
//   HU5: lo registrado cae directo al CSV que lee el Analizador (trazable).
// USO: java -cp bin Registro [titulados.csv] [empleadores.csv]
// Los bloques condicionales de la encuesta se respetan: ej. si NO hizo
// formación complementaria, no se pregunta nivel/institución (celda vacía).
// ============================================================================
public class Registro {

    // Lector único de teclado para todo el programa (se cierra al salir).
    static Scanner sc = new Scanner(System.in);

    // Vocabularios CERRADOS = los mismos valores que espera el Analizador.
    // (Cambiar un texto aquí y el cruce correspondiente deja de agrupar.)
    static final String[] GENERO = {"MASCULINO", "FEMENINO", "OTRO"};
    static final String[] SINO = {"SI", "NO"};
    static final String[] NIVEL = {"DIPLOMADO", "ESPECIALIDAD", "MAESTRIA", "DOCTORADO", "POSDOCTORADO"};
    static final String[] INST = {"EUPG", "DIR_POSGRADO_FCyT", "IBNORCA", "UNIV_INTERNACIONALES",
            "UNIV_PUBLICAS_NAC", "UNIV_PRIVADAS_NAC", "OTRO"};
    static final String[] FIN = {"RECURSOS_PROPIOS", "BECA_TOTAL", "BECA_PARCIAL"};
    static final String[] AREA = {"IA", "ROBOTICA", "ING_SOFTWARE", "BASE_DATOS", "REDES_SEGURIDAD"};
    static final String[] LIK4 = {"TOTALMENTE_DESACUERDO", "DESACUERDO", "ACUERDO", "TOTALMENTE_ACUERDO"};
    static final String[] SIT = {"TRABAJA_ORG", "NO_TRABAJA", "EMPRENDIMIENTO"};
    static final String[] TPE = {"YA_TRABAJABA", "MENOS_1_MES", "1_4_MESES", "4_8_MESES", "8_12_MESES", "MAS_12_MESES"};
    static final String[] NEMP = {"1", "2", "3", "MAS_DE_3"};
    static final String[] ASP = {"PRACTICAS", "PROYECTOS", "PLAN", "DOCENTES", "LABORATORIOS", "VINCULACION"};
    static final String[] ASG_V = {"INTRODUCCION", "BASE_DATOS", "INTELIGENCIA_ARTIFICIAL", "REDES",
            "ELECTIVAS", "TIS", "BASICAS", "PLANIFICACION", "REDES_AVANZADAS", "OTRA"};
    static final String[] ASG_I = {"BASICAS", "APLIC_TV_DIGITAL", "TELEFONIA_IP", "WEB_SEMANTICA",
            "GRAFICACION", "ROBOTICA", "TALLER_BAJO_NIVEL", "AUTOMATAS", "FUNCIONAL", "GRAFOS",
            "CONTABILIDAD", "OTRA"};
    static final String[] TORG = {"PUBLICO", "PRIVADO"};
    static final String[] TAM = {"GRANDE", "MEDIANA", "PEQUENA", "MICRO"};
    static final String[] SECTOR = {"DESARROLLO_SOFTWARE", "SERVICIOS_TI", "TELECOM_REDES",
            "SOPORTE_TECNICO", "CONSULTORIA", "BANCA_FINANCIERO", "INSTITUCIONES_PUBLICAS",
            "EDUCACION_INVESTIGACION", "INDUSTRIA_MANUFACTURA", "COMERCIO_ELECTRONICO", "SALUD",
            "SEGURIDAD_INFORMATICA", "ANALISIS_DATOS_BI", "STARTUPS", "OTROS"};
    static final String[] POSF = {"ALTA_PROBABILIDAD", "DEPENDE_CRECIMIENTO", "NO_PREVISTA", "NO_REQUIERE"};
    static final String[] NDEM = {"LICENCIATURA", "DIPLOMADO", "ESPECIALIDAD", "MAESTRIA", "DOCTORADO", "POSTDOCTORADO"};
    static final String[] MEDIO = {"MEDIOS_ESCRITOS_AUDIOVISUALES", "INTERNET_REDES", "POSTULACION_COMPETITIVA",
            "OFERTA_PROFESIONAL", "RECOMENDACION_TERCEROS", "OTROS"};
    static final String[] CARGO = {"APOYO_ASISTENCIA", "TECNICO", "ANALISTA", "ESPECIALISTA",
            "SUPERVISOR", "JEFATURA_GERENCIAL", "CONSULTOR", "OTRO"};
    static final String[] LIKE = {"TOTALMENTE_ACUERDO", "PARCIALMENTE_ACUERDO", "TOTALMENTE_DESACUERDO", "NO_SABE"};

    // Encabezados oficiales (mismo orden que §1.1/§1.2 de la especificación).
    static final String[] H_TIT = ("id_titulado,edad,genero,realizo_formacion_complementaria,nivel_posgrado,"
            + "institucion_formacion,financiamiento_formacion,nombre_programa,interes_posgrado,"
            + "area_interes_posgrado,institucion_futura_posgrado,opinion_posgrado_fcyt,anios_vida_profesional,"
            + "anios_desempleado,situacion_laboral_actual,origen_emprendimiento,tipo_entregable_negocio,"
            + "financiamiento_negocio,satisfaccion_negocio,importancia_formacion_negocio,primer_empleo_actual,"
            + "tiempo_primer_empleo,num_empleos_desde_titulacion,satisfaccion_formacion,"
            + "concordancia_formacion_requerimientos,aspectos_utiles,aspectos_mejorar,asignaturas_ventajosas,"
            + "asignaturas_inutiles,asignatura_ventajosa_otra,asignatura_inutil_otra,red_contactos,"
            + "email_contacto,telefono_contacto,linkedin_contacto").split(",");
    static final String[] H_EMP = ("id_empleador,correo,nombre_empresa,tipo_organizacion,tamano_organizacion,"
            + "sector_rubro,contrato_ultimos_5_anios,posibilidad_futura_contratacion,nivel_formacion_demanda,"
            + "medio_convocatoria,cargos_desempenados,areas_conocimiento_emergentes,herramientas_tecnologicas,"
            + "habilidades_competencias_requeridas,likert_confianza_formadora,likert_consistencia_titulo,"
            + "likert_consulta_regular,likert_conoce_perfil,likert_coherencia_perfil,likert_formacion_desempeno,"
            + "likert_considera_opinion,likert_competencias_laborales,likert_valores_actitudes,"
            + "likert_participacion_procesos").split(",");

    // ------------------------------------------------------------------------
    // main(): menú principal. Bucle hasta "Salir". Cada alta valida, anexa al
    // CSV y confirma; el Analizador puede correrse después sobre los mismos
    // archivos (experimentación con escenarios sobre datos frescos).
    // ------------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        String fTit = args.length > 0 ? args[0] : "titulados.csv";
        String fEmp = args.length > 1 ? args[1] : "empleadores.csv";
        System.out.println("=== REGISTRO DE ENCUESTAS UMSS (recoleccion de datos) ===");
        while (true) {
            System.out.println("\n1) Registrar titulado  2) Registrar empleador  3) Salir");
            String op = sc.nextLine().trim();
            if (op.equals("1")) registrarTitulado(fTit);
            else if (op.equals("2")) registrarEmpleador(fEmp);
            else if (op.equals("3")) { System.out.println("Fin de captura."); break; }
            else System.out.println("Opción inválida.");
        }
    }

    // ---- Primitivas del formulario (toda la validación vive aquí) ----

    // elegir(): menú cerrado numerado. Muestra opciones, re-pregunta hasta
    // número válido. Si oblig=false, Enter vacío devuelve "".
    static String elegir(String titulo, String[] ops, boolean oblig) {
        while (true) {
            System.out.println(titulo);
            for (int i = 0; i < ops.length; i++) System.out.println("  " + (i + 1) + ") " + ops[i]);
            System.out.print("Elija [1-" + ops.length + "]" + (oblig ? ": " : " (Enter=omitir): "));
            String l = sc.nextLine().trim();
            if (l.isEmpty() && !oblig) return "";
            try {
                int n = Integer.parseInt(l);
                if (n >= 1 && n <= ops.length) return ops[n - 1];
            } catch (NumberFormatException ignored) {}
            System.out.println("  !! valor inválido, intente de nuevo.");
        }
    }

    // multiple(): multi-selección ("1,3,5") que se guarda con separador "|",
    // el formato que el Analizador ya sabe partir en el módulo univariado.
    static String multiple(String titulo, String[] ops) {
        System.out.println(titulo + " (varios con coma, ej. 1,3 — Enter=omitir)");
        for (int i = 0; i < ops.length; i++) System.out.println("  " + (i + 1) + ") " + ops[i]);
        System.out.print("Elija: ");
        String l = sc.nextLine().trim();
        if (l.isEmpty()) return "";
        List<String> sel = new ArrayList<>();
        for (String p : l.split("[,; ]+")) {
            try {
                int n = Integer.parseInt(p.trim());
                if (n >= 1 && n <= ops.length && !sel.contains(ops[n - 1])) sel.add(ops[n - 1]);
            } catch (NumberFormatException ignored) {}
        }
        return String.join("|", sel);
    }

    // texto(): campo libre (nombres, emails, años como número se validan aparte).
    static String texto(String titulo, boolean oblig) {
        while (true) {
            System.out.print(titulo + (oblig ? " *: " : ": "));
            String l = sc.nextLine().trim();
            if (!l.isEmpty() || !oblig) return l;
            System.out.println("  !! obligatorio.");
        }
    }

    // entero(): HU1 — la antigüedad/edad entran como NÚMERO validado con rango
    // (no texto libre: "cinco" rompería la media). Re-pregunta si no es número.
    static String entero(String titulo, int min, int max) {
        while (true) {
            System.out.print(titulo + " [" + min + "-" + max + "]: ");
            try {
                int n = Integer.parseInt(sc.nextLine().trim());
                if (n >= min && n <= max) return String.valueOf(n);
            } catch (NumberFormatException ignored) {}
            System.out.println("  !! debe ser un número entre " + min + " y " + max + ".");
        }
    }

    // guardar(): anexa la fila al CSV (crea el archivo con encabezado si no
    // existe) reutilizando CsvUtil para que el formato sea idéntico al que
    // lee el Analizador. El id se auto-genera (T001... / E001...).
    static void guardar(String archivo, String[] headers, Map<String, String> fila, String prefijo) throws Exception {
        Path p = Paths.get(archivo);
        CsvUtil.Tabla t;
        if (Files.exists(p)) t = CsvUtil.leer(p);
        else { t = new CsvUtil.Tabla(); t.headers = new ArrayList<>(Arrays.asList(headers)); }
        fila.put(headers[0], prefijo + String.format("%03d", t.filas.size() + 1));
        t.filas.add(fila);
        List<List<String>> fs = new ArrayList<>();
        for (Map<String, String> m : t.filas) {
            List<String> f = new ArrayList<>();
            for (String h : t.headers) f.add(m.getOrDefault(h, ""));
            fs.add(f);
        }
        CsvUtil.escribir(p, t.headers, fs);
        System.out.println(">> Guardado como " + fila.get(headers[0]) + " en " + archivo);
    }

    // ---- Altas (reflejan la encuesta oficial pregunta por pregunta) ----

    // registrarTitulado(): recorre las 36 preguntas con sus condicionales:
    // formación (solo si SI), intención posgrado (solo si SI), bloque
    // emprendimiento (solo si EMPRENDIMIENTO). Lo no aplicable queda "".
    static void registrarTitulado(String archivo) throws Exception {
        Map<String, String> m = new HashMap<>();
        System.out.println("\n--- Nuevo titulado ---");
        m.put("edad", entero("Edad actual", 18, 100));
        m.put("genero", elegir("Género", GENERO, true));
        String fc = elegir("¿Realizó formación complementaria?", SINO, true);
        m.put("realizo_formacion_complementaria", fc);
        m.put("nivel_posgrado", fc.equals("SI") ? elegir("Mayor nivel concluido/en curso", NIVEL, true) : "");
        m.put("institucion_formacion", fc.equals("SI") ? elegir("¿Dónde la cursó?", INST, true) : "");
        m.put("financiamiento_formacion", elegir("Fuente de financiamiento", FIN, true));
        m.put("nombre_programa", texto("Nombre del programa", false));
        String ip = elegir("¿Interés en posgrado en Sistemas?", SINO, true);
        m.put("interes_posgrado", ip);
        m.put("area_interes_posgrado", ip.equals("SI") ? elegir("Área de interés", AREA, true) : "");
        m.put("institucion_futura_posgrado", ip.equals("SI") ? elegir("¿Dónde lo haría?", INST, true) : "");
        m.put("opinion_posgrado_fcyt", elegir("Opinión posgrado FCyT", LIK4, true));
        m.put("anios_vida_profesional", entero("Años de vida profesional", 0, 60));
        m.put("anios_desempleado", entero("De esos, años desempleado", 0, 60));
        String sit = elegir("Situación laboral actual", SIT, true);
        m.put("situacion_laboral_actual", sit);
        boolean emp = sit.equals("EMPRENDIMIENTO");
        m.put("origen_emprendimiento", emp ? elegir("Origen del emprendimiento",
                new String[]{"IDEA_PROPIA", "IDEA_CONJUNTA", "STARTUP", "SPINOFF", "OTRO"}, true) : "");
        m.put("tipo_entregable_negocio", emp ? elegir("Entregable", new String[]{"PRODUCTO", "SERVICIO", "AMBOS"}, true) : "");
        m.put("financiamiento_negocio", emp ? elegir("Financiamiento externo",
                new String[]{"PRESTAMO_BANCARIO", "INVERSORES", "PROPIO"}, true) : "");
        m.put("satisfaccion_negocio", emp ? elegir("Satisfacción con el negocio",
                new String[]{"INSATISFECHO", "ALGO_SATISFECHO", "SATISFECHO"}, true) : "");
        m.put("importancia_formacion_negocio", emp ? elegir("Importancia de su formación",
                new String[]{"NO_IMPORTANTE", "POCO_IMPORTANTE", "IMPORTANTE", "MUY_IMPORTANTE"}, true) : "");
        m.put("primer_empleo_actual", elegir("¿Su trabajo actual es su primer empleo?", SINO, true));
        m.put("tiempo_primer_empleo", elegir("Tiempo hasta el primer empleo", TPE, true));
        m.put("num_empleos_desde_titulacion", elegir("N° empleos desde titulación", NEMP, true));
        m.put("satisfaccion_formacion", elegir("Satisfacción con la formación", LIK4, true));
        m.put("concordancia_formacion_requerimientos", elegir("Concordancia formación-requerimientos", LIK4, true));
        m.put("aspectos_utiles", multiple("Aspectos útiles", ASP));
        m.put("aspectos_mejorar", multiple("Aspectos a mejorar", ASP));
        m.put("asignaturas_ventajosas", multiple("Asignaturas ventajosas (hasta 5)", ASG_V));
        m.put("asignaturas_inutiles", multiple("Asignaturas inútiles (hasta 2)", ASG_I));
        m.put("asignatura_ventajosa_otra", texto("Otra ventajosa (texto)", false));
        m.put("asignatura_inutil_otra", texto("Otra inútil (texto)", false));
        m.put("red_contactos", elegir("¿Unirse a la red de contactos?", SINO, true));
        m.put("email_contacto", texto("Email", false));
        m.put("telefono_contacto", texto("Teléfono/WhatsApp", false));
        m.put("linkedin_contacto", texto("LinkedIn", false));
        guardar(archivo, H_TIT, m, "T");
    }

    // registrarEmpleador(): las 22 columnas (§1.2): datos, empleabilidad
    // (incluye 2 multi-selección y 3 textos libres) y los 10 Likert
    // institucionales de escala cerrada de 4 (evidencia ARCUSUR).
    static void registrarEmpleador(String archivo) throws Exception {
        Map<String, String> m = new HashMap<>();
        System.out.println("\n--- Nuevo empleador ---");
        m.put("correo", texto("Correo", true));
        m.put("nombre_empresa", texto("Nombre empresa/institución", true));
        m.put("tipo_organizacion", elegir("Tipo", TORG, true));
        m.put("tamano_organizacion", elegir("Tamaño", TAM, true));
        m.put("sector_rubro", elegir("Rubro/sector", SECTOR, true));
        m.put("contrato_ultimos_5_anios", elegir("¿Contrató Ing. Sistemas últimos 5 años?", SINO, true));
        m.put("posibilidad_futura_contratacion", elegir("Posibilidad futura", POSF, true));
        m.put("nivel_formacion_demanda", elegir("Nivel de formación que demanda", NDEM, true));
        m.put("medio_convocatoria", multiple("Medios de convocatoria", MEDIO));
        m.put("cargos_desempenados", multiple("Cargos que desempeñan", CARGO));
        m.put("areas_conocimiento_emergentes", texto("Áreas/tec. emergentes (texto)", false));
        m.put("herramientas_tecnologicas", texto("Herramientas que debe manejar (texto)", false));
        m.put("habilidades_competencias_requeridas", texto("Habilidades/competencias (texto)", false));
        // Bloque Likert ×10: misma escala cerrada para los 10 criterios.
        String[] keys = {"likert_confianza_formadora", "likert_consistencia_titulo", "likert_consulta_regular",
                "likert_conoce_perfil", "likert_coherencia_perfil", "likert_formacion_desempeno",
                "likert_considera_opinion", "likert_competencias_laborales", "likert_valores_actitudes",
                "likert_participacion_procesos"};
        for (String k : keys) m.put(k, elegir("Likert: " + k.replace("likert_", ""), LIKE, true));
        guardar(archivo, H_EMP, m, "E");
    }
}
