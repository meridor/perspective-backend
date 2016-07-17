package org.meridor.perspective.sql.impl.index.impl;

import org.meridor.perspective.sql.impl.index.Index;
import org.meridor.perspective.sql.impl.index.Indexer;
import org.meridor.perspective.sql.impl.index.Key;
import org.meridor.perspective.sql.impl.index.Keys;
import org.meridor.perspective.sql.impl.storage.ObjectMapper;
import org.meridor.perspective.sql.impl.storage.ObjectMapperAware;
import org.meridor.perspective.sql.impl.table.Column;
import org.meridor.perspective.sql.impl.table.TablesAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class IndexerImpl implements Indexer {
    
    private static final Logger LOG = LoggerFactory.getLogger(IndexerImpl.class);
    
    @Autowired
    private TablesAware tablesAware;
    
    @Autowired
    private ObjectMapperAware objectMapperAware;

    @Override
    public void add(String tableName, Object bean) {
        LOG.trace("Adding {} to \"{}\" table indexes", bean, tableName);
        forEachIndex(tableName, bean, Index::put);
    }

    @Override
    public void delete(String tableName, Object bean) {
        LOG.trace("Deleting {} from \"{}\" table indexes", bean, tableName);
        forEachIndex(tableName, bean, Index::delete);
    }
    
    private void forEachIndex(String tableName, Object bean, Action action) {
        if (bean == null) {
            return;
        }
        Collection<Column> columns = tablesAware.getColumns(tableName);
        columns.forEach(c -> {
            Set<IndexSignature> indexes = c.getIndexes();
            indexes.forEach(is -> {
                
                @SuppressWarnings("unchecked")
                ObjectMapper<Object> objectMapper = (ObjectMapper<Object>) objectMapperAware.get(bean.getClass());
                Map<String, Object> columnsMap = objectMapper.map(bean);
                
                Optional<Index> indexCandidate = tablesAware.getIndex(is);
                if (indexCandidate.isPresent()) {
                    Map<String, Set<String>> desiredColumns = is.getDesiredColumns();
                    Index index = indexCandidate.get();
                    int keyLength = index.getKeyLength();
                    String id = objectMapper.getId(bean);
                    Object[] columnValues = columnsToValues(desiredColumns.get(tableName), columnsMap);
                    Key key = Keys.key(keyLength, columnValues);
                    action.act(index, key, id);
                }
            });
        });
    }
    
    private static Object[] columnsToValues(Set<String> columnNames, Map<String, Object> columnsMap) {
        return columnNames.stream()
                .map(columnsMap::get)
                .collect(Collectors.toList())
                .toArray(new Object[columnNames.size()]);
    }

    private interface Action {
        void act(Index index, Key key, String id);
    }
}
