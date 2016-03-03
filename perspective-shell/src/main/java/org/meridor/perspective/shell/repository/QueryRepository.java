package org.meridor.perspective.shell.repository;

import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.QueryResult;

public interface QueryRepository {
    
    QueryResult query(Query query);
    
}
