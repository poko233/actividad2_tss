package bo.umss.mercado.servidor;

import bo.umss.mercado.servidor.servicio.ServicioExportacion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ControladorExportacion {

        private final ServicioExportacion exportacion;

        public ControladorExportacion(ServicioExportacion exportacion) {
                this.exportacion = exportacion;
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

        @GetMapping("/exportar/titulados.pdf")
        public ResponseEntity<byte[]> descargarPdfTitulados() {
                byte[] bytes = exportacion.generarPdfTitulados();
                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"titulados.pdf\"")
                                .contentType(MediaType.APPLICATION_PDF)
                                .body(bytes);
        }

        @GetMapping("/exportar/empleadores.pdf")
        public ResponseEntity<byte[]> descargarPdfEmpleadores() {
                byte[] bytes = exportacion.generarPdfEmpleadores();
                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"empleadores.pdf\"")
                                .contentType(MediaType.APPLICATION_PDF)
                                .body(bytes);
        }
}