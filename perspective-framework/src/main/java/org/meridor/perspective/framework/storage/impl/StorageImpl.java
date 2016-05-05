package org.meridor.perspective.framework.storage.impl;


import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.framework.storage.ImagesAware;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.framework.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.meridor.perspective.framework.storage.impl.StorageKey.*;

@Component
public class StorageImpl implements ApplicationListener<ContextClosedEvent>, InstancesAware, ProjectsAware, ImagesAware, Storage {

    private static final Logger LOG = LoggerFactory.getLogger(StorageImpl.class);

    @Autowired
    private HazelcastInstance hazelcastInstance;
    
    @Value("${perspective.storage.lock.timeout:5000}")
    private long lockTimeout;

    private volatile boolean isAvailable = true;

    @Override
    public boolean isAvailable() {
        return isAvailable && hazelcastInstance.getLifecycleService().isRunning();
    }

    @Override
    public <T> BlockingQueue<T> getQueue(String name) {
        return isAvailable() ?
                hazelcastInstance.getQueue(name) :
                new LinkedBlockingQueue<>();
    }

    @Override
    public Lock getLock(String name) {
        return hazelcastInstance.getLock(name);
    }

    @Override
    public <T> T executeSynchronized(String lockName, long timeout, Supplier<T> action) {
        LOG.trace("Trying to obtain lock {}", lockName);
        Lock lock = getLock(lockName);
        try {
            if (lock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
                try {
                    return action.get();
                } finally {
                    LOG.trace("Releasing the lock {}", lockName);
                    lock.unlock();
                }
            } else {
                LOG.trace("Failed to obtain lock {}. Will do nothing.", lockName);
                return null;
            }
        } catch (InterruptedException e) {
            LOG.trace("Lock {} thread interrupted", lock);
            return null;
        }
    }

    private <T> IMap<String, T> getMap(String name) {
        return hazelcastInstance.getMap(name);
    }

    @Override
    public <T> void modifyMap(String mapId, String key, Consumer<Map<String, T>> action) {
        IMap<String, T> map = getMap(mapId);
        map.lock(key, lockTimeout, TimeUnit.MILLISECONDS);
        try {
            action.accept(map);
        } finally {
            map.unlock(key);
        }
    }
    
    @Override
    public <I, O> O readFromMap(String mapId, String key, Function<Map<String, I>, O> function) {
        IMap<String, I> map = getMap(mapId);
        map.lock(key, lockTimeout, TimeUnit.MILLISECONDS);
        try {
            return function.apply(map);
        } finally {
            map.unlock(key);
        }
    }
    
    private void modifyProject(String projectId, Consumer<Map<String, Project>> action) {
        modifyMap(projectsById(), projectId, action);
    }

    private <T> T readProject(String projectId, Function<Map<String, Project>, T> function) {
        return readFromMap(projectsById(), projectId, function);
    }

    @Override
    public void saveProject(Project project) {
        modifyProject(project.getId(), map -> map.put(project.getId(), project));
    }

    @Override
    public Collection<Project> getProjects() {
        return getProjectsByIdMap().values();
    }

    @Override
    public Optional<Project> getProject(String projectId) {
        return readProject(projectId, map -> Optional.ofNullable(map.get(projectId)));
    }

    @Override
    public void saveInstance(Instance instance) {
        modifyInstance(instance.getId(), map -> map.put(instance.getId(), instance));
    }

    private void modifyInstance(String instanceId, Consumer<Map<String, Instance>> action) {
        modifyMap(instancesById(), instanceId, action);
    }

    private <T> T readInstance(String instanceId, Function<Map<String, Instance>, T> function) {
        return readFromMap(instancesById(), instanceId, function);
    }

    //TODO: add @Transactional annotation and respective aspect for Hazelcast transactions
    @Override
    public void deleteInstance(String instanceId) {
        modifyInstance(instanceId, map -> {
            Instance deletedInstance = map.remove(instanceId);
            getDeletedInstancesByIdMap().put(deletedInstance.getId(), deletedInstance);
        });
    }

    @Override
    public boolean isInstanceDeleted(String instanceId) {
        return readInstance(instanceId, map -> getDeletedInstancesByIdMap().containsKey(instanceId));
    }

    @Override
    public boolean instanceExists(String instanceId) {
        return readInstance(instanceId, map -> map.containsKey(instanceId));
    }

    @Override
    public Collection<Instance> getInstances() {
        return getInstancesByIdMap().values();
    }

    @Override
    public Optional<Instance> getInstance(String instanceId) {
        return readInstance(instanceId, map -> Optional.ofNullable(map.get(instanceId)));
    }


    private void modifyImage(String imageId, Consumer<Map<String, Image>> action) {
        modifyMap(imagesById(), imageId, action);
    }

    private <T> T readImage(String imageId, Function<Map<String, Image>, T> function) {
        return readFromMap(imagesById(), imageId, function);
    }

    @Override
    public boolean imageExists(String imageId) {
        return readImage(imageId, map -> getImagesByIdMap().containsKey(imageId));
    }

    @Override
    public Collection<Image> getImages() {
        return getImagesByIdMap().values();
    }

    @Override
    public Optional<Image> getImage(String imageId) {
        return readImage(imageId, map -> Optional.ofNullable(map.get(imageId)));
    }

    @Override
    public void saveImage(Image image) {
        modifyImage(image.getId(), map -> map.put(image.getId(), image));
    }

    @Override
    public boolean isImageDeleted(String imageId) {
        return readImage(imageId, map -> getDeletedImagesByIdMap().containsKey(imageId));
    }

    @Override
    public void deleteImage(String imageId) {
        modifyImage(imageId, map -> {
            Image deletedImage = map.remove(imageId);
            getDeletedImagesByIdMap().put(deletedImage.getId(), deletedImage);
        });
    }

    private IMap<String, Project> getProjectsByIdMap() {
        return getMap(projectsById());
    }

    private IMap<String, Instance> getInstancesByIdMap() {
        return getMap(instancesById());
    }

    private IMap<String, Image> getImagesByIdMap() {
        return getMap(imagesById());
    }

    private Map<String, Instance> getDeletedInstancesByIdMap() {
        return getMap(deletedInstancesByCloud());
    }

    private Map<String, Image> getDeletedImagesByIdMap() {
        return getMap(deletedImagesByCloud());
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        LOG.debug("Marking storage as not available because application context is stopping");
        isAvailable = false;
    }
}
