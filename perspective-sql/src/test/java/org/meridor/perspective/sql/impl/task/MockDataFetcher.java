package org.meridor.perspective.sql.impl.task;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.storage.DataFetcher;
import org.meridor.perspective.sql.impl.table.Column;
import org.meridor.perspective.sql.impl.table.TableName;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MockDataFetcher implements DataFetcher {

    private Map<TableName, List<List<Object>>> dataMap = new LinkedHashMap<>();
    private Map<TableName, List<String>> columnsMap = new LinkedHashMap<>();
    
    @Override
    public DataContainer fetch(TableName tableName, String tableAlias, List<Column> columns) {
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
    
    public void setTableData(TableName tableName, List<String> columns, List<List<Object>> data) {
        this.columnsMap.put(tableName, columns);
        this.dataMap.put(tableName, data);
    }
    
    public void addDataRow(TableName tableName, List<Object> row) {
        List<List<Object>> dataRows = new ArrayList<>(dataMap.getOrDefault(tableName, new ArrayList<>()));
        dataRows.add(new ArrayList<>(row));
        dataMap.put(tableName, dataRows);
    }
    
}
