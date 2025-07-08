package ir.byteplus.expogen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the configuration for generating an Excel exporter class for a target class.
 * This annotation is used within {@link ExportToExcels} to specify one or more Excel exporter
 * configurations. Each configuration generates a distinct exporter class that implements
 * {@link ExportToExcelService} to export data to an Excel file using Apache POI's
 * {@code SXSSFWorkbook} for memory-efficient processing of large datasets.
 *
 * <p>Example usage:
 * <pre>
 * &#064;ExportToExcels({
 *     &#064;ExportToExcel(
 *         className = "UserExporter",
 *         sheetName = "Users",
 *         autoSizeColumns = true,
 *         columns = {
 *             &#064;ColumnDefinition(fieldName = "name", columnName = "Full Name", type = ColumnType.STRING, order = 1),
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
 * </pre>
 *
 * <p>The above example generates a {@code UserExporter} class that exports {@code User} objects
 * to an Excel sheet named "Users" with two columns: "Full Name" and "age".
 *
 * @since 0.1.0
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ExportToExcel {

    /**
     * Specifies the name of the generated exporter class (e.g., "UserExporter").
     * The name must be a valid Java class name (starting with a letter, followed by letters or digits).
     * If invalid, a compilation error will be raised.
     *
     * @return the name of the generated exporter class
     */
    String className();

    /**
     * Specifies the name of the sheet in the generated Excel file.
     * If empty, defaults to the simple name of the annotated class.
     *
     * @return the name of the Excel sheet
     */
    String sheetName() default "";

    /**
     * Defines the columns to be included in the Excel file.
     * Each column is configured using a {@link ColumnDefinition} annotation,
     * specifying the field name, column name, data type, and other properties.
     *
     * @return an array of column configurations
     */
    ColumnDefinition[] columns();

    /**
     * Determines whether the column widths in the Excel file should be automatically adjusted
     * to fit their content. Enabling this may improve readability but can impact performance
     * for large datasets due to the computational cost of calculating column widths.
     * Defaults to {@code false} to optimize performance.
     *
     * @return {@code true} if columns should be auto-sized, {@code false} otherwise
     */
    boolean autoSizeColumns() default false;
}