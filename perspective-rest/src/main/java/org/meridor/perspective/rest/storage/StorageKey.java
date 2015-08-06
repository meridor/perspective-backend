package org.meridor.perspective.rest.storage;

import org.meridor.perspective.config.CloudType;

public final class StorageKey {

    public static final String DELETED_INSTANCES = "deleted_instances";
    
    public static String projectsByCloud(CloudType cloudType) {
        return "projects_" + cloudType;
    }

    public static String projectsSetByCloud(CloudType cloudType) {
        return "projects_set_" + cloudType;
    }

    public static String instancesSetByProject(CloudType cloudType, String projectId) {
        return cloudType + "_project_" + projectId;
    }

    public static String instancesByCloud(CloudType cloudType){
        return "instances_" + cloudType;
    }
    
    public static String deletedInstancesByCloud(CloudType cloudType){
        return "deleted_instances_" + cloudType;
    }

    public static String instancesSetByCloud(CloudType cloudType){
        return "instances_set_" + cloudType;
    }


    private StorageKey(){}
}
