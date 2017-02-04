package fr.qparis.romeo.sql;

import fr.qparis.romeo.excel.ExcelColumn;
import fr.qparis.romeo.excel.ExcelContent;
import fr.qparis.romeo.excel.ExcelWorkbook;
import fr.qparis.romeo.excel.ExcelWorksheet;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class HSQLManagerTest {
    private final Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb;sql.syntax_pgs=true", "SA", "");
    private final HSQLManager hsqlManager = new HSQLManager(connection);

    public HSQLManagerTest() throws SQLException {
    }

    @Test
    public void testInitializeDatabase_onlyStrings() throws SQLException {
        ExcelWorkbook workbook = initializeWorkbookWith(
                Arrays.asList("Col1", "Col2"),
                Arrays.asList(
                        Arrays.asList("A", "B"),
                        Arrays.asList("C", "D")
                )
        );

        hsqlManager.initializeDatabase(
                workbook, throwable -> {
                    throw new IllegalStateException(throwable);
                }
        );

        assertEquals(2L,
                hsqlManager.executeQuery("SELECT count(*) FROM MyWorkbook.MyWorksheet").getResults().get(0).get("C1"));
    }

    @Test
    public void testInitializeDatabase_onlyNumbers() throws SQLException {
        ExcelWorkbook workbook = initializeWorkbookWith(
                Arrays.asList("Col1", "Col2"),
                Arrays.asList(
                        Arrays.asList(1, 2),
                        Arrays.asList(3, 4)
                )
        );

        hsqlManager.initializeDatabase(
                workbook, throwable -> {
                    throw new IllegalStateException(throwable);
                }
        );

        assertEquals(2L,
                hsqlManager.executeQuery("SELECT count(*) FROM MyWorkbook.MyWorksheet").getResults().get(0).get("C1"));
    }

    @Test
    public void testInitializeDatabase_onlyNumbers_whereEqualStatement() throws SQLException {
        ExcelWorkbook workbook = initializeWorkbookWith(
                Arrays.asList("Col1", "Col2"),
                Arrays.asList(
                        Arrays.asList(1, 2),
                        Arrays.asList(1, 4),
                        Arrays.asList(1, 5),
                        Arrays.asList(2, 4)
                )
        );

        hsqlManager.initializeDatabase(
                workbook, throwable -> {
                    throw new IllegalStateException(throwable);
                }
        );

        assertEquals(3L,
                hsqlManager.executeQuery("SELECT count(*) FROM MyWorkbook.MyWorksheet WHERE Col1 = 1").getResults().get(0).get("C1"));
    }

    @Test
    public void testInitializeDatabase_onlyNumbers_whereComparisonStatement() throws SQLException {
        ExcelWorkbook workbook = initializeWorkbookWith(
                Arrays.asList("Col1", "Col2"),
                Arrays.asList(
                        Arrays.asList(1, 2),
                        Arrays.asList(2, 4),
                        Arrays.asList(3, 5),
                        Arrays.asList(4, 4),
                        Arrays.asList(1, 4),
                        Arrays.asList(2, 4),
                        Arrays.asList(-1, 4)
                )
        );

        hsqlManager.initializeDatabase(
                workbook, throwable -> {
                    throw new IllegalStateException(throwable);
                }
        );

        assertEquals(5L,
                hsqlManager.executeQuery("SELECT count(*) FROM MyWorkbook.MyWorksheet WHERE Col1 <= 2").getResults().get(0).get("C1"));
    }

    @Test
    public void testInitializeDatabase_onlyNumbers_largeDataSet() throws SQLException {
        final List<Object> largeListOrder = new ArrayList<>();
        final List<List<Object>> largeListReverseOrder = new ArrayList<>();

        for (int i = 0; i < 100_000; i++) {
            largeListOrder.add(new BigDecimal(i));
            largeListReverseOrder.add(Collections.singletonList(i));
        }
        ExcelWorkbook workbook = initializeWorkbookWith(
                Collections.singletonList("Col1"),
                largeListReverseOrder
        );

        hsqlManager.initializeDatabase(
                workbook, throwable -> {
                    throw new IllegalStateException(throwable);
                }
        );

        final List<Object> col1 = collectColumn(hsqlManager.executeQuery("SELECT * FROM MyWorkbook.MyWorksheet ORDER BY Col1 ASC")
                .getResults(), "COL1");

        assertListEquals(largeListOrder, col1);
    }

    @Test
    public void testInitializeDatabase_onlyDate_largeDataSet() throws SQLException {
        final List<List<Object>> largeListReverseOrder = new ArrayList<>();

        for (int i = 0; i < 100_000; i++) {
            largeListReverseOrder.add(Arrays.asList(new BigDecimal(i), new Date(91, 5, 26)));
        }

        ExcelWorkbook workbook = initializeWorkbookWith(
                Arrays.asList("Col1", "Col2"),
                largeListReverseOrder
        );

        hsqlManager.initializeDatabase(
                workbook, throwable -> {
                    throw new IllegalStateException(throwable);
                }
        );

        final List<Object> years = collectColumn(hsqlManager.executeQuery("SELECT COL1, COL2, YEAR(COL2) as YEARS FROM MyWorkbook.MyWorksheet ORDER BY YEAR(COL2) DESC")
                .getResults(), "YEARS");

        assertEquals(1991, years.get(0));
    }


    @Test
    public void testInitializeDatabase_onlyNumbers_whereIntegerComparisonStatement() throws SQLException {
        ExcelWorkbook workbook = initializeWorkbookWith(
                Arrays.asList("Col1", "Col2"),
                Arrays.asList(
                        Arrays.asList(1, 2),
                        Arrays.asList(10, 4),
                        Arrays.asList(11, 5),
                        Arrays.asList(2, 4),
                        Arrays.asList(3, 4),
                        Arrays.asList(4, 4),
                        Arrays.asList(5, 4)
                )
        );

        hsqlManager.initializeDatabase(
                workbook, throwable -> {
                    throw new IllegalStateException(throwable);
                }
        );

        assertEquals(2L,
                hsqlManager.executeQuery("SELECT count(*) FROM MyWorkbook.MyWorksheet WHERE Col1 <= 2").getResults().get(0).get("C1"));
    }

    @Test
    public void testInitializeDatabase_onlyNumbers_orberByCorrect() throws SQLException {
        ExcelWorkbook workbook = initializeWorkbookWith(
                Arrays.asList("Col1", "Col2"),
                Arrays.asList(
                        Arrays.asList(1, 2),
                        Arrays.asList(10, 4),
                        Arrays.asList(11, 5),
                        Arrays.asList(2, 4),
                        Arrays.asList(3, 4),
                        Arrays.asList(4, 4),
                        Arrays.asList(5, 4)
                )
        );

        hsqlManager.initializeDatabase(
                workbook, throwable -> {
                    throw new IllegalStateException(throwable);
                }
        );

        final List<Object> col1 = collectColumn(hsqlManager.executeQuery("SELECT * FROM MyWorkbook.MyWorksheet ORDER BY Col1 ASC")
                .getResults(), "COL1");
        assertListEquals(Stream.of(1, 2, 3, 4, 5, 10, 11).map(BigDecimal::new).collect(Collectors.toList()), col1);
    }

    @Test
    public void testInitializeDatabase_mixOfNumberAndStrings_orberByAlphaNumerical() throws SQLException {
        ExcelWorkbook workbook = initializeWorkbookWith(
                Arrays.asList("Col1", "Col2"),
                Arrays.asList(
                        Arrays.asList(1, 2),
                        Arrays.asList(10, 4),
                        Arrays.asList(11, 5),
                        Arrays.asList(2, 4),
                        Arrays.asList(3, 4),
                        Arrays.asList(4, 4),
                        Arrays.asList(5, 4),
                        Arrays.asList("a", 10)
                )
        );

        hsqlManager.initializeDatabase(
                workbook, throwable -> {
                    throw new IllegalStateException(throwable);
                }
        );

        final List<Object> col1 = collectColumn(hsqlManager.executeQuery("SELECT * FROM MyWorkbook.MyWorksheet ORDER BY Col1 ASC")
                .getResults(), "COL1");
        assertListEquals(Stream.of("1", "10", "11", "2", "3", "4", "5", "a").collect(Collectors.toList()), col1);
    }


    @Test
    public void testInitializeDatabase_withOneDate_canExtractItsYear() throws SQLException {
        ExcelWorkbook workbook = initializeWorkbookWith(
                Arrays.asList("Col1"),
                Arrays.asList(
                        Arrays.asList(new Date(91, 5, 26))
                )
        );

        hsqlManager.initializeDatabase(
                workbook, throwable -> {
                    throw new IllegalStateException(throwable);
                }
        );

        final List<Object> years = collectColumn(hsqlManager.executeQuery("SELECT COL1, YEAR(COL1) as YEARS" +
                " FROM MyWorkbook.MyWorksheet ORDER BY Col1 ASC")
                .getResults(), "YEARS");
        assertEquals(1991, years.get(0));
    }


    private void assertListEquals(List<Object> l1, List<Object> l2) {
        assertEquals(l1.size(), l2.size());
        for (int i = 0; i < l1.size(); i++) {
            if(l1.get(i) instanceof BigDecimal && l2.get(i) instanceof BigDecimal) {
                assertEquals(((BigDecimal) l1.get(i)).toPlainString(), ((BigDecimal) l2.get(i)).toPlainString());
            } else {
                assertEquals(l1.get(i), l2.get(i));
            }
        }
    }

    private List<Object> collectColumn(List<Map<String, Object>> results, String columnName) {
        final List<Object> collected = new ArrayList<>();
        for (Map<String, Object> result : results) {
            collected.add(result.get(columnName));
        }

        return collected;
    }

    private ExcelWorkbook initializeWorkbookWith(List<String> columns, List<List<Object>> data) {
        ExcelWorkbook workbook = new ExcelWorkbook("MyWorkbook", null,
                Arrays.asList(
                        new ExcelWorksheet("MyWorksheet", "MyWorkbook.MyWorksheet",
                                columns.stream().map(ExcelColumn::new).collect(Collectors.toList()), null
                        )
                )
        );

        workbook.getWorksheets().get(0).setContent(
                new ExcelContent(
                        columns,
                        data
                )
        );

        return workbook;
    }
}