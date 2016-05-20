package org.meridor.perspective.sql.impl.storage.impl;

import org.meridor.perspective.sql.impl.storage.TableFetcher;
import org.meridor.perspective.sql.impl.table.Column;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class TestTableFetcher implements TableFetcher {
    @Override
    public List<List<Object>> fetch(List<Column> columns) {
        return Collections.singletonList(Collections.singletonList("value"));
    }

    @Override
    public List<List<Object>> fetch(List<String> ids, List<Column> columns) {
        return fetch(columns);
    }

    @Override
    public String getTableName() {
        return "existing";
    }
}
