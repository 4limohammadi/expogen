package ir.byteplus.expogen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class to generate one or more Excel exporter classes during annotation processing.
 * This annotation serves as a container for one or more {@link ExportToExcel} annotations,
 * each defining a separate Excel exporter class that implements {@link ExportToExcelService}.
 * The generated classes export data from the annotated class to Excel files using Apache POI's
 * {@code SXSSFWorkbook} for memory-efficient handling of large datasets.
 *
 * <p>Each {@link ExportToExcel} configuration within the {@code value} array generates
 * a distinct exporter class with its own sheet name, column definitions, and styling.
 * This allows multiple Excel export formats to be defined for a single class.
 *
 * <p>Example usage:
 * <pre>
 * @ExportToExcels({
 *     @ExportToExcel(
 *         className = "UserExporter",
 *         sheetName = "Users",
 *         autoSizeColumns = true,
 *         columns = {
 *             @ColumnDefinition(
 *                 fieldName = "name",
 *                 columnName = "Full Name",
 *                 type = ColumnType.STRING,
 *                 order = 1,
 *                 headerStyle = @ExcelStyle(fontName = "Arial", fontSize = 12, bold = true, backgroundColor = "CCCCCC"),
 *                 bodyStyle = @ExcelStyle(fontName = "Arial", fontSize = 10)
 *             ),
 *             @ColumnDefinition(
 *                 fieldName = "age",
 *                 type = ColumnType.NUMBER,
 *                 order = 2
 *             )
 *         }
 *     ),
 *     @ExportToExcel(
 *         className = "UserSummaryExporter",
 *         sheetName = "Summary",
 *         columns = {
 *             @ColumnDefinition(
 *                 fieldName = "age",
 *                 columnName = "User Age",
 *                 type = ColumnType.NUMBER,
 *                 order = 1
 *             )
 *         }
 *     )
 * })
 * public class User {
 *     private String name;
 *     private int age;
 *     public String getName() { return name; }
 *     public int getAge() { return age; }
 * }
 * </pre>
 *
 * <p>The above example generates two exporter classes:
 * <ul>
 *   <li>{@code UserExporter}: Exports data to a sheet named "Users" with columns "Full Name" and "age".</li>
 *   <li>{@code UserSummaryExporter}: Exports data to a sheet named "Summary" with only the "User Age" column.</li>
 * </ul>
 *
 * @since 0.1.0
 * @see ExportToExcel
 * @see ColumnDefinition
 * @see ExportToExcelService
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ExportToExcels {

    /**
     * Specifies an array of {@link ExportToExcel} configurations, each defining a separate
     * Excel exporter class. Each configuration includes details such as the generated class name,
     * sheet name, column definitions, and whether to auto-size columns.
     *
     * @return an array of {@link ExportToExcel} configurations
     */
    ExportToExcel[] value();
}