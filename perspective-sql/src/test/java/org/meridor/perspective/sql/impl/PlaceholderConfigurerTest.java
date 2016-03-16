package org.meridor.perspective.sql.impl;

import org.junit.Test;
import org.meridor.perspective.sql.Parameter;

import java.sql.SQLDataException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class PlaceholderConfigurerTest {

    @Test
    public void testQueryWithPositionalPlaceholder() throws Exception {
        String query = "SELECT * FROM some_table WHERE id = ? AND name = ?";
        List<Parameter> parameters = Arrays.asList(
                createParameter(1, "42"),
                createParameter(2, "test-'name")
        );
        PlaceholderConfigurer placeholderConfigurer = new PlaceholderConfigurer(query, parameters);
        List<String> queries = placeholderConfigurer.getQueries();
        assertThat(queries, hasSize(1));
        assertThat(queries.get(0), equalTo("SELECT * FROM some_table WHERE id = 42 AND name = 'test-\\'name'"));
    }
    
    @Test
    public void testQueryWithNamedPlaceholder() throws Exception {
        String query = "SELECT * FROM some_table WHERE id = :some.id: AND name = :name:;";
        List<Parameter> parameters = Arrays.asList(
                createParameter("some.id", "42"),
                createParameter("name", "test-\\name")
        );
        PlaceholderConfigurer placeholderConfigurer = new PlaceholderConfigurer(query, parameters);
        List<String> queries = placeholderConfigurer.getQueries();
        assertThat(queries, hasSize(1));
        assertThat(queries.get(0), equalTo("SELECT * FROM some_table WHERE id = 42 AND name = 'test-\\\\name'"));
    }
    
    @Test
    public void testMultipleQueries() throws Exception {
        String query = "SELECT * FROM some_table WHERE name = :name:;\nSELECT id, name FROM another_table WHERE enabled = ?";
        List<Parameter> parameters = Arrays.asList(
                createParameter("name", "test-;name"),
                createParameter(1, "1")
        );
        PlaceholderConfigurer placeholderConfigurer = new PlaceholderConfigurer(query, parameters);
        List<String> queries = placeholderConfigurer.getQueries();
        assertThat(queries, hasSize(2));
        assertThat(queries.get(0), equalTo("SELECT * FROM some_table WHERE name = 'test-\\;name'"));
        assertThat(queries.get(1), equalTo("SELECT id, name FROM another_table WHERE enabled = 1"));
    }
    
    @Test
    public void testPlaceholderInLiteral() throws Exception {
        String query = "SELECT * FROM some_table WHERE id = '?' name = ':name:'";
        PlaceholderConfigurer placeholderConfigurer = new PlaceholderConfigurer(query, Collections.emptyList());
        List<String> queries = placeholderConfigurer.getQueries();
        assertThat(queries, hasSize(1));
        assertThat(queries.get(0), equalTo(query));
    }
    
    @Test(expected = SQLDataException.class)
    public void testMissingPositionalParameter() throws Exception {
        String query = "SELECT * FROM some_table WHERE name = ?";
        new PlaceholderConfigurer(query, Collections.emptyList()).getQueries();
    }
    
    @Test(expected = SQLDataException.class)
    public void testMissingNamedParameter() throws Exception {
        String query = "SELECT * FROM some_table WHERE name = :name:";
        new PlaceholderConfigurer(query, Collections.emptyList()).getQueries();
    }
    
    @Test(expected = SQLDataException.class)
    public void testInvalidQuery() throws Exception {
        String query = "? SELECT * FROM some_table WHERE 1 = 1";
        new PlaceholderConfigurer(query, Collections.emptyList()).getQueries();
    }

    private static Parameter createParameter(String name, String value) {
        return createParameter(Optional.of(name), Optional.empty(), value);
    }
    
    private static Parameter createParameter(Integer index, String value) {
        return createParameter(Optional.empty(), Optional.of(index), value);
    }
    
    private static Parameter createParameter(Optional<String> name, Optional<Integer> index, String value) {
        Parameter parameter = new Parameter();
        parameter.setValue(value);
        if (name.isPresent()) {
            parameter.setName(name.get());
        }
        if (index.isPresent()) {
            parameter.setIndex(index.get());
        }
        return parameter;
    }
    
}