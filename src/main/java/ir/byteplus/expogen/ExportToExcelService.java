package ir.byteplus.expogen;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.util.List;

/**
 * Defines the contract for classes that export data to an Excel SXSSFWorkbook.
 * Generated exporter classes implement this interface.
 *
 * @param <T> the type of the data objects to export
 */
public interface ExportToExcelService<T> {
    /**
     * Exports a list of data objects to an Excel SXSSFWorkbook.
     *
     * @param data the list of objects to export
     * @return the generated Excel SXSSFWorkbook
     */
    SXSSFWorkbook export(List<T> data);
}