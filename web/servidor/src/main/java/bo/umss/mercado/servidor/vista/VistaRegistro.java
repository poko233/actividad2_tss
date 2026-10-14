package bo.umss.mercado.servidor.vista;

import bo.umss.mercado.servidor.datos.Empleador;
import bo.umss.mercado.servidor.datos.RepositorioEmpleadores;
import bo.umss.mercado.servidor.datos.RepositorioTitulados;
import bo.umss.mercado.servidor.datos.Titulado;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

// VistaRegistro: formularios para cargar encuestas a mano.
// Listas cerradas (ComboBox) en vez de texto libre: así las categorías
// salen normalizadas y los cruces no se fragmentan. Al guardar, la fila
// va directo a la base SQLite (sin pasar por CSV).
@Route(value = "registro", layout = MainLayout.class)
@PageTitle("Registrar | Mercado UMSS")
public class VistaRegistro extends VerticalLayout {

    private static final String[] SI_NO = {"SI", "NO"};
    private static final String[] AREAS = {"IA", "ROBOTICA", "ING_SOFTWARE",
            "BASE_DATOS", "REDES_SEGURIDAD"};
    private static final String[] LIKERT_4 = {"TOTALMENTE_DESACUERDO",
            "DESACUERDO", "ACUERDO", "TOTALMENTE_ACUERDO"};

    private final RepositorioTitulados repoTitulados;
    private final RepositorioEmpleadores repoEmpleadores;

    public VistaRegistro(RepositorioTitulados repoTitulados,
            RepositorioEmpleadores repoEmpleadores) {
        this.repoTitulados = repoTitulados;
        this.repoEmpleadores = repoEmpleadores;

        add(new H1("Registrar encuesta"));
        Tabs pestanas = new Tabs(new Tab("Titulado"), new Tab("Empleador"));
        VerticalLayout contenido = new VerticalLayout(formularioTitulado());
        pestanas.addSelectedChangeListener(cambio -> {
            contenido.removeAll();
            if (cambio.getSelectedTab().getLabel().equals("Titulado")) {
                contenido.add(formularioTitulado());
            } else {
                contenido.add(formularioEmpleador());
            }
        });
        add(pestanas, contenido);
    }

    // formularioTitulado(): campos principales (el CSV admite los 35, aquí
    // los 14 que alimentan los indicadores; el resto queda vacío).
    private FormLayout formularioTitulado() {
        IntegerField edad = new IntegerField("Edad");
        edad.setMin(18);
        edad.setMax(100);
        ComboBox<String> genero =
                combo("Género", new String[]{"MASCULINO", "FEMENINO", "OTRO"});
        ComboBox<String> situacion = combo("Situación laboral",
                new String[]{"TRABAJA_ORG", "NO_TRABAJA", "EMPRENDIMIENTO"});
        IntegerField aniosVida =
                new IntegerField("Años de vida profesional");
        aniosVida.setMin(0);
        aniosVida.setMax(60);
        IntegerField aniosDesempleo = new IntegerField("Años desempleado");
        aniosDesempleo.setMin(0);
        aniosDesempleo.setMax(60);
        ComboBox<String> interes =
                combo("¿Interés en posgrado?", SI_NO);
        ComboBox<String> area = combo("Área de interés", AREAS);
        ComboBox<String> financiamiento = combo("Financiamiento",
                new String[]{"RECURSOS_PROPIOS", "BECA_TOTAL", "BECA_PARCIAL"});
        ComboBox<String> satisfaccion =
                combo("Satisfacción con la formación", LIKERT_4);
        ComboBox<String> concordancia =
                combo("Concordancia formación-trabajo", LIKERT_4);
        ComboBox<String> red = combo("¿Unirse a la red?", SI_NO);
        TextField email = new TextField("Email");

        Button guardar = new Button("Guardar titulado");
        guardar.addClickListener(clic -> {
            Titulado titulado = new Titulado();
            titulado.setIdTitulado("T-WEB-" + (repoTitulados.count() + 1));
            titulado.setEdad(textoDe(edad.getValue()));
            titulado.setGenero(valorDe(genero));
            titulado.setSituacionLaboralActual(valorDe(situacion));
            titulado.setAniosVidaProfesional(textoDe(aniosVida.getValue()));
            titulado.setAniosDesempleado(textoDe(aniosDesempleo.getValue()));
            titulado.setInteresPosgrado(valorDe(interes));
            titulado.setAreaInteresPosgrado(valorDe(area));
            titulado.setFinanciamientoFormacion(valorDe(financiamiento));
            titulado.setSatisfaccionFormacion(valorDe(satisfaccion));
            titulado.setConcordanciaFormacionRequerimientos(
                    valorDe(concordancia));
            titulado.setRedContactos(valorDe(red));
            titulado.setEmailContacto(email.getValue());
            repoTitulados.save(titulado);
            Notification.show("Titulado guardado en la base.");
            edad.clear();
            email.clear();
        });

        FormLayout formulario = new FormLayout();
        formulario.add(edad, genero, situacion, aniosVida, aniosDesempleo,
                interes, area, financiamiento, satisfaccion, concordancia,
                red, email, guardar);
        return formulario;
    }

    // formularioEmpleador(): datos + 3 Likert clave generados en bucle
    // (los 10 completos entran por CSV en "Importar CSV").
    private FormLayout formularioEmpleador() {
        TextField empresa = new TextField("Nombre empresa");
        ComboBox<String> tipo =
                combo("Tipo", new String[]{"PUBLICO", "PRIVADO"});
        ComboBox<String> tamano = combo("Tamaño",
                new String[]{"GRANDE", "MEDIANA", "PEQUENA", "MICRO"});
        ComboBox<String> sector = combo("Sector", new String[]{
                "DESARROLLO_SOFTWARE", "SERVICIOS_TI", "BANCA_FINANCIERO",
                "TELECOM_REDES", "EDUCACION_INVESTIGACION", "STARTUPS",
                "CONSULTORIA", "OTROS"});
        ComboBox<String> contrato =
                combo("¿Contrató los últimos 5 años?", SI_NO);
        String[] escala = {"TOTALMENTE_ACUERDO", "PARCIALMENTE_ACUERDO",
                "TOTALMENTE_DESACUERDO", "NO_SABE"};

        FormLayout formulario = new FormLayout();
        formulario.add(new H3("Nuevo empleador"),
                empresa, tipo, tamano, sector, contrato);
        // Los 3 Likert se crean en bucle para no repetir código.
        String[][] likerts = {
                {"likert_confianza_formadora", "Confianza formadora"},
                {"likert_consulta_regular", "Consulta regular"},
                {"likert_coherencia_perfil", "Coherencia perfil"}};
        java.util.List<ComboBox<String>> camposLikert = new java.util.ArrayList<>();
        for (String[] likert : likerts) {
            ComboBox<String> campo = combo(likert[1], escala);
            camposLikert.add(campo);
            formulario.add(campo);
        }
        Button guardar = new Button("Guardar empleador");
        guardar.addClickListener(clic -> {
            if (empresa.getValue().trim().isEmpty()) {
                Notification.show("El nombre de la empresa es obligatorio.");
                return;
            }
            Empleador empleador = new Empleador();
            empleador.setIdEmpleador("E-WEB-" + (repoEmpleadores.count() + 1));
            empleador.setNombreEmpresa(empresa.getValue().trim());
            empleador.setTipoOrganizacion(valorDe(tipo));
            empleador.setTamanoOrganizacion(valorDe(tamano));
            empleador.setSectorRubro(valorDe(sector));
            empleador.setContratoUltimos5Anios(valorDe(contrato));
            empleador.setLikertConfianzaFormadora(
                    valorDe(camposLikert.get(0)));
            empleador.setLikertConsultaRegular(valorDe(camposLikert.get(1)));
            empleador.setLikertCoherenciaPerfil(valorDe(camposLikert.get(2)));
            repoEmpleadores.save(empleador);
            Notification.show("Empleador guardado en la base.");
            empresa.clear();
        });
        formulario.add(guardar);
        return formulario;
    }

    // combo(): lista cerrada con etiqueta (evita texto libre dispar).
    private ComboBox<String> combo(String titulo, String[] opciones) {
        ComboBox<String> lista = new ComboBox<>(titulo);
        lista.setItems(opciones);
        lista.setWidthFull();
        return lista;
    }

    private String valorDe(ComboBox<String> lista) {
        return lista.getValue() == null ? "" : lista.getValue();
    }

    private String textoDe(Integer numero) {
        return numero == null ? "" : String.valueOf(numero);
    }
}
