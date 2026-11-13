package bo.umss.mercado.servidor.servicio;

import bo.umss.mercado.servidor.datos.Empleador;
import bo.umss.mercado.servidor.datos.RepositorioEmpleadores;
import bo.umss.mercado.servidor.datos.RepositorioTitulados;
import bo.umss.mercado.servidor.datos.Titulado;
import bo.umss.mercado.servidor.servicio.ServicioImportacionCruda.Accion;
import bo.umss.mercado.servidor.servicio.ServicioImportacionCruda.Resultado;
import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class CargadorInicial {

    private static final Logger log = LoggerFactory.getLogger(CargadorInicial.class);

    private final ServicioImportacionCruda importacion;
    private final RepositorioTitulados repoTitulados;
    private final RepositorioEmpleadores repoEmpleadores;

    public CargadorInicial(ServicioImportacionCruda importacion,
            RepositorioTitulados repoTitulados,
            RepositorioEmpleadores repoEmpleadores) {
        this.importacion = importacion;
        this.repoTitulados = repoTitulados;
        this.repoEmpleadores = repoEmpleadores;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void alEstarListo() {
        try {
            limpiarRegistrosFantasma();
            cargarTituladosSiHaceFalta();
            cargarEmpleadoresSiHaceFalta();
        } catch (Exception ex) {
            log.error("Error en carga inicial de datos: {}", ex.getMessage(), ex);
        }
    }

    private void limpiarRegistrosFantasma() {
        List<Titulado> ts = repoTitulados.findAll();
        if (!ts.isEmpty() && ts.stream().allMatch(t -> esVacio(t.getIdTitulado()))) {
            repoTitulados.deleteAll();
            log.warn("Limpieza: se eliminaron {} titulados fantasma (id vacio, carga vieja fallida)", ts.size());
        } else if (!ts.isEmpty() && ts.stream().anyMatch(t -> esVacio(t.getIdTitulado()))) {
            log.warn("Hay titulados con id vacio mezclados con reales ({}). No se borran. Limpia manualmente si es necesario.", ts.size());
        }

        List<Empleador> es = repoEmpleadores.findAll();
        if (!es.isEmpty() && es.stream().allMatch(e -> esVacio(e.getIdEmpleador()))) {
            repoEmpleadores.deleteAll();
            log.warn("Limpieza: se eliminaron {} empleadores fantasma (id vacio, carga vieja fallida)", es.size());
        } else if (!es.isEmpty() && es.stream().anyMatch(e -> esVacio(e.getIdEmpleador()))) {
            log.warn("Hay empleadores con id vacio mezclados con reales ({}). No se borran.", es.size());
        }
    }

    private boolean esVacio(String s) {
        return s == null || s.isEmpty();
    }

    private void cargarTituladosSiHaceFalta() throws Exception {
        if (repoTitulados.count() > 0) {
            log.info("Titulados: ya hay {} registros en la BD, no se recarga", repoTitulados.count());
            return;
        }
        try (InputStream is = new ClassPathResource("datos-iniciales/titulados.csv").getInputStream()) {
            Resultado r = importacion.importarTitulados(is, Accion.DUPLICAR);
            log.info("Carga inicial titulados: {}", r);
            if (r.nuevos() == 0) {
                log.error("No se cargaron titulados. Verificar formato de datos-iniciales/titulados.csv");
            }
        }
    }

    private void cargarEmpleadoresSiHaceFalta() throws Exception {
        if (repoEmpleadores.count() > 0) {
            log.info("Empleadores: ya hay {} registros en la BD, no se recarga", repoEmpleadores.count());
            return;
        }
        try (InputStream is = new ClassPathResource("datos-iniciales/empleadores.csv").getInputStream()) {
            Resultado r = importacion.importarEmpleadores(is, Accion.DUPLICAR);
            log.info("Carga inicial empleadores: {}", r);
            if (r.nuevos() == 0) {
                log.error("No se cargaron empleadores. Verificar formato de datos-iniciales/empleadores.csv");
            }
        }
    }
}