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

import static org.meridor.perspective.sql.DataContainer.empty;
import static org.meridor.perspective.sql.QueryStatus.*;

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
                    ExecutionResult executionResult = executeTasks(tasks.iterator(), null);
                    queryResults.add(getQueryResult(SUCCESS, executionResult.getCount(), executionResult.getData(), ""));
                } catch (SQLSyntaxErrorException e) {
                    queryResults.add(getQueryResult(SYNTAX_ERROR, 0, empty(), e.getMessage()));
                } catch (SQLException e) {
                    String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    queryResults.add(getQueryResult(EVALUATION_ERROR, 0, empty(), message));
                }
            }
            return queryResults;
        } catch (SQLDataException e) {
            return Collections.singletonList(getQueryResult(MISSING_PARAMETERS, 0, empty(), e.getMessage()));
        }
    }
    
    private List<String> prepareSQL(Query query) throws SQLDataException {
        final PlaceholderConfigurer placeholderConfigurer = new PlaceholderConfigurer(
                query.getSql(),
                query.getParameters()
        ); 
        return placeholderConfigurer.getQueries();
    }
    
    private Queue<Task> parseSQL(String sqlQuery) throws SQLException {
        QueryPlanner queryPlanner = applicationContext.getBean(QueryPlanner.class);
        return queryPlanner.plan(sqlQuery);
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
