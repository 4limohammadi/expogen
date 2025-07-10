package ir.byteplus.expogen;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An annotation processor that generates Excel exporter classes based on {@link ExportToExcels}
 * annotations during compile-time. This processor creates implementations of
 * {@link ExportToExcelService} for each {@link ExportToExcel} configuration, producing
 * memory-efficient Excel files using Apache POI's {@code SXSSFWorkbook}.
 *
 * <p>Example usage:
 * <pre>{@code
 * &#064;ExportToExcels({
 *     &#064;ExportToExcel(
 *         className = "UserExporter",
 *         sheetName = "Users",
 *         autoSizeColumns = true,
 *         columns = {
 *             &#064;ColumnDefinition(
 *                 fieldName = "name",
 *                 columnName = "Full Name",
 *                 type = ColumnType.STRING,
 *                 order = 1,
 *                 headerStyle = &#064;ExcelStyle(fontName = "Arial", fontSize = 12, bold = true, backgroundColor = "CCCCCC"),
 *                 bodyStyle = &#064;ExcelStyle(fontName = "Arial", fontSize = 10)
 *             ),
 *             &#064;ColumnDefinition(fieldName = "age", type = ColumnType.NUMBER, order = 2)
 *         }
 *     )
 * })
 * public class User {
 *     private String name;
 *     private int age;
 *     public String getName() { return name; }
 *     public int getAge() { return age; }
 * }
 * }</pre>
 *
 * @since 0.0.0.2
 * @see ExportToExcels
 * @see ExportToExcel
 * @see ColumnDefinition
 * @see ExportToExcelService
 */
@SupportedAnnotationTypes("ir.byteplus.expogen.ExportToExcels")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ExcelProcessor extends AbstractProcessor {

    /**
     * Utility for accessing element-related information during annotation processing.
     */
    private Elements elementUtils;

    /**
     * Initializes the processor with the processing environment, setting up utilities
     * for element manipulation.
     *
     * @param processingEnv the environment for annotation processing
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementUtils = processingEnv.getElementUtils();
    }

    /**
     * Processes the {@link ExportToExcels} annotations found in the source code and generates
     * Excel exporter classes for each {@link ExportToExcel} configuration. This method validates
     * the annotated classes, ensures valid getter methods, and produces Java source files for
     * the exporter classes.
     *
     * <p>Debug information is printed to the console to aid in troubleshooting during development.
     * If an error occurs during code generation (e.g., I/O issues), an error message is reported
     * using the processing environment's messager.
     *
     * @param annotations the set of annotations to process
     * @param roundEnv    the environment for the current round of annotation processing
     * @return {@code true} to indicate that the annotations have been claimed and processed
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("ExcelProcessor is running!");
        System.out.println("Annotations: " + annotations);
        System.out.println("Elements with @ExportToExcels: " + roundEnv.getElementsAnnotatedWith(ExportToExcels.class));

        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(ExportToExcels.class);
        for (Element element : annotatedElements) {
            if (element.getKind() != ElementKind.CLASS) {
                System.out.println("Skipping non-class element: " + element);
                continue;
            }
            TypeElement classElement = (TypeElement) element;
            ExportToExcels excels = classElement.getAnnotation(ExportToExcels.class);
            if (excels != null) {
                System.out.println("Processing class: " + classElement.getSimpleName());
                for (ExportToExcel excel : excels.value()) {
                    try {
                        generateExcelExporter(classElement, excel);
                    } catch (IOException e) {
                        processingEnv.getMessager().printMessage(
                                Diagnostic.Kind.ERROR,
                                "Failed to generate Excel exporter: " + e.getMessage(),
                                classElement
                        );
                    }
                }
            }
        }
        return true;
    }

    /**
     * Generates an Excel exporter class based on the provided class element and
     * {@link ExportToExcel} configuration. The generated class implements
     * {@link ExportToExcelService} and includes methods to export data from a {@code List}
     * or {@code Stream}. The class is written to a new Java source file.
     *
     * <p>Validates the class name and getter methods for the specified fields. If validation
     * fails, an error is reported, and no code is generated.
     *
     * @param classElement the annotated class element
     * @param excel        the {@link ExportToExcel} annotation configuration
     * @throws IOException if an I/O error occurs during code generation
     */
    private void generateExcelExporter(TypeElement classElement, ExportToExcel excel) throws IOException {
        String className = excel.className();
        boolean isAutoSizeColumns = excel.autoSizeColumns();
        if (!isValidClass(classElement, className)) {
            return;
        }
        String sheetName = excel.sheetName().isEmpty() ? classElement.getSimpleName().toString() : excel.sheetName();
        ColumnDefinition[] columns = excel.columns();
        String packageName = elementUtils.getPackageOf(classElement).getQualifiedName().toString();

        JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + className);
        try (PrintWriter out = new PrintWriter(file.openWriter())) {
            writePackageAndImports(out, packageName);
            writeClassDeclaration(out, className, classElement);
            writeFields(out);
            writeExportMethod(out, classElement, sheetName, columns, isAutoSizeColumns);
            writeExportMethodFromStream(out, classElement, sheetName, columns, isAutoSizeColumns);
            writeHelperMethods(out);
            out.println("}");
        }
    }

    /**
     * Writes the package declaration and necessary import statements for the generated
     * exporter class. Includes imports for Apache POI classes, Java utilities, and the
     * {@link ExportToExcelService} interface.
     *
     * @param out         the writer for the generated source file
     * @param packageName the package name for the generated class
     */
    private void writePackageAndImports(PrintWriter out, String packageName) {
        out.println("package " + packageName + ";");
        out.println();
        out.println("import ir.byteplus.expogen.ExportToExcelService;");
        out.println("import org.apache.poi.ss.usermodel.*;");
        out.println("import org.apache.poi.xssf.streaming.SXSSFWorkbook;");
        out.println("import org.apache.poi.xssf.streaming.SXSSFSheet;");
        out.println("import org.apache.poi.xssf.usermodel.XSSFFont;");
        out.println("import org.apache.poi.xssf.usermodel.XSSFColor;");
        out.println("import java.util.List;");
        out.println("import java.util.Date;");
        out.println("import java.text.SimpleDateFormat;");
        out.println("import java.util.stream.Stream;");
        out.println("import java.util.concurrent.atomic.AtomicInteger;");
        out.println();
    }

    /**
     * Writes the class declaration for the generated exporter class, implementing
     * {@link ExportToExcelService} with the appropriate generic type.
     *
     * @param out          the writer for the generated source file
     * @param className    the name of the generated class
     * @param classElement the annotated class element
     */
    private void writeClassDeclaration(PrintWriter out, String className, TypeElement classElement) {
        out.println("public class " + className + " implements ExportToExcelService<" + classElement.getSimpleName() + "> {");
    }

    /**
     * Writes the instance fields for the generated exporter class, including the workbook,
     * sheet, date format, and style arrays for headers and body cells.
     *
     * @param out the writer for the generated source file
     */
    private void writeFields(PrintWriter out) {
        out.println("    private SXSSFWorkbook workbook;");
        out.println("    private SXSSFSheet sheet;");
        out.println("    private SimpleDateFormat dateFormat;");
        out.println("    private CellStyle[] headerStyles;");
        out.println("    private CellStyle[] bodyStyles;");
        out.println();
    }

    /**
     * Writes the {@code export} method that generates an Excel workbook from a list of data
     * objects. The method creates a sheet, applies header and body styles, formats data based
     * on {@link ColumnType}, and optionally auto-sizes columns if configured.
     *
     * <p>Validates getter methods for each column and reports errors if any are missing.
     * If the input list is {@code null} or empty, only the header row is generated.
     *
     * @param out            the writer for the generated source file
     * @param classElement   the annotated class element
     * @param sheetName      the name of the Excel sheet
     * @param columns        the column definitions for the Excel sheet
     * @param isAutoSizeColumns whether to auto-size columns
     */
    private void writeExportMethod(PrintWriter out, TypeElement classElement, String sheetName, ColumnDefinition[] columns,
                                   boolean isAutoSizeColumns) {
        out.println("    @Override");
        out.println("    public SXSSFWorkbook export(List<" + classElement.getSimpleName() + "> data) {");
        out.println("        workbook = new SXSSFWorkbook(100);");
        out.println("        sheet = workbook.createSheet(\"" + sheetName + "\");");
        out.println("        dateFormat = new SimpleDateFormat(\"dd/MM/yyyy\");");
        out.println();

        List<ColumnDefinition> sortedColumns = Arrays.stream(columns)
                .sorted(Comparator.comparingInt(ColumnDefinition::order))
                .collect(Collectors.toList());

        for (ColumnDefinition column : sortedColumns) {
            if (!hasGetter(classElement, column.fieldName())) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "No valid getter found for field: " + column.fieldName(),
                        classElement
                );
                return;
            }
        }

        writeStyles(out, sortedColumns);

        out.println("        Row headerRow = sheet.createRow(0);");
        for (int i = 0; i < sortedColumns.size(); i++) {
            ColumnDefinition col = sortedColumns.get(i);
            String colName = col.columnName().isEmpty() ? col.fieldName() : col.columnName();
            out.println("        Cell headerCell" + i + " = headerRow.createCell(" + i + ");");
            out.println("        headerCell" + i + ".setCellValue(\"" + colName + "\");");
            out.println("        headerCell" + i + ".setCellStyle(headerStyles[" + i + "]);");
        }

        out.println("        int rowNum = 1;");
        out.println("        int dataRowStart = 2;");
        out.println("        if (data != null && !data.isEmpty()) {");
        out.println("            for (" + classElement.getSimpleName() + " item : data) {");
        out.println("                Row row = sheet.createRow(rowNum++);");
        for (int i = 0; i < sortedColumns.size(); i++) {
            ColumnDefinition col = sortedColumns.get(i);
            String getterChain = buildGetterChain(col.fieldName());
            String type = col.type().name();
            out.println("                Cell cell" + i + " = row.createCell(" + i + ");");
            out.println("                Object value" + i + " = " + getterChain + ";");
            out.println("                if (value" + i + " != null) {");
            out.println("                    switch (\"" + type + "\") {");
            out.println("                        case \"STRING\":");
            out.println("                            cell" + i + ".setCellValue(value" + i + ".toString());");
            out.println("                            break;");
            out.println("                        case \"NUMBER\":");
            out.println("                            cell" + i + ".setCellValue(((Number) value" + i + ").doubleValue());");
            out.println("                            bodyStyles[" + i + "].setDataFormat(workbook.createDataFormat().getFormat(\"0.00\"));");
            out.println("                            break;");
            out.println("                        case \"DATE\":");
            out.println("                            cell" + i + ".setCellValue(dateFormat.format((Date) value" + i + "));");
            out.println("                            bodyStyles[" + i + "].setDataFormat(workbook.createDataFormat().getFormat(\"dd/mm/yyyy\"));");
            out.println("                            break;");
            out.println("                        case \"BOOLEAN\":");
            out.println("                            cell" + i + ".setCellValue((Boolean) value" + i + ");");
            out.println("                            break;");
            out.println("                        default:");
            out.println("                            cell" + i + ".setCellValue(value" + i + ".toString());");
            out.println("                    }");
            out.println("                } else {");
            out.println("                    cell" + i + ".setCellValue(\"\");");
            out.println("                }");
            out.println("                cell" + i + ".setCellStyle(bodyStyles[" + i + "]);");
        }
        out.println("            }");
        out.println("        }");
        out.println("        int lastDataRow = data != null && !data.isEmpty() ? rowNum - 1 : 1;");

        if (isAutoSizeColumns) {
            for (int i = 0; i < sortedColumns.size(); i++) {
                out.println("        sheet.trackColumnForAutoSizing(" + i + ");");
                out.println("        sheet.autoSizeColumn(" + i + ");");
            }
        }

        out.println("        return workbook;");
        out.println("    }");
    }

    /**
     * Writes the {@code export} method that generates an Excel workbook from a stream of data
     * objects. The method creates a sheet, applies header and body styles, formats data based
     * on {@link ColumnType}, and optionally auto-sizes columns if configured. This method is
     * optimized for large datasets by processing data incrementally.
     *
     * <p>Validates getter methods for each column and reports errors if any are missing.
     * If the input stream is {@code null} or empty, only the header row is generated.
     *
     * @param out            the writer for the generated source file
     * @param classElement   the annotated class element
     * @param sheetName      the name of the Excel sheet
     * @param columns        the column definitions for the Excel sheet
     * @param isAutoSizeColumns whether to auto-size columns
     */
    private void writeExportMethodFromStream(PrintWriter out, TypeElement classElement, String sheetName, ColumnDefinition[] columns, boolean isAutoSizeColumns) {
        out.println("    @Override");
        out.println("    public SXSSFWorkbook export(Stream<" + classElement.getSimpleName() + "> dataStream) {");
        out.println("        workbook = new SXSSFWorkbook(100);");
        out.println("        sheet = workbook.createSheet(\"" + sheetName + "\");");
        out.println("        dateFormat = new SimpleDateFormat(\"dd/MM/yyyy\");");
        out.println();

        List<ColumnDefinition> sortedColumns = Arrays.stream(columns)
                .sorted(Comparator.comparingInt(ColumnDefinition::order))
                .collect(Collectors.toList());

        for (ColumnDefinition column : sortedColumns) {
            if (!hasGetter(classElement, column.fieldName())) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "No valid getter found for field: " + column.fieldName(),
                        classElement
                );
                return;
            }
        }

        writeStyles(out, sortedColumns);

        out.println("        Row headerRow = sheet.createRow(0);");
        for (int i = 0; i < sortedColumns.size(); i++) {
            ColumnDefinition col = sortedColumns.get(i);
            String colName = col.columnName().isEmpty() ? col.fieldName() : col.columnName();
            out.println("        Cell headerCell" + i + " = headerRow.createCell(" + i + ");");
            out.println("        headerCell" + i + ".setCellValue(\"" + colName + "\");");
            out.println("        headerCell" + i + ".setCellStyle(headerStyles[" + i + "]);");
        }

        out.println("        AtomicInteger rowNum = new AtomicInteger(1);");
        out.println("        int dataRowStart = 2;");
        out.println("        long[] rowCount = new long[1];"); // Track rows processed
        out.println("        if (dataStream != null) {");
        out.println("            dataStream.forEach(item -> {");
        out.println("                rowCount[0]++;");
        out.println("                Row row = sheet.createRow(rowNum.getAndIncrement());");
        for (int i = 0; i < sortedColumns.size(); i++) {
            ColumnDefinition col = sortedColumns.get(i);
            String getterChain = buildGetterChain(col.fieldName());
            String type = col.type().name();
            out.println("                Cell cell" + i + " = row.createCell(" + i + ");");
            out.println("                Object value" + i + " = " + getterChain + ";");
            out.println("                if (value" + i + " != null) {");
            out.println("                    switch (\"" + type + "\") {");
            out.println("                        case \"STRING\":");
            out.println("                            cell" + i + ".setCellValue(value" + i + ".toString());");
            out.println("                            break;");
            out.println("                        case \"NUMBER\":");
            out.println("                            cell" + i + ".setCellValue(((Number) value" + i + ").doubleValue());");
            out.println("                            bodyStyles[" + i + "].setDataFormat(workbook.createDataFormat().getFormat(\"0.00\"));");
            out.println("                            break;");
            out.println("                        case \"DATE\":");
            out.println("                            cell" + i + ".setCellValue(dateFormat.format((Date) value" + i + "));");
            out.println("                            bodyStyles[" + i + "].setDataFormat(workbook.createDataFormat().getFormat(\"dd/mm/yyyy\"));");
            out.println("                            break;");
            out.println("                        case \"BOOLEAN\":");
            out.println("                            cell" + i + ".setCellValue((Boolean) value" + i + ");");
            out.println("                            break;");
            out.println("                        default:");
            out.println("                            cell" + i + ".setCellValue(value" + i + ".toString());");
            out.println("                    }");
            out.println("                } else {");
            out.println("                    cell" + i + ".setCellValue(\"\");");
            out.println("                }");
            out.println("                cell" + i + ".setCellStyle(bodyStyles[" + i + "]);");
        }
        out.println("            });");
        out.println("        }");
        out.println("        int lastDataRow = rowCount[0] > 0 ? (int) rowCount[0] + 1 : 1;");

        if (isAutoSizeColumns) {
            for (int i = 0; i < sortedColumns.size(); i++) {
                out.println("        sheet.trackColumnForAutoSizing(" + i + ");");
                out.println("        sheet.autoSizeColumn(" + i + ");");
            }
        }
        out.println("        return workbook;");
        out.println("    }");
    }

    /**
     * Builds a null-safe getter chain for accessing nested fields in the data object.
     * Supports nested fields using dot notation (e.g., "user.address.city").
     *
     * @param fieldName the field name, potentially containing nested fields separated by dots
     * @return a string representing the null-safe getter chain
     */
    private String buildGetterChain(String fieldName) {
        String[] parts = fieldName.split("\\.");
        StringBuilder getterChain = new StringBuilder("item");
        for (String part : parts) {
            getterChain.append(".get").append(capitalize(part)).append("()");
        }

        StringBuilder safeGetter = new StringBuilder();
        safeGetter.append("(item == null ? null : ");
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            safeGetter.append("item.get").append(capitalize(part)).append("() == null ? null : ");
        }
        safeGetter.append(getterChain).append(")");
        return safeGetter.toString();
    }

    /**
     * Writes style definitions for header and body cells based on {@link ExcelStyle}
     * configurations in the column definitions. Applies font, size, boldness, background
     * color, and alignment settings.
     *
     * @param out     the writer for the generated source file
     * @param columns the list of column definitions
     */
    private void writeStyles(PrintWriter out, List<ColumnDefinition> columns) {
        out.println("        headerStyles = new CellStyle[" + columns.size() + "];");
        out.println("        bodyStyles = new CellStyle[" + columns.size() + "];");

        for (int i = 0; i < columns.size(); i++) {
            ColumnDefinition col = columns.get(i);
            ExcelStyle headerStyle = col.headerStyle();
            ExcelStyle bodyStyle = col.bodyStyle();

            out.println("        headerStyles[" + i + "] = workbook.createCellStyle();");
            out.println("        XSSFFont headerFont" + i + " = (XSSFFont) workbook.createFont();");
            out.println("        headerFont" + i + ".setFontName(\"" + headerStyle.fontName() + "\");");
            if (isValidFontSize(headerStyle.fontSize())) {
                out.println("        headerFont" + i + ".setFontHeightInPoints((short) " + headerStyle.fontSize() + ");");
            }
            if (headerStyle.bold()) {
                out.println("        headerFont" + i + ".setBold(true);");
            }
            out.println("        headerStyles[" + i + "].setFont(headerFont" + i + ");");
            if (!headerStyle.backgroundColor().isEmpty() && isValidBackgroundColor(headerStyle.backgroundColor())) {
                out.println("        headerStyles[" + i + "].setFillForegroundColor(new XSSFColor(new java.awt.Color(0x" + headerStyle.backgroundColor() + "), null));");
                out.println("        headerStyles[" + i + "].setFillPattern(FillPatternType.SOLID_FOREGROUND);");
            }
            out.println("        headerStyles[" + i + "].setAlignment(HorizontalAlignment." + headerStyle.alignment() + ");");

            out.println("        bodyStyles[" + i + "] = workbook.createCellStyle();");
            out.println("        XSSFFont bodyFont" + i + " = (XSSFFont) workbook.createFont();");
            out.println("        bodyFont" + i + ".setFontName(\"" + bodyStyle.fontName() + "\");");
            if (isValidFontSize(bodyStyle.fontSize())) {
                out.println("        bodyFont" + i + ".setFontHeightInPoints((short) " + bodyStyle.fontSize() + ");");
            }
            if (bodyStyle.bold()) {
                out.println("        bodyFont" + i + ".setBold(true);");
            }
            out.println("        bodyStyles[" + i + "].setFont(bodyFont" + i + ");");
            if (!bodyStyle.backgroundColor().isEmpty() && isValidBackgroundColor(bodyStyle.backgroundColor())) {
                out.println("        bodyStyles[" + i + "].setFillForegroundColor(new XSSFColor(new java.awt.Color(0x" + bodyStyle.backgroundColor() + "), null));");
                out.println("        bodyStyles[" + i + "].setFillPattern(FillPatternType.SOLID_FOREGROUND);");
            }
            out.println("        bodyStyles[" + i + "].setAlignment(HorizontalAlignment." + bodyStyle.alignment() + ");");
        }
    }

    /**
     * Validates that the provided background color is a valid 6-digit hexadecimal value.
     * Reports an error via the messager if the color is invalid.
     *
     * @param color the hexadecimal color value to validate
     * @return {@code true} if the color is valid, {@code false} otherwise
     */
    private boolean isValidBackgroundColor(String color) {
        if (!color.matches("[0-9A-Fa-f]{6}")) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Invalid hex color: " + color);
            return false;
        }
        return true;
    }

    /**
     * Writes helper methods for the generated class, including a method to convert
     * column indices to Excel column letters (e.g., 0 to "A", 1 to "B").
     *
     * @param out the writer for the generated source file
     */
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

    /**
     * Capitalizes the first letter of a string, used for generating getter method names.
     *
     * @param str the input string
     * @return the string with the first letter capitalized
     */
    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Checks if the specified field has valid getter methods, including for nested fields
     * (e.g., "user.address.city"). Reports an error if any getter is missing or invalid.
     *
     * @param classElement the class element to check
     * @param fieldName   the field name, potentially containing nested fields separated by dots
     * @return {@code true} if all getters exist and are valid, {@code false} otherwise
     */
    private boolean hasGetter(TypeElement classElement, String fieldName) {
        String[] fieldParts = fieldName.split("\\.");
        TypeElement currentClass = classElement;

        for (String part : fieldParts) {
            String getterName = "get" + capitalize(part);
            boolean getterFound = false;

            // Check methods in the class
            for (Element enclosed : elementUtils.getAllMembers(currentClass)) {
                if (enclosed.getKind() == ElementKind.METHOD && enclosed.getSimpleName().toString().equals(getterName)) {
                    ExecutableElement method = (ExecutableElement) enclosed;
                    if (method.getParameters().isEmpty() && method.getReturnType() != null) {
                        getterFound = true;
                        if (!part.equals(fieldParts[fieldParts.length - 1])) {
                            TypeMirror returnType = method.getReturnType();
                            if (returnType.getKind() == TypeKind.DECLARED) {
                                currentClass = (TypeElement) ((DeclaredType) returnType).asElement();
                            } else {
                                return false;
                            }
                        }
                        break;
                    }
                }
            }

            if (!getterFound) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validates that the class name is a valid Java identifier. Reports an error via the
     * messager if the class name is empty or contains invalid characters.
     *
     * @param classElement the class element being processed
     * @param className    the name of the generated class
     * @return {@code true} if the class name is valid, {@code false} otherwise
     */
    private boolean isValidClass(TypeElement classElement, String className) {
        if (className.isEmpty() || !className.matches("[A-Za-z][A-Za-z0-9]*")) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "Invalid className: " + className,
                    classElement
            );
            return false;
        }
        return true;
    }

    /**
     * Validates that the font size is positive. Reports an error via the messager if the
     * font size is invalid (e.g., zero or negative).
     *
     * @param fontSize the font size to validate
     * @return {@code true} if the font size is valid, {@code false} otherwise
     */
    private boolean isValidFontSize(int fontSize) {
        if (fontSize <= 0) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "Invalid fontSize: " + fontSize
            );
            return false;
        }
        return true;
    }
}