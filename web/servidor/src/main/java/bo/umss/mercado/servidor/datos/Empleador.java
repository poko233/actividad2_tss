package bo.umss.mercado.servidor.datos;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
public class Empleador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idEmpleador = "";
    private String correo = "";
    private String nombreEmpresa = "";
    private String tipoOrganizacion = "";
    private String tamanoOrganizacion = "";
    private String sectorRubro = "";
    private String contratoUltimos5Anios = "";
    private String posibilidadFuturaContratacion = "";
    private String nivelFormacionDemanda = "";
    private String medioConvocatoria = "";
    private String cargosDesempenados = "";
    private String areasConocimientoEmergentes = "";
    private String herramientasTecnologicas = "";
    private String habilidadesCompetenciasRequeridas = "";

    // 18 items Likert
    private String likertConfianzaFormadora = "";
    private String likertConsistenciaTitulo = "";
    private String likertConsultaRegular = "";
    private String likertConocePerfil = "";
    private String likertCoherenciaPerfil = "";
    private String likertFormacionDesempeno = "";
    private String likertConsideraOpinion = "";
    private String likertCompetenciasLaborales = "";
    private String likertValoresActitudes = "";
    private String likertParticipacionProcesos = "";
    private String likertVinculosOrganizaciones = "";
    private String likertParticipacionVinculacion = "";
    private String likertDifusionOportunidades = "";
    private String likertVinculacionProfesionales = "";
    private String likertConsultadoDesempeno = "";
    private String likertConsultadoPertinencia = "";
    private String likertRecurreUmss = "";
    private String likertConsultadoAutoevaluacion = "";

    // Derivadas Ch.4
    private String tamanoAgrupado = "";
    private String rubroAgrupado = "";
    private String demandaIa = "";
    private String tendenciasCategorias = "";
    private String hardSkillsCategorias = "";
    private String softSkillsCategorias = "";

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public String getIdEmpleador() { return idEmpleador; }
    public void setIdEmpleador(String v) { this.idEmpleador = v; }
    public String getCorreo() { return correo; }
    public void setCorreo(String v) { this.correo = v; }
    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String v) { this.nombreEmpresa = v; }
    public String getTipoOrganizacion() { return tipoOrganizacion; }
    public void setTipoOrganizacion(String v) { this.tipoOrganizacion = v; }
    public String getTamanoOrganizacion() { return tamanoOrganizacion; }
    public void setTamanoOrganizacion(String v) { this.tamanoOrganizacion = v; }
    public String getSectorRubro() { return sectorRubro; }
    public void setSectorRubro(String v) { this.sectorRubro = v; }
    public String getContratoUltimos5Anios() { return contratoUltimos5Anios; }
    public void setContratoUltimos5Anios(String v) { this.contratoUltimos5Anios = v; }
    public String getPosibilidadFuturaContratacion() { return posibilidadFuturaContratacion; }
    public void setPosibilidadFuturaContratacion(String v) { this.posibilidadFuturaContratacion = v; }
    public String getNivelFormacionDemanda() { return nivelFormacionDemanda; }
    public void setNivelFormacionDemanda(String v) { this.nivelFormacionDemanda = v; }
    public String getMedioConvocatoria() { return medioConvocatoria; }
    public void setMedioConvocatoria(String v) { this.medioConvocatoria = v; }
    public String getCargosDesempenados() { return cargosDesempenados; }
    public void setCargosDesempenados(String v) { this.cargosDesempenados = v; }
    public String getAreasConocimientoEmergentes() { return areasConocimientoEmergentes; }
    public void setAreasConocimientoEmergentes(String v) { this.areasConocimientoEmergentes = v; }
    public String getHerramientasTecnologicas() { return herramientasTecnologicas; }
    public void setHerramientasTecnologicas(String v) { this.herramientasTecnologicas = v; }
    public String getHabilidadesCompetenciasRequeridas() { return habilidadesCompetenciasRequeridas; }
    public void setHabilidadesCompetenciasRequeridas(String v) { this.habilidadesCompetenciasRequeridas = v; }

    public String getLikertConfianzaFormadora() { return likertConfianzaFormadora; }
    public void setLikertConfianzaFormadora(String v) { this.likertConfianzaFormadora = v; }
    public String getLikertConsistenciaTitulo() { return likertConsistenciaTitulo; }
    public void setLikertConsistenciaTitulo(String v) { this.likertConsistenciaTitulo = v; }
    public String getLikertConsultaRegular() { return likertConsultaRegular; }
    public void setLikertConsultaRegular(String v) { this.likertConsultaRegular = v; }
    public String getLikertConocePerfil() { return likertConocePerfil; }
    public void setLikertConocePerfil(String v) { this.likertConocePerfil = v; }
    public String getLikertCoherenciaPerfil() { return likertCoherenciaPerfil; }
    public void setLikertCoherenciaPerfil(String v) { this.likertCoherenciaPerfil = v; }
    public String getLikertFormacionDesempeno() { return likertFormacionDesempeno; }
    public void setLikertFormacionDesempeno(String v) { this.likertFormacionDesempeno = v; }
    public String getLikertConsideraOpinion() { return likertConsideraOpinion; }
    public void setLikertConsideraOpinion(String v) { this.likertConsideraOpinion = v; }
    public String getLikertCompetenciasLaborales() { return likertCompetenciasLaborales; }
    public void setLikertCompetenciasLaborales(String v) { this.likertCompetenciasLaborales = v; }
    public String getLikertValoresActitudes() { return likertValoresActitudes; }
    public void setLikertValoresActitudes(String v) { this.likertValoresActitudes = v; }
    public String getLikertParticipacionProcesos() { return likertParticipacionProcesos; }
    public void setLikertParticipacionProcesos(String v) { this.likertParticipacionProcesos = v; }
    public String getLikertVinculosOrganizaciones() { return likertVinculosOrganizaciones; }
    public void setLikertVinculosOrganizaciones(String v) { this.likertVinculosOrganizaciones = v; }
    public String getLikertParticipacionVinculacion() { return likertParticipacionVinculacion; }
    public void setLikertParticipacionVinculacion(String v) { this.likertParticipacionVinculacion = v; }
    public String getLikertDifusionOportunidades() { return likertDifusionOportunidades; }
    public void setLikertDifusionOportunidades(String v) { this.likertDifusionOportunidades = v; }
    public String getLikertVinculacionProfesionales() { return likertVinculacionProfesionales; }
    public void setLikertVinculacionProfesionales(String v) { this.likertVinculacionProfesionales = v; }
    public String getLikertConsultadoDesempeno() { return likertConsultadoDesempeno; }
    public void setLikertConsultadoDesempeno(String v) { this.likertConsultadoDesempeno = v; }
    public String getLikertConsultadoPertinencia() { return likertConsultadoPertinencia; }
    public void setLikertConsultadoPertinencia(String v) { this.likertConsultadoPertinencia = v; }
    public String getLikertRecurreUmss() { return likertRecurreUmss; }
    public void setLikertRecurreUmss(String v) { this.likertRecurreUmss = v; }
    public String getLikertConsultadoAutoevaluacion() { return likertConsultadoAutoevaluacion; }
    public void setLikertConsultadoAutoevaluacion(String v) { this.likertConsultadoAutoevaluacion = v; }

    public String getTamanoAgrupado() { return tamanoAgrupado; }
    public void setTamanoAgrupado(String v) { this.tamanoAgrupado = v; }
    public String getRubroAgrupado() { return rubroAgrupado; }
    public void setRubroAgrupado(String v) { this.rubroAgrupado = v; }
    public String getDemandaIa() { return demandaIa; }
    public void setDemandaIa(String v) { this.demandaIa = v; }
    public String getTendenciasCategorias() { return tendenciasCategorias; }
    public void setTendenciasCategorias(String v) { this.tendenciasCategorias = v; }
    public String getHardSkillsCategorias() { return hardSkillsCategorias; }
    public void setHardSkillsCategorias(String v) { this.hardSkillsCategorias = v; }
    public String getSoftSkillsCategorias() { return softSkillsCategorias; }
    public void setSoftSkillsCategorias(String v) { this.softSkillsCategorias = v; }

    public Map<String, String> comoMapa() {
        Map<String, String> f = new LinkedHashMap<>();
        f.put("id_empleador", idEmpleador);
        f.put("correo", correo);
        f.put("nombre_empresa", nombreEmpresa);
        f.put("tipo_organizacion", tipoOrganizacion);
        f.put("tamano_organizacion", tamanoOrganizacion);
        f.put("sector_rubro", sectorRubro);
        f.put("contrato_ultimos_5_anios", contratoUltimos5Anios);
        f.put("posibilidad_futura_contratacion", posibilidadFuturaContratacion);
        f.put("nivel_formacion_demanda", nivelFormacionDemanda);
        f.put("medio_convocatoria", medioConvocatoria);
        f.put("cargos_desempenados", cargosDesempenados);
        f.put("areas_conocimiento_emergentes", areasConocimientoEmergentes);
        f.put("herramientas_tecnologicas", herramientasTecnologicas);
        f.put("habilidades_competencias_requeridas", habilidadesCompetenciasRequeridas);
        f.put("likert_confianza_formadora", likertConfianzaFormadora);
        f.put("likert_consistencia_titulo", likertConsistenciaTitulo);
        f.put("likert_consulta_regular", likertConsultaRegular);
        f.put("likert_conoce_perfil", likertConocePerfil);
        f.put("likert_coherencia_perfil", likertCoherenciaPerfil);
        f.put("likert_formacion_desempeno", likertFormacionDesempeno);
        f.put("likert_considera_opinion", likertConsideraOpinion);
        f.put("likert_competencias_laborales", likertCompetenciasLaborales);
        f.put("likert_valores_actitudes", likertValoresActitudes);
        f.put("likert_participacion_procesos", likertParticipacionProcesos);
        f.put("likert_vinculos_organizaciones", likertVinculosOrganizaciones);
        f.put("likert_participacion_vinculacion", likertParticipacionVinculacion);
        f.put("likert_difusion_oportunidades", likertDifusionOportunidades);
        f.put("likert_vinculacion_profesionales", likertVinculacionProfesionales);
        f.put("likert_consultado_desempeno", likertConsultadoDesempeno);
        f.put("likert_consultado_pertinencia", likertConsultadoPertinencia);
        f.put("likert_recurre_umss", likertRecurreUmss);
        f.put("likert_consultado_autoevaluacion", likertConsultadoAutoevaluacion);
        f.put("tamano_agrupado", tamanoAgrupado);
        f.put("rubro_agrupado", rubroAgrupado);
        f.put("demanda_ia", demandaIa);
        f.put("tendencias_categorias", tendenciasCategorias);
        f.put("hard_skills_categorias", hardSkillsCategorias);
        f.put("soft_skills_categorias", softSkillsCategorias);
        return f;
    }

    public static Empleador desdeMapa(Map<String, String> fila) {
        Empleador e = new Empleador();
        for (var en : fila.entrySet()) {
            String v = en.getValue() == null ? "" : en.getValue();
            switch (en.getKey()) {
                case "id_empleador" -> e.setIdEmpleador(v);
                case "correo" -> e.setCorreo(v);
                case "nombre_empresa" -> e.setNombreEmpresa(v);
                case "tipo_organizacion" -> e.setTipoOrganizacion(v);
                case "tamano_organizacion" -> e.setTamanoOrganizacion(v);
                case "sector_rubro" -> e.setSectorRubro(v);
                case "contrato_ultimos_5_anios" -> e.setContratoUltimos5Anios(v);
                case "posibilidad_futura_contratacion" -> e.setPosibilidadFuturaContratacion(v);
                case "nivel_formacion_demanda" -> e.setNivelFormacionDemanda(v);
                case "medio_convocatoria" -> e.setMedioConvocatoria(v);
                case "cargos_desempenados" -> e.setCargosDesempenados(v);
                case "areas_conocimiento_emergentes" -> e.setAreasConocimientoEmergentes(v);
                case "herramientas_tecnologicas" -> e.setHerramientasTecnologicas(v);
                case "habilidades_competencias_requeridas" -> e.setHabilidadesCompetenciasRequeridas(v);
                case "likert_confianza_formadora" -> e.setLikertConfianzaFormadora(v);
                case "likert_consistencia_titulo" -> e.setLikertConsistenciaTitulo(v);
                case "likert_consulta_regular" -> e.setLikertConsultaRegular(v);
                case "likert_conoce_perfil" -> e.setLikertConocePerfil(v);
                case "likert_coherencia_perfil" -> e.setLikertCoherenciaPerfil(v);
                case "likert_formacion_desempeno" -> e.setLikertFormacionDesempeno(v);
                case "likert_considera_opinion" -> e.setLikertConsideraOpinion(v);
                case "likert_competencias_laborales" -> e.setLikertCompetenciasLaborales(v);
                case "likert_valores_actitudes" -> e.setLikertValoresActitudes(v);
                case "likert_participacion_procesos" -> e.setLikertParticipacionProcesos(v);
                case "likert_vinculos_organizaciones" -> e.setLikertVinculosOrganizaciones(v);
                case "likert_participacion_vinculacion" -> e.setLikertParticipacionVinculacion(v);
                case "likert_difusion_oportunidades" -> e.setLikertDifusionOportunidades(v);
                case "likert_vinculacion_profesionales" -> e.setLikertVinculacionProfesionales(v);
                case "likert_consultado_desempeno" -> e.setLikertConsultadoDesempeno(v);
                case "likert_consultado_pertinencia" -> e.setLikertConsultadoPertinencia(v);
                case "likert_recurre_umss" -> e.setLikertRecurreUmss(v);
                case "likert_consultado_autoevaluacion" -> e.setLikertConsultadoAutoevaluacion(v);
                case "tamano_agrupado" -> e.setTamanoAgrupado(v);
                case "rubro_agrupado" -> e.setRubroAgrupado(v);
                case "demanda_ia" -> e.setDemandaIa(v);
                case "tendencias_categorias" -> e.setTendenciasCategorias(v);
                case "hard_skills_categorias" -> e.setHardSkillsCategorias(v);
                case "soft_skills_categorias" -> e.setSoftSkillsCategorias(v);
                default -> { }
            }
        }
        return e;
    }
}
