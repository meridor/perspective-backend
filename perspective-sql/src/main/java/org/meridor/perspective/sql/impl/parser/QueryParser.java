package org.meridor.perspective.sql.impl.parser;

import java.sql.SQLSyntaxErrorException;

public interface QueryParser {
    
    void parse(String sql) throws SQLSyntaxErrorException;
    
    QueryType getQueryType();
    
    SelectQueryAware getSelectQueryAware();
    
}
