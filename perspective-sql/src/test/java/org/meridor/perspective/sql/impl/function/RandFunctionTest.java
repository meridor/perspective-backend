package org.meridor.perspective.sql.impl.function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class RandFunctionTest {

    @Autowired
    private RandFunction function;

    @Test
    public void validateInput() throws Exception {
        assertThat(function.validateInput(Collections.singletonList("nan")), is(not(empty())));
        assertThat(function.validateInput(Collections.emptyList()), is(empty()));
        assertThat(function.validateInput(Collections.singletonList(42)), is(empty()));
    }

    @Test
    public void apply() throws Exception {
        assertThat(function.apply(Collections.emptyList()), not(equalTo(function.apply(Collections.emptyList()))));
        assertThat(function.apply(Collections.singletonList(42)), equalTo(function.apply(Collections.singletonList(42))));
    }

}