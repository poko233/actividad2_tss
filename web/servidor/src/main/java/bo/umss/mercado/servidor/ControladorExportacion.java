package bo.umss.mercado.servidor;

import bo.umss.mercado.servidor.servicio.ServicioExportacion;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * ControladorExportacion: entrega los CSV como descarga del navegador.
 *
 * URL: GET /exportar/{nombre}.csv
 * Ejemplos:
 *   /exportar/indicadores_globales.csv
 *   /exportar/matriz_contingencia_area_x_financiamiento.csv
 *
 * Solo responde a los nombres de la lista blanca de
 * ServicioExportacion (asi no se puede leer ningun archivo del servidor).
 */
@RestController
public class ControladorExportacion {

    private final ServicioExportacion exportacion;

    public ControladorExportacion(ServicioExportacion exportacion) {
        this.exportacion = exportacion;
    }

    @GetMapping("/exportar/{nombre}.csv")
    public ResponseEntity<byte[]> descargar(@PathVariable String nombre) {
        String contenido = exportacion.generar(nombre);
        if (contenido == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombre + ".csv\"")
                .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
                .body(contenido.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/exportar/titulados.xlsx")
    public ResponseEntity<byte[]> descargarExcelTitulados() {
        byte[] bytes = exportacion.generarExcelTitulados();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"titulados.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/exportar/empleadores.xlsx")
    public ResponseEntity<byte[]> descargarExcelEmpleadores() {
        byte[] bytes = exportacion.generarExcelEmpleadores();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"empleadores.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }
}
