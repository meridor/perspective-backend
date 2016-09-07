package org.meridor.perspective.sql;

import java.util.List;

public final class DataUtils {

    public static int getColumnIndex(Data data, String columnName) {
        List<String> columnNames = data.getColumnNames();
        int firstColumnIndex = columnNames.indexOf(columnName);
        boolean columnExists = (firstColumnIndex != -1);
        if (!columnExists) {
            throw new IllegalArgumentException(String.format("No such column: %s", columnName));
        }
        return firstColumnIndex;
    }
    
    public static Object get(Data data, Row row, int columnIndex) {
        checkColumnIndex(data, columnIndex);
        return row.getValues().get(columnIndex);
    }

    public static Object get(Data data, Row row, String columnName) {
        return get(data, row, getColumnIndex(data, columnName));
    }

    private static void checkColumnIndex(Data data, int columnIndex) {
        final int NUM_COLS = data.getColumnNames().size();
        if (columnIndex < 0 || columnIndex >= NUM_COLS) {
            throw new IllegalArgumentException(String.format(
                    "Column index should be between 0 and %d but %d was specified",
                    NUM_COLS,
                    columnIndex
            ));
        }
    }
    
}
