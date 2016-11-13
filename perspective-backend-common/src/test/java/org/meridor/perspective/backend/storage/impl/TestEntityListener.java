package org.meridor.perspective.backend.storage.impl;

import org.meridor.perspective.backend.storage.EntityListener;
import org.meridor.perspective.backend.storage.StorageEvent;

import java.util.ArrayList;
import java.util.List;

public class TestEntityListener<T> implements EntityListener<T> {

    private List<T> entities = new ArrayList<>();
    private List<T> previousEntities = new ArrayList<>();
    private List<StorageEvent> events = new ArrayList<>();
    
    @Override
    public void onEvent(T entity, T previousEntity, StorageEvent event) {
        entities.add(entity);
        previousEntities.add(previousEntity);
        events.add(event);
    }

    public List<T> getEntities() {
        return entities;
    }

    public List<T> getPreviousEntities() {
        return previousEntities;
    }

    public List<StorageEvent> getEvents() {
        return events;
    }
}
