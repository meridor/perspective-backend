package org.meridor.perspective.sql.impl.storage.impl;

import org.meridor.perspective.sql.impl.storage.ObjectMapper;
import org.meridor.perspective.sql.impl.storage.TableFetcher;
import org.meridor.perspective.sql.impl.table.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public abstract class BaseTableFetcher<T> implements TableFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(TableFetcher.class);

    @Autowired
    private ApplicationContext applicationContext;

    private ObjectMapper<T> objectMapper;

    protected abstract Class<T> getBeanClass();

    protected abstract Collection<T> getRawData();

    protected Map<String, String> getColumnRemappingRules() {
        return Collections.emptyMap();
    }

    @PostConstruct
    public void init() {
        Class<T> beanClass = getBeanClass();
        for (ObjectMapper objectMapper : applicationContext.getBeansOfType(ObjectMapper.class).values()) {
            if (objectMapper.getInputClass().equals(beanClass)) {
                @SuppressWarnings("unchecked")
                ObjectMapper<T> om = (ObjectMapper<T>) objectMapper;
                this.objectMapper = om;
                return;
            }
        }
        throw new IllegalStateException(String.format("Object mapper for bean class \"%s\" not found", getBeanClass().getCanonicalName()));
    }

    @Override
    public List<List<Object>> fetch(Set<Column> columns) {
        return prepareData(columns);
    }

    @Override
    public abstract String getTableName();

    private List<List<Object>> prepareData(Set<Column> columns) {
        String tableName = getTableName();
        try {
            Set<String> availableColumnNames = remapColumnNames(objectMapper.getAvailableColumnNames());
            columns.forEach(c -> {
                String columnName = c.getName();
                if (!availableColumnNames.contains(columnName)) {
                    throw new IllegalArgumentException(String.format("Table \"%s\" does not contain column \"%s\"", tableName, columnName));
                }
            });
            Collection<T> rawEntities = getRawData();
            return rawEntities.stream()
                    .map(re -> {
                        Map<String, Object> rowAsMap = remapColumnNames(objectMapper.map(re));
                        return columns.stream()
                                .map(c -> {
                                    Object columnValue = rowAsMap.get(c.getName());
                                    return (columnValue == null && c.getDefaultValue() != null) ?
                                            c.getDefaultValue() :
                                            columnValue;
                                })
                                .collect(Collectors.toList());
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error(String.format("Failed to fetch \"%s\" table contents", tableName), e);
            return Collections.emptyList();
        }
    }

    private Map<String, Object> remapColumnNames(Map<String, Object> input) {
        Map<String, String> columnRemappingRules = getColumnRemappingRules();
        if (columnRemappingRules.isEmpty()) {
            return input;
        }
        Map<String, Object> output = new HashMap<>();
        input.keySet().forEach(k -> {
            String newKey = getRemappingFunction(columnRemappingRules).apply(k);
            output.put(newKey, input.get(k));
        });
        return output;
    }

    private Set<String> remapColumnNames(Set<String> input) {
        Map<String, String> columnRemappingRules = getColumnRemappingRules();
        return input.stream()
                .map(getRemappingFunction(columnRemappingRules))
                .collect(Collectors.toSet());
    }

    private Function<String, String> getRemappingFunction(Map<String, String> columnRemappingRules) {
        return k -> columnRemappingRules.containsKey(k) ? columnRemappingRules.get(k) : k;
    }

}
