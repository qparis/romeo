package fr.qparis.romeo.excel;

public abstract class QueryStringProvider {
    abstract public String getQueryHint();

    abstract public String getPreview();

    @Override
    public String toString() {
        return getPreview();
    }
}
