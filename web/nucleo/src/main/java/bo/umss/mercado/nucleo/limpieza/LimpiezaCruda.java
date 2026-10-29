package bo.umss.mercado.nucleo.limpieza;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * LimpiezaCruda: convierte un CSV EXPORTADO DIRECTAMENTE de Microsoft Forms
 * (los "CSV crudos" con 57/32 columnas y encabezados largos) al formato
 * normalizado que usa la aplicacion (35/24 columnas con codigos estables).
 *
 * Por que por indices y no por nombre de columna: los encabezados del Forms
 * tienen caracteres rotos (�) y espacios al inicio/final; los indices
 * verificados del Excel son estables y no dependen de la codificacion.
 *
 * Detecta el tipo de encuesta por el numero de columnas:
 *   - >= 50 columnas -> titulados (encuesta de 36 preguntas)
 *   - ~32 columnas  -> empleadores (encuesta de 22 preguntas)
 */
public class LimpiezaCruda {

    // Indices verificados del CSV crudo de TITULADOS (0-based).
    private static final int T_ID = 0;
    private static final int T_EDAD = 8;
    private static final int T_GENERO = 9;
    private static final int T_FORM_COMP = 10;
    private static final int T_NIVEL = 11;
    private static final int T_INST_FORM = 12;
    private static final int T_FIN_FORM = 13;
    private static final int T_NOMBRE_PROG = 14;
    private static final int T_INTERES = 15;
    private static final int T_AREA = 16;
    private static final int T_INST_FUT = 17;
    private static final int T_OPINION = 18;
    private static final int T_ANIOS_VIDA = 19;
    private static final int T_ANIOS_DESEMP = 20;
    private static final int T_SITUACION = 21;
    private static final int T_ORIGEN_EMP = 35;
    private static final int T_TIPO_ENTREG = 36;
    private static final int T_FIN_NEG = 37;
    private static final int T_SAT_NEG = 38;
    private static final int T_IMP_NEG = 39;
    private static final int T_PRIMER_EMP = 40;
    private static final int T_TIEMPO_PRIMER = 43;
    private static final int T_NUM_EMPLEOS = 44;
    private static final int T_SAT_FORM = 45;
    private static final int T_CONCORD = 46;
    private static final int T_ASPECTOS_UTILES = 47;
    private static final int T_ASPECTOS_MEJORAR = 48;
    private static final int T_ASIG_VENT = 49;
    private static final int T_OTRA_VENT = 50;
    private static final int T_ASIG_INUT = 51;
    private static final int T_OTRA_INUT = 52;
    private static final int T_RED = 53;
    private static final int T_EMAIL = 54;
    private static final int T_TELEFONO = 55;
    private static final int T_LINKEDIN = 56;

    // Indices verificados del CSV crudo de EMPLEADORES (0-based).
    private static final int E_CORREO = 1;
    private static final int E_NOMBRE = 2;
    private static final int E_TIPO = 3;
    private static final int E_TAMANO = 4;
    private static final int E_SECTOR = 5;
    private static final int E_CONTRATO = 6;
    private static final int E_POSIBILIDAD = 7;
    private static final int E_NIVEL_DEM = 8;
    private static final int E_MEDIO = 9;
    private static final int E_CARGOS = 10;
    private static final int E_AREAS = 11;
    private static final int E_HERR = 12;
    private static final int E_HAB = 13;
    private static final int E_LIKERT_INICIO = 14; // 10 items hasta 23

    // ----------------------------------------------------------------
    // Publicos: detectan el tipo y convierten.
    // ----------------------------------------------------------------
    public static boolean esTitulados(List<Map<String, String>> crudo) {
        if (crudo.isEmpty()) return false;
        return crudo.get(0).size() >= 50;
    }

    public static boolean esEmpleadores(List<Map<String, String>> crudo) {
        if (crudo.isEmpty()) return false;
        int cols = crudo.get(0).size();
        return cols >= 28 && cols <= 35;
    }

    public static List<Map<String, String>> convertirTitulados(List<Map<String, String>> crudo) {
        List<Map<String, String>> salida = new ArrayList<>();
        for (Map<String, String> fila : crudo) {
            List<String> c = filaALista(fila);
            Map<String, String> m = new LinkedHashMap<>();
            m.put("id_titulado", "T" + String.format("%03d", salida.size() + 1));
            m.put("edad", edadANumero(obtener(c, T_EDAD)));
            m.put("genero", normalizarTexto(obtener(c, T_GENERO)));
            m.put("realizo_formacion_complementaria", siNo(obtener(c, T_FORM_COMP)));
            m.put("nivel_posgrado", nivelPosgrado(obtener(c, T_NIVEL)));
            m.put("institucion_formacion", institucion(obtener(c, T_INST_FORM)));
            m.put("financiamiento_formacion", financiamiento(obtener(c, T_FIN_FORM)));
            m.put("nombre_programa", textoLibre(obtener(c, T_NOMBRE_PROG)));
            m.put("interes_posgrado", siNo(obtener(c, T_INTERES)));
            m.put("area_interes_posgrado", area(obtener(c, T_AREA)));
            m.put("institucion_futura_posgrado", institucion(obtener(c, T_INST_FUT)));
            m.put("opinion_posgrado_fcyt", likert4(obtener(c, T_OPINION)));
            m.put("anios_vida_profesional", aniosANumero(obtener(c, T_ANIOS_VIDA)));
            m.put("anios_desempleado", aniosADesempleado(obtener(c, T_ANIOS_DESEMP)));
            m.put("situacion_laboral_actual", situacionLaboral(obtener(c, T_SITUACION)));
            m.put("origen_emprendimiento", origenEmprendimiento(obtener(c, T_ORIGEN_EMP)));
            m.put("tipo_entregable_negocio", entregable(obtener(c, T_TIPO_ENTREG)));
            m.put("financiamiento_negocio", financiamientoNegocio(obtener(c, T_FIN_NEG)));
            m.put("satisfaccion_negocio", satisfaccionNegocio(obtener(c, T_SAT_NEG)));
            m.put("importancia_formacion_negocio", importanciaNegocio(obtener(c, T_IMP_NEG)));
            m.put("primer_empleo_actual", siNo(obtener(c, T_PRIMER_EMP)));
            m.put("tiempo_primer_empleo", tiempoPrimerEmpleo(obtener(c, T_TIEMPO_PRIMER)));
            m.put("num_empleos_desde_titulacion", numEmpleos(obtener(c, T_NUM_EMPLEOS)));
            m.put("satisfaccion_formacion", likert4(obtener(c, T_SAT_FORM)));
            m.put("concordancia_formacion_requerimientos", likert4(obtener(c, T_CONCORD)));
            m.put("aspectos_utiles", multi(obtener(c, T_ASPECTOS_UTILES)));
            m.put("aspectos_mejorar", multi(obtener(c, T_ASPECTOS_MEJORAR)));
            m.put("asignaturas_ventajosas", multi(obtener(c, T_ASIG_VENT)));
            m.put("asignaturas_inutiles", multi(obtener(c, T_ASIG_INUT)));
            m.put("asignatura_ventajosa_otra", textoLibre(obtener(c, T_OTRA_VENT)));
            m.put("asignatura_inutil_otra", textoLibre(obtener(c, T_OTRA_INUT)));
            m.put("red_contactos", siNo(obtener(c, T_RED)));
            m.put("email_contacto", textoLibre(obtener(c, T_EMAIL)));
            m.put("telefono_contacto", textoLibre(obtener(c, T_TELEFONO)));
            m.put("linkedin_contacto", textoLibre(obtener(c, T_LINKEDIN)));
            // Limpieza final (nulos, numeros invalidos, SI/NO unificado)
            LimpiezaDatos.limpiarFila(m);
            salida.add(m);
        }
        return salida;
    }

    public static List<Map<String, String>> convertirEmpleadores(List<Map<String, String>> crudo) {
        List<Map<String, String>> salida = new ArrayList<>();
        for (Map<String, String> fila : crudo) {
            List<String> c = filaALista(fila);
            Map<String, String> m = new LinkedHashMap<>();
            m.put("id_empleador", "E" + String.format("%03d", salida.size() + 1));
            m.put("correo", textoLibre(obtener(c, E_CORREO)));
            m.put("nombre_empresa", normalizarTexto(obtener(c, E_NOMBRE)));
            m.put("tipo_organizacion", tipoOrg(obtener(c, E_TIPO)));
            m.put("tamano_organizacion", tamano(obtener(c, E_TAMANO)));
            m.put("sector_rubro", sector(obtener(c, E_SECTOR)));
            m.put("contrato_ultimos_5_anios", siNo(obtener(c, E_CONTRATO)));
            m.put("posibilidad_futura_contratacion", posibilidad(obtener(c, E_POSIBILIDAD)));
            m.put("nivel_formacion_demanda", nivelDemanda(obtener(c, E_NIVEL_DEM)));
            m.put("medio_convocatoria", medio(obtener(c, E_MEDIO)));
            m.put("cargos_desempenados", cargos(obtener(c, E_CARGOS)));
            m.put("areas_conocimiento_emergentes", textoLibre(obtener(c, E_AREAS)));
            m.put("herramientas_tecnologicas", textoLibre(obtener(c, E_HERR)));
            m.put("habilidades_competencias_requeridas", textoLibre(obtener(c, E_HAB)));
            for (int i = 0; i < 10; i++) {
                String item = LIKERT_ITEMS[i];
                m.put(item, likert4(obtener(c, E_LIKERT_INICIO + i)));
            }
            LimpiezaDatos.limpiarFila(m);
            salida.add(m);
        }
        return salida;
    }

    // ----------------------------------------------------------------
    // Helpers: lectura segura por indice.
    // ----------------------------------------------------------------
    private static List<String> filaALista(Map<String, String> fila) {
        List<String> out = new ArrayList<>();
        for (String v : fila.values()) {
            out.add(v == null ? "" : v);
        }
        return out;
    }

    private static String obtener(List<String> fila, int idx) {
        if (idx < 0 || idx >= fila.size()) return "";
        String v = fila.get(idx);
        return v == null ? "" : v;
    }

    private static String normalizarTexto(String v) {
        return v == null ? "" : v.trim().toUpperCase()
                .replace("Á", "A").replace("É", "E").replace("Í", "I")
                .replace("Ó", "O").replace("Ú", "U").replace("Ü", "U")
                .replace("Ñ", "N");
    }

    private static String siNo(String v) {
        String s = normalizarTexto(v);
        if (s.startsWith("SI")) return "SI";
        if (s.startsWith("NO")) return "NO";
        return "";
    }

    private static String textoLibre(String v) {
        return v == null ? "" : v.trim();
    }

    private static String multi(String v) {
        if (v == null || v.trim().isEmpty()) return "";
        String[] partes = v.split(";");
        List<String> out = new ArrayList<>();
        for (String p : partes) {
            p = p.trim();
            if (!p.isEmpty()) out.add(p);
        }
        return String.join("|", out);
    }

    // ----------------------------------------------------------------
    // Transformaciones de TITULADOS.
    // ----------------------------------------------------------------
    private static String edadANumero(String v) {
        String s = normalizarTexto(v);
        if (s.isEmpty()) return "";
        String[] nums = s.replaceAll("[^0-9]", " ").trim().split("\\s+");
        if (nums.length >= 2) {
            try {
                return String.valueOf(Math.round((Integer.parseInt(nums[0])
                        + Integer.parseInt(nums[1])) / 2.0));
            } catch (NumberFormatException e) {
                return "";
            }
        }
        if (nums.length == 1 && !nums[0].isEmpty()) return nums[0];
        return "";
    }

    private static String aniosANumero(String v) {
        String s = normalizarTexto(v);
        if (s.isEmpty()) return "";
        if (s.contains("NINGUNO") || s.contains("SIEMPRE")) return "0";
        String[] nums = s.replaceAll("[^0-9]", " ").trim().split("\\s+");
        if (nums.length >= 1 && !nums[0].isEmpty()) return nums[0];
        return "";
    }

    private static String aniosADesempleado(String v) {
        String s = normalizarTexto(v);
        if (s.isEmpty()) return "";
        if (s.contains("NINGUNO") || s.contains("SIEMPRE")) return "0";
        if (s.contains("MES")) {
            String[] nums = s.replaceAll("[^0-9]", " ").trim().split("\\s+");
            if (nums.length >= 1 && !nums[0].isEmpty()) {
                try {
                    return String.valueOf(Math.round(Integer.parseInt(nums[0]) / 12.0 * 100.0) / 100.0);
                } catch (NumberFormatException e) {
                    return "0.5";
                }
            }
            return "0.5";
        }
        String[] nums = s.replaceAll("[^0-9]", " ").trim().split("\\s+");
        if (nums.length >= 1 && !nums[0].isEmpty()) return nums[0];
        return "";
    }

    private static String nivelPosgrado(String v) {
        String s = normalizarTexto(v);
        if (s.contains("DIPLOMADO")) return "DIPLOMADO";
        if (s.contains("ESPECIALIDAD")) return "ESPECIALIDAD";
        if (s.contains("MAESTR") || s.contains("MASTER")) return "MAESTRIA";
        if (s.contains("DOCTOR")) return "DOCTORADO";
        if (s.contains("POSDOCTOR")) return "POSDOCTORADO";
        return "";
    }

    private static String institucion(String v) {
        String s = normalizarTexto(v);
        if (s.contains("EUPG") || s.contains("ESCUELA UNIVERSITARIA")) return "EUPG";
        if (s.contains("FCYT") || s.contains("DIR. POSGRADO")) return "DIR_POSGRADO_FCyT";
        if (s.contains("IBNORCA")) return "IBNORCA";
        if (s.contains("INTERNACIONAL")) return "UNIV_INTERNACIONALES";
        if (s.contains("PUBLICAS NACIONALES")) return "UNIV_PUBLICAS_NAC";
        if (s.contains("PRIVADAS NACIONALES")) return "UNIV_PRIVADAS_NAC";
        if (s.contains("OTRO")) return "OTRO";
        return "";
    }

    private static String financiamiento(String v) {
        String s = normalizarTexto(v);
        if (s.contains("BECA TOTAL") || s.contains("TOTAL FINANCIAMIENTO")) return "BECA_TOTAL";
        if (s.contains("PARCIALMENTE")) return "BECA_PARCIAL";
        if (s.contains("RECURSOS PROPIOS")) return "RECURSOS_PROPIOS";
        return "";
    }

    private static String area(String v) {
        String s = normalizarTexto(v);
        if (s.equals("IA")) return "IA";
        if (s.contains("ROBOT")) return "ROBOTICA";
        if (s.contains("SOFTWARE")) return "ING_SOFTWARE";
        if (s.contains("BASE DE DATOS")) return "BASE_DATOS";
        if (s.contains("REDES")) return "REDES_SEGURIDAD";
        return "";
    }

    private static String likert4(String v) {
        String s = normalizarTexto(v);
        if (s.contains("TOTALMENTE DE ACUERDO")) return "TOTALMENTE_ACUERDO";
        if (s.contains("TOTALMENTE DESACUERDO")) return "TOTALMENTE_DESACUERDO";
        if (s.contains("TOTALMENTE DESACUERDO")) return "TOTALMENTE_DESACUERDO";
        if (s.contains("DE ACUERDO")) return "ACUERDO";
        if (s.contains("DESACUERDO")) return "DESACUERDO";
        return "";
    }

    private static String satisfaccionNegocio(String v) {
        String s = normalizarTexto(v);
        if (s.contains("ALGO SATISFECHO")) return "ALGO_SATISFECHO";
        if (s.contains("SATISFECHO")) return "SATISFECHO";
        if (s.contains("INSATISFECHO")) return "INSATISFECHO";
        return "";
    }

    private static String importanciaNegocio(String v) {
        String s = normalizarTexto(v);
        if (s.contains("MUY IMPORTANTE")) return "MUY_IMPORTANTE";
        if (s.contains("POCO IMPORTANTE")) return "POCO_IMPORTANTE";
        if (s.contains("NO IMPORTANTE")) return "NO_IMPORTANTE";
        if (s.contains("IMPORTANTE")) return "IMPORTANTE";
        return "";
    }

    private static String situacionLaboral(String v) {
        String s = normalizarTexto(v);
        if (s.contains("EMPRENDIMIENTO")) return "EMPRENDIMIENTO";
        if (s.contains("NO TRABAJO") || s.contains("NO TRABAJA")) return "NO_TRABAJA";
        if (s.contains("TRABAJANDO") || s.contains("TRABAJA")) return "TRABAJA_ORG";
        return "";
    }

    private static String origenEmprendimiento(String v) {
        String s = normalizarTexto(v);
        if (s.contains("PROPIA")) return "IDEA_PROPIA";
        if (s.contains("CONJUNTA")) return "IDEA_CONJUNTA";
        if (s.contains("STARTUP")) return "STARTUP";
        if (s.contains("SPIN")) return "SPINOFF";
        if (s.contains("OTRO")) return "OTRO";
        return "";
    }

    private static String entregable(String v) {
        String s = normalizarTexto(v);
        if (s.contains("AMBOS")) return "AMBOS";
        if (s.contains("PRODUCTO")) return "PRODUCTO";
        if (s.contains("SERVICIO")) return "SERVICIO";
        return "";
    }

    private static String financiamientoNegocio(String v) {
        String s = normalizarTexto(v);
        if (s.contains("BANCARIO") || s.contains("PRESTAMO")) return "PRESTAMO_BANCARIO";
        if (s.contains("INVERSORES")) return "INVERSORES";
        if (s.contains("PROPIO")) return "PROPIO";
        return "";
    }

    private static String tiempoPrimerEmpleo(String v) {
        String s = normalizarTexto(v);
        if (s.contains("YA TRABAJABA")) return "YA_TRABAJABA";
        if (s.contains("MENOS DE 1")) return "MENOS_1_MES";
        if (s.contains("1 - 4")) return "1_4_MESES";
        if (s.contains("4 - 8")) return "4_8_MESES";
        if (s.contains("8 - 12")) return "8_12_MESES";
        if (s.contains("MAS DE 12")) return "MAS_12_MESES";
        return "";
    }

    private static String numEmpleos(String v) {
        String s = normalizarTexto(v);
        if (s.contains("MAS DE 3")) return "MAS_DE_3";
        String[] nums = s.replaceAll("[^0-9]", " ").trim().split("\\s+");
        if (nums.length >= 1 && !nums[0].isEmpty()) return nums[0];
        return "";
    }

    // ----------------------------------------------------------------
    // Transformaciones de EMPLEADORES.
    // ----------------------------------------------------------------
    private static final String[] LIKERT_ITEMS = {
            "likert_confianza_formadora", "likert_consistencia_titulo",
            "likert_consulta_regular", "likert_conoce_perfil",
            "likert_coherencia_perfil", "likert_formacion_desempeno",
            "likert_considera_opinion", "likert_competencias_laborales",
            "likert_valores_actitudes", "likert_participacion_procesos"};

    private static String tipoOrg(String v) {
        String s = normalizarTexto(v);
        if (s.contains("PUBLICO")) return "PUBLICO";
        if (s.contains("PRIVADO")) return "PRIVADO";
        return "";
    }

    private static String tamano(String v) {
        String s = normalizarTexto(v);
        if (s.contains("GRANDE")) return "GRANDE";
        if (s.contains("MEDIANA")) return "MEDIANA";
        if (s.contains("PEQUENA")) return "PEQUENA";
        if (s.contains("MICRO")) return "MICRO";
        return "";
    }

    private static String sector(String v) {
        String s = normalizarTexto(v);
        if (s.contains("DESARROLLO")) return "DESARROLLO_SOFTWARE";
        if (s.contains("SERVICIOS") && s.contains("TI")) return "SERVICIOS_TI";
        if (s.contains("TELECOM") || s.contains("REDES")) return "TELECOM_REDES";
        if (s.contains("SOPORTE")) return "SOPORTE_TECNICO";
        if (s.contains("CONSULTORIA")) return "CONSULTORIA";
        if (s.contains("BANCA") || s.contains("FINANCIERO")) return "BANCA_FINANCIERO";
        if (s.contains("PUBLICAS") || s.contains("GOBIERNO")) return "INSTITUCIONES_PUBLICAS";
        if (s.contains("EDUCACION")) return "EDUCACION_INVESTIGACION";
        if (s.contains("INDUSTRIA") || s.contains("MANUFACTURA")) return "INDUSTRIA_MANUFACTURA";
        if (s.contains("COMERCIO")) return "COMERCIO_ELECTRONICO";
        if (s.contains("SALUD")) return "SALUD";
        if (s.contains("SEGURIDAD") || s.contains("CIBER")) return "SEGURIDAD_INFORMATICA";
        if (s.contains("DATOS") || s.contains("INTELIGENCIA")) return "ANALISIS_DATOS_BI";
        if (s.contains("STARTUP")) return "STARTUPS";
        return "OTROS";
    }

    private static String posibilidad(String v) {
        String s = normalizarTexto(v);
        if (s.contains("ALTA")) return "ALTA_PROBABILIDAD";
        if (s.contains("DEPEND")) return "DEPENDE_CRECIMIENTO";
        if (s.contains("PREVISTA")) return "NO_PREVISTA";
        if (s.contains("REQUIERE")) return "NO_REQUIERE";
        return "";
    }

    private static String nivelDemanda(String v) {
        String s = normalizarTexto(v);
        if (s.contains("DOCTOR")) return "DOCTORADO";
        if (s.contains("POST")) return "POSTDOCTORADO";
        if (s.contains("MAESTR")) return "MAESTRIA";
        if (s.contains("ESPECIAL")) return "ESPECIALIDAD";
        if (s.contains("DIPLOM")) return "DIPLOMADO";
        if (s.contains("LICEN")) return "LICENCIATURA";
        return "";
    }

    private static String medio(String v) {
        String s = normalizarTexto(v);
        List<String> partes = new ArrayList<>();
        if (s.contains("INTERNET") || s.contains("REDES")) partes.add("INTERNET_REDES");
        if (s.contains("ESCRITOS") || s.contains("AUDIOVISUALES")) partes.add("MEDIOS_ESCRITOS_AUDIOVISUALES");
        if (s.contains("POSTULACI")) partes.add("POSTULACION_COMPETITIVA");
        if (s.contains("OFERTA")) partes.add("OFERTA_PROFESIONAL");
        if (s.contains("RECOMENDACI")) partes.add("RECOMENDACION_TERCEROS");
        if (s.contains("OTROS")) partes.add("OTROS");
        return String.join("|", partes);
    }

    private static String cargos(String v) {
        String s = normalizarTexto(v);
        List<String> partes = new ArrayList<>();
        if (s.contains("APOYO") || s.contains("ASISTENCIA")) partes.add("APOYO_ASISTENCIA");
        if (s.contains("TECNICO")) partes.add("TECNICO");
        if (s.contains("ANALISTA")) partes.add("ANALISTA");
        if (s.contains("ESPECIALISTA")) partes.add("ESPECIALISTA");
        if (s.contains("SUPERVISOR") || s.contains("COORDINADOR")) partes.add("SUPERVISOR");
        if (s.contains("JEFATURA") || s.contains("GERENCIAL")) partes.add("JEFATURA_GERENCIAL");
        if (s.contains("CONSULTOR")) partes.add("CONSULTOR");
        if (s.contains("OTRO")) partes.add("OTRO");
        return String.join("|", partes);
    }
}
