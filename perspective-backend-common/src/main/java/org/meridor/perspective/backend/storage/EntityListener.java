package org.meridor.perspective.backend.storage;

public interface EntityListener<T> {
    
    void onEvent(T entity, T previousEntity, StorageEvent event);
    
}
