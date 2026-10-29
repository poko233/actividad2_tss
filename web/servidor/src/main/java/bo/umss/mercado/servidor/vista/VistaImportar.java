package bo.umss.mercado.servidor.vista;

import bo.umss.mercado.servidor.datos.Empleador;
import bo.umss.mercado.servidor.datos.RepositorioEmpleadores;
import bo.umss.mercado.servidor.datos.RepositorioTitulados;
import bo.umss.mercado.servidor.datos.Titulado;
import bo.umss.mercado.servidor.servicio.ServicioCarga;
import bo.umss.mercado.servidor.servicio.ServicioImportacionCruda;
import bo.umss.mercado.servidor.servicio.ServicioImportacionCruda.Accion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
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

/**
 * VistaImportar: sube encuestas crudas (CSV exportado directo de MS Forms)
 * al servidor. Antes de procesar, pregunta con un modal: DUPLICAR (anade
 * sin tocar lo que ya hay) o REEMPLAZAR (borra todo y carga lo nuevo).
 *
 * El CSV crudo se convierte automaticamente al formato normalizado de la
 * app (limpieza de acentos, si/no, situacion laboral "NO trabajo", etc.)
 * antes de guardarse en la base.
 */
@Route(value = "importar", layout = MainLayout.class)
@PageTitle("Importar | Mercado UMSS")
public class VistaImportar extends VerticalLayout {

    public VistaImportar(ServicioCarga carga,
            ServicioImportacionCruda importacionCruda) {
        add(new H1("Importar encuestas"));
        add(new Paragraph("Sube un CSV crudo (exportado directo de Microsoft "
                + "Forms) o un CSV ya normalizado. La app detecta el formato "
                + "por el numero de columnas."));

        add(new H3("Titulados (.csv)"));
        add(subidaPara("Subir titulados (crudo o normalizado)",
                new String[]{".csv"}, contenido -> {
                    mostrarModal(contenido, importacionCruda, true);
                }));

        add(new H3("Empleadores (.csv)"));
        add(subidaPara("Subir empleadores (crudo o normalizado)",
                new String[]{".csv"}, contenido -> {
                    mostrarModal(contenido, importacionCruda, false);
                }));

        // Nota sobre el formato crudo.
        add(new Paragraph("Formato crudo = el CSV que exporta MS Forms "
                + "(57 columnas para titulados, 32 para empleadores). "
                + "Formato normalizado = el CSV de 35/24 columnas que usa la app. "
                + "Si subes el normalizado, se trata como duplicar."));
    }

    // mostrarModal(): pregunta DUPLICAR o REEMPLAZAR antes de procesar.
    private void mostrarModal(InputStream contenido, ServicioImportacionCruda imp,
            boolean esTitulado) {
        Dialog modal = new Dialog();
        modal.setHeaderTitle("¿Qué quieres hacer con los datos?");
        modal.setWidth("420px");

        Paragraph texto = new Paragraph(esTitulado
                ? "Se detectó un CSV de TITULADOS."
                : "Se detectó un CSV de EMPLEADORES.");
        Paragraph explicacion = new Paragraph(
                "DUPLICAR: añade solo los nuevos (no borra los existentes, "
                        + "salta los id repetidos).\n"
                        + "REEMPLAZAR: borra TODOS los registros actuales y "
                        + "carga los del CSV.");

        Button btnDuplicar = new Button("Duplicar (añadir)");
        btnDuplicar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnDuplicar.addClickListener(e -> {
            modal.close();
            procesar(contenido, imp, esTitulado, Accion.DUPLICAR);
        });

        Button btnReemplazar = new Button("Reemplazar (borrar y cargar)");
        btnReemplazar.addThemeVariants(ButtonVariant.LUMO_ERROR);
        btnReemplazar.addClickListener(e -> {
            modal.close();
            procesar(contenido, imp, esTitulado, Accion.REEMPLAZAR);
        });

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.addClickListener(e -> modal.close());

        VerticalLayout cuerpo = new VerticalLayout(texto, explicacion,
                new com.vaadin.flow.component.orderedlayout.HorizontalLayout(
                        btnDuplicar, btnReemplazar, btnCancelar));
        cuerpo.setPadding(false);
        modal.add(cuerpo);
        modal.open();
    }

    // procesar(): ejecuta la importacion y muestra el resultado.
    private void procesar(InputStream contenido, ServicioImportacionCruda imp,
            boolean esTitulado, Accion accion) {
        try {
            int nuevos;
            if (esTitulado) {
                nuevos = imp.importarTitulados(contenido, accion);
            } else {
                nuevos = imp.importarEmpleadores(contenido, accion);
            }
            String mensaje = nuevos + " registros nuevos guardados ("
                    + accion + ").";
            Notification.show(mensaje, 5000, Notification.Position.MIDDLE);
        } catch (Exception error) {
            Notification.show("No se pudo leer: " + error.getMessage(),
                    8000, Notification.Position.MIDDLE);
        }
    }

    // subidaPara(): boton de subida que entrega el contenido al servicio.
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
