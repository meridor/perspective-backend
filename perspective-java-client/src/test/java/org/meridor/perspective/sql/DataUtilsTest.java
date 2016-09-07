package org.meridor.perspective.sql;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.sql.DataUtils.get;
import static org.meridor.perspective.sql.DataUtils.getColumnIndex;

public class DataUtilsTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetIndexForInvalidColumn() {
        getColumnIndex(createData(), "missing_column");
    }
    
    @Test
    public void testGetColumnIndex() {
        Data data = createData();
        assertThat(getColumnIndex(data, "column_one"), equalTo(0));
        assertThat(getColumnIndex(data, "column_two"), equalTo(1));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetInvalidColumnIndex(){
        Data data = createData();
        Row row = createRow("11", "12");
        get(data, row, 2);
    }
    
    @Test
    public void testGet() {
        Data data = createData();
        Row row = createRow("11", "12");
        assertThat(get(data, row, "column_one"), equalTo("11"));
        assertThat(get(data, row, 1), equalTo("12"));
    }
    
    private static Data createData() {
        Data data = new Data();
        data.getColumnNames().addAll(Arrays.asList("column_one", "column_two"));
        return data;
    }
    
    private static Row createRow(Object...values) {
        Row row = new Row();
        row.getValues().addAll(Arrays.asList(values));
        return row;
    }
    
}