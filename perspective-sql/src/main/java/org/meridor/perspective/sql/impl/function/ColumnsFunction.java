package org.meridor.perspective.sql.impl.function;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.expression.ExpressionUtils;
import org.meridor.perspective.sql.impl.storage.IndexStorage;
import org.meridor.perspective.sql.impl.table.TablesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.meridor.perspective.sql.impl.function.FunctionUtils.argsCount;
import static org.meridor.perspective.sql.impl.function.FunctionUtils.oneOf;

@Component
public class ColumnsFunction implements Function<DataContainer> {
    
    private static final String COLUMN_NAME = "column_name";
    private static final String TYPE = "type";
    private static final String DEFAULT_VALUE = "default_value";
    private static final String INDEXED = "indexed";

    private final TablesAware tablesAware;

    private final IndexStorage indexStorage;

    @Autowired
    public ColumnsFunction(TablesAware tablesAware, IndexStorage indexStorage) {
        this.tablesAware = tablesAware;
        this.indexStorage = indexStorage;
    }

    @Override
    public Set<String> validateInput(List<Object> args) {
        return oneOf(
                args,
                argsCount(1)
        );
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
        DataContainer dataContainer = new DataContainer(Arrays.asList(COLUMN_NAME, TYPE, DEFAULT_VALUE, INDEXED));
        tablesAware.getColumns(tableName).forEach(c -> {
            String columnName = c.getName();
            boolean isIndexed = indexStorage.getSignatures().stream()
                    .anyMatch(s -> 
                            s.getDesiredColumns().containsKey(tableName) &&
                            s.getDesiredColumns().get(tableName).contains(columnName)
                    );
            dataContainer.addRow(Arrays.asList(
                    columnName,
                    humanizeType(c.getType()),
                    String.valueOf(c.getDefaultValue()),
                    String.valueOf(isIndexed)
            ));
        });
        return dataContainer;
    }
    
    private String humanizeType(Class<?> columnType) {
        if (ExpressionUtils.isConstant(columnType)) {
            return columnType.getSimpleName().toLowerCase();
        }
        return columnType.getCanonicalName();
    }

    @Override
    public String getSignature() {
        return "COLUMNS(T)";
    }

    @Override
    public String getDescription() {
        return "Returns a list of columns for table T.";
    }


}
