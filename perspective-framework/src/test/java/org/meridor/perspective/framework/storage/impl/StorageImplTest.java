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
import org.meridor.perspective.framework.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.framework.storage.StorageEvent.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ContextConfiguration(locations = "/META-INF/spring/real-storage-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class StorageImplTest {
    
    @Autowired
    private InstancesAware instancesAware;
    
    @Autowired
    private ImagesAware imagesAware;
    
    @Autowired
    private ProjectsAware projectsAware;
    
    @Autowired
    private Storage storage;
    
    @Test
    public void testIsAvailable() {
        assertThat(storage.isAvailable(), is(true));
    }
    
    @Test
    public void testGetQueue() {
        BlockingQueue<Object> queue = storage.getQueue("test");
        assertThat(queue, hasSize(0));
        queue.add("test");
        assertThat(queue, contains("test"));
    }
    
    @Test
    public void testGetLock() {
        Lock lock = storage.getLock("test");
        assertThat(lock.tryLock(), is(true));
        lock.unlock();
    }
    
    @Test
    public void testInstanceOperations() {
        Instance instance = EntityGenerator.getInstance();
        String instanceId = instance.getId();
        instancesAware.saveInstance(instance);
        assertThat(instancesAware.instanceExists(instanceId), is(true));
        assertThat(instancesAware.instanceExists("missing"), is(false));
        assertThat(instancesAware.getInstance(instanceId), equalTo(Optional.of(instance)));
        assertThat(instancesAware.getInstance("missing"), equalTo(Optional.empty()));
        assertThat(instancesAware.getInstances(), contains(instance));
        assertThat(instancesAware.getInstances(Collections.singleton(instanceId)), contains(instance));
        instancesAware.deleteInstance(instanceId);
        assertThat(instancesAware.instanceExists(instanceId), is(false));
        assertThat(instancesAware.isInstanceDeleted(instanceId), is(true));
    }
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
    public void testImageOperations() {
        Image image = EntityGenerator.getImage();
        String imageId = image.getId();
        imagesAware.saveImage(image);
        assertThat(imagesAware.imageExists(imageId), is(true));
        assertThat(imagesAware.imageExists("missing"), is(false));
        assertThat(imagesAware.getImage(imageId), equalTo(Optional.of(image)));
        assertThat(imagesAware.getImage("missing"), equalTo(Optional.empty()));
        assertThat(imagesAware.getImages(), contains(image));
        assertThat(imagesAware.getImages(Collections.singleton(imageId)), contains(image));
        imagesAware.deleteImage(imageId);
        assertThat(imagesAware.imageExists(imageId), is(false));
        assertThat(imagesAware.isImageDeleted(imageId), is(true));
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
    public void testProjectOperations() {
        Project project = EntityGenerator.getProject();
        String projectId = project.getId();
        projectsAware.saveProject(project);
        assertThat(projectsAware.projectExists(projectId), is(true));
        assertThat(projectsAware.projectExists("missing"), is(false));
        assertThat(projectsAware.getProject(projectId), equalTo(Optional.of(project)));
        assertThat(projectsAware.getProject("missing"), equalTo(Optional.empty()));
        assertThat(projectsAware.getProjects(), contains(project));
        assertThat(projectsAware.getProjects(Collections.singleton(projectId)), contains(project));
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