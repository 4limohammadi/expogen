package ir.byteplus.expogen;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * */

@SupportedAnnotationTypes("ir.byteplus.expogen.ExportToExcels")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ExcelProcessor extends AbstractProcessor {
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("ExcelProcessor is running!");

        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(ExportToExcels.class);
        for (Element element : annotatedElements) {
            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }
            TypeElement classElement = (TypeElement) element;
            ExportToExcels excels = classElement.getAnnotation(ExportToExcels.class);
            if (excels != null) {
                for (ExportToExcel excel : excels.value()) {
                    try {
                        generateExcelExporter(classElement, excel);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }

    private void generateExcelExporter(TypeElement classElement, ExportToExcel excel) throws IOException {
        String className = excel.className();
        String sheetName = excel.sheetName().isEmpty() ? classElement.getSimpleName().toString() : excel.sheetName();
        ColumnDefinition[] columns = excel.columns();
        String packageName = elementUtils.getPackageOf(classElement).getQualifiedName().toString();

        JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + className);
        try (PrintWriter out = new PrintWriter(file.openWriter())) {
            writePackageAndImports(out, packageName);
            writeClassDeclaration(out, className, classElement);
            writeExportMethod(out, classElement, sheetName, columns);
        }
    }

    private void writePackageAndImports(PrintWriter out, String packageName) {
        out.println("package " + packageName + ";");
        out.println();
        out.println("import ir.byteplus.expogen.ExportToExcelService;");
        out.println("import org.apache.poi.ss.usermodel.*;");
        out.println("import org.apache.poi.xssf.usermodel.XSSFWorkbook;");
        out.println("import org.apache.poi.xssf.usermodel.XSSFFont;");
        out.println("import org.apache.poi.xssf.usermodel.XSSFColor;");
        out.println("import java.util.List;");
        out.println();
    }

    private void writeClassDeclaration(PrintWriter out, String className, TypeElement classElement) {
        out.println("public class " + className + " implements ExportToExcelService<" + classElement.getSimpleName() + "> {");
    }

    private void writeExportMethod(PrintWriter out, TypeElement classElement, String sheetName, ColumnDefinition[] columns) {
        out.println("    @Override");
        out.println("    public Workbook export(List<" + classElement.getSimpleName() + "> data) {");
        out.println("        Workbook workbook = new XSSFWorkbook();");
        out.println("        Sheet sheet = workbook.createSheet(\"" + sheetName + "\");");
        out.println();

        // مرتب‌سازی ستون‌ها بر اساس order
        List<ColumnDefinition> sortedColumns = Arrays.stream(columns)
                .sorted(Comparator.comparingInt(ColumnDefinition::order))
                .collect(Collectors.toList());

        // تولید استایل‌ها
        writeStyles(out, sortedColumns);

        // تولید هدرها
        out.println("        Row headerRow = sheet.createRow(0);");
        for (int i = 0; i < sortedColumns.size(); i++) {
            ColumnDefinition col = sortedColumns.get(i);
            String colName = col.columnName().isEmpty() ? col.fieldName() : col.columnName();
            out.println("        Cell headerCell" + i + " = headerRow.createCell(" + i + ");");
            out.println("        headerCell" + i + ".setCellValue(\"" + colName + "\");");
            out.println("        headerCell" + i + ".setCellStyle(headerStyles[" + i + "]);");
        }

        // پر کردن داده‌ها
        out.println("        int rowNum = 1;");
        out.println("        int dataRowStart = 2;");
        out.println("        if (data != null && !data.isEmpty()) {");
        out.println("            for (" + classElement.getSimpleName() + " item : data) {");
        out.println("                Row row = sheet.createRow(rowNum++);");
        for (int i = 0; i < sortedColumns.size(); i++) {
            ColumnDefinition col = sortedColumns.get(i);
            String getterChain = buildGetterChain(col.fieldName());
            out.println("                Cell cell" + i + " = row.createCell(" + i + ");");
            out.println("                Object value" + i + " = " + getterChain + ";");
            out.println("                if (value" + i + " != null) {");
            out.println("                    if (value" + i + " instanceof Number) {");
            out.println("                        cell" + i + ".setCellValue(((Number) value" + i + ").doubleValue());");
            out.println("                    } else {");
            out.println("                        cell" + i + ".setCellValue(value" + i + ".toString());");
            out.println("                    }");
            out.println("                } else {");
            out.println("                    cell" + i + ".setCellValue(\"\");");
            out.println("                }");
            out.println("                cell" + i + ".setCellStyle(bodyStyles[" + i + "]);");
        }
        out.println("            }");
        out.println("        }");
        out.println("        int lastDataRow = data != null && !data.isEmpty() ? rowNum - 1 : 1;");

        // اعمال فرمول‌ها
        writeFormulas(out, sortedColumns);

        // تنظیم اندازه ستون‌ها
        for (int i = 0; i < sortedColumns.size(); i++) {
            out.println("        sheet.autoSizeColumn(" + i + ");");
        }

        out.println("        return workbook;");
        out.println("    }");

        // متدهای کمکی
        writeHelperMethods(out);
        out.println("}");
    }

    private String buildGetterChain(String fieldName) {
        String[] parts = fieldName.split("\\.");
        StringBuilder getterChain = new StringBuilder("item");
        for (String part : parts) {
            getterChain.append(".get").append(capitalize(part)).append("()");
        }
        // اضافه کردن چک null برای زنجیره
        StringBuilder safeGetter = new StringBuilder();
        safeGetter.append("(item == null ? null : ");
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            safeGetter.append("item.get").append(capitalize(part)).append("() == null ? null : ");
        }
        safeGetter.append(getterChain).append(")");
        return safeGetter.toString();
    }

    private void writeStyles(PrintWriter out, List<ColumnDefinition> columns) {
        out.println("        CellStyle[] headerStyles = new CellStyle[" + columns.size() + "];");
        out.println("        CellStyle[] bodyStyles = new CellStyle[" + columns.size() + "];");
        out.println("        String[] formulas = new String[" + columns.size() + "];");
        out.println("        String[] formulaApplyTo = new String[" + columns.size() + "];");
        out.println("        int[] formulaRowOffsets = new int[" + columns.size() + "];");

        for (int i = 0; i < columns.size(); i++) {
            ColumnDefinition col = columns.get(i);
            ExcelStyle headerStyle = col.headerStyle();
            ExcelStyle bodyStyle = col.bodyStyle();
            ExcelFormula formula = col.formula();

            // استایل هدر
            out.println("        headerStyles[" + i + "] = workbook.createCellStyle();");
            out.println("        XSSFFont headerFont" + i + " = (XSSFFont) workbook.createFont();");
            out.println("        headerFont" + i + ".setFontName(\"" + headerStyle.fontName() + "\");");
            out.println("        headerFont" + i + ".setFontHeightInPoints((short) " + headerStyle.fontSize() + ");");
            if (headerStyle.bold()) {
                out.println("        headerFont" + i + ".setBold(true);");
            }
            out.println("        headerStyles[" + i + "].setFont(headerFont" + i + ");");
            if (!headerStyle.backgroundColor().isEmpty()) {
                out.println("        headerStyles[" + i + "].setFillForegroundColor(new XSSFColor(new java.awt.Color(0x" + headerStyle.backgroundColor() + "), null));");
                out.println("        headerStyles[" + i + "].setFillPattern(FillPatternType.SOLID_FOREGROUND);");
            }
            out.println("        headerStyles[" + i + "].setAlignment(HorizontalAlignment." + headerStyle.alignment() + ");");

            // استایل بدنه
            out.println("        bodyStyles[" + i + "] = workbook.createCellStyle();");
            out.println("        XSSFFont bodyFont" + i + " = (XSSFFont) workbook.createFont();");
            out.println("        bodyFont" + i + ".setFontName(\"" + bodyStyle.fontName() + "\");");
            out.println("        bodyFont" + i + ".setFontHeightInPoints((short) " + bodyStyle.fontSize() + ");");
            if (bodyStyle.bold()) {
                out.println("        bodyFont" + i + ".setBold(true);");
            }
            out.println("        bodyStyles[" + i + "].setFont(bodyFont" + i + ");");
            if (!bodyStyle.backgroundColor().isEmpty()) {
                out.println("        bodyStyles[" + i + "].setFillForegroundColor(new XSSFColor(new java.awt.Color(0x" + bodyStyle.backgroundColor() + "), null));");
                out.println("        bodyStyles[" + i + "].setFillPattern(FillPatternType.SOLID_FOREGROUND);");
            }
            out.println("        bodyStyles[" + i + "].setAlignment(HorizontalAlignment." + bodyStyle.alignment() + ");");

            // فرمول‌ها
            if (!formula.formula().isEmpty()) {
                out.println("        formulas[" + i + "] = \"" + formula.formula() + "\";");
                out.println("        formulaApplyTo[" + i + "] = \"" + formula.applyTo() + "\";");
                out.println("        formulaRowOffsets[" + i + "] = " + formula.rowOffset() + ";");
            } else {
                out.println("        formulas[" + i + "] = \"\";");
                out.println("        formulaApplyTo[" + i + "] = \"\";");
                out.println("        formulaRowOffsets[" + i + "] = -1;");
            }
        }
    }

    private void writeFormulas(PrintWriter out, List<ColumnDefinition> columns) {
        out.println("        int formulaRowNum = lastDataRow + 1;");
        out.println("        if (data != null && !data.isEmpty()) {");
        for (int i = 0; i < columns.size(); i++) {
            out.println("            if (!formulas[" + i + "].isEmpty()) {");
            out.println("                Row formulaRow = sheet.getRow(formulaRowNum) != null ? sheet.getRow(formulaRowNum) : sheet.createRow(formulaRowNum);");
            out.println("                Cell formulaCell = formulaRow.createCell(" + i + ");");
            out.println("                String formula = \"\";");
            out.println("                if (formulaApplyTo[" + i + "].equals(\"COLUMN\")) {");
            out.println("                    String range = getColumnLetter(" + i + ") + \"2:\" + getColumnLetter(" + i + ") + lastDataRow;");
            out.println("                    formula = formulas[" + i + "] + \"(\" + range + \")\";");
            out.println("                    formulaCell.setCellFormula(formula);");
            out.println("                } else if (formulaApplyTo[" + i + "].equals(\"CELL\")) {");
            out.println("                    int targetRow = formulaRowOffsets[" + i + "] == -1 ? formulaRowNum : formulaRowOffsets[" + i + "];");
            out.println("                    Row targetFormulaRow = sheet.getRow(targetRow) != null ? sheet.getRow(targetRow) : sheet.createRow(targetRow);");
            out.println("                    formulaCell = targetFormulaRow.createCell(" + i + ");");
            out.println("                    formula = formulas[" + i + "];");
            out.println("                    formulaCell.setCellFormula(formula);");
            out.println("                }");
            out.println("                formulaCell.setCellStyle(bodyStyles[" + i + "]);");
            out.println("            }");
        }
        out.println("        }");
    }

    private void writeHelperMethods(PrintWriter out) {
        out.println("    private String getColumnLetter(int columnNumber) {");
        out.println("        int dividend = columnNumber + 1;");
        out.println("        StringBuilder columnLetter = new StringBuilder();");
        out.println("        while (dividend > 0) {");
        out.println("            int remainder = (dividend - 1) % 26;");
        out.println("            columnLetter.insert(0, (char) (65 + remainder));");
        out.println("            dividend = (dividend - remainder) / 26;");
        out.println("        }");
        out.println("        return columnLetter.toString();");
        out.println("    }");
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}