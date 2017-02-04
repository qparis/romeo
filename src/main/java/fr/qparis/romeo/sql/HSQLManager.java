package fr.qparis.romeo.sql;

import fr.qparis.romeo.excel.ExcelContent;
import fr.qparis.romeo.excel.ExcelWorkbook;
import fr.qparis.romeo.excel.ExcelWorksheet;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.function.Consumer;

public class HSQLManager {
    private final Connection connection;

    public HSQLManager(Connection connection) {
        this.connection = connection;
    }

    private static void setParameter(PreparedStatement statement,
                                     int index,
                                     Object objectToInsert) throws SQLException {
        if (objectToInsert == null) {
            statement.setNull(index, index);
            return;
        }

        String toStringValue = HSQLTypeMatrix.toString(objectToInsert);
        statement.setString(index, toStringValue);
    }

    public SQLResult executeQuery(String query) throws SQLException {
        final Statement statement = connection.createStatement();
        try (ResultSet resultSet = statement.executeQuery(query)) {
            final ResultSetMetaData metaData = resultSet.getMetaData();
            final List<Map<String, Object>> results = new ArrayList<>();
            int numberOfColumns = metaData.getColumnCount();

            while (resultSet.next()) {
                final Map<String, Object> row = new HashMap<>(numberOfColumns);
                for (int i = 1; i <= numberOfColumns; ++i) {
                    Object object = resultSet.getObject(i);
                    if (object instanceof BigDecimal) {
                        row.put(metaData.getColumnName(i), ((BigDecimal) object).stripTrailingZeros());
                    } else {
                        row.put(metaData.getColumnName(i), object);
                    }
                }
                results.add(row);
            }

            final List<String> columnNames = new ArrayList<>();
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                columnNames.add(metaData.getColumnName(i + 1));
            }
            return new SQLResult(results, columnNames);
        }
    }

    public void initializeDatabase(ExcelWorkbook workbook, Consumer<Throwable> errorCallback) throws SQLException {
        for (ExcelWorksheet excelWorksheet : workbook.getWorksheets()) {
            initializeDatabase(excelWorksheet, errorCallback);
        }
    }

    public void initializeDatabase(List<ExcelWorkbook> workbooks, Consumer<Throwable> errorCallback) throws SQLException {
        for (ExcelWorkbook workbook : workbooks) {
            initializeDatabase(workbook, errorCallback);
        }
    }

    public void initializeDatabase(ExcelWorksheet excelWorksheet, Consumer<Throwable> errorCallback) throws SQLException {
        final String tableName = excelWorksheet.getTableName();
        if (excelWorksheet.getContent() != null && excelWorksheet.getContent().getHeader().size() > 0) {
            createSchema(tableName);
            createTable(
                    tableName,
                    excelWorksheet.getContent().getHeader(),
                    errorCallback
            );
            populateDatabase(
                    connection,
                    tableName,
                    excelWorksheet.getContent(),
                    errorCallback
            );
        }
    }

    public void clearDatabase() throws SQLException {
        final SQLResult schemasQuery = executeQuery("SELECT TABLE_SCHEMA\n" +
                "FROM INFORMATION_SCHEMA.TABLES \n" +
                "WHERE TABLE_SCHEMA != 'INFORMATION_SCHEMA' AND TABLE_SCHEMA != 'SYSTEM_LOBS'\n" +
                "GROUP BY TABLE_SCHEMA");

        for (Map<String, Object> queryResult : schemasQuery.getResults()) {
            final String schemaName = queryResult.get("TABLE_SCHEMA").toString();
            executeQuery("DROP SCHEMA " + schemaName + " CASCADE");
        }
    }

    private void createSchema(String tableName) throws SQLException {
        final String schemaName = tableName.split("\\.")[0];

        try {
            final String querySchema = "CREATE SCHEMA " + schemaName + " ";
            executeQuery(querySchema);
            System.out.println(querySchema);
        } catch (SQLSyntaxErrorException ignored) {
            System.out.println(ignored.getMessage());
        }
    }

    private void createTable(String tableName,
                             List<String> header,
                             Consumer<Throwable> errorCallback) throws SQLException {
        try {
            final StringBuilder createTableFieldString = new StringBuilder();

            for (String field : header) {
                createTableFieldString
                        .append(field)
                        .append(" LONGVARCHAR, ")
                        .append("\n");
            }
            executeQuery("DROP TABLE IF EXISTS " + tableName);

            final String query = "CREATE TABLE " + tableName + " (" + createTableFieldString.toString() + ")";
            System.out.println(query);
            executeQuery(query);
        } catch (Throwable e) {
            errorCallback.accept(e);
        }
    }

    private void populateDatabase(Connection connection,
                                  String tableName,
                                  ExcelContent excelContent,
                                  Consumer<Throwable> errorCallback) throws SQLException {
        final List<String> columns = excelContent.getHeader();
        final Map<String, HSQLTypeMatrix> detectedTypes = new HashMap<>();
        columns.forEach(columnName -> detectedTypes.put(columnName, HSQLTypeMatrix.NULL));

        try {
            final String insertQuery =
                    String.format(
                            "INSERT INTO %s VALUES (" + String.join(", ", Collections.nCopies(columns.size(), "?")) + ")",
                            tableName
                    );

            PreparedStatement statement = connection.prepareStatement(insertQuery);

            for (List<Object> objects : excelContent.getContent()) {
                int i = 1;
                for (Object object : objects) {
                    final String columnName = columns.get(i - 1);
                    detectedTypes.put(
                            columnName,
                            detectedTypes.get(columnName).reduceWith(HSQLTypeMatrix.guessFromSample(object))
                    );

                    setParameter(statement, i, object);
                    i += 1;
                }

                statement.executeUpdate();
            }

            connection.commit();

            alterColumnTypes(tableName, detectedTypes);
        } catch (Throwable e) {
            errorCallback.accept(e);
            e.printStackTrace();
        }
    }

    private void alterColumnTypes(String tableName, Map<String, HSQLTypeMatrix> detectedTypes) throws SQLException {
        for(String columnName: detectedTypes.keySet()) {
            final HSQLTypeMatrix detectedType = detectedTypes.get(columnName);
            if(detectedType == HSQLTypeMatrix.NUMBER) {
                String alterTableQuery = "ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " " + detectedType.getHSQLFieldName();
                System.out.println(alterTableQuery);
                executeQuery(alterTableQuery);
            } else if(detectedType == HSQLTypeMatrix.DATE) {
                executeQuery("ALTER TABLE " + tableName + " ADD COLUMN newDate TIMESTAMP");
                executeQuery("UPDATE " + tableName + " SET newDate = timestamp("+columnName+")");
                executeQuery("ALTER TABLE " + tableName + " DROP COLUMN " + columnName);
                executeQuery("ALTER TABLE " + tableName + " ALTER COLUMN newDate RENAME TO " + columnName);
            }
        }

    }


}
