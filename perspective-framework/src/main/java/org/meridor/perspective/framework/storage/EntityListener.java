package org.meridor.perspective.framework.storage;

public interface EntityListener<T> {
    
    void onEvent(T entity, T previousEntity, StorageEvent event);
    
}
