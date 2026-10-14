package bo.umss.mercado.servidor.vista;

import bo.umss.mercado.servidor.servicio.ServicioCarga;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.io.InputStream;

// VistaImportar: sube titulados.csv / empleadores.csv al servidor.
// Cada archivo se limpia y se guarda saltando los id repetidos,
// así importar dos veces no duplica nada.
@Route(value = "importar", layout = MainLayout.class)
@PageTitle("Importar | Mercado UMSS")
public class VistaImportar extends VerticalLayout {

    public VistaImportar(ServicioCarga carga) {
        add(new H1("Importar encuestas desde CSV"));
        add(new Paragraph("Los archivos deben tener los encabezados oficiales "
                + "de la encuesta (§1.1 titulados, §1.2 empleadores)."));

        add(new H3("Titulados (.csv)"));
        add(subidaPara("Subir titulados.csv", extensionCsv(), contenido -> {
            try {
                int nuevos = carga.cargarTitulados(contenido);
                Notification.show(nuevos + " titulados nuevos guardados.");
            } catch (Exception archivoInvalido) {
                Notification.show("No se pudo leer: " + archivoInvalido.getMessage());
            }
        }));

        add(new H3("Empleadores (.csv)"));
        add(subidaPara("Subir empleadores.csv", extensionCsv(), contenido -> {
            try {
                int nuevos = carga.cargarEmpleadores(contenido);
                Notification.show(nuevos + " empleadores nuevos guardados.");
            } catch (Exception archivoInvalido) {
                Notification.show("No se pudo leer: " + archivoInvalido.getMessage());
            }
        }));
    }

    // extensionCsv(): acepta solo .csv en el diálogo del sistema.
    private String[] extensionCsv() {
        return new String[]{".csv"};
    }

    // subidaPara(): botón de subida que entrega el contenido al servicio.
    // MemoryBuffer = el archivo vive en memoria, no toca el disco.
    private Upload subidaPara(String titulo, String[] extensiones,
            java.util.function.Consumer<InputStream> alRecibir) {
        MemoryBuffer memoria = new MemoryBuffer();
        Upload subida = new Upload(memoria);
        subida.setAcceptedFileTypes(extensiones);
        subida.setUploadButton(new com.vaadin.flow.component.button.Button(titulo));
        subida.addSucceededListener(exito -> alRecibir.accept(
                memoria.getInputStream()));
        return subida;
    }
}
