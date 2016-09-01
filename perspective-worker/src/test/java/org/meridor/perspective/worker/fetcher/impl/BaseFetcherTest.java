package org.meridor.perspective.worker.fetcher.impl;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = "/META-INF/spring/base-fetcher-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class BaseFetcherTest {
    
    @Autowired
    private MockFetcher mockFetcher;
    
    @Test
    @Ignore
    public void testFetchByIds() throws Exception {
        mockFetcher.start();
        Thread.sleep(600);
//        assertThat(mockFetcher.getFetches("all"), equalTo(2));
        //TODO: to be continued...
    }

}