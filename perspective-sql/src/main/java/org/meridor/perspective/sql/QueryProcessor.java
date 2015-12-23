package org.meridor.perspective.sql;

import java.util.List;

public interface QueryProcessor {
    
    List<QueryResult> process(Query query);
    
}
