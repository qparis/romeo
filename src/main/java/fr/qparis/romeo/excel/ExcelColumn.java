package fr.qparis.romeo.excel;

public class ExcelColumn extends QueryStringProvider {
    private final String columnName;

    public ExcelColumn(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public String getQueryHint() {
        return columnName.toUpperCase();
    }

    @Override
    public String getPreview() {
        return columnName.toUpperCase();
    }

    public String getColumnName() {
        return columnName;
    }
}
