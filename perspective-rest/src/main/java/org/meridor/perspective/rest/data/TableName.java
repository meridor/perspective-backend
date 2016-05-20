package org.meridor.perspective.rest.data;

public enum TableName {
    
    AVAILABILITY_ZONES,
    CLOUDS,
    FLAVORS,
    IMAGES,
    IMAGE_METADATA,
    INSTANCES,
    INSTANCE_NETWORKS,
    INSTANCE_METADATA,
    KEYPAIRS,
    NETWORKS,
    NETWORK_SUBNETS,
    PROJECTS,
    PROJECT_IMAGES,
    PROJECT_METADATA;
    
    public String getTableName() {
        return name().toLowerCase();
    }
    
}
