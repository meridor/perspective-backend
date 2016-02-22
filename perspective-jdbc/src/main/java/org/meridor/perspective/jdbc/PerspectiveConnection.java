package org.meridor.perspective.jdbc;

import javax.ws.rs.client.Client;
import java.sql.Connection;

public interface PerspectiveConnection extends Connection {
    
    UrlInfo getUrlInfo();

    Client getClient();

    String getServerVersion();
}
