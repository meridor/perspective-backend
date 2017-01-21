package org.meridor.perspective.sql.impl.function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class ConvFunctionTest {

    @Autowired
    private ConvFunction function;

    @Test
    public void validateInput() throws Exception {
        assertThat(function.validateInput(Collections.emptyList()), is(not(empty())));
        assertThat(function.validateInput(Arrays.asList("32", 1, 16)), is(not(empty())));
        assertThat(function.validateInput(Arrays.asList("42", 16, -1)), is(not(empty())));
        assertThat(function.validateInput(Arrays.asList("-symbols!", 2, 16)), is(not(empty())));
        assertThat(function.validateInput(Arrays.asList("a", 16, 2)), is(empty()));
    }

    @Test
    public void apply() throws Exception {
        assertThat(function.apply(Arrays.asList("a", 16, 2)), equalTo("1010"));
        assertThat(function.apply(Arrays.asList("6E", 18, 8)), equalTo("172"));
    }

}