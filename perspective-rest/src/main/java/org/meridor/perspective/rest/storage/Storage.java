package org.meridor.perspective.rest.storage;


import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.core.HazelcastInstance;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.CloudType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
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
    
    public <T> Map<String, T> getMap(String name) {
        return getMapWithConfig(name, null);
    }
    
    public <T> Map<String, T> getLRUMap(String name) {
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

    public <T> Map<String, T> getIdleMap(String name) {
        MapConfig mapConfig = new MapConfig();
        mapConfig.setName(name);
        mapConfig.setMaxIdleSeconds(30);
        return getMapWithConfig(name, mapConfig);
    }

    private <T> Map<String, T> getMapWithConfig(String name, MapConfig mapConfig) {
        if (isAvailable()) {
            if (mapConfig != null) {
                hazelcastInstance.getConfig().addMapConfig(mapConfig);
            }
            return hazelcastInstance.getMap(name);
        }
        return Collections.emptyMap();
    }
    
    public void saveProject(Project project) {
        CloudType cloudType = project.getCloudType();
        getProjectsByIdMap(cloudType).put(project.getId(), project);
    }
    
    public Collection<Project> getProjects(CloudType cloudType) {
        return getProjectsByIdMap(cloudType).values();
    }

    public Optional<Project> getProject(CloudType cloudType, String projectId) {
        return Optional.ofNullable(getProjectsByIdMap(cloudType).get(projectId));
    }

    public void saveInstance(Instance instance) {
        CloudType cloudType = instance.getCloudType();
        getInstancesByIdMap(cloudType).put(instance.getId(), instance);
        getInstancesByProject(cloudType, instance.getProjectId()).put(instance.getId(), instance);
    }
    
    //TODO: add @Transactional annotation and respective aspect for Hazelcast transactions
    public void deleteInstance(Instance instance) {
        CloudType cloudType = instance.getCloudType();
        Instance deletedInstance = getInstancesByIdMap(cloudType).remove(instance.getId());
        getInstancesByProject(cloudType, instance.getProjectId()).remove(instance.getId());
        getDeletedInstancesByIdMap(cloudType).put(deletedInstance.getId(), deletedInstance);
    }
    
    public boolean isInstanceDeleted(CloudType cloudType, String instanceId) {
        return getDeletedInstancesByIdMap(cloudType).containsKey(instanceId);
    }
    
    public boolean instanceExists(Instance instance) {
        String instanceId = instance.getId();
        CloudType cloudType = instance.getCloudType();
        return getInstancesByIdMap(cloudType).containsKey(instanceId);
    }
    
    public Collection<Instance> getInstances(CloudType cloudType, String projectId) {
        return getInstancesByProject(cloudType, projectId).values();
    }
    
    public Optional<Instance> getInstance(CloudType cloudType, String instanceId) {
        return Optional.ofNullable(getInstancesByIdMap(cloudType).get(instanceId));
    }
    
    private Map<String, Project> getProjectsByIdMap(CloudType cloudType) {
        return getMap(projectsByCloud(cloudType));
    }
    
    private Map<String, Instance> getInstancesByProject(CloudType cloudType, String projectId) {
        return getMap(instancesSetByProject(cloudType, projectId));
    }

    private Map<String, Instance> getInstancesByIdMap(CloudType cloudType) {
        return getMap(instancesByCloud(cloudType));
    }
    
    private Map<String, Instance> getDeletedInstancesByIdMap(CloudType cloudType) {
        return getLRUMap(deletedInstancesByCloud(cloudType));
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        LOG.debug("Marking storage as not available because application context is stopping");
        isAvailable = false;
    }
}
