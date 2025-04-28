package ir.byteplus.expogen;

/**
 * Specifies the data type of a column in the generated Excel file.
 */
public enum ColumnType {
    STRING,  // Text data
    NUMBER,  // Numeric data (e.g., integer, double)
    DATE,    // Date or datetime data
    BOOLEAN  // True/false values
}