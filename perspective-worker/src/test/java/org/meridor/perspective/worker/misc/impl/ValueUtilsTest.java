package org.meridor.perspective.worker.misc.impl;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.worker.misc.impl.ValueUtils.formatQuota;

public class ValueUtilsTest {
    
    @Test
    public void testFormatQuota() {
        assertThat(formatQuota(null, null), equalTo(null));
        assertThat(formatQuota(null, -1), equalTo(null));
        assertThat(formatQuota(0, 0), equalTo(null));
        assertThat(formatQuota(0, -1), equalTo(null));
        assertThat(formatQuota(-1, -1), equalTo(null));
        assertThat(formatQuota(-1, 42), equalTo(null));
        assertThat(formatQuota(1, null), equalTo("1/?"));
        assertThat(formatQuota(0, 45), equalTo("0/45"));
        assertThat(formatQuota(42, 45), equalTo("42/45"));
    }

}