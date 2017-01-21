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
public class ReplaceFunctionTest {

    @Autowired
    private ReplaceFunction function;

    @Test
    public void validateInput() throws Exception {
        assertThat(function.validateInput(Collections.emptyList()), is(not(empty())));
        assertThat(function.validateInput(Collections.singletonList("one")), is(not(empty())));
        assertThat(function.validateInput(Arrays.asList("one", "two")), is(not(empty())));
        assertThat(function.validateInput(Arrays.asList("one", "two", "three")), is(empty()));
    }

    @Test
    public void apply() throws Exception {
        assertThat(function.apply(Arrays.asList("one two three two", "two", "four")), equalTo("one four three four"));
    }

}