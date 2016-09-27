package org.meridor.perspective.shell.common.repository.impl;

import org.meridor.perspective.shell.common.repository.QueryRepository;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.QueryResult;

import java.util.LinkedList;
import java.util.Queue;

public class MockQueryRepository implements QueryRepository {

    private final Queue<QueryResult> queryResult = new LinkedList<>();
    
    @Override
    public QueryResult query(Query query) {
        return queryResult.poll();
    }

    void addQueryResult(QueryResult queryResult) {
        this.queryResult.add(queryResult);
    }
}
