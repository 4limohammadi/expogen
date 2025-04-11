package ir.byteplus.expogen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({})
public @interface ExcelStyle {
    String backgroundColor() default ""; // رنگ پس‌زمینه (مثلاً "FFFF00" برای زرد)
    String fontName() default "Arial"; // اسم فونت
    int fontSize() default 12; // اندازه فونت
    boolean bold() default false; // فونت بولد
    String alignment() default "CENTER"; // ترازبندی: LEFT, CENTER, RIGHT
}
