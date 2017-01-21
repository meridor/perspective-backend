package org.meridor.perspective.sql.impl.function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.sql.DataContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.sql.impl.function.FunctionName.ABS;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class FunctionsFunctionTest {

    @Autowired
    private FunctionsFunction function;

    @Test
    public void testValidateInput() {
        assertThat(function.validateInput(Collections.emptyList()), is(empty()));
        assertThat(function.validateInput(Collections.singletonList(ABS.name())), is(empty()));
    }

    @Test
    public void testListAllFunctions() {
        DataContainer data = function.apply(Collections.emptyList());
        assertThat(data.getColumnNames(), contains("function_name", "description"));
        assertThat(data.getRows(), hasSize(FunctionName.values().length));
    }

    @Test
    public void testOneFunction() {
        DataContainer data = function.apply(Collections.singletonList(ABS.name()));
        assertThat(data.getColumnNames(), contains("function_name", "description"));
        assertThat(data.getRows(), hasSize(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingFunction() {
        function.apply(Collections.singletonList("missing_function"));
    }
}