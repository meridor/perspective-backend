package org.meridor.perspective.worker.misc.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.worker.misc.CloudConfigurationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/mocked-storage-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class CloudConfigurationProviderImplTest {

    @Autowired
    private CloudConfigurationProvider cloudConfigurationProvider;
    
    private static final Cloud CLOUD = new MockCloud();
    
    @Test
    public void testGetCloud() throws Exception {
        Cloud cloud = cloudConfigurationProvider.getCloud("test-id");
        assertThat(cloud, equalTo(CLOUD));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetCloudWithMissingId() throws Exception {
        cloudConfigurationProvider.getCloud("missing-id");
    }

    @Test
    public void testGetClouds() throws Exception {
        List<Cloud> clouds = new ArrayList<>(cloudConfigurationProvider.getClouds());
        assertThat(clouds, hasSize(1));
        assertThat(clouds.get(0), equalTo(CLOUD));
    }

}