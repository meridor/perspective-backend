package org.meridor.perspective.sql.impl.task;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.storage.DataFetcher;
import org.meridor.perspective.sql.impl.table.Column;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MockDataFetcher implements DataFetcher {

    public static final String INSTANCES_TABLE = "instances";
    public static final String PROJECTS_TABLE = "projects";

    private Map<String, List<List<Object>>> dataMap = new LinkedHashMap<>();
    private Map<String, List<String>> columnsMap = new LinkedHashMap<>();

    @Override
    public DataContainer fetch(String tableName, String tableAlias, Set<Column> columns) {
        if (!INSTANCES_TABLE.equals(tableName) && !PROJECTS_TABLE.equals(tableName)) {
            throw new IllegalArgumentException(String.format("Table \"%s\" does not exist", tableName));
        }
        List<String> columnNames = columnsMap.getOrDefault(tableName, Collections.emptyList());
        Map<String, List<String>> columnsMap = new HashMap<String, List<String>>(){
            {
                put(tableAlias, columnNames);
            }
        };
        DataContainer dataContainer = new DataContainer(columnsMap);
        if (dataMap.containsKey(tableName)) {
            dataMap.get(tableName).forEach(dataContainer::addRow);
        }
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
