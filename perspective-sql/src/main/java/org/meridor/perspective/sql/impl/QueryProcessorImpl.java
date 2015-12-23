package org.meridor.perspective.sql.impl;

import org.meridor.perspective.sql.*;
import org.meridor.perspective.sql.impl.task.Task;

import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.*;

public class QueryProcessorImpl implements QueryProcessor {
    
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
                        queryResults.add(getQueryResult(QueryStatus.EVALUATION_ERROR, 0, Collections.emptyList(), e.getMessage()));
                    }
                } catch (SQLSyntaxErrorException e) {
                    queryResults.add(getQueryResult(QueryStatus.SYNTAX_ERROR, 0, Collections.emptyList(), e.getMessage()));
                }
            }
            return queryResults;
        } catch (SQLDataException e) {
            return Collections.singletonList(getQueryResult(QueryStatus.MISSING_PARAMETERS, 0, Collections.emptyList(), e.getMessage()));
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
        QueryScheduler queryScheduler = new QuerySchedulerImpl(sqlQuery);
        return queryScheduler.schedule();
    }
    
    private ExecutionResult executeTasks(Iterator<Task> tasks, ExecutionResult previousTaskResult) throws SQLException {
        if (!tasks.hasNext()) {
            return previousTaskResult;
        }
        return executeTasks(tasks, tasks.next().execute(previousTaskResult));
    }
    
    private static QueryResult getQueryResult(QueryStatus queryStatus, int count, List<DataRow> data, String message) {
        QueryResult queryResult = new QueryResult();
        queryResult.setStatus(queryStatus);
        queryResult.setCount(count);
        queryResult.setData(data);
        queryResult.setMessage(message);
        return queryResult;
    }
    
}
