package org.meridor.perspective.sql.impl;

import org.meridor.perspective.sql.*;

import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.List;

public class QueryProcessorImpl implements QueryProcessor {
    
    @Override
    public QueryResult process(Query query) {
        QueryType queryType = QueryType.UNKNOWN;
        try {
            List<String> queries = prepareSQL(query);
            ExecutionStrategy executionStrategy = parseSQL(queries);
            queryType = executionStrategy.getQueryType();
            ExecutionResult executionResult = executionStrategy.execute();
            return getQueryResult(queryType, QueryStatus.SUCCESS, executionResult.getCount(), executionResult.getData(), "");
        } catch (SQLDataException e) {
            return getQueryResult(queryType, QueryStatus.MISSING_PARAMETERS, 0, new DataRowMap(), e.getMessage());
        } catch (SQLSyntaxErrorException e) {
            return getQueryResult(queryType, QueryStatus.SYNTAX_ERROR, 0, new DataRowMap(), e.getMessage());
        } catch (SQLException e) {
            return getQueryResult(queryType, QueryStatus.SYNTAX_ERROR, 0, new DataRowMap(), e.getMessage());
        }
    }
    
    private List<String> prepareSQL(Query query) throws SQLDataException {
        final PlaceholderConfigurer placeholderConfigurer = new PlaceholderConfigurer(
                query.getSql(),
                query.getParameters()
        ); 
        return placeholderConfigurer.getQueries();
    }
    
    private ExecutionStrategy parseSQL(List<String> queries) throws SQLSyntaxErrorException {
        throw new UnsupportedOperationException();
    }
    
    private static QueryResult getQueryResult(QueryType queryType, QueryStatus queryStatus, int count, DataRowMap data, String message) {
        QueryResult queryResult = new QueryResult();
        queryResult.setType(queryType);
        queryResult.setStatus(queryStatus);
        queryResult.setCount(count);
        queryResult.setData(data);
        queryResult.setMessage(message);
        return queryResult;
    }
    
}
