package org.meridor.perspective.sql.impl;

import org.meridor.perspective.sql.impl.task.Task;

import java.sql.SQLSyntaxErrorException;
import java.util.Queue;

public interface QueryPlanner {
    
    Queue<Task> plan(String sql) throws SQLSyntaxErrorException;
    
}
