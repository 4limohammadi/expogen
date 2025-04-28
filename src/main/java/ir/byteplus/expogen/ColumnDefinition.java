package ir.byteplus.expogen;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a column in the generated Excel file, including its field source, display name, styles, and formula.
 * This annotation is used within {@link ExportToExcel} to configure individual columns.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({})
public @interface ColumnDefinition {
    /**
     * The name of the field in the source class to extract data from. Supports nested fields (e.g., "a.name").
     * @return the field name or nested field path
     */
    String fieldName();

    /**
     * The display name of the column in the Excel file. If empty, defaults to the field name.
     * @return the column name
     */
    String columnName() default "";

    /**
     * The style to apply to the column header.
     * @return the header style configuration
     */
    ExcelStyle headerStyle() default @ExcelStyle;

    /**
     * The style to apply to the column data cells.
     * @return the body style configuration
     */
    ExcelStyle bodyStyle() default @ExcelStyle;

    /**
     * The order of the column in the Excel file. Columns are sorted by this value in ascending order.
     * @return the column order
     */
    int order() default 0;

    /**
     * The formula to apply to the column, if any.
     * @return the formula configuration
     */
    ExcelFormula formula() default @ExcelFormula;

    /**
     * The data type of the column to control formatting in the Excel file.
     * @return the column type (e.g., STRING, NUMBER, DATE, BOOLEAN)
     */
    ColumnType type() default ColumnType.STRING;
}