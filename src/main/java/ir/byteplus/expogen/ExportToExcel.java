package ir.byteplus.expogen;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures a single Excel exporter class to be generated for a target class.
 * Used within {@link ExportToExcels} to define multiple exporters.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({})
public @interface ExportToExcel {
    /**
     * The name of the generated exporter class (e.g., "UserExporter").
     * @return the class name
     */
    String className();

    /**
     * The name of the sheet in the generated Excel file. Defaults to the target class name if empty.
     * @return the sheet name
     */
    String sheetName() default "";

    /**
     * The array of column definitions for the Excel file.
     * @return the column configurations
     */
    ColumnDefinition[] columns();
}