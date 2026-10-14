package bo.umss.mercado.servidor.datos;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// RepositorioSimulaciones: historial de corridas Monte Carlo.
// Lo único extra: traerlas de la más nueva a la más vieja.
public interface RepositorioSimulaciones extends JpaRepository<Simulacion, Long> {

    // Spring traduce el nombre en "ORDER BY fechaHora DESC" sola.
    List<Simulacion> findAllByOrderByFechaHoraDesc();
}
