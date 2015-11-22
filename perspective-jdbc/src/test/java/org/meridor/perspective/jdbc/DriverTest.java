package org.meridor.perspective.jdbc;

import org.junit.BeforeClass;
import org.junit.Test;
import org.meridor.perspective.config.CloudType;

import java.sql.*;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class DriverTest {
    
    public static final String CONNECTION_URL = "http://localhost/";
    
    @BeforeClass
    public static void beforeClass() throws ClassNotFoundException {
        Class.forName("org.meridor.perspective.jdbc.Driver");
    }
    
    @Test
    public void testEndToEndPreparedStatement() throws SQLException {
        try (
                Connection connection = DriverManager.getConnection(CONNECTION_URL);
                PreparedStatement statement = connection.prepareStatement("SELECT name FROM instances WHERE cloud_type = ?")
        ) {
            statement.setString(1, CloudType.MOCK.value());
            ResultSet resultSet = statement.executeQuery();
            String name = resultSet.getString("name");
            resultSet.close();
            assertThat(name, equalTo("test-instance"));
        }
    }
    
}