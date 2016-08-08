package org.meridor.perspective.framework.storage.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.framework.EntityGenerator;
import org.meridor.perspective.framework.storage.ImagesAware;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.framework.storage.StorageEvent.*;

@ContextConfiguration(locations = "/META-INF/spring/real-storage-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class StorageImplTest {
    
    @Autowired
    private InstancesAware instancesAware;
    
    @Autowired
    private ImagesAware imagesAware;
    
    @Autowired
    private ProjectsAware projectsAware;
    
    @Test
    public void testInstanceEvents() throws Exception {
        TestEntityListener<Instance> listener = new TestEntityListener<>(); 
        instancesAware.addInstanceListener(listener);
        Instance testInstance = EntityGenerator.getInstance();
        instancesAware.saveInstance(testInstance);
        Instance anotherInstance = EntityGenerator.getInstance();
        anotherInstance.setName("another");
        instancesAware.saveInstance(anotherInstance);
        instancesAware.deleteInstance(anotherInstance.getId());
        Thread.sleep(1000);
        assertThat(listener.getEntities(), contains(testInstance, anotherInstance, null));
        assertThat(listener.getPreviousEntities(), contains(null, testInstance, anotherInstance));
        assertThat(listener.getEvents(), contains(ADDED, MODIFIED, DELETED));
    }
    
    @Test
    public void testImageEvents() throws Exception {
        TestEntityListener<Image> listener = new TestEntityListener<>(); 
        imagesAware.addImageListener(listener);
        Image testImage = EntityGenerator.getImage();
        imagesAware.saveImage(testImage);
        Image anotherImage = EntityGenerator.getImage();
        anotherImage.setName("another");
        imagesAware.saveImage(anotherImage);
        imagesAware.deleteImage(anotherImage.getId());
        Thread.sleep(1000);
        assertThat(listener.getEntities(), contains(testImage, anotherImage, null));
        assertThat(listener.getPreviousEntities(), contains(null, testImage, anotherImage));
        assertThat(listener.getEvents(), contains(ADDED, MODIFIED, DELETED));
    }
    
    @Test
    public void testProjectEvents() throws Exception {
        TestEntityListener<Project> listener = new TestEntityListener<>(); 
        projectsAware.addProjectListener(listener);
        Project testProject = EntityGenerator.getProject();
        projectsAware.saveProject(testProject);
        Project anotherProject = EntityGenerator.getProject();
        anotherProject.setName("another");
        projectsAware.saveProject(anotherProject);
        Thread.sleep(1000);
        assertThat(listener.getEntities(), contains(testProject, anotherProject));
        assertThat(listener.getPreviousEntities(), contains(null, testProject));
        assertThat(listener.getEvents(), contains(ADDED, MODIFIED));
    }
    
}