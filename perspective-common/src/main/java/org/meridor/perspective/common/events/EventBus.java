package org.meridor.perspective.common.events;

public interface EventBus {

    <T> void addListener(Class<T> eventClass, EventListener<T> listener);
    
    void fire(Object event);
    
}
