package fr.qparis.romeo.excel;

import com.jacob.com.Dispatch;

import java.lang.ref.WeakReference;
import java.util.List;

public class ExcelWorkbook extends QueryStringProvider {
    private final String workbookName;
    private final WeakReference<Dispatch> workbook;
    private final List<ExcelWorksheet> worksheets;

    public ExcelWorkbook(String workbookName, Dispatch workbook, List<ExcelWorksheet> worksheets) {
        this.workbookName = workbookName;
        this.workbook = new WeakReference<>(workbook);
        this.worksheets = worksheets;
    }

    public String getWorkbookName() {
        return workbookName;
    }

    public Dispatch getWorkbook() {
        return workbook.get();
    }

    public List<ExcelWorksheet> getWorksheets() {
        return worksheets;
    }

    @Override
    public String getQueryHint() {
        return "";
    }

    @Override
    public String getPreview() {
        return workbookName;
    }
}
