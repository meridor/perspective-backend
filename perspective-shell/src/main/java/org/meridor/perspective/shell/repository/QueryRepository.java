package org.meridor.perspective.shell.repository;

import org.meridor.perspective.sql.Parameter;
import org.meridor.perspective.sql.QueryResult;

public interface QueryRepository {
    
    QueryResult query(String sql, Parameter... parameter);
    
}
