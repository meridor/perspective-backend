package org.meridor.perspective.sql.impl;

import org.meridor.perspective.sql.impl.parser.QueryType;
import org.meridor.perspective.sql.impl.task.Task;

import java.util.Queue;

public class QueryPlanImpl implements QueryPlan {
    
    private final Queue<Task> tasksQueue;
    
    private final QueryType queryType;

    public QueryPlanImpl(Queue<Task> tasksQueue, QueryType queryType) {
        this.tasksQueue = tasksQueue;
        this.queryType = queryType;
    }

    @Override
    public Queue<Task> getTasks() {
        return tasksQueue;
    }

    @Override
    public QueryType getQueryType() {
        return queryType;
    }
}
