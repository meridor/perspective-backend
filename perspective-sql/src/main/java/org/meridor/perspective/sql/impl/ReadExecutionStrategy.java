package org.meridor.perspective.sql.impl;

import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.QueryType;

import java.sql.SQLException;

public class ReadExecutionStrategy extends AbstractExecutionStrategy {

    public ReadExecutionStrategy(String sql) {
        super(sql);
    }

    @Override
    public ExecutionResult execute() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public QueryType getQueryType() {
        return QueryType.READ;
    }
}
