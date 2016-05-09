package org.meridor.perspective.framework.storage;

public interface EntityListener<T> {
    
    void onEvent(T entity, StorageEvent event);
    
}
