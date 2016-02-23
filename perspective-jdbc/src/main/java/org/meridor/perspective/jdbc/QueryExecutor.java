package org.meridor.perspective.jdbc;

import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.QueryResult;

import java.util.List;

public interface QueryExecutor {
    
    List<QueryResult> execute(List<Query> queries);
    
}
