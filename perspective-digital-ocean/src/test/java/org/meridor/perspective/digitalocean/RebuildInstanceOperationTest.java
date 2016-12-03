package org.meridor.perspective.digitalocean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.backend.EntityGenerator;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.Cloud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/META-INF/spring/operation-context.xml")
@DirtiesContext(classMode = BEFORE_CLASS)
public class RebuildInstanceOperationTest {

    @Autowired
    private RebuildInstanceOperation rebuildInstanceOperation;

    @Test
    public void testListAll() {
        Cloud cloud = EntityGenerator.getCloud();
        Instance instance = EntityGenerator.getInstance();
        instance.setRealId("345");
        instance.getImage().setRealId("123"); //It's a number for DigitalOcean
        assertThat(rebuildInstanceOperation.perform(cloud, () -> instance), is(true));
    }

}