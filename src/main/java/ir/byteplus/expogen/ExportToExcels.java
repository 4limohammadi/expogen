package ir.byteplus.expogen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class to generate one or more Excel exporter classes during annotation processing.
 * This annotation serves as a container for one or more {@link ExportToExcel} annotations.
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
 *             &#064;ColumnDefinition(
 *                 fieldName = "age",
 *                 type = ColumnType.NUMBER,
 *                 order = 2
 *             )
 *         }
 *     ),
 *     &#064;ExportToExcel(
 *         className = "UserSummaryExporter",
 *         sheetName = "Summary",
 *         columns = {
 *             &#064;ColumnDefinition(
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
 * }</pre>
 *
 * @since 0.0.0.2
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