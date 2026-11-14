package bo.umss.mercado.servidor.servicio.exportacion;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

public final class ConversorSvg {

    private ConversorSvg() {
    }

    public static byte[] aPng(String svg, int ancho, int alto) {
        if (svg == null || svg.isEmpty()) {
            return null;
        }
        try {
            String svgAjustado = svg;

            if (!svgAjustado.contains("xmlns=")) {
                svgAjustado = svgAjustado.replaceFirst("<svg ",
                        "<svg xmlns='http://www.w3.org/2000/svg' ");
            }

            svgAjustado = svgAjustado.replace(
                    "style='width:100%;height:auto;background:#ffffff'",
                    "width='" + ancho + "' height='" + alto
                            + "' style='background:#ffffff'");

            PNGTranscoder transcoder = new PNGTranscoder();
            transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, (float) ancho);
            transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, (float) alto);
            transcoder.addTranscodingHint(PNGTranscoder.KEY_BACKGROUND_COLOR, Color.WHITE);

            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            TranscoderInput input = new TranscoderInput(new StringReader(svgAjustado));
            TranscoderOutput output = new TranscoderOutput(salida);
            transcoder.transcode(input, output);
            return salida.toByteArray();
        } catch (Exception error) {
            System.err.println("[ConversorSvg] Fallo al convertir SVG: "
                    + error.getClass().getSimpleName() + ": " + error.getMessage());
            error.printStackTrace();
            throw new IllegalStateException(
                    "No se pudo convertir SVG a PNG: " + error.getMessage(), error);
        }
    }
}