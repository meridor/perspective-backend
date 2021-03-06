package org.meridor.perspective.sql;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataContainer implements Comparable<DataContainer> {
    
    private static final String ANY = "any";
    
    private final Map<String, List<String>> columnsMap = new LinkedHashMap<>();
    
    private final List<DataRow> rows = new ArrayList<>();

    public static DataContainer empty(){
        return new DataContainer(Collections.emptyMap());
    }
    
    public DataContainer(Collection<String> columnNames) {
        this(Collections.singletonMap(
                ANY,
                new ArrayList<String>() {
                    {
                        if (columnNames != null) {
                            addAll(columnNames);
                        }
                    }
                }
        ));
    }
    
    public DataContainer(Map<String, List<String>> columnsMap) {
        updateColumnsMap(columnsMap);
    }
    
    public DataContainer(DataContainer another, Function<List<DataRow>, List<DataRow>> processor) {
        this(another.getColumnsMap());
        processor.apply(another.getRows()).forEach(this::addRow);
    }
    
    private void updateColumnsMap(Map<String, List<String>> columnsMap) {
        if (columnsMap != null) {
            this.columnsMap.putAll(columnsMap);
        }
    }
    
    public void addRow(List<Object> values) {
        rows.add(createRow(values));
    }
    
    public void addRow(DataRow dataRow) {
        rows.add(createRow(dataRow.getValues()));
    }

    private DataRow createRow(List<Object> values) {
        return new DataRow(this, values);
    }

    public Map<String, List<String>> getColumnsMap() {
        return columnsMap;
    }

    public List<String> getColumnNames() {
        return columnsMap.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
    
    public List<DataRow> getRows() {
        return rows;
    }
    
    public Data toData() {
        Data data = new Data();
        data.getColumnNames().addAll(getColumnNames());
        getRows().forEach(r -> {
            Row row = new Row();
            row.getValues().addAll(r.getValues());
            data.getRows().add(row);
        });
        return data;
    }

    @Override
    public int compareTo(DataContainer another) {
        if (another.getColumnsMap().equals(getColumnsMap())) {
            return 0;
        }
        return -1;
    }
}
