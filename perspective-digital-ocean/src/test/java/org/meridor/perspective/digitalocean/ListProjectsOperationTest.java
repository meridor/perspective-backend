package org.meridor.perspective.digitalocean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.backend.EntityGenerator;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/META-INF/spring/operation-context.xml")
@DirtiesContext(classMode = BEFORE_CLASS)
public class ListProjectsOperationTest {

    @Autowired
    private ListProjectsOperation listProjectsOperation;

    @Test
    public void testListAll() {
        Cloud cloud = EntityGenerator.getCloud();
        Map<String, Project> storage = new HashMap<>();
        listProjectsOperation.perform(cloud, p -> storage.put(p.getId(), p));
        assertThat(storage.keySet(), hasSize(2));
    }

}