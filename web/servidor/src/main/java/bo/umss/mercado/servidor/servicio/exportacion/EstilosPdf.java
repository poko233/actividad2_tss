package bo.umss.mercado.servidor.servicio.exportacion;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import java.awt.Color;

/**
 * EstilosPdf: helpers para el PDF ejecutivo (OpenPDF, namespace com.lowagie.*).
 *
 * Paleta alineada con la UI:
 * AZUL = #0B3D62, AZUL2 = #0984E3, VERDE = #27AE60,
 * NARANJA = #E17055, MORADO = #6C5CE7, GRIS = #7F8C8D,
 * TEXTO = #2C3E50, BG_AZUL = #E8F2FC, BG_GRIS = #F5F7FA
 */
public final class EstilosPdf {

    private EstilosPdf() {
    }

    public static final Color AZUL = new Color(0x0B, 0x3D, 0x62);
    public static final Color AZUL2 = new Color(0x09, 0x84, 0xE3);
    public static final Color VERDE = new Color(0x27, 0xAE, 0x60);
    public static final Color NARANJA = new Color(0xE1, 0x70, 0x55);
    public static final Color MORADO = new Color(0x6C, 0x5C, 0xE7);
    public static final Color GRIS = new Color(0x7F, 0x8C, 0x8D);
    public static final Color TEXTO = new Color(0x2C, 0x3E, 0x50);
    public static final Color BG_AZUL = new Color(0xE8, 0xF2, 0xFC);
    public static final Color BG_GRIS = new Color(0xF5, 0xF7, 0xFA);
    public static final Color BORDE = new Color(0xD0, 0xD7, 0xDE);
    public static final Color BLANCO = Color.WHITE;

    public static Font fuentePortada() {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, AZUL);
    }

    public static Font fuenteSubtituloPortada() {
        return FontFactory.getFont(FontFactory.HELVETICA, 13, GRIS);
    }

    public static Font fuenteH1() {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, AZUL);
    }

    public static Font fuenteH2() {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, AZUL);
    }

    public static Font fuenteCuerpo() {
        return FontFactory.getFont(FontFactory.HELVETICA, 10, TEXTO);
    }

    public static Font fuenteItalica() {
        return FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9,
                new Color(0x55, 0x55, 0x55));
    }

    public static Font fuenteHeaderBlanco() {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BLANCO);
    }

    public static Font fuenteBannerTitulo() {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BLANCO);
    }

    public static Font fuenteBannerSub() {
        return FontFactory.getFont(FontFactory.HELVETICA, 11, BLANCO);
    }

    public static Paragraph tituloSeccion(String texto) {
        Paragraph p = new Paragraph(texto, fuenteH1());
        p.setSpacingBefore(14);
        p.setSpacingAfter(8);
        return p;
    }

    public static Paragraph subtitulo(String texto) {
        Paragraph p = new Paragraph(texto, fuenteH2());
        p.setSpacingBefore(10);
        p.setSpacingAfter(6);
        return p;
    }

    public static Paragraph parrafo(String texto) {
        Paragraph p = new Paragraph(texto, fuenteCuerpo());
        p.setAlignment(Element.ALIGN_JUSTIFIED);
        p.setSpacingAfter(6);
        return p;
    }

    public static Paragraph nota(String texto) {
        Paragraph p = new Paragraph(texto, fuenteItalica());
        p.setAlignment(Element.ALIGN_JUSTIFIED);
        p.setSpacingAfter(6);
        return p;
    }

    public static Paragraph centrado(String texto, Font fuente) {
        Paragraph p = new Paragraph(texto, fuente);
        p.setAlignment(Element.ALIGN_CENTER);
        return p;
    }

    /** Banner coloreado (usado en portada y al inicio de secciones grandes). */
    public static void banner(Document doc, String titulo, String subtitulo) throws Exception {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);

        PdfPCell c1 = new PdfPCell(new Phrase(titulo, fuenteBannerTitulo()));
        c1.setBackgroundColor(AZUL);
        c1.setPadding(18);
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        t.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(subtitulo, fuenteBannerSub()));
        c2.setBackgroundColor(AZUL);
        c2.setPadding(10);
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setHorizontalAlignment(Element.ALIGN_LEFT);
        t.addCell(c2);

        t.setSpacingAfter(18);
        doc.add(t);
    }

    /** Tabla base con 100% de ancho y separación estándar. */
    public static PdfPTable tabla(int columnas) {
        PdfPTable t = new PdfPTable(columnas);
        t.setWidthPercentage(100);
        t.setSpacingBefore(6);
        t.setSpacingAfter(10);
        return t;
    }

    /** Celda de encabezado (fondo azul, texto blanco). */
    public static PdfPCell celdaHeader(String texto) {
        PdfPCell c = new PdfPCell(new Phrase(texto == null ? "" : texto, fuenteHeaderBlanco()));
        c.setBackgroundColor(AZUL);
        c.setPadding(6);
        c.setBorderColor(AZUL2);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return c;
    }

    /** Celda de texto normal. */
    public static PdfPCell celdaTexto(String texto) {
        PdfPCell c = new PdfPCell(new Phrase(texto == null ? "" : texto, fuenteCuerpo()));
        c.setPadding(5);
        c.setBorderColor(BORDE);
        return c;
    }

    /** Celda numérica (alineada a la derecha). */
    public static PdfPCell celdaNumero(String texto) {
        PdfPCell c = new PdfPCell(new Phrase(texto == null ? "" : texto, fuenteCuerpo()));
        c.setPadding(5);
        c.setBorderColor(BORDE);
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return c;
    }

    /** Celda con fondo suave y texto centrado. */
    public static PdfPCell celdaFondo(Color fondo, String texto, Font fuente) {
        PdfPCell c = new PdfPCell(new Phrase(texto == null ? "" : texto, fuente));
        c.setBackgroundColor(fondo);
        c.setPadding(5);
        c.setBorderColor(fondo);
        return c;
    }

    /** "n/d" si el double es NaN, o el valor redondeado. */
    public static String fmt(double valor, int decimales) {
        if (Double.isNaN(valor)) {
            return "n/d";
        }
        double factor = Math.pow(10, decimales);
        return String.valueOf(Math.round(valor * factor) / factor);
    }
}