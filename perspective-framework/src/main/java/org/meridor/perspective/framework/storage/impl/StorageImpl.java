package org.meridor.perspective.framework.storage.impl;


import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.SqlPredicate;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.framework.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.function.Supplier;

import static org.meridor.perspective.framework.storage.impl.StorageKey.*;

@Component
public class StorageImpl implements ApplicationListener<ContextClosedEvent>, InstancesAware, ProjectsAware, ImagesAware, Storage {

    private static final Logger LOG = LoggerFactory.getLogger(StorageImpl.class);

    @Autowired
    private HazelcastInstance hazelcastInstance;

    private volatile boolean isAvailable = true;

    @Override
    public boolean isAvailable() {
        return isAvailable && hazelcastInstance.getLifecycleService().isRunning();
    }

    @Override
    public <T> BlockingQueue<T> getQueue(String name) {
        return isAvailable() ?
                hazelcastInstance.<T>getQueue(name) :
                new LinkedBlockingQueue<T>();
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
    public void saveProject(Project project) {
        getProjectsByIdMap().put(project.getId(), project);
    }

    @Override
    public Collection<Project> getProjects(Optional<String> query) throws IllegalQueryException {
        if (query.isPresent() && !query.get().isEmpty()) {
            Predicate predicate = getPredicateFromQuery(query.get());
            return getProjectsByIdMap().values(predicate);
        }
        return getProjectsByIdMap().values();
    }

    @Override
    public Optional<Project> getProject(String projectId) {
        return Optional.ofNullable(getProjectsByIdMap().get(projectId));
    }

    @Override
    public void saveInstance(Instance instance) {
        getInstancesByIdMap().put(instance.getId(), instance);
    }

    //TODO: add @Transactional annotation and respective aspect for Hazelcast transactions
    @Override
    public void deleteInstance(String instanceId) {
        Instance deletedInstance = getInstancesByIdMap().remove(instanceId);
        getDeletedInstancesByIdMap().put(deletedInstance.getId(), deletedInstance);
    }

    @Override
    public boolean isInstanceDeleted(String instanceId) {
        return getDeletedInstancesByIdMap().containsKey(instanceId);
    }

    @Override
    public boolean instanceExists(String instanceId) {
        return getInstancesByIdMap().containsKey(instanceId);
    }

    @Override
    public Collection<Instance> getInstances(Optional<String> query) throws IllegalQueryException {
        if (query.isPresent() && !query.get().isEmpty()) {
            Predicate predicate = getPredicateFromQuery(query.get());
            return getInstancesByIdMap().values(predicate);
        }
        return getInstancesByIdMap().values();
    }

    @Override
    public Optional<Instance> getInstance(String instanceId) {
        return Optional.ofNullable(getInstancesByIdMap().get(instanceId));
    }

    @Override
    public boolean imageExists(String imageId) {
        return getImagesByIdMap().containsKey(imageId);
    }

    @Override
    public Collection<Image> getImages(Optional<String> query) throws IllegalQueryException {
        if (query.isPresent() && !query.get().isEmpty()) {
            Predicate predicate = getPredicateFromQuery(query.get());
            return getImagesByIdMap().values(predicate);
        }
        return getImagesByIdMap().values();
    }

    @Override
    public Optional<Image> getImage(String imageId) {
        return Optional.ofNullable(getImagesByIdMap().get(imageId));
    }

    @Override
    public void saveImage(Image image) {
        getImagesByIdMap().put(image.getId(), image);
    }

    @Override
    public boolean isImageDeleted(String imageId) {
        return getDeletedImagesByIdMap().containsKey(imageId);
    }

    @Override
    public void deleteImage(String imageId) {
        Image deletedImage = getImagesByIdMap().remove(imageId);
        getDeletedImagesByIdMap().put(deletedImage.getId(), deletedImage);
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

    private Predicate getPredicateFromQuery(String query) throws IllegalQueryException {
        try {
            return new SqlPredicate(query);
        } catch (Exception e) {
            throw new IllegalQueryException(e);
        }
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        LOG.debug("Marking storage as not available because application context is stopping");
        isAvailable = false;
    }
}
