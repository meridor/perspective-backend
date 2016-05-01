package org.meridor.perspective.rest.data;

import java.util.Arrays;
import java.util.Optional;

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
    
    public static TableName fromString(String name) {
        Optional<TableName> tableNameCandidate = Arrays.asList(TableName.values()).stream()
                .filter(v -> v.name().equalsIgnoreCase(name)).findFirst();
        if (!tableNameCandidate.isPresent()) {
            throw new IllegalArgumentException(String.format("Table \"%s\" does not exist", name));
        }
        return tableNameCandidate.get();
    }
}
