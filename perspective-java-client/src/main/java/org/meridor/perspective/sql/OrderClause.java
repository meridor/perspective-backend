package org.meridor.perspective.sql;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrderClause extends BaseQueryPart {

    private final Map<String, OrderDirection> orderExpressions = new LinkedHashMap<>();
    private final QueryPart previousQueryPart;

    public OrderClause(QueryPart previousQueryPart) {
        this.previousQueryPart = previousQueryPart;
        addToSql(" order by");
    }
    
    public OrderClause column(String name) {
        return column(name, OrderDirection.ASC);
    }

    public OrderClause column(String name, OrderDirection orderDirection) {
        orderExpressions.put(name, orderDirection);
        return this;
    }

    public OrderClause columns(String...names) {
        if (names != null) {
            Arrays.stream(names).forEach(this::column);
        }
        return this;
    }

    @Override
    public String getSql() {
        String exprs = orderExpressions.keySet().stream()
                .map(o ->
                        (orderExpressions.get(o) != null) ?
                                String.format("%s %s", o, orderExpressions.get(o).getValue()) :
                                o
                )
                .collect(Collectors.joining(", "));
        addToSql(" " + exprs);
        return super.getSql();
    }

    @Override
    public Optional<QueryPart> getPreviousPart() {
        return Optional.of(previousQueryPart);
    }
}
