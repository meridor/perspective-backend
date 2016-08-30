package org.meridor.perspective.worker.fetcher;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.worker.fetcher.impl.SchedulerUtils.getMomentsAgoLimit;
import static org.meridor.perspective.worker.fetcher.impl.SchedulerUtils.getSomeTimeAgoLimit;

public class SchedulerUtilsTest {
    @Test
    public void testGetMomentsAgoLimit() throws Exception {
        assertThat(getMomentsAgoLimit(30000), equalTo(3000));
        assertThat(getMomentsAgoLimit(30001), equalTo(3000));
    }

    @Test
    public void testGetSomeTimeAgoLimit() throws Exception {
        assertThat(getSomeTimeAgoLimit(30000), equalTo(15000));
        assertThat(getSomeTimeAgoLimit(30001), equalTo(15000));
        assertThat(getSomeTimeAgoLimit(30002), equalTo(15001));
    }

}