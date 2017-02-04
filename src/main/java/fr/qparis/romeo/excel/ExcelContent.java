package fr.qparis.romeo.excel;

import java.util.List;
import java.util.Map;

public class ExcelContent {
    private final List<String> header;
    private final Iterable<List<Object>> content;

    public ExcelContent(List<String> header,
                        Iterable<List<Object>> content) {
        this.header = header;
        this.content = content;
    }

    public List<String> getHeader() {
        return header;
    }

    public Iterable<List<Object>> getContent() {
        return content;
    }
}
