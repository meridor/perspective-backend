package org.meridor.perspective.shell.common.events;

public interface EventListener<T> {
    
    void onEvent(T event);
    
}
