package org.meridor.perspective.sql.impl;

import org.meridor.perspective.sql.impl.parser.QueryParser;
import org.meridor.perspective.sql.impl.parser.QueryType;
import org.meridor.perspective.sql.impl.parser.SelectQueryAware;
import org.springframework.stereotype.Component;

import java.sql.SQLSyntaxErrorException;

@Component
public class MockQueryParser implements QueryParser {

    private QueryType queryType = QueryType.SELECT;
    private SelectQueryAware selectQueryAware;
    
    @Override
    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    @Override
    public SelectQueryAware getSelectQueryAware() {
        if (selectQueryAware == null) {
            throw new IllegalStateException("You should set SelectQueryAware before using it.");
        }
        return selectQueryAware;
    }

    public void setSelectQueryAware(SelectQueryAware selectQueryAware) {
        this.selectQueryAware = selectQueryAware;
    }

    @Override
    public void parse(String sql) throws SQLSyntaxErrorException {
        // Does nothing!
    }

}
