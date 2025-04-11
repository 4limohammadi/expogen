package ir.byteplus.expogen;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({}) // فقط توی انوتیشن دیگه استفاده می‌شه
public @interface ColumnDefinition {
    String fieldName(); // اسم فیلد توی کلاس
    String columnName() default ""; // اسم ستون توی اکسل
    ExcelStyle headerStyle() default @ExcelStyle;
    ExcelStyle bodyStyle() default @ExcelStyle;
    int order() default 0; // ترتیب ستون
    ExcelFormula formula() default @ExcelFormula;
}
