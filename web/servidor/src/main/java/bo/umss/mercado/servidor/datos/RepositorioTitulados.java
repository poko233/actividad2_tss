package bo.umss.mercado.servidor.datos;

import org.springframework.data.jpa.repository.JpaRepository;

// RepositorioTitulados: puerta a la tabla "titulado".
// Es SOLO una interfaz: Spring escribe solo el código de
// guardar/buscar/contar. Aquí no hay ni una línea de SQL.
public interface RepositorioTitulados extends JpaRepository<Titulado, Long> {
}
