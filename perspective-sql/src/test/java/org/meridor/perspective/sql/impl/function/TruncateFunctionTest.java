package org.meridor.perspective.sql.impl.function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TruncateFunctionTest {

    @Autowired
    private TruncateFunction function;

    @Test
    public void apply() throws Exception {
        assertThat(function.apply(Collections.singletonList(5.5)), equalTo(5d));
        assertThat(function.apply(Arrays.asList(1.999, 1)), equalTo(1.9));
        assertThat(function.apply(Arrays.asList(-1.999, 1)), equalTo(-1.9));
    }

}