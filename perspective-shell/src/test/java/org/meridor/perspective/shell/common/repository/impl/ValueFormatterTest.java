package org.meridor.perspective.shell.common.repository.impl;

import org.junit.Test;
import org.meridor.perspective.sql.Data;
import org.meridor.perspective.sql.Row;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ValueFormatterTest {
    
    @Test
    public void testGetString() {
        Data data = new Data();
        data.setColumnNames(Arrays.asList("not-null", "nullable"));
        Row row = new Row();
        row.getValues().addAll(Arrays.asList("value", null));
        data.setRows(Collections.singletonList(row));
        ValueFormatter valueFormatter = new ValueFormatter(data, row);
        
        assertThat(valueFormatter.getString("not-null"), equalTo("value"));
        assertThat(valueFormatter.getString("nullable"), equalTo("-"));
    }

}