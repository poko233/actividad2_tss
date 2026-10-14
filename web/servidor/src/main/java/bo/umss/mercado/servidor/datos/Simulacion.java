package bo.umss.mercado.servidor.datos;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

// Simulacion: guarda el resultado de CADA corrida Monte Carlo.
// Así el pronóstico queda registrado (quién, cuándo, cuánto) y la
// pantalla muestra el historial sin volver a sortear.
@Entity
public class Simulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaHora = LocalDateTime.now();
    private String escenario = "";   // ej. "Abrir maestría en IA"
    private String area = "";        // ej. "IA"
    private double proporcionEmpirica; // % real afín (0..1)
    private double demandaMedia;       // inscritos esperados
    private double limiteInferior;     // piso del IC95
    private double limiteSuperior;     // techo del IC95
    private int repeticiones = 1000;

    public Long getId() { return id; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime v) { this.fechaHora = v; }
    public String getEscenario() { return escenario; }
    public void setEscenario(String v) { this.escenario = v; }
    public String getArea() { return area; }
    public void setArea(String v) { this.area = v; }
    public double getProporcionEmpirica() { return proporcionEmpirica; }
    public void setProporcionEmpirica(double v) { this.proporcionEmpirica = v; }
    public double getDemandaMedia() { return demandaMedia; }
    public void setDemandaMedia(double v) { this.demandaMedia = v; }
    public double getLimiteInferior() { return limiteInferior; }
    public void setLimiteInferior(double v) { this.limiteInferior = v; }
    public double getLimiteSuperior() { return limiteSuperior; }
    public void setLimiteSuperior(double v) { this.limiteSuperior = v; }
    public int getRepeticiones() { return repeticiones; }
    public void setRepeticiones(int v) { this.repeticiones = v; }

    // fraseSimple(): el pronóstico en una frase para la pantalla.
    public String fraseSimple() {
        return "Si abres este programa, espera unos " + Math.round(demandaMedia)
                + " inscritos (entre " + Math.round(limiteInferior)
                + " y " + Math.round(limiteSuperior) + ").";
    }
}
