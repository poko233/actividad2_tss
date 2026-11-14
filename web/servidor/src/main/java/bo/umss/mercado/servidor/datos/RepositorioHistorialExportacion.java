package bo.umss.mercado.servidor.datos;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioHistorialExportacion
        extends JpaRepository<HistorialExportacion, Long> {

    List<HistorialExportacion> findAllByOrderByFechaHoraDesc();
}
