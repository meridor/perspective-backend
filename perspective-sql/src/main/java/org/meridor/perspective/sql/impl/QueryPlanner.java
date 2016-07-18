package org.meridor.perspective.sql.impl;

import java.sql.SQLException;

public interface QueryPlanner {
    
    QueryPlan plan(String sql) throws SQLException;
    
}
