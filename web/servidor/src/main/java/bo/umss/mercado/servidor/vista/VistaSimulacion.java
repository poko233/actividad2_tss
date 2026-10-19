package bo.umss.mercado.servidor.vista;

import bo.umss.mercado.nucleo.guia.GuiasLectura;
import bo.umss.mercado.servidor.datos.Simulacion;
import bo.umss.mercado.servidor.servicio.FichaEscenario;
import bo.umss.mercado.servidor.servicio.ServicioSimulacion;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;

/**
 * VistaSimulacion: botón para correr los 5 escenarios + tabla de
 * pronósticos con frase simple + historial guardado en la base.
 * Cada clic guarda una corrida nueva (queda registrada con fecha).
 */
@Route(value = "simulacion", layout = MainLayout.class)
@PageTitle("Simulación | Mercado UMSS")
public class VistaSimulacion extends VerticalLayout {

    private final ServicioSimulacion simulacion;
    private final VerticalLayout zonaResultados = new VerticalLayout();
    private final Grid<Simulacion> tablaHistorial = new Grid<>(Simulacion.class,
            false);

    public VistaSimulacion(ServicioSimulacion simulacion) {
        this.simulacion = simulacion;

        add(new H1("Proyección de demanda (Monte Carlo ligero)"));
        add(new Details("Cómo leer esta pantalla",
                new Paragraph(GuiasLectura.COMO_LEER_MONTECARLO)));

        Button correr = new Button("Correr simulación (1000 sorteos × 5)");
        correr.addClickListener(clic -> correrSimulacion());
        add(correr, zonaResultados);

        add(new H3("Historial guardado en la base"));
        tablaHistorial.addColumn(Simulacion::getFechaHora).setHeader("Fecha");
        tablaHistorial.addColumn(Simulacion::getEscenario)
                .setHeader("Escenario");
        tablaHistorial.addColumn(s -> String.valueOf(
                Math.round(s.getProporcionEmpirica() * 1000.0) / 10.0) + "%")
                .setHeader("% afín real");
        tablaHistorial.addColumn(s -> String.valueOf(
                Math.round(s.getDemandaMedia() * 100.0) / 100.0))
                .setHeader("Demanda media");
        tablaHistorial.addColumn(Simulacion::fraseSimple)
                .setHeader("En palabras simples");
        tablaHistorial.setWidthFull();
        refrescarHistorial();
        add(tablaHistorial);
    }

    // correrSimulacion(): pide las 5 fichas, las muestra y avisa.
    // El servicio ya guardó cada corrida en la base.
    private void correrSimulacion() {
        zonaResultados.removeAll();
        List<FichaEscenario> fichas = simulacion.simularTodo();
        for (FichaEscenario ficha : fichas) {
            zonaResultados.add(new Paragraph("[" + ficha.titulo() + "] "
                    + ficha.fraseSimple()));
        }
        refrescarHistorial();
        Notification.show("Simulación guardada en la base de datos.");
    }

    private void refrescarHistorial() {
        tablaHistorial.setItems(simulacion.historial());
    }
}
