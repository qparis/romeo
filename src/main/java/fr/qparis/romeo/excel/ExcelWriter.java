package fr.qparis.romeo.excel;


import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.SafeArray;
import com.jacob.com.Variant;
import fr.qparis.romeo.sql.SQLResult;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class ExcelWriter {
    public void write(SQLResult sqlResult) {
        if(sqlResult == null) {
            return;
        }
        final ActiveXComponent excel =
                ActiveXComponent.connectToActiveInstance("Excel.Application");
        if(excel == null) {
            return;
        }

        final Dispatch workbooks = excel.getProperty("Workbooks").toDispatch();
        final Dispatch newWorkbook = Dispatch.call(workbooks, "Add").getDispatch();
        final Dispatch worksheets = Dispatch.call(newWorkbook, "Worksheets").getDispatch();
        final Dispatch worksheet = Dispatch.call(worksheets, "Item", 1).getDispatch();

        final Dispatch range = Dispatch.call(worksheet, "Range", "A1:"+ExcelColumnConverter.toName(sqlResult.getColumns().size()) + Integer.toString(1 + sqlResult.getResults().size()))
                .getDispatch();

        final SafeArray safeArray = (SafeArray) Dispatch.get(range, "Value2").toJavaObject();

        for (int i = 0; i < sqlResult.getColumns().size(); i++) {
            final String columnName = sqlResult.getColumns().get(i);
            safeArray.setString(1, i + 1, columnName);
            for (int j = 0; j < sqlResult.getResults().size(); j++) {
                writeValue(safeArray, i, j, sqlResult.getResults().get(j).get(columnName));
            }
        }

        Dispatch.put(range, "Value", safeArray);
    }


    private void writeValue(SafeArray safeArray, int i, int j, Object data) {
        if(data == null) {
            return;
        }
        switch (data.getClass().getCanonicalName()) {
            case "java.lang.Double":
                safeArray.setDouble(2 + j, i + 1, (Double) data);
                break;
            case "java.math.BigDecimal":
                safeArray.setString(2 + j, i + 1, ((BigDecimal) data).toPlainString());
                break;
            case "java.util.Date":
                final Variant variantDate = new Variant();
                variantDate.putDate((Date) data);
                safeArray.setString(2 + j, i + 1, data.toString());
                break;
            case "java.sql.Timestamp":
                final Variant variantDateTimestamp = new Variant();
                variantDateTimestamp.putDate(new Date( ((Timestamp) data).getTime() ));
                safeArray.setVariant(2 + j, i + 1, variantDateTimestamp);
                break;
            default:
                safeArray.setString(2 + j, i + 1, data.toString());
                break;
        }
    }
}
