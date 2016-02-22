package org.meridor.perspective.sql.impl;

import org.meridor.perspective.sql.*;
import org.meridor.perspective.sql.impl.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.*;

@Component
public class QueryProcessorImpl implements QueryProcessor {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Override
    public List<QueryResult> process(Query query) {
        try {
            List<String> sqlQueries = prepareSQL(query);
            List<QueryResult> queryResults = new ArrayList<>();
            for (String sqlQuery : sqlQueries) {
                try {
                    Queue<Task> tasks = parseSQL(sqlQuery);
                    try {
                        ExecutionResult executionResult = executeTasks(tasks.iterator(), null);
                        queryResults.add(getQueryResult(QueryStatus.SUCCESS, executionResult.getCount(), executionResult.getData(), ""));
                    } catch (SQLException e) {
                        queryResults.add(getQueryResult(QueryStatus.EVALUATION_ERROR, 0, DataContainer.empty(), e.getMessage()));
                    }
                } catch (SQLSyntaxErrorException e) {
                    queryResults.add(getQueryResult(QueryStatus.SYNTAX_ERROR, 0, DataContainer.empty(), e.getMessage()));
                }
            }
            return queryResults;
        } catch (SQLDataException e) {
            return Collections.singletonList(getQueryResult(QueryStatus.MISSING_PARAMETERS, 0, DataContainer.empty(), e.getMessage()));
        }
    }
    
    private List<String> prepareSQL(Query query) throws SQLDataException {
        final PlaceholderConfigurer placeholderConfigurer = new PlaceholderConfigurer(
                query.getSql(),
                query.getParameters()
        ); 
        return placeholderConfigurer.getQueries();
    }
    
    private Queue<Task> parseSQL(String sqlQuery) throws SQLSyntaxErrorException {
        QueryScheduler queryScheduler = applicationContext.getBean(QueryScheduler.class);
        return queryScheduler.schedule(sqlQuery);
    }
    
    private ExecutionResult executeTasks(Iterator<Task> tasks, ExecutionResult previousTaskResult) throws SQLException {
        if (!tasks.hasNext()) {
            return previousTaskResult;
        }
        Task currentTask = tasks.next();
        return executeTasks(tasks, currentTask.execute(previousTaskResult));
    }
    
    private static QueryResult getQueryResult(QueryStatus queryStatus, int count, DataContainer dataContainer, String message) {
        QueryResult queryResult = new QueryResult();
        queryResult.setStatus(queryStatus);
        queryResult.setCount(count);
        queryResult.setData(dataContainer.toData());
        queryResult.setMessage(message);
        return queryResult;
    }
    
}
