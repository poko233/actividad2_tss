package bo.umss.mercado.servidor.servicio;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// CargadorInicial: cuando el servidor termina de arrancar Y la base está
// vacía, importa los CSV de ejemplo que viajan con el programa.
// Así la primera visita ya muestra datos sin hacer nada.
@Component
public class CargadorInicial {

    private final ServicioCarga carga;

    public CargadorInicial(ServicioCarga carga) {
        this.carga = carga;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void alEstarListo() throws Exception {
        carga.cargarDatosInicialesSiHaceFalta();
    }
}
