package bo.umss.mercado.servidor.vista;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout {

    public MainLayout() {
        H1 titulo = new H1("Mercado Titulados · Ing. Sistemas UMSS");
        titulo.getStyle().set("font-size", "1.2em").set("margin", "0");

        HorizontalLayout barra = new HorizontalLayout(new DrawerToggle(), titulo);
        barra.setAlignItems(FlexComponent.Alignment.CENTER);
        addToNavbar(barra);

        Tabs menu = new Tabs(
                pestañaPara("Panel", VistaPanel.class),
                pestañaPara("Titulados", VistaTitulados.class),
                pestañaPara("Empleadores", VistaEmpleadores.class),
                pestañaPara("Simulación", VistaSimulacion.class),
                pestañaPara("Registrar", VistaRegistro.class),
                pestañaPara("Importar CSV", VistaImportar.class),
                pestañaPara("Exportar", VistaExportar.class));
        menu.setOrientation(Tabs.Orientation.VERTICAL);
        addToDrawer(menu);
    }

    private Tab pestañaPara(String texto, Class<? extends Component> vista) {
        RouterLink enlace = new RouterLink(texto, vista);
        return new Tab(enlace);
    }
}