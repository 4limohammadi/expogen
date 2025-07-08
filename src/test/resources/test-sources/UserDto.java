package ir.byteplus.expogen.test;

import ir.byteplus.expogen.*;
import java.util.Date;

@ExportToExcels({
        @ExportToExcel(
                className = "UserExporter",
                sheetName = "Users",
                columns = {
                        @ColumnDefinition(
                                fieldName = "name",
                                columnName = "نام کاربر",
                                type = ColumnType.STRING,
                                headerStyle = @ExcelStyle(fontName = "Arial", fontSize = 12, bold = true, backgroundColor = "FF00FF", alignment = "CENTER"),
                                bodyStyle = @ExcelStyle(fontName = "Arial", fontSize = 12, alignment = "CENTER")
                        ),
                        @ColumnDefinition(
                                fieldName = "age",
                                columnName = "سن",
                                type = ColumnType.NUMBER,
                                headerStyle = @ExcelStyle(fontName = "Arial", fontSize = 12, alignment = "CENTER"),
                                bodyStyle = @ExcelStyle(fontName = "Arial", fontSize = 12, alignment = "CENTER"),
                                formula = @ExcelFormula(formula = "SUM", applyTo = "COLUMN", rowOffset = -1)
                        ),
                        @ColumnDefinition(
                                fieldName = "birthDate",
                                columnName = "تاریخ تولد",
                                type = ColumnType.DATE,
                                headerStyle = @ExcelStyle(fontName = "Arial", fontSize = 12, alignment = "CENTER"),
                                bodyStyle = @ExcelStyle(fontName = "Arial", fontSize = 12, alignment = "CENTER")
                        )
                }
        )
})
public class UserDto {
    private String name;
    private int age;
    private Date birthDate;

    public String getName() { return name; }
    public int getAge() { return age; }
    public Date getBirthDate() { return birthDate; }

    public UserDto(String name, int age, Date birthDate) {
        this.name = name;
        this.age = age;
        this.birthDate = birthDate;
    }
}