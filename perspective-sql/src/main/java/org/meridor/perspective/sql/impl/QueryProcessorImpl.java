package org.meridor.perspective.sql.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.meridor.perspective.sql.*;
import org.meridor.perspective.sql.impl.parser.QueryType;
import org.meridor.perspective.sql.impl.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.meridor.perspective.sql.DataContainer.empty;
import static org.meridor.perspective.sql.QueryStatus.*;
import static org.meridor.perspective.sql.impl.parser.QueryType.EXPLAIN;

@Component
public class QueryProcessorImpl implements QueryProcessor {
    
    private static final Logger LOG = LoggerFactory.getLogger(QueryProcessorImpl.class);
    
    @Autowired
    private ApplicationContext applicationContext;
    
    private LoadingCache<Query, List<QueryResult>> queryCache = CacheBuilder.newBuilder()
            .concurrencyLevel(4) //This can be converted to property
            .expireAfterAccess(10, TimeUnit.SECONDS)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build(
                    new CacheLoader<Query, List<QueryResult>>() {
                        public List<QueryResult> load(Query query) throws Exception {
                            return processQuery(query);
                        }
                    }
            );


    @Override
    public List<QueryResult> process(Query query) {
        try {
            return queryCache.get(query);
        } catch (ExecutionException e) {
            return Collections.singletonList(getQueryResult(EVALUATION_ERROR, 0, empty(), e.getMessage()));
        }
    }
    
    private List<QueryResult> processQuery(Query query) {
        try {
            List<String> sqlQueries = prepareSQL(query);
            List<QueryResult> queryResults = new ArrayList<>();
            for (String sqlQuery : sqlQueries) {
                try {
                    QueryPlan queryPlan = parseSQL(sqlQuery);
                    Queue<Task> tasks = queryPlan.getTasks();
                    QueryType queryType = queryPlan.getQueryType();
                    ExecutionResult executionResult = (queryType != EXPLAIN) ?
                            executeTasks(tasks.iterator(), null) :
                            createExplainExecutionResult(tasks);
                    queryResults.add(getQueryResult(SUCCESS, executionResult.getCount(), executionResult.getData(), ""));
                } catch (SQLSyntaxErrorException e) {
                    LOG.debug("Marking query as having syntax error", e);
                    queryResults.add(getQueryResult(SYNTAX_ERROR, 0, empty(), e.getMessage()));
                } catch (SQLException e) {
                    LOG.debug("Marking query as having evaluation error", e);
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
    
    private QueryPlan parseSQL(String sqlQuery) throws SQLException {
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
    
    private static ExecutionResult createExplainExecutionResult(Queue<Task> tasks) {
        ExecutionResult executionResult = new ExecutionResult();
        executionResult.setCount(tasks.size());
        DataContainer data = new DataContainer(Collections.singletonList("task"));
        tasks.stream().map(Object::toString)
                .forEach(s -> data.addRow(Collections.singletonList(s)));
        executionResult.setData(data);
        return executionResult;
    }
    
}
