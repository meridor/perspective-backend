package org.meridor.perspective.common.events;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

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
        forEachListener(event, l -> l.onEvent(event));
    }

    @Override
    public void fireAsync(Object event) {
        forEachListener(event, l -> executorService.submit(
                () -> l.onEvent(event)
        ));
    }

    private void forEachListener(Object event, Consumer<EventListener<Object>> action) {
        Class<?> eventClass = event.getClass();
        if (recipients.containsKey(eventClass)) {
            initExecutorServiceIfNeeded();
            recipients.getOrDefault(eventClass, Collections.emptyList())
                    .forEach(action);
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
