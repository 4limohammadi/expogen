package ir.byteplus.expogen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures the styling properties for headers or data cells in the generated Excel file.
 * This annotation is used within {@link ColumnDefinition} to customize the appearance of
 * column headers and data cells.
 *
 * <p>Example usage:
 * <pre>{@code
 * &#064;ExportToExcels({
 *     &#064;ExportToExcel(
 *         className = "UserExporter",
 *         sheetName = "Users",
 *         columns = {
 *             &#064;ColumnDefinition(
 *                 fieldName = "name",
 *                 columnName = "Full Name",
 *                 type = ColumnType.STRING,
 *                 order = 1,
 *                 headerStyle = &#064;ExcelStyle(
 *                     fontName = "Arial",
 *                     fontSize = 12,
 *                     bold = true,
 *                     backgroundColor = "CCCCCC",
 *                     alignment = "CENTER"
 *                 ),
 *                 bodyStyle = &#064;ExcelStyle(
 *                     fontName = "Arial",
 *                     fontSize = 10,
 *                     alignment = "LEFT"
 *                 )
 *             )
 *         }
 *     )
 * })
 * public class User {
 *     private String name;
 *     public String getName() { return name; }
 * }
 * }</pre>
 *
 * @since 0.0.0.2
 * @see ColumnDefinition
 * @see ExportToExcel
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ExcelStyle {

    /**
     * Specifies the background color of the cell in hexadecimal format (e.g., "FFFF00" for yellow).
     * The value must be a valid 6-digit hexadecimal color code (without the "#" prefix).
     * If empty, no background color is applied.
     * Invalid color codes will cause a compilation error during annotation processing.
     *
     * @return the background color in hexadecimal format
     */
    String backgroundColor() default "";

    /**
     * Specifies the font name to use for the cell (e.g., "Arial", "Times New Roman").
     * Defaults to "Arial" if not specified. Ensure the font is supported by Apache POI
     * and available in the target environment.
     *
     * @return the font name
     */
    String fontName() default "Arial";

    /**
     * Specifies the font size in points (e.g., 12 for 12pt).
     * Must be a positive integer. Defaults to 12 if not specified.
     * Invalid (e.g., negative or zero) values may cause a compilation error.
     *
     * @return the font size in points
     */
    int fontSize() default 12;

    /**
     * Determines whether the font should be bold.
     * Defaults to {@code false} (regular weight).
     *
     * @return {@code true} if the font should be bold, {@code false} otherwise
     */
    boolean bold() default false;

    /**
     * Specifies the text alignment for the cell.
     * Supported values are:
     * <ul>
     *   <li>{@code LEFT}: Left-aligned text</li>
     *   <li>{@code CENTER}: Center-aligned text</li>
     *   <li>{@code RIGHT}: Right-aligned text</li>
     * </ul>
     * Defaults to {@code CENTER} if not specified.
     * Invalid values may cause a compilation error during annotation processing.
     *
     * @return the alignment value
     */
    String alignment() default "CENTER";
}