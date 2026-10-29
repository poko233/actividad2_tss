package bo.umss.mercado.servidor.servicio;

import bo.umss.mercado.nucleo.csv.LectorCsv;
import bo.umss.mercado.nucleo.limpieza.LimpiezaCruda;
import bo.umss.mercado.servidor.datos.Empleador;
import bo.umss.mercado.servidor.datos.RepositorioEmpleadores;
import bo.umss.mercado.servidor.datos.RepositorioTitulados;
import bo.umss.mercado.servidor.datos.Titulado;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * ServicioImportacionCruda: lee un CSV crudo (exportado directo de MS Forms)
 * y lo convierte al formato normalizado de la app, aplicando todas las
 * correcciones y validaciones (tilde en "NO trabajo", acentos, si/no, etc.).
 *
 * Acciones:
 *   DUPLICAR  = anade los nuevos sin borrar los existentes (salta id ya
 *               guardados para no duplicar).
 *   REEMPLAZAR = borra todos los registros del tipo y carga los del CSV.
 */
@Service
public class ServicioImportacionCruda {

    private final RepositorioTitulados repoTitulados;
    private final RepositorioEmpleadores repoEmpleadores;

    public ServicioImportacionCruda(RepositorioTitulados repoTitulados,
            RepositorioEmpleadores repoEmpleadores) {
        this.repoTitulados = repoTitulados;
        this.repoEmpleadores = repoEmpleadores;
    }

    public enum Accion { DUPLICAR, REEMPLAZAR }

    // ----------------------------------------------------------------
    // Importar titulados desde CSV crudo.
    // ----------------------------------------------------------------
    public int importarTitulados(InputStream csvCrudo, Accion accion) throws Exception {
        List<Map<String, String>> filasCrudas = LectorCsv.leer(csvCrudo);
        List<Map<String, String>> filasNormales;

        // Detectar si es CSV crudo (>= 50 columnas) o normalizado (35 columnas).
        if (LimpiezaCruda.esTitulados(filasCrudas)) {
            filasNormales = LimpiezaCruda.convertirTitulados(filasCrudas);
        } else {
            // CSV normalizado: aplicar limpieza estandar.
            filasNormales = filasCrudas;
            for (Map<String, String> fila : filasNormales) {
                bo.umss.mercado.nucleo.limpieza.LimpiezaDatos.limpiarFila(fila);
            }
        }

        if (accion == Accion.REEMPLAZAR) {
            repoTitulados.deleteAll();
        }

        Set<String> idsGuardados = new HashSet<>();
        for (Titulado existente : repoTitulados.findAll()) {
            idsGuardados.add(existente.getIdTitulado());
        }

        int nuevos = 0;
        for (Map<String, String> fila : filasNormales) {
            Titulado titulado = Titulado.desdeMapa(fila);
            if (!titulado.getIdTitulado().isEmpty()
                    && idsGuardados.contains(titulado.getIdTitulado())) {
                continue; // ya estaba: se salta para no duplicar
            }
            repoTitulados.save(titulado);
            idsGuardados.add(titulado.getIdTitulado());
            nuevos++;
        }
        return nuevos;
    }

    // ----------------------------------------------------------------
    // Importar empleadores desde CSV crudo.
    // ----------------------------------------------------------------
    public int importarEmpleadores(InputStream csvCrudo, Accion accion) throws Exception {
        List<Map<String, String>> filasCrudas = LectorCsv.leer(csvCrudo);
        List<Map<String, String>> filasNormales;

        if (LimpiezaCruda.esEmpleadores(filasCrudas)) {
            filasNormales = LimpiezaCruda.convertirEmpleadores(filasCrudas);
        } else {
            filasNormales = filasCrudas;
            for (Map<String, String> fila : filasNormales) {
                bo.umss.mercado.nucleo.limpieza.LimpiezaDatos.limpiarFila(fila);
            }
        }

        if (accion == Accion.REEMPLAZAR) {
            repoEmpleadores.deleteAll();
        }

        Set<String> idsGuardados = new HashSet<>();
        for (Empleador existente : repoEmpleadores.findAll()) {
            idsGuardados.add(existente.getIdEmpleador());
        }

        int nuevos = 0;
        for (Map<String, String> fila : filasNormales) {
            Empleador empleador = Empleador.desdeMapa(fila);
            if (!empleador.getIdEmpleador().isEmpty()
                    && idsGuardados.contains(empleador.getIdEmpleador())) {
                continue;
            }
            repoEmpleadores.save(empleador);
            idsGuardados.add(empleador.getIdEmpleador());
            nuevos++;
        }
        return nuevos;
    }
}
