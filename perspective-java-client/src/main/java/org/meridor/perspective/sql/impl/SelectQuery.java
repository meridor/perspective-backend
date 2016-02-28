package org.meridor.perspective.sql.impl;

import java.util.*;
import java.util.stream.Collectors;

public class SelectQuery extends BaseQueryPart {

    private final Map<String, String> columns = new LinkedHashMap<>();

    public SelectQuery() {
        addToSql("select");
    }

    public SelectQuery all() {
        addToSql(" *");
        return this;
    }
    
    public SelectQuery column(String name) {
        columns.put(name, null);
        return this;
    }
    
    public SelectQuery column(String name, String alias) {
        columns.put(name, alias);
        return this;
    }
    
    public SelectQuery columns(String... names) {
        if (names != null) {
            Arrays.stream(names).forEach(this::column);
        }
        return this;
    }

    public FromClause from() {
        return new FromClause(this);
    }
    
    @Override
    public String getSql() {
        if (!columns.isEmpty()) {
            String columnNames = columns.keySet().stream()
                    .map(n ->
                            (columns.get(n) != null) ?
                                    String.format("%s as %s", n, columns.get(n)) :
                                    n
                    )
                    .collect(Collectors.joining(", "));
            addToSql(" " + columnNames);
        }
        return super.getSql();
    }

}
