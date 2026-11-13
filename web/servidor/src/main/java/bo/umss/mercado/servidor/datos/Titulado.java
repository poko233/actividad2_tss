package bo.umss.mercado.servidor.datos;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
public class Titulado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idTitulado = "";
    private String edad = "";
    private String genero = "";
    private String realizoFormacionComplementaria = "";
    private String nivelPosgrado = "";
    private String institucionFormacion = "";
    private String financiamientoFormacion = "";
    private String nombrePrograma = "";
    private String interesPosgrado = "";
    private String areaInteresPosgrado = "";
    private String institucionFuturaPosgrado = "";
    private String opinionPosgradoFcyt = "";
    private String aniosVidaProfesional = "";
    private String aniosDesempleado = "";
    private String situacionLaboralActual = "";
    private String nombreEmpresa = "";
    private String rubro = "";
    private String otroRubro = "";
    private String sector = "";
    private String departamento = "";
    private String areaOrganizacion = "";
    private String otraArea = "";
    private String cargo = "";
    private String otroCargo = "";
    private String pertinenciaTrabajo = "";
    private String antiguedadPuesto = "";
    private String medioObtencionEmpleo = "";
    private String remuneracionRango = "";
    private String razonNoTrabaja = "";
    private String haTenidoTrabajoAntes = "";
    private String origenEmprendimiento = "";
    private String tipoEntregableNegocio = "";
    private String financiamientoNegocio = "";
    private String satisfaccionNegocio = "";
    private String importanciaFormacionNegocio = "";
    private String primerEmpleoActual = "";
    private String tiempoPrimerEmpleo = "";
    private String numEmpleosDesdeTitulacion = "";
    private String satisfaccionFormacion = "";
    private String concordanciaFormacionRequerimientos = "";
    private String aspectosUtiles = "";
    private String aspectosMejorar = "";
    private String asignaturasVentajosas = "";
    private String asignaturasInutiles = "";
    private String asignaturaVentajosaOtra = "";
    private String asignaturaInutilOtra = "";
    private String redContactos = "";
    private String emailContacto = "";
    private String telefonoContacto = "";
    private String linkedinContacto = "";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIdTitulado() { return idTitulado; }
    public void setIdTitulado(String v) { this.idTitulado = v; }
    public String getEdad() { return edad; }
    public void setEdad(String v) { this.edad = v; }
    public String getGenero() { return genero; }
    public void setGenero(String v) { this.genero = v; }
    public String getRealizoFormacionComplementaria() { return realizoFormacionComplementaria; }
    public void setRealizoFormacionComplementaria(String v) { this.realizoFormacionComplementaria = v; }
    public String getNivelPosgrado() { return nivelPosgrado; }
    public void setNivelPosgrado(String v) { this.nivelPosgrado = v; }
    public String getInstitucionFormacion() { return institucionFormacion; }
    public void setInstitucionFormacion(String v) { this.institucionFormacion = v; }
    public String getFinanciamientoFormacion() { return financiamientoFormacion; }
    public void setFinanciamientoFormacion(String v) { this.financiamientoFormacion = v; }
    public String getNombrePrograma() { return nombrePrograma; }
    public void setNombrePrograma(String v) { this.nombrePrograma = v; }
    public String getInteresPosgrado() { return interesPosgrado; }
    public void setInteresPosgrado(String v) { this.interesPosgrado = v; }
    public String getAreaInteresPosgrado() { return areaInteresPosgrado; }
    public void setAreaInteresPosgrado(String v) { this.areaInteresPosgrado = v; }
    public String getInstitucionFuturaPosgrado() { return institucionFuturaPosgrado; }
    public void setInstitucionFuturaPosgrado(String v) { this.institucionFuturaPosgrado = v; }
    public String getOpinionPosgradoFcyt() { return opinionPosgradoFcyt; }
    public void setOpinionPosgradoFcyt(String v) { this.opinionPosgradoFcyt = v; }
    public String getAniosVidaProfesional() { return aniosVidaProfesional; }
    public void setAniosVidaProfesional(String v) { this.aniosVidaProfesional = v; }
    public String getAniosDesempleado() { return aniosDesempleado; }
    public void setAniosDesempleado(String v) { this.aniosDesempleado = v; }
    public String getSituacionLaboralActual() { return situacionLaboralActual; }
    public void setSituacionLaboralActual(String v) { this.situacionLaboralActual = v; }
    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String v) { this.nombreEmpresa = v; }
    public String getRubro() { return rubro; }
    public void setRubro(String v) { this.rubro = v; }
    public String getOtroRubro() { return otroRubro; }
    public void setOtroRubro(String v) { this.otroRubro = v; }
    public String getSector() { return sector; }
    public void setSector(String v) { this.sector = v; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String v) { this.departamento = v; }
    public String getAreaOrganizacion() { return areaOrganizacion; }
    public void setAreaOrganizacion(String v) { this.areaOrganizacion = v; }
    public String getOtraArea() { return otraArea; }
    public void setOtraArea(String v) { this.otraArea = v; }
    public String getCargo() { return cargo; }
    public void setCargo(String v) { this.cargo = v; }
    public String getOtroCargo() { return otroCargo; }
    public void setOtroCargo(String v) { this.otroCargo = v; }
    public String getPertinenciaTrabajo() { return pertinenciaTrabajo; }
    public void setPertinenciaTrabajo(String v) { this.pertinenciaTrabajo = v; }
    public String getAntiguedadPuesto() { return antiguedadPuesto; }
    public void setAntiguedadPuesto(String v) { this.antiguedadPuesto = v; }
    public String getMedioObtencionEmpleo() { return medioObtencionEmpleo; }
    public void setMedioObtencionEmpleo(String v) { this.medioObtencionEmpleo = v; }
    public String getRemuneracionRango() { return remuneracionRango; }
    public void setRemuneracionRango(String v) { this.remuneracionRango = v; }
    public String getRazonNoTrabaja() { return razonNoTrabaja; }
    public void setRazonNoTrabaja(String v) { this.razonNoTrabaja = v; }
    public String getHaTenidoTrabajoAntes() { return haTenidoTrabajoAntes; }
    public void setHaTenidoTrabajoAntes(String v) { this.haTenidoTrabajoAntes = v; }
    public String getOrigenEmprendimiento() { return origenEmprendimiento; }
    public void setOrigenEmprendimiento(String v) { this.origenEmprendimiento = v; }
    public String getTipoEntregableNegocio() { return tipoEntregableNegocio; }
    public void setTipoEntregableNegocio(String v) { this.tipoEntregableNegocio = v; }
    public String getFinanciamientoNegocio() { return financiamientoNegocio; }
    public void setFinanciamientoNegocio(String v) { this.financiamientoNegocio = v; }
    public String getSatisfaccionNegocio() { return satisfaccionNegocio; }
    public void setSatisfaccionNegocio(String v) { this.satisfaccionNegocio = v; }
    public String getImportanciaFormacionNegocio() { return importanciaFormacionNegocio; }
    public void setImportanciaFormacionNegocio(String v) { this.importanciaFormacionNegocio = v; }
    public String getPrimerEmpleoActual() { return primerEmpleoActual; }
    public void setPrimerEmpleoActual(String v) { this.primerEmpleoActual = v; }
    public String getTiempoPrimerEmpleo() { return tiempoPrimerEmpleo; }
    public void setTiempoPrimerEmpleo(String v) { this.tiempoPrimerEmpleo = v; }
    public String getNumEmpleosDesdeTitulacion() { return numEmpleosDesdeTitulacion; }
    public void setNumEmpleosDesdeTitulacion(String v) { this.numEmpleosDesdeTitulacion = v; }
    public String getSatisfaccionFormacion() { return satisfaccionFormacion; }
    public void setSatisfaccionFormacion(String v) { this.satisfaccionFormacion = v; }
    public String getConcordanciaFormacionRequerimientos() { return concordanciaFormacionRequerimientos; }
    public void setConcordanciaFormacionRequerimientos(String v) { this.concordanciaFormacionRequerimientos = v; }
    public String getAspectosUtiles() { return aspectosUtiles; }
    public void setAspectosUtiles(String v) { this.aspectosUtiles = v; }
    public String getAspectosMejorar() { return aspectosMejorar; }
    public void setAspectosMejorar(String v) { this.aspectosMejorar = v; }
    public String getAsignaturasVentajosas() { return asignaturasVentajosas; }
    public void setAsignaturasVentajosas(String v) { this.asignaturasVentajosas = v; }
    public String getAsignaturasInutiles() { return asignaturasInutiles; }
    public void setAsignaturasInutiles(String v) { this.asignaturasInutiles = v; }
    public String getAsignaturaVentajosaOtra() { return asignaturaVentajosaOtra; }
    public void setAsignaturaVentajosaOtra(String v) { this.asignaturaVentajosaOtra = v; }
    public String getAsignaturaInutilOtra() { return asignaturaInutilOtra; }
    public void setAsignaturaInutilOtra(String v) { this.asignaturaInutilOtra = v; }
    public String getRedContactos() { return redContactos; }
    public void setRedContactos(String v) { this.redContactos = v; }
    public String getEmailContacto() { return emailContacto; }
    public void setEmailContacto(String v) { this.emailContacto = v; }
    public String getTelefonoContacto() { return telefonoContacto; }
    public void setTelefonoContacto(String v) { this.telefonoContacto = v; }
    public String getLinkedinContacto() { return linkedinContacto; }
    public void setLinkedinContacto(String v) { this.linkedinContacto = v; }

    public Map<String, String> comoMapa() {
        Map<String, String> fila = new LinkedHashMap<>();
        fila.put("id_titulado", nz(idTitulado));
        fila.put("edad", nz(edad));
        fila.put("genero", nz(genero));
        fila.put("realizo_formacion_complementaria", nz(realizoFormacionComplementaria));
        fila.put("nivel_posgrado", nz(nivelPosgrado));
        fila.put("institucion_formacion", nz(institucionFormacion));
        fila.put("financiamiento_formacion", nz(financiamientoFormacion));
        fila.put("nombre_programa", nz(nombrePrograma));
        fila.put("interes_posgrado", nz(interesPosgrado));
        fila.put("area_interes_posgrado", nz(areaInteresPosgrado));
        fila.put("institucion_futura_posgrado", nz(institucionFuturaPosgrado));
        fila.put("opinion_posgrado_fcyt", nz(opinionPosgradoFcyt));
        fila.put("anios_vida_profesional", nz(aniosVidaProfesional));
        fila.put("anios_desempleado", nz(aniosDesempleado));
        fila.put("situacion_laboral_actual", nz(situacionLaboralActual));
        fila.put("nombre_empresa", nz(nombreEmpresa));
        fila.put("rubro", nz(rubro));
        fila.put("otro_rubro", nz(otroRubro));
        fila.put("sector", nz(sector));
        fila.put("departamento", nz(departamento));
        fila.put("area_organizacion", nz(areaOrganizacion));
        fila.put("otra_area", nz(otraArea));
        fila.put("cargo", nz(cargo));
        fila.put("otro_cargo", nz(otroCargo));
        fila.put("pertinencia_trabajo", nz(pertinenciaTrabajo));
        fila.put("antiguedad_puesto", nz(antiguedadPuesto));
        fila.put("medio_obtencion_empleo", nz(medioObtencionEmpleo));
        fila.put("remuneracion_rango", nz(remuneracionRango));
        fila.put("razon_no_trabaja", nz(razonNoTrabaja));
        fila.put("ha_tenido_trabajo_antes", nz(haTenidoTrabajoAntes));
        fila.put("origen_emprendimiento", nz(origenEmprendimiento));
        fila.put("tipo_entregable_negocio", nz(tipoEntregableNegocio));
        fila.put("financiamiento_negocio", nz(financiamientoNegocio));
        fila.put("satisfaccion_negocio", nz(satisfaccionNegocio));
        fila.put("importancia_formacion_negocio", nz(importanciaFormacionNegocio));
        fila.put("primer_empleo_actual", nz(primerEmpleoActual));
        fila.put("tiempo_primer_empleo", nz(tiempoPrimerEmpleo));
        fila.put("num_empleos_desde_titulacion", nz(numEmpleosDesdeTitulacion));
        fila.put("satisfaccion_formacion", nz(satisfaccionFormacion));
        fila.put("concordancia_formacion_requerimientos", nz(concordanciaFormacionRequerimientos));
        fila.put("aspectos_utiles", nz(aspectosUtiles));
        fila.put("aspectos_mejorar", nz(aspectosMejorar));
        fila.put("asignaturas_ventajosas", nz(asignaturasVentajosas));
        fila.put("asignaturas_inutiles", nz(asignaturasInutiles));
        fila.put("asignatura_ventajosa_otra", nz(asignaturaVentajosaOtra));
        fila.put("asignatura_inutil_otra", nz(asignaturaInutilOtra));
        fila.put("red_contactos", nz(redContactos));
        fila.put("email_contacto", nz(emailContacto));
        fila.put("telefono_contacto", nz(telefonoContacto));
        fila.put("linkedin_contacto", nz(linkedinContacto));
        return fila;
    }

    public static Titulado desdeMapa(Map<String, String> fila) {
        Titulado titulado = new Titulado();
        for (var entrada : fila.entrySet()) {
            String valor = entrada.getValue() == null ? "" : entrada.getValue();
            switch (entrada.getKey()) {
                case "id_titulado" -> titulado.setIdTitulado(valor);
                case "edad" -> titulado.setEdad(valor);
                case "genero" -> titulado.setGenero(valor);
                case "realizo_formacion_complementaria" -> titulado.setRealizoFormacionComplementaria(valor);
                case "nivel_posgrado" -> titulado.setNivelPosgrado(valor);
                case "institucion_formacion" -> titulado.setInstitucionFormacion(valor);
                case "financiamiento_formacion" -> titulado.setFinanciamientoFormacion(valor);
                case "nombre_programa" -> titulado.setNombrePrograma(valor);
                case "interes_posgrado" -> titulado.setInteresPosgrado(valor);
                case "area_interes_posgrado" -> titulado.setAreaInteresPosgrado(valor);
                case "institucion_futura_posgrado" -> titulado.setInstitucionFuturaPosgrado(valor);
                case "opinion_posgrado_fcyt" -> titulado.setOpinionPosgradoFcyt(valor);
                case "anios_vida_profesional" -> titulado.setAniosVidaProfesional(valor);
                case "anios_desempleado" -> titulado.setAniosDesempleado(valor);
                case "situacion_laboral_actual" -> titulado.setSituacionLaboralActual(valor);
                case "nombre_empresa" -> titulado.setNombreEmpresa(valor);
                case "rubro" -> titulado.setRubro(valor);
                case "otro_rubro" -> titulado.setOtroRubro(valor);
                case "sector" -> titulado.setSector(valor);
                case "departamento" -> titulado.setDepartamento(valor);
                case "area_organizacion" -> titulado.setAreaOrganizacion(valor);
                case "otra_area" -> titulado.setOtraArea(valor);
                case "cargo" -> titulado.setCargo(valor);
                case "otro_cargo" -> titulado.setOtroCargo(valor);
                case "pertinencia_trabajo" -> titulado.setPertinenciaTrabajo(valor);
                case "antiguedad_puesto" -> titulado.setAntiguedadPuesto(valor);
                case "medio_obtencion_empleo" -> titulado.setMedioObtencionEmpleo(valor);
                case "remuneracion_rango" -> titulado.setRemuneracionRango(valor);
                case "razon_no_trabaja" -> titulado.setRazonNoTrabaja(valor);
                case "ha_tenido_trabajo_antes" -> titulado.setHaTenidoTrabajoAntes(valor);
                case "origen_emprendimiento" -> titulado.setOrigenEmprendimiento(valor);
                case "tipo_entregable_negocio" -> titulado.setTipoEntregableNegocio(valor);
                case "financiamiento_negocio" -> titulado.setFinanciamientoNegocio(valor);
                case "satisfaccion_negocio" -> titulado.setSatisfaccionNegocio(valor);
                case "importancia_formacion_negocio" -> titulado.setImportanciaFormacionNegocio(valor);
                case "primer_empleo_actual" -> titulado.setPrimerEmpleoActual(valor);
                case "tiempo_primer_empleo" -> titulado.setTiempoPrimerEmpleo(valor);
                case "num_empleos_desde_titulacion" -> titulado.setNumEmpleosDesdeTitulacion(valor);
                case "satisfaccion_formacion" -> titulado.setSatisfaccionFormacion(valor);
                case "concordancia_formacion_requerimientos" -> titulado.setConcordanciaFormacionRequerimientos(valor);
                case "aspectos_utiles" -> titulado.setAspectosUtiles(valor);
                case "aspectos_mejorar" -> titulado.setAspectosMejorar(valor);
                case "asignaturas_ventajosas" -> titulado.setAsignaturasVentajosas(valor);
                case "asignaturas_inutiles" -> titulado.setAsignaturasInutiles(valor);
                case "asignatura_ventajosa_otra" -> titulado.setAsignaturaVentajosaOtra(valor);
                case "asignatura_inutil_otra" -> titulado.setAsignaturaInutilOtra(valor);
                case "red_contactos" -> titulado.setRedContactos(valor);
                case "email_contacto" -> titulado.setEmailContacto(valor);
                case "telefono_contacto" -> titulado.setTelefonoContacto(valor);
                case "linkedin_contacto" -> titulado.setLinkedinContacto(valor);
                default -> { /* columna extra: se ignora sin romper nada */ }
            }
        }
        return titulado;
    }

    private static String nz(String v) {
        return v == null ? "" : v;
    }
}