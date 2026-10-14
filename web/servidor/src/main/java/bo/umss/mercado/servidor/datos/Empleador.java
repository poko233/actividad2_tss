package bo.umss.mercado.servidor.datos;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.LinkedHashMap;
import java.util.Map;

// Empleador: UNA fila = UNA empresa encuestada. Espejo de empleadores.csv
// (las 24 columnas: datos + empleabilidad + 10 Likert, §1.2).
// Hibernate crea sola la tabla "empleador".
@Entity
public class Empleador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // número interno de la base

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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

    // comoMapa(): claves idénticas al CSV para reusar el núcleo.
    public Map<String, String> comoMapa() {
        Map<String, String> fila = new LinkedHashMap<>();
        fila.put("id_empleador", idEmpleador);
        fila.put("correo", correo);
        fila.put("nombre_empresa", nombreEmpresa);
        fila.put("tipo_organizacion", tipoOrganizacion);
        fila.put("tamano_organizacion", tamanoOrganizacion);
        fila.put("sector_rubro", sectorRubro);
        fila.put("contrato_ultimos_5_anios", contratoUltimos5Anios);
        fila.put("posibilidad_futura_contratacion", posibilidadFuturaContratacion);
        fila.put("nivel_formacion_demanda", nivelFormacionDemanda);
        fila.put("medio_convocatoria", medioConvocatoria);
        fila.put("cargos_desempenados", cargosDesempenados);
        fila.put("areas_conocimiento_emergentes", areasConocimientoEmergentes);
        fila.put("herramientas_tecnologicas", herramientasTecnologicas);
        fila.put("habilidades_competencias_requeridas",
                habilidadesCompetenciasRequeridas);
        fila.put("likert_confianza_formadora", likertConfianzaFormadora);
        fila.put("likert_consistencia_titulo", likertConsistenciaTitulo);
        fila.put("likert_consulta_regular", likertConsultaRegular);
        fila.put("likert_conoce_perfil", likertConocePerfil);
        fila.put("likert_coherencia_perfil", likertCoherenciaPerfil);
        fila.put("likert_formacion_desempeno", likertFormacionDesempeno);
        fila.put("likert_considera_opinion", likertConsideraOpinion);
        fila.put("likert_competencias_laborales", likertCompetenciasLaborales);
        fila.put("likert_valores_actitudes", likertValoresActitudes);
        fila.put("likert_participacion_procesos", likertParticipacionProcesos);
        return fila;
    }

    // desdeMapa(): construye un Empleador desde una fila de CSV ya limpia.
    public static Empleador desdeMapa(Map<String, String> fila) {
        Empleador empleador = new Empleador();
        for (var entrada : fila.entrySet()) {
            String valor = entrada.getValue() == null ? "" : entrada.getValue();
            switch (entrada.getKey()) {
                case "id_empleador" -> empleador.setIdEmpleador(valor);
                case "correo" -> empleador.setCorreo(valor);
                case "nombre_empresa" -> empleador.setNombreEmpresa(valor);
                case "tipo_organizacion" -> empleador.setTipoOrganizacion(valor);
                case "tamano_organizacion" -> empleador.setTamanoOrganizacion(valor);
                case "sector_rubro" -> empleador.setSectorRubro(valor);
                case "contrato_ultimos_5_anios" ->
                        empleador.setContratoUltimos5Anios(valor);
                case "posibilidad_futura_contratacion" ->
                        empleador.setPosibilidadFuturaContratacion(valor);
                case "nivel_formacion_demanda" ->
                        empleador.setNivelFormacionDemanda(valor);
                case "medio_convocatoria" -> empleador.setMedioConvocatoria(valor);
                case "cargos_desempenados" -> empleador.setCargosDesempenados(valor);
                case "areas_conocimiento_emergentes" ->
                        empleador.setAreasConocimientoEmergentes(valor);
                case "herramientas_tecnologicas" ->
                        empleador.setHerramientasTecnologicas(valor);
                case "habilidades_competencias_requeridas" ->
                        empleador.setHabilidadesCompetenciasRequeridas(valor);
                case "likert_confianza_formadora" ->
                        empleador.setLikertConfianzaFormadora(valor);
                case "likert_consistencia_titulo" ->
                        empleador.setLikertConsistenciaTitulo(valor);
                case "likert_consulta_regular" ->
                        empleador.setLikertConsultaRegular(valor);
                case "likert_conoce_perfil" -> empleador.setLikertConocePerfil(valor);
                case "likert_coherencia_perfil" ->
                        empleador.setLikertCoherenciaPerfil(valor);
                case "likert_formacion_desempeno" ->
                        empleador.setLikertFormacionDesempeno(valor);
                case "likert_considera_opinion" ->
                        empleador.setLikertConsideraOpinion(valor);
                case "likert_competencias_laborales" ->
                        empleador.setLikertCompetenciasLaborales(valor);
                case "likert_valores_actitudes" ->
                        empleador.setLikertValoresActitudes(valor);
                case "likert_participacion_procesos" ->
                        empleador.setLikertParticipacionProcesos(valor);
                default -> { /* columna extra: se ignora */ }
            }
        }
        return empleador;
    }
}
