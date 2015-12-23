package org.meridor.perspective.sql.impl.task;

import org.meridor.perspective.sql.ExecutionResult;

import java.sql.SQLException;

public interface Task {
    
    ExecutionResult execute(ExecutionResult previousTaskResult) throws SQLException;
    
}
