package org.meridor.perspective.rest.storage;


import com.hazelcast.core.HazelcastInstance;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.CloudType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;

import static org.meridor.perspective.rest.storage.StorageKey.*;

@Component
public class Storage {

    @Autowired
    private HazelcastInstance hazelcastClient;
    
    public boolean isAvailable() {
        return hazelcastClient.getLifecycleService().isRunning();
    }
    
    public BlockingQueue<Object> getQueue(String name) {
        return hazelcastClient.getQueue(name);
    }
    
    public Lock getLock(String name) {
        return hazelcastClient.getLock(name);
    }
    
    public <T> Map<String, T> getMap(String name) {
        return hazelcastClient.getMap(name);
    }
    
    public <T> Set<T> getSet(String name) {
        return hazelcastClient.getSet(name);
    }
    
    public void saveProject(Project project) {
        CloudType cloudType = project.getCloudType();
        getProjectsByIdMap(cloudType).put(project.getId(), project);
        getProjectsSet(cloudType).add(project);
    }
    
    public Set<Project> getProjects(CloudType cloudType) {
        return getProjectsSet(cloudType);
    }

    public Optional<Project> getProject(CloudType cloudType, String projectId) {
        return Optional.ofNullable(getProjectsByIdMap(cloudType).get(projectId));
    }

    public void saveInstance(Instance instance) {
        CloudType cloudType = instance.getCloudType();
        getInstancesByIdMap(cloudType).put(instance.getId(), instance);
        getInstancesSet(cloudType).add(instance);
        getInstancesSetByProjectAndRegion(cloudType, instance.getProjectId(), instance.getRegionId()).add(instance);
    }
    
    public void deleteInstance(Instance instance) {
        CloudType cloudType = instance.getCloudType();
        getInstancesByIdMap(cloudType).remove(instance.getId());
        getInstancesSet(cloudType).remove(instance);
        getInstancesSetByProjectAndRegion(cloudType, instance.getProjectId(), instance.getRegionId()).remove(instance);
    }
    
    public boolean instanceExists(Instance instance) {
        String instanceId = instance.getId();
        CloudType cloudType = instance.getCloudType();
        return getInstancesByIdMap(cloudType).containsKey(instanceId);
    }
    
    public Set<Instance> getInstances(CloudType cloudType, String projectId, String regionId) {
        return getInstancesSetByProjectAndRegion(cloudType, projectId, regionId);
    }
    
    public Optional<Instance> getInstance(CloudType cloudType, String instanceId) {
        return Optional.ofNullable(getInstancesByIdMap(cloudType).get(instanceId));
    }
    
    private Set<Project> getProjectsSet(CloudType cloudType) {
        return getSet(projectsSetByCloud(cloudType));
    }
    
    private Map<String, Project> getProjectsByIdMap(CloudType cloudType) {
        return getMap(projectsByCloud(cloudType));
    }
    
    private Set<Instance> getInstancesSet(CloudType cloudType) {
        return getSet(instancesSetByCloud(cloudType));
    }
    
    private Set<Instance> getInstancesSetByProjectAndRegion(CloudType cloudType, String projectId, String regionId) {
        return getSet(instancesSetByProjectAndRegion(cloudType, projectId, regionId));
    }

    private Map<String, Instance> getInstancesByIdMap(CloudType cloudType) {
        return getMap(instancesByCloud(cloudType));
    }
    
}
