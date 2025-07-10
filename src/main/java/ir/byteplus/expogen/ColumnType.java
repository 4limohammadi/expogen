package ir.byteplus.expogen;

/**
 * Specifies the data type for a column in the generated Excel file, used within
 * {@link ColumnDefinition}. Defines how data is formatted in the Excel output.
 *
 * <p>Example usage:
 * <pre>{@code
 * &#064;ExportToExcels({
 *     &#064;ExportToExcel(
 *         className = "UserExporter",
 *         sheetName = "Users",
 *         columns = {
 *             &#064;ColumnDefinition(fieldName = "name", columnName = "Full Name", type = ColumnType.STRING, order = 1),
 *             &#064;ColumnDefinition(fieldName = "age", type = ColumnType.NUMBER, order = 2),
 *             &#064;ColumnDefinition(fieldName = "birthDate", type = ColumnType.DATE, order = 3),
 *             &#064;ColumnDefinition(fieldName = "active", type = ColumnType.BOOLEAN, order = 4)
 *         }
 *     )
 * })
 * public class User {
 *     private String name;
 *     private int age;
 *     private java.util.Date birthDate;
 *     private boolean active;
 *     public String getName() { return name; }
 *     public int getAge() { return age; }
 *     public java.util.Date getBirthDate() { return birthDate; }
 *     public boolean isActive() { return active; }
 * }
 * }</pre>
 *
 * @since 0.0.0.2
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