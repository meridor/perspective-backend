package org.meridor.perspective.shell.common.validator;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SettingTest {
    
    @Test
    public void testContains(){
        assertThat(Setting.contains("PAGE_SIZE"), is(true));
        assertThat(Setting.contains("MISSING"), is(false));
    }

}