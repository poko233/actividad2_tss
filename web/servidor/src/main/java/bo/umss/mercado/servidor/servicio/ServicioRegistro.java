package bo.umss.mercado.servidor.servicio;

import bo.umss.mercado.nucleo.limpieza.LimpiezaDatos;
import bo.umss.mercado.servidor.datos.Empleador;
import bo.umss.mercado.servidor.datos.RepositorioEmpleadores;
import bo.umss.mercado.servidor.datos.RepositorioTitulados;
import bo.umss.mercado.servidor.datos.Titulado;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * ServicioRegistro: punto único de escritura para las encuestas cargadas
 * a mano desde VistaRegistro. Reutiliza el pipeline de importación
 * (LimpiezaDatos + desdeMapa) para que un registro web y uno importado
 * por CSV terminen con la misma estructura. Además calcula los campos
 * derivados que el usuario no teclea: tamano_agrupado, rubro_agrupado,
 * demanda_ia, tendencias_categorias, hard_skills_categorias y
 * soft_skills_categorias en el caso del empleador.
 */
@Service
public class ServicioRegistro {

    private final RepositorioTitulados repoTitulados;
    private final RepositorioEmpleadores repoEmpleadores;

    public ServicioRegistro(RepositorioTitulados repoTitulados,
            RepositorioEmpleadores repoEmpleadores) {
        this.repoTitulados = repoTitulados;
        this.repoEmpleadores = repoEmpleadores;
    }

    // ---------------------------------------------------------------
    // TITULADO
    // ---------------------------------------------------------------
    public Titulado guardarTitulado(Map<String, String> datos) {
        if (datos == null)
            throw new IllegalArgumentException("Sin datos");
        if (esVacio(datos.get("id_titulado"))) {
            datos.put("id_titulado", generarId("T-WEB"));
        }
        LimpiezaDatos.limpiarFila(datos);
        Titulado titulado = Titulado.desdeMapa(datos);
        if (titulado.getIdTitulado().isEmpty()) {
            throw new IllegalArgumentException("El ID del titulado es obligatorio");
        }
        return repoTitulados.save(titulado);
    }

    // ---------------------------------------------------------------
    // EMPLEADOR
    // ---------------------------------------------------------------
    public Empleador guardarEmpleador(Map<String, String> datos) {
        if (datos == null)
            throw new IllegalArgumentException("Sin datos");
        if (esVacio(datos.get("id_empleador"))) {
            datos.put("id_empleador", generarId("E-WEB"));
        }
        LimpiezaDatos.limpiarFila(datos);

        // Derivadas calculadas en base al contenido (mismas reglas que LimpiezaCruda)
        datos.put("tamano_agrupado", tamanoAgrupado(datos.get("tamano_organizacion")));
        datos.put("rubro_agrupado", rubroAgrupado(datos.get("sector_rubro")));
        datos.put("demanda_ia", demandaIA(datos.get("areas_conocimiento_emergentes")));
        datos.put("tendencias_categorias",
                categorizar(datos.get("areas_conocimiento_emergentes"), REGLAS_TENDENCIAS));
        datos.put("hard_skills_categorias",
                categorizar(datos.get("herramientas_tecnologicas"), REGLAS_HARD));
        datos.put("soft_skills_categorias",
                categorizar(datos.get("habilidades_competencias_requeridas"), REGLAS_SOFT));

        Empleador empleador = Empleador.desdeMapa(datos);
        if (empleador.getNombreEmpresa().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la empresa es obligatorio");
        }
        return repoEmpleadores.save(empleador);
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------
    private static boolean esVacio(String v) {
        return v == null || v.trim().isEmpty();
    }

    private static String generarId(String prefijo) {
        return prefijo + "-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 8).toUpperCase();
    }

    private static String tamanoAgrupado(String tamano) {
        if (tamano == null || tamano.isEmpty())
            return "";
        if (tamano.equals("MICRO") || tamano.equals("PEQUENA"))
            return "MICRO_PEQUENA";
        if (tamano.equals("MEDIANA") || tamano.equals("GRANDE"))
            return "MEDIANA_GRANDE";
        return "";
    }

    private static String rubroAgrupado(String sector) {
        if (sector == null || sector.isEmpty())
            return "";
        return sector.equals("DESARROLLO_SOFTWARE") ? "DESARROLLO_SOFTWARE" : "OTROS";
    }

    private static String demandaIA(String texto) {
        if (texto == null || texto.trim().isEmpty())
            return "";
        String n = normalizar(texto);
        if (n.contains("inteligencia artificial"))
            return "SI";
        if (n.contains("machine learning"))
            return "SI";
        if (n.contains("deep learning"))
            return "SI";
        if (n.contains("redes neuronales"))
            return "SI";
        if (n.contains("large language model"))
            return "SI";
        if (n.contains("agentes de ia") || n.contains("agentes ia"))
            return "SI";
        if (n.matches(".*\\bia\\b.*"))
            return "SI";
        return "NO";
    }

    private static String normalizar(String texto) {
        return texto.toLowerCase()
                .replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u").replace("ñ", "n");
    }

    private static String categorizar(String texto, Map<String, List<String>> reglas) {
        if (texto == null || texto.trim().isEmpty())
            return "";
        String n = normalizar(texto);
        List<String> encontradas = new ArrayList<>();
        for (var e : reglas.entrySet()) {
            for (String kw : e.getValue()) {
                if (n.contains(kw)) {
                    encontradas.add(e.getKey());
                    break;
                }
            }
        }
        return String.join("|", encontradas);
    }

    private static final Map<String, List<String>> REGLAS_TENDENCIAS = Map.of(
            "IA", List.of("inteligencia artificial", "machine learning", "deep learning",
                    "redes neuronales", "ia aplicada", "llm"),
            "CIBERSEGURIDAD", List.of("ciberseguridad", "seguridad informatica", "ciber"),
            "CLOUD", List.of("cloud", "nube", "aws", "azure", "gcp", "computacion en la nube"),
            "CIENCIA_DATOS", List.of("ciencia de datos", "data science", "analisis de datos",
                    "analitica", "big data"),
            "IOT", List.of("iot", "internet de las cosas", "internet of things"),
            "DEVOPS", List.of("devops", "ci/cd", "docker", "kubernetes"),
            "MOVIL", List.of("aplicaciones moviles", "apps moviles", "mobile", "android", "ios"),
            "AUTOMATIZACION", List.of("automatizacion", "rpa", "automatizar"),
            "BLOCKCHAIN", List.of("blockchain", "web3", "solidity"),
            "AR_VR", List.of("realidad virtual", "vr", "realidad aumentada"));

    private static final Map<String, List<String>> REGLAS_HARD = Map.of(
            "HERRAMIENTAS_IA", List.of("chatgpt", "copilot", "claude", "codex", "cursor",
                    "qodo", "gemini", "openai", "antigravity", "agentes"),
            "CLOUD", List.of("aws", "azure", "gcp", "google cloud", "supabase", "firebase", "vercel"),
            "DEVOPS", List.of("docker", "kubernetes", "github actions", "gitlab ci", "terraform"),
            "LENGUAJES", List.of("python", "java", "c#", "javascript", "typescript",
                    "kotlin", "go", "rust", "php"),
            "BASES_DATOS", List.of("sql", "postgresql", "mysql", "oracle", "mongodb", "postgres"),
            "FRONTEND", List.of("react", "vue", "angular", "tailwind"),
            "MOVIL", List.of("flutter", "react native", "android", "ios"),
            "VERSIONAMIENTO", List.of("git", "github", "gitlab"),
            "GESTION_PROYECTOS", List.of("jira", "trello", "clickup", "scrum"));

    private static final Map<String, List<String>> REGLAS_SOFT = Map.of(
            "TRABAJO_EQUIPO", List.of("trabajo en equipo", "colaboracion", "colaborar"),
            "COMUNICACION", List.of("comunicacion", "oratoria", "expresion", "comunicacion efectiva"),
            "RESOLUCION_PROBLEMAS", List.of("resolucion de problemas", "resolver problemas",
                    "problem solving"),
            "CRITERIO_IA", List.of("criterio", "auditar ia", "evaluar ia", "orquestar ia",
                    "uso responsable ia", "agentes ia"),
            "ADAPTABILIDAD", List.of("adaptacion", "adaptabilidad", "aprendizaje continuo",
                    "flexibilidad", "aprender a aprender"),
            "LIDERAZGO", List.of("liderazgo", "iniciativa", "proactividad"),
            "PENSAMIENTO_CRITICO", List.of("pensamiento critico", "criterio tecnico", "analisis"),
            "INGLES", List.of("ingles", "english"));
}