package org.meridor.perspective.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DataRow {
    
    private static final String COMPOUND_NAME_DELIMITER_REGEX = "\\."; 
    
    private final List<Object> values;
    
    private final DataContainer dataContainer;
    
    public DataRow(DataContainer dataContainer, List<Object> values) {
        if (values == null) {
            throw new IllegalArgumentException("Values can't be null");
        }
        this.dataContainer = dataContainer;
        this.values = new ArrayList<>(values);
    }
    
    public Object get(int columnIndex) {
        if (!isColumnIndexValid(columnIndex)) {
            throw new IllegalArgumentException(String.format("Index should be one of 0..%d", getColumnsCount() - 1));
        }
        return (columnIndex <= values.size() - 1) ? values.get(columnIndex) : null;
    }
    
    private boolean isColumnIndexValid(int columnIndex) {
        return columnIndex >= 0 && columnIndex <= getColumnsCount();
    }
    
    private int getColumnsCount() {
        return dataContainer.getColumnNames().size() - 1;
    }
    
    public Object get(String columnName, String tableAlias) {
        Optional<Integer> columnIndex = getColumnIndex(columnName, tableAlias);
        if (!columnIndex.isPresent()) {
            throw new IllegalArgumentException(String.format("Column %s does not exist for table %s", columnName, tableAlias));
        }
        return get(columnIndex.get());
    }
    
    public Object get(String columnName) {
        Optional<Integer> columnIndex = getColumnIndex(columnName);
        if (!columnIndex.isPresent()) {
            throw new IllegalArgumentException(String.format("Column %s does not exist", columnName));
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
        Optional<Integer> columnIndexCandidate = getColumnIndex(columnName, dataContainer.getColumnNames());
        if (!columnIndexCandidate.isPresent()) {
            return getColumnIndexFromCompoundName(columnName);
        }
        return columnIndexCandidate;
    }
    
    private Optional<Integer> getColumnIndexFromCompoundName(String compoundColumnName) {
        String[] compoundName = compoundColumnName.split(COMPOUND_NAME_DELIMITER_REGEX);
        if (compoundName.length == 2) {
            String tableAlias = compoundName[0];
            String columnName = compoundName[1];
            return getColumnIndex(columnName, tableAlias);
        }
        return Optional.empty();
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
    
    private Optional<Integer> getColumnIndex(String columnName, String tableAlias) {
        Map<String, List<String>> columnsMap = dataContainer.getColumnsMap();
        if (columnsMap.containsKey(tableAlias)) {
            Optional<Integer> columnIndex = getColumnIndex(columnName, dataContainer.getColumnsMap().get(tableAlias));
            if (columnIndex.isPresent()) {
                int initialOffset = 0;
                for (String currentTableAlias : columnsMap.keySet()) {
                    if (currentTableAlias.equals(tableAlias)) {
                        break;
                    }
                    initialOffset += columnsMap.get(currentTableAlias).size();
                }
                return Optional.of(initialOffset + columnIndex.get());
            }
        }
        return Optional.empty();
    }
    
    public List<Object> getValues() {
        return values;
    }
}
