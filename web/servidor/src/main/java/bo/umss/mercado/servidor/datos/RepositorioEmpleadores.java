package bo.umss.mercado.servidor.datos;

import org.springframework.data.jpa.repository.JpaRepository;

// RepositorioEmpleadores: puerta a la tabla "empleador".
// Igual que el de titulados: interfaz vacía, Spring hace el resto.
public interface RepositorioEmpleadores extends JpaRepository<Empleador, Long> {
}
