package org.meridor.perspective.sql.impl.function;

import org.junit.Test;
import org.meridor.perspective.sql.impl.expression.Null;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.sql.impl.table.DataType.*;

public class TypeOfFunctionTest {

    private final TypeOfFunction function = new TypeOfFunction();
    
    @Test
    public void testValidateInput() throws Exception {
        assertThat(function.validateInput(Collections.emptyList()), is(not(empty())));
        assertThat(function.validateInput(Collections.singletonList(1)), is(not(empty())));
        assertThat(function.validateInput(Arrays.asList(1, INTEGER)), is(empty()));
    }

    @Test
    public void testApply() throws Exception {
        assertThat(function.apply(Arrays.asList(1, INTEGER)), is(true));
        assertThat(function.apply(Arrays.asList("str", STRING)), is(true));
        assertThat(function.apply(Arrays.asList(5d, FLOAT)), is(true));
        assertThat(function.apply(Arrays.asList(new Null(), NULL)), is(true));
        assertThat(function.apply(Arrays.asList(null, NULL)), is(true));
        assertThat(function.apply(Arrays.asList("test", INTEGER)), is(false));
    }
}