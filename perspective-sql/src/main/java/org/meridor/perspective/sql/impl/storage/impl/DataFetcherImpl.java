package org.meridor.perspective.sql.impl.storage.impl;

import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.storage.DataFetcher;
import org.meridor.perspective.sql.impl.storage.TableFetcher;
import org.meridor.perspective.sql.impl.table.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static org.meridor.perspective.sql.impl.expression.ExpressionUtils.columnsToMap;
import static org.meridor.perspective.sql.impl.expression.ExpressionUtils.columnsToNames;

@Component
public class DataFetcherImpl implements DataFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(DataFetcherImpl.class);

    private final Map<String, TableFetcher> tableFetchers = new HashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        applicationContext.getBeansOfType(TableFetcher.class).values().forEach(
                tf -> tableFetchers.put(tf.getTableName(), tf)
        );
    }

    @Override
    public Map<String, List<Object>> fetch(String tableName, Collection<Column> columns, Set<String> ids) {
        return fetchData(tableName, columns, ids);
    }

    @Override
    public DataContainer fetch(String tableName, String tableAlias, Collection<Column> columns) {
        LOG.trace("Fetching from {} as {} columns: {}", tableName, tableAlias, columnsToNames(columns).stream().collect(Collectors.joining(", ")));
        Map<String, List<String>> columnsMap = columnsToMap(tableAlias, columns);
        DataContainer dataContainer = new DataContainer(columnsMap);
        Collection<List<Object>> rows = fetchData(tableName, columns, null).values();
        rows.forEach(dataContainer::addRow);
        return dataContainer;
    }

    private Map<String, List<Object>> fetchData(String tableName, Collection<Column> columns, Set<String> ids) {
        if (!tableFetchers.containsKey(tableName)) {
            throw new IllegalArgumentException(String.format("Fetching from table \"%s\" is not supported", tableName));
        }
        return tableFetchers.get(tableName).fetch(ids, columns);
    }

}
