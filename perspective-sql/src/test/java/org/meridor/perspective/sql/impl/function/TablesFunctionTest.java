package org.meridor.perspective.sql.impl.function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.table.TablesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TablesFunctionTest {

    @Autowired
    private TablesFunction function;
    
    @Autowired
    private TablesAware tablesAware;

    @Test
    public void testValidateInput() {
        assertThat(function.validateInput(Collections.singletonList("instances")), is(not(empty())));
        assertThat(function.validateInput(Collections.emptyList()), is(empty()));
    }

    @Test
    public void testApply() {
        DataContainer data = function.apply(Collections.emptyList());
        assertThat(data.getColumnNames(), contains("table_name"));
        assertThat(data.getRows(), hasSize(tablesAware.getTables().size()));
        
    }
}