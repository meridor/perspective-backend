package org.meridor.perspective.jdbc.impl;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.meridor.perspective.client.Perspective;
import org.meridor.perspective.jdbc.*;
import org.meridor.perspective.sql.Data;
import org.meridor.perspective.sql.QueryResult;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import static org.meridor.perspective.jdbc.impl.DataUtils.*;

public class PerspectiveConnectionImpl extends BaseEntity implements PerspectiveConnection {
    
    private final UrlInfo urlInfo;
    
    private final Client client;
    
    private boolean isClosed = false;
    
    private boolean autoCommitEnabled = true;
    
    private boolean isReadOnly = false;
    
    private int networkTimeout = 1000;
    
    public PerspectiveConnectionImpl(String url) {
        this.urlInfo = new UrlInfo(url);
        this.client = createClient(urlInfo);
    }
    
    private Client createClient(UrlInfo urlInfo) {
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basicBuilder()
                .nonPreemptive()
                .credentials(urlInfo.getUserName(), urlInfo.getPassword())
                .build();

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(feature);

        return ClientBuilder.newClient(clientConfig);
    }

    @Override
    public UrlInfo getUrlInfo() {
        return urlInfo;
    }

    @Override
    public Client getClient() {
        return client;
    }

    @Override
    public String getServerVersion() {
        final String SERVER_VERSION = "version";
        QueryResult queryResult = Perspective.root(getClient()).version()
                .getAs(QueryResult.class);
        Data data = queryResult.getData();
        return (getDataSize(data) == 1) ? get(data, getRow(data, 0), SERVER_VERSION).toString() : "unknown";
    }

    private void assertNotClosed() throws SQLException {
        if (isClosed()) {
            throw new SQLException("Can not execute this operation on closed connection");
        }
    }
    
    private void assertNotAutoCommit() throws SQLException {
        if (getAutoCommit()) {
            throw new SQLException("Can not execute this operation when auto-commit mode is enabled");
        }
    }

    @Override
    public Statement createStatement() throws SQLException {
        return new PerspectiveStatement(this);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return new PerspectiveStatement(sql, this);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException("Stored procedures are not supported.");
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        assertNotClosed();
        return null;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        assertNotClosed();
        this.autoCommitEnabled = autoCommit;
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        assertNotClosed();
        return autoCommitEnabled;
    }

    @Override
    public void commit() throws SQLException {
        assertNotClosed();
        assertNotAutoCommit();

    }

    @Override
    public void rollback() throws SQLException {
        assertNotClosed();
        assertNotAutoCommit();

    }

    @Override
    public void close() throws SQLException {
        if (!isClosed()) {
            isClosed = true;
        }
    }

    @Override
    public boolean isClosed() throws SQLException {
        return isClosed;
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        assertNotClosed();
        return new PerspectiveMetadata(this);
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        assertNotClosed();
        this.isReadOnly = readOnly;
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        assertNotClosed();
        return isReadOnly;
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        assertNotClosed();
        
    }

    @Override
    public String getCatalog() throws SQLException {
        assertNotClosed();
        return null;
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        assertNotClosed();
        throw new SQLFeatureNotSupportedException("Transactions are not supported yet");
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        assertNotClosed();
        return Connection.TRANSACTION_NONE;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        assertNotClosed();
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {
        assertNotClosed();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        assertNotClosed();
        return createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        assertNotClosed();
        return prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return prepareCall(sql);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getHoldability() throws SQLException {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return setSavepoint(null);
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        rollback(savepoint);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        assertNotClosed();
        return createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        assertNotClosed();
        return prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return prepareCall(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        assertNotClosed();
        return prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        assertNotClosed();
        return prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        assertNotClosed();
        return prepareStatement(sql);
    }

    @Override
    public Clob createClob() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Blob createBlob() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public NClob createNClob() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        throw new SQLClientInfoException();
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        throw new SQLClientInfoException();
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        //Silently ignoring as schema is not supported
    }

    @Override
    public String getSchema() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        if (executor == null) {
            throw new SQLException("Executor can't be null.");
        }
        
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        assertNotClosed();
        this.networkTimeout = milliseconds;
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        assertNotClosed();
        return networkTimeout;
    }

}
