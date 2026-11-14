package bo.umss.mercado.servidor.servicio.exportacion;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import java.awt.Color;

public final class EstilosExcel {

    private EstilosExcel() {
    }

    public static final String AZUL_PRINCIPAL_HEX = "0B3D62";
    public static final String AZUL_SECUNDARIO_HEX = "0984E3";
    public static final String GRIS_FONDO_HEX = "F5F7FA";
    public static final String GRIS_BORDE_HEX = "D0D7DE";

    public static CellStyle titulo(Workbook wb) {
        XSSFCellStyle s = (XSSFCellStyle) wb.createCellStyle();
        XSSFFont f = (XSSFFont) wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 14);
        f.setColor(new XSSFColor(new Color(255, 255, 255), null));
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(colorDe(AZUL_PRINCIPAL_HEX), null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.LEFT);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.THIN);
        return s;
    }

    public static CellStyle headerPrincipal(Workbook wb) {
        XSSFCellStyle s = (XSSFCellStyle) wb.createCellStyle();
        XSSFFont f = (XSSFFont) wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 11);
        f.setColor(new XSSFColor(new Color(255, 255, 255), null));
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(colorDe(AZUL_PRINCIPAL_HEX), null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(true);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBottomBorderColor(new XSSFColor(colorDe(GRIS_BORDE_HEX), null));
        return s;
    }

    public static CellStyle headerSecundario(Workbook wb) {
        XSSFCellStyle s = (XSSFCellStyle) wb.createCellStyle();
        XSSFFont f = (XSSFFont) wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 11);
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(colorDe(GRIS_FONDO_HEX), null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.LEFT);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBottomBorderColor(new XSSFColor(colorDe(GRIS_BORDE_HEX), null));
        return s;
    }

    public static CellStyle celdaTexto(Workbook wb) {
        XSSFCellStyle s = (XSSFCellStyle) wb.createCellStyle();
        s.setBorderBottom(BorderStyle.HAIR);
        s.setBottomBorderColor(new XSSFColor(colorDe(GRIS_BORDE_HEX), null));
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    public static CellStyle celdaWrap(Workbook wb) {
        CellStyle s = celdaTexto(wb);
        s.setWrapText(true);
        return s;
    }

    public static CellStyle celdaNumero(Workbook wb, String formato) {
        CellStyle s = celdaTexto(wb);
        s.setDataFormat(wb.createDataFormat().getFormat(formato));
        s.setAlignment(HorizontalAlignment.RIGHT);
        return s;
    }

    public static CellStyle celdaEntero(Workbook wb) {
        CellStyle s = celdaTexto(wb);
        s.setDataFormat(wb.createDataFormat().getFormat("#,##0"));
        s.setAlignment(HorizontalAlignment.RIGHT);
        return s;
    }

    public static int escribirTitulo(Workbook wb, Sheet hoja, int rowIdx, String texto) {
        Cell c = fila(hoja, rowIdx).createCell(0);
        c.setCellValue(texto);
        c.setCellStyle(titulo(wb));
        hoja.setColumnWidth(0, Math.min(60 * 256,
                Math.max(hoja.getColumnWidth(0), (texto.length() + 4) * 256)));
        return rowIdx + 2;
    }

    public static int escribirEncabezados(Workbook wb, Sheet hoja, int rowIdx, String... valores) {
        for (int i = 0; i < valores.length; i++) {
            Cell c = fila(hoja, rowIdx).createCell(i);
            c.setCellValue(valores[i]);
            c.setCellStyle(headerPrincipal(wb));
        }
        return rowIdx + 1;
    }

    public static int escribirFila(Workbook wb, Sheet hoja, int rowIdx, String... valores) {
        CellStyle estilo = celdaTexto(wb);
        for (int i = 0; i < valores.length; i++) {
            Cell c = fila(hoja, rowIdx).createCell(i);
            c.setCellValue(valores[i] == null ? "" : valores[i]);
            c.setCellStyle(estilo);
        }
        return rowIdx + 1;
    }

    public static int escribirFilaNumerica(Workbook wb, Sheet hoja, int rowIdx,
            Object... valores) {
        CellStyle txt = celdaTexto(wb);
        CellStyle ent = celdaEntero(wb);
        CellStyle dec = celdaNumero(wb, "0.0000");
        for (int i = 0; i < valores.length; i++) {
            Cell c = fila(hoja, rowIdx).createCell(i);
            Object v = valores[i];
            if (v == null) {
                c.setCellValue("");
                c.setCellStyle(txt);
            } else if (v instanceof Integer || v instanceof Long) {
                c.setCellValue(((Number) v).doubleValue());
                c.setCellStyle(ent);
            } else if (v instanceof Double || v instanceof Float) {
                double d = ((Number) v).doubleValue();
                if (Double.isNaN(d)) {
                    c.setCellValue("");
                    c.setCellStyle(txt);
                } else {
                    c.setCellValue(d);
                    c.setCellStyle(dec);
                }
            } else {
                c.setCellValue(v.toString());
                c.setCellStyle(txt);
            }
        }
        return rowIdx + 1;
    }

    public static void autoSize(Sheet hoja) {
        int maxCol = 0;
        for (int r = hoja.getFirstRowNum(); r <= hoja.getLastRowNum(); r++) {
            Row row = hoja.getRow(r);
            if (row == null)
                continue;
            int last = row.getLastCellNum();
            if (last > maxCol)
                maxCol = last;
        }
        for (int c = 0; c < maxCol; c++) {
            hoja.autoSizeColumn(c);
            int ancho = hoja.getColumnWidth(c);
            hoja.setColumnWidth(c, Math.min(ancho + 512, 60 * 256));
        }
    }

    public static int filaVacia(int rowIdx) {
        return rowIdx + 1;
    }

    private static Row fila(Sheet hoja, int rowIdx) {
        Row row = hoja.getRow(rowIdx);
        if (row == null) {
            row = hoja.createRow(rowIdx);
        }
        return row;
    }

    private static Color colorDe(String hex) {
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new Color(r, g, b);
    }
}