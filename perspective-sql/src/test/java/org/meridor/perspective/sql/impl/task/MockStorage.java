package org.meridor.perspective.sql.impl.task;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.storage.Storage;
import org.meridor.perspective.sql.impl.table.Column;
import org.meridor.perspective.sql.impl.table.TableName;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MockStorage implements Storage {

    @Override
    public DataContainer fetch(TableName tableName, String tableAlias, List<Column> columns) {
        List<String> columnNames = columns.stream()
                .map(Column::getName)
                .collect(Collectors.toList());
        Map<String, List<String>> columnsMap = new HashMap<String, List<String>>(){
            {
                put(tableAlias, columnNames);
            }
        };
        int size = columnNames.size();
        return new DataContainer(columnsMap){
            {
                addRow(createRow(1, size));
                addRow(createRow(2, size));
            }
        };
    }
    
    //Creates rows like (1, 2, 3, 4), (2, 4, 6, 8) and so on
    private List<Object> createRow(int rowNumber, int size) {
        List<Object> dataRow = new ArrayList<>();
        for (int value = 1; value <= size; value++) {
            dataRow.add(rowNumber * value);
        }
        return dataRow;
    }
    
}
