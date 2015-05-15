package org.meridor.perspective.rest.storage;


import com.hazelcast.core.HazelcastInstance;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Storage {

    private static final String PROJECTS = "projects";
    private static final String INSTANCES = "instances";
    private static final String LIST = "list";
    
    @Autowired
    private HazelcastInstance hazelcastClient;
    
    public void saveProjects(List<Project> projects) {
        Map<String, Object> projectsMap = new HashMap<>();
        projectsMap.put(LIST, projects);
        projects.stream().forEach(p -> projectsMap.put(p.getId(), p));
        hazelcastClient.getMap(PROJECTS).putAll(projectsMap);
    }
    
    @SuppressWarnings("unchecked")
    public List<Project> getProjects() {
        List<Project> projects = (List<Project>) hazelcastClient.getMap(PROJECTS).get(LIST);
        return (projects != null) ? projects : Collections.emptyList();
    }

    public void saveInstances(List<Instance> instances) {
        Map<String, Object> instancesMap = new HashMap<>();
        instancesMap.put(LIST, instances);
        Map<String, List<Instance>> instancesByProjectAndRegion = new HashMap<>();
        instances.stream().forEach(i -> {
            String regionKey = getRegionKey(i.getProjectId(), i.getRegionId());
            String instanceKey = getInstanceKey(i.getProjectId(), i.getRegionId(), i.getId());
            instancesMap.put(instanceKey, i);
            if (instancesByProjectAndRegion.containsKey(regionKey)) {
                instancesByProjectAndRegion.get(regionKey).add(i);
            } else {
                instancesByProjectAndRegion.put(regionKey, new ArrayList<Instance>(){
                    {
                        add(i);
                    }
                });
            }
        });
        instancesMap.putAll(instancesByProjectAndRegion);
        hazelcastClient.getMap(INSTANCES).putAll(instancesMap);
    }
    
    @SuppressWarnings("unchecked")
    public List<Instance> getInstances(String projectId, String regionId) {
        List<Instance> instances = (List<Instance>) hazelcastClient.getMap(INSTANCES).get(getRegionKey(projectId, regionId));
        return (instances != null) ? instances : Collections.emptyList();
    }
    
    @SuppressWarnings("unchecked")
    public Optional<Instance> getInstance(String projectId, String regionId, String instanceId) {
        return Optional.ofNullable((Instance) hazelcastClient.getMap(INSTANCES).get(getInstanceKey(projectId, regionId, instanceId)));
    }
    
    private static String getRegionKey(String projectId, String regionId) {
        return "project_" + projectId + "_region_" + regionId; 
    }
    
    private static String getInstanceKey(String projectId, String regionId, String instanceId) {
        return getRegionKey(projectId, regionId) + "_instance_" + instanceId; 
    }
}
