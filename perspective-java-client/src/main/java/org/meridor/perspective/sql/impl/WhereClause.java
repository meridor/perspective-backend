package org.meridor.perspective.sql.impl;

import java.util.Optional;

public class WhereClause extends BaseQueryPart {
    
    private final QueryPart previousQueryPart;

    public WhereClause(QueryPart previousQueryPart) {
        this.previousQueryPart = previousQueryPart;
        addToSql(" where");
    }

    public WhereClause equal(String columnName, String value) {
        addToSql(String.format(" %s = :%s:", columnName, columnName));
        addParameter(columnName, value);
        return this;
    } 
    
    public WhereClause and() {
        addToSql(" and");
        return this;
    }
    
    public WhereClause or() {
        addToSql(" or");
        return this;
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
    public Optional<QueryPart> getPreviousPart() {
        return Optional.of(previousQueryPart);
    }
}
