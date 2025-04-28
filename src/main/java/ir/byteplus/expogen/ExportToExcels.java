package ir.byteplus.expogen;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Marks a class to generate one or more Excel exporter classes.
 * Each {@link ExportToExcel} within this annotation results in a separate exporter class.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ExportToExcels {
    /**
     * The array of Excel exporter configurations to generate.
     * @return the exporter configurations
     */
    ExportToExcel[] value();
}