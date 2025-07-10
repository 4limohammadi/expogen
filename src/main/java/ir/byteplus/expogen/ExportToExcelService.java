package ir.byteplus.expogen;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.util.List;
import java.util.stream.Stream;

/**
 * Defines the contract for classes that export data to an Excel file using Apache POI's
 * {@code SXSSFWorkbook}. This interface is implemented by classes generated through the
 * {@link ExportToExcels} and {@link ExportToExcel} annotations.
 *
 * <p>Example usage with a generated exporter:
 * <pre>{@code
 * &#064;ExportToExcels({
 *     &#064;ExportToExcel(
 *         className = "UserExporter",
 *         sheetName = "Users",
 *         autoSizeColumns = true,
 *         columns = {
 *             &#064;ColumnDefinition(
 *                 fieldName = "name",
 *                 columnName = "Full Name",
 *                 type = ColumnType.STRING,
 *                 order = 1,
 *                 headerStyle = &#064;ExcelStyle(fontName = "Arial", fontSize = 12, bold = true, backgroundColor = "CCCCCC"),
 *                 bodyStyle = &#064;ExcelStyle(fontName = "Arial", fontSize = 10)
 *             ),
 *             &#064;ColumnDefinition(fieldName = "age", type = ColumnType.NUMBER, order = 2)
 *         }
 *     )
 * })
 * public class User {
 *     private String name;
 *     private int age;
 *     public String getName() { return name; }
 *     public int getAge() { return age; }
 * }
 *
 * // Using the generated exporter
 * UserExporter exporter = new UserExporter();
 * List<User> users = Arrays.asList(new User("Alice", 30), new User("Bob", 25));
 * SXSSFWorkbook workbook = exporter.export(users);
 * try (FileOutputStream out = new FileOutputStream("users.xlsx")) {
 *     workbook.write(out);
 * }
 * }</pre>
 *
 * @param <T> the type of the data objects to export
 * @since 0.0.0.2
 * @see ExportToExcels
 * @see ExportToExcel
 * @see ColumnDefinition
 */
public interface ExportToExcelService<T> {

    /**
     * Exports a list of data objects to an Excel file, returning an {@code SXSSFWorkbook}.
     * The generated Excel file includes a sheet with configured columns and styles as defined
     * in the corresponding {@link ExportToExcel} annotation. If the input list is {@code null}
     * or empty, only the header row is generated.
     *
     * @param data the list of objects to export, or {@code null} to generate only headers
     * @return the generated {@code SXSSFWorkbook} containing the exported data
     */
    SXSSFWorkbook export(List<T> data);

    /**
     * Exports a stream of data objects to an Excel file, returning an {@code SXSSFWorkbook}.
     * This method is optimized for large datasets, as it processes data incrementally using
     * a {@code Stream}. The generated Excel file includes a sheet with configured columns
     * and styles as defined in the corresponding {@link ExportToExcel} annotation.
     * If the input stream is {@code null} or empty, only the header row is generated.
     *
     * @param dataStream the stream of objects to export, or {@code null} to generate only headers
     * @return the generated {@code SXSSFWorkbook} containing the exported data
     */
    SXSSFWorkbook export(Stream<T> dataStream);
}