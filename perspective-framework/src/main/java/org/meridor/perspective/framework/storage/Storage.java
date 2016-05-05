package org.meridor.perspective.framework.storage;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Storage {

    boolean isAvailable();

    <T> BlockingQueue<T> getQueue(String id);

    Lock getLock(String name);
    
    <T> T executeSynchronized(String lockName, long timeout, Supplier<T> action);

    <T> void modifyMap(String mapId, String key, Consumer<Map<String, T>> action);

    <I, O> O readFromMap(String mapId, String key, Function<Map<String, I>, O> function);

}
