package org.meridor.perspective.rest.storage;


import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.SqlPredicate;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.rest.storage.impl.EmptyIMap;
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
import java.util.concurrent.locks.Lock;

import static org.meridor.perspective.rest.storage.StorageKey.*;

@Component
public class Storage implements ApplicationListener<ContextClosedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(Storage.class);
    
    @Autowired
    private HazelcastInstance hazelcastInstance;
    
    private volatile boolean isAvailable = true;
    
    public boolean isAvailable() {
        return isAvailable && hazelcastInstance.getLifecycleService().isRunning();
    }
    
    public BlockingQueue<Object> getQueue(String name) {
        return isAvailable() ? 
                hazelcastInstance.getQueue(name) :
                new LinkedBlockingQueue<>();
    }
    
    public Lock getLock(String name) {
        return hazelcastInstance.getLock(name);
    }
    
    private <T> IMap<String, T> getMap(String name) {
        return getMapWithConfig(name, null);
    }
    
    private <T> IMap<String, T> getLRUMap(String name) {
        MapConfig mapConfig = new MapConfig();
        mapConfig.setName(name);
        mapConfig.setEvictionPolicy(EvictionPolicy.LRU);
        MaxSizeConfig maxSizeConfig = new MaxSizeConfig();
        maxSizeConfig.setMaxSizePolicy(MaxSizeConfig.MaxSizePolicy.USED_HEAP_SIZE);
        maxSizeConfig.setSize(70);
        mapConfig.setMaxSizeConfig(maxSizeConfig);
        hazelcastInstance.getConfig().addMapConfig(mapConfig);
        return getMap(name);
    }

    private <T> Map<String, T> getIdleMap(String name) {
        MapConfig mapConfig = new MapConfig();
        mapConfig.setName(name);
        mapConfig.setMaxIdleSeconds(30);
        return getMapWithConfig(name, mapConfig);
    }

    private <T> IMap<String, T> getMapWithConfig(String name, MapConfig mapConfig) {
        if (isAvailable()) {
            if (mapConfig != null) {
                hazelcastInstance.getConfig().addMapConfig(mapConfig);
            }
            return hazelcastInstance.getMap(name);
        }
        return new EmptyIMap<>();
    }
    
    public void saveProject(Project project) {
        getProjectsByIdMap().put(project.getId(), project);
    }
    
    public Collection<Project> getProjects(Optional<String> query) throws IllegalQueryException {
        if (query.isPresent() && !query.get().isEmpty()) {
            Predicate predicate = getPredicateFromQuery(query.get());
            return getProjectsByIdMap().values(predicate);
        }
        return getProjectsByIdMap().values();
    }

    public Optional<Project> getProject(String projectId) {
        return Optional.ofNullable(getProjectsByIdMap().get(projectId));
    }

    public void saveInstance(Instance instance) {
        getInstancesByIdMap().put(instance.getId(), instance);
    }
    
    //TODO: add @Transactional annotation and respective aspect for Hazelcast transactions
    public void deleteInstance(Instance instance) {
        Instance deletedInstance = getInstancesByIdMap().remove(instance.getId());
        getDeletedInstancesByIdMap().put(deletedInstance.getId(), deletedInstance);
    }
    
    public boolean isInstanceDeleted(String instanceId) {
        return getDeletedInstancesByIdMap().containsKey(instanceId);
    }
    
    public boolean instanceExists(Instance instance) {
        String instanceId = instance.getId();
        return getInstancesByIdMap().containsKey(instanceId);
    }
    
    public Collection<Instance> getInstances(Optional<String> query) throws IllegalQueryException {
        if (query.isPresent() && !query.get().isEmpty()) {
            Predicate predicate = getPredicateFromQuery(query.get());
            return getInstancesByIdMap().values(predicate);
        }
        return getInstancesByIdMap().values();
    }

    public Optional<Instance> getInstance(String instanceId) {
        return Optional.ofNullable(getInstancesByIdMap().get(instanceId));
    }
    
    private IMap<String, Project> getProjectsByIdMap() {
        return getMap(projectsById());
    }
    
    private IMap<String, Instance> getInstancesByIdMap() {
        return getMap(instancesById());
    }
    
    private Map<String, Instance> getDeletedInstancesByIdMap() {
        return getLRUMap(deletedInstancesByCloud());
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
