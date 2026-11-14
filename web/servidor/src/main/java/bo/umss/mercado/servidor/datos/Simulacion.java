package bo.umss.mercado.servidor.datos;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Simulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaHora = LocalDateTime.now();
    private String escenario = "";
    private String area = "";
    private double proporcionEmpirica;
    private double demandaMedia;
    private double limiteInferior;
    private double limiteSuperior;
    private int repeticiones = 1000;

    private long semilla = 12345L;
    private double probInteresados = 0.8;
    private double probResto = 0.0;

    public Long getId() {
        return id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime v) {
        this.fechaHora = v;
    }

    public String getEscenario() {
        return escenario;
    }

    public void setEscenario(String v) {
        this.escenario = v;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String v) {
        this.area = v;
    }

    public double getProporcionEmpirica() {
        return proporcionEmpirica;
    }

    public void setProporcionEmpirica(double v) {
        this.proporcionEmpirica = v;
    }

    public double getDemandaMedia() {
        return demandaMedia;
    }

    public void setDemandaMedia(double v) {
        this.demandaMedia = v;
    }

    public double getLimiteInferior() {
        return limiteInferior;
    }

    public void setLimiteInferior(double v) {
        this.limiteInferior = v;
    }

    public double getLimiteSuperior() {
        return limiteSuperior;
    }

    public void setLimiteSuperior(double v) {
        this.limiteSuperior = v;
    }

    public int getRepeticiones() {
        return repeticiones;
    }

    public void setRepeticiones(int v) {
        this.repeticiones = v;
    }

    public long getSemilla() {
        return semilla;
    }

    public void setSemilla(long v) {
        this.semilla = v;
    }

    public double getProbInteresados() {
        return probInteresados;
    }

    public void setProbInteresados(double v) {
        this.probInteresados = v;
    }

    public double getProbResto() {
        return probResto;
    }

    public void setProbResto(double v) {
        this.probResto = v;
    }

    public String fraseSimple() {
        return "Si abres este programa, espera unos " + Math.round(demandaMedia)
                + " inscritos (entre " + Math.round(limiteInferior)
                + " y " + Math.round(limiteSuperior) + ").";
    }
}