package org.meridor.perspective.sql.impl.table;

import org.meridor.perspective.sql.impl.index.impl.IndexSignature;
import org.meridor.perspective.sql.impl.storage.IndexStorage;
import org.meridor.perspective.sql.impl.table.annotation.ForeignKey;
import org.meridor.perspective.sql.impl.table.annotation.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Component
@Lazy
public class TablesAwareImpl implements TablesAware {
    
    private static final Logger LOG = LoggerFactory.getLogger(TablesAwareImpl.class);
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired(required = false)
    private IndexStorage indexStorage;

    private Map<String, Set<Column>> tables = new HashMap<>();
    
    @PostConstruct
    public void init() {
        forEachTable(getFieldsConsumer());
        forEachTable(getIndexesConsumer());
    }


    private void forEachTable(Consumer<Table> consumer) {
        applicationContext.getBeansOfType(Table.class).values()
                .forEach(consumer);
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
            tables.putIfAbsent(tableName, new LinkedHashSet<>());
            try {
                Object defaultValue = field.get(table);
                tables.get(tableName).add(new Column(field.getName(), field.getType(), defaultValue, table));
            } catch (IllegalAccessException e) {
                tables.get(tableName).add(new Column(field.getName(), field.getType(), null, table));
            }
        };
    }
    
    private Consumer<Table> getIndexesConsumer() {
        return table -> {
            if (indexStorage != null) {
                String tableName = table.getName();
                Class<? extends Table> tableClass = table.getClass();
                Index[] indexAnnotations = tableClass.getAnnotationsByType(Index.class);
                ForeignKey[] foreignKeyAnnotations = tableClass.getAnnotationsByType(ForeignKey.class);
                if (indexAnnotations.length > 0) {
                    Arrays.stream(indexAnnotations).forEach(ia -> processIndexAnnotation(tableName, ia));
                }
                if (foreignKeyAnnotations.length > 0) {
                    Arrays.stream(foreignKeyAnnotations).forEach(fka -> processForeignKeyAnnotation(tableName, fka));
                }
            }
        };
    }

    private void processIndexAnnotation(String tableName, Index indexAnnotation) {
        String[] columnNames = indexAnnotation.columnNames();
        int keyLength = indexAnnotation.length();
        addIndexIfValid(tableName, new LinkedHashSet<>(Arrays.asList(columnNames)),  keyLength);
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
        
        addIndexIfValid(tableName, new LinkedHashSet<>(Arrays.asList(columnNames)), keyLength);
        addIndexIfValid(foreignTableName, new LinkedHashSet<>(Arrays.asList(foreignTableColumnNames)), keyLength);
    }
    
    private void addIndexIfValid(String tableName, Set<String> columnNames, int keyLength) {
        IndexSignature indexSignature = new IndexSignature(tableName, columnNames);
        Map<String, Set<String>> indexColumns = indexSignature.getDesiredColumns();
        if (!isTablePresent(tableName)) {
            LOG.error("Not creating index {}: table {} does not exist", indexColumns, tableName);
            return;
        }
        Optional<String> invalidColumnCandidate = areColumnsPresent(tableName, columnNames);
        if (invalidColumnCandidate.isPresent()) {
            LOG.error("Not creating index {}: table {} column {} does not exist", indexColumns, tableName, invalidColumnCandidate.get());
            return;
        }
        LOG.info("Creating index {}", indexColumns);
        indexStorage.create(indexSignature, keyLength);
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
    public Set<Column> getColumns(String tableName) {
        return tables.containsKey(tableName) ? 
                tables.get(tableName):
                Collections.emptySet();
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
    public Set<String> getTables() {
        return tables.keySet();
    }

}
