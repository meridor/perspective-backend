package org.meridor.perspective.sql.impl.storage.impl;

import org.meridor.perspective.sql.impl.storage.TableFetcher;
import org.meridor.perspective.sql.impl.table.Column;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TestTableFetcher implements TableFetcher {

    @Override
    public Map<String, List<Object>> fetch(Set<String> ids, Collection<Column> columns) {
        return Collections.singletonMap("id", Collections.singletonList("value"));
    }

    @Override
    public String getTableName() {
        return "existing";
    }
}
