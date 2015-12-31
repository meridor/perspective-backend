package org.meridor.perspective.sql.impl.task;

import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.impl.storage.Storage;
import org.meridor.perspective.sql.impl.table.TableName;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MockStorage implements Storage {

    @Override
    public List<DataRow> fetch(TableName tableName, List<String> columnNames) {
        return new ArrayList<DataRow>(){
            {
                add(createRow(columnNames, 1));
                add(createRow(columnNames, 2));
            }
        };
    }
    
    private DataRow createRow(List<String> columnNames, int value) {
        DataRow dataRow = new DataRow();
        for (String columnName : columnNames) {
            dataRow.put(columnName, value);
        }
        return dataRow;
    }
    
}
