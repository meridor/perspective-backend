package org.meridor.perspective.sql.impl.task;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.storage.DataFetcher;
import org.meridor.perspective.sql.impl.table.Column;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MockDataFetcher implements DataFetcher {

    private final Map<String, List<List<Object>>> dataMap = new LinkedHashMap<>();
    private final Map<String, List<String>> columnsMap = new LinkedHashMap<>();

    @Override
    public Map<String, List<Object>> fetch(String tableName, Collection<Column> columns, Set<String> ids) {
        //By convention data for mock data fetcher ID column should be the first
        if (!dataMap.containsKey(tableName)) {
            throw new IllegalArgumentException(String.format("Table \"%s\" does not exist", tableName));
        }
        return dataMap.get(tableName).stream()
                        .filter(dr -> ids == null || ids.contains(MockDataFetcher.getId(dr)))
                        .collect(Collectors.toMap(
                                MockDataFetcher::getId,
                                Function.identity())
                        );
    }

    private static String getId(List<Object> dr) {
        return String.valueOf(dr.get(0));
    }
    
    @Override
    public DataContainer fetch(String tableName, String tableAlias, Collection<Column> columns) {
        List<String> columnNames = columnsMap.getOrDefault(tableName, Collections.emptyList());
        Map<String, List<String>> columnsMap = Collections.singletonMap(tableAlias, columnNames);
        DataContainer dataContainer = new DataContainer(columnsMap);
        fetch(tableName, columns, null).values()
                .forEach(dataContainer::addRow);
        return dataContainer;
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
