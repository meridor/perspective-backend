package org.meridor.perspective.worker.fetcher.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/base-fetcher-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class BaseFetcherTest {
    
    @Autowired
    private MockFetcher mockFetcher;
    
    @Test
    public void testSchedulerProportions() throws Exception {
        Thread.sleep(650);
        int all = mockFetcher.getFetches("all");
        int now = mockFetcher.getFetches("now");
        int momentsAgo = mockFetcher.getFetches("moments");
        int someTimeAgo = mockFetcher.getFetches("some_time");
        int longAgo = mockFetcher.getFetches("long");
        assertThat(now, greaterThan(0));
        assertThat(momentsAgo, greaterThan(0));
        assertThat(someTimeAgo, greaterThan(0));
        assertThat(longAgo, equalTo(0));
        assertThat(all, greaterThan(0));
        
        //Because of precision errors we expect numbers near some value
        final double ROUND_ERROR = 1.0;
        assertThat((double) (now / momentsAgo), closeTo(4.0, ROUND_ERROR));
        assertThat((double) (momentsAgo / someTimeAgo), closeTo(10.0, ROUND_ERROR));
        assertThat((double) (someTimeAgo / all), closeTo(3.0, ROUND_ERROR));
    }

}