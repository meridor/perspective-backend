package org.meridor.perspective.jdbc;

import org.meridor.perspective.jdbc.impl.PerspectiveConnectionImpl;
import org.slf4j.LoggerFactory;

import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

public class Driver implements java.sql.Driver {
    
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Driver.class);
    
    static {
        try {
            LOG.debug("Registering driver to DriverManager");
            Driver driver = new Driver();
            DriverManager.registerDriver(driver);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to register Perspective driver", e);
        }
    }
    
    @Override
    public PerspectiveConnection connect(String url, Properties info) throws SQLException {
        if (url == null) {
            throw new SQLException("Connection URL can't be null.");
        }
        return new PerspectiveConnectionImpl(url);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return new UrlInfo(url).isValid();
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        //TODO: think about it
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return DriverVersionProvider.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return DriverVersionProvider.getMinorVersion();
    }
    
    @Override
    public boolean jdbcCompliant() {
        //TODO: investigate whether this should be changed to true
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("This driver is using slf4j for logging.");
    }
    
}
