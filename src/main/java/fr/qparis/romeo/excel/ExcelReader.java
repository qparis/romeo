package fr.qparis.romeo.excel;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.SafeArray;
import com.jacob.com.Variant;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ExcelReader {

    public List<ExcelWorkbook> getWorkBooks() {
        final ActiveXComponent excel =
                ActiveXComponent.connectToActiveInstance("Excel.Application");
        if(excel == null) {
            return new ArrayList<>();
        }
        final List<ExcelWorkbook> workbooks = fetchOpenedWorkbooks(excel);
        populate(workbooks);
        return workbooks;
    }

    public void populate(ExcelWorksheet excelWorksheet) {
        final List<String> header = readHeader(excelWorksheet.getWorksheet());
        if (header != null) {
            final Iterable<List<Object>> content = readContent(excelWorksheet, header.size());
            excelWorksheet.setContent(new ExcelContent(header, content));
        }
    }

    private Iterable<List<Object>> readContent(ExcelWorksheet excelWorksheet, int headerSize) {
        if (headerSize == 0) {
            return new LinkedList<>();
        }

        System.out.println("Reading : " + excelWorksheet.getName());
        final String rangeValue = "A2:" + ExcelColumnConverter.toName(headerSize) + "300000";
        final Dispatch range = Dispatch.call(excelWorksheet.getWorksheet(), "Range", rangeValue).getDispatch();
        return iterateRange(() -> range);
    }

    private List<String> normalizeHeaders(List<Object> header) {
        final List<String> results = new ArrayList<>();
        int i = 0;
        for (Object field : header) {
            final String realHeaderName;
            if (field == null || StringUtils.isBlank(field.toString())) {
                if (i == 0) {
                    realHeaderName = "ID_KEY";
                } else {
                    break;
                }
            } else {
                realHeaderName = formatName(field);
            }

            if (results.contains(realHeaderName)) {
                final long count = results.stream().filter(s -> s.equals(realHeaderName)).count();
                results.add(realHeaderName + "_" + count);
            } else {
                results.add(realHeaderName);
            }
            i += 1;
        }

        return results;
    }

    private String formatName(Object field) {
        String simplifiedName = field.toString().replaceAll("[^A-Za-z0-9]", "_");
        if(simplifiedName.startsWith("_")) {
            simplifiedName = "C"+simplifiedName;
        }
        return simplifiedName;
    }

    private List<String> readHeader(Dispatch worksheet) {
        final Dispatch range = Dispatch.call(worksheet, "Range", "1:1").getDispatch();
        final Iterable<List<Object>> results = iterateRange(() -> range);
        return normalizeHeaders(results.iterator().next());
    }

    private Iterable<List<Object>> iterateRange(Supplier<Dispatch> range) {
        return () -> new Iterator<List<Object>>() {
            int i = 1;
            final SafeArray valued = (SafeArray) Dispatch.call(range.get(), "Value").toJavaObject();
            final int xMax = valued.getUBound(2);
            final int yMax = valued.getUBound(1);
            List<Object> nextItem = getNextItem();

            @Override
            public boolean hasNext() {
                return i < yMax && nextItem != null;
            }

            @Override
            public List<Object> next() {
                if (nextItem == null) {
                    throw new NoSuchElementException();
                }
                i++;
                final List<Object> currentItem = nextItem;
                nextItem = getNextItem();
                return currentItem;
            }

            private List<Object> getNextItem() {
                final List<Object> line = new ArrayList<>(xMax);

                boolean allNull = true;

                for (int j = 1; j < xMax + 1; j++) {
                    Variant variant = valued.getVariant(i, j);
                    final Object cellContent = variant.getvt() == Variant.VariantError ? null : variant.toJavaObject();
                    if (!(cellContent == null)) {
                        allNull = false;
                    }
                    line.add(cellContent);
                }

                if (!allNull) {
                    return line;
                } else {
                    return null;
                }
            }
        };


    }

    private List<ExcelWorkbook> fetchOpenedWorkbooks(ActiveXComponent excel) {
        final List<ExcelWorkbook> excelWorkbooks = new ArrayList<>();
        final Dispatch workbooks = excel.getProperty("Workbooks").toDispatch();

        int numberOfWorkbooks = Dispatch.call(workbooks, "Count").getInt();

        for (int i = 0; i < numberOfWorkbooks; i++) {
            final Dispatch workbook = Dispatch.call(workbooks, "Item", i + 1).toDispatch();
            try {
                excelWorkbooks.add(new ExcelWorkbook(Dispatch.get(workbook, "Name").getString(), workbook, fetchWorksheets(workbook)));
            } catch(NoSuchElementException ingored) {

            }
        }

        return excelWorkbooks;
    }

    private List<ExcelWorksheet> fetchWorksheets(Dispatch workbook) {
        final List<ExcelWorksheet> excelWorksheets = new ArrayList<>();

        final Dispatch worksheets = Dispatch.get(workbook, "Worksheets").toDispatch();

        int numberOfWorksheets = Dispatch.call(worksheets, "Count").getInt();

        for (int i = 0; i < numberOfWorksheets; i++) {
            final Dispatch worksheet = Dispatch.call(worksheets, "Item", i + 1).toDispatch();
            final String workSheetName = Dispatch.get(worksheet, "Name").getString();
            final String workBookName = Dispatch.get(workbook, "Name").getString();
            final List<ExcelColumn> columns = readHeader(worksheet).stream().map(ExcelColumn::new).collect(Collectors.toList());
            final String tableName = formatName(workBookName
                    .replace(".xlsx", "")
                    .replace(".csv","")
                    .replace(".xlsb", "")
                    .replace(".xlsm", "")
                    .replace(".xls", "")
            ) + "." + formatName(workSheetName);
            excelWorksheets.add(new ExcelWorksheet(workSheetName, tableName, columns, worksheet));
        }

        return excelWorksheets;
    }

    public void populate(ExcelWorkbook excelWorkbook) {
        System.out.println("Reading " + excelWorkbook.getWorkbookName());
        for (ExcelWorksheet excelWorksheet : excelWorkbook.getWorksheets()) {
            populate(excelWorksheet);
        }
    }

    public void populate(List<ExcelWorkbook> worbooks) {

        for (ExcelWorkbook worbook : worbooks) {
            populate(worbook);
        }
    }
}
