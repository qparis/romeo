package fr.qparis.romeo.sql;

import java.text.SimpleDateFormat;

public enum HSQLTypeMatrix {
    STRING,
    NUMBER,
    DATE,
    NULL;

    public String getHSQLFieldName() {
        switch (this) {
            case NUMBER:
                return "DECIMAL(200, 100)";
            case DATE:
                return "TIMESTAMP";
            case STRING:
            case NULL:
            default:
                return "LONGVARCHAR";
        }
    }

    public static HSQLTypeMatrix guessFromSample(Object object) {
        if (object == null) {
            return NULL;
        }
        switch (object.getClass().getCanonicalName()) {
            case "java.lang.Double":
            case "java.lang.Integer":
            case "java.lang.Long":
            case "java.math.BigDecimal":
                return NUMBER;
            case "java.util.Date":
                return DATE;
            default:
                return STRING;
        }
    }

    public static String toString(Object objectToInsert) {
        final HSQLTypeMatrix objectType = guessFromSample(objectToInsert);

        switch (objectType) {
            case NULL:
                return "";
            case STRING:
                return objectToInsert.toString();
            case DATE:
                return new SimpleDateFormat("yyyy-MM-dd").format(objectToInsert);
            default:
                return objectToInsert.toString();
        }
    }

    public static HSQLTypeMatrix reduce(HSQLTypeMatrix argument1, HSQLTypeMatrix argument2) {
        if(argument1 == STRING || argument2 == STRING) {
            return STRING;
        }

        if(argument1 == argument2) {
            return argument1;
        }

        if(argument1 == NULL) {
            return argument2;
        }

        if(argument2 == NULL) {
            return argument1;
        }

        return STRING;
    }

    public HSQLTypeMatrix reduceWith(HSQLTypeMatrix otherType) {
        return reduce(this, otherType);
    }
}
