package org.meridor.perspective.rest.storage;


import com.hazelcast.core.HazelcastInstance;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Storage {

    @Autowired
    private HazelcastInstance hazelcastClient;
    
    @SuppressWarnings("unchecked")
    public List<Project> getProjects() {
        return (List<Project>) hazelcastClient.getMap("projects").get("list");
    }

    @SuppressWarnings("unchecked")
    public List<Instance> getInstances(String projectId, String regionId) {
        return (List<Instance>) hazelcastClient.getMap("instances").get(getRegionKey(projectId, regionId));
    }
    
    private static String getRegionKey(String projectId, String regionId) {
        return "project_" + projectId + "_region_" + regionId; 
    }
    
    private static String getInstanceKey(String projectId, String regionId, String instanceId) {
        return getRegionKey(projectId, regionId) + "_instance_" + instanceId; 
    }
}
