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

@Service
public class ServicioImportacionCruda {

    public record Resultado(int leidas, int nuevos, int omitidos) {}

    private final RepositorioTitulados repoTitulados;
    private final RepositorioEmpleadores repoEmpleadores;

    public ServicioImportacionCruda(RepositorioTitulados repoTitulados,
            RepositorioEmpleadores repoEmpleadores) {
        this.repoTitulados = repoTitulados;
        this.repoEmpleadores = repoEmpleadores;
    }

    public enum Accion { DUPLICAR, REEMPLAZAR }

    public Resultado importarTitulados(InputStream csv, Accion accion) throws Exception {
        List<Map<String, String>> filas = LectorCsv.leer(csv);
        List<Map<String, String>> normales;
        if (LimpiezaCruda.esTitulados(filas)) {
            normales = LimpiezaCruda.convertirTitulados(filas);
        } else {
            normales = filas;
            for (Map<String, String> f : normales) {
                bo.umss.mercado.nucleo.limpieza.LimpiezaDatos.limpiarFila(f);
            }
        }

        if (accion == Accion.REEMPLAZAR) repoTitulados.deleteAll();

        Set<String> existentes = new HashSet<>();
        for (Titulado t : repoTitulados.findAll()) existentes.add(t.getIdTitulado());

        int nuevos = 0, omitidos = 0;
        for (Map<String, String> f : normales) {
            Titulado t = Titulado.desdeMapa(f);
            if (!t.getIdTitulado().isEmpty() && existentes.contains(t.getIdTitulado())) {
                omitidos++;
                continue;
            }
            repoTitulados.save(t);
            existentes.add(t.getIdTitulado());
            nuevos++;
        }
        return new Resultado(normales.size(), nuevos, omitidos);
    }

    public Resultado importarEmpleadores(InputStream csv, Accion accion) throws Exception {
        List<Map<String, String>> filas = LectorCsv.leer(csv);
        List<Map<String, String>> normales;
        if (LimpiezaCruda.esEmpleadores(filas)) {
            normales = LimpiezaCruda.convertirEmpleadores(filas);
        } else {
            normales = filas;
            for (Map<String, String> f : normales) {
                bo.umss.mercado.nucleo.limpieza.LimpiezaDatos.limpiarFila(f);
            }
        }

        if (accion == Accion.REEMPLAZAR) repoEmpleadores.deleteAll();

        Set<String> existentes = new HashSet<>();
        for (Empleador e : repoEmpleadores.findAll()) existentes.add(e.getIdEmpleador());

        int nuevos = 0, omitidos = 0;
        for (Map<String, String> f : normales) {
            Empleador e = Empleador.desdeMapa(f);
            if (!e.getIdEmpleador().isEmpty() && existentes.contains(e.getIdEmpleador())) {
                omitidos++;
                continue;
            }
            repoEmpleadores.save(e);
            existentes.add(e.getIdEmpleador());
            nuevos++;
        }
        return new Resultado(normales.size(), nuevos, omitidos);
    }
}
