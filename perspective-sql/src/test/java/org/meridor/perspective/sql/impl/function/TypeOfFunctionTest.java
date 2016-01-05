package org.meridor.perspective.sql.impl.function;

import org.junit.Test;
import org.meridor.perspective.sql.impl.expression.Null;
import org.meridor.perspective.sql.impl.table.DataType;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;
import static org.meridor.perspective.sql.impl.table.DataType.*;

public class TypeOfFunctionTest {

    private TypeOfFunction function = new TypeOfFunction();
    
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