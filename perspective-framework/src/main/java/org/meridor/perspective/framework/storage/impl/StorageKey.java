package org.meridor.perspective.framework.storage.impl;

import org.meridor.perspective.config.CloudType;

public final class StorageKey {

    public static final String DELETED_INSTANCES = "deleted_instances";

    public static String projectsById() {
        return "projects_by_id";
    }

    public static String projectsSetByCloud(CloudType cloudType) {
        return "projects_set_" + cloudType;
    }

    public static String instancesSetByProject(CloudType cloudType, String projectId) {
        return cloudType + "_project_" + projectId;
    }

    public static String instancesById() {
        return "instances_by_id";
    }

    public static String deletedInstancesByCloud() {
        return "deleted-instances";
    }

    public static String instancesSetByCloud(CloudType cloudType) {
        return "instances_set_" + cloudType;
    }


    private StorageKey() {
    }
}
