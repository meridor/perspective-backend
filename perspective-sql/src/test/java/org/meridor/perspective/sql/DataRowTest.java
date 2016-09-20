package org.meridor.perspective.sql;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class DataRowTest {
    
    private static final String FIRST_TABLE = "first_table";
    private static final String SECOND_TABLE = "second_table";
    private static final String FIRST_COLUMN = "first_column";
    private static final String SECOND_COLUMN = "second_column";
    private static final String FIRST_COLUMN_VALUE = "first_column_value";
    private static final String SECOND_COLUMN_VALUE = "second_column_value";
    private static final String THIRD_COLUMN_VALUE = "third_column_value";
    private static final String NEW_VALUE = "new_value";
    
    private static final Map<String, List<String>> COLUMNS_MAP = new HashMap<String, List<String>>(){
        {
            put(FIRST_TABLE, Arrays.asList(FIRST_COLUMN, SECOND_COLUMN));
            put(SECOND_TABLE, Collections.singletonList(FIRST_COLUMN));
        }
    };

    private static final DataContainer DATA_CONTAINER = new DataContainer(COLUMNS_MAP);
    
    private static final List<Object> VALUES = Arrays.asList(FIRST_COLUMN_VALUE, SECOND_COLUMN_VALUE, THIRD_COLUMN_VALUE);
    
    private DataRow dataRow;
    
    @Before
    public void init() throws Exception {
        dataRow = new DataRow(DATA_CONTAINER, VALUES);
    }

    @Test
    public void testGetByIndex() throws Exception {
        assertThat(dataRow.get(0), equalTo(FIRST_COLUMN_VALUE));
        assertThat(dataRow.get(1), equalTo(SECOND_COLUMN_VALUE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWithWrongIndex() {
        dataRow.get(4);
    }
    
    @Test
    public void testGetByColumnName() throws Exception {
        assertThat(dataRow.get(0), equalTo(FIRST_COLUMN_VALUE));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetWithWrongColumnName() throws Exception {
        dataRow.get("missing-column");
    }
    
    @Test
    public void testGetByCompoundColumnName() throws Exception {
        String columnName = getCompoundColumnName(FIRST_COLUMN, FIRST_TABLE);
        assertThat(dataRow.get(columnName), equalTo(FIRST_COLUMN_VALUE));
    }

    private String getCompoundColumnName(String columnName, String tableAlias) {
        return String.format("%s.%s", tableAlias, columnName);
    }
    
    @Test
    public void testGetByColumnNameAndTableAlias() throws Exception {
        assertThat(dataRow.get(FIRST_COLUMN, FIRST_TABLE), equalTo(FIRST_COLUMN_VALUE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWithWrongColumnNameAndTableAlias() throws Exception {
        dataRow.get("missing-column", "any-alias");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWithAmbiguousColumnName() throws Exception {
        dataRow.get(FIRST_COLUMN);
    }

    @Test
    public void testPutByIndex() throws Exception {
        dataRow.put(0, NEW_VALUE);
        assertThat(dataRow.getValues(), contains(NEW_VALUE, SECOND_COLUMN_VALUE, THIRD_COLUMN_VALUE));
    }

    @Test
    public void testPutByColumnName() throws Exception {
        dataRow.put(SECOND_COLUMN, NEW_VALUE);
        assertThat(dataRow.getValues(), contains(FIRST_COLUMN_VALUE, NEW_VALUE, THIRD_COLUMN_VALUE));
    }

    @Test
    public void testPutByCompoundColumnName() throws Exception {
        String columnName = getCompoundColumnName(FIRST_COLUMN, SECOND_TABLE);
        dataRow.put(columnName, NEW_VALUE);
        assertThat(dataRow.getValues(), contains(FIRST_COLUMN_VALUE, SECOND_COLUMN_VALUE, NEW_VALUE));
    }
    
    @Test
    public void testPutByColumnNameAndTableAlias() throws Exception {
        dataRow.put(FIRST_COLUMN, SECOND_TABLE, NEW_VALUE);
        assertThat(dataRow.getValues(), contains(FIRST_COLUMN_VALUE, SECOND_COLUMN_VALUE, NEW_VALUE));
    }

    @Test
    public void testGetValues() throws Exception {
        assertThat(dataRow.getValues(), equalTo(VALUES));
    }
}