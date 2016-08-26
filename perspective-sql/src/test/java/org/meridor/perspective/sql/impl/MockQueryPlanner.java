package org.meridor.perspective.sql.impl;

import org.meridor.perspective.sql.impl.parser.QueryType;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.LinkedList;

@Component
public class MockQueryPlanner implements QueryPlanner {
    
    private QueryPlan queryPlan = new QueryPlanImpl(new LinkedList<>(), QueryType.UNKNOWN);
    
    private SQLException exception;

    public void setQueryPlan(QueryPlan queryPlan) {
        this.queryPlan = queryPlan;
    }

    public void setException(SQLException exception) {
        this.exception = exception;
    }

    @Override
    public QueryPlan plan(String sql) throws SQLException {
        if (exception != null) {
            throw exception;
        }
        return queryPlan;
    }
}
