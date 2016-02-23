package org.meridor.perspective.jdbc;

import org.meridor.perspective.sql.Parameter;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.QueryResult;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class PerspectiveStatement extends BaseEntity implements PreparedStatement {

    private String sql;
    
    private final List<List<Parameter>> parametersList = new ArrayList<>();
    
    private int currentRowIndex = 0;
    
    private final PerspectiveConnection connection;
    
    private boolean isClosed = false;
    
    private boolean closeOnCompletion = false;

    public PerspectiveStatement(PerspectiveConnection connection) {
        this(null, connection);
    }
    
    public PerspectiveStatement(String sql, PerspectiveConnection connection) {
        this.sql = sql;
        this.connection = connection;
    }

    private void assertNotClosed() throws SQLException {
        if (isClosed()) {
            throw new SQLException("Can not execute this operation on closed statement");
        }
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        assertNotClosed();
        QueryExecutor queryExecutor = this.connection.getQueryExecutor();
        Query query = new Query();
        query.setSql(this.sql);
        List<QueryResult> queryResults = queryExecutor.execute(Collections.singletonList(query));
        if (queryResults.isEmpty()) {
            throw new SQLException("Request returned no data");
        }
        return new PerspectiveResultSet(this, queryResults.get(0).getData());
    }

    @Override
    public int executeUpdate() throws SQLException {
        assertNotClosed();
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        setObject(parameterIndex, null);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void clearParameters() throws SQLException {
        parametersList.clear();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        setObject(parameterIndex, x, targetSqlType, 0);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        setObject(parameterIndex, x, Types.OTHER);
    }

    @Override
    public boolean execute() throws SQLException {
        assertNotClosed();
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void addBatch() throws SQLException {
        currentRowIndex++;
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return null;
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        setObject(parameterIndex, x.toInstant().toEpochMilli());
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        setObject(parameterIndex, x.toInstant().toEpochMilli());
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        setObject(parameterIndex, x.toInstant().toEpochMilli());
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        setObject(parameterIndex, null);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        setObject(parameterIndex, x.toString());
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return null;
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        Parameter parameter = new Parameter();
        parameter.setIndex(parameterIndex);
        parameter.setValue(x.toString());
        if (parametersList.size() <= currentRowIndex) {
            parametersList.add(new ArrayList<>());
        }
        parametersList.get(currentRowIndex).add(parameterIndex - 1, parameter);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        setCharacterStream(parameterIndex, reader, 0);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        setNCharacterStream(parameterIndex, value, 0);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        setClob(parameterIndex, reader, 0);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        setBlob(parameterIndex, inputStream, 0);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        setNClob(parameterIndex, reader, 0);
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        this.sql = sql;
        return executeQuery();
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        this.sql = sql;
        return executeUpdate();
    }

    @Override
    public void close() throws SQLException {
        isClosed = true;
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getMaxRows() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void cancel() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void clearWarnings() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        this.sql = sql;
        return execute();
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return 0;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return ResultSet.FETCH_FORWARD;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getFetchSize() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public int getResultSetType() throws SQLException {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void clearBatch() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        assertNotClosed();
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Connection getConnection() throws SQLException {
        assertNotClosed();
        return connection;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        assertNotClosed();
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        assertNotClosed();
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        assertNotClosed();
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        assertNotClosed();
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        //TODO: think about it
        return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return isClosed;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        //Does nothing
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return true;
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        assertNotClosed();
        closeOnCompletion = true;
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        assertNotClosed();
        return closeOnCompletion;
    }
}
