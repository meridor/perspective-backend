package org.meridor.perspective.sql.impl;

import org.meridor.perspective.sql.impl.parser.QueryType;
import org.meridor.perspective.sql.impl.task.Task;

import java.util.Queue;

public interface QueryPlan {

    Queue<Task> getTasks();
    
    QueryType getQueryType();
    
}
