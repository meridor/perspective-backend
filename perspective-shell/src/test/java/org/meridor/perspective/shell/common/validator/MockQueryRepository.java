package org.meridor.perspective.shell.common.validator;

import org.meridor.perspective.shell.common.repository.QueryRepository;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.QueryResult;

public class MockQueryRepository implements QueryRepository {
    
    private QueryResult queryResult;
    
    @Override
    public QueryResult query(Query query) {
        return queryResult;
    }

    public void setQueryResult(QueryResult queryResult) {
        this.queryResult = queryResult;
    }
}
