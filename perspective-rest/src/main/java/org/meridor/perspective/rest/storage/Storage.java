package org.meridor.perspective.rest.storage;


import com.hazelcast.core.HazelcastInstance;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.CloudType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;

@Component
public class Storage {

    private static final String LIST = "list";
    
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
    
    public Map<String, Object> getMap(String name) {
        return hazelcastClient.getMap(name);
    }
    
    @SuppressWarnings("unchecked")
    public void saveProject(CloudType cloudType, Project project) {
        String projectsKey = getProjectsKey(cloudType);
        Map<String, Object> projectsMap = new HashMap<>(getMap(projectsKey));
        if (projectsMap.containsKey(LIST)) {
            ((List<Project>) projectsMap.get(LIST)).add(project);
        } else {
            projectsMap.put(LIST, new ArrayList<Project>() {
                {
                    add(project);
                }
            });
        }
        projectsMap.put(project.getId(), project);
        getMap(projectsKey).putAll(projectsMap);
    }
    
    @SuppressWarnings("unchecked")
    public List<Project> getProjects(CloudType cloudType) {
        List<Project> projects = (List<Project>) getMap(getProjectsKey(cloudType)).get(LIST);
        return (projects != null) ? projects : Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public void saveInstance(CloudType cloudType, Instance instance) {
        String cloudKey = getCloudInstancesKey(cloudType);
        Map<String, Object> instancesMap = new HashMap<>(getMap(cloudKey));
        
        //Full list
        if (instancesMap.containsKey(LIST)) {
            ((List<Instance>) instancesMap.get(LIST)).add(instance);
        } else {
            instancesMap.put(LIST, new ArrayList<Instance>() {
                {
                    add(instance);
                }
            });
        }
        
        //Access by id
        String instanceKey = getInstanceKey(instance.getProjectId(), instance.getRegionId(), instance.getId());
        instancesMap.put(instanceKey, instance);
        
        //Access by project and region
        String regionKey = getRegionKey(instance.getProjectId(), instance.getRegionId());
        if (instancesMap.containsKey(regionKey)) {
            ((List<Instance>) instancesMap.get(regionKey)).add(instance);
        } else {
            instancesMap.put(regionKey, new ArrayList<Instance>() {
                {
                    add(instance);
                }
            });
        }
        getMap(cloudKey).putAll(instancesMap);
    }
    
    @SuppressWarnings("unchecked")
    public void deleteInstance(CloudType cloudType, Instance instance) {
        Map<String, Object> instancesMap = getMap(getCloudInstancesKey(cloudType));
        String instanceKey = getInstanceKey(instance.getProjectId(), instance.getRegionId(), instance.getId());
        String regionKey = getRegionKey(instance.getProjectId(), instance.getRegionId());
        instancesMap.remove(instanceKey);
        ((List<Instance>) instancesMap.get(regionKey)).remove(instance);
        ((List<Instance>) instancesMap.get(LIST)).remove(instance);
    }
    
    @SuppressWarnings("unchecked")
    public List<Instance> getInstances(CloudType cloudType, String projectId, String regionId) {
        String cloudInstancesKey = getCloudInstancesKey(cloudType);
        String regionKey = getRegionKey(projectId, regionId);
        List<Instance> instances = (List<Instance>) getMap(cloudInstancesKey).get(regionKey);
        return (instances != null) ? instances : Collections.emptyList();
    }
    
    @SuppressWarnings("unchecked")
    public Optional<Instance> getInstance(CloudType cloudType, String projectId, String regionId, String instanceId) {
        String cloudInstancesKey = getCloudInstancesKey(cloudType);
        String instanceKey = getInstanceKey(projectId, regionId, instanceId);
        return Optional.ofNullable((Instance) getMap(cloudInstancesKey).get(instanceKey));
    }
    
    private static String getProjectsKey(CloudType cloudType) {
        return "projects_" + cloudType;
    }
    
    private static String getRegionKey(String projectId, String regionId) {
        return "project_" + projectId + "_region_" + regionId; 
    }
    
    private static String getCloudInstancesKey(CloudType cloudType){
        return "cloud_" + cloudType.value() + "_instances";
    }
    
    private static String getInstanceKey(String projectId, String regionId, String instanceId) {
        return getRegionKey(projectId, regionId) + "_instance_" + instanceId; 
    }
}
