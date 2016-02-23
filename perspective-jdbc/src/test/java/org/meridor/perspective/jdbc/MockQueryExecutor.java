package org.meridor.perspective.jdbc;

import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.QueryResult;

import java.util.List;

public class MockQueryExecutor implements QueryExecutor {
    private final List<QueryResult> queryResults;

    public MockQueryExecutor(List<QueryResult> queryResults) {
        this.queryResults = queryResults;
    }

    @Override
    public List<QueryResult> execute(List<Query> queries) {
        return queryResults;
    }
}
