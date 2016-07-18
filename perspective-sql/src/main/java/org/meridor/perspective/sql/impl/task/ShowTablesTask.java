package org.meridor.perspective.sql.impl.task;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.impl.table.TablesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Collections;

@Component
@Lazy
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ShowTablesTask implements Task {
    
    private static final String TABLE_NAME = "table_name";
    
    @Autowired
    private TablesAware tablesAware;
    
    @Override
    public ExecutionResult execute(ExecutionResult previousTaskResult) throws SQLException {
        DataContainer newData = new DataContainer(Collections.singletonList(TABLE_NAME));
        tablesAware.getTables()
                .forEach(tn -> newData.addRow(Collections.singletonList(tn)));
        return new ExecutionResult(){
            {
                setData(newData);
                setCount(data.getRows().size());
            }
        };
    }

    @Override
    public String toString() {
        return "ShowTablesTask{}";
    }
}
