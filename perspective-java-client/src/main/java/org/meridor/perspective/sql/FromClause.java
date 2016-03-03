package org.meridor.perspective.sql;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FromClause extends BaseQueryPart {

    private final Map<String, String> tables = new LinkedHashMap<>();
    private final QueryPart previousQueryPart;

    public FromClause(QueryPart previousQueryPart) {
        this.previousQueryPart = previousQueryPart;
        addToSql(" from");
    }
    
    public FromClause table(String name) {
        tables.put(name, null);
        return this;
    }

    public FromClause table(String name, String alias) {
        tables.put(name, alias);
        return this;
    }

    public FromClause tables(String...names) {
        if (names != null) {
            Arrays.stream(names).forEach(this::table);
        }
        return this;
    }
    
    public JoinClause innerJoin() {
        return new JoinClause(this, JoinClause.JoinType.INNER);
    }

    public WhereClause where() {
        return new WhereClause(this);
    }
    
    public OrderClause orderBy() {
        return new OrderClause(this);
    }
    
    public LimitClause limit(int limit) {
        return new LimitClause(this, limit);
    }
    
    public LimitClause limit(int limit, int offset) {
        return new LimitClause(this, limit, offset);
    }
    
    @Override
    public String getSql() {
        String tableNames = tables.keySet().stream()
                .map(n ->
                        (tables.get(n) != null) ?
                                String.format("%s as %s", n, tables.get(n)) :
                                n
                )
                .collect(Collectors.joining(", "));
        addToSql(" " + tableNames);
        return super.getSql();
    }

    @Override
    public Optional<QueryPart> getPreviousPart() {
        return Optional.of(previousQueryPart);
    }
}
