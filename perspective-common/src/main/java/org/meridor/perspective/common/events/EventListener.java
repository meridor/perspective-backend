package org.meridor.perspective.common.events;

public interface EventListener<T> {
    
    void onEvent(T event);
    
}
