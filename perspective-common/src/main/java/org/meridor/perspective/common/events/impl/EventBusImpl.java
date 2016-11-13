package org.meridor.perspective.common.events.impl;

import org.meridor.perspective.common.events.EventBus;
import org.meridor.perspective.common.events.EventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBusImpl implements EventBus {
    
    private final Map<Class<?>, List<EventListener<Object>>> recipients = new HashMap<>();

    @Override
    public <T> void addListener(Class<T> eventClass, EventListener<T> listener) {
        if (!recipients.containsKey(eventClass)) {
            recipients.put(eventClass, new ArrayList<>());
        }
        @SuppressWarnings("unchecked")
        EventListener<Object> l = (EventListener<Object>) listener;
        recipients.get(eventClass).add(l);
    }

    @Override
    public void fire(Object event) {
        Class<?> eventClass = event.getClass();
        if (recipients.containsKey(eventClass)) {
            recipients.get(eventClass).forEach(l -> l.onEvent(event));
        }
    }
}
