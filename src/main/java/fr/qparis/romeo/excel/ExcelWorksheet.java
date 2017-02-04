package fr.qparis.romeo.excel;

import com.jacob.com.Dispatch;

import java.lang.ref.WeakReference;
import java.util.List;

public class ExcelWorksheet extends QueryStringProvider {
    private final String name;
    private final String tableName;
    private final WeakReference<Dispatch> worksheet;
    private final List<ExcelColumn> columns;
    private ExcelContent content;

    public ExcelWorksheet(String name, String tableName, List<ExcelColumn> columns, Dispatch worksheet) {
        this.name = name;
        this.tableName = tableName;
        this.worksheet = new WeakReference<>(worksheet);
        this.columns = columns;
    }

    public String getName() {
        return name;
    }

    public Dispatch getWorksheet() {
        return worksheet.get();
    }

    public ExcelContent getContent() {
        return content;
    }

    public void setContent(ExcelContent content) {
        this.content = content;
    }

    public String getTableName() {
        return tableName;
    }

    public List<ExcelColumn> getColumns() {
        return columns;
    }

    @Override
    public String getQueryHint() {
        return tableName.toUpperCase();
    }

    @Override
    public String getPreview() {
        return name;
    }

}
