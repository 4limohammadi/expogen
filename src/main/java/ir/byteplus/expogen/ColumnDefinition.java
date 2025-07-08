package ir.byteplus.expogen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures a single column in the generated Excel file, specifying the data source field,
 * display name, styling, data type, and optional formula.
 * This annotation is used within {@link ExportToExcel} to define the structure and formatting
 * of individual columns in the exported Excel file.
 *
 * <p>The {@code fieldName} must correspond to a valid field in the annotated class, with
 * appropriate getter methods. Nested fields (e.g., "user.address.city") are supported,
 * provided valid getters exist for each segment. If the field or its getters are invalid,
 * a compilation error will be raised during annotation processing.
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
 *                 order = 2,
 *                 headerStyle = @ExcelStyle(fontName = "Arial", fontSize = 12, bold = true)
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
 * <p>The above example generates an Excel file with two columns: "Full Name" (string) and "age" (number),
 * with specified header and body styles, sorted by the {@code order} value.
 *
 * @since 0.1.0
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ColumnDefinition {

    /**
     * Specifies the name of the field in the source class to extract data from.
     * Supports nested fields using dot notation (e.g., "user.address.city").
     * The field must have valid getter methods for each segment, or a compilation error
     * will be raised during annotation processing.
     *
     * @return the field name or nested field path
     */
    String fieldName();

    /**
     * Specifies the display name of the column in the Excel file.
     * If empty, defaults to the value of {@code fieldName}.
     *
     * @return the column display name
     */
    String columnName() default "";

    /**
     * Configures the style for the column's header cell, including font, size, boldness,
     * background color, and alignment.
     * Defaults to a basic style if not specified.
     *
     * @return the header style configuration
     * @see ExcelStyle
     */
    ExcelStyle headerStyle() default @ExcelStyle;

    /**
     * Configures the style for the column's data cells, including font, size, boldness,
     * background color, and alignment.
     * Defaults to a basic style if not specified.
     *
     * @return the body style configuration
     * @see ExcelStyle
     */
    ExcelStyle bodyStyle() default @ExcelStyle;

    /**
     * Specifies the order of the column in the Excel file.
     * Columns are sorted in ascending order based on this value.
     * If multiple columns have the same order, their relative order is not guaranteed.
     *
     * @return the column order
     */
    int order() default 0;

    /**
     * Defines an optional formula to apply to the column.
     * Currently, formulas are not supported in the generated exporter and are ignored.
     * Future versions may enable formula support.
     *
     * @return the formula configuration
     * @see ExcelFormula
     */
    ExcelFormula formula() default @ExcelFormula;

    /**
     * Specifies the data type of the column to control its formatting in the Excel file.
     * Supported types include:
     * <ul>
     *   <li>{@code STRING}: Formats the cell as plain text.</li>
     *   <li>{@code NUMBER}: Formats the cell as a number with two decimal places (e.g., "0.00").</li>
     *   <li>{@code DATE}: Formats the cell as a date (e.g., "dd/mm/yyyy").</li>
     *   <li>{@code BOOLEAN}: Formats the cell as a boolean value.</li>
     * </ul>
     * Defaults to {@code STRING} if not specified.
     *
     * @return the column data type
     * @see ColumnType
     */
    ColumnType type() default ColumnType.STRING;
}