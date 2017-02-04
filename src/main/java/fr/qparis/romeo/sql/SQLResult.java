package fr.qparis.romeo.sql;

import java.util.List;
import java.util.Map;

public class SQLResult {
    private final List<Map<String, Object>> results;
    private final List<String> columns;

    public SQLResult(List<Map<String, Object>> results, List<String> columns) {
        this.results = results;
        this.columns = columns;
    }

    public List<Map<String, Object>> getResults() {
        return results;
    }

    public List<String> getColumns() {
        return columns;
    }
}
