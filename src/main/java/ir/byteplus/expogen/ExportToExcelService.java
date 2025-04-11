package ir.byteplus.expogen;

import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public interface ExportToExcelService <T>{

    Workbook export(List<T> data);
}
