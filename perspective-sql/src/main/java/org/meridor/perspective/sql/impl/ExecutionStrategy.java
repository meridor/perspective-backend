package org.meridor.perspective.sql.impl;

import org.meridor.perspective.sql.ExecutionResult;
import org.meridor.perspective.sql.QueryType;

import java.sql.SQLException;

public interface ExecutionStrategy {
    
    ExecutionResult execute() throws SQLException;
    
    QueryType getQueryType();
    
}
