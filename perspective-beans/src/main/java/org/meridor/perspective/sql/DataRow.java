package org.meridor.perspective.sql;

import java.util.List;
import java.util.Optional;

public class DataRow {
    
    private final List<Object> values;
    
    private final DataContainer dataContainer;
    
    public DataRow(DataContainer dataContainer, List<Object> values) {
        if (values == null) {
            throw new IllegalArgumentException("Values can't be null");
        }
        this.dataContainer = dataContainer;
        this.values = values;
    }
    
    public Object get(int columnIndex) {
        if (!isColumnIndexValid(columnIndex)) {
            throw new IllegalArgumentException(String.format("Index should be one of 0..%d", values.size() - 1));
        }
        return (columnIndex <= values.size() - 1) ? values.get(columnIndex) : null;
    }
    
    private boolean isColumnIndexValid(int columnIndex) {
        return columnIndex >= 0 && columnIndex < dataContainer.getColumnNames().size();
    }
    
    public Object get(String columnName, String tableAlias) {
        Optional<Integer> columnIndex = getColumnIndex(columnName, tableAlias);
        if (!columnIndex.isPresent()) {
            throw new IllegalArgumentException(String.format("No such column: %s for table: %s", columnName, tableAlias));
        }
        return get(columnIndex.get());
    }
    
    public Object get(String columnName) {
        Optional<Integer> columnIndex = getColumnIndex(columnName);
        if (!columnIndex.isPresent()) {
            throw new IllegalArgumentException(String.format("No such column: %s", columnName));
        }
        return get(columnIndex.get());
    }
    
    public void put(int columnIndex, Object value) {
        if (!isColumnIndexValid(columnIndex)) {
            throw new IllegalArgumentException(String.format("Index should be one of 0..%d", values.size() - 1));
        }
        this.values.set(columnIndex, value);
    }

    public void put(String columnName, String tableAlias, Object value) {
        Optional<Integer> columnIndex = getColumnIndex(columnName, tableAlias);
        if (!columnIndex.isPresent()) {
            throw new IllegalArgumentException(String.format("Column %s does not exist for table %s", columnName, tableAlias));
        }
        put(columnIndex.get(), value);
    }
    
    public void put(String columnName, Object value) {
        Optional<Integer> columnIndex = getColumnIndex(columnName);
        if (!columnIndex.isPresent()) {
            throw new IllegalArgumentException(String.format("Column %s does not exist", columnName));
        }
        put(columnIndex.get(), value);
    }

    private Optional<Integer> getColumnIndex(String columnName) {
        return getColumnIndex(columnName, dataContainer.getColumnNames());
    }
    
    private static Optional<Integer> getColumnIndex(String columnName, List<String> columnNames) {
        int firstColumnIndex = columnNames.indexOf(columnName);
        int lastColumnIndex = columnNames.lastIndexOf(columnName);
        boolean columnExists = (firstColumnIndex != -1);
        if (columnExists && lastColumnIndex != firstColumnIndex) {
            throw new IllegalArgumentException(String.format("Ambiguous column name: %s", columnName));
        }
        return columnExists ? Optional.of(firstColumnIndex) : Optional.empty();
    }
    
    private Optional<Integer> getColumnIndex(String columnName, String tableName) {
        if (dataContainer.getColumnsMap().containsKey(tableName)) {
            return getColumnIndex(columnName, dataContainer.getColumnsMap().get(tableName));
        }
        return Optional.empty();
    }
    
    public List<Object> getValues() {
        return values;
    }
}
