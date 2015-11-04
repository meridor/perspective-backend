package org.meridor.perspective.worker.misc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.config.Cloud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/mocked-storage-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class IdGeneratorTest {

    public static final String ID = "id";
    public static final Cloud CLOUD = new Cloud(){
        {
            setId(ID);
        }
    };
    
    @Autowired
    private IdGenerator idGenerator;
    
    @Test
    public void testGenerate() throws Exception {
        String attempt1 = idGenerator.generate(getClass(), ID);
        String attempt2 = idGenerator.generate(getClass(), ID);
        assertThat(attempt1, equalTo("54a31dbb-57f2-3ec5-9366-e53e59c3999f"));
        assertThat(attempt1, equalTo(attempt2));
    }

    @Test
    public void testGetProjectId() throws Exception {
        String projectId = idGenerator.getProjectId(CLOUD, ID);
        assertThat(projectId, equalTo("8f348574-f915-3c28-9c6e-14905ca09a21"));
    }

    @Test
    public void testGetProjectIdEmptyId() throws Exception {
        String projectId = idGenerator.getProjectId(CLOUD);
        assertThat(projectId, equalTo("3edc0fed-ab7a-31e0-bae8-cb01b95e9919"));
    }

    @Test
    public void testGetInstanceId() throws Exception {
        String instanceId = idGenerator.getInstanceId(CLOUD, ID);
        assertThat(instanceId, equalTo("539becb6-6bfd-3630-809b-848b50723081"));
    }

    @Test
    public void testGetImageId() throws Exception {
        String imageId = idGenerator.getImageId(CLOUD, ID);
        assertThat(imageId, equalTo("f4db4cbb-8f6b-3a3b-beef-904305645342"));
    }
}