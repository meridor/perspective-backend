package org.meridor.perspective.sql;

import org.meridor.perspective.sql.Data;
import org.meridor.perspective.sql.Row;

import java.util.List;

public final class DataUtils {

    public static List<String> getColumnsNames(Data data) {
        return data.getColumnNames().getColumnNames();
    }
    
    public static int getDataSize(Data data) {
        return data.getRows().getRows().size();
    }
    
    public static Row getRow(Data data, int rowIndex) {
        List<Row> rows = data.getRows().getRows();
        final int NUM_ROWS = rows.size();
        if (rowIndex < 0 || rowIndex >= NUM_ROWS) {
            throw new IllegalArgumentException(String.format(
                    "Row index should be between 0 and %d but %d was specified",
                    NUM_ROWS,
                    rowIndex
            ));
        }
        return rows.get(rowIndex);
    }
    
    public static int getColumnIndex(Data data, String columnName) {
        List<String> columnNames = getColumnsNames(data);
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

    public static void put(Data data, Row row, int columnIndex, Object value) {
        checkColumnIndex(data, columnIndex);
        row.getValues().add(columnIndex, value);
    }

    public static void put(Data data, Row row, String columnName, Object value) {
        int columnIndex = getColumnIndex(data, columnName);
        checkColumnIndex(data, columnIndex);
        row.getValues().add(columnIndex, value);
    }
    
    private static void checkColumnIndex(Data data, int columnIndex) {
        final int NUM_COLS = getColumnsNames(data).size();
        if (columnIndex < 0 && columnIndex >= NUM_COLS) {
            throw new IllegalArgumentException(String.format(
                    "Column index should be between 0 and %d but %d was specified",
                    NUM_COLS,
                    columnIndex
            ));
        }
    }
    
    private DataUtils() {
    }
}
