package org.meridor.perspective.sql.impl.function;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.expression.ExpressionUtils;
import org.meridor.perspective.sql.impl.table.TablesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class ColumnsFunction implements Function<DataContainer> {
    
    private static final String COLUMN_NAME = "column_name";
    private static final String TYPE = "type";
    private static final String DEFAULT_VALUE = "default_value";
    
    @Autowired
    private TablesAware tablesAware;


    @Override
    public Set<String> validateInput(List<Object> args) {
        if (args.size() != 1) {
            return Collections.singleton("Function accepts one argument only - name of the table to show columns for");
        }
        return Collections.emptySet();
    }

    @Override
    public Class<DataContainer> getReturnType() {
        return DataContainer.class;
    }

    @Override
    public FunctionName getName() {
        return FunctionName.COLUMNS;
    }

    @Override
    public DataContainer apply(List<Object> objects) {
        String tableName = String.valueOf(objects.get(0));
        if (!tablesAware.getTables().contains(tableName)) {
            throw new IllegalArgumentException(String.format("Table %s does not exist", tableName));
        }
        DataContainer dataContainer = new DataContainer(Arrays.asList(COLUMN_NAME, TYPE, DEFAULT_VALUE));
        tablesAware.getColumns(tableName).forEach(c -> dataContainer.addRow(Arrays.asList(
                c.getName(),
                humanizeType(c.getType()),
                String.valueOf(c.getDefaultValue())
        )));
        return dataContainer;
    }
    
    private String humanizeType(Class<?> columnType) {
        if (ExpressionUtils.isConstant(columnType)) {
            return columnType.getSimpleName().toLowerCase();
        }
        return columnType.getCanonicalName();
    }
    
}
