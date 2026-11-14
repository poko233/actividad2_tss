package bo.umss.mercado.servidor.servicio;

import bo.umss.mercado.servidor.datos.RepositorioEmpleadores;
import bo.umss.mercado.servidor.datos.RepositorioTitulados;
import bo.umss.mercado.servidor.datos.HistorialExportacion;
import bo.umss.mercado.servidor.datos.RepositorioHistorialExportacion;
import bo.umss.mercado.servidor.servicio.exportacion.ExportadorExcelEmpleadores;
import bo.umss.mercado.servidor.servicio.exportacion.ExportadorExcelTitulados;
import bo.umss.mercado.servidor.servicio.exportacion.ExportadorPdfEmpleadores;
import bo.umss.mercado.servidor.servicio.exportacion.ExportadorPdfTitulados;
import org.springframework.stereotype.Service;

@Service
public class ServicioExportacion {

    private final RepositorioTitulados repoTitulados;
    private final RepositorioEmpleadores repoEmpleadores;
    private final ExportadorExcelTitulados exportadorExcelTitulados;
    private final ExportadorExcelEmpleadores exportadorExcelEmpleadores;
    private final ExportadorPdfTitulados exportadorPdfTitulados;
    private final ExportadorPdfEmpleadores exportadorPdfEmpleadores;
    private final RepositorioHistorialExportacion historial;

    public ServicioExportacion(RepositorioTitulados repoTitulados,
            RepositorioEmpleadores repoEmpleadores,
            ExportadorExcelTitulados exportadorExcelTitulados,
            ExportadorExcelEmpleadores exportadorExcelEmpleadores,
            ExportadorPdfTitulados exportadorPdfTitulados,
            ExportadorPdfEmpleadores exportadorPdfEmpleadores,
            RepositorioHistorialExportacion historial) {
        this.repoTitulados = repoTitulados;
        this.repoEmpleadores = repoEmpleadores;
        this.exportadorExcelTitulados = exportadorExcelTitulados;
        this.exportadorExcelEmpleadores = exportadorExcelEmpleadores;
        this.exportadorPdfTitulados = exportadorPdfTitulados;
        this.exportadorPdfEmpleadores = exportadorPdfEmpleadores;
        this.historial = historial;
    }

    public byte[] generarExcelTitulados() {
        long inicio = System.currentTimeMillis();
        byte[] bytes = exportadorExcelTitulados.exportar();
        long duracion = System.currentTimeMillis() - inicio;
        HistorialExportacion registro = new HistorialExportacion();
        registro.setTipo("EXCEL_TITULADOS");
        registro.setParametros("filas=" + repoTitulados.count());
        registro.setTamanoBytes(bytes.length);
        registro.setDuracionMs(duracion);
        historial.save(registro);
        return bytes;
    }

    public byte[] generarExcelEmpleadores() {
        long inicio = System.currentTimeMillis();
        byte[] bytes = exportadorExcelEmpleadores.exportar();
        long duracion = System.currentTimeMillis() - inicio;
        HistorialExportacion registro = new HistorialExportacion();
        registro.setTipo("EXCEL_EMPLEADORES");
        registro.setParametros("filas=" + repoEmpleadores.count());
        registro.setTamanoBytes(bytes.length);
        registro.setDuracionMs(duracion);
        historial.save(registro);
        return bytes;
    }

    public byte[] generarPdfTitulados() {
        long inicio = System.currentTimeMillis();
        byte[] bytes = exportadorPdfTitulados.exportar();
        long duracion = System.currentTimeMillis() - inicio;
        HistorialExportacion registro = new HistorialExportacion();
        registro.setTipo("PDF_TITULADOS");
        registro.setParametros("pdf");
        registro.setTamanoBytes(bytes.length);
        registro.setDuracionMs(duracion);
        historial.save(registro);
        return bytes;
    }

    public byte[] generarPdfEmpleadores() {
        long inicio = System.currentTimeMillis();
        byte[] bytes = exportadorPdfEmpleadores.exportar();
        long duracion = System.currentTimeMillis() - inicio;
        HistorialExportacion registro = new HistorialExportacion();
        registro.setTipo("PDF_EMPLEADORES");
        registro.setParametros("pdf");
        registro.setTamanoBytes(bytes.length);
        registro.setDuracionMs(duracion);
        historial.save(registro);
        return bytes;
    }
}