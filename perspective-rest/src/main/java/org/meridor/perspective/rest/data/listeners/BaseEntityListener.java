package org.meridor.perspective.rest.data.listeners;

import org.meridor.perspective.backend.storage.EntityListener;
import org.meridor.perspective.backend.storage.StorageEvent;
import org.meridor.perspective.sql.impl.index.Indexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.stream.Stream;

@Component
public abstract class BaseEntityListener<T> implements EntityListener<T> {
    
    @Autowired
    private Indexer indexer;

    protected <I> void updateEntity(StorageEvent event, String tableName, I entity, I oldEntity) {
        switch (event) {
            case ADDED: {
                indexer.add(tableName, entity);
                break;
            }
            case MODIFIED: {
                indexer.delete(tableName, oldEntity);
                indexer.add(tableName, entity);
                break;
            }
            case DELETED:
            case EVICTED:{
                indexer.delete(tableName, entity);
                break;
            }
        }
    }
    
    protected <I, O> void updateDerivedEntities(StorageEvent event, String tableName, I entity, I oldEntity, Function<I, Stream<O>> mapper) {
        Stream<O> derivedEntities = entity != null ? mapper.apply(entity) : Stream.empty();
        Stream<O> oldDerivedEntities = oldEntity != null ? mapper.apply(oldEntity) : Stream.empty();
        switch (event) {
            case ADDED: {
                derivedEntities.forEach(de -> indexer.add(tableName, de));
                break;
            }
            case MODIFIED: {
                oldDerivedEntities.forEach(ode -> indexer.delete(tableName, ode));
                derivedEntities.forEach(de -> indexer.add(tableName, de));
                break;
            }
            case DELETED:
            case EVICTED:{
                derivedEntities.forEach(de -> indexer.delete(tableName, de));
                break;
            }
        }
    }
    
}
