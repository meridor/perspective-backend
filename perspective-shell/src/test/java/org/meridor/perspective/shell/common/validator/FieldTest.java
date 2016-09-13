package org.meridor.perspective.shell.common.validator;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FieldTest {
    
    @Test
    public void testContains(){
        assertThat(Field.contains("CLOUDS"), is(true));
        assertThat(Field.contains("MISSING"), is(false));
    }

}