package org.meridor.perspective.sql.impl.table;

import org.meridor.perspective.sql.impl.index.Index;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TablesAwareImpl implements TablesAware {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    private Map<String, List<Column>> tables = new HashMap<>();
    
    @PostConstruct
    public void init() {
        applicationContext.getBeansOfType(Table.class).values().stream()
        .forEach(t -> {
            String tableName = t.getName();
            List<Column> columns = getTableColumns(t);
            tables.put(tableName, columns);
        });
    }
    
    private static List<Column> getTableColumns(Table table) {
        return Arrays.stream(table.getClass().getFields()) //Only public fields!
                .map(f -> {
                    try {
                        f.setAccessible(true);
                        Object defaultValue = f.get(table);
                        return new Column(f.getName(), f.getType(), defaultValue);
                    } catch (IllegalAccessException e) {
                        return new Column(f.getName(), f.getType(), null);
                    }
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Column> getColumns(String tableName) {
        return tables.containsKey(tableName) ? 
                tables.get(tableName):
                Collections.emptyList();
    }

    @Override
    public Optional<Column> getColumn(String tableName, String columnName) {
        if (columnName == null || !tables.containsKey(tableName)) {
            return Optional.empty();
        }
        return tables.get(tableName).stream()
                .filter(c -> c.getName().equals(columnName))
                .findFirst();
    }

    @Override
    public Optional<Index> getIndex(Map<String, Set<Column>> desiredColumns) {
        throw new UnsupportedOperationException("To be implemented!");
    }

    @Override
    public Set<String> getTables() {
        return tables.keySet();
    }
}
