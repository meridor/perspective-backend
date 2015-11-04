package org.meridor.perspective.worker.misc;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.framework.storage.*;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class TestStorage implements InstancesAware, ProjectsAware, ImagesAware, Storage {

    private volatile Map<String, Lock> locks = new ConcurrentHashMap<>();
    private volatile Map<String, BlockingQueue<?>> queues = new ConcurrentHashMap<>();
    private volatile Map<String, Image> imageMap = new ConcurrentHashMap<>();
    private volatile Map<String, Instance> instanceMap = new ConcurrentHashMap<>();
    private volatile Map<String, Project> projectMap = new ConcurrentHashMap<>();
    
    @Override
    public boolean imageExists(String imageId) {
        return imageMap.containsKey(imageId);
    }

    @Override
    public Collection<Image> getImages(Optional<String> query) throws IllegalQueryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Image> getImage(String imageId) {
        return Optional.ofNullable(imageMap.get(imageId));
    }

    @Override
    public void saveImage(Image image) {
        imageMap.put(image.getId(), image);
    }

    @Override
    public boolean isImageDeleted(String imageId) {
        return !imageExists(imageId);
    }

    @Override
    public void deleteImage(Image image) {
        imageMap.remove(image.getId());
    }

    @Override
    public boolean instanceExists(String instanceId) {
        return instanceMap.containsKey(instanceId);
    }

    @Override
    public Collection<Instance> getInstances(Optional<String> query) throws IllegalQueryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Instance> getInstance(String instanceId) {
        return Optional.ofNullable(instanceMap.get(instanceId));
    }

    @Override
    public void saveInstance(Instance instance) {
        instanceMap.put(instance.getId(), instance);
    }

    @Override
    public boolean isInstanceDeleted(String instanceId) {
        return !instanceExists(instanceId);
    }

    @Override
    public void deleteInstance(Instance instance) {
        instanceMap.remove(instance.getId());
    }

    @Override
    public Optional<Project> getProject(String projectId) {
        return Optional.ofNullable(projectMap.get(projectId));
    }

    @Override
    public Collection<Project> getProjects(Optional<String> query) throws IllegalQueryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveProject(Project project) {
        projectMap.put(project.getId(), project);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> BlockingQueue<T> getQueue(String id) {
        return (BlockingQueue<T>) queues.computeIfAbsent(id, s -> new PriorityBlockingQueue<T>());
    }

    @Override
    public Lock getLock(String name) {
        return locks.computeIfAbsent(name, s -> new ReentrantLock());
    }
}
