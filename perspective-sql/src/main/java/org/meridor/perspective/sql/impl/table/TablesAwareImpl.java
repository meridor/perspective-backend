package org.meridor.perspective.sql.impl.table;

import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.impl.HashTableIndex;
import org.meridor.perspective.sql.impl.index.impl.IndexSignature;
import org.meridor.perspective.sql.impl.storage.IndexStorage;
import org.meridor.perspective.sql.impl.table.annotation.ForeignKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Component
public class TablesAwareImpl implements TablesAware {
    
    private static final Logger LOG = LoggerFactory.getLogger(TablesAwareImpl.class);
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired(required = false)
    private IndexStorage indexStorage;
    
    private Map<String, List<Column>> tables = new HashMap<>();

    @PostConstruct
    public void init() {
        forEachTable(Arrays.asList(
                getFieldsConsumer(),
                getIndexesConsumer()
        ));
    }


    private void forEachTable(List<Consumer<Table>> consumers) {
        applicationContext.getBeansOfType(Table.class).values().stream()
                .forEach(t -> consumers.forEach(c -> c.accept(t)));
    }
    
    private static void forEachField(Table table, List<BiConsumer<Table, Field>> consumers) {
        Arrays.stream(table.getClass().getFields()) //Only public fields!
            .forEach(f -> {
                f.setAccessible(true);
                consumers.forEach(c -> c.accept(table, f));
            });
    }
    
    private Consumer<Table> getFieldsConsumer() {
        return t -> forEachField(
                t,
                Collections.singletonList(getFieldConsumer())
        );
    };
    
    private BiConsumer<Table, Field> getFieldConsumer() {
        return (table, field) -> {
            String tableName = table.getName();
            tables.putIfAbsent(tableName, new ArrayList<>());
            try {
                Object defaultValue = field.get(table);
                tables.get(tableName).add(new Column(field.getName(), field.getType(), defaultValue));
            } catch (IllegalAccessException e) {
                tables.get(tableName).add(new Column(field.getName(), field.getType(), null));
            }
        };
    }
    
    private Consumer<Table> getIndexesConsumer() {
        return table -> {
            if (indexStorage != null) {
                String tableName = table.getName();
                Class<? extends Table> tableClass = table.getClass();
                org.meridor.perspective.sql.impl.table.annotation.Index[] indexAnnotations = tableClass.getAnnotationsByType(org.meridor.perspective.sql.impl.table.annotation.Index.class);
                ForeignKey[] foreignKeyAnnotations = tableClass.getAnnotationsByType(ForeignKey.class);
                if (indexAnnotations.length > 0) {
                    Arrays.stream(indexAnnotations).forEach(ia -> processIndexAnnotation(tableName, ia));
                } else if (foreignKeyAnnotations.length > 0) {
                    Arrays.stream(foreignKeyAnnotations).forEach(fka -> processForeignKeyAnnotation(tableName, fka));
                }
            }
        };
    }

    private void processIndexAnnotation(String tableName, org.meridor.perspective.sql.impl.table.annotation.Index indexAnnotation) {
        String[] columnNames = indexAnnotation.columnNames();
        int keyLength = indexAnnotation.length();

        Map<String, Set<String>> indexColumns = new HashMap<>();
        indexColumns.put(tableName, new HashSet<>(Arrays.asList(columnNames)));
        addIndexIfValid(indexColumns, keyLength);
    }

    private void processForeignKeyAnnotation(String tableName, ForeignKey foreignKeyAnnotation) {
        String[] columnNames = foreignKeyAnnotation.columns();
        String foreignTableName = foreignKeyAnnotation.table();
        String[] foreignTableColumnNames = foreignKeyAnnotation.tableColumns();
        int keyLength = foreignKeyAnnotation.length();

        if (columnNames.length != foreignTableColumnNames.length) {
            LOG.error(
                    "Total number of columns in foreign key should be equal for each table, but \"{}\" table has {} columns and \"{}\" table has {}",
                    tableName,
                    columnNames.length,
                    foreignTableName,
                    foreignTableColumnNames.length
            );
            return;
        }
        
        Map<String, Set<String>> indexColumns = new HashMap<>();
        indexColumns.put(tableName, new HashSet<>(Arrays.asList(columnNames)));
        indexColumns.put(foreignTableName, new HashSet<>(Arrays.asList(foreignTableColumnNames)));
        addIndexIfValid(indexColumns, keyLength);
    }
    
    private void addIndexIfValid(Map<String, Set<String>> indexColumns, int keyLength) {
        for (String tableName : indexColumns.keySet()) {
            if (!isTablePresent(tableName)) {
                LOG.error("Not creating index {}: table {} does not exist", indexColumns, tableName);
                return;
            }
            Set<String> columnNames = indexColumns.get(tableName);
            Optional<String> invalidColumnCandidate = areColumnsPresent(tableName, columnNames);
            if (invalidColumnCandidate.isPresent()) {
                LOG.error("Not creating index {}: table {} column {} does not exist", indexColumns, tableName, invalidColumnCandidate.get());
                return;
            }
        }
        IndexSignature indexSignature = new IndexSignature(indexColumns);
        Index index = new HashTableIndex(keyLength);
        indexStorage.put(indexSignature, index);
    }
    
    private boolean isTablePresent(String tableName) {
        return getTables().contains(tableName);
    }
    
    private Optional<String> areColumnsPresent(String tableName, Set<String> columnNames) {
        for (String columnName : columnNames) {
            if (!getColumn(tableName, columnName).isPresent()) {
                return Optional.of(columnName);
            }
        }
        return Optional.empty();
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
    public Optional<Index> getIndex(Map<String, Set<String>> desiredColumns) {
        if (indexStorage == null) {
            return Optional.empty();
        }
        return indexStorage.get(new IndexSignature(desiredColumns));
    }

    @Override
    public Set<String> getTables() {
        return tables.keySet();
    }

}
