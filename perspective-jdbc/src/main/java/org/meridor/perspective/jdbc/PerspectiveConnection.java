package org.meridor.perspective.jdbc;

import java.sql.Connection;

public interface PerspectiveConnection extends Connection {
    
    UrlInfo getUrlInfo();

    QueryExecutor getQueryExecutor();
    
    void setQueryExecutor(QueryExecutor queryExecutor);

    String getServerVersion();
}
