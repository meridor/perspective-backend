package org.meridor.perspective.common.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;

public abstract class AbstractEventBus implements EventBus {
    
    private final Map<Class<?>, List<EventListener<Object>>> recipients = new HashMap<>();
    private ExecutorService executorService;

    @Override
    public <T> void addListener(Class<T> eventClass, EventListener<T> listener) {
        recipients.putIfAbsent(eventClass, new ArrayList<>());
        @SuppressWarnings("unchecked")
        EventListener<Object> l = (EventListener<Object>) listener;
        recipients.get(eventClass).add(l);
    }

    @Override
    public void fire(Object event) {
        Class<?> eventClass = event.getClass();
        if (recipients.containsKey(eventClass)) {
            initExecutorServiceIfNeeded();
            recipients.get(eventClass)
                    .forEach(l -> executorService.submit(
                            () -> l.onEvent(event)
                    ));
        }
    }

    private void initExecutorServiceIfNeeded() {
        if (executorService == null) {
            executorService = newFixedThreadPool(getParallelConsumers());
        }
    }

    protected abstract int getParallelConsumers();

    protected void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }
}
