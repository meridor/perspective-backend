package org.meridor.perspective.rest.resources;

import org.junit.Test;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.mock.EntityGenerator;

import javax.ws.rs.core.GenericType;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class ProjectsResourceIntegrationTest extends BaseIntegrationTest {

    @Test
    public void testList() throws InterruptedException {
        List<Project> projects = target("/projects").request().get(new GenericType<List<Project>>() {
        });
        assertThat(projects, hasSize(1));
        assertThat(projects.get(0), equalTo(EntityGenerator.getProject()));
    }

}
