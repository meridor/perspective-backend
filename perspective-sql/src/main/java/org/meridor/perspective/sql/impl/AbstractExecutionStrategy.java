package org.meridor.perspective.sql.impl;

public abstract class AbstractExecutionStrategy implements ExecutionStrategy {
    
    private final String sql;

    public AbstractExecutionStrategy(String sql) {
        this.sql = sql;
    }

    protected String getSql() {
        return sql;
    }
}
