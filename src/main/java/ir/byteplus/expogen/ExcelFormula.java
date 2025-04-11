package ir.byteplus.expogen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({})
public @interface ExcelFormula {
    String formula() default ""; // فرمول اکسل (مثلاً "SUM(A2:A10)")
    String applyTo() default "COLUMN"; // فرمول برای کل ستون اعمال بشه ("COLUMN") یا فقط یه سلول خاص ("CELL")
    int rowOffset() default -1; // اگه applyTo=CELL باشه، فرمول توی کدوم ردیف اعمال بشه (-1 یعنی آخرین ردیف)
}
