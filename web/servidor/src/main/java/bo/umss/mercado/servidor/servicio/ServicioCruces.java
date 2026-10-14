package bo.umss.mercado.servidor.servicio;

import bo.umss.mercado.nucleo.guia.GuiasLectura;
import bo.umss.mercado.nucleo.stats.PruebaChiCuadrada;
import bo.umss.mercado.nucleo.stats.ResultadoChiCuadrada;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

// ServicioCruces: cruza DOS preguntas y dice si están relacionadas.
// Cada cruce trae su frase simple ("SÍ está relacionada...") para
// que nadie tenga que interpretar el número p a mano.
@Service
public class ServicioCruces {

    private final ServicioAnalisis analisis;

    public ServicioCruces(ServicioAnalisis analisis) {
        this.analisis = analisis;
    }

    // columnaDerivada(): resuelve columnas normales y la DERIVADA
    // "segmento_antiguedad" (no existe en la BD: se calcula de los años).
    private List<String> columnaDerivada(List<Map<String, String>> mapas,
            String columna) {
        if (columna.equals("segmento_antiguedad")) {
            List<String> segmentos = new ArrayList<>();
            for (Map<String, String> fila : mapas) {
                try {
                    double anios = Double.parseDouble(fila
                            .getOrDefault("anios_vida_profesional", "")
                            .replace(",", "."));
                    segmentos.add(ServicioAnalisis.segmentoDe(anios));
                } catch (Exception sinDato) {
                    segmentos.add("");
                }
            }
            return segmentos;
        }
        return analisis.columnaDe(mapas, columna);
    }

    private FichaCruce armarCruce(String etiqueta, String columnaA,
            String columnaB, List<Map<String, String>> mapas) {
        ResultadoChiCuadrada resultado = PruebaChiCuadrada.cruzar(
                columnaDerivada(mapas, columnaA),
                columnaDerivada(mapas, columnaB));
        return new FichaCruce(etiqueta, columnaA, columnaB, resultado,
                GuiasLectura.fraseDependencia(columnaA, columnaB,
                        resultado.valorP()));
    }

    // crucesTitulados(): los 5 cruces HU1/HU2/HU3 de la guía.
    public List<FichaCruce> crucesTitulados() {
        List<Map<String, String>> mapas = analisis.mapasTitulados();
        List<FichaCruce> cruces = new ArrayList<>();
        cruces.add(armarCruce("HU2: área x financiamiento",
                "area_interes_posgrado", "financiamiento_formacion", mapas));
        cruces.add(armarCruce("HU2/HU3: nivel x institución",
                "nivel_posgrado", "institucion_formacion", mapas));
        cruces.add(armarCruce("HU3: financiamiento x nivel",
                "financiamiento_formacion", "nivel_posgrado", mapas));
        cruces.add(armarCruce("HU1: situación x satisfacción",
                "situacion_laboral_actual", "satisfaccion_formacion", mapas));
        cruces.add(armarCruce("HU1: antigüedad x intención",
                "segmento_antiguedad", "interes_posgrado", mapas));
        return cruces;
    }

    // crucesEmpleadores(): sector x nivel y tamaño x posibilidad futura.
    public List<FichaCruce> crucesEmpleadores() {
        List<Map<String, String>> mapas = analisis.mapasEmpleadores();
        List<FichaCruce> cruces = new ArrayList<>();
        cruces.add(armarCruce("Sector x nivel demandado",
                "sector_rubro", "nivel_formacion_demanda", mapas));
        cruces.add(armarCruce("Tamaño x posibilidad futura",
                "tamano_organizacion", "posibilidad_futura_contratacion", mapas));
        return cruces;
    }
}
