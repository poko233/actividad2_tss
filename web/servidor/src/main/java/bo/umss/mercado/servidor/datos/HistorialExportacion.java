package bo.umss.mercado.servidor.datos;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class HistorialExportacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaHora = LocalDateTime.now();
    private String tipo = "";
    private String parametros = "";
    private long tamanoBytes;
    private long duracionMs;

    public Long getId() {
        return id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime v) {
        this.fechaHora = v;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String v) {
        this.tipo = v;
    }

    public String getParametros() {
        return parametros;
    }

    public void setParametros(String v) {
        this.parametros = v;
    }

    public long getTamanoBytes() {
        return tamanoBytes;
    }

    public void setTamanoBytes(long v) {
        this.tamanoBytes = v;
    }

    public long getDuracionMs() {
        return duracionMs;
    }

    public void setDuracionMs(long v) {
        this.duracionMs = v;
    }
}
