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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    public DataContainer fetch(String tableName, String tableAlias, Set<Column> columns) {
        LOG.trace("Fetching from {} as {} columns: {}", tableName, tableAlias, columnsToNames(columns).stream().collect(Collectors.joining(", ")));
        List<String> columnNames = columnsToNames(columns);
        Map<String, List<String>> columnsMap = new HashMap<String, List<String>>() {
            {
                put(tableAlias, columnNames);
            }
        };
        DataContainer dataContainer = new DataContainer(columnsMap);
        List<List<Object>> rows = fetchData(tableName, columns);
        rows.forEach(dataContainer::addRow);
        return dataContainer;
    }

    private List<List<Object>> fetchData(String tableName, Set<Column> columns) {
        if (!tableFetchers.containsKey(tableName)) {
            throw new IllegalArgumentException(String.format("Fetching from table \"%s\" is not supported", tableName));
        }
        return tableFetchers.get(tableName).fetch(columns);
    }

}
