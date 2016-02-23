package org.meridor.perspective.jdbc;

import org.junit.BeforeClass;
import org.junit.Test;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.sql.Data;
import org.meridor.perspective.sql.QueryResult;
import org.meridor.perspective.sql.QueryStatus;
import org.meridor.perspective.sql.Row;

import java.sql.*;
import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DriverTest {
    
    public static final String CONNECTION_URL = "http://localhost/";
    
    @BeforeClass
    public static void beforeClass() throws ClassNotFoundException {
        Class.forName("org.meridor.perspective.jdbc.Driver");
    }
    
    @Test
    public void testEndToEndPreparedStatement() throws SQLException {
        ResultSet resultSet = null;
        try (
                Connection connection = DriverManager.getConnection(CONNECTION_URL);
                PreparedStatement statement = connection.prepareStatement("SELECT name FROM instances WHERE cloud_type = ?")
        ) {
            
            QueryExecutor queryExecutor = new MockQueryExecutor(Collections.singletonList(getTestResult()));
            ((PerspectiveConnection)connection).setQueryExecutor(queryExecutor);
            
            statement.setString(1, CloudType.MOCK.value());
            resultSet = statement.executeQuery();

            assertThat(resultSet.next(), is(true));
            
            String name = resultSet.getString("name");
            resultSet.close();
            assertThat(name, equalTo("test-instance"));
        } finally {
            if (resultSet != null && resultSet.isClosed()) {
                resultSet.close();
            }
        }
    }
    
    private static QueryResult getTestResult() {
        QueryResult queryResult = new QueryResult();
        queryResult.setCount(1);
        queryResult.setStatus(QueryStatus.SUCCESS.value());
        Data data = new Data();
        Data.ColumnNames columnNames = new Data.ColumnNames();
        columnNames.getColumnNames().add("name");
        data.setColumnNames(columnNames);
        Data.Rows rows = new Data.Rows();
        Row row = new Row();
        row.getValues().add("test-instance");
        rows.getRows().add(row);
        data.setRows(rows);
        queryResult.setData(data);
        return queryResult;
    }
    
}