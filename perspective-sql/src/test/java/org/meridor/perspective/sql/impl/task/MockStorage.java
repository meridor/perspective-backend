package org.meridor.perspective.sql.impl.task;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.impl.storage.Storage;
import org.meridor.perspective.sql.impl.table.Column;
import org.meridor.perspective.sql.impl.table.TableName;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MockStorage implements Storage {

    @Override
    public DataContainer fetch(TableName tableName, List<Column> columns) {
        List<String> columnNames = columns.stream()
                .map(Column::getName)
                .collect(Collectors.toList());
        return new DataContainer(columnNames){
            {
                addRow(createRow(columnNames, 1));
                addRow(createRow(columnNames, 2));
            }
        };
    }
    
    private List<Object> createRow(List<String> columnNames, int value) {
        List<Object> dataRow = new ArrayList<>();
        for (String columnName : columnNames) {
            dataRow.add(value);
        }
        return dataRow;
    }
    
}
