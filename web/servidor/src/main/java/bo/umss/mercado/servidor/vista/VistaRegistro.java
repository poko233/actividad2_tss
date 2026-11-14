package bo.umss.mercado.servidor.vista;

import bo.umss.mercado.servidor.datos.Empleador;
import bo.umss.mercado.servidor.datos.RepositorioEmpleadores;
import bo.umss.mercado.servidor.datos.RepositorioTitulados;
import bo.umss.mercado.servidor.datos.Titulado;
import bo.umss.mercado.servidor.servicio.ServicioRegistro;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Route(value = "registro", layout = MainLayout.class)
@PageTitle("Registrar | Mercado UMSS")
public class VistaRegistro extends VerticalLayout {

    private static final String AZUL = "#0b3d62";
    private static final String AZUL2 = "#0984e3";
    private static final String VERDE = "#27ae60";
    private static final String NARANJA = "#e17055";
    private static final String MORADO = "#6c5ce7";
    private static final String GRIS = "#7f8c8d";

    // SVGs (Material Design Icons)
    private static final String SVG_USERS = "M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z";
    private static final String SVG_BUILDING = "M12 7V3H2v18h20V7H12zM6 19H4v-2h2v2zm0-4H4v-2h2v2zm0-4H4V9h2v2zm0-4H4V5h2v2zm4 12H8v-2h2v2zm0-4H8v-2h2v2zm0-4H8V9h2v2zm0-4H8V5h2v2zm10 12h-8v-2h2v-2h-2v-2h2v-2h-2V9h8v10zm-2-8h-2v2h2v-2zm0 4h-2v2h2v-2z";
    private static final String SVG_SAVE = "M17 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V7l-4-4zm-5 16c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm3-10H5V5h10v4z";
    private static final String SVG_CLEAR = "M19 6.41 17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z";

    private static final String[] SI_NO = { "SI", "NO" };
    private static final String[] GENERO = { "HOMBRE", "MUJER", "OTRO" };
    private static final String[] NIVEL_POSGRADO = { "DIPLOMADO", "ESPECIALIDAD", "MAESTRIA", "DOCTORADO",
            "POSDOCTORADO" };
    private static final String[] INSTITUCION = { "EUPG", "DIR_POSGRADO_FCyT", "UNIV_PUBLICAS_NAC", "UNIV_PRIVADAS_NAC",
            "UNIV_INTERNACIONALES", "IBNORCA", "OTRO" };
    private static final String[] FINANCIAMIENTO_FORM = { "RECURSOS_PROPIOS", "BECA_TOTAL", "BECA_PARCIAL",
            "RECURSOS_ESTATALES", "FINANCIAMIENTO_EMPRESA", "CREDITO_EDUCATIVO" };
    private static final String[] AREA_INTERES = { "IA", "ING_SOFTWARE", "REDES_SEGURIDAD", "BASE_DATOS", "ROBOTICA",
            "OTRA" };
    private static final String[] SITUACION = { "TRABAJA_ORG", "NO_TRABAJA", "EMPRENDIMIENTO" };
    private static final String[] TIEMPO_PRIMER = { "YA_TRABAJABA", "MENOS_1_MES", "1_4_MESES", "4_8_MESES",
            "8_12_MESES", "MAS_12_MESES" };
    private static final String[] SECTOR_TRAB = { "PRIVADO", "PUBLICO" };
    private static final String[] DEPARTAMENTO = { "COCHABAMBA", "SANTA_CRUZ", "LA_PAZ", "ORURO", "POTOSI", "TARIJA",
            "CHUQUISACA", "BENI", "PANDO", "EXTRANJERO" };
    private static final String[] AREA_ORG = { "DESARROLLO", "COMPUTO", "ADMINISTRACION", "CONTABILIDAD", "R.R.H.H.",
            "OTRA" };
    private static final String[] CARGO = { "DESARROLLO", "EJECUTIVO", "TECNICO", "HELP_DESK", "OTRO" };
    private static final String[] ANTIGUEDAD = { "MENOS_1_ANIO", "1_2_ANIOS", "2_5_ANIOS", "5_8_ANIOS", "8_11_ANIOS",
            "11_15_ANIOS", "15_20_ANIOS", "20_25_ANIOS", "MAS_25_ANIOS" };
    private static final String[] MEDIO_OBT = { "POSTULACION_COMPETITIVA", "INTERNET_REDES", "RECOMENDACION_TERCEROS",
            "MEDIOS_ESCRITOS_AUDIOVISUALES", "OTROS" };
    private static final String[] REMUNERACION = { "MENOS_2500", "2500_3500", "3501_5000", "5001_7000", "7001_10000",
            "MAS_10000" };
    private static final String[] ORIGEN_EMP = { "IDEA_PROPIA", "IDEA_CONJUNTA", "STARTUP", "SPINOFF", "OTRO" };
    private static final String[] ENTREGABLE = { "PRODUCTO", "SERVICIO", "AMBOS" };
    private static final String[] FIN_NEGOCIO = { "PROPIO", "INVERSORES", "PRESTAMO_BANCARIO" };
    private static final String[] SAT_NEGOCIO = { "SATISFECHO", "ALGO_SATISFECHO", "INSATISFECHO" };
    private static final String[] IMP_NEGOCIO = { "MUY_IMPORTANTE", "IMPORTANTE", "POCO_IMPORTANTE", "NO_IMPORTANTE" };
    private static final String[] RAZON_NO_TRAB = { "INCUMPLIMIENTO_HABILIDADES", "DEMANDA_INEXISTENTE",
            "INCUMPLIMIENTO_EXPERIENCIA", "OFERTAS_BAJAS", "RED_CONTACTOS", "HABILIDADES_ENTREVISTA", "OTRO" };
    private static final String[] LIKERT_4 = { "TOTALMENTE_DESACUERDO", "DESACUERDO", "ACUERDO", "TOTALMENTE_ACUERDO" };
    private static final String[] LIKERT_3 = { "TOTALMENTE_DESACUERDO", "PARCIALMENTE_ACUERDO", "TOTALMENTE_ACUERDO",
            "NO_SABE" };

    private static final String[] ASPECTOS_UTILES = {
            "Aplicación práctica de conceptos teóricos en proyectos reales",
            "Desarrollo de habilidades analíticas y de resolución de problemas",
            "Enseñanza de herramientas y software específico de la industria",
            "Oportunidad de realizar pasantías o prácticas profesionales",
            "Colaboración de equipos multidisciplinarios",
            "Distribución de los horarios de clases" };
    private static final String[] ASPECTOS_MEJORAR = {
            "Actualización constante del plan de estudios",
            "Mayor relacionamiento con las empresas (prácticas)",
            "Mayor integración de la teoría con la práctica",
            "Ofrecimiento de más opciones de especialización",
            "Distribución de horarios de clases",
            "Incremento de actividades extracurriculares" };
    private static final String[] ASIGNATURAS = {
            "Base de Datos", "Redes de Computadora", "T.I.S.", "Tecnologia Redes Avanzadas",
            "Electivas", "Inteligencia Artificial", "Planificación de Proyectos y Gestión",
            "Introducción", "Básicas (Física, Calculo, Algebras)", "Taller de Programación en Bajo Nivel",
            "Contabilidad Básica", "Aplic. Interactivas para Televisión Digital", "Web Semánticas",
            "Telefonía IP", "Robótica", "Teoría de Grafos", "Teoría de Autómatas y Leng. Formales",
            "Programación Funcional", "Graficación por Computadora", "Costos", "Ingeniería económica" };

    private static final String[] TIPO_ORG = { "PUBLICO", "PRIVADO" };
    private static final String[] TAMANO = { "MICRO", "PEQUENA", "MEDIANA", "GRANDE" };
    private static final String[] SECTOR_EMP = {
            "DESARROLLO_SOFTWARE", "SERVICIOS_TI", "TELECOM_REDES", "SOPORTE_TECNICO",
            "CONSULTORIA", "BANCA_FINANCIERO", "INSTITUCIONES_PUBLICAS",
            "EDUCACION_INVESTIGACION", "INDUSTRIA_MANUFACTURA", "COMERCIO_ELECTRONICO",
            "SALUD", "SEGURIDAD_INFORMATICA", "ANALISIS_DATOS_BI", "STARTUPS", "OTROS" };
    private static final String[] POSIBILIDAD = { "ALTA_PROBABILIDAD", "DEPENDE_CRECIMIENTO", "NO_PREVISTA",
            "NO_REQUIERE" };
    private static final String[] NIVEL_DEMANDA = { "LICENCIATURA", "DIPLOMADO", "ESPECIALIDAD", "MAESTRIA",
            "DOCTORADO" };
    private static final String[] MEDIO_CONV = { "INTERNET_REDES", "POSTULACION_COMPETITIVA", "RECOMENDACION_TERCEROS",
            "MEDIOS_ESCRITOS_AUDIOVISUALES", "OFERTA_PROFESIONAL", "OTROS" };
    private static final String[] CARGOS_EMP = { "APOYO_ASISTENCIA", "TECNICO", "ANALISTA", "ESPECIALISTA",
            "SUPERVISOR", "JEFATURA_GERENCIAL", "CONSULTOR", "OTRO" };

    private static final String[] LIKERT_EMP = {
            "L1. Confianza como formadora",
            "L2. Consistencia del título",
            "L3. Consulta regular a empleadores",
            "L4. Conoce el perfil de egreso",
            "L5. Coherencia del perfil",
            "L6. Formación permite desempeño destacado",
            "L7. Incorpora opinión en mejoras",
            "L8. Desempeño evidencia competencias",
            "L9. Valores y actitudes",
            "L10. Participación en evaluación",
            "L11. Vínculos con organizaciones",
            "L12. Participación en vinculación",
            "L13. Difusión de oportunidades",
            "L14. Vinculación entre profesionales",
            "L15. Consultado sobre desempeño",
            "L16. Consultado sobre pertinencia",
            "L17. Recurre a la UMSS",
            "L18. Consultado en autoevaluación" };

    private final ServicioRegistro servicio;
    private final RepositorioTitulados repoTitulados;
    private final RepositorioEmpleadores repoEmpleadores;

    private final Map<String, AbstractField<?, ?>> cTit = new LinkedHashMap<>();
    private final Map<String, AbstractField<?, ?>> cEmp = new LinkedHashMap<>();
    private final Span contadorTit = new Span();
    private final Span contadorEmp = new Span();

    public VistaRegistro(ServicioRegistro servicio,
            RepositorioTitulados repoTitulados,
            RepositorioEmpleadores repoEmpleadores) {
        this.servicio = servicio;
        this.repoTitulados = repoTitulados;
        this.repoEmpleadores = repoEmpleadores;

        inyectarCss();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background", "#f8fafc").set("min-height", "100vh");

        add(construirHeader());
        add(construirSubtitulo());

        Tabs pestanas = new Tabs(new Tab("Titulado"), new Tab("Empleador"));
        pestanas.setWidthFull();
        VerticalLayout contenido = new VerticalLayout();
        contenido.setPadding(false);
        contenido.add(construirTabTitulado());
        pestanas.addSelectedChangeListener(ev -> {
            contenido.removeAll();
            if ("Titulado".equals(ev.getSelectedTab().getLabel())) {
                contenido.add(construirTabTitulado());
            } else {
                contenido.add(construirTabEmpleador());
            }
        });
        add(pestanas, contenido);
    }

    // ================================================================
    // Header + subtítulo
    // ================================================================
    private Div construirHeader() {
        Div header = new Div();
        header.getStyle()
                .set("background", AZUL)
                .set("padding", "28px 36px")
                .set("border-radius", "16px")
                .set("color", "white")
                .set("margin-bottom", "8px")
                .set("box-shadow", "0 6px 20px rgba(11,61,98,.2)")
                .set("position", "relative").set("overflow", "hidden");

        Div titulo = new Div();
        titulo.setText("Registrar encuesta");
        titulo.getStyle().set("font-size", "26px").set("font-weight", "700")
                .set("margin-bottom", "4px");

        Div sub = new Div();
        sub.setText("Carga manual de titulados y empleadores con el set completo de variables");
        sub.getStyle().set("font-size", "13px").set("opacity", "0.85");

        HorizontalLayout chips = new HorizontalLayout();
        chips.getStyle().set("gap", "10px").set("flex-wrap", "wrap").set("margin-top", "16px");
        chips.add(chip(SVG_USERS, "50 variables · Titulado"));
        chips.add(chip(SVG_BUILDING, "32 variables · Empleador"));

        Div circ = new Div();
        circ.getStyle().set("position", "absolute").set("top", "-60px").set("right", "-60px")
                .set("width", "180px").set("height", "180px")
                .set("background", "rgba(255,255,255,0.04)").set("border-radius", "50%");

        header.add(circ, titulo, sub, chips);
        return header;
    }

    private Div chip(String svg, String texto) {
        Div c = new Div();
        c.getStyle().set("background", "rgba(255,255,255,0.12)")
                .set("padding", "6px 14px").set("border-radius", "20px")
                .set("font-size", "12px").set("font-weight", "600")
                .set("border", "1px solid rgba(255,255,255,0.2)");
        c.getElement().executeJs("this.innerHTML = $0",
                "<span style='display:inline-flex;align-items:center;gap:6px;'>"
                        + svgIcon(svg, "#ffffff", 12) + "<span>" + esc(texto) + "</span></span>");
        return c;
    }

    private Paragraph construirSubtitulo() {
        Paragraph p = new Paragraph(
                "Los campos marcados como condicionales aparecerán u ocultarán "
                        + "según la rama elegida (situación laboral, formación complementaria, "
                        + "interés en posgrado). Los campos derivados (tamaño agrupado, rubro "
                        + "agrupado, demanda IA, tendencias, hard skills, soft skills) se calculan "
                        + "automáticamente al guardar.");
        p.getStyle().set("font-size", "13px").set("color", "#666")
                .set("background", "#ffffff").set("padding", "12px 16px")
                .set("border-left", "3px solid " + AZUL2).set("border-radius", "6px");
        return p;
    }

    // ================================================================
    // Tab Titulado
    // ================================================================
    private VerticalLayout construirTabTitulado() {
        cTit.clear();

        FormLayout fDemo = formDemografia();
        FormLayout fForm = formFormacion();
        FormLayout fTray = formTrayectoria();
        FormLayout fTrab = formTrabajoActual();
        FormLayout fEmpr = formEmprendimiento();
        FormLayout fDesemp = formDesempleo();
        FormLayout fPerc = formPercepcion();
        FormLayout fCont = formContacto();

        AccordionPanel pForm = new AccordionPanel("Formación complementaria", fForm);
        AccordionPanel pTrab = new AccordionPanel("Trabajo actual", fTrab);
        AccordionPanel pEmpr = new AccordionPanel("Emprendimiento", fEmpr);
        AccordionPanel pDesemp = new AccordionPanel("Desempleo", fDesemp);

        // Visibilidad condicional
        wireVisibilidadTitulado(pForm, pTrab, pEmpr, pDesemp);

        Accordion acc = new Accordion();
        acc.setWidthFull();
        acc.add(new AccordionPanel("Demografía", fDemo));
        acc.add(pForm);
        acc.add(new AccordionPanel("Trayectoria", fTray));
        acc.add(pTrab);
        acc.add(pEmpr);
        acc.add(pDesemp);
        acc.add(new AccordionPanel("Percepción formativa", fPerc));
        acc.add(new AccordionPanel("Contacto", fCont));
        acc.open(0);

        Button guardar = botonGuardar("Guardar titulado", SVG_SAVE, AZUL2);
        guardar.addClickListener(e -> guardarTitulado());

        Button limpiar = botonGuardar("Limpiar formulario", SVG_CLEAR, GRIS);
        limpiar.addClickListener(e -> {
            cTit.values().forEach(AbstractField::clear);
            Notification.show("Formulario limpio", 2000, Notification.Position.TOP_CENTER);
        });

        HorizontalLayout acciones = new HorizontalLayout(guardar, limpiar);
        acciones.setWidthFull();
        acciones.setJustifyContentMode(
                com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.END);
        acciones.getStyle().set("gap", "12px").set("margin-top", "20px");

        contadorTit.getStyle().set("color", "#666").set("font-size", "13px")
                .set("padding", "8px 16px").set("background", "#ffffff")
                .set("border-radius", "8px").set("align-self", "center");
        actualizarContadorTit();

        HorizontalLayout footer = new HorizontalLayout(contadorTit, acciones);
        footer.setWidthFull();
        footer.setJustifyContentMode(
                com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN);
        footer.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);

        VerticalLayout layout = new VerticalLayout(acc, footer);
        layout.setPadding(false);
        layout.setSpacing(true);
        return layout;
    }

    // ---------- Secciones Titulado ----------
    private FormLayout formDemografia() {
        FormLayout f = formBase();

        TextField id = campo("ID del titulado (opcional)", "T-WEB-A1B2C3D4", cTit, "id_titulado");
        IntegerField edad = intCampo("Edad", 18, 90, cTit, "edad");
        ComboBox<String> genero = combo("Género", GENERO, cTit, "genero");

        f.add(id, edad, genero);
        return f;
    }

    private FormLayout formFormacion() {
        FormLayout f = formBase();

        ComboBox<String> realizo = combo("¿Realizó formación complementaria?", SI_NO, cTit,
                "realizo_formacion_complementaria");
        ComboBox<String> nivel = combo("Nivel de posgrado alcanzado", NIVEL_POSGRADO, cTit, "nivel_posgrado");
        ComboBox<String> inst = combo("Institución donde cursó", INSTITUCION, cTit, "institucion_formacion");
        ComboBox<String> fin = combo("Fuente de financiamiento", FINANCIAMIENTO_FORM, cTit, "financiamiento_formacion");
        TextField nombreProg = campo("Nombre del programa", "Ej. Diplomado en Ciencia de Datos", cTit,
                "nombre_programa");
        ComboBox<String> interes = combo("¿Interés en realizar un posgrado?", SI_NO, cTit, "interes_posgrado");
        ComboBox<String> area = combo("Área de interés del posgrado", AREA_INTERES, cTit, "area_interes_posgrado");
        ComboBox<String> instFut = combo("Institución futura preferida", INSTITUCION, cTit,
                "institucion_futura_posgrado");
        RadioButtonGroup<String> opinion = radioLikert4("Opinión sobre el posgrado de la FCyT", cTit,
                "opinion_posgrado_fcyt");

        f.add(realizo, nivel, inst, fin, nombreProg, interes, area, instFut, opinion);
        return f;
    }

    private FormLayout formTrayectoria() {
        FormLayout f = formBase();

        IntegerField aniosVida = intCampo("Años de vida profesional", 0, 60, cTit, "anios_vida_profesional");
        NumberField aniosDesemp = numCampo("Años desempleado (acepta decimales)", 0.0, 60.0, cTit, "anios_desempleado");
        ComboBox<String> situacion = combo("Situación laboral actual", SITUACION, cTit, "situacion_laboral_actual");
        ComboBox<String> primerEmp = combo("¿Su trabajo actual es su primer empleo?", SI_NO, cTit,
                "primer_empleo_actual");
        ComboBox<String> tiempoPrimer = combo("Tiempo al primer empleo", TIEMPO_PRIMER, cTit, "tiempo_primer_empleo");
        IntegerField numEmp = intCampo("N° empleos desde la titulación", 0, 30, cTit, "num_empleos_desde_titulacion");

        f.add(aniosVida, aniosDesemp, situacion, primerEmp, tiempoPrimer, numEmp);
        return f;
    }

    private FormLayout formTrabajoActual() {
        FormLayout f = formBase();

        TextField empresa = campo("Nombre de la empresa", "Ej. Digital Harbor", cTit, "nombre_empresa");
        TextField rubro = campo("Rubro", "Ej. Desarrollo de Software", cTit, "rubro");
        TextField otroRubro = campo("Otro rubro (si aplica)", "", cTit, "otro_rubro");
        ComboBox<String> sector = combo("Sector", SECTOR_TRAB, cTit, "sector");
        ComboBox<String> depto = combo("Departamento", DEPARTAMENTO, cTit, "departamento");
        ComboBox<String> area = combo("Área de la organización", AREA_ORG, cTit, "area_organizacion");
        TextField otraArea = campo("Otra área (si aplica)", "", cTit, "otra_area");
        ComboBox<String> cargo = combo("Cargo", CARGO, cTit, "cargo");
        TextField otroCargo = campo("Otro cargo (si aplica)", "", cTit, "otro_cargo");
        RadioButtonGroup<String> pertinencia = radioLikert4("¿Sus labores son pertinentes a su formación?", cTit,
                "pertinencia_trabajo");
        ComboBox<String> antig = combo("Antigüedad en el puesto", ANTIGUEDAD, cTit, "antiguedad_puesto");
        ComboBox<String> medio = combo("Medio por el que obtuvo el empleo", MEDIO_OBT, cTit, "medio_obtencion_empleo");
        ComboBox<String> rem = combo("Rango salarial mensual", REMUNERACION, cTit, "remuneracion_rango");

        f.add(empresa, rubro, otroRubro, sector, depto, area, otraArea, cargo, otroCargo,
                pertinencia, antig, medio, rem);
        return f;
    }

    private FormLayout formEmprendimiento() {
        FormLayout f = formBase();

        ComboBox<String> origen = combo("Origen del emprendimiento", ORIGEN_EMP, cTit, "origen_emprendimiento");
        ComboBox<String> entregable = combo("Tipo de entregable", ENTREGABLE, cTit, "tipo_entregable_negocio");
        ComboBox<String> fin = combo("Financiamiento del negocio", FIN_NEGOCIO, cTit, "financiamiento_negocio");
        ComboBox<String> sat = combo("Satisfacción con el negocio", SAT_NEGOCIO, cTit, "satisfaccion_negocio");
        ComboBox<String> imp = combo("Importancia de la formación", IMP_NEGOCIO, cTit, "importancia_formacion_negocio");

        f.add(origen, entregable, fin, sat, imp);
        return f;
    }

    private FormLayout formDesempleo() {
        FormLayout f = formBase();

        ComboBox<String> razon = combo("Razón por la que no trabaja", RAZON_NO_TRAB, cTit, "razon_no_trabaja");
        ComboBox<String> haTrabajado = combo("¿Ha tenido trabajo antes?", SI_NO, cTit, "ha_tenido_trabajo_antes");

        f.add(razon, haTrabajado);
        return f;
    }

    private FormLayout formPercepcion() {
        FormLayout f = formBase();

        RadioButtonGroup<String> sat = radioLikert4("Satisfacción global con la formación", cTit,
                "satisfaccion_formacion");
        RadioButtonGroup<String> conc = radioLikert4("Concordancia formación-requerimientos", cTit,
                "concordancia_formacion_requerimientos");
        MultiSelectComboBox<String> utiles = multiCombo("Aspectos útiles", ASPECTOS_UTILES, cTit, "aspectos_utiles");
        MultiSelectComboBox<String> mejorar = multiCombo("Aspectos a mejorar", ASPECTOS_MEJORAR, cTit,
                "aspectos_mejorar");
        MultiSelectComboBox<String> asigVent = multiCombo("Asignaturas ventajosas", ASIGNATURAS, cTit,
                "asignaturas_ventajosas");
        MultiSelectComboBox<String> asigInut = multiCombo("Asignaturas inútiles", ASIGNATURAS, cTit,
                "asignaturas_inutiles");
        TextField otraVent = campo("Otra asignatura ventajosa", "", cTit, "asignatura_ventajosa_otra");
        TextField otraInut = campo("Otra asignatura inútil", "", cTit, "asignatura_inutil_otra");

        f.add(sat, conc, utiles, mejorar, asigVent, asigInut, otraVent, otraInut);
        return f;
    }

    private FormLayout formContacto() {
        FormLayout f = formBase();

        ComboBox<String> red = combo("¿Le interesa unirse a la red de contactos?", SI_NO, cTit, "red_contactos");
        EmailField email = emailCampo("Email de contacto (opcional)", cTit, "email_contacto");
        TextField tel = campo("Teléfono / WhatsApp (opcional)", "", cTit, "telefono_contacto");
        TextField li = campo("LinkedIn (opcional)", "https://linkedin.com/in/…", cTit, "linkedin_contacto");

        f.add(red, email, tel, li);
        return f;
    }

    private void wireVisibilidadTitulado(AccordionPanel pForm, AccordionPanel pTrab,
            AccordionPanel pEmpr, AccordionPanel pDesemp) {
        AbstractField<?, ?> realizo = cTit.get("realizo_formacion_complementaria");
        AbstractField<?, ?> interes = cTit.get("interes_posgrado");
        AbstractField<?, ?> situacion = cTit.get("situacion_laboral_actual");

        Runnable refrescarForm = () -> {
            boolean si = "SI".equals(str(realizo));
            setVisible(cTit, "nivel_posgrado", si);
            setVisible(cTit, "institucion_formacion", si);
            setVisible(cTit, "financiamiento_formacion", si);
            setVisible(cTit, "nombre_programa", si);
        };
        Runnable refrescarInteres = () -> {
            boolean si = "SI".equals(str(interes));
            setVisible(cTit, "area_interes_posgrado", si);
            setVisible(cTit, "institucion_futura_posgrado", si);
        };
        Runnable refrescarSituacion = () -> {
            String s = str(situacion);
            boolean trabaja = "TRABAJA_ORG".equals(s);
            boolean emprende = "EMPRENDIMIENTO".equals(s);
            boolean desempleado = "NO_TRABAJA".equals(s);

            pTrab.setVisible(trabaja);
            pEmpr.setVisible(emprende);
            pDesemp.setVisible(desempleado);
        };

        if (realizo != null) {
            realizo.addValueChangeListener(e -> refrescarForm.run());
        }
        if (interes != null) {
            interes.addValueChangeListener(e -> refrescarInteres.run());
        }
        if (situacion != null) {
            situacion.addValueChangeListener(e -> refrescarSituacion.run());
        }

        refrescarForm.run();
        refrescarInteres.run();
        refrescarSituacion.run();
    }

    private void guardarTitulado() {
        try {
            Map<String, String> datos = snapshot(cTit);
            Titulado guardado = servicio.guardarTitulado(datos);
            Notification.show("Titulado guardado: " + guardado.getIdTitulado(),
                    3500, Notification.Position.TOP_CENTER);
            cTit.values().forEach(AbstractField::clear);
            actualizarContadorTit();
        } catch (Exception ex) {
            Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER);
        }
    }

    private void actualizarContadorTit() {
        contadorTit.setText("Registrados en la base: " + repoTitulados.count() + " titulados");
    }

    // ================================================================
    // Tab Empleador
    // ================================================================
    private VerticalLayout construirTabEmpleador() {
        cEmp.clear();

        FormLayout fId = formIdentEmpleador();
        FormLayout fCar = formCaractEmpleador();
        FormLayout fDem = formDemandaEmpleador();
        FormLayout fCarComp = formCargosCompetencias();
        FormLayout fLikert = formLikert18();

        Accordion acc = new Accordion();
        acc.setWidthFull();
        acc.add(new AccordionPanel("Identificación", fId));
        acc.add(new AccordionPanel("Caracterización", fCar));
        acc.add(new AccordionPanel("Demanda y proyección", fDem));
        acc.add(new AccordionPanel("Cargos y competencias", fCarComp));
        acc.add(new AccordionPanel("Evaluación institucional (18 Likert)", fLikert));
        acc.open(0);

        Button guardar = botonGuardar("Guardar empleador", SVG_SAVE, VERDE);
        guardar.addClickListener(e -> guardarEmpleador());

        Button limpiar = botonGuardar("Limpiar formulario", SVG_CLEAR, GRIS);
        limpiar.addClickListener(e -> {
            cEmp.values().forEach(AbstractField::clear);
            Notification.show("Formulario limpio", 2000, Notification.Position.TOP_CENTER);
        });

        HorizontalLayout acciones = new HorizontalLayout(guardar, limpiar);
        acciones.setWidthFull();
        acciones.setJustifyContentMode(
                com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.END);
        acciones.getStyle().set("gap", "12px").set("margin-top", "20px");

        contadorEmp.getStyle().set("color", "#666").set("font-size", "13px")
                .set("padding", "8px 16px").set("background", "#ffffff")
                .set("border-radius", "8px").set("align-self", "center");
        actualizarContadorEmp();

        HorizontalLayout footer = new HorizontalLayout(contadorEmp, acciones);
        footer.setWidthFull();
        footer.setJustifyContentMode(
                com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN);
        footer.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);

        VerticalLayout layout = new VerticalLayout(acc, footer);
        layout.setPadding(false);
        layout.setSpacing(true);
        return layout;
    }

    private FormLayout formIdentEmpleador() {
        FormLayout f = formBase();
        TextField id = campo("ID del empleador (opcional)", "E-WEB-A1B2C3D4", cEmp, "id_empleador");
        EmailField correo = emailCampo("Correo de contacto", cEmp, "correo");
        TextField nombre = campo("Nombre de la empresa / institución (obligatorio)", "", cEmp, "nombre_empresa");
        f.add(id, correo, nombre);
        return f;
    }

    private FormLayout formCaractEmpleador() {
        FormLayout f = formBase();
        ComboBox<String> tipo = combo("Tipo de organización", TIPO_ORG, cEmp, "tipo_organizacion");
        ComboBox<String> tam = combo("Tamaño de la organización", TAMANO, cEmp, "tamano_organizacion");
        ComboBox<String> sector = combo("Sector / rubro principal", SECTOR_EMP, cEmp, "sector_rubro");
        f.add(tipo, tam, sector);
        return f;
    }

    private FormLayout formDemandaEmpleador() {
        FormLayout f = formBase();
        ComboBox<String> contrato = combo("¿Contrató Ingenieros en los últimos 5 años?", SI_NO, cEmp,
                "contrato_ultimos_5_anios");
        ComboBox<String> proy = combo("Proyección de contratación", POSIBILIDAD, cEmp,
                "posibilidad_futura_contratacion");
        ComboBox<String> nivel = combo("Nivel académico demandado", NIVEL_DEMANDA, cEmp, "nivel_formacion_demanda");
        MultiSelectComboBox<String> medio = multiCombo("Medios de convocatoria", MEDIO_CONV, cEmp,
                "medio_convocatoria");
        f.add(contrato, proy, nivel, medio);
        return f;
    }

    private FormLayout formCargosCompetencias() {
        FormLayout f = formBase();
        MultiSelectComboBox<String> cargos = multiCombo("Cargos desempeñados", CARGOS_EMP, cEmp, "cargos_desempenados");
        TextArea areas = textArea("Áreas de conocimiento emergentes", 3, cEmp, "areas_conocimiento_emergentes");
        TextArea herr = textArea("Herramientas tecnológicas", 3, cEmp, "herramientas_tecnologicas");
        TextArea hab = textArea("Habilidades / competencias requeridas", 3, cEmp,
                "habilidades_competencias_requeridas");
        f.add(cargos, areas, herr, hab);
        return f;
    }

    private FormLayout formLikert18() {
        FormLayout f = formBase();
        for (int i = 0; i < LIKERT_EMP.length; i++) {
            String clave = switch (i) {
                case 0 -> "likert_confianza_formadora";
                case 1 -> "likert_consistencia_titulo";
                case 2 -> "likert_consulta_regular";
                case 3 -> "likert_conoce_perfil";
                case 4 -> "likert_coherencia_perfil";
                case 5 -> "likert_formacion_desempeno";
                case 6 -> "likert_considera_opinion";
                case 7 -> "likert_competencias_laborales";
                case 8 -> "likert_valores_actitudes";
                case 9 -> "likert_participacion_procesos";
                case 10 -> "likert_vinculos_organizaciones";
                case 11 -> "likert_participacion_vinculacion";
                case 12 -> "likert_difusion_oportunidades";
                case 13 -> "likert_vinculacion_profesionales";
                case 14 -> "likert_consultado_desempeno";
                case 15 -> "likert_consultado_pertinencia";
                case 16 -> "likert_recurre_umss";
                default -> "likert_consultado_autoevaluacion";
            };
            RadioButtonGroup<String> rb = new RadioButtonGroup<>(LIKERT_EMP[i]);
            rb.setItems(LIKERT_3);
            rb.getElement().setAttribute("theme", "horizontal");
            rb.setWidthFull();
            cEmp.put(clave, rb);
            f.add(rb);
        }
        return f;
    }

    private void guardarEmpleador() {
        try {
            Map<String, String> datos = snapshot(cEmp);
            Empleador guardado = servicio.guardarEmpleador(datos);
            Notification.show("Empleador guardado: " + guardado.getNombreEmpresa(),
                    3500, Notification.Position.TOP_CENTER);
            cEmp.values().forEach(AbstractField::clear);
            actualizarContadorEmp();
        } catch (Exception ex) {
            Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER);
        }
    }

    private void actualizarContadorEmp() {
        contadorEmp.setText("Registrados en la base: " + repoEmpleadores.count() + " empleadores");
    }

    // ================================================================
    // Helpers de construcción
    // ================================================================
    private FormLayout formBase() {
        FormLayout f = new FormLayout();
        f.setWidthFull();
        f.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2),
                new FormLayout.ResponsiveStep("1000px", 3));
        return f;
    }

    private TextField campo(String label, String placeholder,
            Map<String, AbstractField<?, ?>> map, String clave) {
        TextField t = new TextField(label);
        if (placeholder != null && !placeholder.isEmpty())
            t.setPlaceholder(placeholder);
        t.setWidthFull();
        map.put(clave, t);
        return t;
    }

    private EmailField emailCampo(String label,
            Map<String, AbstractField<?, ?>> map, String clave) {
        EmailField t = new EmailField(label);
        t.setWidthFull();
        map.put(clave, t);
        return t;
    }

    private IntegerField intCampo(String label, int min, int max,
            Map<String, AbstractField<?, ?>> map, String clave) {
        IntegerField t = new IntegerField(label);
        t.setMin(min);
        t.setMax(max);
        t.setWidthFull();
        map.put(clave, t);
        return t;
    }

    private NumberField numCampo(String label, double min, double max,
            Map<String, AbstractField<?, ?>> map, String clave) {
        NumberField t = new NumberField(label);
        t.setMin(min);
        t.setMax(max);
        t.setStepButtonsVisible(true);
        t.setWidthFull();
        map.put(clave, t);
        return t;
    }

    private TextArea textArea(String label, int filas,
            Map<String, AbstractField<?, ?>> map, String clave) {
        TextArea t = new TextArea(label);
        t.setMinRows(filas);
        t.setMaxRows(filas + 2);
        t.setWidthFull();
        map.put(clave, t);
        return t;
    }

    private ComboBox<String> combo(String label, String[] opciones,
            Map<String, AbstractField<?, ?>> map, String clave) {
        ComboBox<String> c = new ComboBox<>(label);
        c.setItems(opciones);
        c.setWidthFull();
        c.setClearButtonVisible(true);
        map.put(clave, c);
        return c;
    }

    private MultiSelectComboBox<String> multiCombo(String label, String[] opciones,
            Map<String, AbstractField<?, ?>> map, String clave) {
        MultiSelectComboBox<String> c = new MultiSelectComboBox<>(label);
        c.setItems(opciones);
        c.setWidthFull();
        c.setClearButtonVisible(true);
        map.put(clave, c);
        return c;
    }

    private RadioButtonGroup<String> radioLikert4(String label,
            Map<String, AbstractField<?, ?>> map, String clave) {
        RadioButtonGroup<String> r = new RadioButtonGroup<>(label);
        r.setItems(LIKERT_4);
        r.getElement().setAttribute("theme", "horizontal");
        r.setWidthFull();
        map.put(clave, r);
        return r;
    }

    private Button botonGuardar(String texto, String svg, String color) {
        Button b = new Button();
        b.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        b.getStyle()
                .set("background", color).set("color", "white")
                .set("border", "none").set("padding", "12px 22px")
                .set("border-radius", "10px").set("font-weight", "600")
                .set("font-size", "13px").set("cursor", "pointer")
                .set("box-shadow", "0 3px 10px " + color + "40")
                .set("transition", "all 0.3s ease");
        b.getElement().executeJs("this.innerHTML = $0",
                "<span style='display:inline-flex;align-items:center;gap:8px;'>"
                        + svgIcon(svg, "#ffffff", 16) + "<span>" + esc(texto) + "</span></span>");
        b.getElement().executeJs(
                "this.addEventListener('mouseenter', () => {"
                        + "  this.style.transform = 'translateY(-2px)';"
                        + "  this.style.boxShadow = '0 8px 20px " + color + "60';"
                        + "});"
                        + "this.addEventListener('mouseleave', () => {"
                        + "  this.style.transform = 'translateY(0)';"
                        + "  this.style.boxShadow = '0 3px 10px " + color + "40';"
                        + "});");
        return b;
    }

    // ================================================================
    // Utilidades
    // ================================================================
    private static void setVisible(Map<String, AbstractField<?, ?>> map, String clave, boolean visible) {
        AbstractField<?, ?> f = map.get(clave);
        if (f != null) {
            f.setVisible(visible);
        }
    }

    private static String str(AbstractField<?, ?> f) {
        if (f == null)
            return "";
        Object v = f.getValue();
        return v == null ? "" : v.toString();
    }

    private static String val(AbstractField<?, ?> f) {
        if (f == null)
            return "";
        Object v = f.getValue();
        if (v == null)
            return "";
        if (v instanceof Set<?> set) {
            return set.stream().map(Object::toString).collect(Collectors.joining("|"));
        }
        if (v instanceof Double d) {
            // Sin notación científica, sin .0 sobrante
            return d == Math.floor(d) && !d.isInfinite()
                    ? String.valueOf(d.longValue())
                    : String.valueOf(d);
        }
        return v.toString();
    }

    private static Map<String, String> snapshot(Map<String, AbstractField<?, ?>> c) {
        Map<String, String> m = new LinkedHashMap<>();
        for (var e : c.entrySet())
            m.put(e.getKey(), val(e.getValue()));
        return m;
    }

    private static String svgIcon(String path, String color, int size) {
        return "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' "
                + "width='" + size + "' height='" + size + "' fill='" + color + "'>"
                + "<path d='" + path + "'/></svg>";
    }

    private static String esc(String s) {
        if (s == null)
            return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void inyectarCss() {
        String css = """
                @keyframes registroFadeInUp {
                    from { opacity: 0; transform: translateY(16px); }
                    to { opacity: 1; transform: translateY(0); }
                }
                vaadin-accordion-panel {
                    animation: registroFadeInUp 0.5s cubic-bezier(0.4, 0, 0.2, 1) both;
                    background: #ffffff !important;
                    border-radius: 10px !important;
                    margin-bottom: 8px !important;
                    box-shadow: 0 1px 3px rgba(0,0,0,.05) !important;
                    border: 1px solid #eef2f6 !important;
                }
                vaadin-accordion-panel[opened] {
                    box-shadow: 0 4px 14px rgba(0,0,0,.08) !important;
                }
                vaadin-accordion-heading {
                    font-weight: 600 !important;
                    color: #0b3d62 !important;
                }
                vaadin-radio-group[theme="horizontal"] {
                    display: flex !important;
                    flex-direction: row !important;
                    flex-wrap: wrap !important;
                    gap: 12px !important;
                }
                """;
        getElement().executeJs(
                "const id='registro-anim';"
                        + "const p=document.getElementById(id);"
                        + "if(p)p.remove();"
                        + "const s=document.createElement('style');"
                        + "s.id=id;s.textContent=$0;"
                        + "document.head.appendChild(s);",
                css);
    }
}