package ir.byteplus.expogen;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class ExcelProcessorTest {

    private static final Compiler compiler = Compiler.javac()
            .withProcessors(new ExcelProcessor());

    @Test
    void testProcessorGeneratesExporterSuccessfully() {
        JavaFileObject userDto = JavaFileObjects.forResource("test-sources/UserDto.java");

        Compilation compilation = compiler.compile(userDto);

        CompilationSubject.assertThat(compilation)
                .succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedSourceFile("ir.byteplus.expogen.test.UserExporter")
                .hasSourceEquivalentTo(JavaFileObjects.forResource("test-sources/expected/UserExporter.java"));
    }

    @Test
    void testGeneratedExporterProducesCorrectExcel() throws Exception {
        JavaFileObject userDto = JavaFileObjects.forResource("test-sources/UserDto.java");

        Compilation compilation = compiler.compile(userDto);

        CompilationSubject.assertThat(compilation).succeeded();

        // Load and instantiate the generated UserExporter
        File outputDir = new File("target/generated-test-sources");
        ClassLoader classLoader = Compiler.javac()
                .withOptions("-d", outputDir.getAbsolutePath())
                .compile(userDto)
                .generatedFiles()
                .stream()
                .filter(f -> f.getKind() == JavaFileObject.Kind.CLASS)
                .map(f -> new File(outputDir, f.getName().replace(".class", "")))
                .findFirst()
                .map(f -> {
                    try {
                        return new URLClassLoader(new URL[]{outputDir.toURI().toURL()}, getClass().getClassLoader());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseThrow(() -> new IllegalStateException("No class file generated"));

        Class<?> exporterClass = classLoader.loadClass("ir.byteplus.expogen.test.UserExporter");
        Object exporter = exporterClass.getDeclaredConstructor().newInstance();

        // Prepare test data
        Object userDtoInstance = Class.forName("ir.byteplus.expogen.test.UserDto")
                .getDeclaredConstructor(String.class, int.class, Date.class)
                .newInstance("Ali", 30, new Date());

        // Invoke export method
        Method exportMethod = exporterClass.getMethod("export", java.util.List.class);
        SXSSFWorkbook workbook = (SXSSFWorkbook) exportMethod.invoke(exporter, Arrays.asList(userDtoInstance));

        // Verify Excel content
        SXSSFSheet sheet = workbook.getSheet("Users");
        assertNotNull(sheet, "Sheet should exist");
        assertEquals("نام کاربر", sheet.getRow(0).getCell(0).getStringCellValue(), "Header for name should match");
        assertEquals("سن", sheet.getRow(0).getCell(1).getStringCellValue(), "Header for age should match");
        assertEquals("Ali", sheet.getRow(1).getCell(0).getStringCellValue(), "Name should match");
        assertEquals(30.0, sheet.getRow(1).getCell(1).getNumericCellValue(), "Age should match");
        assertEquals("SUM(B2:B2)", sheet.getRow(2).getCell(1).getCellFormula(), "Formula should be SUM");
    }

    @Test
    void testProcessorHandlesEmptyAnnotations() {
        JavaFileObject emptyDto = JavaFileObjects.forSourceLines(
                "ir.byteplus.expogen.test.EmptyDto",
                "package ir.byteplus.expogen.test;",
                "import ir.byteplus.expogen.ExportToExcels;",
                "@ExportToExcels({})",
                "public class EmptyDto implements ir.byteplus.expogen.Exportable {",
                "}"
        );

        Compilation compilation = compiler.compile(emptyDto);

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation)
                .generatedSourceFile("ir.byteplus.expogen.test.EmptyDtoExporter")
                .containsElementsIn(JavaFileObjects.forSourceLines(
                        "ir.byteplus.expogen.test.EmptyDtoExporter",
                        "public class EmptyDtoExporter implements ExportToExcelService<EmptyDto> {"
                ));
    }

    @Test
    void testProcessorHandlesInvalidColumnType() {
        JavaFileObject invalidDto = JavaFileObjects.forSourceLines(
                "ir.byteplus.expogen.test.InvalidDto",
                "package ir.byteplus.expogen.test;",
                "import ir.byteplus.expogen.*;",
                "@ExportToExcels({",
                "    @ExportToExcel(",
                "        className = \"InvalidExporter\",",
                "        columns = {",
                "            @ColumnDefinition(fieldName = \"value\", type = ColumnType.STRING, headerStyle = @ExcelStyle(fontName = \"Arial\", fontSize = 12), bodyStyle = @ExcelStyle(fontName = \"Arial\", fontSize = 12))",
                "        })",
                "})",
                "public class InvalidDto {",
                "    private int value;", // Mismatch: int field, STRING type
                "    public int getValue() { return value; }",
                "}"
        );

        Compilation compilation = compiler.compile(invalidDto);

        CompilationSubject.assertThat(compilation).succeeded();
        // Note: Processor should ideally warn about type mismatch, but current code doesn't
    }
}