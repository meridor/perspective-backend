package org.meridor.perspective.jdbc;

import java.sql.Connection;

public interface PerspectiveConnection extends Connection {
    
    String getUrl();

    String getHost();

    Integer getPort();
    
    String getUserName();
    
    String getServerVersion();
}
