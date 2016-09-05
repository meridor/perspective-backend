package org.meridor.perspective.worker.fetcher.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.framework.EntityGenerator;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.worker.misc.impl.MockCloud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/mocked-storage-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class ProjectModificationListenerTest {

    @Autowired
    private ProjectsAware projectsAware;
    
    @Autowired
    private ProjectModificationListener projectModificationListener;
    
    private static final String CLOUD_ID = new MockCloud().getId();
    
    @Test
    public void testListen() {
        final String ID = "new-id";
        Project project = EntityGenerator.getProject();
        project.setId(ID);
        project.setCloudId(CLOUD_ID);
        project.setTimestamp(ZonedDateTime.now().minus(1, ChronoUnit.DAYS));
        projectsAware.saveProject(project);
        Set<String> ids = projectModificationListener.getIds(CLOUD_ID, LastModified.LONG_AGO);
        assertThat(ids, contains(ID));
    }
    
}