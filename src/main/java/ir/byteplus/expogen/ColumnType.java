package ir.byteplus.expogen;

/**
 * Defines the data type for a column in the generated Excel file.
 * This enum is used within the {@link ColumnDefinition} annotation to specify
 * how a column's data should be formatted in the exported Excel file.
 * Each type corresponds to a specific formatting rule applied during export
 * using Apache POI's {@code SXSSFWorkbook}.
 *
 * <p>The supported data types and their formatting are:
 * <ul>
 *   <li>{@link #STRING}: Formats the cell as plain text.</li>
 *   <li>{@link #NUMBER}: Formats the cell as a number with two decimal places (e.g., "0.00").</li>
 *   <li>{@link #DATE}: Formats the cell as a date in the format "dd/mm/yyyy".</li>
 *   <li>{@link #BOOLEAN}: Formats the cell as a boolean value (true/false).</li>
 * </ul>
 *
 * <p>Example usage within {@link ColumnDefinition}:
 * <pre>
 * @ExportToExcels({
 *     @ExportToExcel(
 *         className = "UserExporter",
 *         sheetName = "Users",
 *         columns = {
 *             @ColumnDefinition(fieldName = "name", columnName = "Full Name", type = ColumnType.STRING, order = 1),
 *             @ColumnDefinition(fieldName = "age", type = ColumnType.NUMBER, order = 2),
 *             @ColumnDefinition(fieldName = "birthDate", type = ColumnType.DATE, order = 3),
 *             @ColumnDefinition(fieldName = "active", type = ColumnType.BOOLEAN, order = 4)
 *         }
 *     )
 * })
 * public class User {
 *     private String name;
 *     private double age;
 *     private java.util.Date birthDate;
 *     private boolean active;
 *     public String getName() { return name; }
 *     public double getAge() { return age; }
 *     public java.util.Date getBirthDate() { return birthDate; }
 *     public boolean isActive() { return active; }
 * }
 * </pre>
 *
 * <p>The above example generates an Excel file with four columns, each formatted
 * according to its specified {@code ColumnType}.
 *
 * @since 0.1.0
 * @see ColumnDefinition
 * @see ExportToExcel
 */
public enum ColumnType {
    /**
     * Represents a text column in the Excel file.
     * Data is formatted as plain text using the {@code toString()} method.
     */
    STRING,

    /**
     * Represents a numeric column in the Excel file.
     * Data is formatted as a number with two decimal places (e.g., "0.00").
     * The field must return a {@code Number} type (e.g., {@code Integer}, {@code Double}).
     */
    NUMBER,

    /**
     * Represents a date column in the Excel file.
     * Data is formatted as a date in the "dd/mm/yyyy" format.
     * The field must return a {@code java.util.Date} type.
     */
    DATE,

    /**
     * Represents a boolean column in the Excel file.
     * Data is formatted as a boolean value (true/false).
     * The field must return a {@code Boolean} type.
     */
    BOOLEAN
}