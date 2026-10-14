package bo.umss.mercado.servidor.servicio;

import bo.umss.mercado.nucleo.csv.LectorCsv;
import bo.umss.mercado.nucleo.limpieza.LimpiezaDatos;
import bo.umss.mercado.servidor.datos.Empleador;
import bo.umss.mercado.servidor.datos.RepositorioEmpleadores;
import bo.umss.mercado.servidor.datos.RepositorioTitulados;
import bo.umss.mercado.servidor.datos.Titulado;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

// ServicioCarga: mete encuestas a la base de datos.
// Entradas: archivos CSV (subidos por pantalla o los iniciales).
// Reglas: limpia cada fila (Data Wrangling) y salta los id ya
// guardados para no duplicar al importar dos veces lo mismo.
@Service
public class ServicioCarga {

    private final RepositorioTitulados titulados;
    private final RepositorioEmpleadores empleadores;

    // Spring entrega solos los repositorios (inyección por constructor).
    public ServicioCarga(RepositorioTitulados titulados,
            RepositorioEmpleadores empleadores) {
        this.titulados = titulados;
        this.empleadores = empleadores;
    }

    // cargarDatosInicialesSiHaceFalta(): al arrancar con la base vacía,
    // importa los CSV de ejemplo que viajan con el programa.
    public void cargarDatosInicialesSiHaceFalta() throws Exception {
        if (titulados.count() == 0) {
            InputStream archivo = new ClassPathResource(
                    "datos-iniciales/titulados.csv").getInputStream();
            cargarTitulados(archivo);
        }
        if (empleadores.count() == 0) {
            InputStream archivo = new ClassPathResource(
                    "datos-iniciales/empleadores.csv").getInputStream();
            cargarEmpleadores(archivo);
        }
    }

    // cargarTitulados(): lee, limpia y guarda. Devuelve cuántos NUEVOS entró.
    public int cargarTitulados(InputStream archivoCsv) throws Exception {
        List<Map<String, String>> filas = LectorCsv.leer(archivoCsv);
        Set<String> idsGuardados = new HashSet<>();
        for (Titulado existente : titulados.findAll()) {
            idsGuardados.add(existente.getIdTitulado());
        }
        int nuevos = 0;
        for (Map<String, String> fila : filas) {
            LimpiezaDatos.limpiarFila(fila);
            Titulado titulado = Titulado.desdeMapa(fila);
            if (!titulado.getIdTitulado().isEmpty()
                    && idsGuardados.contains(titulado.getIdTitulado())) {
                continue; // ya estaba: se salta para no duplicar
            }
            titulados.save(titulado);
            nuevos++;
        }
        return nuevos;
    }

    // cargarEmpleadores(): igual que titulados pero a su propia tabla.
    public int cargarEmpleadores(InputStream archivoCsv) throws Exception {
        List<Map<String, String>> filas = LectorCsv.leer(archivoCsv);
        Set<String> idsGuardados = new HashSet<>();
        for (Empleador existente : empleadores.findAll()) {
            idsGuardados.add(existente.getIdEmpleador());
        }
        int nuevos = 0;
        for (Map<String, String> fila : filas) {
            LimpiezaDatos.limpiarFila(fila);
            Empleador empleador = Empleador.desdeMapa(fila);
            if (!empleador.getIdEmpleador().isEmpty()
                    && idsGuardados.contains(empleador.getIdEmpleador())) {
                continue;
            }
            empleadores.save(empleador);
            nuevos++;
        }
        return nuevos;
    }
}
