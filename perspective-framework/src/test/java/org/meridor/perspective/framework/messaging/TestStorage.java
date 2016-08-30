package org.meridor.perspective.framework.messaging;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.framework.storage.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.meridor.perspective.framework.storage.StorageEvent.*;

@Component
public class TestStorage implements InstancesAware, ProjectsAware, ImagesAware, Storage {

    private volatile Map<String, Lock> locks = new ConcurrentHashMap<>();
    private volatile Map<String, BlockingQueue<?>> queues = new ConcurrentHashMap<>();
    private volatile Map<String, Image> imageMap = new ConcurrentHashMap<>();
    private volatile Map<String, Instance> instanceMap = new ConcurrentHashMap<>();
    private volatile Map<String, Project> projectMap = new ConcurrentHashMap<>();
    private volatile Map<String, Map<String, Object>> otherMaps = new ConcurrentHashMap<>();
    private List<EntityListener<Project>> projectListeners = new ArrayList<>();
    private List<EntityListener<Image>> imageListeners = new ArrayList<>();
    private List<EntityListener<Instance>> instanceListeners = new ArrayList<>();
    
    @Override
    public boolean imageExists(String imageId) {
        return imageMap.containsKey(imageId);
    }

    @Override
    public Collection<Image> getImages() {
        return imageMap.values();
    }

    @Override
    public Collection<Image> getImages(Set<String> ids) {
        return imageMap.keySet().stream()
                .filter(ids::contains)
                .map(imageMap::get)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Image> getImage(String imageId) {
        return Optional.ofNullable(imageMap.get(imageId));
    }

    @Override
    public void saveImage(Image image) {
        String imageId = image.getId();
        StorageEvent event = imageExists(imageId) ?
                MODIFIED :
                ADDED;
        imageListeners.forEach(l -> l.onEvent(image, imageMap.get(imageId), event));
        imageMap.put(image.getId(), image);
    }

    @Override
    public boolean isImageDeleted(String imageId) {
        return !imageExists(imageId);
    }

    @Override
    public void deleteImage(String imageId) {
        Image image = getImage(imageId).get();
        imageListeners.forEach(l -> l.onEvent(image, null, DELETED));
        imageMap.remove(imageId);
    }

    @Override
    public void addImageListener(EntityListener<Image> listener) {
        imageListeners.add(listener);
    }

    @Override
    public boolean instanceExists(String instanceId) {
        return instanceMap.containsKey(instanceId);
    }

    @Override
    public Collection<Instance> getInstances() {
        return instanceMap.values();
    }

    @Override
    public Collection<Instance> getInstances(Set<String> ids) {
        return instanceMap.keySet().stream()
                .filter(ids::contains)
                .map(instanceMap::get)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Instance> getInstance(String instanceId) {
        return Optional.ofNullable(instanceMap.get(instanceId));
    }

    @Override
    public void saveInstance(Instance instance) {
        String instanceId = instance.getId();
        StorageEvent event = instanceExists(instanceId) ?
                MODIFIED :
                ADDED;
        instanceListeners.forEach(l -> l.onEvent(instance, instanceMap.get(instanceId), event));
        instanceMap.put(instance.getId(), instance);
    }

    @Override
    public boolean isInstanceDeleted(String instanceId) {
        return !instanceExists(instanceId);
    }

    @Override
    public void deleteInstance(String instanceId) {
        Instance instance = getInstance(instanceId).get();
        instanceListeners.forEach(l -> l.onEvent(instance, null, DELETED));
        instanceMap.remove(instanceId);
    }

    @Override
    public void addInstanceListener(EntityListener<Instance> listener) {
        instanceListeners.add(listener);
    }

    @Override
    public boolean projectExists(String projectId) {
        return projectMap.containsKey(projectId);
    }

    @Override
    public Optional<Project> getProject(String projectId) {
        return Optional.ofNullable(projectMap.get(projectId));
    }

    @Override
    public Collection<Project> getProjects() {
        return projectMap.values();
    }

    @Override
    public Collection<Project> getProjects(Set<String> ids) {
        return projectMap.keySet().stream()
                .filter(ids::contains)
                .map(projectMap::get)
                .collect(Collectors.toList());
    }

    @Override
    public void saveProject(Project project) {
        String projectId = project.getId();
        StorageEvent event = getProject(projectId).isPresent() ?
                MODIFIED :
                ADDED;
        projectListeners.forEach(l -> l.onEvent(project, projectMap.get(projectId), event));
        projectMap.put(project.getId(), project);
    }

    @Override
    public void addProjectListener(EntityListener<Project> listener) {
        projectListeners.add(listener);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> BlockingQueue<T> getQueue(String id) {
        return (BlockingQueue<T>) queues.computeIfAbsent(id, s -> new ArrayBlockingQueue<>(10));
    }

    @Override
    public Lock getLock(String name) {
        return locks.computeIfAbsent(name, s -> new ReentrantLock());
    }

    @Override
    public <T> T executeSynchronized(String lockName, long timeout, Supplier<T> action) {
        Lock lock = getLock(lockName);
        try {
            if (lock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
                try {
                    return action.get();
                } finally {
                    lock.unlock();
                }
            } else {
                return null;
            }
        } catch (InterruptedException e) {
            return null;
        }
    }

    @Override
    public <K, T> void modifyMap(String mapId, K key, Consumer<Map<K, T>> action) {
        otherMaps.putIfAbsent(mapId, new HashMap<>());
        @SuppressWarnings("unchecked")
        Map<K, T> map = (Map<K, T>) otherMaps.get(mapId);
        action.accept(map);
    }

    @Override
    public <K, I, O> O readFromMap(String mapId, K key, Function<Map<K, I>, O> function) {
        otherMaps.putIfAbsent(mapId, new HashMap<>());
        @SuppressWarnings("unchecked")
        Map<K, I> map = (Map<K, I>) otherMaps.get(mapId);
        return function.apply(map);
    }

}
