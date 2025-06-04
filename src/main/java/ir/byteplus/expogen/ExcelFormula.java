package ir.byteplus.expogen;

import javax.lang.model.element.TypeElement;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies an Excel formula to be applied to a column or cell in the generated Excel file.
 * Used within {@link ColumnDefinition} to add computational logic.
 * Please don't use formula with @{@link ExcelProcessor#writeExportMethodFromStream(PrintWriter, TypeElement, String, ColumnDefinition[])},
 */
@Retention(RetentionPolicy.SOURCE)
@Target({})
public @interface ExcelFormula {
    /**
     * The Excel formula to apply (e.g., "SUM", "AVERAGE"). Does not include the range; range is added dynamically.
     * @return the formula name
     */
    String formula() default "";

    /**
     * Specifies where the formula should be applied: "COLUMN" for the entire column or "CELL" for a specific row.
     * @return "COLUMN" or "CELL"
     */
    String applyTo() default "COLUMN";

    /**
     * The row offset where the formula should be applied if applyTo is "CELL". Use -1 for the last row.
     * @return the row offset
     */
    int rowOffset() default -1;
}