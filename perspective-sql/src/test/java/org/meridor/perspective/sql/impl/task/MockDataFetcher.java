package org.meridor.perspective.sql.impl.task;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.storage.DataFetcher;
import org.meridor.perspective.sql.impl.table.Column;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MockDataFetcher implements DataFetcher {

    private final Map<String, List<List<Object>>> dataMap = new LinkedHashMap<>();
    private final Map<String, List<String>> columnsMap = new LinkedHashMap<>();

    @Override
    public DataContainer fetch(String tableName, String tableAlias, Set<String> ids, Collection<Column> columns) {
        //By convention data for mock data fetcher ID column should be the first
        if (!dataMap.containsKey(tableName)) {
            throw new IllegalArgumentException(String.format("Table \"%s\" does not exist", tableName));
        }
        List<String> columnNames = columnsMap.getOrDefault(tableName, Collections.emptyList());
        Map<String, List<String>> columnsMap = new HashMap<String, List<String>>(){
            {
                put(tableAlias, columnNames);
            }
        };
        DataContainer dataContainer = new DataContainer(columnsMap);
        dataMap.get(tableName).forEach(dr -> {
            String id = String.valueOf(dr.get(0));
            if (ids == null || ids.contains(id)){
                dataContainer.addRow(dr);
            }
        });
        return dataContainer;
    }

    @Override
    public DataContainer fetch(String tableName, String tableAlias, Collection<Column> columns) {
        return fetch(tableName, tableAlias, null, columns);
    }
    
    public void setTableData(String tableName, List<String> columns, List<List<Object>> data) {
        this.columnsMap.put(tableName, columns);
        this.dataMap.put(tableName, data);
    }
    
    public void addDataRow(String tableName, List<Object> row) {
        List<List<Object>> dataRows = new ArrayList<>(dataMap.getOrDefault(tableName, new ArrayList<>()));
        dataRows.add(new ArrayList<>(row));
        dataMap.put(tableName, dataRows);
    }
    
}
