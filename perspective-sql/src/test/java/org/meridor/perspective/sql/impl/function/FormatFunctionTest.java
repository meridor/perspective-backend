package org.meridor.perspective.sql.impl.function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.IllegalFormatException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class FormatFunctionTest {

    @Autowired
    private FormatFunction function;

    @Test
    public void validateInput() throws Exception {
        assertThat(function.validateInput(Collections.emptyList()), is(not(empty())));
        assertThat(function.validateInput(Collections.singletonList("anything")), is(empty()));
        assertThat(function.validateInput(Arrays.asList("%s", "a string")), is(empty()));
    }

    @Test
    public void apply() throws Exception {
        assertThat(function.apply(Collections.singletonList("a string")), equalTo("a string"));
        assertThat(function.apply(Arrays.asList("number: %.2f", -1.5)), equalTo("number: -1.50"));
    }

    @Test(expected = IllegalFormatException.class)
    public void illegalFormat() throws Exception {
        //Passing string as integer
        function.apply(Arrays.asList("%d", "string"));
    }

}