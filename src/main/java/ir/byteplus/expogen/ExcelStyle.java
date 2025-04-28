package ir.byteplus.expogen;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the styling properties for headers or data cells in the generated Excel file.
 * Used within {@link ColumnDefinition} to customize appearance.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({})
public @interface ExcelStyle {
    /**
     * The background color in hexadecimal format (e.g., "FFFF00" for yellow). Empty string means no color.
     * @return the background color
     */
    String backgroundColor() default "";

    /**
     * The font name to use (e.g., "Arial", "Times New Roman").
     * @return the font name
     */
    String fontName() default "Arial";

    /**
     * The font size in points (e.g., 12).
     * @return the font size
     */
    int fontSize() default 12;

    /**
     * Whether the font should be bold.
     * @return true if bold, false otherwise
     */
    boolean bold() default false;

    /**
     * The text alignment: "LEFT", "CENTER", or "RIGHT".
     * @return the alignment
     */
    String alignment() default "CENTER";
}