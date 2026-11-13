package bo.umss.mercado.servidor.vista;

import bo.umss.mercado.servidor.servicio.FichaCruce;
import bo.umss.mercado.servidor.servicio.ServicioCruces;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "cruces", layout = MainLayout.class)
@PageTitle("Cruces χ² | Mercado UMSS")
public class VistaCruces extends VerticalLayout {

    public VistaCruces(ServicioCruces cruces) {
        add(new H1("Cruces de variables (χ²)"));
        add(new Paragraph("Esta pantalla se mantiene por compatibilidad. "
                + "Los cruces también están disponibles dentro de cada pestaña "
                + "(Titulados → Bivariado y Empleadores → Bivariado)."));

        add(new H3("Titulados"));
        for (FichaCruce c : cruces.crucesTitulados()) {
            add(PanelCruce.render(c, cruces));
        }
        add(new H3("Empleadores"));
        for (FichaCruce c : cruces.crucesEmpleadores()) {
            add(PanelCruce.render(c, cruces));
        }
    }
}