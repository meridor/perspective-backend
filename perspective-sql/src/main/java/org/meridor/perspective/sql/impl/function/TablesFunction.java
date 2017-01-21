package org.meridor.perspective.sql.impl.function;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.table.TablesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class TablesFunction implements Function<DataContainer> {
    
    private static final String TABLE_NAME = "table_name";
    
    private final TablesAware tablesAware;

    @Autowired
    public TablesFunction(TablesAware tablesAware) {
        this.tablesAware = tablesAware;
    }
    
    @Override
    public Class<DataContainer> getReturnType() {
        return DataContainer.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.TABLES;
    }

    @Override
    public DataContainer apply(List<Object> objects) {
        DataContainer dataContainer = new DataContainer(Collections.singleton(TABLE_NAME));
        tablesAware.getTables()
                .stream()
                .sorted()
                .forEach(t -> dataContainer.addRow(Collections.singletonList(t)));
        return dataContainer;
    }

    @Override
    public String getDescription() {
        return "Returns the list of available tables.";
    }

}
