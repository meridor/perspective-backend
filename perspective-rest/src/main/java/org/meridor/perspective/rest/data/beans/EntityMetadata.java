package org.meridor.perspective.rest.data.beans;

public abstract class EntityMetadata {
    private final String entityId;
    private final String key;
    private final String value;

    public EntityMetadata(String entityId, String key, String value) {
        this.entityId = entityId;
        this.key = key;
        this.value = value;
    }

    public String getId() {
        return entityId;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
