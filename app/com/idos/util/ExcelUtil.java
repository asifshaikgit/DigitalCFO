package com.idos.util;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.FillPatternType;

import java.awt.*;

/**
 * Created by admin on 15-04-2017.
 */
public class ExcelUtil {
    public static XSSFCellStyle getCellStyleHeader(XSSFWorkbook wb){
        XSSFCellStyle cellStyle = wb.createCellStyle();
        cellStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat(IdosConstants.DECIMAL_FORMAT_STR));
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
       // XSSFColor myColor = new XSSFColor(Color.LIGHT_GRAY);
        XSSFColor myColor = new XSSFColor(new byte[]{(byte) 192, (byte) 192, (byte) 192}, null);
        cellStyle.setFillForegroundColor(myColor);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = (XSSFFont) wb.createFont();
        font.setBold(true);
        cellStyle.setFont(font);
        return cellStyle;
    }

    public static XSSFCellStyle getCsBorderBoldFont(XSSFWorkbook wb){
        XSSFCellStyle cellStyle = wb.createCellStyle();
        cellStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat(IdosConstants.DECIMAL_FORMAT_STR));
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        XSSFFont font = (XSSFFont) wb.createFont();
        font.setBold(true);
        cellStyle.setFont(font);
        return cellStyle;
    }
}
