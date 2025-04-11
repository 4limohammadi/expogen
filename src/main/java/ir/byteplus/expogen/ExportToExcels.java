package ir.byteplus.expogen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface ExportToExcels {
    ExportToExcel[] value(); // آرایه‌ای از ExportToExcel
}
