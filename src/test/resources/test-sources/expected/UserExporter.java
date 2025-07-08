package ir.byteplus.expogen.test;

import ir.byteplus.expogen.ExportToExcelService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFColor;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.stream.Stream;
import java.util.concurrent.atomic.AtomicInteger;

public class UserExporter implements ExportToExcelService<UserDto> {
    private SXSSFWorkbook workbook;
    private SXSSFSheet sheet;
    private SimpleDateFormat dateFormat;
    private CellStyle[] headerStyles;
    private CellStyle[] bodyStyles;
    private String[] formulas;
    private String[] formulaApplyTo;
    private int[] formulaRowOffsets;

    @Override
    public SXSSFWorkbook export(List<UserDto> data) {
        workbook = new SXSSFWorkbook(100);
        sheet = workbook.createSheet("Users");
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        headerStyles = new CellStyle[3];
        bodyStyles = new CellStyle[3];
        formulas = new String[3];
        formulaApplyTo = new String[3];
        formulaRowOffsets = new int[3];

        // Styles for name
        headerStyles[0] = workbook.createCellStyle();
        XSSFFont headerFont0 = (XSSFFont) workbook.createFont();
        headerFont0.setFontName("Arial");
        headerFont0.setFontHeightInPoints((short) 12);
        headerFont0.setBold(true);
        headerStyles[0].setFont(headerFont0);
        headerStyles[0].setFillForegroundColor(new XSSFColor(new java.awt.Color(0xFF00FF), null));
        headerStyles[0].setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyles[0].setAlignment(HorizontalAlignment.CENTER);
        bodyStyles[0] = workbook.createCellStyle();
        XSSFFont bodyFont0 = (XSSFFont) workbook.createFont();
        bodyFont0.setFontName("Arial");
        bodyFont0.setFontHeightInPoints((short) 12);
        bodyStyles[0].setFont(bodyFont0);
        bodyStyles[0].setAlignment(HorizontalAlignment.CENTER);
        formulas[0] = "";
        formulaApplyTo[0] = "";
        formulaRowOffsets[0] = -1;

        // Styles for age (simplified)
        headerStyles[1] = workbook.createCellStyle();
        // ... (similar style setup)
        formulas[1] = "SUM";
        formulaApplyTo[1] = "COLUMN";
        formulaRowOffsets[1] = -1;

        // Styles for birthDate (simplified)
        headerStyles[2] = workbook.createCellStyle();
        // ... (similar style setup)
        formulas[2] = "";
        formulaApplyTo[2] = "";
        formulaRowOffsets[2] = -1;

        Row headerRow = sheet.createRow(0);
        Cell headerCell0 = headerRow.createCell(0);
        headerCell0.setCellValue("نام کاربر");
        headerCell0.setCellStyle(headerStyles[0]);
        Cell headerCell1 = headerRow.createCell(1);
        headerCell1.setCellValue("سن");
        headerCell1.setCellStyle(headerStyles[1]);
        Cell headerCell2 = headerRow.createCell(2);
        headerCell2.setCellValue("تاریخ تولد");
        headerCell2.setCellStyle(headerStyles[2]);

        int rowNum = 1;
        int dataRowStart = 2;
        if (data != null && !data.isEmpty()) {
            for (UserDto item : data) {
                Row row = sheet.createRow(rowNum++);
                Cell cell0 = row.createCell(0);
                Object value0 = (item == null ? null : item.getName());
                if (value0 != null) {
                    switch ("STRING") {
                        case "STRING":
                            cell0.setCellValue(value0.toString());
                            break;
                        // ... other cases
                    }
                } else {
                    cell0.setCellValue("");
                }
                cell0.setCellStyle(bodyStyles[0]);
                // ... similar for age and birthDate
            }
        }
        int lastDataRow = data != null && !data.isEmpty() ? rowNum - 1 : 1;

        int formulaRowNum = lastDataRow + 1;
        if (lastDataRow > 1) {
            if (!formulas[1].isEmpty()) {
                Row formulaRow = sheet.createRow(formulaRowNum);
                Cell formulaCell = formulaRow.createCell(1);
                String formula = "";
                if (formulaApplyTo[1].equals("COLUMN")) {
                    String range = getColumnLetter(1) + "2:" + getColumnLetter(1) + lastDataRow;
                    formula = formulas[1] + "(" + range + ")";
                    formulaCell.setCellFormula(formula);
                }
                formulaCell.setCellStyle(bodyStyles[1]);
            }
        }

        sheet.trackColumnForAutoSizing(0);
        sheet.autoSizeColumn(0);
        sheet.trackColumnForAutoSizing(1);
        sheet.autoSizeColumn(1);
        sheet.trackColumnForAutoSizing(2);
        sheet.autoSizeColumn(2);

        return workbook;
    }

    // ... (export(Stream<UserDto> dataStream) and helper methods)

    private String getColumnLetter(int columnNumber) {
        int dividend = columnNumber + 1;
        StringBuilder columnLetter = new StringBuilder();
        while (dividend > 0) {
            int remainder = (dividend - 1) % 26;
            columnLetter.insert(0, (char) (65 + remainder));
            dividend = (dividend - remainder) / 26;
        }
        return columnLetter.toString();
    }
}