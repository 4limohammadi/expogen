package ir.byteplus.expogen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({})
public @interface ExportToExcel {
    String className(); // اسم کلاس تولیدشده (جایگزین serviceName)
    String sheetName() default "";
    ColumnDefinition[] columns();
}