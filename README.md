# Expogen - Excel Exporter Generator

Expogen is a Java annotation processor that generates memory-efficient Excel exporter classes at compile-time. By annotating your data classes with `@ExportToExcels`, Expogen creates implementations of `ExportToExcelService` that export data to Excel files using Apache POI's `SXSSFWorkbook`. It supports customizable column definitions, styling, and data types, making it ideal for generating Excel files from large datasets with minimal memory usage.

## Features

- **Annotation-Driven**: Define Excel export configurations using annotations like `@ExportToExcels`, `@ExportToExcel`, and `@ColumnDefinition`.
- **Memory Efficiency**: Leverages `SXSSFWorkbook` for streaming data to disk, suitable for large-scale exports.
- **Flexible Column Configuration**: Supports custom column names, data types (`STRING`, `NUMBER`, `DATE`, `BOOLEAN`), and styling (font, alignment, background color).
- **Nested Field Support**: Access nested object fields using dot notation (e.g., `user.address.city`).
- **Multiple Exporters**: Generate multiple exporter classes for a single data class with different configurations.
- **Compile-Time Validation**: Ensures valid getter methods, class names, and styling properties, reporting errors during compilation.

## Prerequisites

- Java 8 or higher
- Apache Maven or Gradle for dependency management
- Apache POI (automatically included as transitive dependencies)

## Installation

Add Expogen as a dependency in your Maven or Gradle project. The required Apache POI dependencies are included transitively.

### Maven

```xml
<dependency>
    <groupId>ir.byteplus</groupId>
    <artifactId>expogen</artifactId>
    <version>0.0.1.2</version>
</dependency>
```

### Gradle

```groovy
implementation 'ir.byteplus:expogen:0.0.1.2'
```

Expogen includes the following Apache POI dependencies:
- `org.apache.poi:poi:5.2.3`
- `org.apache.poi:poi-ooxml:5.2.3`

Ensure your project is configured to fetch transitive dependencies.

## Usage

1. **Annotate Your Data Class**: Use `@ExportToExcels` to define one or more Excel exporter configurations.
2. **Compile the Project**: The `ExcelProcessor` generates exporter classes during compilation.
3. **Use the Generated Exporter**: Instantiate the generated class and call its `export` method to generate an Excel file.

### Example

```java
package example;

import ir.byteplus.expogen.*;

@ExportToExcels({
    @ExportToExcel(
        className = "UserExporter",
        sheetName = "Users",
        autoSizeColumns = true,
        columns = {
            @ColumnDefinition(
                fieldName = "name",
                columnName = "Full Name",
                type = ColumnType.STRING,
                order = 1,
                headerStyle = @ExcelStyle(fontName = "Arial", fontSize = 12, bold = true, backgroundColor = "CCCCCC", alignment = "CENTER"),
                bodyStyle = @ExcelStyle(fontName = "Arial", fontSize = 10, alignment = "LEFT")
            ),
            @ColumnDefinition(
                fieldName = "age",
                type = ColumnType.NUMBER,
                order = 2,
                headerStyle = @ExcelStyle(fontName = "Arial", fontSize = 12, bold = true),
                bodyStyle = @ExcelStyle(fontName = "Arial", fontSize = 10)
            ),
            @ColumnDefinition(
                fieldName = "birthDate",
                type = ColumnType.DATE,
                order = 3,
                headerStyle = @ExcelStyle(fontName = "Arial", fontSize = 12, bold = true),
                bodyStyle = @ExcelStyle(fontName = "Arial", fontSize = 10)
            )
        }
    ),
    @ExportToExcel(
        className = "UserSummaryExporter",
        sheetName = "Summary",
        columns = {
            @ColumnDefinition(
                fieldName = "age",
                columnName = "User Age",
                type = ColumnType.NUMBER,
                order = 1
            )
        }
    )
})
public class User {
    private String name;
    private int age;
    private java.util.Date birthDate;

    public User(String name, int age, java.util.Date birthDate) {
        this.name = name;
        this.age = age;
        this.birthDate = birthDate;
    }

    public String getName() { return name; }
    public int getAge() { return age; }
    public java.util.Date getBirthDate() { return birthDate; }
}
```

**Using the Generated Exporter**:

```java
import example.UserExporter;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import java.util.Arrays;
import java.util.Date;

public class Main {
    public static void main(String[] args) throws Exception {
        UserExporter exporter = new UserExporter();
        List<User> users = Arrays.asList(
            new User("Alice", 30, new Date()),
            new User("Bob", 25, new Date())
        );
        SXSSFWorkbook workbook = exporter.export(users);
        try (var out = new java.io.FileOutputStream("users.xlsx")) {
            workbook.write(out);
        }
        workbook.close();
    }
}
```

This generates an Excel file (`users.xlsx`) with a "Users" sheet containing "Full Name", "age", and "birthDate" columns, styled and formatted as specified.

## API Reference

### Annotations

- **`@ExportToExcels`**: Container annotation for multiple `@ExportToExcel` configurations.
  - Usage: Applied to a class to define one or more Excel exporters.
- **`@ExportToExcel`**: Defines a single exporter class with sheet name, columns, and auto-sizing options.
  - Parameters: `className`, `sheetName`, `autoSizeColumns`, `columns`.
- **`@ColumnDefinition`**: Configures a column with field name, display name, data type, order, and styles.
  - Parameters: `fieldName`, `columnName`, `type`, `order`, `headerStyle`, `bodyStyle`, `formula` (not currently supported).
- **`@ExcelStyle`**: Specifies styling for headers or data cells (font, size, boldness, background color, alignment).
  - Parameters: `fontName`, `fontSize`, `bold`, `backgroundColor`, `alignment` (LEFT, CENTER, RIGHT).
- **`@ExcelFormula`**: Reserved for future formula support (not currently implemented).

### Enum

- **`ColumnType`**: Defines column data types with specific formatting:
  - `STRING`: Plain text (uses `toString()`).
  - `NUMBER`: Number with two decimal places ("0.00").
  - `DATE`: Date in "dd/mm/yyyy" format.
  - `BOOLEAN`: True/false values.

### Interface

- **`ExportToExcelService<T>`**: Contract for exporter classes with two methods:
  - `export(List<T> data)`: Exports a list of objects to an Excel file.
  - `export(Stream<T> dataStream)`: Exports a stream of objects, optimized for large datasets.

## Development

### Building the Project

Clone the repository and build with Maven:

```bash
git clone https://github.com/byteplus/expogen.git
cd expogen
mvn clean install
```

### Running Tests

Run unit tests to verify functionality:

```bash
mvn test
```

### Contributing

We welcome contributions! To contribute:

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/your-feature`).
3. Commit your changes (`git commit -m "Add your feature"`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Open a pull request.

Please ensure your code includes tests and follows the project's coding standards.

## License

Expogen is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contact

For issues or feature requests, please open an issue on the [GitHub repository](https://github.com/byteplus/expogen).
